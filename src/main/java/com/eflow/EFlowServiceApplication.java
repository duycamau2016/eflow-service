package com.eflow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.eflow.mapper")
public class EFlowServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EFlowServiceApplication.class, args);
    }
}
