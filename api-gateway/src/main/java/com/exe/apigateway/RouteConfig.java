package com.exe.apigateway;

import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {
    @Bean
    public org.springframework.cloud.gateway.route.RouteLocator customRouteLocator(org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder builder) {
        return builder.routes()
                .route("payment-service", r -> r.path("/api/v1/sneakerx/pago/**")
                        .filters(f -> f.stripPrefix(4).prefixPath("/api"))
                        .uri("lb://payment-service"))
                .build();
    }
}
