package com.smart.planner.LocalDB.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.smart.planner.LocalDB.NotificationReminder;

import java.util.List;

@Dao
public interface NotificationReminderDAO {
    @Insert
    void insertAll(List<NotificationReminder> reminders);

    @Insert
    void insertOne(NotificationReminder reminder);

    @Update
    void update(NotificationReminder reminder);

    @Delete
    void delete(NotificationReminder reminder);

    @Delete
    void deleteAll(List<NotificationReminder> reminders);

    @Query("UPDATE reminder SET title = :title WHERE doc_id = :docId")
    void updateTitleByDocId(String title,String docId);

    @Query("UPDATE reminder SET title = :title, time=:time,`desc` =:desc WHERE doc_id = :docId")
    void updateReminderByDocId(String docId,String title,long time,String desc);

    @Query("DELETE FROM reminder WHERE doc_id = :docId")
    void deleteByDocId(String docId);

    @Query("SELECT * FROM reminder")
    List<NotificationReminder> getAllReminders();

    @Query("SELECT * FROM reminder WHERE time BETWEEN :time1 AND :time2")
    public List<NotificationReminder> loadAllReminderByTime(long time1,long time2);

}
