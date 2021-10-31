package com.DistributedSystems.asg3.remote;

import java.util.HashMap;

public class WebServiceConstants {
    public static HashMap<String, Integer> webServicePorts = new HashMap<String, Integer>() {
        {
            put("DVL", 8081);
            put("KKL", 8082);
            put("WST", 8083);
        }
    };
}
