package com.annimetsaniitty.cycletracker.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.annimetsaniitty.cycletracker.repository.AuthTokenRepository;
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

    @Autowired
    private AuthTokenRepository authTokenRepository;

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
        String accessToken = objectMapper.readTree(response).get("accessToken").asText();
        assertThat(authTokenRepository.count()).isPositive();
        String cyclePayload = objectMapper.writeValueAsString(Map.of("userId", userId));

        mockMvc.perform(post("/cycle/start")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cyclePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(true)))
                .andExpect(jsonPath("$.currentDay", is(1)));

        mockMvc.perform(get("/cycle/current/{userId}", userId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is((int) userId)))
                .andExpect(jsonPath("$.active", is(true)));

        mockMvc.perform(get("/medication/status/{userId}", userId)
                        .header("Authorization", "Bearer " + accessToken))
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
                .andExpect(jsonPath("$.username", is("anni")))
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void rejectsCrossUserCycleAccess() throws Exception {
        String firstUserResponse = registerUser("cross-anni", "cross-anni@example.com");
        long firstUserId = objectMapper.readTree(firstUserResponse).get("id").asLong();
        String firstAccessToken = objectMapper.readTree(firstUserResponse).get("accessToken").asText();

        String secondUserResponse = registerUser("cross-other", "cross-other@example.com");
        long secondUserId = objectMapper.readTree(secondUserResponse).get("id").asLong();

        mockMvc.perform(get("/cycle/history/{userId}", secondUserId)
                        .header("Authorization", "Bearer " + firstAccessToken))
                .andExpect(status().isForbidden());

        String secondCyclePayload = objectMapper.writeValueAsString(Map.of("userId", secondUserId));
        mockMvc.perform(post("/cycle/start")
                        .header("Authorization", "Bearer " + firstAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondCyclePayload))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/cycle/history/{userId}", firstUserId)
                        .header("Authorization", "Bearer " + firstAccessToken))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsMissingBearerTokenForProtectedCycleAccess() throws Exception {
        mockMvc.perform(get("/cycle/history/{userId}", 1L))
                .andExpect(status().isUnauthorized());
    }

    private String registerUser(String username, String email) throws Exception {
        String userPayload = objectMapper.writeValueAsString(Map.of(
                "username", username,
                "password", "secret123",
                "email", email));

        return mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(username)))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }
}
