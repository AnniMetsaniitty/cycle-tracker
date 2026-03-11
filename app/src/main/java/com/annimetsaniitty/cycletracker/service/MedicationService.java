package com.annimetsaniitty.cycletracker.service;

import com.annimetsaniitty.cycletracker.dto.MedicationStatusResponse;
import com.annimetsaniitty.cycletracker.exception.ResourceNotFoundException;
import com.annimetsaniitty.cycletracker.model.Cycle;
import com.annimetsaniitty.cycletracker.model.Medication;
import com.annimetsaniitty.cycletracker.repository.CycleRepository;
import com.annimetsaniitty.cycletracker.repository.MedicationRepository;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MedicationService {
    private final CycleRepository cycleRepository;
    private final MedicationRepository medicationRepository;
    private final CycleService cycleService;
    private final Clock clock;

    public MedicationService(
            CycleRepository cycleRepository,
            MedicationRepository medicationRepository,
            CycleService cycleService,
            Clock clock) {
        this.cycleRepository = cycleRepository;
        this.medicationRepository = medicationRepository;
        this.cycleService = cycleService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public MedicationStatusResponse getMedicationStatus(Long userId) {
        LocalDate today = LocalDate.now(clock);
        Cycle cycle = cycleRepository.findByUserIdAndEndDateIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No active cycle for user: " + userId));
        Medication medication = medicationRepository.findByCycleId(cycle.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Medication plan missing for cycle: " + cycle.getId()));

        long currentDay = cycleService.getCurrentCycleDay(cycle, today);
        boolean active = currentDay >= medication.getStartDay() && currentDay <= medication.getEndDay();

        return new MedicationStatusResponse(
                cycle.getId(),
                currentDay,
                medication.getStartDay(),
                medication.getEndDay(),
                active,
                medication.isTaken());
    }
}
