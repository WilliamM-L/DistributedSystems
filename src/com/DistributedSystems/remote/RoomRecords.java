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
import java.util.*;
import java.util.concurrent.TimeUnit;

public class RoomRecords extends UnicastRemoteObject implements IRoomRecords{

    public RoomRecords()  throws RemoteException{
        DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MM-yyyy").toFormatter(Locale.ENGLISH);
        // conversion ex: LocalDateTime date = LocalDateTime.from(LocalDate.parse("01-10-2021", formatter).atStartOfDay());

        LocalDate date = LocalDate.parse("01-10-2021", formatter);
        HashMap<Integer, List<RoomRecord>> entry = new HashMap<>();
        List<RoomRecord> roomRecordList = new ArrayList<>();
        roomRecordList.add(new RoomRecord(new TimeSlot(LocalTime.of(6,0))));
        entry.put(1, roomRecordList);
        roomRecords.put(date, entry);

        date = date.plusDays(1);
        entry = new HashMap<>();
        roomRecordList = new ArrayList<>();
        roomRecordList.add(new RoomRecord(new TimeSlot(LocalTime.of(6,0))));
        entry.put(1, roomRecordList);
        roomRecords.put(date, entry);

        date = date.plusDays(1);
        entry = new HashMap<>();
        roomRecordList = new ArrayList<>();
        roomRecordList.add(new RoomRecord(new TimeSlot(LocalTime.of(6,0))));
        entry.put(1, roomRecordList);
        roomRecords.put(date, entry);

    }

    // ADMIN
    public String createRoom(int room_Number, LocalDate date, TimeSlot[] list_Of_Time_Slots) throws java.rmi.RemoteException{
        List<RoomRecord> roomRecordList, newRoomRecordList;
        HashMap<Integer, List<RoomRecord>> dayRooms = roomRecords.get(date);
        String msg = "Room records added successfully";

        if (dayRooms == null){
            dayRooms = new HashMap<>();
            roomRecordList = null;
        } else {
            roomRecordList = dayRooms.get(room_Number);
            // might be null
        }

        if (roomRecordList == null){
            roomRecordList =  RoomRecord.makeFromTimeSlotList(list_Of_Time_Slots);
        }else {
            newRoomRecordList =  RoomRecord.makeFromTimeSlotList(list_Of_Time_Slots);
            msg = RoomRecord.addValidRoomRecords(roomRecordList, newRoomRecordList);
        }

        dayRooms.put(room_Number, roomRecordList);
        roomRecords.put(date, dayRooms);
        return "room created yo";
        // todo: try waiting to see if middleware starts a thread per request: it does!
//        try {
//            TimeUnit.SECONDS.sleep(5);
//        }catch (Exception e){
//            System.out.println(e.getStackTrace());
//        }
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
