package com.wuzuhao.cpm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * RabbitMQ配置类
 */
@Configuration
public class RabbitMQConfig {

    // 居民数据同步队列
    public static final String RESIDENT_SYNC_QUEUE = "resident.sync.queue";
    public static final String RESIDENT_SYNC_EXCHANGE = "resident.sync.exchange";
    public static final String RESIDENT_SYNC_ROUTING_KEY = "resident.sync";

    // 户籍数据同步队列
    public static final String HOUSEHOLD_SYNC_QUEUE = "household.sync.queue";
    public static final String HOUSEHOLD_SYNC_EXCHANGE = "household.sync.exchange";
    public static final String HOUSEHOLD_SYNC_ROUTING_KEY = "household.sync";

    // 搜索索引更新队列
    public static final String SEARCH_INDEX_QUEUE = "search.index.queue";
    public static final String SEARCH_INDEX_EXCHANGE = "search.index.exchange";
    public static final String SEARCH_INDEX_ROUTING_KEY = "search.index";

    // 用户数据同步队列
    public static final String USER_SYNC_QUEUE = "user.sync.queue";
    public static final String USER_SYNC_EXCHANGE = "user.sync.exchange";
    public static final String USER_SYNC_ROUTING_KEY = "user.sync";

    // 管理员数据同步队列
    public static final String ADMIN_SYNC_QUEUE = "admin.sync.queue";
    public static final String ADMIN_SYNC_EXCHANGE = "admin.sync.exchange";
    public static final String ADMIN_SYNC_ROUTING_KEY = "admin.sync";

    // 户籍成员数据同步队列
    public static final String HOUSEHOLD_MEMBER_SYNC_QUEUE = "household-member.sync.queue";
    public static final String HOUSEHOLD_MEMBER_SYNC_EXCHANGE = "household-member.sync.exchange";
    public static final String HOUSEHOLD_MEMBER_SYNC_ROUTING_KEY = "household-member.sync";

    /**
     * 日期时间格式化器：yyyy-MM-dd HH:mm:ss
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 日期格式化器：yyyy-MM-dd
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 配置 ObjectMapper，支持 Java 8 时间类型
     */
    @Bean
    @NonNull
    public ObjectMapper rabbitMQObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 禁用将日期写为时间戳
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 创建并注册 JavaTimeModule
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // 配置 LocalDateTime 序列化和反序列化
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATE_TIME_FORMATTER));
        
        // 配置 LocalDate 序列化和反序列化
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DATE_FORMATTER));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DATE_FORMATTER));
        
        // 注册模块
        objectMapper.registerModule(javaTimeModule);
        
        return objectMapper;
    }

    /**
     * 消息转换器
     */
    @Bean
    @NonNull
    public MessageConverter messageConverter(@NonNull ObjectMapper rabbitMQObjectMapper) {
        return new Jackson2JsonMessageConverter(rabbitMQObjectMapper);
    }

    /**
     * RabbitTemplate配置
     */
    @Bean
    @NonNull
    public RabbitTemplate rabbitTemplate(@NonNull ConnectionFactory connectionFactory, 
                                         @NonNull MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    // ========== 居民数据同步 ==========
    @Bean
    @NonNull
    public Queue residentSyncQueue() {
        return QueueBuilder.durable(RESIDENT_SYNC_QUEUE).build();
    }

    @Bean
    @NonNull
    public DirectExchange residentSyncExchange() {
        return new DirectExchange(RESIDENT_SYNC_EXCHANGE);
    }

    @Bean
    @NonNull
    public Binding residentSyncBinding() {
        return BindingBuilder.bind(residentSyncQueue())
                .to(residentSyncExchange())
                .with(RESIDENT_SYNC_ROUTING_KEY);
    }

    // ========== 户籍数据同步 ==========
    @Bean
    @NonNull
    public Queue householdSyncQueue() {
        return QueueBuilder.durable(HOUSEHOLD_SYNC_QUEUE).build();
    }

    @Bean
    @NonNull
    public DirectExchange householdSyncExchange() {
        return new DirectExchange(HOUSEHOLD_SYNC_EXCHANGE);
    }

    @Bean
    @NonNull
    public Binding householdSyncBinding() {
        return BindingBuilder.bind(householdSyncQueue())
                .to(householdSyncExchange())
                .with(HOUSEHOLD_SYNC_ROUTING_KEY);
    }

    // ========== 搜索索引更新 ==========
    @Bean
    @NonNull
    public Queue searchIndexQueue() {
        return QueueBuilder.durable(SEARCH_INDEX_QUEUE).build();
    }

    @Bean
    @NonNull
    public DirectExchange searchIndexExchange() {
        return new DirectExchange(SEARCH_INDEX_EXCHANGE);
    }

    @Bean
    @NonNull
    public Binding searchIndexBinding() {
        return BindingBuilder.bind(searchIndexQueue())
                .to(searchIndexExchange())
                .with(SEARCH_INDEX_ROUTING_KEY);
    }

    // ========== 用户数据同步 ==========
    @Bean
    @NonNull
    public Queue userSyncQueue() {
        return QueueBuilder.durable(USER_SYNC_QUEUE).build();
    }

    @Bean
    @NonNull
    public DirectExchange userSyncExchange() {
        return new DirectExchange(USER_SYNC_EXCHANGE);
    }

    @Bean
    @NonNull
    public Binding userSyncBinding() {
        return BindingBuilder.bind(userSyncQueue())
                .to(userSyncExchange())
                .with(USER_SYNC_ROUTING_KEY);
    }

    // ========== 管理员数据同步 ==========
    @Bean
    @NonNull
    public Queue adminSyncQueue() {
        return QueueBuilder.durable(ADMIN_SYNC_QUEUE).build();
    }

    @Bean
    @NonNull
    public DirectExchange adminSyncExchange() {
        return new DirectExchange(ADMIN_SYNC_EXCHANGE);
    }

    @Bean
    @NonNull
    public Binding adminSyncBinding() {
        return BindingBuilder.bind(adminSyncQueue())
                .to(adminSyncExchange())
                .with(ADMIN_SYNC_ROUTING_KEY);
    }

    // ========== 户籍成员数据同步 ==========
    @Bean
    @NonNull
    public Queue householdMemberSyncQueue() {
        return QueueBuilder.durable(HOUSEHOLD_MEMBER_SYNC_QUEUE).build();
    }

    @Bean
    @NonNull
    public DirectExchange householdMemberSyncExchange() {
        return new DirectExchange(HOUSEHOLD_MEMBER_SYNC_EXCHANGE);
    }

    @Bean
    @NonNull
    public Binding householdMemberSyncBinding() {
        return BindingBuilder.bind(householdMemberSyncQueue())
                .to(householdMemberSyncExchange())
                .with(HOUSEHOLD_MEMBER_SYNC_ROUTING_KEY);
    }
}

