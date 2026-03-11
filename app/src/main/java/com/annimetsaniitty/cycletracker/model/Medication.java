package com.annimetsaniitty.cycletracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "medications")
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cycle_id", nullable = false, unique = true)
    private Cycle cycle;

    @Column(nullable = false)
    private int startDay;

    @Column(nullable = false)
    private int endDay;

    @Column(nullable = false)
    private boolean taken;

    protected Medication() {
    }

    public Medication(Cycle cycle, int startDay, int endDay) {
        this.cycle = cycle;
        this.startDay = startDay;
        this.endDay = endDay;
        this.taken = false;
    }

    public Long getId() {
        return id;
    }

    public Cycle getCycle() {
        return cycle;
    }

    public int getStartDay() {
        return startDay;
    }

    public int getEndDay() {
        return endDay;
    }

    public boolean isTaken() {
        return taken;
    }

    public void setTaken(boolean taken) {
        this.taken = taken;
    }
}
