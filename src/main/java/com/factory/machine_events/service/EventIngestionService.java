package com.factory.machine_events.service;

import com.factory.machine_events.dto.BatchIngestResponse;
import com.factory.machine_events.model.Event;
import com.factory.machine_events.store.EventStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class EventIngestionService {

    private final EventStore store;
    private static final long MAX_DURATION = 6 * 60 * 60 * 1000;

    public EventIngestionService(EventStore store) {
        this.store = store;
    }

    public BatchIngestResponse ingest(List<Event> events) {

        BatchIngestResponse response = new BatchIngestResponse();

        for (Event incoming : events) {

            if (incoming.getDurationMs() < 0 || incoming.getDurationMs() > MAX_DURATION) {
                response.reject(incoming.getEventId(), "INVALID_DURATION");
                continue;
            }

            if (incoming.getEventTime().isAfter(Instant.now().plusSeconds(900))) {
                response.reject(incoming.getEventId(), "EVENT_TIME_IN_FUTURE");
                continue;
            }

            incoming.setReceivedTime(Instant.now());

            store.compute(incoming.getEventId(), (id, existing) -> {

                if (existing == null) {
                    response.accepted++;
                    return incoming;
                }

                if (existing.equals(incoming)) {
                    response.deduped++;
                    return existing;
                }

                if (incoming.getReceivedTime().isAfter(existing.getReceivedTime())) {
                    response.updated++;
                    return incoming;
                }

                return existing;
            });
        }

        return response;
    }
}
