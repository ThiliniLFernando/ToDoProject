package com.smart.planner;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

public class FragmentReports extends Fragment {

    private PieChart pieChart;
    private BarChart weekBarChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports,container,false);
        initComponents(view);
        return view;
    }

    private void initComponents(View view) {
        pieChart = view.findViewById(R.id.pie_chart);

        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(85,"Completed"));
        pieEntries.add(new PieEntry(15,"Undone"));
        PieDataSet pieDataSet = new PieDataSet(pieEntries,"");
        PieData pieData = new PieData(pieDataSet);
        pieData.setValueTextSize(0);
        pieChart.setData(pieData);

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(getContext(),R.color.pieChart_completed));
        colors.add(ContextCompat.getColor(getContext(),R.color.pieChart_undone));
        pieDataSet.setColors(colors);

        pieChart.setHoleRadius(85f);
        pieChart.setDrawEntryLabels(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("85%");
        pieChart.setCenterTextColor(ContextCompat.getColor(getContext(),R.color.pieChart_completed));
        pieChart.setCenterTextSize(40f);
        pieChart.setDrawSlicesUnderHole(true);
        pieChart.invalidate();

        weekBarChart = view.findViewById(R.id.bar_chart);

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(5,1));
        barEntries.add(new BarEntry(10,10));
        barEntries.add(new BarEntry(15,4));
        barEntries.add(new BarEntry(20,2));
        barEntries.add(new BarEntry(25,3));
        barEntries.add(new BarEntry(30,5));
        barEntries.add(new BarEntry(35,6));
        BarDataSet barDataSet = new BarDataSet(barEntries,"");
        BarData barData = new BarData(barDataSet);
        barDataSet.setColor(ContextCompat.getColor(getContext(),R.color.weeklyBarChartColor));
        barData.setBarWidth(3);
        weekBarChart.setData(barData);
        weekBarChart.getDescription().setEnabled(false);
        weekBarChart.setFitBars(true);

//        ArrayList<LegendEntry> legendEntries = new ArrayList<>();
//        legendEntries.add(new LegendEntry("Mon",Legend.LegendForm.CIRCLE,Float.NaN,Float.NaN,null,ContextCompat.getColor(getContext(),R.color.weeklyBarChartColor)));
//        legendEntries.add(new LegendEntry("Tue",Legend.LegendForm.CIRCLE,Float.NaN,Float.NaN,null,ContextCompat.getColor(getContext(),R.color.weeklyBarChartColor)));
//        legendEntries.add(new LegendEntry("Wed",Legend.LegendForm.CIRCLE,Float.NaN,Float.NaN,null,ContextCompat.getColor(getContext(),R.color.weeklyBarChartColor)));
//        legendEntries.add(new LegendEntry("Thu",Legend.LegendForm.CIRCLE,Float.NaN,Float.NaN,null,ContextCompat.getColor(getContext(),R.color.weeklyBarChartColor)));
//        legendEntries.add(new LegendEntry("Fri",Legend.LegendForm.CIRCLE,Float.NaN,Float.NaN,null,ContextCompat.getColor(getContext(),R.color.weeklyBarChartColor)));
//        legendEntries.add(new LegendEntry("Sat",Legend.LegendForm.CIRCLE,Float.NaN,Float.NaN,null,ContextCompat.getColor(getContext(),R.color.weeklyBarChartColor)));
//        legendEntries.add(new LegendEntry("Sun",Legend.LegendForm.CIRCLE,Float.NaN,Float.NaN,null,ContextCompat.getColor(getContext(),R.color.weeklyBarChartColor)));

//        weekBarChart.getLegend().setCustom(legendEntries);
//        weekBarChart.getLegend().setWordWrapEnabled(true);
//        weekBarChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);

        weekBarChart.getAxisRight().setDrawGridLines(false);
        weekBarChart.getAxisLeft().setDrawGridLines(false);
        weekBarChart.getXAxis().setDrawGridLines(false);
        weekBarChart.invalidate();
    }
}
