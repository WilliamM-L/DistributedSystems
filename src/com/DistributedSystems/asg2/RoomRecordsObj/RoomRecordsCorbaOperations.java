package com.DistributedSystems.asg2.RoomRecordsObj;


/**
* RoomRecordsObj/RoomRecordsCorbaOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from RoomRecords.idl
* Monday, October 18, 2021 9:05:05 o'clock PM EDT
*/

public interface RoomRecordsCorbaOperations 
{
  String createRoom (int room_Number, String date, String list_Of_Time_Slots, String userID);
  String deleteRoom (int room_Number, String date, String list_Of_Time_Slots, String userID);
  String bookRoom (String campusName, int roomNumber, String date, String timeslot, String userID);
  String getAvailableTimeSlot (String dateText, String userID);
  String cancelBooking (String bookingID, String userID);
  String changeReservation (String bookingID, String newCampusName, int newRoomNum, String newTimeSlot, String dateText, String userID);
  void shutdown ();
} // interface RoomRecordsCorbaOperations
