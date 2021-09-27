package com.DistributedSystems.remote;

import com.DistributedSystems.local.RoomRecord;
import com.DistributedSystems.local.TimeSlot;

import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

public class RoomRecords extends UnicastRemoteObject implements IRoomRecords{

    public RoomRecords()  throws RemoteException{
        //todo first key: LocalDate, use LocalDateTime in Timeslot only
        DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MM-yyyy").toFormatter(Locale.ENGLISH);

        LocalDateTime date = LocalDateTime.from(LocalDate.parse("01-10-2021", formatter).atStartOfDay());
        HashMap<Integer, RoomRecord> entry = new HashMap<Integer, RoomRecord>();
        entry.put(1, new RoomRecord(new TimeSlot(date.plusHours(6)), false));
        roomRecords.put(date, entry);

        date = date.plusDays(1);
        entry = new HashMap<Integer, RoomRecord>();
        entry.put(1, new RoomRecord(new TimeSlot(date.plusHours(6)), false));
        roomRecords.put(date, entry);

        date = date.plusDays(1);
        entry = new HashMap<Integer, RoomRecord>();
        entry.put(1, new RoomRecord(new TimeSlot(date.plusHours(6)), false));
        roomRecords.put(date, entry);

    }

    // ADMIN
    public String createRoom(int room_Number, Date date, TimeSlot[] list_Of_Time_Slots) throws java.rmi.RemoteException{
        // todo: try waiting to see if middleware starts a thread per request
        return "room created yo";
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
