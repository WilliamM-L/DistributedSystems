package com.DistributedSystems.asg2.remote;

import com.DistributedSystems.asg1.local.RoomRecord;
import com.DistributedSystems.asg1.local.TimeSlot;
import com.DistributedSystems.asg2.RoomRecordsObj.RoomRecordsCorba;
import com.DistributedSystems.asg2.RoomRecordsObj.RoomRecordsCorbaHelper;
import com.DistributedSystems.asg2.RoomRecordsObj.RoomRecordsCorbaPOA;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RoomRecords extends RoomRecordsCorbaPOA {
    private NamingContextExt namingContextExt;
    private static String baseCorbaObjName = "RoomRecords";
    private ORB orb;
    public final static String logsFolder = "logs\\server\\";
    public final String campusName;
    private HashMap<String, Integer> externalSocketPorts;
    private static BufferedWriter logger = null;
    private static final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MM-yyyy").toFormatter(Locale.ENGLISH);
    ConcurrentHashMap<LocalDate,HashMap<Integer, List<RoomRecord>>> roomRecords = new ConcurrentHashMap<>();
    // studentID: list of
    ConcurrentHashMap<String, List<String>> studentBookingCount = new ConcurrentHashMap<>();

    // ADMIN
    public String createRoom(int room_Number, String dateText, String timeSlotListText, String userID) {
        List<RoomRecord> roomRecordList, newRoomRecordList;
        LocalDate date = parseDate(dateText);
        TimeSlot[] timeSlotList = parseTimeSlotList(timeSlotListText);

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
            roomRecordList =  RoomRecord.makeFromTimeSlotList(timeSlotList, campusName);
            dayRooms.put(room_Number, roomRecordList);
            // if there's only one entry, it used to be null
            if (dayRooms.size() == 1){
                roomRecords.put(date, dayRooms);
            }
            msg = RoomRecord.successPrefix + "All room records added.";
        }else {
            newRoomRecordList =  RoomRecord.makeFromTimeSlotList(timeSlotList, campusName);
            synchronized (roomRecordList){
                msg = RoomRecord.addValidRoomRecords(roomRecordList, newRoomRecordList);
            }
        }
        // put only if they did not exist

        HashMap<String, String> paramNames = new HashMap<>();
        paramNames.put("room number", Integer.toString(room_Number));
        paramNames.put("Date at which to create room records", date.toString());
        paramNames.put("Time slots", Arrays.toString(timeSlotList));
        log("Create Room", paramNames, msg, userID);
        return msg;
    }

    public String  deleteRoom (int roomNumber, String dateText, String timeSlotListText, String userID){
        List<RoomRecord> roomRecordList = null;
        LocalDate date = parseDate(dateText);
        TimeSlot[] timeSlotList = parseTimeSlotList(timeSlotListText);
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
                    msg = RoomRecord.deleteRoomRecordsFromTimeSlots(roomRecordList, timeSlotList, studentBookingCount);
                }
            }
        }

        HashMap<String, String> paramNames = new HashMap<>();
        paramNames.put("room number", Integer.toString(roomNumber));
        paramNames.put("Date at which to delete room records", date.toString());
        paramNames.put("Time slots", Arrays.toString(timeSlotList));
        log("Delete Room", paramNames, msg, userID);
        return msg;
    }
    // STUDENT
    public String bookRoom(String campusName, int roomNumber, String dateText, String timeSlotText, String userID) {
        return bookRoom(campusName, roomNumber, dateText, timeSlotText, userID, true);
    }

    private String bookRoom(String campusName, int roomNumber, String dateText, String timeSlotText, String userID, boolean log) {
        String msg = null;
        if (campusName.equals(this.campusName)){
            LocalDate date = parseDate(dateText);
            TimeSlot timeslot = parseTimeSlotList(timeSlotText)[0];
            List<RoomRecord> roomRecordList = null;
            HashMap<Integer, List<RoomRecord>> dayRooms = roomRecords.get(date);
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

            if (log){
                HashMap<String, String> paramNames = new HashMap<>();
                paramNames.put("room number", Integer.toString(roomNumber));
                paramNames.put("Date at which to book the room", date.toString());
                paramNames.put("Time slot", timeslot.toString());
                paramNames.put("User ID", userID);
                log("Book Room", paramNames, msg, userID);
            }

        } else {
            // send request to appropriate corba obj
            try {
                RoomRecordsCorba destination = RoomRecordsCorbaHelper.narrow(namingContextExt.resolve_str(baseCorbaObjName + campusName));
                msg = destination.bookRoom(campusName, roomNumber, dateText, timeSlotText, userID);
            } catch (Exception e) {
                e.printStackTrace();
                msg = e.getMessage();
            }
        }

        return msg;
    }

    public String getAvailableTimeSlot(LocalDate date){
        // only used to check yourself
        int numRoomRecordAvailable = 0;
        HashMap<Integer, List<RoomRecord>> dayRooms = roomRecords.get(date);
        if (dayRooms != null){
            for (Map.Entry<Integer, List<RoomRecord>> set: dayRooms.entrySet()){
                List<RoomRecord> roomRecords = set.getValue();
                synchronized (roomRecords){
                    numRoomRecordAvailable += roomRecords.size();
                }
            }
        }

        return campusName + ":" + numRoomRecordAvailable + " ";
    }

    public String getAvailableTimeSlot(String dateText, String userID) {
        DatagramSocket datagramSocket = null;
        int serverPort;
        String stringToSend = UdpPacketType.GET_AVAILABLE_DATES.getValue() +","+ dateText+",";
        StringBuilder stringBuilder = new StringBuilder();
        // fetching external data
        try {
            for (Map.Entry<String, Integer> set: externalSocketPorts.entrySet()){
                datagramSocket = new DatagramSocket();
                byte [] toSend = stringToSend.getBytes();
                InetAddress host = InetAddress.getByName(null);
                serverPort = set.getValue();
                DatagramPacket request = new DatagramPacket(toSend,  stringToSend.length(), host, serverPort);
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

    public String cancelBooking(String bookingID, String userID)  {
        return cancelBooking(bookingID, userID, true);
    }

    private String cancelBooking(String bookingID, String userID, boolean log)  {
        String campusName = RoomRecord.extractCampusFromRecordID(bookingID);
        String msg = RoomRecord.failurePrefix + "The booked room record could not be found, it was mostly likely deleted.";
        //bookingID is the recordID
        if (campusName.equals(this.campusName)){
            outerloop:
            for (Map.Entry<LocalDate, HashMap<Integer, List<RoomRecord>>> outerSet: roomRecords.entrySet()){
                for(Map.Entry<Integer, List<RoomRecord>> innerSet: outerSet.getValue().entrySet()){
                    List<RoomRecord> roomRecords = innerSet.getValue();
                    synchronized (roomRecords){
                        for(RoomRecord roomRecord: roomRecords){
                            if (roomRecord.recordID.equals(bookingID)){
                                roomRecord.booked_by = null;
                                List<String> studentRecords = studentBookingCount.get(userID);
                                synchronized (studentRecords){
                                    studentRecords.remove(roomRecord.recordID);
                                }
                                msg = RoomRecord.successPrefix + "Booking cancelled.";
                                break outerloop;
                            }
                        }
                    }
                }
            }
            if (log){
                HashMap<String, String> paramNames = new HashMap<>();
                paramNames.put("Booking ID", bookingID);
                log("Cancel Booking", paramNames, msg, userID);
            }
        } else {
            // send request to appropriate corba obj
            try {
                RoomRecordsCorba destination = RoomRecordsCorbaHelper.narrow(namingContextExt.resolve_str(baseCorbaObjName + campusName));
                msg = destination.cancelBooking(bookingID, userID);
            } catch (Exception e) {
                e.printStackTrace();
                msg = e.getMessage();
            }
        }

        return msg;
    }

    public String changeReservation(String bookingID, String newCampusName, int newRoomNum, String newTimeSlotText, String newDateText, String userID){
        String bookingMsg;
        String cancelMsg = RoomRecord.successPrefix + "The record old was deleted before it could be cancelled.";
        String msg = RoomRecord.failurePrefix + "The new room record could not be found, it was mostly likely deleted.";
        String campusName = RoomRecord.extractCampusFromRecordID(bookingID);

        if (campusName.equals(this.campusName)){
            // making sure the old record is owned by this corba obj
            List<String> studentRecords = studentBookingCount.get(userID);
            // removing the booking, will re-add it if necessary

            if (newCampusName.equals(this.campusName)){
                synchronized (studentRecords){
                    studentRecords.remove(bookingID);
                }
                bookingMsg = bookRoom(newCampusName, newRoomNum, newDateText, newTimeSlotText, userID, false);

            } else {
                // contact other objs with udp messages
                bookingMsg = contactCampusToBookUDP(campusName, newRoomNum, newDateText, newTimeSlotText, userID);
            }

            if (bookingMsg.startsWith(RoomRecord.failurePrefix)){
                // the bookingID was only removed in advanced for local new bookings, don't need to do anything if the failed booking was remote
                if (newCampusName.equals(this.campusName)){
                    //re-add if the booking has failed, leave it as is otherwise todo look if the record still exists!
                    outerloop:
                    for (Map.Entry<LocalDate, HashMap<Integer, List<RoomRecord>>> outerSet: roomRecords.entrySet()){
                        for(Map.Entry<Integer, List<RoomRecord>> innerSet: outerSet.getValue().entrySet()){
                            List<RoomRecord> roomRecords = innerSet.getValue();
                            synchronized (roomRecords){
                                for(RoomRecord roomRecord: roomRecords){
                                    if (roomRecord.recordID.equals(bookingID)){
                                        if(roomRecord.booked_by.equals(userID)){
                                            synchronized (studentRecords){
                                                studentRecords.add(bookingID);
                                                cancelMsg = RoomRecord.successPrefix + "Could not book, will not cancel previous booking.";
                                                break outerloop;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            } else {
                // cancel the old booking(will check its existence )
                cancelMsg = cancelBooking(bookingID, userID, false);
            }
            HashMap<String, String> paramNames = new HashMap<>();
            paramNames.put("Booking ID", bookingID);
            log("Change Reservation", paramNames, msg, userID);
        } else {
            // send request to appropriate corba obj
            try {
                RoomRecordsCorba destination = RoomRecordsCorbaHelper.narrow(namingContextExt.resolve_str(baseCorbaObjName + campusName));
                msg = destination.changeReservation(bookingID, newCampusName, newRoomNum, newTimeSlotText, newDateText, userID);
            } catch (Exception e) {
                e.printStackTrace();
                msg = e.getMessage();
            }
        }

        return msg;
    }

    private String contactCampusToBookUDP(String campusName, int roomNumber, String dateText, String timeSlotText, String userID){
        DatagramSocket datagramSocket = null;
        int serverPort;
        StringBuilder stringBuilder = new StringBuilder();
        String stringToSend = UdpPacketType.CHANGE_RESERVATION.getValue() + "," + campusName + "," + roomNumber +"," + dateText +"," + timeSlotText +"," + userID+",";
        try {
            for (Map.Entry<String, Integer> set: externalSocketPorts.entrySet()){
                if (set.getKey().equals(campusName)){
                    datagramSocket = new DatagramSocket();
                    byte [] toSend = stringToSend.getBytes();
                    InetAddress host = InetAddress.getByName(null);
                    serverPort = set.getValue();
                    DatagramPacket request = new DatagramPacket(toSend,  stringToSend.length(), host, serverPort);
                    datagramSocket.send(request);
                    byte[] buffer = new byte[1000];
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                    datagramSocket.receive(reply);
                    stringBuilder.append(new String(buffer, 0, reply.getLength()));
//                System.out.println("Reply: " + new String(reply.getData()));
                    break;
                }

            }
        }catch (SocketException e){
            System.out.println("Socket: " + e.getMessage());
        }catch (IOException e){
            System.out.println("IO: " + e.getMessage());
        }finally {if(datagramSocket != null) datagramSocket.close();}
        return stringBuilder.toString();
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

    public RoomRecords(String campusName, HashMap<String, Integer> externalSocketPorts, ORB orb) {
        this.orb = orb;
        // get the root naming context
        org.omg.CORBA.Object objRef = orb.string_to_object("corbaloc::localhost:8050/NameService");
        // Use NamingContextExt instead of NamingContext. This is part of the Interoperable naming Service.
        this.namingContextExt = NamingContextExtHelper.narrow(objRef);
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
    private LocalDate parseDate(String dateText){
        return LocalDate.parse(dateText, dateTimeFormatter);
    }

    private TimeSlot[] parseTimeSlotList(String text){
        return TimeSlot.parseTimeSlots(text.split("/"));
    }

    public void setORB(ORB orb) {
        this.orb = orb;
    }
    @Override
    public void shutdown() {
        orb.shutdown(true);
    }
}
