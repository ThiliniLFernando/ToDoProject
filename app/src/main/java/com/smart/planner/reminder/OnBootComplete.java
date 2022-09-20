package com.smart.planner.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class OnBootComplete extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent i) {
        if (i.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);

            Intent intent = new Intent(context, QuarterAlarmReceiver.class) ;
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,QuarterAlarmReceiver.REQUEST_CODE,intent,0);

            Calendar now = Calendar.getInstance() ;
            now.setTimeInMillis(System.currentTimeMillis());

            Calendar start = Calendar.getInstance();
            start.set(Calendar.HOUR_OF_DAY,1);
            start.set(Calendar.MINUTE,00);
            start.set(Calendar.SECOND,00);

            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),1000*60*60,pendingIntent);
        }
    }
}
