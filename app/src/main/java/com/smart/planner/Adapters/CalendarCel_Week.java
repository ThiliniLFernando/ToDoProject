package com.smart.planner.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.smart.planner.Main;
import com.smart.planner.POJOs.CustomCalCel;
import com.smart.planner.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarCel_Week extends RecyclerView.Adapter<CalendarCel_Week.ViewHolder> {

    private static String[] colorArray = new String[]{"#FAA990", "#8cff32", "#F44701", "#3CBEFC", "#fdff32"};
    private static Context mContext;
    private ArrayList<CustomCalCel> cells;
    private Fragment fragment;
    private RecyclerView calBody;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    private int heightPixel;
    private static int widthPixel;
    private int density;

    public CalendarCel_Week(ArrayList<CustomCalCel> cells, Fragment fragment, RecyclerView calBody, Spinner calDropdown, int height, int width, float density) {
        this.cells = cells;
        this.fragment = fragment;
        this.calBody = calBody;
        this.heightPixel = height;
        this.widthPixel = width;
        this.density = (int) density;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout cellLayout;
        private TextView cellDate;
        private LinearLayout colorLinear;
        private RecyclerView recyclerView;
        private RecyclerView.LayoutManager recycleViewLayoutManager;
        private CustomCellLinearAdapter adapter;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cellLayout = itemView.findViewById(R.id.calendar_cell_layout);
            cellDate = itemView.findViewById(R.id.cell_date);
            colorLinear = itemView.findViewById(R.id.color_linear);
            recyclerView = itemView.findViewById(R.id.customCal_Recycler);
        }

    }

    @NonNull
    @Override
    public CalendarCel_Week.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.temp_calendar_cell, parent, false);
        mContext = view.getContext();
        return new CalendarCel_Week.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarCel_Week.ViewHolder holder, int position) {
        final CustomCalCel cel = (CustomCalCel) this.cells.get(position);
        holder.colorLinear.removeAllViews();
        ViewGroup.LayoutParams lp1 = holder.cellLayout.getLayoutParams();
        lp1.width = 150;
        holder.cellLayout.setLayoutParams(lp1);
        holder.cellDate.setVisibility(View.VISIBLE);
        holder.recyclerView.setVisibility(View.GONE);
        holder.colorLinear.setVisibility(View.GONE);
        if (cel.isHeadCel()) {
            holder.cellDate.setText(cel.getText());
        } else if (cel.isBodyCel()) {
            holder.colorLinear.setVisibility(View.VISIBLE);
            holder.colorLinear.setOrientation(LinearLayout.HORIZONTAL);
            holder.cellDate.setText(cel.getText());
            holder.cellDate.setTextColor(Color.parseColor(cel.getColorCode()));
            createCalendarEvents(holder, cel);
        } else if (cel.isFooterCel()) {
            ArrayList<String> data = new ArrayList<>();
            holder.recyclerView.setVisibility(View.VISIBLE);
            holder.colorLinear.setOrientation(LinearLayout.HORIZONTAL);
            holder.cellDate.setVisibility(View.GONE);
            holder.adapter = new CustomCellLinearAdapter(data,(CalendarCel_Week.widthPixel-400));
            holder.recycleViewLayoutManager = new LinearLayoutManager(CalendarCel_Week.mContext);
            holder.recyclerView.setLayoutManager(holder.recycleViewLayoutManager);
            holder.recyclerView.setAdapter(holder.adapter);
            ViewGroup.LayoutParams lp = holder.cellLayout.getLayoutParams();
            lp.width = ((widthPixel) - 300);
            holder.cellLayout.setLayoutParams(lp);
            holder.cellLayout.setClickable(false);
            createVerticalCalendarEvents(holder, cel, (widthPixel - 300),data);
        }
    }

    private void createVerticalCalendarEvents(final CalendarCel_Week.ViewHolder holder, CustomCalCel cell, final int width,ArrayList<String> data) {
        try {
            Date startDate = format.parse(cell.getDate().toString() + "");
            Date endDate = format.parse(cell.getDate().toString() + "");
            Calendar en = Calendar.getInstance();
            en.setTime(endDate);
            en.set(Calendar.HOUR, 23);
            en.set(Calendar.MINUTE, 59);
            en.set(Calendar.SECOND, 59);
            en.set(Calendar.MILLISECOND, 59);
            endDate = en.getTime();
            Query q = FirebaseFirestore.getInstance().collection("Users")
                    .document(Main.CURRENT_USER_KEY)
                    .collection("Tasks")
                    .whereGreaterThan("dueDate", startDate)
                    .whereLessThan("dueDate", endDate);
            q.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> list = task.getResult().getDocuments();
                        if (list.size() > 0) {
                            int i = 0;
                            for (DocumentSnapshot snapshot : list) {
                                com.smart.planner.POJOs.Task t = snapshot.toObject(com.smart.planner.POJOs.Task.class);
                                data.add(t.getTaskName());
                            }
                            holder.adapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return cells.size();
    }

    private void createCalendarEvents(final CalendarCel_Week.ViewHolder holder, CustomCalCel cell) {
        try {
            Date startDate = format.parse(cell.getDate().toString() + "");
            Date endDate = format.parse(cell.getDate().toString() + "");
            Calendar en = Calendar.getInstance();
            en.setTime(endDate);
            en.set(Calendar.HOUR, 23);
            en.set(Calendar.MINUTE, 59);
            en.set(Calendar.SECOND, 59);
            en.set(Calendar.MILLISECOND, 59);
            endDate = en.getTime();
            Query q = FirebaseFirestore.getInstance().collection("Users")
                    .document(Main.CURRENT_USER_KEY)
                    .collection("Tasks")
                    .whereGreaterThan("dueDate", startDate)
                    .whereLessThan("dueDate", endDate);
            q.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> list = task.getResult().getDocuments();
                        if (list.size() > 0) {
                            int i = 0;
                            for (DocumentSnapshot snapshot : list) {
                                if (i <= 4) {
                                    TextView tv = new TextView(mContext);
                                    tv.setWidth(20);
                                    tv.setHeight(20);

                                    GradientDrawable gd = new GradientDrawable();
                                    gd.setShape(GradientDrawable.OVAL);
                                    gd.setColor(Color.parseColor(colorArray[i]));

                                    tv.setBackground(gd);
                                    holder.colorLinear.addView(tv);
                                    i++;
                                }
                            }
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
