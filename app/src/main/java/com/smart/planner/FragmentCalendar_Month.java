package com.smart.planner;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smart.planner.Adapters.CalendarCel;
import com.smart.planner.POJOs.CustomCalCel;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;

public class FragmentCalendar_Month extends Fragment {

    private TextView year, month, forward, backward;
    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
    private LocalDate currentCalDate;
    private ArrayList<CustomCalCel> cells;
    private CalendarCel calendarViewAdapter;
    private CustomCalendarFragment fragment ;

    public  FragmentCalendar_Month(){}

    public FragmentCalendar_Month(CustomCalendarFragment fragment,LocalDate currentCalDate){
        this.fragment = fragment;
        this.currentCalDate = currentCalDate;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar_month, container, false);
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
        layoutManager = new GridLayoutManager(getContext(), 7);
        recyclerView.setHasFixedSize(true);

        DividerItemDecoration div1 = new DividerItemDecoration(getContext(), DividerItemDecoration.HORIZONTAL);
        div1.setDrawable(new ColorDrawable(Color.parseColor("#808080")));
        recyclerView.addItemDecoration(div1);

        DividerItemDecoration div2 = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        div2.setDrawable(new ColorDrawable(Color.parseColor("#808080")));
        recyclerView.addItemDecoration(div2);

        recyclerView.setLayoutManager(layoutManager);
        displayParticularMonth();

        backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentCalDate != null) {
                    layoutManager = new GridLayoutManager(getContext(), 7);
                    currentCalDate = currentCalDate.minusMonths(1);
                    recyclerView.setLayoutManager(layoutManager);
                    displayParticularMonth();
                }
            }
        });

        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentCalDate != null) {
                    layoutManager = new GridLayoutManager(getContext(), 7);
                    currentCalDate = currentCalDate.plusMonths(1);
                    recyclerView.setLayoutManager(layoutManager);
                    displayParticularMonth();
                }
            }
        });
    }

    private void displayParticularMonth() {
        cells.clear();
        ArrayList<LocalDate> dates = new ArrayList();
        year.setText(currentCalDate.getYear() + "");
        month.setText(currentCalDate.getMonth().name());

        cells.add(new CustomCalCel("Mon").setIsAHeadCel(true).setHasRandomHeight(true));
        cells.add(new CustomCalCel("Tue").setIsAHeadCel(true).setHasRandomHeight(true));
        cells.add(new CustomCalCel("Wed").setIsAHeadCel(true).setHasRandomHeight(true));
        cells.add(new CustomCalCel("Thu").setIsAHeadCel(true).setHasRandomHeight(true));
        cells.add(new CustomCalCel("Fri").setIsAHeadCel(true).setHasRandomHeight(true));
        cells.add(new CustomCalCel("Sat").setIsAHeadCel(true).setHasRandomHeight(true));
        cells.add(new CustomCalCel("Sun").setIsAHeadCel(true).setHasRandomHeight(true));

        LocalDate start = currentCalDate;
        start = start.withDayOfMonth(1);

        int emptyDatesBefore = start.getDayOfWeek().getValue() - 1; // tuesday --> 1
        LocalDate end = start;
        end = start.plusMonths(1);

        int emptyDatesAfter = 0; // monday --> 6

        if (emptyDatesBefore > 0) {
            for (int i = emptyDatesBefore; i > 0; i--) {
                LocalDate lastMonthDate = start.minusDays(i);
                cells.add(new CustomCalCel(lastMonthDate.getDayOfMonth() + "").setDate(lastMonthDate).setIsABodyCel(true).setColorCode("#808080"));
            }
        }

        while (start.isBefore(end)) {
            if (start.isEqual(LocalDate.now())) {
                cells.add(new CustomCalCel(start.getDayOfMonth() + "").setDate(start).setIsABodyCel(true).setColorCode("#808080"));
            } else {
                cells.add(new CustomCalCel(start.getDayOfMonth() + "").setDate(start).setIsABodyCel(true).setColorCode("#000000"));
            }
            start = start.plusDays(1);
        }

        if (cells.size() < 49) {
            emptyDatesAfter = 49 - cells.size();
        }

        if (emptyDatesAfter > 0) {
            for (int i = 0; i < emptyDatesAfter; i++) {
                LocalDate nextMonthDate = end.plusDays(i);
                cells.add(new CustomCalCel(nextMonthDate.getDayOfMonth() + "").setDate(nextMonthDate).setIsABodyCel(true).setColorCode("#808080"));
            }
        }

        this.fragment.currentCalDate = currentCalDate;
        calendarViewAdapter.notifyDataSetChanged();
    }
}
