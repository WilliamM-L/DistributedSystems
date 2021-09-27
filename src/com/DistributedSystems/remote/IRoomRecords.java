package com.DistributedSystems.remote;
import java.rmi.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;

import com.DistributedSystems.local.RoomRecord;
import com.DistributedSystems.local.TimeSlot;

public interface IRoomRecords extends Remote {
    HashMap<LocalDateTime,HashMap<Integer, RoomRecord>> roomRecords = new HashMap<>();
    // ADMIN
    String createRoom(int room_Number, Date date, TimeSlot[] list_Of_Time_Slots) throws java.rmi.RemoteException;
    String  deleteRoom (int room_Number, Date date, TimeSlot[] list_Of_Time_Slots) throws java.rmi.RemoteException;
    // STUDENT
    String bookRoom(String campusName, int roomNumber, Date date, TimeSlot timeslot) throws java.rmi.RemoteException;
    String getAvailableTimeSlot(Date date) throws java.rmi.RemoteException;
    String cancelBooking(String bookingID)  throws java.rmi.RemoteException;

} //end interface
