package com.example.TaskTracker.controller;

import com.example.TaskTracker.exception.FoundTaskException;
import com.example.TaskTracker.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
public class TaskControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Test
    void getTaskById_notExistingId_returns404WithMessage() throws Exception{
        when(taskService.getTaskById(anyLong())).thenThrow(FoundTaskException.class);
        mockMvc.perform(get("/api/task/90"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("No task found"));
    }

    @Test
    void createTask_blankTitle_returns400AndDontCreateTask() throws Exception{
        mockMvc.perform(post("/api/task").
                contentType(MediaType.APPLICATION_JSON).content(
    "{\"title\":\"\",\"priority\":\"HIGH\",\"description\":\"desc\"}"))
                .andExpect(status().isBadRequest());
    }
    @Test
    void createTask_allowedData_createsNewTask(){

    }
}
