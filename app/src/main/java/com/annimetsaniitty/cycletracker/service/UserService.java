package com.annimetsaniitty.cycletracker.service;

import com.annimetsaniitty.cycletracker.dto.LoginRequest;
import com.annimetsaniitty.cycletracker.dto.RegisterRequest;
import com.annimetsaniitty.cycletracker.dto.UserResponse;
import com.annimetsaniitty.cycletracker.exception.AuthenticationFailedException;
import com.annimetsaniitty.cycletracker.exception.InvalidStateException;
import com.annimetsaniitty.cycletracker.exception.ResourceNotFoundException;
import com.annimetsaniitty.cycletracker.model.User;
import com.annimetsaniitty.cycletracker.repository.UserRepository;
import java.util.Locale;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final AuthTokenService authTokenService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository, AuthTokenService authTokenService) {
        this.userRepository = userRepository;
        this.authTokenService = authTokenService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedUsername = normalizeUsername(request.username());
        String normalizedEmail = normalizeEmail(request.email());

        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new InvalidStateException("Username already exists: " + normalizedUsername);
        }
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new InvalidStateException("Email already exists: " + normalizedEmail);
        }

        User user = new User(
                normalizedUsername,
                normalizedEmail,
                passwordEncoder.encode(request.password()));
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.username()));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthenticationFailedException("Invalid username or password");
        }

        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), authTokenService.issueToken(user));
    }

    private String normalizeUsername(String username) {
        return username.trim();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
