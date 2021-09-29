package com.DistributedSystems.client;
import com.DistributedSystems.lambda.Lambdas;
import com.DistributedSystems.local.TimeSlot;
import com.DistributedSystems.remote.IRoomRecords;

import java.io.*;
import java.rmi.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class StudentClient {
    private static final String instructionDir = "instructions";

    private static void executeInstructionGroup(String instructionFileName, String[] remoteObjectNames) throws IOException, NotBoundException {
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
                    excecuteInstruction(args, roomRecords, admin);
                } else {
                    System.out.println("Sorry that campus is not available at the moment");
                }

            }

            DateFormat dateFormat = new SimpleDateFormat("dd MM");
            // --> could do this dateFormat.parse("10 11") for a simpler date format
            System.out.println(roomRecords.createRoom(1, new Date(), new TimeSlot[]{new TimeSlot()}));
        } catch (IOException | NotBoundException e) {
            System.out.println("Exception: at line" + line);
            throw e;
        }

    }

    private static void excecuteInstruction(String[] args, IRoomRecords roomRecords, boolean isAdmin) {

    }

    public static void main(String[] args) {
        try {
            int portNum = 1313;
            String registryURL = "rmi://localhost:" + portNum;
            String[] remoteObjectNames = Naming.list(registryURL);

            System.out.println("Lookup completed");

            executeInstructionGroup("Admin1.txt", remoteObjectNames);

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
