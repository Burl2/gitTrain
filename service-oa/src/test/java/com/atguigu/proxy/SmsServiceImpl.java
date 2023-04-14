package com.atguigu.proxy;

//被代理类
public class SmsServiceImpl implements SmsService{
    @Override
    public String send(String msg) {
        System.out.println("send Message : " + msg);
        return msg;
    }
}
