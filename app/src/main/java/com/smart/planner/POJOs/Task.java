package com.smart.planner.POJOs;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Task extends Reminder implements Serializable,Cloneable {
    private String documentId ;
    private String reminderType;
    private String taskUniqueId;
    private String taskName;
    private String listName;
    private String priority;
    private String repeatMethod ;
    private boolean isCompleted ;
    private Date dueDate ;
    private Date completeDate;

    private ArrayList<Date> reminders ;

    public Task(){
        super(false);
    }

    public Task(String taskName,String listName){
        super(false);
        this.taskName = taskName ;
        this.listName = listName ;
    }

    @Override
    public boolean isEvent() {
        return super.isEvent();
    }

    @Override
    public void setEvent(boolean event) {
        super.setEvent(false);
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getReminderType() {
        return reminderType;
    }

    public void setReminderType(String reminderType) {
        this.reminderType = reminderType;
    }

    public void setCompleteDate(Date completeDate) {
        this.completeDate = completeDate;
    }

    public String getTaskUniqueId() {
        return taskUniqueId;
    }

    public void setTaskUniqueId(String taskUniqueId) {
        this.taskUniqueId = taskUniqueId;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getListName() {
        return listName;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getRepeatMethod() {
        return repeatMethod;
    }

    public void setRepeatMethod(String repeatMethod) {
        this.repeatMethod = repeatMethod;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public ArrayList<Date> getReminders() {
        return reminders;
    }

    public void setReminders(ArrayList<Date> reminder) {
        this.reminders = reminder;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
