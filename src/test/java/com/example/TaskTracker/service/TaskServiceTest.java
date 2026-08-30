package com.example.TaskTracker.service;
import com.example.TaskTracker.dto.TaskResponse;
import com.example.TaskTracker.exception.ChangeStatusException;
import com.example.TaskTracker.exception.ExistingTagException;
import com.example.TaskTracker.exception.FoundTaskException;
import com.example.TaskTracker.exception.NotExistingTagExceptions;
import com.example.TaskTracker.model.Task;
import com.example.TaskTracker.model.TaskPriority;
import com.example.TaskTracker.model.TaskStatus;
import com.example.TaskTracker.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {
    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;
    @Test
    void createTask_always_savesTaskWithStatusNew(){
        when(taskRepository.save(any(Task.class))).
                thenAnswer(call ->call.getArgument(0));

        TaskResponse response = taskService.createTask
                ("Test", TaskPriority.HIGH, "Test");

        ArgumentCaptor<Task> saved = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(TaskStatus.NEW);
        assertThat(response.getStatus()).isEqualTo(TaskStatus.NEW);
    }

    @Test
    void changeStatus_forbiddenTransition_throwsExceptionAndDoesNotSaveChanges(){
        Task task = new Task("Test", TaskPriority.HIGH, "Test");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        assertThatThrownBy(()-> taskService.changeStatus(TaskStatus.DONE, 1L))
                .isInstanceOf(ChangeStatusException.class);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.NEW);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void changeStatus_allowedTransition_applyAndSaveChanges(){
        Task task = new Task("Test", TaskPriority.HIGH, "Test");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        TaskResponse response = taskService.changeStatus(TaskStatus.CANCELED, 1L);

        assertThat(response.getStatus()).isEqualTo(TaskStatus.CANCELED);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.CANCELED);
        verify(taskRepository).findById(1L);
    }

    @Test
    void changePriority_allowedData_applyAndSaveChanges(){
        Task task = new Task("Test", TaskPriority.HIGH, "Test");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        TaskResponse response = taskService.changePriority(TaskPriority.LOW, 1L);
        assertThat(response.getPriority()).isEqualTo(TaskPriority.LOW);
        assertThat(task.getPriority()).isEqualTo(TaskPriority.LOW);
        verify(taskRepository).findById(1L);
    }

    @Test
    void addTag_duplicateData_throwsExceptionAndDontAddTag(){
        Task task = new Task("Test", TaskPriority.HIGH, "Test");
        task.addTag("test");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        assertThatThrownBy(()->taskService.addTag("test", 1L))
                .isInstanceOf(ExistingTagException.class);
        assertThat(task.getTags()).containsExactly("test");
        verify(taskRepository).findById(1L);
        verifyNoMoreInteractions(taskRepository);

    }
    @Test
    void addTag_unexistingTaskId_throwsException(){
        Task task = new Task("Test", TaskPriority.HIGH, "Test");
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() ->taskService.addTag("test", 1L))
                .isInstanceOf(FoundTaskException.class);
        verify(taskRepository).findById(1L);
        verifyNoMoreInteractions(taskRepository);
    }
    @Test
    void deleteTask_existingTask_deleteTask(){
        Task task = new Task("Test", TaskPriority.HIGH, "Test");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        taskService.deleteTask(1L);
        verify(taskRepository).delete(task);
    }
    @Test
    void removeTag_existingTag_returnsRemainingTags(){
        Task task = new Task("Test", TaskPriority.HIGH, "Test");
        task.addTag("Test1");
        task.addTag("Test");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        List<String> remaining = taskService.removeTag("Test1", 1L);

        assertThat(remaining).containsExactly("Test");
        assertThat(task.getTags()).containsExactly("Test");
    }
    @Test
    void removeTag_unexistingTag_throwsException(){
        Task task = new Task("Test", TaskPriority.HIGH, "Test");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        assertThatThrownBy(()->taskService.removeTag("test", 1L)).isInstanceOf(NotExistingTagExceptions.class);
        verify(taskRepository).findById(1L);
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void search_byStatus_returnsMappedResponsesAndKeepsPagingMetadata(){
        Pageable pageable = PageRequest.of(0, 20);
        Task task = new Task("Отчёт", TaskPriority.HIGH, "квартальный");
        when(taskRepository.search(TaskStatus.NEW, null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(task), pageable, 1));

        Page<TaskResponse> result = taskService.search(TaskStatus.NEW, null, null, pageable);

        assertThat(result.getContent())
                .extracting(TaskResponse::getTitle).containsExactly("Отчёт");
        assertThat(result.getContent())
                .extracting(TaskResponse::getStatus).containsExactly(TaskStatus.NEW);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void search_allFiltersNull_stillMapsEntitiesToResponses(){
        Pageable pageable = PageRequest.of(0, 20);
        Task task = new Task("Отчёт", TaskPriority.HIGH, "квартальный");
        when(taskRepository.search(null, null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(task), pageable, 1));

        Page<TaskResponse> result = taskService.search(null, null, null, pageable);

        assertThat(result.getContent())
                .extracting(TaskResponse::getPriority).containsExactly(TaskPriority.HIGH);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}
