package com.example.TaskTracker.service;
import com.example.TaskTracker.dto.LoginRequest;
import com.example.TaskTracker.dto.LoginResponse;
import com.example.TaskTracker.dto.RegistrationRequest;
import com.example.TaskTracker.dto.UserResponse;
import com.example.TaskTracker.model.AppUser;
import com.example.TaskTracker.model.Role;
import com.example.TaskTracker.repository.AppUserRepository;
import com.example.TaskTracker.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.AuthenticationException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final AppUserRepository userRepository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(AppUserRepository userRepository, PasswordEncoder encoder,
                       AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegistrationRequest request){
        String hash = encoder.encode(request.getPassword());
        AppUser user = new AppUser(request.getUsername(), hash, Role.USER);
        AppUser saved = userRepository.save(user);
        log.info("Зарегистрирован пользователь {}", saved.getUsername());
        return new UserResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse me(String username){
        return userRepository.findByUsername(username)
                .map(UserResponse::new)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }

    public LoginResponse login(LoginRequest request){
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (AuthenticationException e) {
            log.warn("Неудачный вход пользователя {}", request.getUsername());
            throw e;
        }
        List<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .toList();

        String token = jwtService.issue(authentication.getName(), roles);
        return new LoginResponse(token, jwtService.getLifetime().toSeconds());

    }
}
