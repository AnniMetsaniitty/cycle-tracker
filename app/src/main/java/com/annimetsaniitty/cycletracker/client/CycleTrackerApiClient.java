package com.annimetsaniitty.cycletracker.client;

import com.annimetsaniitty.cycletracker.dto.CycleResponse;
import com.annimetsaniitty.cycletracker.dto.LoginRequest;
import com.annimetsaniitty.cycletracker.dto.MedicationStatusResponse;
import com.annimetsaniitty.cycletracker.dto.RegisterRequest;
import com.annimetsaniitty.cycletracker.dto.StartCycleRequest;
import com.annimetsaniitty.cycletracker.dto.UserResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class CycleTrackerApiClient {
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public CycleTrackerApiClient(String baseUrl) {
        this(HttpClient.newHttpClient(), new ObjectMapper().findAndRegisterModules(), baseUrl);
    }

    CycleTrackerApiClient(HttpClient httpClient, ObjectMapper objectMapper, String baseUrl) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public UserResponse register(String username, String email, String password) {
        return sendJsonRequest("/user/register", "POST", new RegisterRequest(username, password, email), UserResponse.class);
    }

    public UserResponse login(String username, String password) {
        return sendJsonRequest("/user/login", "POST", new LoginRequest(username, password), UserResponse.class);
    }

    public CycleResponse startCycle(Long userId) {
        return sendJsonRequest("/cycle/start", "POST", new StartCycleRequest(userId), CycleResponse.class);
    }

    public CycleResponse endCycle(Long userId) {
        return sendRequest("/cycle/end/" + userId, "POST", null, CycleResponse.class);
    }

    public CycleResponse getCurrentCycle(Long userId) {
        return sendRequest("/cycle/current/" + userId, "GET", null, CycleResponse.class);
    }

    public List<CycleResponse> getCycleHistory(Long userId) {
        return sendRequest("/cycle/history/" + userId, "GET", null, new TypeReference<List<CycleResponse>>() {
        });
    }

    public MedicationStatusResponse getMedicationStatus(Long userId) {
        return sendRequest("/medication/status/" + userId, "GET", null, MedicationStatusResponse.class);
    }

    private <T> T sendJsonRequest(String path, String method, Object payload, Class<T> responseType) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            return sendRequest(path, method, json, responseType);
        } catch (JsonProcessingException exception) {
            throw new ApiClientException("Failed to serialize request payload", exception);
        }
    }

    private <T> T sendRequest(String path, String method, String body, Class<T> responseType) {
        try {
            HttpResponse<String> response = send(path, method, body);
            return objectMapper.readValue(response.body(), responseType);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiClientException("Failed to call backend API", exception);
        } catch (IOException exception) {
            throw new ApiClientException(buildReadErrorMessage(exception), exception);
        }
    }

    private <T> T sendRequest(String path, String method, String body, TypeReference<T> responseType) {
        try {
            HttpResponse<String> response = send(path, method, body);
            return objectMapper.readValue(response.body(), responseType);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiClientException("Failed to call backend API", exception);
        } catch (IOException exception) {
            throw new ApiClientException(buildReadErrorMessage(exception), exception);
        }
    }

    private HttpResponse<String> send(String path, String method, String body) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json");

        if (body != null) {
            requestBuilder.header("Content-Type", "application/json");
        }

        HttpRequest request = requestBuilder.method(
                method,
                body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new ApiClientException(extractErrorMessage(response.body(), response.statusCode()));
        }
        return response;
    }

    private String extractErrorMessage(String responseBody, int statusCode) {
        try {
            Map<String, Object> payload = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {
            });
            Object message = payload.get("message");
            if (message instanceof String errorMessage && !errorMessage.isBlank()) {
                return errorMessage;
            }
        } catch (JsonProcessingException ignored) {
            // Fall through to the generic error message.
        }
        return "Backend request failed with status " + statusCode;
    }

    private String buildReadErrorMessage(IOException exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return "Failed to read backend API response";
        }
        return "Failed to read backend API response: " + message;
    }
}
