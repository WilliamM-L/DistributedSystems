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

    private static void executeInstructions(String instructionFileName, IRoomRecords roomRecords) throws RemoteException {
        DateFormat dateFormat = new SimpleDateFormat("dd MM");
        // --> could do this dateFormat.parse("10 11") for a simpler date format
        String path = instructionDir + "\\" + instructionFileName;
        // todo read from file, check if you get a reply
        System.out.println(roomRecords.createRoom(1, new Date(), new TimeSlot[]{new TimeSlot()}));

    }

    public static void main(String[] args) {
        try {

            int portNum = 1313;
            String registryURL = "rmi://localhost:" + portNum + "/RoomRecords";
            // find the remote object and cast it to an interface object
            IRoomRecords roomRecords = (IRoomRecords)Naming.lookup(registryURL);
            System.out.println("Lookup completed");
            executeInstructions("AdminClient1.txt", roomRecords);
        }
        catch (Exception e) {
            System.out.println("Exception in StudentClient: " + e);
        }
    }
}
