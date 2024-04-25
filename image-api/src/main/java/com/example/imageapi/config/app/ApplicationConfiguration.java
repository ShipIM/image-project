package com.example.imageapi.config.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public Map<String, String> violationsMap() {
        return Map.of(
                "user_unique", "a user with that name already exists"
        );
    }

}