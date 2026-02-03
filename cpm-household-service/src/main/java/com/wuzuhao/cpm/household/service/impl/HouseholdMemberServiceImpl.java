package com.wuzuhao.cpm.household.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuzuhao.cpm.household.entity.HouseholdMember;
import com.wuzuhao.cpm.common.dto.ESSyncMessage;
import com.wuzuhao.cpm.config.RabbitMQConfig;
import com.wuzuhao.cpm.household.mapper.HouseholdMemberMapper;
import com.wuzuhao.cpm.household.service.HouseholdMemberService;
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
 * 户籍成员服务实现类
 */
@Service
public class HouseholdMemberServiceImpl extends ServiceImpl<HouseholdMemberMapper, HouseholdMember> implements HouseholdMemberService {

    @Autowired
    private HouseholdService householdService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final Logger log = LoggerFactory.getLogger(HouseholdMemberServiceImpl.class);
    
    @Override
    public HouseholdMember addMember(Long householdId, Long residentId, String relationship) {
        // 检查是否已存在
        LambdaQueryWrapper<HouseholdMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseholdMember::getHouseholdId, householdId)
                .eq(HouseholdMember::getResidentId, residentId);
        HouseholdMember exist = this.getOne(wrapper);
        if (exist != null) {
            throw new RuntimeException("该成员已在此户籍中");
        }

        HouseholdMember member = new HouseholdMember();
        member.setHouseholdId(householdId);
        member.setResidentId(residentId);
        member.setRelationship(relationship);
        // createTime 和 updateTime 由 MyBatis-Plus 自动填充
        this.save(member);

        // 更新户籍成员数量
        householdService.updateMemberCount(householdId);

        // 发送 ES 同步消息
        sendESSyncMessage(ESSyncMessage.create("household_member_index", member.getId(), convertToMap(member)));

        return member;
    }

    @Override
    public boolean removeMember(Long householdId, Long residentId) {
        LambdaQueryWrapper<HouseholdMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseholdMember::getHouseholdId, householdId)
                .eq(HouseholdMember::getResidentId, residentId);
        boolean result = this.remove(wrapper);

        // 更新户籍成员数量
        if (result) {
            householdService.updateMemberCount(householdId);
            // 发送 ES 同步消息（需要先查询出要删除的成员ID）
            LambdaQueryWrapper<HouseholdMember> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(HouseholdMember::getHouseholdId, householdId)
                    .eq(HouseholdMember::getResidentId, residentId);
            HouseholdMember member = this.getOne(queryWrapper);
            if (member != null) {
                sendESSyncMessage(ESSyncMessage.delete("household_member_index", member.getId()));
            }
        }

        return result;
    }
    
    @Override
    public boolean updateById(HouseholdMember member) {
        boolean result = super.updateById(member);
        if (result) {
            // 发送 ES 同步消息
            sendESSyncMessage(ESSyncMessage.update("household_member_index", member.getId(), convertToMap(member)));
        }
        return result;
    }
    
    @Override
    public boolean removeById(java.io.Serializable id) {
        Long memberId = id instanceof Long ? (Long) id : Long.valueOf(id.toString());
        log.info("开始删除户籍成员，id: {}", memberId);
        boolean result = super.removeById(id);
        log.info("数据库删除操作完成，id: {}, 结果: {}", memberId, result);
        if (result) {
            // 发送 ES 同步消息
            log.info("户籍成员删除成功，准备发送 ES 删除消息，id: {}", memberId);
            sendESSyncMessage(ESSyncMessage.delete("household_member_index", memberId));
            log.info("ES 删除消息已发送，id: {}", memberId);
        } else {
            log.warn("数据库删除失败，id: {}", memberId);
        }
        return result;
    }
    
    /**
     * 发送 ES 同步消息
     */
    private void sendESSyncMessage(ESSyncMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.HOUSEHOLD_MEMBER_SYNC_EXCHANGE,
                RabbitMQConfig.HOUSEHOLD_MEMBER_SYNC_ROUTING_KEY,
                message
            );
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(HouseholdMemberServiceImpl.class)
                .warn("发送 ES 同步消息失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 将 HouseholdMember 对象转换为 Map
     */
    private Map<String, Object> convertToMap(HouseholdMember member) {
        Map<String, Object> map = new HashMap<>();
        if (member != null) {
            map.put("id", member.getId());
            map.put("householdId", member.getHouseholdId());
            map.put("residentId", member.getResidentId());
            map.put("relationship", member.getRelationship());
            // 将 LocalDateTime 转换为字符串格式（yyyy-MM-dd HH:mm:ss），以便前端正确显示
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if (member.getCreateTime() != null) {
                map.put("createTime", member.getCreateTime().format(formatter));
            }
            if (member.getUpdateTime() != null) {
                map.put("updateTime", member.getUpdateTime().format(formatter));
            }
        }
        return map;
    }
}

