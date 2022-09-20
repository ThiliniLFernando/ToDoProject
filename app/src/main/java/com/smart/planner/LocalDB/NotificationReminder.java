package com.smart.planner.LocalDB;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "reminder")
public class NotificationReminder {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "doc_id")
    private String docId;

    @ColumnInfo(name = "time")
    private long time;

    @ColumnInfo(name = "reminded")
    private boolean doneRemind;

    @NonNull
    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "desc")
    private String description;

    public NotificationReminder(@NonNull String docId,
                                long time,
                                @NonNull String title,
                                String description) {
        this.docId = docId;
        this.time = time;
        this.title = title;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getDocId() {
        return docId;
    }

    public void setDocId(@NonNull String docId) {
        this.docId = docId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isDoneRemind() {
        return doneRemind;
    }

    public void setDoneRemind(boolean doneRemind) {
        this.doneRemind = doneRemind;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
