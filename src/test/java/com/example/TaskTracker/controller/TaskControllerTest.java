package com.example.TaskTracker.controller;

import com.example.TaskTracker.dto.TaskResponse;
import com.example.TaskTracker.dto.TaskStatistic;
import com.example.TaskTracker.exception.ChangeStatusException;
import com.example.TaskTracker.exception.ExistingTagException;
import com.example.TaskTracker.exception.FoundTaskException;
import com.example.TaskTracker.exception.NotExistingTagExceptions;
import com.example.TaskTracker.model.Task;
import com.example.TaskTracker.model.TaskPriority;
import com.example.TaskTracker.model.TaskStatus;
import com.example.TaskTracker.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(TaskController.class)
public class TaskControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;
    private TaskResponse taskResponse(long id, String title, TaskStatus status, TaskPriority priority){
        Task task = new Task(title, TaskPriority.HIGH, "Test");
        ReflectionTestUtils.setField(task, "id", id);
        ReflectionTestUtils.setField(task, "status", status);
        ReflectionTestUtils.setField(task, "priority", priority);
        return new TaskResponse(task);
    }


    @Test
    void getTaskById_notExistingId_returns404WithMessage() throws Exception{
        when(taskService.getTaskById(anyLong())).thenThrow(new FoundTaskException());
        mockMvc.perform(get("/api/tasks/90"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("No tasks found"));
    }

    @Test
    void createTask_blankTitle_returns400AndDontCreateTask() throws Exception{
        mockMvc.perform(post("/api/tasks").
                contentType(MediaType.APPLICATION_JSON).content(
                        "{\"title\":\"\",\"priority\":\"HIGH\",\"description\":\"desc\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(taskService);
    }
    @Test
    void createTask_allowedData_createsNewTask() throws Exception{
        when(taskService.createTask("Test", TaskPriority.HIGH, "Test")).
                thenReturn(taskResponse(1L, "Test", TaskStatus.NEW, TaskPriority.HIGH));
        mockMvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Test\",\"priority\":\"HIGH\",\"description\":\"Test\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/tasks/1"))
                .andExpect(jsonPath("$.id").value(1));
    }
    @Test
    void createTask_titleLengthOver100_returns400AndDontCreateTask() throws Exception{
        mockMvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\""+"a".repeat(101)+
                        "\",\"priority\":\"HIGH\",\"description\":\"Test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Размер названия не должен превышать 100 символов"));
        verifyNoInteractions(taskService);
    }
    @Test
    void getTasks_noFiltersNoTasks_returns200WithEmptyContent() throws Exception{
        when(taskService.search(isNull(), isNull(), isNull(), any(Pageable.class))).thenReturn(Page.empty());
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }
    @Test
    void getTasks_unknownStatusParam_returns400NoServerError() throws Exception{
        mockMvc.perform(get("/api/tasks")
                .param("status", "Test"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(taskService);
    }
    @Test
    void getTasks_filteredByStatus_returns200AndOnlyThoseTasks() throws Exception{
        Page<TaskResponse> page = new PageImpl<>(List.of(
                taskResponse(1L, "Первая", TaskStatus.NEW, TaskPriority.HIGH),
                taskResponse(2L, "Вторая", TaskStatus.NEW, TaskPriority.HIGH)
        ));
        when(taskService.search(eq(TaskStatus.NEW), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);
        mockMvc.perform(get("/api/tasks")
                        .param("status", "NEW")).andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("Первая"))
                .andExpect(jsonPath("$.totalElements").value(2));
        verify(taskService).search(eq(TaskStatus.NEW), isNull(), isNull(), any(Pageable.class));
    }
    @Test
    void changeStatus_allowedTransition_returns200AndChangeStatus() throws Exception{
        when(taskService.changeStatus(TaskStatus.IN_PROCESS, 1L))
                .thenReturn(taskResponse(1L, "Test", TaskStatus.IN_PROCESS, TaskPriority.HIGH));
        mockMvc.perform(patch("/api/tasks/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"IN_PROCESS\"}")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROCESS"));
    }
    @Test
    void changeStatus_notAllowedTransition_returns400AdnDontChangeStatus() throws Exception{
        when(taskService.changeStatus(TaskStatus.DONE, 1L))
                .thenThrow(new ChangeStatusException());
        mockMvc.perform(patch("/api/tasks/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"DONE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Ошибка изменения статуса, " +
                                "статус можно менять только на 1 шаг"));
    }
    @Test
    void addTag_newTag_returns201AndAddsTagaddTag_newTag_returns201AndAddsTag() throws Exception{
        when(taskService.addTag("Test", 1L)).thenReturn(List.of("Test"));
        mockMvc.perform(post("/api/tasks/1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tag\":\"Test\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.[0]").value("Test"))
                .andExpect(jsonPath("$.length()").value(1));
    }
    @Test
    void addTag_duplicateTag_returns409AndDontAddTag() throws Exception{
        when(taskService.addTag("Test", 1L)).thenThrow(new ExistingTagException());
        mockMvc.perform(post("/api/tasks/1/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tag\":\"Test\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Данный тэг был уже добавлен"));
    }
    @Test
    void removeTag_existingTag_returns204AndEmptyBody() throws Exception{
        when(taskService.removeTag("Test", 1L)).thenReturn(List.of("Work"));
        mockMvc.perform(delete("/api/tasks/1/tags/Test"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(taskService).removeTag("Test", 1L);
    }
    @Test
    void removeTag_notExistingTag_returns404NoServerError() throws Exception{
        when(taskService.removeTag("Test", 1L)).thenThrow(new NotExistingTagExceptions());
        mockMvc.perform(delete("/api/tasks/1/tags/Test"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Тэга для удаления не существует"));
    }
    @Test
    void deleteTask_existingTask_returns204AndDeleteTask() throws Exception{
        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(taskService).deleteTask(1L);
    }
    @Test
    void deleteTask_notExistingTask_returns404NoServerError() throws Exception{
        doThrow(new FoundTaskException()).when(taskService).deleteTask(99L);
        mockMvc.perform(delete("/api/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No tasks found"));
    }
    @Test
    void changePriority_existingId_returns200AndChangePriority() throws Exception{
        when(taskService.changePriority(TaskPriority.LOW, 1L))
                .thenReturn(taskResponse(1L, "Test", TaskStatus.NEW, TaskPriority.LOW));
        mockMvc.perform(patch("/api/tasks/1/priority")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"priority\":\"LOW\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").value("LOW"));
    }


    @Test
    void getStatistic_noTasks_returns200WithZeroCounts() throws Exception{
        when(taskService.getStatistic())
                .thenReturn(new TaskStatistic(List.of(), List.of()));

        mockMvc.perform(get("/api/tasks/statistic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasks").value(0))
                .andExpect(jsonPath("$.countByStatus.NEW").value(0))
                .andExpect(jsonPath("$.countByStatus.DONE").value(0));
    }

    @Test
    void getTasks_filteredByStatusAndPriority_returns200AndTasks() throws Exception{
        Page<TaskResponse> page = new PageImpl<>(
                List.of(taskResponse(1L, "Test", TaskStatus.NEW, TaskPriority.HIGH)));
        when(taskService.search(eq(TaskStatus.NEW), eq(TaskPriority.HIGH), isNull(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/tasks")
                        .param("status", "NEW").param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("NEW"))
                .andExpect(jsonPath("$.content[0].priority").value("HIGH"));
    }

    @Test
    void getTasks_unknownPriorityParam_returns400NoServerError() throws Exception{
        mockMvc.perform(get("/api/tasks")
                        .param("status", "NEW").param("priority", "ОЧЕНЬ_ВАЖНО"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        verifyNoInteractions(taskService);
    }

    @Test
    void getTasks_filteredByTitleFragment_returns200AndTasks() throws Exception{
        Page<TaskResponse> page = new PageImpl<>(
                List.of(taskResponse(1L, "Отчёт", TaskStatus.NEW, TaskPriority.HIGH)));
        when(taskService.search(isNull(), isNull(), eq("отч"), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/tasks").param("title", "отч"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Отчёт"));
    }

    @Test
    void getTasks_filterMatchesNothing_returns200AndEmptyContent() throws Exception{
        when(taskService.search(eq(TaskStatus.CANCELED), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/tasks").param("status", "CANCELED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void createTask_clientSuppliedIdAndStatus_ignoresThem() throws Exception{
        when(taskService.createTask(anyString(), any(TaskPriority.class), anyString()))
                .thenReturn(taskResponse(7L, "Sneaky", TaskStatus.NEW, TaskPriority.LOW));

        mockMvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":999,\"status\":\"DONE\",\"title\":\"Sneaky\"," +
                                 "\"priority\":\"LOW\",\"description\":\"Test\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.status").value("NEW"));

        verify(taskService).createTask("Sneaky", TaskPriority.LOW, "Test");
    }
}
