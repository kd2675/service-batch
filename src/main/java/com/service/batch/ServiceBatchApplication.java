package com.service.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ServiceBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceBatchApplication.class, args);
    }

}
