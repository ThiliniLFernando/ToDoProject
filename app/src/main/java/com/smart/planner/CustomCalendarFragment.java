package com.smart.planner;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.smart.planner.Adapters.CalendarCel;
import com.smart.planner.POJOs.CustomCalCel;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CustomCalendarFragment extends Fragment {
    private Spinner calendarDropdown;
    private FrameLayout calendarDynamicFrame;
    private Fragment selectedFragment;
    protected LocalDate currentCalDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customcalendar, container, false);
        initComponents(view);
        return view;
    }

    private void initComponents(View view) {
        currentCalDate = LocalDate.now();
        calendarDropdown = view.findViewById(R.id.calendar_dropdown);
        calendarDynamicFrame = view.findViewById(R.id.calendarDynamicFrame);
        selectedFragment = new FragmentCalendar_Month(this,currentCalDate);
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.calendarDynamicFrame, selectedFragment)
                .commit();

        calendarDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        // monthly
                        selectedFragment = new FragmentCalendar_Month(CustomCalendarFragment.this,currentCalDate);
                        break;
                    case 1:
                        //weekly
                        selectedFragment = new FragmentCalendar_Week(CustomCalendarFragment.this,currentCalDate);
                        //layoutManager = new GridLayoutManager(getContext(), 7, GridLayoutManager.HORIZONTAL, false);
                        break;
                    case 2:
                        //3 day
                        selectedFragment = new FragmentCalendar_3Day(CustomCalendarFragment.this,currentCalDate);
                        //layoutManager = new GridLayoutManager(getContext(), 3, GridLayoutManager.HORIZONTAL, false);
                        break;
                    case 3:
                        // a day
                        selectedFragment = new FragmentCalendar_ADay(CustomCalendarFragment.this,currentCalDate);
                        //layoutManager = new GridLayoutManager(getContext(), 1, GridLayoutManager.HORIZONTAL, false);
                        break;
                }
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.calendarDynamicFrame, selectedFragment)
                        .commit();
                //recyclerView.setLayoutManager(layoutManager);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(getContext(), "Nothing Selected " + adapterView.getCount(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
