package com.DistributedSystems.local;

import java.io.Serializable;
import java.time.LocalTime;

public class TimeSlot implements Serializable {
    public LocalTime start;
    public LocalTime  end;

    public TimeSlot(LocalTime start, LocalTime end) {
        this.start = start;
        this.end = end;
    }

    public TimeSlot(LocalTime start) {
        this.start = start;
        this.end = start.plusHours(1);
    }

    public TimeSlot() {
        this.start = LocalTime.now();
        this.end = start.plusHours(1);;
    }
}
