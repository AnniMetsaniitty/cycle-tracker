package com.annimetsaniitty.cycletracker.repository;

import com.annimetsaniitty.cycletracker.model.Cycle;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CycleRepository extends JpaRepository<Cycle, Long> {
    Optional<Cycle> findByUserIdAndEndDateIsNull(Long userId);

    List<Cycle> findByUserIdOrderByStartDateDesc(Long userId);

    long countByUserIdAndEndDateIsNull(Long userId);
}
