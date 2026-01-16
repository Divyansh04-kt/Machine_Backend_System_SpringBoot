package com.factory.machine_events.service;


import com.factory.machine_events.model.Event;
import com.factory.machine_events.store.EventStore;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class EventStoreTest {

    @Test
    void newerReceivedTimeShouldUpdate() {
        EventStore store = new EventStore();

        // Existing (older) event
        Event existing = baseEvent("E1");
        existing.setReceivedTime(Instant.now().minusSeconds(10));

        store.compute(existing.getEventId(), (id, old) -> existing);

        // Incoming event with newer receivedTime + different payload
        Event incoming = baseEvent("E1");
        incoming.setDurationMs(2000);
        incoming.setDefectCount(5);
        incoming.setReceivedTime(Instant.now());

        store.compute(incoming.getEventId(), (id, old) -> {
            if (old == null) return incoming;
            if (incoming.getReceivedTime().isAfter(old.getReceivedTime())) {
                return incoming;
            }
            return old;
        });

        Event stored = store.get("E1");

        assertEquals(2000, stored.getDurationMs());
        assertEquals(5, stored.getDefectCount());
    }

    @Test
    void olderReceivedTimeShouldBeIgnored() {
        EventStore store = new EventStore();

        // Newer event stored first
        Event newer = baseEvent("E2");
        newer.setDurationMs(1000);
        newer.setDefectCount(1);
        newer.setReceivedTime(Instant.now());

        store.compute(newer.getEventId(), (id, old) -> newer);

        // Older incoming event with different payload
        Event older = baseEvent("E2");
        older.setDurationMs(5000);
        older.setDefectCount(9);
        older.setReceivedTime(Instant.now().minusSeconds(30));

        store.compute(older.getEventId(), (id, old) -> {
            if (old == null) return older;
            if (older.getReceivedTime().isAfter(old.getReceivedTime())) {
                return older;
            }
            return old; // SHOULD KEEP NEWER
        });

        Event stored = store.get("E2");

        // Assert: older event did NOT overwrite
        assertEquals(1000, stored.getDurationMs());
        assertEquals(1, stored.getDefectCount());
    }

    @Test
    void identicalEventShouldDeduplicate() {
        EventStore store = new EventStore();

        Event e1 = baseEvent("E3");
        e1.setReceivedTime(Instant.now());

        Event e2 = baseEvent("E3");
        e2.setReceivedTime(Instant.now().plusSeconds(1));

        store.compute("E3", (id, old) -> e1);
        store.compute("E3", (id, old) -> {
            if (old.equals(e2)) {
                return old; // dedup
            }
            return e2;
        });

        Event stored = store.get("E3");

        assertEquals(e1.getDurationMs(), stored.getDurationMs());
        assertEquals(e1.getDefectCount(), stored.getDefectCount());
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

