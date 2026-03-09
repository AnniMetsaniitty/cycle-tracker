package com.annimetsaniitty.cycletracker;

import com.annimetsaniitty.cycletracker.service.CycleService;

public class App {
    public static void main(String[] args) {
        CycleService cycleService = new CycleService();

        System.out.println("=== Medication & Cycle Tracker ===");

        cycleService.startNewCycle();

        int currentDay = cycleService.getCurrentCycleDay();
        System.out.println("Current cycle day: " + currentDay);

        if (cycleService.isMedicationActive()) {
            System.out.println("Medication is active.");
        } else {
            System.out.println("Medication is not active.");
        }

        cycleService.endCurrentCycle();
    }
}