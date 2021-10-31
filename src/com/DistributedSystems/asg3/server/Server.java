package com.DistributedSystems.asg3.server;

import com.DistributedSystems.asg3.remote.RoomRecords;
import com.DistributedSystems.asg2.remote.UdpPacketType;
import com.DistributedSystems.asg3.remote.WebServiceConstants;

import javax.xml.ws.Endpoint;
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

            // create servant and register it with the ORB
            HashMap<String, Integer> socketPortsToSend = new HashMap<>();
            for(Map.Entry<String, Integer> set: socketPorts.entrySet()){
                if (!set.getKey().equals(campusName)){
                    socketPortsToSend.put(set.getKey(), set.getValue());
                }
            }
            RoomRecords roomRecords = new RoomRecords(campusName, socketPortsToSend);
            Endpoint endpoint = Endpoint.create(roomRecords);

            // get the root naming context
            // NameService invokes the name service
            // Use NamingContextExt which is part of the Interoperable Naming Service (INS) specification.
            // bind the Object Reference in Naming
            new Thread(() -> {
                try {
                    openSocket(socketPorts.get(campusName), roomRecords);
                } catch (IOException e) {
                    System.out.println("Problem with socket!");
                    e.printStackTrace();
                }
            }).start();

            endpoint.publish("http://127.0.0.1:"+ WebServiceConstants.webServicePorts.get(campusName) + "/" +  campusName);
            // wait for invocations from clients
            System.out.println("RoomRecords ready and waiting ...");
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
                        String availabilities = roomRecords.getLocalAvailableTimeSlot(dateToCheck);
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
