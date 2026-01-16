package com.factory.machine_events.model;

import java.time.Instant;
import java.util.Objects;

public class Event {

    private String eventId;
    private Instant eventTime;
    private Instant receivedTime;
    private String machineId;
    private String lineId;
    private long durationMs;
    private int defectCount;

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public Instant getEventTime() { return eventTime; }
    public void setEventTime(Instant eventTime) { this.eventTime = eventTime; }

    public Instant getReceivedTime() { return receivedTime; }
    public void setReceivedTime(Instant receivedTime) { this.receivedTime = receivedTime; }

    public String getMachineId() { return machineId; }
    public void setMachineId(String machineId) { this.machineId = machineId; }

    public String getLineId() { return lineId; }
    public void setLineId(String lineId) { this.lineId = lineId; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

    public int getDefectCount() { return defectCount; }
    public void setDefectCount(int defectCount) { this.defectCount = defectCount; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;
        Event event = (Event) o;
        return durationMs == event.durationMs &&
                defectCount == event.defectCount &&
                Objects.equals(eventId, event.eventId) &&
                Objects.equals(eventTime, event.eventTime) &&
                Objects.equals(machineId, event.machineId) &&
                Objects.equals(lineId, event.lineId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, eventTime, machineId, lineId, durationMs, defectCount);
    }
}
