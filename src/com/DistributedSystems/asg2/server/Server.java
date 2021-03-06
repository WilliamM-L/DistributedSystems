package com.DistributedSystems.asg2.server;

import com.DistributedSystems.asg2.RoomRecordsObj.RoomRecordsCorba;
import com.DistributedSystems.asg2.RoomRecordsObj.RoomRecordsCorbaHelper;
import com.DistributedSystems.asg2.remote.RoomRecords;
import com.DistributedSystems.asg2.remote.UdpPacketType;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;

public class Server {
    private static HashMap<String, Integer> socketPorts = new HashMap<String, Integer>() {
        {
            put("DVL", 6789);
            put("KKL", 6790);
            put("WST", 6791);
        }
    };
    private static final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MM-yyyy").toFormatter(Locale.ENGLISH);

    public static void main(String[] args) {
        try{
//            String[] campusNames = {"DVL", "KKL", "WST"};
            String campusName = args[0];
            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBInitialHost", "localhost");
            props.put("org.omg.CORBA.ORBInitialPort", "8050");
            String[] newArgs = new String[0];
            // create and initialize the ORB
            ORB orb = ORB.init(newArgs, props);
            // get reference to rootpoa & activate the POAManager
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            // create servant and register it with the ORB
            HashMap<String, Integer> socketPortsToSend = new HashMap<>();
            for(Map.Entry<String, Integer> set: socketPorts.entrySet()){
                if (!set.getKey().equals(campusName)){
                    socketPortsToSend.put(set.getKey(), set.getValue());
                }
            }
            RoomRecords roomRecords = new RoomRecords(campusName, socketPortsToSend, orb);
//            roomRecords.setORB(orb);

            // get object reference from the servant
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(roomRecords);
            RoomRecordsCorba href = RoomRecordsCorbaHelper.narrow(ref);

            // get the root naming context
            // NameService invokes the name service
            org.omg.CORBA.Object objRef = orb.string_to_object("corbaloc::localhost:8050/NameService"); // InvalidName
            // Use NamingContextExt which is part of the Interoperable Naming Service (INS) specification.
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            // bind the Object Reference in Naming
            String name = "RoomRecords" + campusName;
            NameComponent path[] = ncRef.to_name(name);
            ncRef.rebind(path, href);
            new Thread(() -> {
                try {
                    openSocket(socketPorts.get(campusName), roomRecords);
                } catch (IOException e) {
                    System.out.println("Problem with socket!");
                    e.printStackTrace();
                }
            }).start();


            // wait for invocations from clients
            System.out.println("RoomRecords ready and waiting ...");
            orb.run();
        }
        catch (Exception e) {
            System.out.println("Exception in Server: " + e);
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }

    private static void openSocket(int socketPort, RoomRecords roomRecords) throws IOException {
        DatagramPacket reply = null;
        try (DatagramSocket socket = new DatagramSocket(socketPort)) {
            // create socket at agreed port
            byte[] buffer = new byte[1000];
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);
                String stringReceived = new String(buffer, 0, request.getLength());
                // string received will have the format: operation,arg1,arg2,....argN,garbage data
                String[] args = stringReceived.split(",");
                UdpPacketType operation =  UdpPacketType.UdpPacketType(Integer.parseInt(args[0])).get();
                switch (operation){
                    case GET_AVAILABLE_DATES:
                        LocalDate dateToCheck = LocalDate.parse(args[1], dateTimeFormatter);
                        String availabilities = roomRecords.getAvailableTimeSlot(dateToCheck);
                        reply = new DatagramPacket(availabilities.getBytes(), availabilities.length(), request.getAddress(), request.getPort());
                        break;
                    case CHANGE_RESERVATION:
                        String book_msg = roomRecords.bookRoom(args[1], Integer.parseInt(args[2]), args[3], args[4], args[5]);
                        reply = new DatagramPacket(book_msg.getBytes(), book_msg.length(), request.getAddress(), request.getPort());
                        break;
                    default:
                        String defaultMessage = "Does not this operation through udp messages";
                        reply = new DatagramPacket(defaultMessage.getBytes(), defaultMessage.length(), request.getAddress(), request.getPort());
                        break;
                }

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

}
