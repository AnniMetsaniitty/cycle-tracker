package com.annimetsaniitty.cycletracker.dto;

import jakarta.validation.constraints.NotNull;

public record StartCycleRequest(@NotNull Long userId) {
}
