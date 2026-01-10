package com.wuzuhao.cpm.resident;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 居民服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.wuzuhao.cpm.resident.feign")
@MapperScan("com.wuzuhao.cpm.resident.mapper")
@ComponentScan(basePackages = "com.wuzuhao.cpm")
public class ResidentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ResidentServiceApplication.class, args);
    }
}

