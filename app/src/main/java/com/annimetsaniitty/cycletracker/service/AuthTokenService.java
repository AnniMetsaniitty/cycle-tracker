package com.annimetsaniitty.cycletracker.service;

import com.annimetsaniitty.cycletracker.exception.AuthenticationFailedException;
import com.annimetsaniitty.cycletracker.exception.AuthorizationFailedException;
import com.annimetsaniitty.cycletracker.model.AuthToken;
import com.annimetsaniitty.cycletracker.model.User;
import com.annimetsaniitty.cycletracker.repository.AuthTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthTokenService {
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int TOKEN_BYTE_LENGTH = 32;

    private final AuthTokenRepository authTokenRepository;
    private final Clock clock;
    private final Duration tokenTtl;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthTokenService(
            AuthTokenRepository authTokenRepository,
            Clock clock,
            @Value("${cycletracker.auth.token-ttl:PT12H}") Duration tokenTtl) {
        this.authTokenRepository = authTokenRepository;
        this.clock = clock;
        this.tokenTtl = tokenTtl;
    }

    @Transactional
    public String issueToken(User user) {
        byte[] tokenBytes = new byte[TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        authTokenRepository.save(new AuthToken(hashToken(token), user, Instant.now(clock).plus(tokenTtl)));
        return token;
    }

    @Transactional(readOnly = true)
    public Long requireUserId(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new AuthenticationFailedException("Missing bearer token");
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isBlank()) {
            throw new AuthenticationFailedException("Missing bearer token");
        }

        AuthToken authToken = authTokenRepository.findByTokenHash(hashToken(token))
                .orElseThrow(() -> new AuthenticationFailedException("Invalid bearer token"));
        if (authToken.isExpired(Instant.now(clock))) {
            throw new AuthenticationFailedException("Expired bearer token");
        }
        return authToken.getUser().getId();
    }

    @Transactional(readOnly = true)
    public Long requireMatchingUserId(String authorizationHeader, Long requestedUserId) {
        Long authenticatedUserId = requireUserId(authorizationHeader);
        if (!authenticatedUserId.equals(requestedUserId)) {
            throw new AuthorizationFailedException("Not authorized for requested user");
        }
        return authenticatedUserId;
    }

    private String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                hash.append(String.format("%02x", value));
            }
            return hash.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 hashing is not available", exception);
        }
    }
}
