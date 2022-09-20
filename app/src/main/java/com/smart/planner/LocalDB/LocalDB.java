package com.smart.planner.LocalDB;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.smart.planner.LocalDB.DAO.NotificationReminderDAO;

@Database(entities = NotificationReminder.class,exportSchema = false,version = 8)
public abstract class LocalDB extends RoomDatabase {
    private static final String DB_NAME = "reminder_db";
    private static LocalDB instance;

    public static synchronized LocalDB getInstance(Context context){
        if (instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(),LocalDB.class,DB_NAME)
            .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public abstract NotificationReminderDAO notificationReminderDAO();

}
