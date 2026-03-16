package com.annimetsaniitty.cycletracker.ui;

import com.annimetsaniitty.cycletracker.dto.CycleResponse;
import com.annimetsaniitty.cycletracker.dto.MedicationStatusResponse;
import java.util.List;

public record DashboardSnapshot(
        CycleResponse currentCycle,
        MedicationStatusResponse medicationStatus,
        List<CycleResponse> history,
        String nextMedicationText,
        long completedCycles,
        String averageCycleLength) {
}
