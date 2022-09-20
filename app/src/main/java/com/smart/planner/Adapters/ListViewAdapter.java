package com.smart.planner.Adapters;

import android.graphics.Color;
import android.os.Bundle;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.smart.planner.DialogNewList;
import com.smart.planner.FragmentList;
import com.smart.planner.POJOs.List;
import com.smart.planner.R;
import java.util.ArrayList;

public class ListViewAdapter extends RecyclerView.Adapter<ListViewAdapter.ViewHolder> {
    private java.util.List<List> lists ;
    private FragmentList mList ;

    public ListViewAdapter(ArrayList<List> lists, FragmentList list) {
        this.lists = lists;
        this.mList = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView listName ;
        public LinearLayout linearLayout ;
        public TextView colorView ;

        public ViewHolder(View itemView) {
            super(itemView);
            listName = itemView.findViewById(R.id.list_name);
            linearLayout = itemView.findViewById(R.id.list_item_view_linear);
            colorView = itemView.findViewById(R.id.color_view);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.temp_list_item,parent,false);
        return new ViewHolder(view) ;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final List list = this.lists.get(position);
        holder.listName.setText(list.getListName());
        holder.colorView.setBackgroundColor(Color.parseColor(list.getListColor()));
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(mList.getContext(),holder.listName);
                popup.inflate(R.menu.list_view_popup_menu);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.popup_edit:
                                Bundle arguments = new Bundle();
                                arguments.putString("list_document_id",list.getDocumentId());
                                arguments.putString("list_name",list.getListName());
                                arguments.putString("list_color",list.getListColor());
                                DialogNewList dNewList = new DialogNewList();
                                dNewList.setArguments(arguments);
                                dNewList.show(mList.getChildFragmentManager(),"Update_List");
                                break;
                            case R.id.popup_delete:
                                mList.removeLists(new List[]{list});
                                break;
                            case R.id.popup_showItems:
                                break;
                        }
                        return false;
                    }
                });
                popup.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }
}