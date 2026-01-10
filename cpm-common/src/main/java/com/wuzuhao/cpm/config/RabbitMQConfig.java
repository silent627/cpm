package com.wuzuhao.cpm.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

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

    /**
     * 消息转换器
     */
    @Bean
    @NonNull
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate配置
     */
    @Bean
    @NonNull
    public RabbitTemplate rabbitTemplate(@NonNull ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
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
}

