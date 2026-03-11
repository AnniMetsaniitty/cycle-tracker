package com.annimetsaniitty.cycletracker.dto;

public record MedicationStatusResponse(
        Long cycleId,
        long currentDay,
        int medicationStartDay,
        int medicationEndDay,
        boolean active,
        boolean taken) {
}
