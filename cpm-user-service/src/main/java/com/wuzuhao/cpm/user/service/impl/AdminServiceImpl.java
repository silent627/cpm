package com.wuzuhao.cpm.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuzuhao.cpm.common.dto.ESSyncMessage;
import com.wuzuhao.cpm.config.RabbitMQConfig;
import com.wuzuhao.cpm.user.entity.Admin;
import com.wuzuhao.cpm.user.mapper.AdminMapper;
import com.wuzuhao.cpm.user.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员服务实现类
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);
    
    @Override
    public Admin getByUserId(Long userId) {
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getUserId, userId);
        return this.getOne(wrapper);
    }

    @Override
    public Admin createAdmin(Long userId, Admin admin) {
        admin.setUserId(userId);
        // createTime 和 updateTime 由 MyBatis-Plus 自动填充
        this.save(admin);
        // 发送 ES 同步消息
        sendESSyncMessage(ESSyncMessage.create("admin_index", admin.getId(), convertToMap(admin)));
        return admin;
    }
    
    @Override
    public boolean updateById(Admin admin) {
        boolean result = super.updateById(admin);
        if (result) {
            // 发送 ES 同步消息
            sendESSyncMessage(ESSyncMessage.update("admin_index", admin.getId(), convertToMap(admin)));
        }
        return result;
    }
    
    @Override
    public boolean removeById(java.io.Serializable id) {
        Long adminId = id instanceof Long ? (Long) id : Long.valueOf(id.toString());
        log.info("开始删除管理员，id: {}", adminId);
        boolean result = super.removeById(id);
        log.info("数据库删除操作完成，id: {}, 结果: {}", adminId, result);
        if (result) {
            // 发送 ES 同步消息
            log.info("管理员删除成功，准备发送 ES 删除消息，id: {}", adminId);
            sendESSyncMessage(ESSyncMessage.delete("admin_index", adminId));
            log.info("ES 删除消息已发送，id: {}", adminId);
        } else {
            log.warn("数据库删除失败，id: {}", adminId);
        }
        return result;
    }
    
    /**
     * 发送 ES 同步消息
     */
    private void sendESSyncMessage(ESSyncMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.ADMIN_SYNC_EXCHANGE,
                RabbitMQConfig.ADMIN_SYNC_ROUTING_KEY,
                message
            );
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(AdminServiceImpl.class)
                .warn("发送 ES 同步消息失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 将 Admin 对象转换为 Map
     */
    private Map<String, Object> convertToMap(Admin admin) {
        Map<String, Object> map = new HashMap<>();
        if (admin != null) {
            map.put("id", admin.getId());
            map.put("userId", admin.getUserId());
            map.put("adminNo", admin.getAdminNo());
            map.put("department", admin.getDepartment());
            map.put("position", admin.getPosition());
            map.put("remark", admin.getRemark());
            // 将 LocalDateTime 转换为字符串格式（yyyy-MM-dd HH:mm:ss），以便前端正确显示
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if (admin.getCreateTime() != null) {
                map.put("createTime", admin.getCreateTime().format(formatter));
            }
            if (admin.getUpdateTime() != null) {
                map.put("updateTime", admin.getUpdateTime().format(formatter));
            }
        }
        return map;
    }
}

