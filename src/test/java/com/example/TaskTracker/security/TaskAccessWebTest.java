package com.example.TaskTracker.security;

import com.example.TaskTracker.controller.AdminTaskController;
import com.example.TaskTracker.controller.TaskController;
import com.example.TaskTracker.model.TaskPriority;
import com.example.TaskTracker.model.TaskStatus;
import com.example.TaskTracker.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({TaskController.class, AdminTaskController.class})
@Import(SecurityConfig.class)
class TaskAccessWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Test
    @WithAnonymousUser
    void getTasks_withoutToken_returns401AndServiceUntouched() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));

        verifyNoInteractions(taskService);
    }

    @Test
    @WithAnonymousUser
    void deleteTask_withoutToken_returns401AndServiceUntouched() throws Exception {
        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(taskService);
    }

    @Test
    void adminTasks_userRole_returns403AndServiceUntouched() throws Exception {
        mockMvc.perform(get("/api/admin/tasks")
                        .with(jwt().jwt(j -> j.subject("anna"))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        verifyNoInteractions(taskService);
    }

    @Test
    void adminTasks_adminRole_returns200() throws Exception {
        when(taskService.searchAll(isNull(TaskStatus.class), isNull(TaskPriority.class),
                isNull(String.class), isNull(String.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/admin/tasks")
                        .with(jwt().jwt(j -> j.subject("root"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void unknownPath_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/nosuchpath"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unknownPath_withToken_returns403FromDenyAll() throws Exception {
        mockMvc.perform(get("/api/nosuchpath")
                        .with(jwt().jwt(j -> j.subject("anna"))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void adminUnmappedPath_userRole_returns403FromChain() throws Exception {
        mockMvc.perform(get("/api/admin/nosuchpath")
                        .with(jwt().jwt(j -> j.subject("anna"))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void adminUnmappedPath_adminRole_passesChainAndReturns404() throws Exception {
        mockMvc.perform(get("/api/admin/nosuchpath")
                        .with(jwt().jwt(j -> j.subject("root"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNotFound());
    }
}
