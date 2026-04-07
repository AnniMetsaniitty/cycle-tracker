package com.annimetsaniitty.cycletracker.controller;

import com.annimetsaniitty.cycletracker.dto.CycleResponse;
import com.annimetsaniitty.cycletracker.dto.StartCycleRequest;
import com.annimetsaniitty.cycletracker.service.AuthTokenService;
import com.annimetsaniitty.cycletracker.service.CycleService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cycle")
public class CycleController {
    private final CycleService cycleService;
    private final AuthTokenService authTokenService;

    public CycleController(CycleService cycleService, AuthTokenService authTokenService) {
        this.cycleService = cycleService;
        this.authTokenService = authTokenService;
    }

    @PostMapping("/start")
    public CycleResponse startCycle(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Valid @RequestBody StartCycleRequest request) {
        Long userId = authTokenService.requireMatchingUserId(authorizationHeader, request.userId());
        return cycleService.startNewCycle(userId);
    }

    @PostMapping("/end/{userId}")
    public CycleResponse endCycle(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @PathVariable Long userId) {
        authTokenService.requireMatchingUserId(authorizationHeader, userId);
        return cycleService.endCurrentCycle(userId);
    }

    @GetMapping("/current/{userId}")
    public CycleResponse getCurrentCycle(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @PathVariable Long userId) {
        authTokenService.requireMatchingUserId(authorizationHeader, userId);
        return cycleService.getCurrentCycle(userId);
    }

    @GetMapping("/history/{userId}")
    public List<CycleResponse> getCycleHistory(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @PathVariable Long userId) {
        authTokenService.requireMatchingUserId(authorizationHeader, userId);
        return cycleService.getCycleHistory(userId);
    }
}
