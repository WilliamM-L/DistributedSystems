package com.DistributedSystems.local;

public class RoomRecord {
    String recordID;
    TimeSlot timeSlot;
    String booked_by;

    public RoomRecord(TimeSlot timeSlot, boolean booked) {
        this.recordID = "RR" + Math.floor(Math.random()*1000000);
        this.timeSlot = timeSlot;
        this.booked_by = null;
    }
}
