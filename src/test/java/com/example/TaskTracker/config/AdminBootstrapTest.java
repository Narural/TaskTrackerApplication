package com.example.TaskTracker.config;

import com.example.TaskTracker.model.AppUser;
import com.example.TaskTracker.model.Role;
import com.example.TaskTracker.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(OutputCaptureExtension.class)
class AdminBootstrapTest {

    private static final String PASSWORD = "first-admin-pass-42";

    private final AppUserRepository userRepository = mock(AppUserRepository.class);
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void run_noAdminYet_savesAdminWithHashedPasswordAndDoesNotLogIt(CapturedOutput output) {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        new AdminBootstrap(new AdminProperties("admin", PASSWORD), userRepository, encoder).run(null);

        ArgumentCaptor<AppUser> saved = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getUsername()).isEqualTo("admin");
        assertThat(saved.getValue().getRole()).isEqualTo(Role.ADMIN);
        assertThat(saved.getValue().getPasswordHash()).isNotEqualTo(PASSWORD);
        assertThat(encoder.matches(PASSWORD, saved.getValue().getPasswordHash())).isTrue();
        assertThat(output).contains("Создан администратор admin").doesNotContain(PASSWORD);
    }

    @Test
    void run_userAlreadyExists_savesNothing() {
        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(new AppUser("admin", "hash", Role.ADMIN)));

        new AdminBootstrap(new AdminProperties("admin", PASSWORD), userRepository, encoder).run(null);

        verify(userRepository, never()).save(any(AppUser.class));
    }

    @Test
    void run_passwordNotSet_savesNothing() {
        new AdminBootstrap(new AdminProperties("admin", ""), userRepository, encoder).run(null);

        verify(userRepository, never()).save(any(AppUser.class));
    }
}