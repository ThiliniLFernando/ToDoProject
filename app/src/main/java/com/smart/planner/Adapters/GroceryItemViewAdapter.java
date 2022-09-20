package com.smart.planner.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smart.planner.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class GroceryItemViewAdapter extends RecyclerView.Adapter<GroceryItemViewAdapter.ViewHolder> {

    private List<String> mGroceryList ;
    public GroceryItemViewAdapter(List<String> list){
        mGroceryList = list ;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout mLayout ;
        public TextView mGroceryTitle ;
        public TextView mItemClose ;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mLayout = itemView.findViewById(R.id.item_layout);
            mGroceryTitle = itemView.findViewById(R.id.item_title);
            mItemClose = itemView.findViewById(R.id.item_close);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.temp_location_reminder_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
        holder.mGroceryTitle.setText(mGroceryList.get(i));
        holder.mItemClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeGroceryItem(i);
            }
        });
    }

    public void removeGroceryItem(int position){
        mGroceryList.remove(position);
        notifyDataSetChanged();
    }

    public void addGroceryItem(String item){
        mGroceryList.add(item);
        notifyDataSetChanged();
    }

    public void clearGroceryList(){
        mGroceryList.clear();
        notifyDataSetChanged();
    }

    public List<String> getGroceryList(){
        return mGroceryList;
    }

    @Override
    public int getItemCount() {
        return mGroceryList.size();
    }

}
