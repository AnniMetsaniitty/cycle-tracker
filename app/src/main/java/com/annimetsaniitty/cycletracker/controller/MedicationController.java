package com.annimetsaniitty.cycletracker.controller;

import com.annimetsaniitty.cycletracker.dto.MedicationStatusResponse;
import com.annimetsaniitty.cycletracker.service.MedicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/medication")
public class MedicationController {
    private final MedicationService medicationService;

    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @GetMapping("/status/{userId}")
    public MedicationStatusResponse getMedicationStatus(@PathVariable Long userId) {
        return medicationService.getMedicationStatus(userId);
    }
}
