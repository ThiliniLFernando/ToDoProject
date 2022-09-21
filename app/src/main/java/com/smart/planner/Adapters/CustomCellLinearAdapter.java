package com.smart.planner.Adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.smart.planner.POJOs.Event;
import com.smart.planner.POJOs.Reminder;
import com.smart.planner.POJOs.Task;
import com.smart.planner.R;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class CustomCellLinearAdapter extends RecyclerView.Adapter<CustomCellLinearAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Reminder> taskItems;
    private int width;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

    public CustomCellLinearAdapter(ArrayList<Reminder> mList, int width){
        this.taskItems = mList;
        this.width = width;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private CardView cellLayout;
        private TextView cellData;
        private TextView cellTime;
        private ConstraintLayout constraintLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cellLayout = itemView.findViewById(R.id.parent_layout);
            cellData = itemView.findViewById(R.id.calendar_linear_text);
            cellTime = itemView.findViewById(R.id.task_time);
            constraintLayout = itemView.findViewById(R.id.cons_layout);
        }

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.temp_customcal_linear_item, parent, false);
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

            holder.constraintLayout.setBackground(gd);
            Event event = (Event) taskItems.get(position);
            holder.cellData.setText(event.getEventTitle());
            holder.cellTime.setText(timeFormat.format(event.getDueDate()));
        }else{
            Task task = (Task) taskItems.get(position);
            holder.cellData.setText(task.getTaskName());
            holder.cellTime.setText(timeFormat.format(task.getDueDate()));
        }
        holder.cellData.setWidth(width);
        holder.cellLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        holder.cellLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(mContext, "Long Clicked", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.taskItems.size();
    }
}
