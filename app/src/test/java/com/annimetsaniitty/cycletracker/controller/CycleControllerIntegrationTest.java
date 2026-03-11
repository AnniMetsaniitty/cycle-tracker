package com.annimetsaniitty.cycletracker.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class CycleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void startsCycleAndReturnsCurrentCycle() throws Exception {
        String userPayload = objectMapper.writeValueAsString(Map.of(
                "username", "anni",
                "password", "secret123",
                "email", "anni@example.com"));

        String response = mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("anni")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long userId = objectMapper.readTree(response).get("id").asLong();
        String cyclePayload = objectMapper.writeValueAsString(Map.of("userId", userId));

        mockMvc.perform(post("/cycle/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cyclePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(true)))
                .andExpect(jsonPath("$.currentDay", is(1)));

        mockMvc.perform(get("/cycle/current/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is((int) userId)))
                .andExpect(jsonPath("$.active", is(true)));

        mockMvc.perform(get("/medication/status/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(false)))
                .andExpect(jsonPath("$.medicationStartDay", is(16)))
                .andExpect(jsonPath("$.medicationEndDay", is(26)));

        String loginPayload = objectMapper.writeValueAsString(Map.of(
                "username", "anni",
                "password", "secret123"));

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("anni")));
    }
}
