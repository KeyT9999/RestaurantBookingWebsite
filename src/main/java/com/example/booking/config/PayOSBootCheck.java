package com.example.booking.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Component để kiểm tra cấu hình PayOS khi khởi động ứng dụng
 */
@Component
public class PayOSBootCheck {
    
    @Autowired
    private Environment env;
    
    @PostConstruct
    void checkPayOSConfig() {
        System.out.println("=== PayOS Configuration Check ===");
        System.out.println("[PayOS] CLIENT_ID: " + (env.getProperty("payment.payos.client-id") != null ? "✓ SET" : "✗ NOT SET"));
        System.out.println("[PayOS] API_KEY: " + (env.getProperty("payment.payos.api-key") != null ? "✓ SET" : "✗ NOT SET"));
        System.out.println("[PayOS] CHECKSUM_KEY: " + (env.getProperty("payment.payos.checksum-key") != null ? "✓ SET" : "✗ NOT SET"));
        System.out.println("[PayOS] ENDPOINT: " + env.getProperty("payment.payos.endpoint"));
        System.out.println("[PayOS] RETURN_URL: " + env.getProperty("payment.payos.return-url"));
        System.out.println("[PayOS] CANCEL_URL: " + env.getProperty("payment.payos.cancel-url"));
        System.out.println("[PayOS] WEBHOOK_URL: " + env.getProperty("payment.payos.webhook-url"));
        System.out.println("=== End PayOS Check ===");
    }
}
