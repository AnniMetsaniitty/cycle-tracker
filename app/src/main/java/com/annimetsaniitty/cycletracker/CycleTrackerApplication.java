package com.annimetsaniitty.cycletracker;

import java.time.Clock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CycleTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CycleTrackerApplication.class, args);
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
