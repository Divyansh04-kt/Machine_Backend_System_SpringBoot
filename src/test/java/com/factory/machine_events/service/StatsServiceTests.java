package com.factory.machine_events.service;

import com.factory.machine_events.model.Event;
import com.factory.machine_events.store.EventStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatsServiceTest {

    private EventStore store;
    private StatsService statsService;

    @BeforeEach
    void setup() {
        store = new EventStore();
        statsService = new StatsService(store);
    }

//    // ✅ Test Case 6: defectCount = -1 ignored
//    @Test
//    void defectCountMinusOneIsIgnored() {
//
//        Event event1 = new Event();
//        event1.setEventId("E1");
//        event1.setMachineId("M1");
//        event1.setLineId("L1");
//        event1.setEventTime(Instant.now());
//        event1.setDurationMs(1000);
//        event1.setDefectCount(3);
//        event1.setReceivedTime(Instant.now());
//
//        Event event2 = new Event();
//        event2.setEventId("E2");
//        event2.setMachineId("M1");
//        event2.setLineId("L1");
//        event2.setEventTime(Instant.now());
//        event2.setDurationMs(1000);
//        event2.setDefectCount(-1); // unknown
//        event2.setReceivedTime(Instant.now());
//
//        store.compute("E1", (id, old) -> event1);
//        store.compute("E2", (id, old) -> event2);
//
//        var stats = statsService.getStats(
//                "M1",
//                Instant.now().minusSeconds(60),
//                Instant.now().plusSeconds(60)
//        );
//
//        assertEquals(2, stats.eventsCount());   // both events counted
//        assertEquals(3, stats.defectsCount());  // -1 ignored
//    }
// ✅ Test Case 7: start inclusive, end exclusive
@Test
void startInclusiveEndExclusive() {

    Instant T = Instant.parse("2026-01-15T10:00:00Z");

    Event event = new Event();
    event.setEventId("E3");
    event.setMachineId("M1");
    event.setLineId("L1");
    event.setEventTime(T);
    event.setDurationMs(1000);
    event.setDefectCount(1);
    event.setReceivedTime(Instant.now());

    store.compute("E3", (id, old) -> event);

    // start = T, end = T → should NOT count
    var stats1 = statsService.getStats("M1", T, T);
    assertEquals(0, stats1.eventsCount());

    // start = T, end = T + 1 second → SHOULD count
    var stats2 = statsService.getStats("M1", T, T.plusSeconds(1));
    assertEquals(1, stats2.eventsCount());
}

}

