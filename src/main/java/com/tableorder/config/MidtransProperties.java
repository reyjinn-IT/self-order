package com.tableorder.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "midtrans")
@Data
public class MidtransProperties {
    private String baseUrl;
    private String apiBaseUrl;
    private String serverKey;
    private String clientKey;
}
