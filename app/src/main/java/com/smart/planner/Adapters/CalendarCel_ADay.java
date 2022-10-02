package com.smart.planner.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.smart.planner.Main;
import com.smart.planner.POJOs.CustomCalCel;
import com.smart.planner.POJOs.Event;
import com.smart.planner.POJOs.Reminder;
import com.smart.planner.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarCel_ADay extends RecyclerView.Adapter<CalendarCel_ADay.ViewHolder> {

    private ArrayList<CustomCalCel> cells;
    private Fragment fragment;
    private RecyclerView recyclerView;
    private int heightPixel;
    private static int widthPixel;
    private float density;
    private Context mContext;

    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    public CalendarCel_ADay(ArrayList<CustomCalCel> cells, Fragment fragment, RecyclerView calBody, int height, int width, float density) {
        this.cells = cells;
        this.fragment = fragment;
        this.recyclerView = calBody;
        this.heightPixel = height;
        widthPixel = width;
        this.density = density;
    }

    @NonNull
    @Override
    public CalendarCel_ADay.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.temp_calendar_cell, parent, false);
        mContext = view.getContext();
        return new CalendarCel_ADay.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarCel_ADay.ViewHolder holder, int position) {
        final CustomCalCel cel = (CustomCalCel) this.cells.get(position);
        holder.colorLinear.removeAllViews();
        ViewGroup.LayoutParams lp1 = holder.cellLayout.getLayoutParams();
        lp1.width = 150;
        lp1.height = heightPixel;
        holder.cellLayout.setLayoutParams(lp1);
        holder.cellDate.setVisibility(View.VISIBLE);
        holder.recyclerView.setVisibility(View.GONE);
        holder.colorLinear.setVisibility(View.GONE);
        if (cel.isHeadCel()) {
            holder.cellDate.setText(cel.getText());
        } else if (cel.isFooterCel()) {
            ArrayList<Reminder> data = new ArrayList<>();
            holder.recyclerView.setVisibility(View.VISIBLE);
            holder.colorLinear.setOrientation(LinearLayout.HORIZONTAL);
            holder.cellDate.setVisibility(View.GONE);
            holder.adapter = new CustomCellLinearAdapter_ADay(fragment,data, (CalendarCel_ADay.widthPixel - 400));
            holder.recycleViewLayoutManager = new LinearLayoutManager(mContext);
            holder.recyclerView.setLayoutManager(holder.recycleViewLayoutManager);
            holder.recyclerView.setAdapter(holder.adapter);
            ViewGroup.LayoutParams lp = holder.cellLayout.getLayoutParams();
            lp.width = ((widthPixel) - 150);
            holder.cellLayout.setLayoutParams(lp);
            holder.cellLayout.setClickable(false);
            createADayEvent(holder, cel, data);
        }
    }

    private void createADayEvent(final CalendarCel_ADay.ViewHolder holder, CustomCalCel cell, ArrayList<Reminder> data) {
        System.out.println("A Day Holder "+holder);
        Date startDate = null;
        try {
            startDate = format.parse(cell.getDate().toString() + "");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar st = Calendar.getInstance();
        st.setTime(startDate);
        st.set(Calendar.DATE, st.get(Calendar.DATE) - 1);
        st.set(Calendar.HOUR, 23);
        st.set(Calendar.MINUTE, 59);
        st.set(Calendar.SECOND, 59);
        st.set(Calendar.MILLISECOND, 59);
        startDate = st.getTime();
        Date endDate = null;
        try {
            endDate = format.parse(cell.getDate().toString() + "");
        } catch (ParseException e) {
            e.printStackTrace();
        }
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
        q.addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                data.clear();
                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                if (list.size() > 0) {
                    Toast.makeText(mContext, "In the Method", Toast.LENGTH_SHORT).show();
                    for (DocumentSnapshot snapshot : list) {
                        Reminder t = snapshot.toObject(Reminder.class);
                        if (t.isEvent()) {
                            t = snapshot.toObject(Event.class);
                        } else {
                            t = snapshot.toObject(com.smart.planner.POJOs.Task.class);
                        }
                        t.setDocumentId(snapshot.getId());
                        data.add(t);
                    }
                    holder.adapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return cells.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout cellLayout;
        private TextView cellDate;
        private LinearLayout colorLinear;
        private RecyclerView recyclerView;
        private RecyclerView.LayoutManager recycleViewLayoutManager;
        private CustomCellLinearAdapter_ADay adapter;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cellLayout = itemView.findViewById(R.id.calendar_cell_layout);
            cellDate = itemView.findViewById(R.id.cell_date);
            colorLinear = itemView.findViewById(R.id.color_linear);
            recyclerView = itemView.findViewById(R.id.customCal_Recycler);
        }

    }
}
