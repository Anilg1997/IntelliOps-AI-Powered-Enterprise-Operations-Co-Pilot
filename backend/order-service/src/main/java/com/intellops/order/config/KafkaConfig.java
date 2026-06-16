package com.intellops.order.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    public static final String ORDER_EVENTS_TOPIC = "order-events";
    public static final String ACTIVITY_LOG_TOPIC = "activity-log";

    @Bean
    public NewTopic orderEventsTopic() {
        return new NewTopic(ORDER_EVENTS_TOPIC, 3, (short) 1);
    }

    @Bean
    public NewTopic activityLogTopic() {
        return new NewTopic(ACTIVITY_LOG_TOPIC, 3, (short) 1);
    }
}
