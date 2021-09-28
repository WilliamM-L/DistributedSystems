package com.DistributedSystems.client;
import com.DistributedSystems.local.TimeSlot;
import com.DistributedSystems.remote.IRoomRecords;

import java.io.*;
import java.rmi.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class StudentClient {
    private static final String instructionDir = "instructions";

    private static void executeInstructions(String instructionFileName, String[] remoteObjectNames) throws IOException, NotBoundException {
        String path = instructionDir + "\\" + instructionFileName;
        File file = new File(path);
        FileReader fileReader = new FileReader(file);
        String line = "none";
        IRoomRecords roomRecords = null;
        try (fileReader) {
            BufferedReader br = new BufferedReader(fileReader);

            line = br.readLine();
            String username = line.substring(6);
            String campus = username.substring(0, 3);
            boolean admin = username.charAt(3) == 'A';

            for (String remoteObjectName : remoteObjectNames){
                if (remoteObjectName.endsWith(campus)){
                    roomRecords = (IRoomRecords)Naming.lookup(remoteObjectName);
                }
            }

            while((line=br.readLine())!=null){

            }

            DateFormat dateFormat = new SimpleDateFormat("dd MM");
            // --> could do this dateFormat.parse("10 11") for a simpler date format
            System.out.println(roomRecords.createRoom(1, new Date(), new TimeSlot[]{new TimeSlot()}));
        } catch (IOException | NotBoundException e) {
            System.out.println("Exception: at line" + line);
            throw e;
        }

    }

    public static void main(String[] args) {
        try {
            int portNum = 1313;
            String registryURL = "rmi://localhost:" + portNum;
            String[] remoteObjectNames = Naming.list(registryURL);

            System.out.println("Lookup completed");

            executeInstructions("Admin1.txt", remoteObjectNames);

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
        }
    }
}
