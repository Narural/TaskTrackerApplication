package com.example.TaskTracker.security;

import com.example.TaskTracker.integration.AbstractIntegrationTest;
import com.example.TaskTracker.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceMethodSecurityIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TaskService taskService;

    @Test
    @WithMockUser(username = "anna", roles = "USER")
    void searchAll_userRole_throwsAccessDenied() {
        assertThatThrownBy(() -> taskService.searchAll(null, null, null, null, PageRequest.of(0, 10)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(username = "root", roles = "ADMIN")
    void searchAll_adminRole_returnsPage() {
        assertThat(taskService.searchAll(null, null, null, null, PageRequest.of(0, 10))).isNotNull();
    }
}