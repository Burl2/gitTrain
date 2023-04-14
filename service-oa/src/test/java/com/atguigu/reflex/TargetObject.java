package com.atguigu.reflex;


public class TargetObject {
    private String value;

    TargetObject() {
        value = "testreflex!";
    }

    public void publicMethod(String s) {
        System.out.println("test:  " + s);
    }

    private void privateMethod() {
        System.out.println("value is  " + value);
    }
}
