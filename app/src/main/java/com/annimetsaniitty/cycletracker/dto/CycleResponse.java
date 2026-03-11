package com.annimetsaniitty.cycletracker.dto;

import java.time.LocalDate;

public record CycleResponse(
        Long id,
        Long userId,
        LocalDate startDate,
        LocalDate endDate,
        boolean active,
        long currentDay) {
}
