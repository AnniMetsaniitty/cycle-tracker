package com.annimetsaniitty.cycletracker.service;

import com.annimetsaniitty.cycletracker.dto.CycleResponse;
import com.annimetsaniitty.cycletracker.exception.InvalidStateException;
import com.annimetsaniitty.cycletracker.exception.ResourceNotFoundException;
import com.annimetsaniitty.cycletracker.model.Cycle;
import com.annimetsaniitty.cycletracker.model.Medication;
import com.annimetsaniitty.cycletracker.model.User;
import com.annimetsaniitty.cycletracker.repository.CycleRepository;
import com.annimetsaniitty.cycletracker.repository.MedicationRepository;
import com.annimetsaniitty.cycletracker.repository.UserRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CycleService {
    public static final int MEDICATION_START_DAY = 16;
    public static final int MEDICATION_END_DAY = 26;

    private final CycleRepository cycleRepository;
    private final UserRepository userRepository;
    private final MedicationRepository medicationRepository;
    private final Clock clock;

    public CycleService(
            CycleRepository cycleRepository,
            UserRepository userRepository,
            MedicationRepository medicationRepository,
            Clock clock) {
        this.cycleRepository = cycleRepository;
        this.userRepository = userRepository;
        this.medicationRepository = medicationRepository;
        this.clock = clock;
    }

    @Transactional
    public CycleResponse startNewCycle(Long userId) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        LocalDate today = LocalDate.now(clock);
        cycleRepository.findByUserIdAndEndDateIsNull(userId)
                .ifPresent(cycle -> cycle.end(today));

        Cycle cycle = cycleRepository.save(new Cycle(user, today));
        Medication medication = new Medication(cycle, MEDICATION_START_DAY, MEDICATION_END_DAY);
        medicationRepository.save(medication);
        cycle.setMedication(medication);

        return toResponse(cycle, today);
    }

    @Transactional
    public CycleResponse endCurrentCycle(Long userId) {
        userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Cycle cycle = cycleRepository.findByUserIdAndEndDateIsNull(userId)
                .orElseThrow(() -> new InvalidStateException("No active cycle for user: " + userId));
        LocalDate today = LocalDate.now(clock);
        cycle.end(today);
        return toResponse(cycle, today);
    }

    @Transactional(readOnly = true)
    public CycleResponse getCurrentCycle(Long userId) {
        LocalDate today = LocalDate.now(clock);
        Cycle cycle = cycleRepository.findByUserIdAndEndDateIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No active cycle for user: " + userId));
        return toResponse(cycle, today);
    }

    @Transactional(readOnly = true)
    public List<CycleResponse> getCycleHistory(Long userId) {
        LocalDate today = LocalDate.now(clock);
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found: " + userId);
        }
        return cycleRepository.findByUserIdOrderByStartDateDesc(userId).stream()
                .map(cycle -> toResponse(cycle, today))
                .toList();
    }

    public long getCurrentCycleDay(Cycle cycle, LocalDate today) {
        if (!cycle.isActive()) {
            return 0;
        }
        return ChronoUnit.DAYS.between(cycle.getStartDate(), today) + 1;
    }

    private CycleResponse toResponse(Cycle cycle, LocalDate today) {
        return new CycleResponse(
                cycle.getId(),
                cycle.getUser().getId(),
                cycle.getStartDate(),
                cycle.getEndDate(),
                cycle.isActive(),
                getCurrentCycleDay(cycle, today));
    }
}
