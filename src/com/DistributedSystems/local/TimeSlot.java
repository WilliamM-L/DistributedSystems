package com.DistributedSystems.local;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;

public class TimeSlot implements Serializable {
    public LocalTime start;
    public LocalTime  end;
    public static final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder().appendPattern("hh:mm").toFormatter(Locale.ENGLISH);

    public static TimeSlot[] parseTimeSlots(String[] timeSlotText){
        LocalTime start,end;
        TimeSlot[] results = new TimeSlot[timeSlotText.length];
        String[] timeSlotPairs;
        for (int index = 0; index < timeSlotText.length; index++) {
            timeSlotPairs = timeSlotText[index].split("-");
            start = LocalTime.parse(timeSlotPairs[0], dateTimeFormatter);
            end = LocalTime.parse(timeSlotPairs[1], dateTimeFormatter);
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
}
