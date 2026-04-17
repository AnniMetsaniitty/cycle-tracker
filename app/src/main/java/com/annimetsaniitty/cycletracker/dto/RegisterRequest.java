package com.annimetsaniitty.cycletracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String username,
        @NotBlank @Size(min = 8, message = "must be at least 8 characters") String password,
        @Email @NotBlank String email) {

    public RegisterRequest {
        if (username != null) {
            username = username.trim();
        }
        if (email != null) {
            email = email.trim();
        }
    }
}
