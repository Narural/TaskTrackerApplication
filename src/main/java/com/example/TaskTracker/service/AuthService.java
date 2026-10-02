package com.example.TaskTracker.service;
import com.example.TaskTracker.dto.RegistrationRequest;
import com.example.TaskTracker.dto.UserResponse;
import com.example.TaskTracker.model.AppUser;
import com.example.TaskTracker.model.Role;
import com.example.TaskTracker.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class AuthService {
    private final AppUserRepository userRepository;
    private final PasswordEncoder encoder;

    public AuthService(AppUserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    @Transactional
    public UserResponse register(RegistrationRequest request){
        String hash = encoder.encode(request.getPassword());
        AppUser user = new AppUser(request.getUsername(), hash, Role.USER);
        return new UserResponse(userRepository.save(user));
    }
}
