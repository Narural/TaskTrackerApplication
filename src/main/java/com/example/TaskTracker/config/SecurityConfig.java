package com.example.TaskTracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import com.example.TaskTracker.dto.ErrorResponse;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private void writeError(HttpServletResponse response, HttpServletRequest request,
                            HttpStatus status, String code, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(),
                new ErrorResponse(status.value(), code, message, request.getRequestURI()));
    }
    @Bean
    SecurityFilterChain api(HttpSecurity http) throws Exception{
        return http
                .csrf(csrf->csrf.disable())
//                .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                writeError(response, request, HttpStatus.UNAUTHORIZED,
                                        "UNAUTHENTICATED", "Требуется аутентификация"))
                        .accessDeniedHandler((request, response, deniedException) ->
                                writeError(response, request, HttpStatus.FORBIDDEN,
                                        "ACCESS_DENIED", "Доступ запрещён")))
                .authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
                .requestMatchers("/api/tasks/statistic").hasRole("ADMIN")
                        .requestMatchers("/api/tasks/**").authenticated().anyRequest().denyAll())
                .build();
    }
    private final ObjectMapper objectMapper;

    public SecurityConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService users(PasswordEncoder encoder){
        return new InMemoryUserDetailsManager(
                User.withUsername("Naru")
                        .password(encoder.encode("testpass"))
                        .roles("ADMIN")
                        .build(),
                User.withUsername("Arrer")
                        .password(encoder.encode("Tpass"))
                        .roles("USER")
                        .build());
    }
}