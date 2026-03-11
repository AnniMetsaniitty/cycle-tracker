package com.annimetsaniitty.cycletracker.controller;

import com.annimetsaniitty.cycletracker.dto.CycleResponse;
import com.annimetsaniitty.cycletracker.dto.StartCycleRequest;
import com.annimetsaniitty.cycletracker.service.CycleService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cycle")
public class CycleController {
    private final CycleService cycleService;

    public CycleController(CycleService cycleService) {
        this.cycleService = cycleService;
    }

    @PostMapping("/start")
    public CycleResponse startCycle(@Valid @RequestBody StartCycleRequest request) {
        return cycleService.startNewCycle(request.userId());
    }

    @PostMapping("/end/{userId}")
    public CycleResponse endCycle(@PathVariable Long userId) {
        return cycleService.endCurrentCycle(userId);
    }

    @GetMapping("/current/{userId}")
    public CycleResponse getCurrentCycle(@PathVariable Long userId) {
        return cycleService.getCurrentCycle(userId);
    }

    @GetMapping("/history/{userId}")
    public List<CycleResponse> getCycleHistory(@PathVariable Long userId) {
        return cycleService.getCycleHistory(userId);
    }
}
