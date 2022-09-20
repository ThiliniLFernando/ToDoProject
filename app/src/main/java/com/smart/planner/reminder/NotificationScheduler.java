package com.smart.planner.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.smart.planner.LocalDB.NotificationReminder;
import com.smart.planner.POJOs.Task;

import java.util.Calendar;

public class NotificationScheduler {

    public static void scheduleNotifyMe(Calendar c, NotificationReminder task, Context context) {
        String contentText = "";
        String notify_type = "";
        int t = (int) ((task.getTime() - c.getTime().getTime())/60000);
        if (t == 0){
            contentText = "Now";
            notify_type = "reminder";
        }else if (t == 5){
            contentText = " after 5 minutes" ;
            notify_type = "previous_reminder";
        }else if (t == 30){
            contentText = " after 30 minutes" ;
            notify_type = "previous_reminder";
        }else if (t == 60){
            contentText = " after 1 hour" ;
            notify_type = "previous_reminder";
        }else if (t == 1440){
            contentText = " for Tomorrow" ;
            notify_type = "previous_reminder";
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(NotificationReceiver.NOTIFICATION_ID,task.getDocId());
        intent.putExtra(NotificationReceiver.NOTIFICATION_TITLE, task.getTitle());
        intent.putExtra(NotificationReceiver.NOTIFICATION_CONTENT, "Task scheduled"+contentText);
        intent.putExtra(NotificationReceiver.NOTIFICATION_TYPE,notify_type);

        SharedPreferences pref = context.getSharedPreferences("com.smart.planner.NotificationScheduler",Context.MODE_PRIVATE);
        int count = pref.getInt("pi_id",0);
        long piId = new Long(count+""+c.getTimeInMillis()) ;

        PendingIntent pendingIntent = PendingIntent
                .getBroadcast(
                        context,
                        (int) piId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);

        count++ ;
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("pi_id",count);
    }

    private void cancelAlarm(Task task,Context context) {
        task.getDueDate().getTime();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, NotificationReceiver.class);

        PendingIntent pendingIntent = PendingIntent
                .getBroadcast(
                        context,
                        1,
                        intent,
                        0
                );

        alarmManager.cancel(pendingIntent);
    }
}
