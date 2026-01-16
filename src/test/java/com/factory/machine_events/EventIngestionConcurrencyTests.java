package com.factory.machine_events;

import com.factory.machine_events.model.Event;
import com.factory.machine_events.service.EventIngestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EventIngestionConcurrencyTests {

    @Autowired
    EventIngestionService service;

    @Test
    void duplicateEventIsDeduped() {
        Event e = baseEvent("E1");
        var response = service.ingest(List.of(e, e));
        assertEquals(1, response.accepted);
        assertEquals(1, response.deduped);
    }

    @Test
    void invalidDurationRejected() {
        Event e = baseEvent("E2");
        e.setDurationMs(-1);
        var response = service.ingest(List.of(e));
        assertEquals(1, response.rejected);
    }

    @Test
    void futureEventRejected() {
        Event e = baseEvent("E3");
        e.setEventTime(Instant.now().plusSeconds(2000));
        var response = service.ingest(List.of(e));
        assertEquals(1, response.rejected);
    }

    @Test
    void concurrentIngestionSafe() throws Exception {
        var pool = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            pool.submit(() -> service.ingest(List.of(baseEvent("E-CONCURRENT"))));
        }
        pool.shutdown();
        assertTrue(pool.awaitTermination(2, java.util.concurrent.TimeUnit.SECONDS));
    }

    private Event baseEvent(String id) {
        Event e = new Event();
        e.setEventId(id);
        e.setMachineId("M1");
        e.setLineId("L1");
        e.setEventTime(Instant.now());
        e.setDurationMs(1000);
        e.setDefectCount(1);
        return e;
    }
}

