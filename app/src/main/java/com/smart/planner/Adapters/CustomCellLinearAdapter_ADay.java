package com.smart.planner.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.smart.planner.BottomSheetTaskView;
import com.smart.planner.FocusActivity;
import com.smart.planner.POJOs.Event;
import com.smart.planner.POJOs.Reminder;
import com.smart.planner.POJOs.Task;
import com.smart.planner.R;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class CustomCellLinearAdapter_ADay extends RecyclerView.Adapter<CustomCellLinearAdapter_ADay.ViewHolder> {

    private Fragment calendarFragment;
    private Context mContext;
    private ArrayList<Reminder> taskItems;
    private int width;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

    public CustomCellLinearAdapter_ADay(Fragment calendarFragment,ArrayList<Reminder> mList, int width){
        this.taskItems = mList;
        this.width = width;
        this.calendarFragment = calendarFragment;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private CardView cellLayout;
        private TextView cellData;
        private TextView cellTime;
        private LinearLayout linearLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cellLayout = itemView.findViewById(R.id.parent_layout);
            cellData = itemView.findViewById(R.id.calendar_linear_text);
            cellTime = itemView.findViewById(R.id.task_time);
            linearLayout = itemView.findViewById(R.id.cons_layout);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.temp_customcal_linear_item_day, parent, false);
        mContext = view.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reminder reminder = (Reminder) taskItems.get(position);
        if (reminder.isEvent()){
            GradientDrawable gd = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[] {0xFFB4F8C8,0xFFA0E7E5});
            gd.setCornerRadius(15f);
            gd.setStroke(3,mContext.getColor(R.color.gray));

            holder.linearLayout.setBackground(gd);
            Event event = (Event) taskItems.get(position);
            holder.cellData.setText(event.getEventTitle());
            holder.cellTime.setText(timeFormat.format(event.getDueDate()));
        }else{
            Task task = (Task) taskItems.get(position);
            holder.cellData.setText(task.getTaskName());
            holder.cellTime.setText(timeFormat.format(task.getDueDate()));

            holder.cellLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Intent intent = new Intent(mContext, FocusActivity.class);
                    intent.putExtra("taskObj", new Gson().toJson(task));
                    mContext.startActivity(intent);
                    return false;
                }
            });

            holder.cellLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle arguments = new Bundle();
                    arguments.putSerializable("taskObj", task);
                    BottomSheetTaskView bottomView = new BottomSheetTaskView();
                    bottomView.setArguments(arguments);
                    bottomView.show(calendarFragment.getChildFragmentManager(), "bottom_task_fragment");
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return this.taskItems.size();
    }
}
