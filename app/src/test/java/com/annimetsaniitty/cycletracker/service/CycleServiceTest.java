package com.annimetsaniitty.cycletracker.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.annimetsaniitty.cycletracker.model.Cycle;
import com.annimetsaniitty.cycletracker.model.User;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class CycleServiceTest {

    @Test
    void calculatesCurrentDayAcrossMonthBoundaries() {
        Clock clock = Clock.fixed(Instant.parse("2026-02-16T08:00:00Z"), ZoneOffset.UTC);
        CycleService cycleService = new CycleService(null, null, null, clock);
        Cycle cycle = new Cycle(new User("anni", "anni@example.com", "hash"), LocalDate.of(2026, 1, 15));

        long currentDay = cycleService.getCurrentCycleDay(cycle, LocalDate.now(clock));

        assertThat(currentDay).isEqualTo(33);
    }

    @Test
    void returnsZeroForEndedCycles() {
        Clock clock = Clock.fixed(Instant.parse("2026-02-16T08:00:00Z"), ZoneOffset.UTC);
        CycleService cycleService = new CycleService(null, null, null, clock);
        Cycle cycle = new Cycle(new User("anni", "anni@example.com", "hash"), LocalDate.of(2026, 2, 1));
        cycle.end(LocalDate.of(2026, 2, 10));

        long currentDay = cycleService.getCurrentCycleDay(cycle, LocalDate.now(clock));

        assertThat(currentDay).isZero();
    }
}
