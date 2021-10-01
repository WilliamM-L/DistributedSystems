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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class RoomRecords extends UnicastRemoteObject implements IRoomRecords{

    public RoomRecords()  throws RemoteException{
        DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MM-yyyy").toFormatter(Locale.ENGLISH);
        // conversion ex: LocalDateTime date = LocalDateTime.from(LocalDate.parse("01-10-2021", formatter).atStartOfDay());

        LocalDate date = LocalDate.parse("01-10-2021", formatter);
        HashMap<Integer, RoomRecord[]> entry = new HashMap<>();
        entry.put(1, new RoomRecord[]{new RoomRecord(new TimeSlot(LocalTime.of(6,0)), false)});
        roomRecords.put(date, entry);

        date = date.plusDays(1);
        entry = new HashMap<>();
        entry.put(1, new RoomRecord[]{new RoomRecord(new TimeSlot(LocalTime.of(6,0)), false)});
        roomRecords.put(date, entry);

        date = date.plusDays(1);
        entry = new HashMap<>();
        entry.put(1, new RoomRecord[]{new RoomRecord(new TimeSlot(LocalTime.of(6,0)), false)});
        roomRecords.put(date, entry);

    }

    // ADMIN
    public String createRoom(int room_Number, LocalDate date, TimeSlot[] list_Of_Time_Slots) throws java.rmi.RemoteException{
        // todo: try waiting to see if middleware starts a thread per request: it does!
//        try {
//            TimeUnit.SECONDS.sleep(5);
//        }catch (Exception e){
//            System.out.println(e.getStackTrace());
//        }
        return "room created yo";
    }
    public String  deleteRoom (int room_Number, LocalDate date, TimeSlot[] list_Of_Time_Slots) throws java.rmi.RemoteException{
        return "room deleted";
    }
    // STUDENT
    public String bookRoom(String campusName, int roomNumber, LocalDate date, TimeSlot timeslot) throws java.rmi.RemoteException{
        return "room booked";
    }
    public String getAvailableTimeSlot(LocalDate date) throws java.rmi.RemoteException{
        return "time slot got";
    }
    public String cancelBooking(String bookingID)  throws java.rmi.RemoteException{
        return "booking cancelled";
    }
}
