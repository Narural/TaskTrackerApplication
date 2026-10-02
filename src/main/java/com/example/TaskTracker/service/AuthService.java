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

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
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
        return new UserResponse(userRepository.save(user));
    }

    public LoginResponse login(LoginRequest request){
        Authentication authentication = authenticationManager.authenticate
                (new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        List<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .toList();

        String token = jwtService.issue(authentication.getName(), roles);
        return new LoginResponse(token, JwtService.TOKEN_LIFETIME.toSeconds());

    }
}
