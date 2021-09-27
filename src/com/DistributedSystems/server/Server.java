package com.DistributedSystems.server;

import com.DistributedSystems.remote.RoomRecords;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

public class Server {
    public static void main(String[] args) {

        String registryURL;
        try{
            int RMIPortNum = 1313;

            startRegistry(RMIPortNum);
            RoomRecords exportedRoomRecords = new RoomRecords();
            registryURL = "rmi://localhost:" + RMIPortNum + "/RoomRecords";
            Naming.rebind(registryURL, exportedRoomRecords);
            System.out.println("Server registered.  Registry currently contains:");
            // list names currently in the registry
            listRegistry(registryURL);
            System.out.println("RoomRecords Server ready.");
        }
        catch (Exception e) {
            System.out.println("Exception in Server: " + e);
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }

    // This method starts an RMI registry on the local host, if it
    // does not already exist at the specified port number.
    private static void startRegistry(int RMIPortNum) throws RemoteException{
        try {
            Registry registry = LocateRegistry.getRegistry(RMIPortNum);
            registry.list();  // This call will throw an exception
            // if the registry does not already exist
        }
        catch (RemoteException e) {
            // No valid registry at that port.
            System.out.println("RMI registry cannot be located at port "+ RMIPortNum);
            Registry registry = LocateRegistry.createRegistry(RMIPortNum);
            System.out.println("RMI registry created at port " + RMIPortNum);
        }
    }

    // This method lists the names registered with a Registry object
    private static void listRegistry(String registryURL) throws RemoteException, MalformedURLException {
        System.out.println("Registry " + registryURL + " contains: ");
        String [ ] names = Naming.list(registryURL);
        for (String name : names) System.out.println(name);
    }
}
