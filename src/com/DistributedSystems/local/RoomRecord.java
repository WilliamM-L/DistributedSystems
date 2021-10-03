package com.DistributedSystems.local;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RoomRecord {
    public String recordID;
    public TimeSlot timeSlot;
    public String booked_by;

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
        if(validNewRoomRecordList.size() < newRoomRecordList.size()){
            msg = "The conflicting timeslots were ignored, the valid ones were added.";
        } else {
            msg = "All timeslots were added.";
        }
        return  msg;
    }

    public static String deleteRoomRecordsFromTimeSlots(List<RoomRecord> roomRecordList, TimeSlot[] list_of_time_slots, HashMap<String, List<String>> studentBookingCount) {
        String msg;
        ArrayList<Integer> indicesToDelete = new ArrayList<>();
        RoomRecord roomRecord;
        List<String> studentBookingList;
        for (int i = 0; i < roomRecordList.size(); i++) {
            for (int j = 0; j < list_of_time_slots.length; j++) {
                roomRecord = roomRecordList.get(i);
                if (roomRecord.timeSlot.equals(list_of_time_slots[j])){
                    indicesToDelete.add(i);
                    //todo reduce booking count for student
                    studentBookingList = studentBookingCount.get(roomRecord.booked_by);
                    studentBookingList.remove(roomRecord.recordID);
                }
            }
        }
        for(int index: indicesToDelete){
            roomRecordList.remove(index);
        }
        msg = indicesToDelete.size() + " out of "+ list_of_time_slots.length + " room records were deleted.";

//        int cout=0;
//        roomRecordList.removeIf( roomRecord -> {
//            boolean delete = false;
//            for (int j = 0; j < list_of_time_slots.length; j++) {
//                if (roomRecord.timeSlot.equals(list_of_time_slots[j])){
//                    delete = true;
//                }
//            }
//            return delete;
//        });
        return msg;
    }

    public static String bookFromList(List<RoomRecord> roomRecordList, TimeSlot timeslot, HashMap<String, List<String>> studentBookingCount, String userID) {
        boolean wasBooked = false;
        List<String> roomsBookedByStudent;
        for(RoomRecord roomRecord: roomRecordList){
            if (roomRecord.timeSlot.equals(timeslot)){
                wasBooked = true;
                roomsBookedByStudent = studentBookingCount.get(userID);
                if (roomsBookedByStudent == null){
                    roomsBookedByStudent = new ArrayList<>();
                    roomsBookedByStudent.add(roomRecord.recordID);
                    studentBookingCount.put(userID, roomsBookedByStudent);
                } else {
                    if (roomsBookedByStudent.size() < 3){
                        roomRecord.booked_by = userID;
                        // IMPORTANT : BOOKINGID IS JUST THE RECORDID FOR SIMPLICITY!
                        roomsBookedByStudent.add(roomRecord.recordID);
                    }else {
                        return "You have booked 3 rooms already, come back next week!";
                    }
                }
                break;
            }
        }
        if (wasBooked){
            return "Room record was booked.";
        } else {
            return "Room record could not be found.";
        }
    }

    private boolean intersect(RoomRecord roomRecord) {
        boolean startWithinTimeSlot = this.timeSlot.start.isAfter(roomRecord.timeSlot.start) && this.timeSlot.start.isBefore(roomRecord.timeSlot.end);
        boolean endWithinTimeSlot = this.timeSlot.end.isAfter(roomRecord.timeSlot.start) && this.timeSlot.end.isBefore(roomRecord.timeSlot.end);
        return startWithinTimeSlot || endWithinTimeSlot;
    }
}
