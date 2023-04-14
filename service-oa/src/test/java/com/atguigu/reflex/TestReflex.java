package com.atguigu.reflex;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TestReflex {

    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {

        Class<?> targetClass = Class.forName("com.atguigu.reflex.TargetObject");

        TargetObject targetObject = (TargetObject) targetClass.newInstance();

        Method[] methods = targetClass.getDeclaredMethods();
        for (Method method : methods) {
            System.out.println(method.getName());
        }

        Method publicMethod = targetClass.getDeclaredMethod("publicMethod", String.class);

        publicMethod.invoke(targetObject,"oooommmm");



        Field field = targetClass.getDeclaredField("value");
        field.setAccessible(true);
        field.set(targetObject,"wogailecanshu");

        Method privateMethod = targetClass.getDeclaredMethod("privateMethod");
        privateMethod.setAccessible(true);
        privateMethod.invoke(targetObject);
    }

}
