package com.DistributedSystems.local;

import java.util.ArrayList;
import java.util.List;

public class RoomRecord {
    String recordID;
    TimeSlot timeSlot;
    String booked_by;

    public static List<RoomRecord> makeFromTimeSlotList(TimeSlot[] timeSlots){
        List<RoomRecord> result = new ArrayList<>(timeSlots.length);
        for(TimeSlot timeSlot: timeSlots){
            result.add(new RoomRecord(timeSlot));
        }

        return  result;
    }

    public RoomRecord(TimeSlot timeSlot) {
        this.recordID = "RR" + Math.floor(Math.random()*1000000);
        this.timeSlot = timeSlot;
        this.booked_by = null;
    }

    public static String addValidRoomRecords(List<RoomRecord> roomRecordList, List<RoomRecord> newRoomRecordList) {
        String msg = null;
        boolean valid;
        List<RoomRecord> validNewRoomRecordList = new ArrayList<>();
        for(RoomRecord newRoomRecord : newRoomRecordList){
            valid = true;
            for (RoomRecord roomRecord : roomRecordList){
                if (newRoomRecord.intersect(roomRecord)){
                    valid = false;
                    break;
                }
            }
            if (valid){
                validNewRoomRecordList.add(newRoomRecord);
            }
        }
        roomRecordList.addAll(validNewRoomRecordList);
        return  msg;
    }

    private boolean intersect(RoomRecord roomRecord) {
        boolean startWithinTimeSlot = this.timeSlot.start.isAfter(roomRecord.timeSlot.start) && this.timeSlot.start.isBefore(roomRecord.timeSlot.end);
        boolean endWithinTimeSlot = this.timeSlot.end.isAfter(roomRecord.timeSlot.start) && this.timeSlot.end.isBefore(roomRecord.timeSlot.end);
        return startWithinTimeSlot || endWithinTimeSlot;
    }
}
