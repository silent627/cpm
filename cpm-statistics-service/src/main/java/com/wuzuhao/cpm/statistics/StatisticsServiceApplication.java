package com.wuzuhao.cpm.statistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 统计服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.wuzuhao.cpm.statistics.feign")
@ComponentScan(basePackages = "com.wuzuhao.cpm")
public class StatisticsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(StatisticsServiceApplication.class, args);
    }
}

