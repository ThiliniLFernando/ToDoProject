package com.smart.planner.POJOs;

import com.google.firebase.firestore.Exclude;

public class Reminder {
    private boolean isEvent;
    public Reminder(){}

    public Reminder(boolean answer){
        this.isEvent = answer;
    }

    public boolean isEvent() {
        return isEvent;
    }

    public void setEvent(boolean event) {
        isEvent = event;
    }
}
