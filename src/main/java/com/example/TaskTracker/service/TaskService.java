package com.example.TaskTracker.service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import com.example.TaskTracker.model.AppUser;
import com.example.TaskTracker.model.Task;
import com.example.TaskTracker.model.TaskPriority;
import com.example.TaskTracker.model.TaskStatus;
import com.example.TaskTracker.exception.ChangeStatusException;
import com.example.TaskTracker.exception.FoundTaskException;
import com.example.TaskTracker.repository.AppUserRepository;
import com.example.TaskTracker.repository.TaskRepository;
import com.example.TaskTracker.dto.TaskResponse;
import com.example.TaskTracker.dto.TaskStatistic;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Владение решается здесь и только здесь: каждый метод получает имя того, кто пришёл,
 * и достаёт из базы только его строки. Чужая задача неотличима от несуществующей — 404.
 */
@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final AppUserRepository userRepository;

    public TaskService(TaskRepository taskRepository, AppUserRepository userRepository){
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    private Task findOwnedTask(long id, String username){
        return taskRepository.findByIdAndOwnerUsername(id, username)
                .orElseThrow(FoundTaskException::new);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> search(String username, TaskStatus status, TaskPriority priority,
                                     String title, String tag, Pageable pageable){
        return taskRepository.searchOwned(username, status, priority, title, tag, pageable)
                .map(TaskResponse::new);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> searchAll(TaskStatus status, TaskPriority priority,
                                        String title, String tag, Pageable pageable){
        return taskRepository.searchAll(status, priority, title, tag, pageable).map(TaskResponse::new);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(long id, String username){
        return new TaskResponse(findOwnedTask(id, username));
    }

    @Transactional
    public TaskResponse createTask(String title, TaskPriority priority, String description, String username){
        AppUser owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        Task newTask = new Task(title, priority, description, owner);
        return new TaskResponse(taskRepository.save(newTask));
    }

    @Transactional
    public TaskResponse changeStatus(TaskStatus status, long id, String username){
        Task newTask = findOwnedTask(id, username);
        if(newTask.getStatus().canMoveTo(status))
            newTask.setStatus(status);
        else
            throw new ChangeStatusException();
        return new TaskResponse(newTask);
    }

    @Transactional
    public TaskResponse changePriority(TaskPriority priority, long id, String username){
        Task newTask = findOwnedTask(id, username);
        newTask.setPriority(priority);
        return new TaskResponse(newTask);
    }

    @Transactional
    public List<String> addTag(String tag, long id, String username){
        Task newTask = findOwnedTask(id, username);
        newTask.addTag(tag);
        return new ArrayList<>(newTask.getTags());
    }

    @Transactional
    public List<String> removeTag(String tag, long id, String username){
        Task newTask = findOwnedTask(id, username);
        newTask.removeTag(tag);
        return new ArrayList<>(newTask.getTags());
    }

    @Transactional
    public void deleteTask(long id, String username){
        Task newTask = findOwnedTask(id, username);
        taskRepository.delete(newTask);
    }

    @Transactional(readOnly = true)
    public TaskStatistic getStatistic(String username){
        return new TaskStatistic(
                taskRepository.countGroupedByStatus(username),
                taskRepository.countGroupedByPriority(username));
    }
}
