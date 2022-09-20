package com.smart.planner;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

import android.os.SystemClock;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.smart.planner.Adapters.TaskViewSection;
import com.smart.planner.Listener.OnSwipeTouchListener;
import com.smart.planner.POJOs.Event;
import com.smart.planner.POJOs.List;
import com.smart.planner.POJOs.Reminder;
import com.smart.planner.POJOs.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class FragmentTasks extends Fragment implements Filterable, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    public static final long DAY = 86400000L;

    private ViewSwitcher taskContainer;
    private RecyclerView taskRecycleView;
    private ConstraintLayout emptyMessageLayout;
    private FloatingActionButton fab_event;
    private FloatingActionButton fab_reminder;
    private FloatingActionsMenu floatMenu;
    private TabLayout tabLayout ;

    private ArrayList<Reminder> previousTask;
    private ArrayList<Reminder> todayTask;
    private ArrayList<Reminder> tomorrowTask;
    private ArrayList<Reminder> upcomingTask;

    protected RecyclerView.Adapter taskViewAdapter;
    public SectionedRecyclerViewAdapter sectionAdapter;
    private RecyclerView.LayoutManager recycleViewLayoutManager;

    private FirebaseFirestore firestore;
    private Query firestoreQuery;
    ListenerRegistration listenerRegistration;

    Date today;
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);
        initComponents(view);
        return view;
    }

    private void initComponents(final View view) {
        taskContainer = view.findViewById(R.id.task_container);
        emptyMessageLayout = view.findViewById(R.id.emptyTasksMessageLayout);
        fab_event = view.findViewById(R.id.fab_submenu_event);
        fab_reminder = view.findViewById(R.id.fab_submenu_reminder);
        taskRecycleView = view.findViewById(R.id.task_recycle_view);
        taskRecycleView.setVisibility(View.VISIBLE);
        emptyMessageLayout.setVisibility(View.INVISIBLE);
        tabLayout = view.findViewById(R.id.tabLayout);
        floatMenu = view.findViewById(R.id.fab_add);
        try {
            today = dateFormat.parse(dateFormat.format(new Date()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // firestore initialization
        firestore = FirebaseFirestore.getInstance();
        createTabByList();
        previousTask = new ArrayList<>();
        todayTask = new ArrayList<>();
        tomorrowTask = new ArrayList<>();
        upcomingTask = new ArrayList<>();

        sectionAdapter = new SectionedRecyclerViewAdapter();

        TaskViewSection prevTaskSec = new TaskViewSection(taskRecycleView,FragmentTasks.this, "Up to date", previousTask,sectionAdapter);
        TaskViewSection todayTaskSec = new TaskViewSection(taskRecycleView,FragmentTasks.this, "Today", todayTask,sectionAdapter);
        TaskViewSection tomorrowTaskSec = new TaskViewSection(taskRecycleView,FragmentTasks.this, "Tomorrow", tomorrowTask,sectionAdapter);
        TaskViewSection upcomingTaskSec = new TaskViewSection(taskRecycleView,FragmentTasks.this, "Upcoming", upcomingTask,sectionAdapter);

        sectionAdapter.addSection(prevTaskSec);
        sectionAdapter.addSection(todayTaskSec);
        sectionAdapter.addSection(tomorrowTaskSec);
        sectionAdapter.addSection(upcomingTaskSec);

        retrieveTaskData(null);
        recycleViewLayoutManager = new LinearLayoutManager(getActivity());
        taskRecycleView.setHasFixedSize(true);

        taskRecycleView.setLayoutManager(recycleViewLayoutManager);
        taskRecycleView.setAdapter(sectionAdapter);

        fab_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), EventEditor.class));
            }
        });

        fab_reminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), TaskEditor.class);
                startActivity(i);
            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                retrieveTaskData(tab.getText().toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void createTabByList() {
        if(Main.CURRENT_USER_KEY != null){
            tabLayout.removeAllTabs();
            firestore.collection("Users").document(Main.CURRENT_USER_KEY).collection("Lists").addSnapshotListener(MetadataChanges.INCLUDE,new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    assert queryDocumentSnapshots != null;
                    for (DocumentChange d : queryDocumentSnapshots.getDocumentChanges()){
                        List li = d.getDocument().toObject(List.class);
                        tabLayout.addTab(tabLayout.newTab().setText(li.getListName()));
                    }
                }
            });
        }
    }

    private synchronized void retrieveTaskData(final String list) {
        if (Main.CURRENT_USER_KEY != null) {
            if (list != null && !list.trim().equalsIgnoreCase("inbox")){
                firestoreQuery = firestore.collection("Users").document(Main.CURRENT_USER_KEY).collection("Tasks").orderBy("dueDate").whereEqualTo("listName",list);
            }else {
                firestoreQuery = firestore.collection("Users").document(Main.CURRENT_USER_KEY).collection("Tasks").orderBy("dueDate");
            }
            listenerRegistration = firestoreQuery.addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    assert queryDocumentSnapshots != null;
                    updateTasksList(queryDocumentSnapshots);
                    sectionAdapter.notifyDataSetChanged();
                    if (previousTask.size()==0 && todayTask.size()==0 && tomorrowTask.size()==0 && upcomingTask.size()==0){
                        if (R.id.emptyTasksMessageLayout == taskContainer.getNextView().getId()) {
                            taskContainer.showNext();
                        }
                    }else if(R.id.task_recycle_view == taskContainer.getNextView().getId()){
                        taskContainer.showNext();
                    }
                }

                private void addToSection(Reminder task) {
                    try {
                        Date taskDate ;
                        if (task.isEvent()){
                            taskDate = dateFormat.parse(dateFormat.format(((Event)task).getDueDate()));
                        }else{
                            taskDate = dateFormat.parse(dateFormat.format(((Task)task).getDueDate()));
                        }
                        if (((today.getTime() - taskDate.getTime()) / DAY) > 0) {
                            previousTask.add(task);
                        } else if (((today.getTime() - taskDate.getTime()) / DAY) == 0) {
                            todayTask.add(task);
                        } else if (((today.getTime() - taskDate.getTime()) / DAY) == -1) {
                            tomorrowTask.add(task);
                        } else {
                            upcomingTask.add(task);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                private void updateTasksList(QuerySnapshot querySnapshots) {
                    previousTask.clear();
                    todayTask.clear();
                    tomorrowTask.clear();
                    upcomingTask.clear();
                    if(querySnapshots.size() > 0) {
                        for (DocumentSnapshot snapshot : querySnapshots) {
                            Reminder re = snapshot.toObject(Reminder.class);
                            if(!re.isEvent()) {
                                Task task = snapshot.toObject(Task.class);
                                assert task != null;
                                task.setDocumentId(snapshot.getId());
                                addToSection(task);
                            }else{
                                Event event = snapshot.toObject(Event.class);
                                assert event != null;
                                event.setDocumentId(snapshot.getId());
                                addToSection(event);
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    // task data filtering
    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
//            ArrayList<Task> filteredList = new ArrayList<>();
//
//            if (constraint == null || constraint.length() == 0) {
//                filteredList.addAll(tasksListFullCopy);
//            } else {
//                String filterPattern = constraint.toString().toLowerCase().trim();
//                for (Task taskItem : tasksListFullCopy) {
//                    if (taskItem.getTaskName().toLowerCase().startsWith(filterPattern)) {
//                        filteredList.add(taskItem);
//                    }
//                }
//            }
            FilterResults results = new FilterResults();
//            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            todayTask.clear();
            todayTask.addAll((ArrayList) results.values);
            taskViewAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dateOfMonth) {

    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {

    }

    public void collapseFloatingActionMenu(){
        floatMenu.collapse();
    }
}