package com.example.TaskTracker.integration;

import com.example.TaskTracker.model.AppUser;
import com.example.TaskTracker.model.Role;
import com.example.TaskTracker.repository.AppUserRepository;
import com.example.TaskTracker.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    static {
        postgres.start();
    }

    protected static final String USER = "user";
    protected static final String USER_PASS = "userpass123";
    protected static final String ADMIN = "admin";
    protected static final String ADMIN_PASS = "adminpass123";

    @Autowired protected RestTestClient rest;
    @Autowired protected TaskRepository taskRepository;
    @Autowired protected AppUserRepository userRepository;
    @Autowired protected PasswordEncoder passwordEncoder;
    @Autowired protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    void prepare() {
        taskRepository.deleteAll();
        userRepository.deleteAll();

        userRepository.save(new AppUser(USER, passwordEncoder.encode(USER_PASS), Role.USER));
        userRepository.save(new AppUser(ADMIN, passwordEncoder.encode(ADMIN_PASS), Role.ADMIN));

        rest = rest.mutate()
                .defaultHeaders(h -> h.setBasicAuth(USER, USER_PASS))
                .build();
    }
}