package com.annimetsaniitty.cycletracker.controller;

import com.annimetsaniitty.cycletracker.dto.MedicationStatusResponse;
import com.annimetsaniitty.cycletracker.service.AuthTokenService;
import com.annimetsaniitty.cycletracker.service.MedicationService;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/medication")
public class MedicationController {
    private final MedicationService medicationService;
    private final AuthTokenService authTokenService;

    public MedicationController(MedicationService medicationService, AuthTokenService authTokenService) {
        this.medicationService = medicationService;
        this.authTokenService = authTokenService;
    }

    @GetMapping("/status/{userId}")
    public MedicationStatusResponse getMedicationStatus(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @PathVariable Long userId) {
        authTokenService.requireMatchingUserId(authorizationHeader, userId);
        return medicationService.getMedicationStatus(userId);
    }
}
