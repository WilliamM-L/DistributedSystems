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
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;

public class RoomRecords extends UnicastRemoteObject implements IRoomRecords{
    public final static String logsFolder = "logs\\server\\";
    public final String campusName;
    private HashMap<String, Integer> externalSocketPorts;
    private static BufferedWriter logger = null;
    private static final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MM-yyyy").toFormatter(Locale.ENGLISH);

    // ADMIN
    public String createRoom(int room_Number, LocalDate date, TimeSlot[] list_Of_Time_Slots, String userID) throws java.rmi.RemoteException{
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
            roomRecordList =  RoomRecord.makeFromTimeSlotList(list_Of_Time_Slots, campusName);
            dayRooms.put(room_Number, roomRecordList);
            // if there's only one entry, it used to be null
            if (dayRooms.size() == 1){
                roomRecords.put(date, dayRooms);
            }
            msg = RoomRecord.successPrefix + "All room records added.";
        }else {
            newRoomRecordList =  RoomRecord.makeFromTimeSlotList(list_Of_Time_Slots, campusName);
            synchronized (roomRecordList){
                msg = RoomRecord.addValidRoomRecords(roomRecordList, newRoomRecordList);
            }
        }
        // put only if they did not exist

        HashMap<String, String> paramNames = new HashMap<>();
        paramNames.put("room number", Integer.toString(room_Number));
        paramNames.put("Date at which to create room records", date.toString());
        paramNames.put("Time slots", Arrays.toString(list_Of_Time_Slots));
        log("Create Room", paramNames, msg, userID);
        return msg;
    }

    public String  deleteRoom (int roomNumber, LocalDate date, TimeSlot[] list_Of_Time_Slots, String userID) throws java.rmi.RemoteException{
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
                synchronized (roomRecordList){
                    // the individual records are deleted, but the lists and maps remain
                    msg = RoomRecord.deleteRoomRecordsFromTimeSlots(roomRecordList, list_Of_Time_Slots, studentBookingCount);
                }
            }
        }

        HashMap<String, String> paramNames = new HashMap<>();
        paramNames.put("room number", Integer.toString(roomNumber));
        paramNames.put("Date at which to delete room records", date.toString());
        paramNames.put("Time slots", Arrays.toString(list_Of_Time_Slots));
        log("Delete Room", paramNames, msg, userID);
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
                synchronized (roomRecordList){
                    msg = RoomRecord.bookFromList(roomRecordList, timeslot, studentBookingCount, userID);
                }
            }
        }

        HashMap<String, String> paramNames = new HashMap<>();
        paramNames.put("room number", Integer.toString(roomNumber));
        paramNames.put("Date at which to book the room", date.toString());
        paramNames.put("Time slot", timeslot.toString());
        paramNames.put("User ID", userID);
        log("Book Room", paramNames, msg, userID);
        return msg;
    }

    public String getAvailableTimeSlot(LocalDate date) throws java.rmi.RemoteException{
        // only used to check yourself
        int numRoomRecordAvailable = 0;
        HashMap<Integer, List<RoomRecord>> dayRooms = roomRecords.get(date);
        for (Map.Entry<Integer, List<RoomRecord>> set: dayRooms.entrySet()){
            List<RoomRecord> roomRecords = set.getValue();
            synchronized (roomRecords){
                numRoomRecordAvailable += roomRecords.size();
            }
        }
        return campusName + ":" + numRoomRecordAvailable + " ";
    }

    public String getAvailableTimeSlot(String dateText, String userID) throws java.rmi.RemoteException{
        //todo send text to other objs' servers, then parse it for ourselves, call local method.
        DatagramSocket datagramSocket = null;
        int serverPort;

        StringBuilder stringBuilder = new StringBuilder();
        // fetching external data
        try {
            for (Map.Entry<String, Integer> set: externalSocketPorts.entrySet()){
                datagramSocket = new DatagramSocket();
                byte [] toSend = dateText.getBytes();
                InetAddress host = InetAddress.getByName(null);
                serverPort = set.getValue();
                DatagramPacket request = new DatagramPacket(toSend,  dateText.length(), host, serverPort);
                datagramSocket.send(request);
                byte[] buffer = new byte[1000];
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(reply);
                stringBuilder.append(new String(buffer, 0, reply.getLength()));
//                System.out.println("Reply: " + new String(reply.getData()));
            }
        }catch (SocketException e){
            System.out.println("Socket: " + e.getMessage());
        }catch (IOException e){
            System.out.println("IO: " + e.getMessage());
        }finally {if(datagramSocket != null) datagramSocket.close();}

        //querying yourself
        LocalDate date = LocalDate.parse(dateText, dateTimeFormatter);
        stringBuilder.append(getAvailableTimeSlot(date));
        String msg = stringBuilder.toString();

        HashMap<String, String> paramNames = new HashMap<>();
        paramNames.put("DateText", dateText);
        log("getAvailableTimeSlot", paramNames, msg, userID);
        return msg;

    }

    public String cancelBooking(String bookingID, String userID)  throws java.rmi.RemoteException{
        //bookingID is the recordID
        String msg = RoomRecord.failurePrefix + "The booked room record could not be found, it was mostly likely deleted.";

        for (Map.Entry<LocalDate, HashMap<Integer, List<RoomRecord>>> outerSet: roomRecords.entrySet()){
            for(Map.Entry<Integer, List<RoomRecord>> innerSet: outerSet.getValue().entrySet()){
                List<RoomRecord> roomRecords = innerSet.getValue();
                synchronized (roomRecords){
                    for(RoomRecord roomRecord: roomRecords){
                        if (roomRecord.recordID.equals(bookingID)){
                            roomRecord.booked_by = null;
                            List<String> studentRecords = studentBookingCount.get(userID);
                            studentRecords.remove(roomRecord.recordID);
                            msg = RoomRecord.successPrefix + "Booking cancelled.";
                            break;
                        }
                    }
                }
            }
        }

        HashMap<String, String> paramNames = new HashMap<>();
        paramNames.put("Booking ID", bookingID);
        log("Cancel Booking", paramNames, msg, userID);
        return msg;
    }

    private static void log(String operation, HashMap<String, String> operationParams, String reply, String userID){
        LocalDateTime timeOfRequest = LocalDateTime.now();
        try {
            synchronized (logger){
                logger.write("==========\n");
                logger.write("User: " + userID + "\n");
                logger.write("Operation: " + operation + "\n");
                for (Map.Entry<String, String> set: operationParams.entrySet()){
                    logger.write(set.getKey() + " : " + set.getValue() + "\n");
                }
                logger.write("Time at Request: " + timeOfRequest + "\n");
                logger.write("Reply: " + reply + "\n");
                logger.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public RoomRecords(String campusName, HashMap<String, Integer> externalSocketPorts)  throws RemoteException{
        this.campusName = campusName;
        this.externalSocketPorts = externalSocketPorts;
        try {
            logger = new BufferedWriter(new FileWriter(logsFolder + campusName + ".txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MM-yyyy").toFormatter(Locale.ENGLISH);
        // conversion ex: LocalDateTime date = LocalDateTime.from(LocalDate.parse("01-10-2021", formatter).atStartOfDay());

        LocalDate date = LocalDate.parse("31-10-2021", formatter);
        HashMap<Integer, List<RoomRecord>> entry = new HashMap<>();
        List<RoomRecord> roomRecordList = new ArrayList<>();
        roomRecordList.add(new RoomRecord(new TimeSlot(LocalTime.of(6,0)), campusName));
        roomRecordList.add(new RoomRecord(new TimeSlot(LocalTime.of(8,0)), campusName));
        entry.put(1, roomRecordList);
        roomRecords.put(date, entry);

        date = date.plusDays(1);
        entry = new HashMap<>();
        roomRecordList = new ArrayList<>();
        roomRecordList.add(new RoomRecord(new TimeSlot(LocalTime.of(6,0)), campusName));
        entry.put(1, roomRecordList);
        roomRecords.put(date, entry);

        date = date.plusDays(1);
        entry = new HashMap<>();
        roomRecordList = new ArrayList<>();
        roomRecordList.add(new RoomRecord(new TimeSlot(LocalTime.of(6,0)), campusName));
        entry.put(1, roomRecordList);
        roomRecords.put(date, entry);

    }
}
