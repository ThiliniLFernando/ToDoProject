package com.smart.planner.Adapters;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.smart.planner.BottomSheetTaskView;
import com.smart.planner.EventEditor;
import com.smart.planner.FocusActivity;
import com.smart.planner.FragmentTasks;
import com.smart.planner.Main;
import com.smart.planner.POJOs.Event;
import com.smart.planner.POJOs.Reminder;
import com.smart.planner.POJOs.Task;
import com.smart.planner.R;
import com.smart.planner.TaskEditor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.ColorUtils;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

public class TaskViewSection extends StatelessSection {
    FragmentTasks mTaskFragment;
    List<Reminder> itemList;
    List<Reminder> itemListClone;
    String headerTitle;
    RecyclerView mTaskRecyclerView;
    SectionedRecyclerViewAdapter sectionAdapter;
    private SharedPreferences preferences;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy MMM dd");
    private static SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

    public TaskViewSection(RecyclerView taskRecyclerView,FragmentTasks fragmentTasks, String title, ArrayList<Reminder> tasks,SectionedRecyclerViewAdapter sa) {
        super(SectionParameters.builder()
                .headerResourceId(R.layout.temp_tasks_section_header)
                .itemResourceId(R.layout.temp_task_item1)
                .build());
        mTaskRecyclerView = taskRecyclerView;
        mTaskFragment = fragmentTasks;
        itemList = tasks;
        itemListClone = new ArrayList<>(itemList);
        headerTitle = title;
        sectionAdapter = sa;
        preferences = PreferenceManager.getDefaultSharedPreferences(mTaskFragment.getContext().getApplicationContext());
    }

