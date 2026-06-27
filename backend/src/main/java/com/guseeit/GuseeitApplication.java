package com.guseeit;

import com.guseeit.config.GuseeitProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(GuseeitProperties.class)
public class GuseeitApplication {

    public static void main(String[] args) {
        SpringApplication.run(GuseeitApplication.class, args);
    }
}
