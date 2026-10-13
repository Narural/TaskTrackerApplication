package com.example.TaskTracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.admin")
public record AdminProperties(
        @DefaultValue("admin") String username,
        String password) {
}