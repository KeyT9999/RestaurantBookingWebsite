package com.example.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RestaurantBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestaurantBookingApplication.class, args);
    }
} 