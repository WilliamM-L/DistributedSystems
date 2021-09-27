package com.DistributedSystems.local;

import java.io.Serializable;
import java.time.LocalDateTime;

public class TimeSlot implements Serializable {
    public LocalDateTime start;
    public LocalDateTime  end;

    public TimeSlot(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    public TimeSlot(LocalDateTime start) {
        this.start = start;
        this.end = start.plusHours(1);
    }

    public TimeSlot() {
        this.start = LocalDateTime.now();
        this.end = start.plusHours(1);;
    }
}
