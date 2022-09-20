package com.smart.planner.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {
    public static final String NOTIFICATION_ID = "key";
    public static final String NOTIFICATION_TITLE = "notify_title";
    public static final String NOTIFICATION_CONTENT = "notify_content";
    public static final String NOTIFICATION_TYPE = "notify_type";


    @Override
    public void onReceive(Context context, Intent intent) {
        // IT USUFULL WHEN MULTIPLE NOTIFICATION TO SCHEDULE
        SharedPreferences pref = context.getSharedPreferences("com.smart.planner.NotificationReceiverPref", Context.MODE_PRIVATE);
        int visitCount = pref.getInt("visit_count", 0);
        // IT USUFULL WHEN MULTIPLE NOTIFICATION TO SCHEDULE

        String title = intent.getStringExtra(NOTIFICATION_TITLE);
        String content = intent.getStringExtra(NOTIFICATION_CONTENT);
        String notify_type = intent.getStringExtra(NOTIFICATION_TYPE);
        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = null;

        if (notify_type.equals("reminder")) {
            nb = notificationHelper.getReminderNotification(context, title, content);
        } else {
            nb = notificationHelper.getPreviousReNotification(context, title, content);
        }

        if (nb != null)
            notificationHelper.getManager().notify(visitCount, nb.build());

        visitCount++;
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("visit_count", visitCount);
    }
}
