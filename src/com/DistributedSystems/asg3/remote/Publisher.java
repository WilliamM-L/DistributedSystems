package com.DistributedSystems.asg3.remote;

import javax.xml.ws.Endpoint;
import java.util.HashMap;

public class Publisher {

    public static void main(String[] args) {
        Endpoint ep = Endpoint.create(new RoomRecords("test", new HashMap<>()));
        ep.publish("http://127.0.0.1:8080/auth");
    }

}
