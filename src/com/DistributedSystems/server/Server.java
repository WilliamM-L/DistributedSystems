package com.DistributedSystems.server;

import com.DistributedSystems.remote.RoomRecords;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

public class Server {
    public static void main(String[] args) {
        // todo start a server process for each campus! pass a string from args[] to decide how to call the obj

        String registryURL;
        String objectURL;
        try{
//            String[] campusNames = {"DVL", "KKL", "WST"};
            int RMIPortNum = 1313;
            startRegistry(RMIPortNum);
            registryURL = "rmi://localhost:" + RMIPortNum;

            RoomRecords exportedRoomRecords = new RoomRecords();
            objectURL = registryURL + "/RoomRecords" + args[0];
            Naming.rebind(objectURL, exportedRoomRecords);

            System.out.println("Server registered.  Registry currently contains:");
            // list names currently in the registry
            listRegistry(registryURL);
            System.out.println("RoomRecords Server ready.");
            // can have an infinite loop here since threads are created per connection for rmi
            DatagramSocket aSocket = null;
            try{
                aSocket = new DatagramSocket(6789);
                // create socket at agreed port
                byte[] buffer = new byte[1000];
                while(true){
                    DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                    aSocket.receive(request);
                    DatagramPacket reply = new DatagramPacket(request.getData(), request.getLength(),
                            request.getAddress(), request.getPort());
                    aSocket.send(reply);
                }
            }catch (SocketException e){
                System.out.println("Socket: " + e.getMessage());
                throw e;
            }catch (IOException e) {
                System.out.println("IO: " + e.getMessage());
                throw e;
            }finally {if(aSocket != null) aSocket.close();}


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
            LocateRegistry.createRegistry(RMIPortNum);
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
