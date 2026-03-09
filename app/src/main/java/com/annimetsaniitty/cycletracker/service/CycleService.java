package com.annimetsaniitty.cycletracker.service;

import com.annimetsaniitty.cycletracker.model.Cycle;
import java.time.LocalDate;
import java.time.Period;

public class CycleService {
    private Cycle currentCycle;

    public void startNewCycle() {
        if (currentCycle != null && currentCycle.isActive()) {
            currentCycle.setEndDate(LocalDate.now());
            System.out.println("Previous cycle ended on: " + currentCycle.getEndDate());
        }

        currentCycle = new Cycle(LocalDate.now());
        System.out.println("New cycle started on: " + currentCycle.getStartDate());
    }

    public void endCurrentCycle() {
        if (currentCycle == null || !currentCycle.isActive()) {
            System.out.println("No active cycle to end.");
            return;
        }

        currentCycle.setEndDate(LocalDate.now());
        System.out.println("Cycle ended on: " + currentCycle.getEndDate());
    }

    public int getCurrentCycleDay() {
        if (currentCycle == null || !currentCycle.isActive()) {
            return -1;
        }

        return Period.between(currentCycle.getStartDate(), LocalDate.now()).getDays() + 1;
    }

    public boolean isMedicationActive() {
        int day = getCurrentCycleDay();
        return day >= 16 && day <= 26;
    }

    public Cycle getCurrentCycle() {
        return currentCycle;
    }
}
