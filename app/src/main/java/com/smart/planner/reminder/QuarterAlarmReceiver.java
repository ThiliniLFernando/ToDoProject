package com.smart.planner.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class QuarterAlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 000111;
    public static final String ACTION = "com.planner.alarm.reminder";
    public static final String TAG = "WAKE LOCK";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper.getPreviousReNotification(context,"Half","Success");
        notificationHelper.getManager().notify(19, nb.build());
        Toast.makeText(context, "Quarter Alarm Receiver !", Toast.LENGTH_SHORT).show();
    }

}
