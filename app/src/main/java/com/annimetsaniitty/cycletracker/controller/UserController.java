package com.annimetsaniitty.cycletracker.controller;

import com.annimetsaniitty.cycletracker.dto.LoginRequest;
import com.annimetsaniitty.cycletracker.dto.RegisterRequest;
import com.annimetsaniitty.cycletracker.dto.UserResponse;
import com.annimetsaniitty.cycletracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }
}
