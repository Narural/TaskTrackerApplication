package com.example.TaskTracker.controller;

import com.example.TaskTracker.dto.UserResponse;
import com.example.TaskTracker.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    /** Кто я и какая у меня роль. Поля с паролем в UserResponse нет вообще. */
    @GetMapping("/api/me")
    public ResponseEntity<UserResponse> me(Principal principal) {
        return ResponseEntity.ok(authService.me(principal.getName()));
    }
}
