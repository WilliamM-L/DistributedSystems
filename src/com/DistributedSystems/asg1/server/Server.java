package com.DistributedSystems.asg1.server;

import com.DistributedSystems.asg1.remote.RoomRecords;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;

public class Server {
    private static HashMap<String, Integer> socketPorts = new HashMap<>() {
        {
            put("DVL", 6789);
            put("KKL", 6790);
            put("WST", 6791);
        }
    };
    private static final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MM-yyyy").toFormatter(Locale.ENGLISH);

    public static void main(String[] args) {
        String registryURL;
        String objectURL;
        try{
//            String[] campusNames = {"DVL", "KKL", "WST"};
            String campusName = args[0];
            int RMIPortNum = 1313;
            startRegistry(RMIPortNum);
            registryURL = "rmi://localhost:" + RMIPortNum;
            int socketPort = socketPorts.get(campusName);

            HashMap<String, Integer> socketPortsToSend = new HashMap<>();
            for(Map.Entry<String, Integer> set: socketPorts.entrySet()){
                if (!set.getKey().equals(campusName)){
                    socketPortsToSend.put(set.getKey(), set.getValue());
                }
            }
            RoomRecords exportedRoomRecords = new RoomRecords(campusName, socketPortsToSend);
            objectURL = registryURL + "/RoomRecords" + campusName;
            Naming.rebind(objectURL, exportedRoomRecords);

            System.out.println("Server registered.  Registry currently contains:");
            // list names currently in the registry
            listRegistry(registryURL);
            System.out.println("RoomRecords Server ready.");
            // can have an infinite loop here since threads are created per connection for rmi
            try (DatagramSocket socket = new DatagramSocket(socketPort)) {
                // create socket at agreed port
                byte[] buffer = new byte[1000];
                while (true) {
                    DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                    socket.receive(request);
                    LocalDate dateToCheck = LocalDate.parse(new String(buffer, 0, request.getLength()).substring(0,10), dateTimeFormatter);
                    String availabilities = exportedRoomRecords.getAvailableTimeSlot(dateToCheck);
                    DatagramPacket reply = new DatagramPacket(availabilities.getBytes(), availabilities.length(), request.getAddress(), request.getPort());
                    socket.send(reply);
                }
            } catch (SocketException e) {
                System.out.println("Socket: " + e.getMessage());
                throw e;
            } catch (IOException e) {
                System.out.println("IO: " + e.getMessage());
                throw e;
            }


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
