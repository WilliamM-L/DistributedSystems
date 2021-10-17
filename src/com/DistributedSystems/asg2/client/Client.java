package com.DistributedSystems.asg2.client;

import com.DistributedSystems.asg1.lambda.Lambdas;
import com.DistributedSystems.asg1.local.TimeSlot;
import com.DistributedSystems.asg2.RoomRecordsObj.RoomRecordsCorba;
import com.DistributedSystems.asg2.RoomRecordsObj.RoomRecordsCorbaHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import java.io.*;
import java.rmi.NotBoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Client {
    private static final String instructionDir = "instructions";
//    private static final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MM-yyyy").toFormatter(Locale.ENGLISH);
    private static final String logsFolder = "logs\\client\\";

    public static void main(String[] args) throws InterruptedException, InvalidName, CannotProceed, NotFound {

        try {
            //todo make changeReservation, cancelBooking, bookroom call other corbas

            // Waiting for the server to come online when they are started at the same time
            TimeUnit.SECONDS.sleep(4);

            // create and initialize the ORB
            ORB orb = ORB.init(args, null);
            // get the root naming context
            org.omg.CORBA.Object objRef = orb.string_to_object("corbaloc::localhost:8050/NameService");
            // Use NamingContextExt instead of NamingContext. This is part of the Interoperable naming Service.
            NamingContextExt namingContextExt = NamingContextExtHelper.narrow(objRef);

            // resolve the Object Reference in Naming
//            String name = "RoomRecords";
//            RoomRecordsCorba roomRecords = RoomRecordsCorbaHelper.narrow(namingContextExt.resolve_str(name));
//            System.out.println("Obtained a handle on server object: " + roomRecords);
//            System.out.println(roomRecords.changeReservation("1","2",2,"i","i"));

            String[] fileNames = new String[]{
                    "AdminDVL1.txt",
//                    "StudentDVL1.txt",
//                    "AdminKKL1.txt"
                    // test case 1 - trying to book the same room in 3 threads. Testing the booking capacity as well, also test delete and see if it properly updates the student booking list + admin privileges
//                    "TestCase1A.txt",
//                    "TestCase1S.txt",
//                    "TestCase1S2.txt",
                    //// test case 2 - doing a bunch of stuff at once, kept same conflict, activity across servers, + simple get available dates test
//                    "TestCase2A1.txt",
//                    "TestCase2A2.txt",
//                    "TestCase2A3.txt",
//                    "TestCase2S1.txt",
//                    "TestCase2S2.txt",
//                    "TestCase2S3.txt",
                    //// test case 3 - Will try to book and delete a room that doesn’t exist anymore.
//                    "TestCase3A1.txt",
                    //// test case 4 - Will try to cancel a booking that doesn’t exist.
//                    "TestCase4A1.txt",
                    //// test case 5 - Trying to create room records with conflicting timeslots.
//                    "TestCase5A1.txt",
//                    //// test case 6 - Trying to create/delete/book a room at once. Testing if the proper objects are synched across methods.
//                    "TestCase6A1.txt",
//                    "TestCase6A2.txt",
//                    "TestCase6S1.txt",
            };

            for (int i = 0; i < fileNames.length; i++) {
                int finalI = i;
//                TimeUnit.SECONDS.sleep(1);
                new Thread(() -> {
                    try {
                        executeInstructionFile(fileNames[finalI], namingContextExt);
                    } catch (IOException | NotBoundException | InvalidName | CannotProceed | NotFound e) {
                        e.printStackTrace();
                    }
                }).start();
            }

            //shutting down the corba obj, might not want to do that since multiple clients will contact the same obj
//            roomRecords.shutdown();
        } catch (Exception e) {
            System.out.println("ERROR in client: " + e);
            e.printStackTrace(System.out);
            throw e;
        }

    }

    private static void executeInstructionFile(String instructionFileName, NamingContextExt namingContextExt) throws IOException, NotBoundException, InvalidName, CannotProceed, NotFound {
        String baseCorbaObjName = "RoomRecords";
        String path = instructionDir + "\\" + "corba\\" + instructionFileName;
        File file = new File(path);
        FileReader fileReader = new FileReader(file);
        String line = "none";
        String[] args = null;
        Lambdas.ChooseCampus evaluator = null;
        boolean campusServerFound;
        Stack<String> bookingIDs = new Stack<>();
        try {
            BufferedReader br = new BufferedReader(fileReader);

            line = br.readLine();
            String username = line.substring(6);
            String campus = username.substring(0, 3);
            boolean admin = username.charAt(3) == 'A';
            BufferedWriter logger = new BufferedWriter(new FileWriter(logsFolder + username + ".txt"));
            // get corba obj based on username
            RoomRecordsCorba roomRecords = RoomRecordsCorbaHelper.narrow(namingContextExt.resolve_str(baseCorbaObjName + campus));
            while((line=br.readLine())!=null){
                args = line.split(" ");
                if (args.length == 0) return;
//
//                if (args[0].equals("bookRoom")){
//                    evaluator = (remoteObjectName, args_) -> remoteObjectName.endsWith(args_[1]);
//                } else if (args[0].equals("cancelBooking")){
//                    if (bookingIDs.empty()){
//                        HashMap<String, String> toLog = new HashMap<String, String>() {{put("Client ignored command", "There is no booking to cancel!");}};
//                        log(logger, toLog);
//                        continue;
//                    } else {
//                        evaluator = (remoteObjectName, args_) -> remoteObjectName.endsWith(bookingIDs.peek());
//                    }
//                } else {
//                    evaluator = (remoteObjectName, args_) -> remoteObjectName.endsWith(campus);
//                }
//
//                for (String remoteObjectName : remoteObjectNames){
//                    if (evaluator.verifyCampus(remoteObjectName, args)){
//                        roomRecords = (IRoomRecords)Naming.lookup(remoteObjectName);
//                    }
//                }

                if (roomRecords != null){
                    log(logger, executeInstruction(args, roomRecords, admin, username, bookingIDs));
                } else {
                    System.out.println("Sorry that campus is not available at the moment");
                }

            }

        } catch (Exception e) {
            System.out.println("Exception: at line\n" + line);
            throw e;
        } finally {
            fileReader.close();
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

    private static HashMap<String, String> executeInstruction(String[] args, RoomRecordsCorba roomRecords, boolean isAdmin, String userID, Stack<String> bookingIDs) throws IOException {
        int roomNum;
        String timeSlotText;
        TimeSlot[] timeSlots;
        String campusNameArg, reply;
        String instructionName = args[0];
        String date;
        String bookingID;
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
                    date = args[2];
                    timeSlotText = args[3];
                    toLog.put("reply", roomRecords.createRoom(roomNum, date, timeSlotText, userID));
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
                    date = args[2];
                    timeSlotText = args[3];
                    toLog.put("reply", roomRecords.deleteRoom(roomNum, date, timeSlotText, userID));
                    toLog.put("room number", args[1]);
                    toLog.put("date", args[2]);
                    toLog.put("time slots", args[3]);
                }
                break;
            case "bookRoom":
                campusNameArg = args[1];
                roomNum = Integer.parseInt(args[2]);
                date = args[3];
                timeSlotText = args[4];
                reply = roomRecords.bookRoom(campusNameArg, roomNum, date, timeSlotText, userID);
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
                toLog.put("reply", roomRecords.getAvailableTimeSlot(dateText, userID));
                toLog.put("date", dateText);
                break;
            case "cancelBooking":
                if (bookingIDs.empty()){
                    toLog.put("Client ignored command", "There is no booking to cancel!");
                } else {
                    toLog.put("bookingID", bookingIDs.peek());
                    toLog.put("reply", roomRecords.cancelBooking(bookingIDs.pop(), userID));
                }
                break;
            case "changeReservation":
                campusNameArg = args[1];
                roomNum = Integer.parseInt(args[2]);
                timeSlotText = args[3];
                if (bookingIDs.empty()){
                    toLog.put("Client ignored command", "There is no booking to cancel!");
                } else {
                    toLog.put("bookingID", bookingIDs.peek());
                    toLog.put("reply", roomRecords.changeReservation(bookingIDs.pop(), campusNameArg, roomNum,timeSlotText, userID));
                }
                break;
            case "wait":
                int toWait = Integer.parseInt(args[1]);
                try {
                    TimeUnit.SECONDS.sleep(toWait);
                } catch (InterruptedException e) {
                    System.out.println("Could not wait!");
                    e.printStackTrace();
                }

            default:
                toLog.put("Client ignored command", "Could not understand command");
                break;
        }

        if (unauthorised){
            toLog.put("Client ignored command", "Admin status required");
        }
        return toLog;
    }

}
