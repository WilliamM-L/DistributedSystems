package com.DistributedSystems.remote;

import com.DistributedSystems.local.RoomRecord;
import com.DistributedSystems.local.TimeSlot;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.HashMap;

public class RoomRecords extends UnicastRemoteObject implements IRoomRecords{

    public RoomRecords()  throws RemoteException{
        //todo partially populate the hashmap
        roomRecords.put(
                new Date(), new HashMap<Integer, RoomRecord>()
        );
    }

    // ADMIN
    public String createRoom(int room_Number, Date date, TimeSlot[] list_Of_Time_Slots) throws java.rmi.RemoteException{
        return "room crated yo";
    }
    public String  deleteRoom (int room_Number, Date date, TimeSlot[] list_Of_Time_Slots) throws java.rmi.RemoteException{
        return "";
    }
    // STUDENT
    public String bookRoom(String campusName, int roomNumber, Date date, TimeSlot timeslot) throws java.rmi.RemoteException{
        return "";
    }
    public String getAvailableTimeSlot(Date date) throws java.rmi.RemoteException{
        return "";
    }
    public String cancelBooking(String bookingID)  throws java.rmi.RemoteException{
        return "";
    }
}
