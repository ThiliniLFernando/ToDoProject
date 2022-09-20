package com.smart.planner.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smart.planner.EventEditor;
import com.smart.planner.POJOs.Contact;
import com.smart.planner.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
    public List<Contact> cont ;
    private ArrayList<Contact> arrayList ;
    View vv ;
    EventEditor editor ;

    public ContactAdapter(List<Contact> items, EventEditor editor){
        this.cont = items;
        this.arrayList = new ArrayList<>();
        this.arrayList.addAll(cont);
        this.editor = editor;
    }

    @NonNull
    @Override
    public ContactAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.temp_contact_list_row,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ContactAdapter.ViewHolder holder, final int position) {
        Contact list = cont.get(position);
        String name = (list.getName());

        holder.title.setText(name);
        holder.phone.setText(list.getPhone());

        holder.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cont.remove(position);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return cont.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        public EditText title;
        public TextView phone;
        public LinearLayout contact_select_layout;
        public ImageView close ;

        public ViewHolder(View itemView){
            super(itemView);
            this.setIsRecyclable(false);
            title =  itemView.findViewById(R.id.name);
            phone =  itemView.findViewById(R.id.no);
            contact_select_layout = itemView.findViewById(R.id.contact_select_layout);
            close = itemView.findViewById(R.id.close);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
