package com.smart.planner.POJOs;

import com.google.firebase.firestore.Exclude;

public class CustomDate {
    private String date;
    private String time ;

    public CustomDate(){

    }

    public CustomDate(String date,String time){
        this.date = date ;
        this.time = time ;
    }

    public CustomDate(String date){
        this.date = date ;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Exclude
    public String getDateAndTime(){
        return this.date+" "+this.time;
    }
}
