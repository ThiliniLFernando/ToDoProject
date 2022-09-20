package com.smart.planner.POJOs;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Event extends Reminder implements Serializable,Cloneable {
    private String documentId ;
    private String eventTitle;
    private Date dueDate;
    private Date startTime;
    private Date endTime;
    private boolean isReminderOn;
    private Date reminderTime;
    private String location;
    private String note;
    private String colorCode ;
    private String invitation;
    private Date inviteTime;
    private ArrayList<Contact> invitees;
    private ArrayList<String> attachments;

    public Event(){
        super(true);
    }

    public Event(String event){
        super(true);
        this.eventTitle = event;
    }

    @Override
    public boolean isEvent() {
        return super.isEvent();
    }

    @Override
    public void setEvent(boolean event) {
        super.setEvent(true);
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public boolean isReminderOn() {
        return isReminderOn;
    }

    public void setReminderOn(boolean reminderOn) {
        isReminderOn = reminderOn;
    }

    public Date getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(Date reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ArrayList<Contact> getInvitees() {
        return invitees;
    }

    public void setInvitees(ArrayList<Contact> invitees) {
        this.invitees = invitees;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public ArrayList<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(ArrayList<String> attachments) {
        this.attachments = attachments;
    }

    public Date getInviteTime() {
        return inviteTime;
    }

    public String getInvitation() {
        return invitation;
    }

    public void setInvitation(String invitation) {
        this.invitation = invitation;
    }

    public void setInviteTime(Date inviteTime) {
        this.inviteTime = inviteTime;
    }

    @NonNull
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

