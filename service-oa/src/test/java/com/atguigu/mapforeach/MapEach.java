package com.atguigu.mapforeach;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MapEach {

    public static void main(String[] args) {

        HashMap<String, String> map = new HashMap<>();
        map.put("language","java");
        map.put("sdk","jdk");
        map.put("version","1.8");
        map.put("orm","MyBatis framework");
        map.put("been","spring framework");
        map.put("社区","Java中文社区");


        //1
//        Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
//        while (iterator.hasNext()) {
//            Map.Entry<String, String> entry = iterator.next();
//            System.out.println(entry.getKey()+":   "+entry.getValue());
//        }


        //2
//        Iterator<String> iterator = map.keySet().iterator();
//        while (iterator.hasNext()) {
//            String key = iterator.next();
//            String value = map.get(key);
//            System.out.println(key+":   "+value);
//        }


        //3
//        for (Map.Entry<String,String> entry : map.entrySet()) {
//            System.out.println(entry.getKey()+":   "+entry.getValue());
//        }

        //4
//        for (String key: map.keySet()) {
//            System.out.println(key+":   "+map.get(key));
//        }


        //5
//        map.forEach((key,value) -> {
//            System.out.println(key+":   "+value);
//        });

       //6
       map.entrySet().stream().forEach(e-> System.out.println(e.getKey()+":   "+e.getValue()));

    }
}
