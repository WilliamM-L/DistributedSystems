package com.DistributedSystems.asg2.RoomRecordsObj;


/**
* RoomRecordsObj/RoomRecordsCorbaOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from RoomRecords.idl
* Friday, October 15, 2021 9:26:23 o'clock PM EDT
*/

public interface RoomRecordsCorbaOperations 
{
  String createRoom (int room_Number, String date, String list_Of_Time_Slots, String userID);
  String deleteRoom (int room_Number, String date, String list_Of_Time_Slots, String userID);
  String bookRoom (int roomNumber, String date, String timeslot, String userID);
  String getAvailableTimeSlot (String dateText, String userID);
  String cancelBooking (String bookingID, String userID);
  String changeReservation (String booking_id, String new_campus_name, int new_room_no, String new_time_slot);
  void shutdown ();
} // interface RoomRecordsCorbaOperations