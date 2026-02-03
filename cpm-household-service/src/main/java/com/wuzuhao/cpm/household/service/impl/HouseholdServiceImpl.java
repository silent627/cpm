package com.wuzuhao.cpm.household.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuzuhao.cpm.household.entity.Household;
import com.wuzuhao.cpm.household.entity.HouseholdMember;
import com.wuzuhao.cpm.common.dto.ESSyncMessage;
import com.wuzuhao.cpm.config.RabbitMQConfig;
import com.wuzuhao.cpm.household.mapper.HouseholdMapper;
import com.wuzuhao.cpm.household.mapper.HouseholdMemberMapper;
import com.wuzuhao.cpm.household.service.HouseholdService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 户籍服务实现类
 */
@Service
public class HouseholdServiceImpl extends ServiceImpl<HouseholdMapper, Household> implements HouseholdService {

    @Autowired
    private HouseholdMemberMapper householdMemberMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final Logger log = LoggerFactory.getLogger(HouseholdServiceImpl.class);

    @Override
    public Household getByHouseholdNo(String householdNo) {
        LambdaQueryWrapper<Household> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Household::getHouseholdNo, householdNo);
        return this.getOne(wrapper);
    }

    @Override
    public Household createHousehold(Household household) {
        household.setStatus(1);
        household.setMemberCount(0);
        // createTime 和 updateTime 由 MyBatis-Plus 自动填充
        this.save(household);
        // 发送 ES 同步消息
        sendESSyncMessage(ESSyncMessage.create("household_index", household.getId(), convertToMap(household)));
        return household;
    }

    @Override
    public void updateMemberCount(Long householdId) {
        LambdaQueryWrapper<HouseholdMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseholdMember::getHouseholdId, householdId);
        long count = householdMemberMapper.selectCount(wrapper);

        Household household = this.getById(householdId);
        if (household != null) {
            household.setMemberCount((int) count);
            // updateTime 由 MyBatis-Plus 自动填充
            this.updateById(household);
            // 发送 ES 同步消息
            sendESSyncMessage(ESSyncMessage.update("household_index", household.getId(), convertToMap(household)));
        }
    }
    
    @Override
    public boolean updateById(Household household) {
        boolean result = super.updateById(household);
        if (result) {
            // 发送 ES 同步消息
            sendESSyncMessage(ESSyncMessage.update("household_index", household.getId(), convertToMap(household)));
        }
        return result;
    }
    
    @Override
    public boolean removeById(java.io.Serializable id) {
        Long householdId = id instanceof Long ? (Long) id : Long.valueOf(id.toString());
        log.info("开始删除户籍，id: {}", householdId);
        boolean result = super.removeById(id);
        log.info("数据库删除操作完成，id: {}, 结果: {}", householdId, result);
        if (result) {
            // 发送 ES 同步消息
            log.info("户籍删除成功，准备发送 ES 删除消息，id: {}", householdId);
            sendESSyncMessage(ESSyncMessage.delete("household_index", householdId));
            log.info("ES 删除消息已发送，id: {}", householdId);
        } else {
            log.warn("数据库删除失败，id: {}", householdId);
        }
        return result;
    }
    
    /**
     * 发送 ES 同步消息
     */
    private void sendESSyncMessage(ESSyncMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.HOUSEHOLD_SYNC_EXCHANGE,
                RabbitMQConfig.HOUSEHOLD_SYNC_ROUTING_KEY,
                message
            );
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(HouseholdServiceImpl.class)
                .warn("发送 ES 同步消息失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 将 Household 对象转换为 Map
     */
    private Map<String, Object> convertToMap(Household household) {
        Map<String, Object> map = new HashMap<>();
        if (household != null) {
            map.put("id", household.getId());
            map.put("headId", household.getHeadId());
            map.put("headName", household.getHeadName());
            map.put("headIdCard", household.getHeadIdCard());
            map.put("householdNo", household.getHouseholdNo());
            map.put("address", household.getAddress());
            map.put("householdType", household.getHouseholdType());
            map.put("memberCount", household.getMemberCount());
            map.put("contactPhone", household.getContactPhone());
            // 将 LocalDateTime 转换为字符串格式（yyyy-MM-dd HH:mm:ss），以便前端正确显示
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if (household.getMoveInDate() != null) {
                map.put("moveInDate", household.getMoveInDate().format(dateTimeFormatter));
            }
            if (household.getMoveOutDate() != null) {
                map.put("moveOutDate", household.getMoveOutDate().format(dateTimeFormatter));
            }
            map.put("moveInReason", household.getMoveInReason());
            map.put("moveOutReason", household.getMoveOutReason());
            map.put("status", household.getStatus());
            map.put("remark", household.getRemark());
            if (household.getCreateTime() != null) {
                map.put("createTime", household.getCreateTime().format(dateTimeFormatter));
            }
            if (household.getUpdateTime() != null) {
                map.put("updateTime", household.getUpdateTime().format(dateTimeFormatter));
            }
        }
        return map;
    }
}

