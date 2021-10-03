package com.DistributedSystems.local;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.Objects;

public class TimeSlot implements Serializable {
    public LocalTime start;
    public LocalTime  end;

    public static TimeSlot[] parseTimeSlots(String[] timeSlotText){
        LocalTime start,end;
        TimeSlot[] results = new TimeSlot[timeSlotText.length];
        String[] timeSlotPairs;
        for (int index = 0; index < timeSlotText.length; index++) {
            timeSlotPairs = timeSlotText[index].split("-");
            start = LocalTime.parse(timeSlotPairs[0]);
            end = LocalTime.parse(timeSlotPairs[1]);
            results[index] = new TimeSlot(start, end);
        }
        return results;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSlot timeSlot = (TimeSlot) o;
        return start.equals(timeSlot.start) && end.equals(timeSlot.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
}
