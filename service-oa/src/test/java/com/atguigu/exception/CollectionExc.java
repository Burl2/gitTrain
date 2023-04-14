package com.atguigu.exception;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CollectionExc {

    public static void main(String[] args) {

        //1
        class Person {
            private String name;
            private String phoneNumber;

            Person(String name,String phone) {
                this.name=name;
                this.phoneNumber=phone;
            }
            public String getName() {
                return name;
            }

            public String getPhoneNumber() {
                return phoneNumber;
            }

            public void setName(String name) {
                this.name = name;
            }

            public void setPhoneNumber(String phoneNumber) {
                this.phoneNumber = phoneNumber;
            }
        }

//        ArrayList<Person> list = new ArrayList<>();
//        list.add(new Person("jack","18163138123"));
//        list.add(new Person("martin",null));


//        Map<String, String> map = list.stream().collect(Collectors.toMap(Person::getName, Person::getPhoneNumber));
        //2
//        ArrayList<Integer> list = new ArrayList<>();
//        for (int i = 0; i < 10; i++) {
//            list.add(i);
//        }
//        list.forEach(e->list.add(e));


        //3
        int[] arr = {1,2,3};
//        List<int[]> list = Arrays.asList(arr);
//        System.out.println(list.size());
//        System.out.println(list.get(0)[1]);
//        int[] ints = list.get(0);
//        System.out.println(ints[2]);
        Integer[] arr1 = {1,2,3};
//        System.out.println(Arrays.asList(arr).getClass());
//        System.out.println(Arrays.asList(arr1).getClass());
        List<Integer> list = Arrays.asList(arr1);
        Class<? extends List> aClass1 = list.getClass();
        Class<? extends List> aClass = Arrays.asList(arr1).getClass();
        System.out.println(aClass1);
//        list.add(5);

    }
}
