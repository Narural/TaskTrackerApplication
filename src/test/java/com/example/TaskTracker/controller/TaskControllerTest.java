package com.example.TaskTracker.controller;

import com.example.TaskTracker.dto.TaskResponse;
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
        mockMvc.perform(get("/api/task/90"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("No tasks found"));
    }

    @Test
    void createTask_blankTitle_returns400AndDontCreateTask() throws Exception{
        mockMvc.perform(post("/api/task").
                contentType(MediaType.APPLICATION_JSON).content(
    "{\"title\":\"\",\"priority\":\"HIGH\",\"description\":\"desc\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(taskService);
    }
    @Test
    void createTask_allowedData_createsNewTask() throws Exception{
        when(taskService.createTask("Test", TaskPriority.HIGH, "Test")).
                thenReturn(taskResponse(1L, "Test", TaskStatus.NEW, TaskPriority.HIGH));
        mockMvc.perform(post("/api/task").contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Test\",\"priority\":\"HIGH\",\"description\":\"Test\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/task/1"))
                .andExpect(jsonPath("$.id").value(1));
    }
    @Test
    void createTask_titleLengthOver100_returns400AndDontCreateTask() throws Exception{
        mockMvc.perform(post("/api/task").contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\""+"a".repeat(101)+
                        "\",\"priority\":\"HIGH\",\"description\":\"Test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Размер названия не должен превышать 100 символов"));
        verifyNoInteractions(taskService);
    }
    @Test
    void getAllTasks_noTasks_return200WithEmptyContent() throws Exception{
        when(taskService.getAllTasks(any(Pageable.class))).thenReturn(Page.empty());
        mockMvc.perform(get("/api/task"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }
    @Test
    void findByStatus_unknownStatus_returns400NoServerError() throws Exception{
        mockMvc.perform(get("/api/task/byStatus")
                .param("status", "Test"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(taskService);
    }
    @Test
    void findByStatus_allowedStatus_returns200AndTasksWithThisStatus() throws Exception{
        Page<TaskResponse> page = new PageImpl<>(List.of(
                taskResponse(1L, "Первая", TaskStatus.NEW, TaskPriority.HIGH),
                taskResponse(2L, "Вторая", TaskStatus.NEW, TaskPriority.HIGH)
        ));
        when(taskService.findByStatus(eq(TaskStatus.NEW), any(Pageable.class)))
                .thenReturn(page);
        mockMvc.perform(get("/api/task/byStatus")
                        .param("status", "NEW")).andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("Первая"))
                .andExpect(jsonPath("$.totalElements").value(2));
        verify(taskService).findByStatus(eq(TaskStatus.NEW), any(Pageable.class));
    }
    @Test
    void changeStatus_allowedTransition_returns200AndChangeStatus() throws Exception{
        when(taskService.changeStatus(TaskStatus.IN_PROCESS, 1L))
                .thenReturn(taskResponse(1L, "Test", TaskStatus.IN_PROCESS, TaskPriority.HIGH));
        mockMvc.perform(patch("/api/task/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"IN_PROCESS\"}")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROCESS"));
    }
    @Test
    void changeStatus_notAllowedTransition_returns400AdnDontChangeStatus() throws Exception{
        when(taskService.changeStatus(TaskStatus.DONE, 1L))
                .thenThrow(new ChangeStatusException());
        mockMvc.perform(patch("/api/task/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"DONE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Ошибка изменения статуса, " +
                                "статус можно менять только на 1 шаг"));
    }
    @Test
    void addTag_newTag_returns201AndAddsTag() throws Exception{
        when(taskService.addTag("Test", 1L)).thenReturn(List.of("Test"));
        mockMvc.perform(post("/api/task/1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tag\":\"Test\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.[0]").value("Test"))
                .andExpect(jsonPath("$.length()").value(1));
    }
    @Test
    void addTag_duplicateTag_returns409AndDontAddTag() throws Exception{
        when(taskService.addTag("Test", 1L)).thenThrow(new ExistingTagException());
        mockMvc.perform(post("/api/task/1/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tag\":\"Test\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Данный тэг был уже добавлен"));
    }
    @Test
    void removeTag_existingTag_returns200DeleteTagAndReturnsRemainingTagsList() throws Exception{
        when(taskService.removeTag("Test", 1L)).thenReturn(List.of("Work"));
        mockMvc.perform(delete("/api/task/1/tags/Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$.[0]").value("Work"));
    }
    @Test
    void removeTag_notExistingTag_returns404NoServerError() throws Exception{
        when(taskService.removeTag("Test", 1L)).thenThrow(new NotExistingTagExceptions());
        mockMvc.perform(delete("/api/task/1/tags/Test"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Тэга для удаления не существует"));
    }
    @Test
    void deleteTask_existingTask_returns204AndDeleteTask() throws Exception{
        mockMvc.perform(delete("/api/task/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(taskService).deleteTask(1L);
    }
    @Test
    void deleteTask_notExistingTask_returns404NoServerError() throws Exception{
        doThrow(new FoundTaskException()).when(taskService).deleteTask(99L);
        mockMvc.perform(delete("/api/task/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No tasks found"));
    }
    @Test
    void changePriority_existingId_returns200AndChangePriority() throws Exception{
        when(taskService.changePriority(TaskPriority.LOW, 1L))
                .thenReturn(taskResponse(1L, "Test", TaskStatus.NEW, TaskPriority.LOW));
        mockMvc.perform(patch("/api/task/1/priority")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"priority\":\"LOW\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").value("LOW"));
    }

}
