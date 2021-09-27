package com.DistributedSystems.local;

public class RoomRecord {
    String recordID;
    TimeSlot timeSlot;
    boolean booked;

    public RoomRecord(TimeSlot timeSlot, boolean booked) {
        this.recordID = "RR" + Math.floor(Math.random()*1000000);
        this.timeSlot = timeSlot;
        this.booked = booked;
    }
}
