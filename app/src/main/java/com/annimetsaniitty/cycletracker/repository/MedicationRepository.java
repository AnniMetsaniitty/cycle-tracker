package com.annimetsaniitty.cycletracker.repository;

import com.annimetsaniitty.cycletracker.model.Medication;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationRepository extends JpaRepository<Medication, Long> {
    Optional<Medication> findByCycleId(Long cycleId);
}
