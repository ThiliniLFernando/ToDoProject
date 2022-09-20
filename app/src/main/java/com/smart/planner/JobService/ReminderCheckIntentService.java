package com.smart.planner.JobService;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.smart.planner.LocalDB.DAO.NotificationReminderDAO;
import com.smart.planner.LocalDB.LocalDB;
import com.smart.planner.LocalDB.NotificationReminder;
import com.smart.planner.R;
import com.smart.planner.reminder.NotificationScheduler;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ReminderCheckIntentService extends IntentService {

    private String CHANNEL_ID = "1";
    private FirebaseFirestore fireStore;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    public ReminderCheckIntentService() {
        super("ReminderCheckIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("MyTestService onCreate");
        createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Title")
                .setContentText("Progressing..")
                .setPriority(NotificationCompat.PRIORITY_MIN);
        Notification notification = builder.build();
        startForeground(1,notification);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String DOC_KEY = intent.getStringExtra("DOC_KEY");
            if (DOC_KEY != null){
                long current = System.currentTimeMillis()+60000;
                NotificationReminderDAO dao =
                        LocalDB.getInstance(getApplicationContext()).notificationReminderDAO();
                for(NotificationReminder reminder : dao.loadAllReminderByTime(current,(current+900000))){
                    System.out.println("Doc Id "+reminder.getDocId()+" \n" +
                            "Name "+reminder.getTitle()+" \n" +
                            "Long Time "+reminder.getTime()+" \n");
                    NotificationScheduler.scheduleNotifyMe(Calendar.getInstance(),reminder,getApplicationContext());
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}