package com.DistributedSystems.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class test {
    public static void main(String[] args) throws InterruptedException {
        ConcurrentHashMap<Integer, List<String>> map = new ConcurrentHashMap<>();
        map.put(1, new ArrayList<>(){{add("a");}});

        new Thread(() -> {
//            synchronized (map){
                try {
//                    TimeUnit.SECONDS.sleep(1);
                    List<String> list = map.get(1);
                    synchronized (list){
                        TimeUnit.SECONDS.sleep(2);
                        list.add("b");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
//            }
        }).start();

        new Thread(() -> {
//            synchronized (map){
                try {
                    List<String> list = map.get(1);
                    synchronized (list){
                        System.out.println("list before deletion:" + list);
                        list.remove("b");
                        System.out.println("list after deletion:" + list);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
//            }
        }).start();

    }
}
