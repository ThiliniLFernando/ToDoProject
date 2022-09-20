package com.smart.planner.Adapters;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.smart.planner.BottomSheetTaskView;
import com.smart.planner.DialogNewList;
import com.smart.planner.FragmentCalender;
import com.smart.planner.Main;
import com.smart.planner.POJOs.List;
import com.smart.planner.POJOs.Reminder;
import com.smart.planner.POJOs.Task;
import com.smart.planner.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import sun.bob.mcalendarview.views.ExpCalendarView;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {

    private ArrayList item_list;
    private FragmentCalender calender;
    private Context context;
    private CalendarAdapter thisAdapter;
    private ExpCalendarView calenderEvent ;

    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

    public CalendarAdapter(ArrayList<Reminder> items, FragmentCalender calender, ExpCalendarView calenderEvent) {
        this.item_list = items;
        this.calender = calender;
        this.thisAdapter = this;
        this.calenderEvent = calenderEvent ;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public MaterialCardView card;
        public ConstraintLayout layout;
        public TextView reminder_name;
        public TextView reminder_time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout);
            reminder_name = itemView.findViewById(R.id.reminder_name);
            reminder_time = itemView.findViewById(R.id.reminder_time);
            card = itemView.findViewById(R.id.layoutCard);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.temp_calendar_item, parent, false);
        context = view.getContext();
        return new CalendarAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Task task = (Task) this.item_list.get(position);
        holder.reminder_name.setText(((Task) task).getTaskName());
        holder.reminder_time.setText(timeFormat.format(((Task) task).getDueDate()));
        if (task.isCompleted()) {
            holder.reminder_name.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.reminder_name.setPaintFlags(Paint.CURSOR_AFTER);
        }
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                PopupMenu popup = new PopupMenu(view.getContext(), holder.reminder_name);
                popup.inflate(R.menu.task_view_popup_menu);
                if (task.isCompleted()) {
                    popup.getMenu().getItem(0).setTitle(R.string.popup_incomplete);
                } else {
                    popup.getMenu().getItem(0).setTitle(R.string.popup_complete);
                }
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.popup_complete:
                                setTaskIsComplete(task);
                                break;
                            case R.id.popup_edit:
                                Bundle arguments = new Bundle();
                                arguments.putSerializable("taskObj", task);
                                BottomSheetTaskView bottomView = new BottomSheetTaskView();
                                bottomView.setArguments(arguments);
                                bottomView.show(calender.getChildFragmentManager(), "bottom_task_fragment");
                                break;
                            case R.id.popup_delete:
                                removeTask(task);
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
        return item_list.size();
    }

    private void setTaskIsComplete(Task task) {
        Task newTask = new Task(task.getTaskName(), task.getListName());
        newTask.setDueDate(task.getDueDate());
        newTask.setDocumentId(task.getDocumentId());
        newTask.setPriority(task.getPriority());
        newTask.setReminders(task.getReminders());
        newTask.setRepeatMethod(task.getRepeatMethod());

        if (task.isCompleted()) {
            newTask.setCompleted(false);
        } else {
            newTask.setCompleted(true);
        }

        FirebaseFirestore.getInstance()
                .collection("Users").document(Main.CURRENT_USER_KEY)
                .collection("Tasks").document(task.getDocumentId())
                .set(newTask);

        refreshSelectedDateTasks(task.getDueDate());
    }

    private void refreshSelectedDateTasks(Date dueDate) {
        final Calendar dueCal = Calendar.getInstance();
        dueCal.setTime(dueDate);
        FirebaseFirestore.getInstance().collection("Users").document(Main.CURRENT_USER_KEY).collection("Tasks")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        item_list.clear();
                        for (DocumentSnapshot document : snapshots.getDocuments()) {
                            Task task = document.toObject(Task.class);
                            task.setDocumentId(document.getId());

                            Calendar cal = Calendar.getInstance();
                            cal.setTime(task.getDueDate());

                            cal.set(Calendar.HOUR_OF_DAY, dueCal.get(Calendar.HOUR_OF_DAY));
                            cal.set(Calendar.MINUTE, dueCal.get(Calendar.MINUTE));
                            cal.set(Calendar.SECOND, dueCal.get(Calendar.SECOND));

                            if (dueCal.compareTo(cal) == 0) {
                                item_list.add(task);
                            }

                        }
                        thisAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void removeTask(Task task) {
        FirebaseFirestore.getInstance()
                .collection("Users").document(Main.CURRENT_USER_KEY)
                .collection("Tasks").document(task.getDocumentId()).delete();

        calenderEvent.getMarkedDates().removeAdd();
        refreshSelectedDateTasks(task.getDueDate());
    }

}
