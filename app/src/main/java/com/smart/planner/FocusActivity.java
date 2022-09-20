package com.smart.planner;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.smart.planner.POJOs.Task;

import java.util.Calendar;
import java.util.Locale;

public class FocusActivity extends AppCompatActivity {

    private TextView taskName;
    private Button startPause, reset;
    private ImageButton activityClose;
    private Chronometer mChronometer;

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
                }
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
    }
}