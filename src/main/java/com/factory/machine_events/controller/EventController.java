package com.factory.machine_events.controller;

import com.factory.machine_events.dto.BatchIngestResponse;
import com.factory.machine_events.model.Event;
import com.factory.machine_events.service.EventIngestionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventIngestionService service;

    public EventController(EventIngestionService service) {
        this.service = service;
    }

    @PostMapping("/batch")
    public BatchIngestResponse ingest(@RequestBody List<Event> events) {
        return service.ingest(events);
    }
}

