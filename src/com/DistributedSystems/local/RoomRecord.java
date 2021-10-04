package com.DistributedSystems.local;

import java.util.ArrayList;
import java.util.HashMap;
import static java.time.temporal.ChronoUnit.MINUTES;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RoomRecord {
    public String recordID;
    public TimeSlot timeSlot;
    public String booked_by;
    public final static String successPrefix = "Success: ";
    public final static String failurePrefix = "Failed: ";

    public static List<RoomRecord> makeFromTimeSlotList(TimeSlot[] timeSlots){
        List<RoomRecord> result = new ArrayList<>(timeSlots.length);
        for(TimeSlot timeSlot: timeSlots){
            result.add(new RoomRecord(timeSlot));
        }

        return  result;
    }

    public RoomRecord(TimeSlot timeSlot) {
        this.recordID = "RR" + (int)Math.floor(Math.random()*1000000);
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
        if (validNewRoomRecordList.size() == 0){
            msg = failurePrefix + "All given timeslots conflict with existing room records.";
        }
        else if(validNewRoomRecordList.size() < newRoomRecordList.size()){
            msg = successPrefix + "The conflicting timeslots were ignored, the valid ones were added.";
        } else {
            msg = successPrefix + "All timeslots were added.";
        }
        return  msg;
    }

    public static String deleteRoomRecordsFromTimeSlots(List<RoomRecord> roomRecordList, TimeSlot[] list_of_time_slots, ConcurrentHashMap<String, List<String>> studentBookingCount) {
        String msg;
        ArrayList<Integer> indicesToDelete = new ArrayList<>();
        RoomRecord roomRecord;
        List<String> studentBookingList;
        for (int i = 0; i < roomRecordList.size(); i++) {
            for (int j = 0; j < list_of_time_slots.length; j++) {
                roomRecord = roomRecordList.get(i);
                if (roomRecord.timeSlot.equals(list_of_time_slots[j])){
                    indicesToDelete.add(i);
                    // updating the student booking count if the room was booked
                    if (roomRecord.booked_by != null){
                        studentBookingList = studentBookingCount.get(roomRecord.booked_by);
                        studentBookingList.remove(roomRecord.recordID);
                    }
                }
            }
        }
        for(int index: indicesToDelete){
            roomRecordList.remove(index);
        }
        msg = indicesToDelete.size() + " out of "+ list_of_time_slots.length + " room records were deleted.";
        if (indicesToDelete.size() == 0){
            msg =  failurePrefix + msg;
        } else {
            msg = successPrefix + msg;
        }
        return msg;
    }

    public static String bookFromList(List<RoomRecord> roomRecordList, TimeSlot timeslot, ConcurrentHashMap<String, List<String>> studentBookingCount, String userID) {
        String confirmation = null;
        List<String> roomsBookedByStudent;
        for(RoomRecord roomRecord: roomRecordList){
            if (roomRecord.timeSlot.equals(timeslot)){
                roomsBookedByStudent = studentBookingCount.get(userID);
                if (roomsBookedByStudent == null){
                    roomsBookedByStudent = new ArrayList<>();
                    roomsBookedByStudent.add(roomRecord.recordID);
                    studentBookingCount.put(userID, roomsBookedByStudent);
                    confirmation = roomRecord.recordID;
                } else {
                    if (roomRecord.booked_by != null){
                        return failurePrefix + "Room is already booked.";
                    }
                    if (roomsBookedByStudent.size() < 3){
                        roomRecord.booked_by = userID;
                        confirmation = roomRecord.recordID;
                        // IMPORTANT : BOOKINGID IS JUST THE RECORDID FOR SIMPLICITY!
                        roomsBookedByStudent.add(roomRecord.recordID);
                    }else {
                        return  failurePrefix + "You have booked 3 rooms already, come back next week!";
                    }
                }
                break;
            }
        }
        if (confirmation != null){
            return successPrefix + confirmation;
        } else {
            return failurePrefix +"Room record could not be found.";
        }
    }

    private boolean intersect(RoomRecord roomRecord) {
        boolean startWithinTimeSlot;
        boolean endWithinTimeSlot;
        startWithinTimeSlot = this.timeSlot.start.isAfter(roomRecord.timeSlot.start) && this.timeSlot.start.isBefore(roomRecord.timeSlot.end);
        endWithinTimeSlot = this.timeSlot.end.isAfter(roomRecord.timeSlot.start) && this.timeSlot.end.isBefore(roomRecord.timeSlot.end);
        boolean timeSlotEntirelyWithin = this.timeSlot.start.isBefore(roomRecord.timeSlot.start) && this.timeSlot.end.isAfter(roomRecord.timeSlot.end);
        return startWithinTimeSlot || endWithinTimeSlot || timeSlotEntirelyWithin;
    }
}
