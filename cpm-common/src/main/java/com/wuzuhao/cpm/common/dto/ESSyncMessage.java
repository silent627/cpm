package com.wuzuhao.cpm.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * Elasticsearch 数据同步消息
 */
@Data
public class ESSyncMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 操作类型：CREATE, UPDATE, DELETE
     */
    private String operation;
    
    /**
     * 索引名称
     */
    private String index;
    
    /**
     * 文档ID
     */
    private Long id;
    
    /**
     * 数据内容（CREATE 和 UPDATE 时需要）
     */
    private Map<String, Object> data;
    
    /**
     * 创建操作消息
     */
    public static ESSyncMessage create(String index, Long id, Map<String, Object> data) {
        ESSyncMessage message = new ESSyncMessage();
        message.setOperation("CREATE");
        message.setIndex(index);
        message.setId(id);
        message.setData(data);
        return message;
    }
    
    /**
     * 更新操作消息
     */
    public static ESSyncMessage update(String index, Long id, Map<String, Object> data) {
        ESSyncMessage message = new ESSyncMessage();
        message.setOperation("UPDATE");
        message.setIndex(index);
        message.setId(id);
        message.setData(data);
        return message;
    }
    
    /**
     * 删除操作消息
     */
    public static ESSyncMessage delete(String index, Long id) {
        ESSyncMessage message = new ESSyncMessage();
        message.setOperation("DELETE");
        message.setIndex(index);
        message.setId(id);
        return message;
    }
}
