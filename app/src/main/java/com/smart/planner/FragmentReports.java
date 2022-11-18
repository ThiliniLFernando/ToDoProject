package com.smart.planner;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.core.utilities.Tree;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.smart.planner.POJOs.Reminder;
import com.smart.planner.POJOs.Task;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class FragmentReports extends Fragment {

    private PieChart pieChart;
    private BarChart weekBarChart;
    private TextView weekName;
    private ImageButton forward, backward;
    private TextView pendingTaskCount, completeTaskCount;

    private Calendar firstDate;
    private Calendar lastDate;
    private static final SimpleDateFormat checkingDateFormat = new SimpleDateFormat("yyyyMMdd");
    private static final SimpleDateFormat weekNameFormat = new SimpleDateFormat("dd/MM/YY");
    {
        firstDate = Calendar.getInstance();
        firstDate.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        lastDate = Calendar.getInstance();
        lastDate.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        System.out.println(firstDate.getTime() + " " + lastDate.getTime());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initComponents(view);
    }

    private void initComponents(View view) {
        pieChart = view.findViewById(R.id.pie_chart);
        weekBarChart = view.findViewById(R.id.bar_chart);
        forward = view.findViewById(R.id.forward_arrow);
        backward = view.findViewById(R.id.backward_arrow);
        weekName = view.findViewById(R.id.week_name);
        pendingTaskCount = view.findViewById(R.id.pending_tasks_count);
        completeTaskCount = view.findViewById(R.id.complete_tasks_count);

        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeBarChartData(1);
            }
        });
        backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeBarChartData(-1);
            }
        });

        drawPieChart();
        swipeWeekStatistics(firstDate);
        countCompletedTaskNPending();
    }

    private void countCompletedTaskNPending() {
        FirebaseFirestore.getInstance().collection("Users").document(Main.CURRENT_USER_KEY)
                .collection("Tasks").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshots, @Nullable FirebaseFirestoreException e) {
                        int totalTasks = 0;
                        int done = 0;
                        int undone = 0 ;
                        List<DocumentSnapshot> list = querySnapshots.getDocuments();
                        for (DocumentSnapshot d: list){
                           Reminder re = d.toObject(Reminder.class);
                           if(!re.isEvent()){
                            ++totalTasks;
                            Task t = d.toObject(Task.class);
                            if (t.isCompleted()){
                                ++done;
                            }else {
                                ++undone;
                            }
                           }
                        }
                        pendingTaskCount.setText(undone+"");
                        completeTaskCount.setText(done+"");
                    }
                });
    }

    private void changeBarChartData(int action) {
        String weekName = "";
        if (action == -1) {
            firstDate.add(Calendar.DATE, -7);
            lastDate.add(Calendar.DATE, -7);
        } else if (action == 1) {
            firstDate.add(Calendar.DATE, 7);
            lastDate.add(Calendar.DATE, 7);
        }
        weekName = weekNameFormat.format(firstDate.getTime())+" - "+weekNameFormat.format(lastDate.getTime());
        this.weekName.setText(weekName);
        swipeWeekStatistics(firstDate);
    }

    public void drawPieChart() {
        Calendar startDate = Calendar.getInstance();
        startDate.setTime(new Date());
        startDate.set(Calendar.HOUR, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);

        Calendar endDate = Calendar.getInstance();
        endDate.setTime(new Date());
        endDate.set(Calendar.HOUR, 23);
        endDate.set(Calendar.MINUTE, 59);
        endDate.set(Calendar.SECOND, 59);

        FirebaseFirestore.getInstance().collection("Users").document(Main.CURRENT_USER_KEY)
                .collection("Tasks")
                .whereGreaterThan("dueDate", startDate)
                .whereLessThan("dueDate", endDate)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        int done = 0;
                        int undone = 0;
                        List<DocumentSnapshot> snapshots = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot snapshot : snapshots) {
                            Reminder reminder = snapshot.toObject(Reminder.class);
                            if (!reminder.isEvent()) {
                                Task task = snapshot.toObject(Task.class);
                                if (task.isCompleted()) {
                                    done++;
                                } else {
                                    undone++;
                                }
                            }
                        }
                        int total = done + undone;
                        if (total == 0) {
                            done = 100;
                            total = done + undone;
                        }
                        int donePer = done * 100;
                        donePer = donePer / total;
                        BigDecimal donePer1 = new BigDecimal(donePer).setScale(0, RoundingMode.HALF_UP);
                        int undonePer = 100 - donePer1.intValue();
                        drawPieChart(donePer1.intValue(), undonePer);
                    }
                });
    }

    public void swipeWeekStatistics(Calendar firstDate) {
        //Toast.makeText(getContext(), "FDate "+firstDate.getTime()+" LDate "+lastDate.getTime(), Toast.LENGTH_SHORT).show();
        Query q = FirebaseFirestore.getInstance().collection("Users")
                .document(Main.CURRENT_USER_KEY)
                .collection("Tasks");
        q.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                Map<String, int[]> barchartData = new HashMap<>();
                Calendar current = Calendar.getInstance();
                current.setTime(firstDate.getTime());
                barchartData.put(checkingDateFormat.format(current.getTime()), new int[]{0, 0});
                for (int i = 1; i < 7; i++) {
                    current.add(Calendar.DATE, 1);
                    barchartData.put(checkingDateFormat.format(current.getTime()), new int[]{0, 0});
                }
                List<DocumentSnapshot> snapshots = queryDocumentSnapshots.getDocuments();
                for (DocumentSnapshot snapshot : snapshots) {
                    Reminder reminder = snapshot.toObject(Reminder.class);
                    if (!reminder.isEvent()) {
                        Task task = snapshot.toObject(Task.class);
                        int[] array = barchartData.get(checkingDateFormat.format(task.getDueDate()));
                        if (array != null) {
                            if (task.isCompleted()) {
                                int done = array[0];
                                done++;
                                array[0] = done;
                            } else {
                                int undone = array[1];
                                undone++;
                                array[1] = undone;
                            }
                            barchartData.put(checkingDateFormat.format(task.getDueDate()), array);
                        }
                    }
                }
                drawWeeklyCompletion(barchartData);
            }
        });
    }

    private void drawWeeklyCompletion(Map<String, int[]> barchartData) {
        weekBarChart.clear();
        String DAYS[] = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        float xAxis = 0f;
        TreeSet<String> sortedSet = new TreeSet<>(barchartData.keySet());
        String[] keys = sortedSet.toArray(new String[0]);
        for (int i = 0; i < 7; i++) {
            String key = keys[i];
            //Toast.makeText(getContext(), ""+i, Toast.LENGTH_SHORT).show();
            DAYS[i] = DAYS[i] +" "+key.substring(key.length() - 2) + "";
            int[] arr = barchartData.get(key);
            float total = arr[0] + arr[1];
            float proportion = 0;
            if (total > 0) {
                proportion = arr[0] / total * 100;
            }
            BigDecimal bg = new BigDecimal(proportion).setScale(1, RoundingMode.HALF_UP);
            barEntries.add(new BarEntry(xAxis, bg.floatValue(), key));
            xAxis += 3f;
        }
        BarDataSet barDataSet = new BarDataSet(barEntries, "Task Completion Percentage");
        BarData barData = new BarData(barDataSet);
        // error occured ContextCompact null error
        barDataSet.setColor(ContextCompat.getColor(getContext(), R.color.weeklyBarChartColor));
        barData.setBarWidth(2);

        weekBarChart.setData(barData);
        weekBarChart.getDescription().setEnabled(false);
        weekBarChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                int i = (int) value / 3;
                return DAYS[i];
            }
        });
        weekBarChart.setFitBars(true);
        weekBarChart.getAxisRight().setEnabled(false);
        weekBarChart.getAxisLeft().setDrawGridLines(true);
        weekBarChart.getAxisLeft().setAxisMaximum(110);
        weekBarChart.getXAxis().setDrawGridLines(true);
        weekBarChart.invalidate();
    }

    private void drawPieChart(int donePer, int undonePer) {
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(donePer, donePer + "%"));
        pieEntries.add(new PieEntry(undonePer, undonePer + "%"));
        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        PieData pieData = new PieData(pieDataSet);
        pieData.setValueTextSize(0);
        pieChart.setData(pieData);

        ArrayList<LegendEntry> legendEntries = new ArrayList<>();
        legendEntries.add(new LegendEntry("Done", Legend.LegendForm.SQUARE, 15f, Float.NaN, null, ContextCompat.getColor(getContext(), R.color.pieChart_completed)));
        legendEntries.add(new LegendEntry("Undone", Legend.LegendForm.SQUARE, 15f, Float.NaN, null, ContextCompat.getColor(getContext(), R.color.pieChart_undone)));
        pieChart.getLegend().setCustom(legendEntries);

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(this.getContext(), R.color.pieChart_completed));
        colors.add(ContextCompat.getColor(this.getContext(), R.color.pieChart_undone));
        pieDataSet.setColors(colors);

        pieChart.setDrawEntryLabels(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(false);
        pieChart.setDrawCenterText(false);
        pieChart.setDrawSlicesUnderHole(true);
        pieChart.invalidate();
    }
}
