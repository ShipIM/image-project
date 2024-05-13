package com.example.imageapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class ImageApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImageApiApplication.class, args);
    }

}
