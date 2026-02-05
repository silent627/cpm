package com.wuzuhao.cpm.search.listener;

import com.wuzuhao.cpm.common.dto.ESSyncMessage;
import com.wuzuhao.cpm.config.RabbitMQConfig;
import com.wuzuhao.cpm.search.service.ESDocumentSyncService;
import com.wuzuhao.cpm.search.util.ElasticsearchIndexUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Elasticsearch 数据同步监听器
 * 监听 RabbitMQ 消息，实时同步数据到 ES
 */
@Slf4j
@Component
public class ESDataSyncListener {

    @Autowired
    private ESDocumentSyncService syncService;

    /**
     * 监听居民数据同步队列
     */
    @RabbitListener(queues = RabbitMQConfig.RESIDENT_SYNC_QUEUE)
    public void handleResidentSync(ESSyncMessage message) {
        try {
            processSyncMessage(message, ElasticsearchIndexUtil.RESIDENT_INDEX);
        } catch (Exception e) {
            log.error("处理居民数据同步消息失败", e);
        }
    }

    /**
     * 监听户籍数据同步队列
     */
    @RabbitListener(queues = RabbitMQConfig.HOUSEHOLD_SYNC_QUEUE)
    public void handleHouseholdSync(ESSyncMessage message) {
        try {
            processSyncMessage(message, ElasticsearchIndexUtil.HOUSEHOLD_INDEX);
        } catch (Exception e) {
            log.error("处理户籍数据同步消息失败", e);
        }
    }

    /**
     * 监听用户数据同步队列
     */
    @RabbitListener(queues = RabbitMQConfig.USER_SYNC_QUEUE)
    public void handleUserSync(ESSyncMessage message) {
        log.info("收到用户数据同步消息，operation: {}, id: {}", 
            message != null ? message.getOperation() : "null", 
            message != null ? message.getId() : "null");
        try {
            processSyncMessage(message, ElasticsearchIndexUtil.USER_INDEX);
        } catch (Exception e) {
            log.error("处理用户数据同步消息失败", e);
        }
    }

    /**
     * 监听管理员数据同步队列
     */
    @RabbitListener(queues = RabbitMQConfig.ADMIN_SYNC_QUEUE)
    public void handleAdminSync(ESSyncMessage message) {
        try {
            processSyncMessage(message, ElasticsearchIndexUtil.ADMIN_INDEX);
        } catch (Exception e) {
            log.error("处理管理员数据同步消息失败", e);
        }
    }

    /**
     * 监听户籍成员数据同步队列
     */
    @RabbitListener(queues = RabbitMQConfig.HOUSEHOLD_MEMBER_SYNC_QUEUE)
    public void handleHouseholdMemberSync(ESSyncMessage message) {
        try {
            processSyncMessage(message, ElasticsearchIndexUtil.HOUSEHOLD_MEMBER_INDEX);
        } catch (Exception e) {
            log.error("处理户籍成员数据同步消息失败", e);
        }
    }

    /**
     * 处理同步消息
     */
    private void processSyncMessage(ESSyncMessage message, String index) {
        if (message == null || message.getOperation() == null || message.getId() == null) {
            log.warn("收到无效的同步消息: {}", message);
            return;
        }

        String operation = message.getOperation();
        Long id = message.getId();

        try {
            switch (operation) {
                case "CREATE":
                    if (message.getData() != null) {
                        syncService.saveDocument(index, message.getData());
                        log.info("ES 创建文档成功，index: {}, id: {}", index, id);
                    } else {
                        log.warn("CREATE 操作缺少数据，index: {}, id: {}", index, id);
                    }
                    break;
                case "UPDATE":
                    log.info("收到 ES 更新消息，index: {}, id: {}", index, id);
                    if (message.getData() != null) {
                        boolean success = syncService.updateDocument(index, id, message.getData());
                        if (success) {
                            log.info("ES 更新文档成功，index: {}, id: {}", index, id);
                        } else {
                            log.warn("ES 更新文档失败，index: {}, id: {}", index, id);
                        }
                    } else {
                        log.warn("UPDATE 操作缺少数据，index: {}, id: {}", index, id);
                    }
                    break;
                case "DELETE":
                    log.info("收到 ES 删除消息，index: {}, id: {}", index, id);
                    boolean success = syncService.removeDocument(index, id);
                    if (success) {
                        log.info("ES 删除文档成功，index: {}, id: {}", index, id);
                    } else {
                        log.warn("ES 删除文档失败，index: {}, id: {}", index, id);
                    }
                    break;
                default:
                    log.warn("未知的操作类型: {}, index: {}, id: {}", operation, index, id);
            }
        } catch (Exception e) {
            log.error("处理同步消息失败，operation: {}, index: {}, id: {}", operation, index, id, e);
            // 不抛出异常，避免消息被重复处理
        }
    }
}
