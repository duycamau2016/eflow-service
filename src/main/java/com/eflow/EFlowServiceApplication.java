package com.eflow;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.eflow.mapper")
@OpenAPIDefinition(
    info = @Info(
        title = "eFlow SW2 API",
        version = "1.0",
        description = "HR Hierarchy & Project Management REST API",
        contact = @Contact(name = "eFlow Team")
    )
)
public class EFlowServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EFlowServiceApplication.class, args);
    }
}
