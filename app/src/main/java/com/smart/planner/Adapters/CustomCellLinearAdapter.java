package com.smart.planner.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.smart.planner.R;

import java.util.ArrayList;

public class CustomCellLinearAdapter extends RecyclerView.Adapter<CustomCellLinearAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<String> taskItems;
    private int width;

    public CustomCellLinearAdapter(ArrayList<String> mList,int width){
        this.taskItems = mList;
        this.width = width;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout cellLayout;
        private TextView cellData;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cellLayout = itemView.findViewById(R.id.parent_layout);
            cellData = itemView.findViewById(R.id.calendar_linear_text);
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
        String reminder = taskItems.get(position);
        holder.cellData.setWidth(width);
        holder.cellData.setText(reminder);
    }

    @Override
    public int getItemCount() {
        return this.taskItems.size();
    }
}
