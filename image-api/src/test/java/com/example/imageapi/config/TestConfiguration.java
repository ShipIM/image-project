package com.example.imageapi.config;

import com.example.imageapi.controller.ImageFilterController;
import com.example.imageapi.service.FilterRequestService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile(value = "test")
@Configuration
public class TestConfiguration {

    @Bean
    public ImageFilterController imageFilterController() {
        return new ImageFilterController(filterRequestService());
    }

    @Bean
    public FilterRequestService filterRequestService() {
        return Mockito.mock(FilterRequestService.class);
    }

}
