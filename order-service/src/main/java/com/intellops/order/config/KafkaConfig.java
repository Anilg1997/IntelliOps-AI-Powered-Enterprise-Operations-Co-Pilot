package com.intellops.order.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    public static final String ORDER_CREATED_TOPIC = "order.created";
    public static final String ORDER_STATUS_CHANGED_TOPIC = "order.status.changed";
    public static final String PAYMENT_FAILED_TOPIC = "payment.failed";

    @Bean
    public NewTopic orderCreatedTopic() {
        return new NewTopic(ORDER_CREATED_TOPIC, 3, (short) 1);
    }

    @Bean
    public NewTopic orderStatusChangedTopic() {
        return new NewTopic(ORDER_STATUS_CHANGED_TOPIC, 3, (short) 1);
    }

    @Bean
    public NewTopic paymentFailedTopic() {
        return new NewTopic(PAYMENT_FAILED_TOPIC, 3, (short) 1);
    }
}
