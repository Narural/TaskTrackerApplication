package com.example.TaskTracker.dto;
import com.example.TaskTracker.validation.MaxUtf8Bytes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistrationRequest {
    @NotBlank(message = "Имя не должно быть пустым")
    @Size(max = 50, message = "Имя не может превышать 50 символов")
    private String username;

    @NotBlank(message = "Пароль не должен быть пустым")
    @Size(max = 70, message = "Пароль не может привышать 70 символов")
    @Size(min = 8, message = "Пароль не может быть короче 8 символов")
    @MaxUtf8Bytes(value = 72, message = "Пароль слишком длинный: не больше 72 байт (русская буква — 2 байта)")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
