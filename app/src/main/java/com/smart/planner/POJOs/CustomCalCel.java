package com.smart.planner.POJOs;

import androidx.annotation.NonNull;

import java.time.LocalDate;
import java.util.ArrayList;

public class CustomCalCel {
    private boolean hasRandomHeight = false;
    private boolean hasRandomWidth = false ;
    private boolean isHeadCel = false;
    private boolean isBodyCel = false;
    private boolean isFooterCel = false;
    private LocalDate date = null;
    private Task task;
    private String text = null;
    private String colorCode ;
    private ArrayList<String> event ;

    public CustomCalCel(String mText){
        text = mText;
    }

    public String getColorCode() {
        return colorCode;
    }

    public CustomCalCel setEvent(ArrayList<String> event) {
        this.event = event;
        return this;
    }

    public ArrayList<String> getEvent() {
        return event;
    }

    public CustomCalCel setColorCode(String colorCode) {
        this.colorCode = colorCode;
        return this;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public String getText() {
        return text;
    }

    public LocalDate getDate() {
        return date;
    }

    public boolean isHasRandomHeight() {
        return hasRandomHeight;
    }

    public boolean isHasRandomWidth() {
        return hasRandomWidth;
    }

    public boolean isBodyCel() {
        return isBodyCel;
    }

    public boolean isFooterCel() {
        return isFooterCel;
    }

    public boolean isHeadCel() {
        return isHeadCel;
    }

    public CustomCalCel setDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public CustomCalCel setText(String text) {
        this.text = text;
        return this;
    }

    public CustomCalCel setIsABodyCel(boolean bodyCel) {
        isBodyCel = bodyCel;
        return this;
    }

    public CustomCalCel setIsAFooterCel(boolean footerCel) {
        isFooterCel = footerCel;
        return this;
    }

    public CustomCalCel setIsAHeadCel(boolean headCel) {
        isHeadCel = headCel;
        return this;
    }

    public CustomCalCel setHasRandomHeight(boolean hasRandomHeight) {
        this.hasRandomHeight = hasRandomHeight;
        return this;
    }

    public CustomCalCel setHasRandomWidth(boolean hasRandomWidth) {
        this.hasRandomWidth = hasRandomWidth;
        return this;
    }

    @NonNull
    @Override
    public CustomCalCel clone(){
        try {
            return (CustomCalCel) super.clone();
        }catch(CloneNotSupportedException ex){
            return null;
        }
    }
}

