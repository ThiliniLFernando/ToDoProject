package com.smart.planner.POJOs;
import java.util.ArrayList;

public class Reminder {
    private String documentId ;
    private boolean isEvent;
    private ArrayList<FocusTime> focusTimes = new ArrayList<>();

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

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public ArrayList<FocusTime> getFocusTimes() {
        return focusTimes;
    }

    public void setFocusTimes(ArrayList<FocusTime> focusTimes) {
        this.focusTimes = focusTimes;
    }
}
