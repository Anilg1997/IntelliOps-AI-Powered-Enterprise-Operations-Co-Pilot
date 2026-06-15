package com.intellops.billing.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    public static final String INVOICE_CREATED_TOPIC = "invoice.created";
    public static final String INVOICE_PAID_TOPIC = "invoice.paid";
    public static final String INVOICE_OVERDUE_TOPIC = "invoice.overdue";
    public static final String PAYMENT_RECEIVED_TOPIC = "payment.received";
    public static final String BILLING_ACCOUNT_CHANGED_TOPIC = "billing.account.changed";

    @Bean
    public NewTopic invoiceCreatedTopic() {
        return new NewTopic(INVOICE_CREATED_TOPIC, 3, (short) 1);
    }

    @Bean
    public NewTopic invoicePaidTopic() {
        return new NewTopic(INVOICE_PAID_TOPIC, 3, (short) 1);
    }

    @Bean
    public NewTopic invoiceOverdueTopic() {
        return new NewTopic(INVOICE_OVERDUE_TOPIC, 3, (short) 1);
    }

    @Bean
    public NewTopic paymentReceivedTopic() {
        return new NewTopic(PAYMENT_RECEIVED_TOPIC, 3, (short) 1);
    }

    @Bean
    public NewTopic billingAccountChangedTopic() {
        return new NewTopic(BILLING_ACCOUNT_CHANGED_TOPIC, 3, (short) 1);
    }
}
