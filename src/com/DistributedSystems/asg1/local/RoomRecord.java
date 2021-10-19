package com.DistributedSystems.asg1.local;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RoomRecord {
    public String recordID;
    public TimeSlot timeSlot;
    public String booked_by;
    public String campusName;
    public final static String successPrefix = "Success: ";
    public final static String failurePrefix = "Failed: ";

    public static List<RoomRecord> makeFromTimeSlotList(TimeSlot[] timeSlots, String campusName){
        List<RoomRecord> result = new ArrayList<>(timeSlots.length);
        for(TimeSlot timeSlot: timeSlots){
            result.add(new RoomRecord(timeSlot, campusName));
        }

        return  result;
    }

    public RoomRecord(TimeSlot timeSlot, String campusName) {
        this.campusName = campusName;
        this.recordID = "RR" + (int)Math.floor(Math.random()*1000000) + campusName;
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
            roomRecord = roomRecordList.get(i);
            for (TimeSlot timeSlot : list_of_time_slots) {
                if (roomRecord.timeSlot.equals(timeSlot)) {
                    indicesToDelete.add(i);
                    // updating the student booking count if the room was booked
                    if (roomRecord.booked_by != null) {
                        studentBookingList = studentBookingCount.get(roomRecord.booked_by);
                        synchronized (studentBookingList){
                            studentBookingList.remove(roomRecord.recordID);
                        }
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

    public static synchronized String bookFromList(List<RoomRecord> roomRecordList, TimeSlot timeslot, ConcurrentHashMap<String, List<String>> studentBookingCount, String userID) {
        String confirmation = null;
        List<String> roomsBookedByStudent;
        for (RoomRecord roomRecord : roomRecordList) {
            if (roomRecord.timeSlot.equals(timeslot)) {
                roomsBookedByStudent = studentBookingCount.get(userID);
                if (roomRecord.booked_by != null) {
                    return failurePrefix + "Room is already booked.";
                } else {
                    if (roomsBookedByStudent == null) {
                        roomRecord.booked_by = userID;
                        confirmation = roomRecord.recordID;
                        // IMPORTANT : BOOKINGID IS JUST THE RECORDID FOR SIMPLICITY!
                        roomsBookedByStudent = new ArrayList<>();
                        roomsBookedByStudent.add(roomRecord.recordID);
                        studentBookingCount.put(userID, roomsBookedByStudent);
                        return successPrefix + confirmation;
                    } else {
                        if (roomsBookedByStudent.size() < 3) {
                            roomRecord.booked_by = userID;
                            confirmation = roomRecord.recordID;
                            // IMPORTANT : BOOKINGID IS JUST THE RECORDID FOR SIMPLICITY!
                            synchronized (roomsBookedByStudent){
                                roomsBookedByStudent.add(roomRecord.recordID);
                            }
                            return successPrefix + confirmation;
                        } else {
                            return failurePrefix + "You have booked 3 rooms already, come back next week!";
                        }
                    }
                }
            }
        }
        return failurePrefix + "Room record could not be found.";
    }

    private boolean intersect(RoomRecord roomRecord) {
        boolean startWithinTimeSlot = this.timeSlot.start.isAfter(roomRecord.timeSlot.start) && this.timeSlot.start.isBefore(roomRecord.timeSlot.end);
        boolean endWithinTimeSlot = this.timeSlot.end.isAfter(roomRecord.timeSlot.start) && this.timeSlot.end.isBefore(roomRecord.timeSlot.end);
        boolean timeSlotEntirelyWithin = this.timeSlot.start.isBefore(roomRecord.timeSlot.start) && this.timeSlot.end.isAfter(roomRecord.timeSlot.end);
        return startWithinTimeSlot || endWithinTimeSlot || timeSlotEntirelyWithin;
    }

    public static String extractCampusFromRecordID(String recordID){
        return recordID.substring(recordID.length() - 3);
    }
}
