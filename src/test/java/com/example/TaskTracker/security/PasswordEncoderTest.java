package com.example.TaskTracker.security;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
public class PasswordEncoderTest {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void encode_samePasswordTwice_givesDifferentHashes(){
        String first = encoder.encode("1234");
        String second = encoder.encode("1234");
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void matches_rightPassword_true_wrongPassword_false(){
        String hash = encoder.encode("1234");
        assertThat(encoder.matches("1234", hash)).isTrue();
        assertThat(encoder.matches("123", hash)).isFalse();
    }

    @Test
    void encode_hashIsNotThePasswordItself() {
        String hash = encoder.encode("1234");

        assertThat(hash).isNotEqualTo("1234");
        assertThat(hash).hasSize(60);
    }
}
