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
import com.smart.planner.Adapters.CalendarCel_ADay;
import com.smart.planner.POJOs.CustomCalCel;

import java.time.LocalDate;
import java.util.ArrayList;

public class FragmentCalendar_ADay extends Fragment {

    private TextView year, month, forward, backward;
    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
    private LocalDate currentCalDate;
    private ArrayList<CustomCalCel> cells;
    private CalendarCel_ADay calendarViewAdapter;
    private CustomCalendarFragment fragment ;

    public FragmentCalendar_ADay(){}

    FragmentCalendar_ADay(CustomCalendarFragment fragment, LocalDate cuLocalDate){
        this.fragment = fragment;
        this.currentCalDate = cuLocalDate;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar_day, container, false);
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

        calendarViewAdapter = new CalendarCel_ADay(cells, this, recyclerView,metrics.heightPixels, metrics.widthPixels, metrics.density);
        recyclerView.setAdapter(calendarViewAdapter);
        layoutManager = new GridLayoutManager(getContext(), 1, GridLayoutManager.HORIZONTAL, false);
        recyclerView.setHasFixedSize(true);

        DividerItemDecoration div1 = new DividerItemDecoration(getContext(), DividerItemDecoration.HORIZONTAL);
        div1.setDrawable(new ColorDrawable(Color.parseColor("#808080")));
        recyclerView.addItemDecoration(div1);

        DividerItemDecoration div2 = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        div2.setDrawable(new ColorDrawable(Color.parseColor("#808080")));
        recyclerView.addItemDecoration(div2);

        recyclerView.setLayoutManager(layoutManager);
        displayParticularDay();

        backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentCalDate != null) {
                    layoutManager = new GridLayoutManager(getContext(), 1, GridLayoutManager.HORIZONTAL, false);
                    currentCalDate = currentCalDate.minusDays(1);
                    recyclerView.setLayoutManager(layoutManager);
                    displayParticularDay();
                }
            }
        });

        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentCalDate != null) {
                    layoutManager = new GridLayoutManager(getContext(), 1, GridLayoutManager.HORIZONTAL, false);
                    currentCalDate = currentCalDate.plusDays(1);
                    recyclerView.setLayoutManager(layoutManager);
                    displayParticularDay();
                }
            }
        });
    }

    private void displayParticularDay() {
        cells.clear();
        ArrayList<LocalDate> dates = new ArrayList();
        year.setText(currentCalDate.getYear() + "");
        month.setText(currentCalDate.getMonth().name());

        cells.add(new CustomCalCel(
                currentCalDate.getDayOfWeek().name().substring(0, 3)+" \n"+" \n "+currentCalDate.getDayOfMonth())
                        .setIsAHeadCel(true)
                        .setHasRandomHeight(false));
        cells.add(new CustomCalCel(
                currentCalDate.getDayOfMonth() + "")
                .setDate(currentCalDate)
                .setIsAFooterCel(true));

        this.fragment.currentCalDate = currentCalDate;
        calendarViewAdapter.notifyDataSetChanged();
    }

}
