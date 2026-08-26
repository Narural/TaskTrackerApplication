package com.example.TaskTracker.integration;

import com.example.TaskTracker.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
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

    @Autowired protected RestTestClient rest;
    @Autowired protected TaskRepository taskRepository;
    @Autowired protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        taskRepository.deleteAll();
    }
}
