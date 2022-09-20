package com.smart.planner;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smart.planner.Adapters.CalendarCel;
import com.smart.planner.POJOs.CustomCalCel;

import java.time.LocalDate;
import java.util.ArrayList;

public class FragmentCalendar_3Day extends Fragment {
    private TextView year, month, forward, backward;
    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
    private LocalDate currentCalDate;
    private ArrayList<CustomCalCel> cells;
    private CalendarCel calendarViewAdapter;
    private CustomCalendarFragment fragment ;

    public FragmentCalendar_3Day(){}

    FragmentCalendar_3Day(CustomCalendarFragment fragment, LocalDate cuLocalDate){
        this.fragment = fragment;
        this.currentCalDate = cuLocalDate;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar_3day, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initComponents(view);
    }

    private void initComponents(View view) {
        year = view.findViewById(R.id.year);
        month = view.findViewById(R.id.month);
        forward = view.findViewById(R.id.forward);
        backward = view.findViewById(R.id.backward);
        recyclerView = view.findViewById(R.id.custom_calendar);

        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);

        cells = new ArrayList<>();

        calendarViewAdapter = new CalendarCel(cells, this, recyclerView, null, metrics.heightPixels, metrics.widthPixels, metrics.density);
        recyclerView.setAdapter(calendarViewAdapter);
        layoutManager = new GridLayoutManager(getContext(), 3, GridLayoutManager.HORIZONTAL, false);
        recyclerView.setHasFixedSize(true);

        DividerItemDecoration div1 = new DividerItemDecoration(getContext(), DividerItemDecoration.HORIZONTAL);
        div1.setDrawable(new ColorDrawable(Color.parseColor("#808080")));
        recyclerView.addItemDecoration(div1);

        DividerItemDecoration div2 = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        div2.setDrawable(new ColorDrawable(Color.parseColor("#808080")));
        recyclerView.addItemDecoration(div2);

        recyclerView.setLayoutManager(layoutManager);
        displayNext3days();

        backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentCalDate != null) {
                    layoutManager = new GridLayoutManager(getContext(), 3, GridLayoutManager.HORIZONTAL, false);
                    currentCalDate = currentCalDate.minusDays(3);
                    recyclerView.setLayoutManager(layoutManager);
                    displayNext3days();
                }
            }
        });

        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentCalDate != null) {
                    layoutManager = new GridLayoutManager(getContext(), 3, GridLayoutManager.HORIZONTAL, false);
                    currentCalDate = currentCalDate.plusDays(3);
                    recyclerView.setLayoutManager(layoutManager);
                    displayNext3days();
                }
            }
        });
    }

    private void displayNext3days() {
        cells.clear();
        ArrayList<LocalDate> dates = new ArrayList();
        year.setText(currentCalDate.getYear() + "");
        month.setText(currentCalDate.getMonth().name());

        LocalDate start = currentCalDate;
        LocalDate end = currentCalDate.plusDays(3);
        while (start.isBefore(end)) {
            cells.add(new CustomCalCel(start.getDayOfWeek().name().substring(0, 3)).setIsAHeadCel(true).setHasRandomHeight(false));
            start = start.plusDays(1);
        }
        LocalDate start2 = currentCalDate;
        while (start2.isBefore(end)) {
            cells.add(new CustomCalCel(start2.getDayOfMonth() + "").setDate(start2).setIsABodyCel(true).setColorCode("#000000"));
            start2 = start2.plusDays(1);
        }

        this.fragment.currentCalDate = currentCalDate;
        calendarViewAdapter.notifyDataSetChanged();
    }

}