    @Override
    public int getContentItemsTotal() {
        return itemList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView parentView;
        private TextView inviteeCount,taskDate,eventNote,eventLoc,eventDash, eventDate, eventStartTime, eventEndTime, eventTitle;
        private ImageView eventIcon;
        private MaterialButton complete,done,cancelled;
        private LinearLayout l1,l2 ;

        public TaskViewHolder(View itemView) {
            super(itemView);
            parentView = itemView.findViewById(R.id.parent_view);
            eventStartTime = itemView.findViewById(R.id.eventStartTime);
            eventDash = itemView.findViewById(R.id.event_time_dash);
            eventEndTime = itemView.findViewById(R.id.eventEndTime);
            eventIcon = itemView.findViewById(R.id.eventImage);
            eventTitle = itemView.findViewById(R.id.eventName);
            eventDate = itemView.findViewById(R.id.eventDate);
            eventLoc = itemView.findViewById(R.id.event_loc);
            eventNote = itemView.findViewById(R.id.event_note);
            complete = itemView.findViewById(R.id.complete);
            done = itemView.findViewById(R.id.done);
            cancelled = itemView.findViewById(R.id.cancelled);
            taskDate = itemView.findViewById(R.id.taskDate);
            inviteeCount = itemView.findViewById(R.id.invitee_count);
            l2 = itemView.findViewById(R.id.linear2);
            l1 = itemView.findViewById(R.id.linear1);
        }
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        final TaskViewHolder itemHolder = (TaskViewHolder) holder;
        Reminder re = this.itemList.get(position);
        if (!re.isEvent()) {
            itemHolder.complete.setVisibility(View.VISIBLE);
            itemHolder.eventDash.setVisibility(View.GONE);
            itemHolder.eventEndTime.setVisibility(View.GONE);
            itemHolder.eventNote.setVisibility(View.GONE);
            itemHolder.eventLoc.setVisibility(View.GONE);
            itemHolder.eventIcon.setVisibility(View.GONE);
            itemHolder.inviteeCount.setVisibility(View.GONE);
            itemHolder.l2.setVisibility(View.VISIBLE);
            itemHolder.eventDate.setVisibility(View.GONE);
            itemHolder.done.setVisibility(View.GONE);
            itemHolder.cancelled.setVisibility(View.GONE);
            itemHolder.eventStartTime.setCompoundDrawables(null,null,null,null);
            if (!preferences.getBoolean("isDarkTheme",false)) {
                itemHolder.eventTitle.setTextColor(Color.BLACK);
            }else {
                itemHolder.eventTitle.setTextColor(Color.WHITE);
            }
            final Task task = (Task) this.itemList.get(position);
            itemHolder.eventStartTime.setText(timeFormat.format(task.getDueDate()));
            itemHolder.taskDate.setText(dateFormat.format(task.getDueDate()));
            itemHolder.eventTitle.setText(task.getTaskName());

            if (task.isCompleted()) {
                itemHolder.eventTitle.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                itemHolder.complete.setText("INCOMPLETE");
            } else {
                itemHolder.eventTitle.setPaintFlags(Paint.CURSOR_AFTER);
                itemHolder.complete.setText("COMPLETE");
            }

            itemHolder.parentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle arguments = new Bundle();
                    arguments.putSerializable("taskObj", task);
                    BottomSheetTaskView bottomView = new BottomSheetTaskView();
                    bottomView.setArguments(arguments);
                    bottomView.show(mTaskFragment.getChildFragmentManager(), "bottom_task_fragment");
                }
            });

            itemHolder.parentView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Intent intent = new Intent(mTaskFragment.getContext(), FocusActivity.class);
                    intent.putExtra("taskObj", new Gson().toJson(task));
                    mTaskFragment.startActivity(intent);
                    return false;
                }
            });

            itemHolder.complete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setTaskIsComplete(task);
                }
            });

            if (headerTitle.equals("Today") | headerTitle.equals("Tomorrow")) {
                itemHolder.taskDate.setVisibility(View.GONE);
            }
        }else{
            itemHolder.l2.setVisibility(View.GONE);
            itemHolder.complete.setVisibility(View.GONE);

            itemHolder.eventDash.setVisibility(View.VISIBLE);
            itemHolder.eventEndTime.setVisibility(View.VISIBLE);
            itemHolder.eventNote.setVisibility(View.VISIBLE);
            itemHolder.eventLoc.setVisibility(View.VISIBLE);
            itemHolder.eventIcon.setVisibility(View.VISIBLE);
            itemHolder.inviteeCount.setVisibility(View.VISIBLE);
            itemHolder.eventDate.setVisibility(View.VISIBLE);
            itemHolder.done.setVisibility(View.VISIBLE);
            itemHolder.cancelled.setVisibility(View.VISIBLE);

            final Event event = (Event) this.itemList.get(position);
            int darkColor = ColorUtils.blendARGB(Color.parseColor(event.getColorCode()),Color.BLACK,0.4f);
            int lightColor = ColorUtils.blendARGB(Color.parseColor(event.getColorCode()),Color.WHITE,0.4f);
            itemHolder.eventTitle.setText(event.getEventTitle());
            itemHolder.eventDate.setText(dateFormat.format(event.getDueDate()));
            itemHolder.eventStartTime.setText(timeFormat.format(event.getStartTime()));
            itemHolder.eventEndTime.setText(timeFormat.format(event.getEndTime()));
            if (!preferences.getBoolean("isDarkTheme",false)) {
                itemHolder.eventTitle.setTextColor(darkColor);
                itemHolder.eventIcon.setColorFilter(darkColor);
                itemHolder.eventIcon.setBackgroundColor(lightColor);
            }else {
                itemHolder.eventTitle.setTextColor(lightColor);
                itemHolder.eventIcon.setColorFilter(lightColor);
                itemHolder.eventIcon.setBackgroundColor(darkColor);
            }
            itemHolder.eventNote.setText(event.getNote());
            itemHolder.inviteeCount.setText(event.getInvitees().size()+" Invitees");
            if (event.getLocation() != null && !event.getLocation().trim().equals("")){
                itemHolder.eventLoc.setVisibility(View.VISIBLE);
                itemHolder.eventLoc.setText(event.getLocation());
            }else{
                itemHolder.eventLoc.setVisibility(View.GONE);
            }

            itemHolder.parentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(mTaskFragment.getContext(), EventEditor.class);
                    i.putExtra("EventId",event.getDocumentId());
                    mTaskFragment.getContext().startActivity(i);
                }
            });
        }

    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView header;
        private final ConstraintLayout headerLayout;
        private final TextView expandBtn, taskCount;

        public HeaderViewHolder(View headerView) {
            super(headerView);
            header = headerView.findViewById(R.id.section_header);
            headerLayout = headerView.findViewById(R.id.task_section_header_layout);
            expandBtn = headerView.findViewById(R.id.expand_btn);
            taskCount = headerView.findViewById(R.id.task_count);
        }
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
        headerViewHolder.header.setText(headerTitle);
        headerViewHolder.taskCount.setText(itemList.size() + "");
    }

    private void setTaskIsComplete(Task task) {
        try {
            Task newTask = (Task) task.clone();

            if (task.isCompleted()) {
                newTask.setCompleted(false);
            } else {
                newTask.setCompleted(true);
            }

            FirebaseFirestore.getInstance()
                    .collection("Users").document(Main.CURRENT_USER_KEY)
                    .collection("Tasks").document(task.getDocumentId())
                    .set(newTask);
        }catch (Exception e){
            Toast.makeText(mTaskFragment.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
