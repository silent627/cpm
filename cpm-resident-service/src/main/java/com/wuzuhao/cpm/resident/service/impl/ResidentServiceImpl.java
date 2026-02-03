package com.wuzuhao.cpm.resident.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuzuhao.cpm.common.Result;
import com.wuzuhao.cpm.resident.entity.Resident;
import com.wuzuhao.cpm.resident.feign.UserServiceClient;
import com.wuzuhao.cpm.resident.mapper.ResidentMapper;
import com.wuzuhao.cpm.resident.feign.FileServiceClient;
import com.wuzuhao.cpm.common.dto.ESSyncMessage;
import com.wuzuhao.cpm.config.RabbitMQConfig;
import com.wuzuhao.cpm.resident.service.ResidentService;
import com.wuzuhao.cpm.util.RedisUtil;
import com.wuzuhao.cpm.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 居民服务实现类
 */
@Service
public class ResidentServiceImpl extends ServiceImpl<ResidentMapper, Resident> implements ResidentService {

    private static final String CACHE_PREFIX = "resident:";
    private static final String CACHE_BY_ID = CACHE_PREFIX + "id:";
    private static final String CACHE_BY_USER_ID = CACHE_PREFIX + "userId:";
    private static final String CACHE_BY_ID_CARD = CACHE_PREFIX + "idCard:";
    private static final long CACHE_EXPIRE_TIME = 3600; // 缓存过期时间：1小时

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    @Lazy
    private FileServiceClient fileServiceClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final Logger log = LoggerFactory.getLogger(ResidentServiceImpl.class);

    public Resident getById(Long id) {
        if (id == null) {
            return null;
        }
        String cacheKey = CACHE_BY_ID + id;
        Resident resident = (Resident) redisUtil.get(cacheKey);
        if (resident != null) {
            return resident;
        }
        resident = super.getById(id);
        if (resident != null) {
            redisUtil.set(cacheKey, resident, CACHE_EXPIRE_TIME);
        }
        return resident;
    }

