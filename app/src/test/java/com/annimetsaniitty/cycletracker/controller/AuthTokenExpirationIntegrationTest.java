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

@SpringBootTest(properties = "cycletracker.auth.token-ttl=PT0S")
@AutoConfigureMockMvc
class AuthTokenExpirationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void rejectsExpiredBearerToken() throws Exception {
        String userPayload = objectMapper.writeValueAsString(Map.of(
                "username", "expired-token-user",
                "password", "secret123",
                "email", "expired-token-user@example.com"));

        String response = mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("expired-token-user")))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long userId = objectMapper.readTree(response).get("id").asLong();
        String accessToken = objectMapper.readTree(response).get("accessToken").asText();

        mockMvc.perform(get("/cycle/history/{userId}", userId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Expired bearer token")));
    }
}
