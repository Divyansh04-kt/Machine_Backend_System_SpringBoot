package com.factory.machine_events.dto;

public record TopDefectLineResponse(
        String lineId,
        long totalDefects,
        long eventCount,
        double defectsPercent
) {}
