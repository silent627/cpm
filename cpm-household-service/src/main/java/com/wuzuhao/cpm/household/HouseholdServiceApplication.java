package com.wuzuhao.cpm.household;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 户籍服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.wuzuhao.cpm.household.feign")
@MapperScan("com.wuzuhao.cpm.household.mapper")
@ComponentScan(basePackages = "com.wuzuhao.cpm")
public class HouseholdServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(HouseholdServiceApplication.class, args);
    }
}

