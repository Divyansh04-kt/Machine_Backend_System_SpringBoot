package com.factory.machine_events.service;

import com.factory.machine_events.dto.StatsResponse;
import com.factory.machine_events.dto.TopDefectLineResponse;
import com.factory.machine_events.model.Event;
import com.factory.machine_events.store.EventStore;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private final EventStore store;

    public StatsService(EventStore store) {
        this.store = store;
    }

    public StatsResponse getStats(String machineId, Instant start, Instant end) {

        List<Event> events = store.getAll().stream()
                .filter(e -> e.getMachineId().equals(machineId))
                .filter(e -> !e.getEventTime().isBefore(start))
                .filter(e -> e.getEventTime().isBefore(end))
                .toList();

        long eventsCount = events.size();

        long defects = events.stream()
                .filter(e -> e.getDefectCount() != -1)
                .mapToLong(Event::getDefectCount)
                .sum();

        double hours = Duration.between(start, end).toSeconds() / 3600.0;
        double avgDefectRate = hours == 0 ? 0 : defects / hours;

        String status = avgDefectRate < 2.0 ? "Healthy" : "Warning";

        return new StatsResponse(machineId, start, end, eventsCount, defects, avgDefectRate, status);
    }

    public List<TopDefectLineResponse> getTopDefectLines(Instant from, Instant to, int limit) {

        Map<String, List<Event>> byLine = store.getAll().stream()
                .filter(e -> !e.getEventTime().isBefore(from))
                .filter(e -> e.getEventTime().isBefore(to))
                .collect(Collectors.groupingBy(Event::getLineId));

        return byLine.entrySet().stream()
                .map(e -> {
                    long eventCount = e.getValue().size();
                    long defects = e.getValue().stream()
                            .filter(ev -> ev.getDefectCount() != -1)
                            .mapToLong(Event::getDefectCount)
                            .sum();
                    double percent = eventCount == 0 ? 0 :
                            Math.round((defects * 10000.0 / eventCount)) / 100.0;
                    return new TopDefectLineResponse(e.getKey(), defects, eventCount, percent);
                })
                .sorted(Comparator.comparingLong(TopDefectLineResponse::totalDefects).reversed())
                .limit(limit)
                .toList();
    }
}
