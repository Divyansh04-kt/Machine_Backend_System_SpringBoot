package com.factory.machine_events.controller;

import com.factory.machine_events.dto.StatsResponse;
import com.factory.machine_events.dto.TopDefectLineResponse;
import com.factory.machine_events.service.StatsService;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/stats")
public class StatsController {

    private final StatsService service;

    public StatsController(StatsService service) {
        this.service = service;
    }

    @GetMapping
    public StatsResponse stats(
            @RequestParam String machineId,
            @RequestParam Instant start,
            @RequestParam Instant end) {
        return service.getStats(machineId, start, end);
    }

    @GetMapping("/top-defect-lines")
    public List<TopDefectLineResponse> topDefectLines(
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam(defaultValue = "10") int limit) {
        return service.getTopDefectLines(from, to, limit);
    }
}

