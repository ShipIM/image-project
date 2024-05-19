package com.example.filter.config.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;

@Profile("recognition")
@Configuration
public class WebConfiguration {

    @Value("${spring.rest-client.base-uri}")
    private String baseURI;
    @Value("${spring.rest-client.basic}")
    private String basic;

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .defaultHeader("AUTHORIZATION", basic)
                .baseUrl(baseURI)
                .build();
    }

}
