package com.annimetsaniitty.cycletracker.service;

import com.annimetsaniitty.cycletracker.dto.LoginRequest;
import com.annimetsaniitty.cycletracker.dto.RegisterRequest;
import com.annimetsaniitty.cycletracker.dto.UserResponse;
import com.annimetsaniitty.cycletracker.exception.AuthenticationFailedException;
import com.annimetsaniitty.cycletracker.exception.InvalidStateException;
import com.annimetsaniitty.cycletracker.exception.ResourceNotFoundException;
import com.annimetsaniitty.cycletracker.model.User;
import com.annimetsaniitty.cycletracker.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new InvalidStateException("Username already exists: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new InvalidStateException("Email already exists: " + request.email());
        }

        User user = new User(
                request.username(),
                request.email(),
                passwordEncoder.encode(request.password()));
        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.username()));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthenticationFailedException("Invalid username or password");
        }

        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail());
    }
}
