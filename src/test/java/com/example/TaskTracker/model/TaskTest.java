package com.example.TaskTracker.model;

import com.example.TaskTracker.model.TaskPriority;
import com.example.TaskTracker.model.TaskStatus;
import com.example.TaskTracker.exception.ExistingTagException;
import com.example.TaskTracker.exception.NotExistingTagExceptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TaskTest {
    @Test
    @DisplayName("Статус новой задачи всегла NEW")
    void taskConstructor_alwaysNew_isTrue() {
        Task task = new Task("title", TaskPriority.HIGH, "description");
        assertThat(task.getStatus()).isEqualTo(TaskStatus.NEW);
    }
    @Test
    @DisplayName("Список тэгов создаётся пыстум, а не null")
    void taskConstructor_tagsCreatedEmpty_isTrue(){
        Task task = new Task("title", TaskPriority.HIGH, "description");
        assertThat(task.getTags()).isNotNull();
    }

    @Test
    @DisplayName("Тэг добавляется и содержится")
    void addTag_tagAddsAndContains_isTrue(){
        Task task = new Task("title", TaskPriority.HIGH, "description");
        task.addTag("rawr");
        assertThat(task.getTags()).contains("rawr");
    }

    @Test
    @DisplayName("Добавление повторного тэга возвращает ошибку")
    void addTag_duplicateTag_throwsExistingTagException(){
        Task task = new Task("title", TaskPriority.HIGH, "description");
        task.addTag("rawr");
        assertThatThrownBy(() -> task.addTag("rawr"))
                .isInstanceOf(ExistingTagException.class);
        assertThat(task.getTags()).containsExactly("rawr");
    }

    @Test
    @DisplayName("Удаление происходит успешно")
    void removeTag_tagRemoves_isTrue(){
        Task task = new Task("title", TaskPriority.HIGH, "description");
        task.addTag("rawr");
        task.removeTag("rawr");
        assertThat(task.getTags()).isEmpty();
    }

    @Test
    @DisplayName("Попытка удаления несуществующего тэга выбрасывает исключение")
    void removeTag_removeNotExistingTag_throwsNotExistingTagException(){
        Task task = new Task("title", TaskPriority.HIGH, "description");
        task.addTag("rawr");
        assertThatThrownBy(() -> task.removeTag("raw"))
                .isInstanceOf(NotExistingTagExceptions.class);
        assertThat(task.getTags()).containsExactly("rawr");
    }


}
