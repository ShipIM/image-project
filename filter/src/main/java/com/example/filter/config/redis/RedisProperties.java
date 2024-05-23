package com.example.filter.config.redis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "redis")
public class RedisProperties {

    private String host;

    private Integer port;

    private String password;

}
