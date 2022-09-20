package com.smart.planner.reminder;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.smart.planner.Main;
import com.smart.planner.R;


public class NotificationHelper extends ContextWrapper {
    public static final String channelID = "channelID";
    public static final String channelName = "Channel Name";

    private NotificationManager mManager;

    public NotificationHelper(Context base) {
        super(base);
        createChannel();
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel channel
                = new NotificationChannel(
                channelID,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
        );
        getManager().createNotificationChannel(channel);
    }

    public NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    public NotificationCompat.Builder getReminderNotification(Context context, String title, String content) {
        Intent intent = new Intent(context, Main.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

        Intent broadcastIntent = new Intent(context, NotifyDismissReceiver.class);
        PendingIntent actionIntent = PendingIntent.getBroadcast(context, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(getApplicationContext(), channelID)
                .setContentTitle(title)
                .setContentText(content)
                .setColor(Color.BLUE)
                .setContentIntent(contentIntent)
                .addAction(R.mipmap.ic_launcher, "Dismiss", actionIntent)
                .setSmallIcon(R.drawable.tm_logo_round);
    }

    public NotificationCompat.Builder getPreviousReNotification(Context context, String title, String content){
        return new NotificationCompat.Builder(getApplicationContext(), channelID)
                .setContentTitle(title)
                .setContentText(content)
                .setColor(Color.BLUE)
                .setSmallIcon(R.drawable.tm_logo_round);
    }
}
