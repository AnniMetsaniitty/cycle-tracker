package com.annimetsaniitty.cycletracker.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void normalizesRegistrationFieldsBeforeSavingAndCheckingDuplicates() throws Exception {
        String firstPayload = objectMapper.writeValueAsString(Map.of(
                "username", "  normalize-user  ",
                "password", "secret123",
                "email", "  Normalize.User@Example.com  "));

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("normalize-user")))
                .andExpect(jsonPath("$.email", is("normalize.user@example.com")))
                .andExpect(jsonPath("$.accessToken").isNotEmpty());

        String duplicateUsernamePayload = objectMapper.writeValueAsString(Map.of(
                "username", "normalize-user",
                "password", "secret123",
                "email", "different@example.com"));

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateUsernamePayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Username already exists: normalize-user")));

        String duplicateEmailPayload = objectMapper.writeValueAsString(Map.of(
                "username", "different-user",
                "password", "secret123",
                "email", "NORMALIZE.USER@example.com"));

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateEmailPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Email already exists: normalize.user@example.com")));
    }

    @Test
    void rejectsPasswordsShorterThanMinimumLength() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
                "username", "shortpass-user",
                "password", "short7",
                "email", "shortpass@example.com"));

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("password: must be at least 8 characters")));
    }
}
