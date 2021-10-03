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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Client {
    private static final String instructionDir = "instructions";
    private static final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MM-yyyy").toFormatter(Locale.ENGLISH);
    private static Stack<String> bookingIDs = new Stack<>();

    private static void executeInstructionFile(String instructionFileName, String[] remoteObjectNames) throws IOException, NotBoundException {
        String path = instructionDir + "\\" + instructionFileName;
        File file = new File(path);
        FileReader fileReader = new FileReader(file);
        String line = "none";
        String[] args = null;
        IRoomRecords roomRecords = null;
        Lambdas.ChooseCampus evaluator = null;
        boolean campusServerFound = false;
        try (fileReader) {
            BufferedReader br = new BufferedReader(fileReader);

            line = br.readLine();
            String username = line.substring(6);
            String campus = username.substring(0, 3);
            boolean admin = username.charAt(3) == 'A';

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
                    executeInstruction(args, roomRecords, admin, username);
                } else {
                    System.out.println("Sorry that campus is not available at the moment");
                }

            }

        } catch (IOException | NotBoundException e) {
            System.out.println("Exception: at line" + line);
            throw e;
        }

    }

    private static void executeInstruction(String[] args, IRoomRecords roomRecords, boolean isAdmin, String userID) throws IOException {
        int roomNum;
        String[] timeSlotText;
        TimeSlot[] timeSlots;
        String campusNameArg, reply;
        String instructionName = args[0];
        LocalDate date;
        boolean unauthorised = false;
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
                    roomRecords.createRoom(roomNum, date, timeSlots);
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
                    roomRecords.deleteRoom(roomNum, date, timeSlots);
                }
                break;
            case "bookRoom":
                campusNameArg = args[1];
                roomNum = Integer.parseInt(args[2]);
                date = LocalDate.parse(args[3], dateTimeFormatter);
                timeSlotText = args[4].split("/");
                timeSlots = TimeSlot.parseTimeSlots(timeSlotText);
                reply = roomRecords.bookRoom(roomNum, date, timeSlots[0], userID);
                if (reply.startsWith("Success")){
                    bookingIDs.push(reply.split(" ")[0]);
                }
                break;
            case "getAvailableTimeSlot":
                String dateText = args[1];
                roomRecords.getAvailableTimeSlot(dateText);

                break;
            case "cancelBooking":
                roomRecords.cancelBooking(bookingIDs.pop());
                break;
            default:
                System.out.println("Could not understand command");
                break;
        }

        if (unauthorised){
            System.out.println("Cannot execute this command.");
        }

    }

    public static void main(String[] args) throws NotBoundException, IOException, InterruptedException {
        try {
            //todo implement rmi methods
            //todo test most of them!!!

            //todo generate logs for both clients and servers
            //todo sync methods so multiple can edit at once
            //todo should I start a process per client? It would better emulate having people on different hosts.
            int portNum = 1313;
            String registryURL = "rmi://localhost:" + portNum;

            // Waiting for the server to come online when they are started at the same time
            TimeUnit.SECONDS.sleep(2);

            String[] remoteObjectNames = Naming.list(registryURL);
            System.out.println("Lookup completed");


            executeInstructionFile("Admin1.txt", remoteObjectNames);

//            new Thread(() -> {
//                try {
//                    executeInstructions("Admin1.txt", remoteObjectNames);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (NotBoundException e) {
//                    e.printStackTrace();
//                }
//            }).start();
//            new Thread(() -> {
//                try {
//                    executeInstructions("Admin1.txt", remoteObjectNames);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (NotBoundException e) {
//                    e.printStackTrace();
//                }
//            }).start();


        }
        catch (Exception e) {
            System.out.println("Exception in StudentClient: " + e);
            System.out.println(Arrays.toString(e.getStackTrace()));
            throw e;
        }
    }
}
