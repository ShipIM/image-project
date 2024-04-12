package com.example.imageproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class ImageProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImageProjectApplication.class, args);
    }

}
