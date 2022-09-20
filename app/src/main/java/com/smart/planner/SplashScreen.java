package com.smart.planner;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.firebase.database.FirebaseDatabase;
import com.smart.planner.JobService.QuarterChecker;
import com.smart.planner.LocalDB.SQLiteHelper;

import java.util.Calendar;

public class SplashScreen extends AppCompatActivity{
    public String CURRENT_USER_KEY;
    int APP_OPENING_TIME_COUNT;
    private boolean FIRST_TIME_APP_OPEN ;

    final Handler mHandler = new Handler();
    final Calendar myCalendar = Calendar. getInstance () ;

    FirebaseDatabase database;
    SQLiteHelper sqLiteHelper;
    SQLiteDatabase sqLiteDatabase;
    SharedPreferences sharedPreferences;
    private ImageView splashScreen_tick;

    private void initComponents() {
        splashScreen_tick = findViewById(R.id.splashScreen_logo);
        database = FirebaseDatabase.getInstance() ;
    }

    private void animateLogo() {
        Animation animation2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.app_logo_animation);
        animation2.setDuration(3000);
        splashScreen_tick.startAnimation(animation2);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        initComponents();
        animateLogo();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        // APP_OPENING_TIME_COUNT = sharedPreferences.getInt("app_opening_time_COUNT", -1);
        FIRST_TIME_APP_OPEN = sharedPreferences.getBoolean("first_time_app_open",false);
        CURRENT_USER_KEY = sharedPreferences.getString("current_user_KEY", null);
        if (CURRENT_USER_KEY != null) {
            // user exist
            animateLogo();
            if (FIRST_TIME_APP_OPEN) {
                // 1st time logged

                // sqlite helper
//                sqLiteHelper = new SQLiteHelper(getApplicationContext());
//                sqLiteDatabase = sqLiteHelper.getWritableDatabase();
//                fetchAllUserData(CURRENT_USER_KEY);

                startActivity(new Intent(SplashScreen.this, Main.class));
                //overridePendingTransition(R.anim.layout_translate_ltr_design,R.anim.layout_translate_rtl_design);
                finish();
            }else {
                // start main activity
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(SplashScreen.this, Main.class));
                        //overridePendingTransition(R.anim.layout_translate_ltr_design,R.anim.layout_translate_rtl_design);
                        finish();
                    }
                },4000);
            }

        } else {
            // user not exist
            // start sign in
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashScreen.this, SignIn.class));
                    //overridePendingTransition(R.anim.layout_translate_ltr_design,R.anim.layout_translate_rtl_design);
                    finish();
                }
            },4000);
        }
    }

    public void scheduleHalfNotificationChecker() {
        Intent intent = new Intent(getApplicationContext(), QuarterChecker.class);
        intent.putExtra("DOC_KEY",CURRENT_USER_KEY);
        PendingIntent pIntent = PendingIntent.getBroadcast(this, QuarterChecker.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, pIntent);
    }

    // fetch user data
    private void fetchAllUserData(String current_user_key) {
        //
        /*
        database.getReference("lists").child(current_user_key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot d : dataSnapshot.getChildren()) {
                            String list_name = DatabaseUtils.sqlEscapeString(d.getKey());
                            String list_color = d.getValue().toString();
                            sqLiteDatabase.execSQL("INSERT INTO list (\n" +
                                    "                     listName,\n" +
                                    "                     listColor,\n" +
                                    "                     isSync\n" +
                                    "                 )\n" +
                                    "                 VALUES (\n" +
                                    "                     "+list_name+",\n" +
                                    "                     '"+list_color+"',\n" +
                                    "                     '1'\n" +
                                    "                 )");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        */
        //
    }



    public void cancelHalfNotificationChecker(){

    }
}


