package com.smart.planner;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.smart.planner.Adapters.CalendarAdapter;
import com.smart.planner.POJOs.Reminder;
import com.smart.planner.POJOs.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import sun.bob.mcalendarview.MarkStyle;
import sun.bob.mcalendarview.listeners.OnDateClickListener;
import sun.bob.mcalendarview.views.ExpCalendarView;
import sun.bob.mcalendarview.vo.DateData;
import sun.bob.mcalendarview.vo.MarkedDates;

public class FragmentCalender extends Fragment {

    private RecyclerView dynamic_list;
    private ExpCalendarView calender_event;
    private QuerySnapshot snapshotsCopy;
    protected RecyclerView.Adapter calendarViewAdapter;
    private RecyclerView.LayoutManager recycleViewLayoutManager;
    private ArrayList<Reminder> reminder_list;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        calender_event = view.findViewById(R.id.calenderEvent);
        dynamic_list = view.findViewById(R.id.dynamic_view);

        reminder_list = new ArrayList<>();

        recycleViewLayoutManager = new LinearLayoutManager(getActivity());
        dynamic_list.setHasFixedSize(true);
        dynamic_list.setLayoutManager(recycleViewLayoutManager);
        calendarViewAdapter = new CalendarAdapter(reminder_list, FragmentCalender.this, calender_event);
        dynamic_list.setAdapter(calendarViewAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        createCalendarEvents();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        calender_event.setOnDateClickListener(new OnDateClickListener() {
            @Override
            public void onDateClick(View view, DateData date) {
                try {
                    reminder_list.clear();
                    final Calendar cal1 = Calendar.getInstance();
                    cal1.setTime(dateFormat.parse(date.getYear() + "-" + date.getMonth() + "-" + date.getDay()));
                    cal1.set(Calendar.MILLISECOND, 0);
                    Calendar cal2 = Calendar.getInstance();

                    for (DocumentSnapshot doc : snapshotsCopy.getDocuments()) {
                        Task task = doc.toObject(Task.class);
                        task.setDocumentId(doc.getId());
                        cal2.setTime(task.getDueDate());
                        cal2.set(Calendar.HOUR_OF_DAY, 0);
                        cal2.set(Calendar.MINUTE, 0);
                        cal2.set(Calendar.SECOND, 0);
                        cal2.set(Calendar.MILLISECOND, 0);

                        if (cal1.getTimeInMillis() == cal2.getTimeInMillis()) {
                            reminder_list.add(task);
                        }
                    }
                    calendarViewAdapter.notifyDataSetChanged();

                } catch (ParseException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity().getBaseContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createCalendarEvents() {
        calender_event.getMarkedDates().removeAdd();
        FirebaseFirestore.getInstance().collection("Users").document(Main.CURRENT_USER_KEY).collection("Tasks")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        snapshotsCopy = snapshots;
                        Calendar today = Calendar.getInstance();

                        reminder_list.clear();
                        for (DocumentSnapshot document : snapshots.getDocuments()) {
                            Task task = document.toObject(Task.class);
                            task.setDocumentId(document.getId());

                            Calendar cal = Calendar.getInstance();
                            cal.setTime(task.getDueDate());

                            today.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
                            today.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
                            today.set(Calendar.SECOND, cal.get(Calendar.SECOND));

                            if (DateUtils.isToday(cal.getTimeInMillis())) {
                                reminder_list.add(task);
                            }

                            calender_event.markDate(
                                    new DateData(
                                            cal.get(Calendar.YEAR),
                                            cal.get(Calendar.MONTH) + 1,
                                            cal.get(Calendar.DAY_OF_MONTH)
                                    ).setMarkStyle(
                                            new MarkStyle(MarkStyle.LEFTSIDEBAR, Color.BLUE))
                            );
                        }
                        calendarViewAdapter.notifyDataSetChanged();
                    }
                });
    }
}
