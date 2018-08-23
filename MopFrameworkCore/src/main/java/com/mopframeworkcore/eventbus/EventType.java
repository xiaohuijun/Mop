package com.mopframeworkcore.eventbus;

public class EventType {
    private int eType;

    public EventType(int eType) {
        this.eType = eType;
    }

    public int geteType() {
        return eType;
    }

    public void seteType(int eType) {
        this.eType = eType;
    }
}
