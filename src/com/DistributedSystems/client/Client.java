package com.DistributedSystems.client;
import com.DistributedSystems.lambda.Lambdas;
import com.DistributedSystems.local.TimeSlot;
import com.DistributedSystems.remote.IRoomRecords;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Client {
    private static final String instructionDir = "instructions";
    private static final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MM-yyyy").toFormatter(Locale.ENGLISH);
    private static final String logsFolder = "logs\\client\\";

    private static void executeInstructionFile(String instructionFileName, String[] remoteObjectNames) throws IOException, NotBoundException {
        String path = instructionDir + "\\" + instructionFileName;
        File file = new File(path);
        FileReader fileReader = new FileReader(file);
        String line = "none";
        String[] args = null;
        IRoomRecords roomRecords = null;
        Lambdas.ChooseCampus evaluator = null;
        boolean campusServerFound = false;
        Stack<String> bookingIDs = new Stack<>();
        try (fileReader) {
            BufferedReader br = new BufferedReader(fileReader);

            line = br.readLine();
            String username = line.substring(6);
            String campus = username.substring(0, 3);
            boolean admin = username.charAt(3) == 'A';
            BufferedWriter logger = new BufferedWriter(new FileWriter(logsFolder + username + ".txt"));

            while((line=br.readLine())!=null){
                args = line.split(" ");

                if (args[0].equals("bookRoom")){
                    evaluator = (remoteObjectName, args_) -> remoteObjectName.endsWith(args_[1]);
                } else {
                    evaluator = (remoteObjectName, args_) -> remoteObjectName.endsWith(campus);
                }

                campusServerFound = false;
                for (String remoteObjectName : remoteObjectNames){
                    if (evaluator.chooseCampus(remoteObjectName, args)){
                        roomRecords = (IRoomRecords)Naming.lookup(remoteObjectName);
                        campusServerFound = true;
                    }
                }

                if (campusServerFound){
                    log(logger, executeInstruction(args, roomRecords, admin, username, bookingIDs));
                } else {
                    System.out.println("Sorry that campus is not available at the moment");
                }

            }

        } catch (IOException | NotBoundException e) {
            System.out.println("Exception: at line" + line);
            throw e;
        }

    }

    private static void log(BufferedWriter logger, HashMap<String, String> toLog) throws IOException {
        LocalDateTime timeOfRequest = LocalDateTime.now();
        try {
            logger.write("==========\n");
            for (Map.Entry<String, String> set: toLog.entrySet()){
                logger.write(set.getKey() + " : " + set.getValue() + "\n");
            }
            logger.write("Time at Request: " + timeOfRequest + "\n");
            logger.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static HashMap<String, String> executeInstruction(String[] args, IRoomRecords roomRecords, boolean isAdmin, String userID, Stack<String> bookingIDs) throws IOException {
        int roomNum;
        String[] timeSlotText;
        TimeSlot[] timeSlots;
        String campusNameArg, reply;
        String instructionName = args[0];
        LocalDate date;
        boolean unauthorised = false;
        HashMap<String, String> toLog = new HashMap<>();
        toLog.put("userID", userID);
        toLog.put("Command", instructionName);
        // String.equals is used implicitly in the switch statement
        switch (instructionName){
            case "createRoom":
                if (!isAdmin){
                    unauthorised = true;
                } else {
                    roomNum = Integer.parseInt(args[1]);
                    date = LocalDate.parse(args[2], dateTimeFormatter);
                    timeSlotText = args[3].split("/");
                    timeSlots = TimeSlot.parseTimeSlots(timeSlotText);
                    toLog.put("reply", roomRecords.createRoom(roomNum, date, timeSlots));
                    toLog.put("room number", args[1]);
                    toLog.put("date", args[2]);
                    toLog.put("time slots", args[3]);
                }
                break;
            case "deleteRoom":
                if (!isAdmin){
                    unauthorised = true;
                } else {
                    roomNum = Integer.parseInt(args[1]);
                    date = LocalDate.parse(args[2], dateTimeFormatter);
                    timeSlotText = args[3].split("/");
                    timeSlots = TimeSlot.parseTimeSlots(timeSlotText);
                    toLog.put("reply", roomRecords.deleteRoom(roomNum, date, timeSlots));
                    toLog.put("room number", args[1]);
                    toLog.put("date", args[2]);
                    toLog.put("time slots", args[3]);
                }
                break;
            case "bookRoom":
                roomNum = Integer.parseInt(args[2]);
                date = LocalDate.parse(args[3], dateTimeFormatter);
                timeSlotText = args[4].split("/");
                timeSlots = TimeSlot.parseTimeSlots(timeSlotText);
                reply = roomRecords.bookRoom(roomNum, date, timeSlots[0], userID);
                toLog.put("reply", reply);
                toLog.put("room number", args[2]);
                toLog.put("date", args[3]);
                toLog.put("time slots", args[4]);
                if (reply.startsWith("Success")){
                    // second part of the message is the booking id
                    bookingIDs.push(reply.split(" ")[1]);
                }
                break;
            case "getAvailableTimeSlot":
                String dateText = args[1];
                toLog.put("reply", roomRecords.getAvailableTimeSlot(dateText));
                toLog.put("date", dateText);
                break;
            case "cancelBooking":
                toLog.put("bookingID", bookingIDs.peek());
                toLog.put("reply", roomRecords.cancelBooking(bookingIDs.pop(), userID));
                break;
            default:
                toLog.put("Client ignored command", "Could not understand command");
                break;
        }

        if (unauthorised){
            toLog.put("Client ignored command", "Admin status required");
        }
        return toLog;
    }

    public static void main(String[] args) throws NotBoundException, IOException, InterruptedException {
        try {
            // TODO: 2021-10-03 add userId to all server/client logs

            //todo generate logs for both clients and servers
            //todo sync methods so multiple can edit at once
            int portNum = 1313;
            String registryURL = "rmi://localhost:" + portNum;

            // Waiting for the server to come online when they are started at the same time
            TimeUnit.SECONDS.sleep(4);

            String[] remoteObjectNames = Naming.list(registryURL);
            System.out.println("Lookup completed");

            String[] fileNames = new String[]{
                    "AdminDVL1.txt",
//                    "AdminKKL1.txt"
            };

            for (int i = 0; i < fileNames.length; i++) {
                int finalI = i;
                new Thread(() -> {
                    try {
                        executeInstructionFile(fileNames[finalI], remoteObjectNames);
                    } catch (IOException | NotBoundException e) {
                        e.printStackTrace();
                    }
                }).start();
            }


        }
        catch (Exception e) {
            System.out.println("Exception in StudentClient: " + e);
            System.out.println(Arrays.toString(e.getStackTrace()));
            throw e;
        }
    }
}
