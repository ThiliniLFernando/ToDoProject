package com.smart.planner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.smart.planner.POJOs.FocusTime;
import com.smart.planner.POJOs.Task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class FocusActivity extends AppCompatActivity {

    private TextView taskName;
    private Button startPause, reset,focusDone;
    private ImageButton activityClose;
    private Chronometer mChronometer;
    private FrameLayout frameLayout;

    private long pauseOffset;
    private boolean running;

    Task task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus);
        Bundle bundle = getIntent().getExtras();
        init();
        if (bundle != null) {
            String jsonString = bundle.getString("taskObj");
            if (!jsonString.isEmpty()) {
                task = new Gson().fromJson(jsonString, Task.class);
            }
        }

        startPause.setVisibility(View.VISIBLE);
        focusDone.setVisibility(View.INVISIBLE);
        mChronometer.setFormat("Time: %s");
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                long time = SystemClock.elapsedRealtime() - chronometer.getBase();
                int hr = (int) time / 3600000;
                int m = (int) (time - hr * 3600000) / 60000;
                int s = (int) (time - hr * 3600000 - m * 60000) / 1000;
                String t = (hr < 10 ? "0" + hr : hr) + ":" + (m < 10 ? "0" + m : m) + ":" + (s < 10 ? "0" + s : s);
                chronometer.setText(t);
            }
        });
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.setText("00:00:00");

        taskName.setText(task.getTaskName());
        startPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!running) {
                    mChronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
                    mChronometer.start();
                    running = true;
                    startPause.setVisibility(View.INVISIBLE);
                    focusDone.setVisibility(View.VISIBLE);
                }
            }
        });

        focusDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChronometer.stop();
                startPause.setVisibility(View.VISIBLE);
                focusDone.setVisibility(View.INVISIBLE);
                AlertDialog.Builder conAlert = new AlertDialog.Builder(FocusActivity.this.getApplicationContext());
                conAlert.setTitle("Confirmation");
                conAlert.setMessage("Do you want to add this time as a Focus Time ?");
                conAlert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        long time = SystemClock.elapsedRealtime() - mChronometer.getBase();
                        long hours = time/(3600*1000);
                        time = time%(3600*1000);
                        long minutes = time/(60*1000);
                        time = time%(60*1000);
                        long seconds = time/(1000);
                        FocusTime ft = new FocusTime();
                        ft.setDate(new Date());
                        ft.setHours((int)hours);
                        ft.setMinutes((int)minutes);
                        ft.setSeconds((int)seconds);
                        task.getFocusTimes().add(ft);
                        final HashMap<String, Object> updates
                                = new HashMap<>();
                        updates.put("focusTimes",task.getFocusTimes());
                        FirebaseFirestore.getInstance().collection("Users").document(Main.CURRENT_USER_KEY)
                                .collection("Tasks")
                                .document(task.getDocumentId()).update(updates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                                        Toast.makeText(FocusActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
                conAlert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                conAlert.show();
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void startTimer() {

    }

    private void pauseTimer() {

    }

    private void resetTimer() {

    }

    private void init() {
        taskName = findViewById(R.id.task_name);
        activityClose = findViewById(R.id.activity_close);
        startPause = findViewById(R.id.start_pause);
        reset = findViewById(R.id.focus_reset);
        mChronometer = findViewById(R.id.chronometer);
        frameLayout = findViewById(R.id.frame_layout);
        focusDone = findViewById(R.id.focus_done);
    }
}