package com.chakraytest.apiusers.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
    @Value("${app.security.secret.key}")
    private String secretKey;

    public String getSecretKey(){
        return this.secretKey;
    }
}