    @Override
    public Resident getByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        String cacheKey = CACHE_BY_USER_ID + userId;
        Resident resident = (Resident) redisUtil.get(cacheKey);
        if (resident != null) {
            return resident;
        }
        LambdaQueryWrapper<Resident> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Resident::getUserId, userId);
        resident = this.getOne(wrapper);
        if (resident != null) {
            redisUtil.set(cacheKey, resident, CACHE_EXPIRE_TIME);
            // 同时缓存按ID查询的结果
            redisUtil.set(CACHE_BY_ID + resident.getId(), resident, CACHE_EXPIRE_TIME);
        }
        return resident;
    }

    @Override
    public Resident getByIdCard(String idCard) {
        if (idCard == null || idCard.trim().isEmpty()) {
            return null;
        }
        String cacheKey = CACHE_BY_ID_CARD + idCard;
        Resident resident = (Resident) redisUtil.get(cacheKey);
        if (resident != null) {
            return resident;
        }
        LambdaQueryWrapper<Resident> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Resident::getIdCard, idCard);
        resident = this.getOne(wrapper);
        if (resident != null) {
            redisUtil.set(cacheKey, resident, CACHE_EXPIRE_TIME);
            // 同时缓存按ID和userId查询的结果
            redisUtil.set(CACHE_BY_ID + resident.getId(), resident, CACHE_EXPIRE_TIME);
            if (resident.getUserId() != null) {
                redisUtil.set(CACHE_BY_USER_ID + resident.getUserId(), resident, CACHE_EXPIRE_TIME);
            }
        }
        return resident;
    }

    /**
     * 清除居民相关缓存
     */
    private void clearResidentCache(Resident resident) {
        if (resident != null) {
            redisUtil.del(CACHE_BY_ID + resident.getId());
            if (resident.getUserId() != null) {
                redisUtil.del(CACHE_BY_USER_ID + resident.getUserId());
            }
            if (resident.getIdCard() != null) {
                redisUtil.del(CACHE_BY_ID_CARD + resident.getIdCard());
            }
        }
        // 清除统计缓存（统计数据会因居民数据变化而失效）
        redisUtil.deleteByPattern("statistics:*");
    }

    @Override
    public Resident createResident(Long userId, Resident resident) {
        // 验证身份证号格式
        if (resident.getIdCard() == null || resident.getIdCard().trim().isEmpty()) {
            throw new RuntimeException("身份证号不能为空");
        }
        if (!ValidationUtil.isValidIdCard(resident.getIdCard())) {
            throw new RuntimeException("身份证号格式不正确，应为18位数字");
        }

        // 检查身份证号是否已存在
        Resident existResident = getByIdCard(resident.getIdCard());
        if (existResident != null) {
            throw new RuntimeException("身份证号已存在");
        }

        // 验证联系电话格式
        if (resident.getContactPhone() != null && !resident.getContactPhone().trim().isEmpty()) {
            if (!ValidationUtil.isValidPhone(resident.getContactPhone())) {
                throw new RuntimeException("联系电话格式不正确，应为11位数字且以1开头");
            }
        }

        // 验证紧急联系人电话格式
        if (resident.getEmergencyPhone() != null && !resident.getEmergencyPhone().trim().isEmpty()) {
            if (!ValidationUtil.isValidPhone(resident.getEmergencyPhone())) {
                throw new RuntimeException("紧急联系人电话格式不正确，应为11位数字且以1开头");
            }
        }

        // 如果realName为空，从User服务获取
        if (resident.getRealName() == null || resident.getRealName().isEmpty()) {
            Result<Object> userResult = userServiceClient.getUserById(userId);
            if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> userData = (Map<String, Object>) userResult.getData();
                if (userData.get("realName") != null) {
                    resident.setRealName(userData.get("realName").toString());
                }
            }
        }

        resident.setUserId(userId);
        // createTime 和 updateTime 由 MyBatis-Plus 自动填充
        this.save(resident);
        // 保存后清除相关缓存（虽然新数据可能不在缓存中，但为了保持一致性）
        clearResidentCache(resident);
        // 发送 ES 同步消息
        sendESSyncMessage(ESSyncMessage.create("resident_index", resident.getId(), convertToMap(resident)));
        return resident;
    }

    @Override
    public boolean updateById(Resident resident) {
        // 更新前获取旧数据，用于清除缓存和删除旧图片
        Resident oldResident = null;
        if (resident.getId() != null) {
            oldResident = super.getById(resident.getId());
        }
        
        // 如果更新了头像或身份证照片，删除旧文件
        if (oldResident != null) {
            try {
                // 处理头像更新
                fileServiceClient.handleFileUpdate(oldResident.getAvatar(), resident.getAvatar());
                // 处理身份证照片更新
                fileServiceClient.handleFileUpdate(oldResident.getIdCardPhoto(), resident.getIdCardPhoto());
            } catch (Exception e) {
                // 删除失败不影响更新，记录日志即可
                org.slf4j.LoggerFactory.getLogger(ResidentServiceImpl.class)
                    .warn("处理文件更新失败: {}", e.getMessage(), e);
            }
        }
        
        boolean result = super.updateById(resident);
        if (result) {
            // 清除相关缓存
            clearResidentCache(oldResident);
            clearResidentCache(resident);
            // 发送 ES 同步消息
            sendESSyncMessage(ESSyncMessage.update("resident_index", resident.getId(), convertToMap(resident)));
        }
        return result;
    }

    @Override
    public boolean removeById(java.io.Serializable id) {
        Long residentId = id instanceof Long ? (Long) id : Long.valueOf(id.toString());
        log.info("开始删除居民，id: {}", residentId);
        // 删除前获取数据，用于清除缓存
        Resident resident = super.getById(residentId);
        if (resident == null) {
            log.warn("要删除的居民不存在，id: {}", residentId);
            return false;
        }
        boolean result = super.removeById(id);
        log.info("数据库删除操作完成，id: {}, 结果: {}", residentId, result);
        if (result) {
            clearResidentCache(resident);
            // 发送 ES 同步消息
            log.info("居民删除成功，准备发送 ES 删除消息，id: {}", residentId);
            sendESSyncMessage(ESSyncMessage.delete("resident_index", residentId));
            log.info("ES 删除消息已发送，id: {}", residentId);
        } else {
            log.warn("数据库删除失败，id: {}", residentId);
        }
        return result;
    }
    
    /**
     * 发送 ES 同步消息
     */
    private void sendESSyncMessage(ESSyncMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.RESIDENT_SYNC_EXCHANGE,
                RabbitMQConfig.RESIDENT_SYNC_ROUTING_KEY,
                message
            );
        } catch (Exception e) {
            // 消息发送失败不影响主流程，只记录日志
            org.slf4j.LoggerFactory.getLogger(ResidentServiceImpl.class)
                .warn("发送 ES 同步消息失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 将 Resident 对象转换为 Map
     */
    private Map<String, Object> convertToMap(Resident resident) {
        Map<String, Object> map = new HashMap<>();
        if (resident != null) {
            map.put("id", resident.getId());
            map.put("userId", resident.getUserId());
            map.put("realName", resident.getRealName());
            map.put("idCard", resident.getIdCard());
            map.put("gender", resident.getGender());
            // 将 LocalDate 转换为字符串格式（yyyy-MM-dd）
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            if (resident.getBirthDate() != null) {
                map.put("birthDate", resident.getBirthDate().format(formatter));
            }
            map.put("nationality", resident.getNationality());
            map.put("registeredAddress", resident.getRegisteredAddress());
            map.put("currentAddress", resident.getCurrentAddress());
            map.put("occupation", resident.getOccupation());
            map.put("education", resident.getEducation());
            map.put("maritalStatus", resident.getMaritalStatus());
            map.put("contactPhone", resident.getContactPhone());
            map.put("emergencyContact", resident.getEmergencyContact());
            map.put("emergencyPhone", resident.getEmergencyPhone());
            map.put("avatar", resident.getAvatar());
            map.put("idCardPhoto", resident.getIdCardPhoto());
            map.put("remark", resident.getRemark());
            // 将 LocalDateTime 转换为字符串格式（yyyy-MM-dd HH:mm:ss），以便前端正确显示
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if (resident.getCreateTime() != null) {
                map.put("createTime", resident.getCreateTime().format(dateTimeFormatter));
            }
            if (resident.getUpdateTime() != null) {
                map.put("updateTime", resident.getUpdateTime().format(dateTimeFormatter));
            }
        }
        return map;
    }

    /**
     * 使用 MyBatis-Plus 进行模糊查询（仅用于性能测试）
     * 模拟原来的 MyBatis-Plus 查询逻辑
     */
    @Override
    public Page<Resident> searchByMyBatisPlus(String keyword, Integer current, Integer size) {
        Page<Resident> page = new Page<>(current, size);
        LambdaQueryWrapper<Resident> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.trim().isEmpty() && !"*".equals(keyword.trim())) {
            String trimmedKeyword = keyword.trim();
            // 多字段模糊查询
            wrapper.and(w -> w
                .like(Resident::getRealName, trimmedKeyword)
                .or()
                .like(Resident::getIdCard, trimmedKeyword)
                .or()
                .like(Resident::getRegisteredAddress, trimmedKeyword)
                .or()
                .like(Resident::getCurrentAddress, trimmedKeyword)
                .or()
                .like(Resident::getContactPhone, trimmedKeyword)
            );
        }
        
        wrapper.orderByDesc(Resident::getCreateTime);
        return this.page(page, wrapper);
    }
}

