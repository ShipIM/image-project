package com.example.filtergray;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class FilterGrayApplication {

    public static void main(String[] args) {
        SpringApplication.run(FilterGrayApplication.class, args);
    }

}
