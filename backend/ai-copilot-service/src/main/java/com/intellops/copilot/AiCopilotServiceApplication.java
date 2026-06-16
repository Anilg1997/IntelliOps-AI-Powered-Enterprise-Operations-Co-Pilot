package com.intellops.copilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EntityScan("com.intellops.copilot.model")
@EnableJpaRepositories("com.intellops.copilot.repository")
@EnableMongoRepositories("com.intellops.copilot.mongo")
@ComponentScan(basePackages = "com.intellops.copilot")
public class AiCopilotServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiCopilotServiceApplication.class, args);
    }
}
