package com.DistributedSystems.remote;

import com.DistributedSystems.local.RoomRecord;
import com.DistributedSystems.local.TimeSlot;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
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
    public final static String logsFolder = "logs\\server\\";
    public final String campusName;
    private BufferedWriter logger;

    public RoomRecords(String campusName)  throws RemoteException{
        this.campusName = campusName;
        try {
            logger = new BufferedWriter(new FileWriter(logsFolder + campusName + ".txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        String msg;

        if (dayRooms == null){
            dayRooms = new HashMap<>();
            roomRecordList = null;
        } else {
            roomRecordList = dayRooms.get(room_Number);
            // might be null
        }

        if (roomRecordList == null){
            roomRecordList =  RoomRecord.makeFromTimeSlotList(list_Of_Time_Slots);
            msg = RoomRecord.successPrefix + "All room records added.";
        }else {
            newRoomRecordList =  RoomRecord.makeFromTimeSlotList(list_Of_Time_Slots);
            msg = RoomRecord.addValidRoomRecords(roomRecordList, newRoomRecordList);
        }

        dayRooms.put(room_Number, roomRecordList);
        roomRecords.put(date, dayRooms);
        HashMap<String, String> paramNames = new HashMap<>();
        paramNames.put("room number", Integer.toString(room_Number));
        paramNames.put("Date at which to create room records", date.toString());
        paramNames.put("Time slots", Arrays.toString(list_Of_Time_Slots));
        log("Create Room", paramNames, msg);
        return msg;
        // try waiting to see if middleware starts a thread per request: it does!
//        try {
//            TimeUnit.SECONDS.sleep(5);
//        }catch (Exception e){
//            System.out.println(e.getStackTrace());
//        }
    }
    public String  deleteRoom (int roomNumber, LocalDate date, TimeSlot[] list_Of_Time_Slots) throws java.rmi.RemoteException{
        List<RoomRecord> roomRecordList = null;
        HashMap<Integer, List<RoomRecord>> dayRooms = roomRecords.get(date);
        boolean leaveNow = false;
        String msg = null;

        if (dayRooms == null){
            msg = RoomRecord.failurePrefix + "No room records under date: " + date.toString();
            leaveNow = true;
        } else {
            roomRecordList = dayRooms.get(roomNumber);
            // might be null
        }

        if (!leaveNow){
            if (roomRecordList == null){
                msg = RoomRecord.failurePrefix + "No room records under room number: " + roomNumber;
            }else {
                msg = RoomRecord.deleteRoomRecordsFromTimeSlots(roomRecordList, list_Of_Time_Slots, studentBookingCount);
            }
        }

        HashMap<String, String> paramNames = new HashMap<>();
        paramNames.put("room number", Integer.toString(roomNumber));
        paramNames.put("Date at which to delete room records", date.toString());
        paramNames.put("Time slots", Arrays.toString(list_Of_Time_Slots));
        log("Delete Room", paramNames, msg);
        return msg;
    }
    // STUDENT
    public String bookRoom(int roomNumber, LocalDate date, TimeSlot timeslot, String userID) throws java.rmi.RemoteException{
        List<RoomRecord> roomRecordList = null;
        HashMap<Integer, List<RoomRecord>> dayRooms = roomRecords.get(date);
        String msg = null;
        boolean leaveNow = false;

        if (dayRooms == null){
            msg = RoomRecord.failurePrefix + "No room records under date: " + date.toString();
            leaveNow = true;
        } else {
            roomRecordList = dayRooms.get(roomNumber);
            // might be null
        }

        if (!leaveNow){
            if (roomRecordList == null){
                msg = RoomRecord.failurePrefix + "No room records under room number: " + roomNumber;
            }else {
                msg = RoomRecord.bookFromList(roomRecordList, timeslot, studentBookingCount, userID);
            }
        }

        HashMap<String, String> paramNames = new HashMap<>();
        paramNames.put("room number", Integer.toString(roomNumber));
        paramNames.put("Date at which to book the room", date.toString());
        paramNames.put("Time slot", timeslot.toString());
        paramNames.put("User ID", userID);
        log("Book Room", paramNames, msg);
        return msg;
    }
    public String getAvailableTimeSlot(LocalDate date) throws java.rmi.RemoteException{
        // todo only used to check yourself
        return "time slot got";
    }

    public String getAvailableTimeSlot(String dateText) throws java.rmi.RemoteException{
        //todo send text to other objs' servers, then parse it for ourselves, call local method.
        DatagramSocket aSocket = null;
//        try {
//            aSocket = new DatagramSocket();
//            byte [] m = args[0].getBytes();
//            InetAddress aHost = InetAddress.getByName(args[1]);
//            int serverPort = 6789;
//            DatagramPacket request =
//                    new DatagramPacket(m,  args[0].length(), aHost, serverPort);
//            aSocket.send(request);
//            byte[] buffer = new byte[1000];
//            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
//            aSocket.receive(reply);
//            System.out.println("Reply: " + new String(reply.getData()));
//        }catch (SocketException e){
//            System.out.println("Socket: " + e.getMessage());
//        }catch (IOException e){
//            System.out.println("IO: " + e.getMessage());
//        }finally {if(aSocket != null) aSocket.close();}

        return "time slot got";

    }

    public String cancelBooking(String bookingID)  throws java.rmi.RemoteException{
        //bookingID is the recordID
        String msg = RoomRecord.failurePrefix + "The booked room record could not be found, it was mostly likely deleted.";

        for (Map.Entry<LocalDate, HashMap<Integer, List<RoomRecord>>> outerSet: roomRecords.entrySet()){
            for(Map.Entry<Integer, List<RoomRecord>> innerSet: outerSet.getValue().entrySet()){
                for(RoomRecord roomRecord: innerSet.getValue()){
                    if (roomRecord.recordID.equals(bookingID)){
                        roomRecord.booked_by = null;
                        // todo update student booking count
                        msg = RoomRecord.successPrefix + "Booking cancelled.";
                    }
                }
            }
        }
        HashMap<String, String> paramNames = new HashMap<>();
        paramNames.put("Booking ID", bookingID);
        log("Cancel Booking", paramNames, msg);
        return msg;
    }

    private synchronized void log(String operation, HashMap<String, String> operationParams, String reply){
        LocalDateTime timeOfRequest = LocalDateTime.now();
        try {
            logger.write("==========\n");
            logger.write("Date: " + timeOfRequest + "\n");
            logger.write("Operation: " + operation + "\n");
            for (Map.Entry<String, String> set: operationParams.entrySet()){
                logger.write(set.getKey() + " : " + set.getValue() + "\n");
            }
            logger.write("Reply: " + reply + "\n");
            logger.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
