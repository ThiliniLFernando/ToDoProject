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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smart.planner.Adapters.CalendarCel;
import com.smart.planner.Adapters.CalendarCel_Week;
import com.smart.planner.POJOs.CustomCalCel;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;

public class FragmentCalendar_Week extends Fragment {
    private TextView year, month, forward, backward;
    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
    private LocalDate currentCalDate;
    private ArrayList<CustomCalCel> cells;
    private CalendarCel_Week calendarViewAdapter;
    private CustomCalendarFragment fragment ;

    public FragmentCalendar_Week(){}

    FragmentCalendar_Week(CustomCalendarFragment fragment, LocalDate cuLocalDate){
        this.fragment = fragment;
        this.currentCalDate = cuLocalDate;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar_week, container, false);
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

        calendarViewAdapter = new CalendarCel_Week(cells, this, recyclerView, metrics.heightPixels, metrics.widthPixels, metrics.density);
        recyclerView.setAdapter(calendarViewAdapter);
        layoutManager = new GridLayoutManager(getContext(), 7, GridLayoutManager.HORIZONTAL, false);
        recyclerView.setHasFixedSize(true);

        DividerItemDecoration div1 = new DividerItemDecoration(getContext(), DividerItemDecoration.HORIZONTAL);
        div1.setDrawable(new ColorDrawable(Color.parseColor("#808080")));
        recyclerView.addItemDecoration(div1);

        DividerItemDecoration div2 = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        div2.setDrawable(new ColorDrawable(Color.parseColor("#808080")));
        recyclerView.addItemDecoration(div2);

        recyclerView.setLayoutManager(layoutManager);
        displayParticularWeek();

        backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentCalDate != null) {
                    layoutManager = new GridLayoutManager(getContext(), 7, GridLayoutManager.HORIZONTAL, false);
                    currentCalDate = currentCalDate.minusWeeks(1);
                    recyclerView.setLayoutManager(layoutManager);
                    displayParticularWeek();
                }
            }
        });

        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentCalDate != null) {
                    layoutManager = new GridLayoutManager(getContext(), 7, GridLayoutManager.HORIZONTAL, false);
                    currentCalDate = currentCalDate.plusWeeks(1);
                    recyclerView.setLayoutManager(layoutManager);
                    displayParticularWeek();
                }
            }
        });
    }

    private void displayParticularWeek() {
        cells.clear();
        ArrayList<LocalDate> dates = new ArrayList();
        year.setText(currentCalDate.getYear() + "");
        month.setText(currentCalDate.getMonth().name());

        cells.add(new CustomCalCel("Mon").setIsAHeadCel(true).setHasRandomHeight(false));
        cells.add(new CustomCalCel("Tue").setIsAHeadCel(true).setHasRandomHeight(false));
        cells.add(new CustomCalCel("Wed").setIsAHeadCel(true).setHasRandomHeight(false));
        cells.add(new CustomCalCel("Thu").setIsAHeadCel(true).setHasRandomHeight(false));
        cells.add(new CustomCalCel("Fri").setIsAHeadCel(true).setHasRandomHeight(false));
        cells.add(new CustomCalCel("Sat").setIsAHeadCel(true).setHasRandomHeight(false));
        cells.add(new CustomCalCel("Sun").setIsAHeadCel(true).setHasRandomHeight(false));

        LocalDate start = currentCalDate;
        int emptyDatesBefore = start.getDayOfWeek().getValue() - 1;
        LocalDate end = currentCalDate.plusDays(1);

        int emptyDatesAfter = 0;

        if (emptyDatesBefore > 0) {
            for (int i = emptyDatesBefore; i > 0; i--) {
                LocalDate lastMonthDate = start.minusDays(i);
                cells.add(new CustomCalCel(lastMonthDate.getDayOfMonth() + "").setDate(lastMonthDate).setIsABodyCel(true).setColorCode("#808080"));
                dates.add(lastMonthDate);
            }
        }

        cells.add(new CustomCalCel(currentCalDate.getDayOfMonth() + "").setDate(currentCalDate).setIsABodyCel(true).setColorCode("#808080"));
        dates.add(currentCalDate);

        if (cells.size() < 14) {
            emptyDatesAfter = 14 - cells.size();
        }

        if (emptyDatesAfter > 0) {
            for (int i = 0; i < emptyDatesAfter; i++) {
                LocalDate nextMonthDate = end.plusDays(i);
                cells.add(new CustomCalCel(nextMonthDate.getDayOfMonth() + "").setDate(nextMonthDate).setIsABodyCel(true).setColorCode("#808080"));
                dates.add(nextMonthDate);
            }
        }

        if (cells.size() == 14) {
            int i = 0;
            while (i <= 6) {
                cells.add(new CustomCalCel(dates.get(i).getDayOfMonth() + "").setDate(dates.get(i)).setIsAFooterCel(true));
                i++;
            }
        }

        this.fragment.currentCalDate = currentCalDate;
        calendarViewAdapter.notifyDataSetChanged();
    }

}
