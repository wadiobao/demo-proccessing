package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.utils.CloudinaryUtils;

@Configuration
public class CloudinaryConfig {
	@Value("${cloudinary.cloud.name}")
    private String cloudName;

    @Value("${cloudinary.api.key}")
    private String apiKey;

    @Value("${cloudinary.api.secret}")
    private String apiSecret;

    @Bean
    public CloudinaryUtils cloudinaryUtils() {
        return new CloudinaryUtils(cloudName, apiKey, apiSecret);
    }
}
