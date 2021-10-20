package com.DistributedSystems.asg2.remote;

import java.util.Arrays;
import java.util.Optional;

public enum UdpPacketType {
    GET_AVAILABLE_DATES(0),
    CHANGE_RESERVATION(1);

    private final int value;

    UdpPacketType(int value) {
        this.value = value;
    }

    public static Optional<UdpPacketType> UdpPacketType(int value) {
        return Arrays.stream(values())
                .filter(udpPacketType -> udpPacketType.value == value)
                .findFirst();
    }
}
