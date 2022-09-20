package com.smart.planner.POJOs;

import java.util.ArrayList;

public class LocationReminder {
    private String reminderTitle ;
    private ArrayList<String> groceryList ;
    private double latitude ;
    private double longitude ;

    public LocationReminder(){}

    public LocationReminder(String reminderTitle, double latitude, double longitude) {
        this.reminderTitle = reminderTitle;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setReminderTitle(String reminderTitle) {
        this.reminderTitle = reminderTitle;
    }

    public void setGroceryList(ArrayList<String> groceryList) {
        this.groceryList = groceryList;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getReminderTitle() {
        return reminderTitle;
    }

    public ArrayList<String> getGroceryList() {
        return groceryList;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
