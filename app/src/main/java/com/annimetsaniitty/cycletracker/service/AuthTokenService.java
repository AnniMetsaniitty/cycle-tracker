package com.annimetsaniitty.cycletracker.service;

import com.annimetsaniitty.cycletracker.exception.AuthenticationFailedException;
import com.annimetsaniitty.cycletracker.exception.AuthorizationFailedException;
import com.annimetsaniitty.cycletracker.model.User;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class AuthTokenService {
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int TOKEN_BYTE_LENGTH = 32;

    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, Long> tokenUserIds = new ConcurrentHashMap<>();

    public String issueToken(User user) {
        byte[] tokenBytes = new byte[TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        tokenUserIds.put(token, user.getId());
        return token;
    }

    public Long requireUserId(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new AuthenticationFailedException("Missing bearer token");
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isBlank()) {
            throw new AuthenticationFailedException("Missing bearer token");
        }

        Long userId = tokenUserIds.get(token);
        if (userId == null) {
            throw new AuthenticationFailedException("Invalid bearer token");
        }
        return userId;
    }

    public Long requireMatchingUserId(String authorizationHeader, Long requestedUserId) {
        Long authenticatedUserId = requireUserId(authorizationHeader);
        if (!authenticatedUserId.equals(requestedUserId)) {
            throw new AuthorizationFailedException("Not authorized for requested user");
        }
        return authenticatedUserId;
    }
}
