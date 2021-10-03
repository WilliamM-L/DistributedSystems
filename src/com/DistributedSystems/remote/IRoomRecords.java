package com.DistributedSystems.remote;
import java.rmi.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.DistributedSystems.local.RoomRecord;
import com.DistributedSystems.local.TimeSlot;

public interface IRoomRecords extends Remote {
    ConcurrentHashMap<LocalDate,HashMap<Integer, List<RoomRecord>>> roomRecords = new ConcurrentHashMap<>();
    // studentID: list of
    ConcurrentHashMap<String, List<String>> studentBookingCount = new ConcurrentHashMap<>();
    // ADMIN
    String createRoom(int room_Number, LocalDate date, TimeSlot[] list_Of_Time_Slots) throws java.rmi.RemoteException;
    String  deleteRoom (int room_Number, LocalDate date, TimeSlot[] list_Of_Time_Slots) throws java.rmi.RemoteException;
    // STUDENT
    String bookRoom(int roomNumber, LocalDate date, TimeSlot timeslot, String userID) throws java.rmi.RemoteException;
    String getAvailableTimeSlot(LocalDate date) throws java.rmi.RemoteException;
    String getAvailableTimeSlot(String dateText) throws java.rmi.RemoteException;
    String cancelBooking(String bookingID)  throws java.rmi.RemoteException;

} //end interface
