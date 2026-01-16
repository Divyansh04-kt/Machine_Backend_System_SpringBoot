package com.factory.machine_events.store;


import com.factory.machine_events.model.Event;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EventStore {

    private final ConcurrentHashMap<String, Event> store = new ConcurrentHashMap<>();

    public Event get(String eventId) {
        return store.get(eventId);
    }

    public Event compute(String eventId,
                         java.util.function.BiFunction<String, Event, Event> fn) {
        return store.compute(eventId, fn);
    }

    public Collection<Event> getAll() {
        return store.values();
    }
}
