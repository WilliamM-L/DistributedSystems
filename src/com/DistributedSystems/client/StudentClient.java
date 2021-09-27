package com.DistributedSystems.client;
import com.DistributedSystems.local.TimeSlot;
import com.DistributedSystems.remote.IRoomRecords;
import com.DistributedSystems.remote.RoomRecords;

import java.io.*;
import java.rmi.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StudentClient {
    private static final String instructionDir = "instructions";

    private static void executeInstructions(String instructionFileName, IRoomRecords[] roomRecordsList) throws RemoteException {
        DateFormat dateFormat = new SimpleDateFormat("dd MM");
        // --> could do this dateFormat.parse("10 11") for a simpler date format
        String path = instructionDir + "\\" + instructionFileName;
        // todo read from file
        System.out.println(roomRecordsList[0].createRoom(1, new Date(), new TimeSlot[]{new TimeSlot()}));
        // todo, decide to call which object from the prefix

    }

    public static void main(String[] args) {
        try {

            int startingPortNum = 1313;
            String registryURL;
//            String[] registryURLs = new String[3];
            IRoomRecords[] roomRecordsList = new IRoomRecords[3];

            for (int i = 0; i <= 2; i++) {
                registryURL = "rmi://localhost:" + (i+startingPortNum) + "/RoomRecords";
                roomRecordsList[i] = (IRoomRecords)Naming.lookup(registryURL);
            }
            // find the remote object and cast it to an interface object

            System.out.println("Lookup completed");
            executeInstructions("Admin1.txt", roomRecordsList);
        }
        catch (Exception e) {
            System.out.println("Exception in StudentClient: " + e);
        }
    }
}
