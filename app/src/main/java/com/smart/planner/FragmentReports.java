package com.smart.planner;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
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

public class FragmentReports extends Fragment {

    private PieChart pieChart;
    private BarChart weekBarChart;

    private Calendar firstDate;
    private Calendar lastDate;
    private static final SimpleDateFormat checkingDateFormat = new SimpleDateFormat("yyyyMMdd");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);
        initComponents(view);
        return view;
    }

    private void initComponents(View view) {
        pieChart = view.findViewById(R.id.pie_chart);
        weekBarChart = view.findViewById(R.id.bar_chart);

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

        firstDate = Calendar.getInstance();
        firstDate.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        firstDate.set(Calendar.HOUR, 0);
        firstDate.set(Calendar.MINUTE, 0);
        firstDate.set(Calendar.SECOND, 0);
        System.out.println("First Date of the Week " + firstDate.getTime());

        lastDate = Calendar.getInstance();
        lastDate.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        lastDate.set(Calendar.HOUR, 23);
        lastDate.set(Calendar.MINUTE, 59);
        lastDate.set(Calendar.SECOND, 59);
        System.out.println("Last Date of the Week " + lastDate.getTime());
        swipeWeekStatistics(firstDate,lastDate);
    }

    public void swipeWeekStatistics(Calendar firstDate, Calendar lastDate){
        FirebaseFirestore.getInstance().collection("Users").document(Main.CURRENT_USER_KEY)
                .collection("Tasks")
                .whereGreaterThan("dueDate", firstDate)
                .whereLessThan("dueDate", lastDate)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        Map<String,int[]> barchartData = new HashMap<>();
                        Calendar current = firstDate;
                        for (int i = 1; i <= 7; i++){
                            current.add(Calendar.DATE,1);
                            barchartData.put(checkingDateFormat.format(current.getTime()),new int[]{0,0});
                        }
                        List<DocumentSnapshot> snapshots = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot snapshot : snapshots) {
                            Reminder reminder = snapshot.toObject(Reminder.class);
                            if (!reminder.isEvent()) {
                                Task task = snapshot.toObject(Task.class);
                                int[] array = barchartData.get(checkingDateFormat.format(task.getDueDate()));
                                if(array != null) {
                                    if (task.isCompleted()) {
                                        int done = array[0];
                                        done++;
                                        array[0] = done;
                                    } else {
                                        int undone = array[1];
                                        undone++;
                                        array[1] = undone;
                                    }
                                    barchartData.put(checkingDateFormat.format(task.getDueDate()),array);
                                }
                            }
                        }
                        drawWeeklyCompletion(barchartData);
                    }
                });
    }

    private void drawWeeklyCompletion(Map<String,int[]> barchartData) {
        System.out.println("Draw Weekly Completion Barchart");
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        float xAxis = 5;
//        for (int[] arr: barchartData.values()) {
//            System.out.println("Looping arr"+arr);
//            float total = arr[0]+arr[1];
//            float proportion = 0;
//            if(total > 0){
//                proportion = arr[0]/total * 100;
//            }
            //BigDecimal bg = new BigDecimal(proportion).setScale(1,RoundingMode.HALF_UP);
            barEntries.add(new BarEntry(5, 20));
            barEntries.add(new BarEntry(10, 12));
            barEntries.add(new BarEntry(15, 5));
            barEntries.add(new BarEntry(20, 100));
            barEntries.add(new BarEntry(25, 0));
            barEntries.add(new BarEntry(30, 9));
            barEntries.add(new BarEntry(35, 9));
            //xAxis += 5;
       // }
        System.out.println("Looping finished");
        BarDataSet barDataSet = new BarDataSet(barEntries, "");
        BarData barData = new BarData(barDataSet);
        barDataSet.setColor(ContextCompat.getColor(getContext(), R.color.weeklyBarChartColor));
        barData.setBarWidth(3);

        weekBarChart.setData(barData);
        weekBarChart.getDescription().setEnabled(false);
        weekBarChart.setFitBars(true);
        System.out.println("Bar Entries setted");

        ArrayList<LegendEntry> legendEntries = new ArrayList<>();
        legendEntries.add(new LegendEntry("Mon", Legend.LegendForm.CIRCLE, Float.NaN, Float.NaN, null, ContextCompat.getColor(getContext(), R.color.weeklyBarChartColor)));
        legendEntries.add(new LegendEntry("Tue", Legend.LegendForm.CIRCLE, Float.NaN, Float.NaN, null, ContextCompat.getColor(getContext(), R.color.weeklyBarChartColor)));
        legendEntries.add(new LegendEntry("Wed", Legend.LegendForm.CIRCLE, Float.NaN, Float.NaN, null, ContextCompat.getColor(getContext(), R.color.weeklyBarChartColor)));
        legendEntries.add(new LegendEntry("Thu", Legend.LegendForm.CIRCLE, Float.NaN, Float.NaN, null, ContextCompat.getColor(getContext(), R.color.weeklyBarChartColor)));
        legendEntries.add(new LegendEntry("Fri", Legend.LegendForm.CIRCLE, Float.NaN, Float.NaN, null, ContextCompat.getColor(getContext(), R.color.weeklyBarChartColor)));
        legendEntries.add(new LegendEntry("Sat", Legend.LegendForm.CIRCLE, Float.NaN, Float.NaN, null, ContextCompat.getColor(getContext(), R.color.weeklyBarChartColor)));
        legendEntries.add(new LegendEntry("Sun", Legend.LegendForm.CIRCLE, Float.NaN, Float.NaN, null, ContextCompat.getColor(getContext(), R.color.weeklyBarChartColor)));

        weekBarChart.getLegend().setCustom(legendEntries);
        weekBarChart.getLegend().setWordWrapEnabled(true);
        //weekBarChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        //weekBarChart.getLegend().setEnabled(false);
        System.out.println("Legend entries setted");
        //weekBarChart.getAxisRight().setDrawGridLines(false);
        //weekBarChart.getAxisLeft().setDrawGridLines(false);
        //weekBarChart.getXAxis().setDrawGridLines(false);
        System.out.println("Legend Entries Size "+legendEntries.size());
        System.out.println("Bar Entries Size "+barEntries.size());
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
