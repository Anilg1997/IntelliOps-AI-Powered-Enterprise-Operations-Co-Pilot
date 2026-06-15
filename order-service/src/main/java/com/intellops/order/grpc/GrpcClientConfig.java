package com.intellops.order.grpc;

import org.springframework.context.annotation.Configuration;

/**
 * gRPC client configuration for connecting to the Inventory Service.
 * <p>
 * The actual channel configuration is driven by application.yml properties:
 * {@code grpc.client.inventory-service.address=static://localhost:9091}
 * <p>
 * When the inventory-service is not available, the Order Service will fall back
 * to a no-op stock check (allowing order creation without inventory validation).
 */
@Configuration
public class GrpcClientConfig {

    // gRPC client configuration is handled by the grpc-client-spring-boot-starter
    // via application.yml properties
}
