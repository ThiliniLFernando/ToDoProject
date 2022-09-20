package com.smart.planner.JobService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class QuarterChecker extends BroadcastReceiver {
    public static final int REQUEST_CODE = 12345;
    public static final String ACTION = "com.planner.alarm.reminder";
    public static final String TAG = "WAKE LOCK";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            System.out.println("Service has took a place"+new Date());
            Intent intentService = new Intent(context, ReminderCheckIntentService.class);
            intentService.putExtra("DOC_KEY", intent.getStringExtra("DOC_KEY"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.getApplicationContext().startForegroundService(intentService);
            } else {
                context.getApplicationContext().startService(intentService);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
