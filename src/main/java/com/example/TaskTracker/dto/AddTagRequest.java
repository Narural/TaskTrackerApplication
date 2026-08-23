package com.example.TaskTracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AddTagRequest {
    @NotBlank(message = "Тэг не может быть пустым")
    @Size(max = 100)
    public String tag;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
