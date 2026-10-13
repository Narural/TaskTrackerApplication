package com.example.TaskTracker.config;

import com.example.TaskTracker.model.AppUser;
import com.example.TaskTracker.model.Role;
import com.example.TaskTracker.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(AdminProperties.class)
public class AdminBootstrap implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final AdminProperties properties;
    private final AppUserRepository userRepository;
    private final PasswordEncoder encoder;

    public AdminBootstrap(AdminProperties properties, AppUserRepository userRepository, PasswordEncoder encoder) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        String password = properties.password();
        if (password == null || password.isBlank()) {
            log.warn("ADMIN_PASSWORD не задан, первый администратор не создаётся");
            return;
        }
        String username = properties.username();
        if (userRepository.findByUsername(username).isPresent()) {
            log.info("Пользователь {} уже есть, администратор не создаётся", username);
            return;
        }
        userRepository.save(new AppUser(username, encoder.encode(password), Role.ADMIN));
        log.info("Создан администратор {}", username);
    }
}