package com.mopframeworkcore.eventbus;


public class CustomerEvent {
    private static CustomerEvent customerEvent;

    private CustomerEvent() {
    }

    public static CustomerEvent getInstance() {
        if (customerEvent == null) {
            synchronized (CustomerEvent.class) {
                customerEvent = new CustomerEvent();
            }
        }
        return new CustomerEvent();
    }

    public EventType eventType;// 可能类型有很多种，数据也不一样
    public Object data;// 数据对象
    public Object data1;// 数据对象

    @Override
    public String toString() {
        return "CustomerEvent{" +
                "eventType=" + eventType +
                ", data=" + data +
                ", data1=" + data1 +
                '}';
    }
}
