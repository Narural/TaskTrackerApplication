package com.example.TaskTracker.dto;

import com.example.TaskTracker.model.AppUser;
import com.example.TaskTracker.model.Role;

import java.time.Instant;

public class UserResponse {
    private final Long id;
    private final String username;
    private final Role role;
    private final Instant createdAt;

    public UserResponse(AppUser user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.role = user.getRole();
        this.createdAt =user.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
