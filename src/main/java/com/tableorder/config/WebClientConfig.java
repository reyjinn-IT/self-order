package com.tableorder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient midtransWebClient() {
        // Tanpa baseUrl tetap: Snap API (base-url) dan Core API (api-base-url)
        // Midtrans memakai host berbeda, jadi URL lengkap dibangun di service layer.
        return WebClient.builder().build();
    }
}
