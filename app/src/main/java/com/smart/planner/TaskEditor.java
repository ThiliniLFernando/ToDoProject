package com.smart.planner;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.smart.planner.AsyncTasks.DBInsertTask;
import com.smart.planner.Dialogs.TimePickerFragment;
import com.smart.planner.LocalDB.DAO.NotificationReminderDAO;
import com.smart.planner.LocalDB.LocalDB;
import com.smart.planner.LocalDB.NotificationReminder;
import com.smart.planner.reminder.NotificationScheduler;
import com.smart.planner.Dialogs.DatePickerFragment;
import com.smart.planner.POJOs.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskEditor extends AppCompatActivity {

    private int CHECKED_PRIORITY_ITEM = 0;
    private int CHECKED_LIST_ITEM = 0;
    private int CHECKED_REPEAT_METHOD_ITEM = 0;
    private boolean[] CHECKED_REMINDER_ITEM = null;
    private static String taskReferenceId = "";

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    private String[] listNameArray;
    private String[] repeatMethodItems;
    private String[] reminderItems;
    private Map<String, Integer> checkedReminders = new HashMap<>();

    private Toolbar toolbar;
    private EditText taskName;
    private TextView list, priority, dueDate, dueTime, remindMe, repeatMethod;
    private LinearLayout hiddenView;
    public ImageButton removeDate, removeTime, removeRepeatMethod, removeReminder;

    private FirebaseFirestore fireStore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Main.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_task);
        retrieveListData();
        initComponents(getIntent());
    }

    private void retrieveListData() {
        fireStore = FirebaseFirestore.getInstance();
        if (Main.CURRENT_USER_KEY != null) {
            fireStore.collection("Users").document(Main.CURRENT_USER_KEY)
                    .collection("Lists").addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Toast.makeText(TaskEditor.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int i = 0;
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        if (listNameArray == null) {
                            listNameArray = new String[(queryDocumentSnapshots.size())];
                        }
                        listNameArray[i] = snapshot.get("listName").toString();
                        i++;
                    }
                }
            });
        }
    }


    private void initComponents(Intent intent) {
        toolbar = findViewById(R.id.te_toolbar);
        taskName = findViewById(R.id.te_new_task_name);
        list = findViewById(R.id.te_list_name);
        dueDate = findViewById(R.id.te_date);
        dueTime = findViewById(R.id.te_time);
        remindMe = findViewById(R.id.te_reminder);
        repeatMethod = findViewById(R.id.te_repeat);
        priority = findViewById(R.id.te_priority);
        removeDate = findViewById(R.id.remove_date);
        removeTime = findViewById(R.id.remove_time);
        removeReminder = findViewById(R.id.remove_remind);
        removeRepeatMethod = findViewById(R.id.remove_repeat);
        hiddenView = findViewById(R.id.hiddenLinear);

        reminderItems = getResources().getStringArray(R.array.reminder_list);
        repeatMethodItems = getResources().getStringArray(R.array.repeat_method_spinner);

        // set toolbar as action bar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("New Task");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // hide Linear layout
        hiddenView.setVisibility(View.GONE);

        // List on click listener
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listNameArray != null) {
                    createAlertDialog(listNameArray, list, CHECKED_LIST_ITEM).show();
                }
            }
        });

        // Priority on click listener
        priority.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAlertDialog(getResources().getStringArray(R.array.priority_list), priority, CHECKED_PRIORITY_ITEM)
                        .show();
            }
        });

        // select date
        dueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment datePicker = new DatePickerFragment(dueDate);
                datePicker.show(getSupportFragmentManager(), "date_picker");
            }
        });

        // select time or change time
        dueTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment timePicker = new TimePickerFragment(dueTime);
                timePicker.show(getSupportFragmentManager(), "time_picker");
            }
        });

        //reminder add Text View setup
        remindMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CHECKED_REMINDER_ITEM == null) {
                    CHECKED_REMINDER_ITEM = new boolean[reminderItems.length];
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(TaskEditor.this);
                builder.setTitle(getResources().getString(R.string.alert_reminder_title));

                builder.setMultiChoiceItems(reminderItems, CHECKED_REMINDER_ITEM, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface mDialog, int position, boolean isChecked) {
                        if (isChecked) {
                            if ("None".equals(reminderItems[position])) {
                                if (checkedReminders.size() > 0) {
                                    for (int i : checkedReminders.values()) {
                                        CHECKED_REMINDER_ITEM[i] = false;
                                        ((AlertDialog) mDialog).getListView().setItemChecked(i, false);
                                    }
                                    checkedReminders.clear();
                                }
                                CHECKED_REMINDER_ITEM[position] = true;
                                checkedReminders.put(reminderItems[position], position);
                            } else {
                                if (checkedReminders.containsKey("None")) {
                                    CHECKED_REMINDER_ITEM[0] = false;
                                    checkedReminders.remove("None");
                                    ((AlertDialog) mDialog).getListView().setItemChecked(0, false);

                                }
                                if (checkedReminders.size() >= 0 && checkedReminders.size() < 2) {
                                    CHECKED_REMINDER_ITEM[position] = true;
                                    checkedReminders.put(reminderItems[position], position);
                                } else {
                                    CHECKED_REMINDER_ITEM[position] = false;
                                    ((AlertDialog) mDialog).getListView().setItemChecked(position, false);
                                    Toast.makeText(TaskEditor.this, "Cannot ann reminders more than two", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            CHECKED_REMINDER_ITEM[position] = false;
                            checkedReminders.remove(reminderItems[position]);
                        }
                    }
                }).setPositiveButton(R.string.positive_button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // positive button
                        if (!checkedReminders.containsKey("None")) {
                            String reminderTxt = "";
                            for (String s : checkedReminders.keySet()) {
                                reminderTxt += s + "\r";
                            }
                            remindMe.setText(reminderTxt);
                        }
                    }
                }).setNegativeButton(R.string.negative_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // negative button
                        CHECKED_REMINDER_ITEM = null;
                        checkedReminders.clear();
                    }
                });
                builder.create().show();
            }
        });

        // repeat method setup
        repeatMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAlertDialog(repeatMethodItems, repeatMethod, CHECKED_REPEAT_METHOD_ITEM)
                        .show();
            }
        });

        // remove buttons code
        removeDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dueDate.setText(null);
                dueTime.setText(null);
                remindMe.setText(null);
                repeatMethod.setText(null);
                hiddenView.setVisibility(View.GONE);
            }
        });

        //remove time
        removeTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dueTime.setText(null);
            }
        });

        //remove reminder
        removeReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkedReminders.clear();
                CHECKED_REMINDER_ITEM = null;
                remindMe.setText(null);
            }
        });

        //remove repeat method
        removeRepeatMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repeatMethod.setText(null);
            }
        });

        // add text change listeners for text view
        dueDate.addTextChangedListener(new TaskEditor.TextChangeListener(dueDate));
        dueTime.addTextChangedListener(new TaskEditor.TextChangeListener(dueTime));
        repeatMethod.addTextChangedListener(new TaskEditor.TextChangeListener(repeatMethod));
        remindMe.addTextChangedListener(new TaskEditor.TextChangeListener(remindMe));

    }

    //option menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveNewTask();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // create Alert Dialog
    private AlertDialog createAlertDialog(final String[] stringArray, final TextView view, final int checkedItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(TaskEditor.this);
        builder.setTitle(view.getHint());

        builder.setSingleChoiceItems(stringArray, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int checked_item) {
                if (checked_item != -1) {
                    if (stringArray[checked_item].equals("None")) {
                        CHECKED_LIST_ITEM = 0;
                        view.setText(null);
                    } else {
                        CHECKED_LIST_ITEM = checked_item;
                        view.setText(stringArray[checked_item]);
                    }
                }
                dialog.dismiss();
            }
        });
        return builder.create();
    }

    // TextChangeListener inner class
    class TextChangeListener implements TextWatcher {

        private TextView textView;

        TextChangeListener(TextView textView) {
            this.textView = textView;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.toString().length() == 0) {
                switch (textView.getId()) {
                    case R.id.te_date:
                        removeDate.setVisibility(View.INVISIBLE);
                        break;
                    case R.id.te_time:
                        removeTime.setVisibility(View.INVISIBLE);
                        break;
                    case R.id.te_reminder:
                        removeReminder.setVisibility(View.INVISIBLE);
                        break;
                    case R.id.te_repeat:
                        removeRepeatMethod.setVisibility(View.INVISIBLE);
                        break;
                }
            } else {
                switch (textView.getId()) {
                    case R.id.te_date:
                        removeDate.setVisibility(View.VISIBLE);
                        break;
                    case R.id.te_time:
                        removeTime.setVisibility(View.VISIBLE);
                        break;
                    case R.id.te_reminder:
                        removeReminder.setVisibility(View.VISIBLE);
                        break;
                    case R.id.te_repeat:
                        removeRepeatMethod.setVisibility(View.VISIBLE);
                        break;
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            switch (textView.getId()) {
                case R.id.te_date:
                    if (!textView.getText().toString().isEmpty()) {
                        hiddenView.setVisibility(View.VISIBLE);
                        TimePickerFragment timePicker = new TimePickerFragment(dueTime);
                        timePicker.show(getSupportFragmentManager(), "time_picker");
                    }
                    break;
            }
        }
    }

    // SAVE NEW TASK
    private void saveNewTask() {
        try {
            final String task_name = this.taskName.getText().toString().trim();
            String list = this.list.getText().toString().trim();
            String priority = this.priority.getText().toString().trim();
            String date = this.dueDate.getText().toString().trim();
            String time = this.dueTime.getText().toString().trim();
            String repeat_method = this.repeatMethod.getText().toString().trim();

            Calendar calendar = Calendar.getInstance();
            if (TextUtils.isEmpty(date)) {
                // DATE NOT SELECTED // IT MEANS TIME ALSO NOT SELECTED
                calendar.setTimeInMillis(System.currentTimeMillis());
            } else {
                try {
                    calendar.setTime(simpleDateFormat.parse(date + " " + "09:00"));

                    if (TextUtils.isEmpty(time)) {
                        // TIME NOT SELECTED
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(System.currentTimeMillis());

                        String today = dateFormat.format(cal.getTime());
                        String scheduled = dateFormat.format(calendar.getTime());

                        if (today.equals(scheduled)) {
                            calendar.set(Calendar.HOUR, cal.get(Calendar.HOUR));
                            calendar.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
                            calendar.add(Calendar.MINUTE, 15);
                        }
                    } else {
                        calendar.setTime(simpleDateFormat.parse(date + " " + time));
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            calendar.set(Calendar.SECOND, 00);

            Calendar checkCal = Calendar.getInstance();
            checkCal.setTimeInMillis(System.currentTimeMillis());
            checkCal.set(Calendar.SECOND, 00);

            if (TextUtils.isEmpty(task_name)) {
                Toast.makeText(TaskEditor.this, "Please enter task name", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(list)) {
                Toast.makeText(TaskEditor.this, "Please select list", Toast.LENGTH_SHORT).show();
            } else if (calendar.getTime().before(checkCal.getTime())) {
                Toast.makeText(TaskEditor.this, "Please select valid date and time !", Toast.LENGTH_SHORT).show();
            } else {
                final Task task = new Task(task_name, list);
                task.setDueDate(calendar.getTime());
                task.setReminderType("Task");

                if (!TextUtils.isEmpty(priority)) {
                    task.setPriority(priority);
                }
                if (!TextUtils.isEmpty(repeat_method)) {
                    task.setRepeatMethod(repeat_method);
                }

                ArrayList<Task> repeatArray = new ArrayList<>();
                final ArrayList<Task> newRepeatArray = new ArrayList<>();
                if (repeat_method != null && !repeat_method.trim().equals("")) {
                    String rm = repeat_method.toLowerCase().trim();
                    switch (rm) {
                        case "daily":
                            // until one year
                            for (int i = 0; i <= 30; i++) {
                                Task t = (Task) task.clone();
                                Calendar cl1 = Calendar.getInstance();
                                cl1.setTime(task.getDueDate());
                                cl1.add(Calendar.WEEK_OF_YEAR, i);
                                t.setDueDate(cl1.getTime());
                                repeatArray.add(t);
                            }
                            break;
                        case "weekly":
                            // until one year
                            for (int i = 0; i <= 13; i++) {
                                Task t = (Task) task.clone();
                                Calendar cl1 = Calendar.getInstance();
                                cl1.setTime(task.getDueDate());
                                cl1.add(Calendar.WEEK_OF_YEAR, i);
                                t.setDueDate(cl1.getTime());
                                repeatArray.add(t);
                            }
                            break;
                        case "monthly":
                            // until one year
                            for (int i = 0; i <= 12; i++) {
                                Task t = (Task) task.clone();
                                Calendar cl1 = Calendar.getInstance();
                                cl1.setTime(task.getDueDate());
                                cl1.add(Calendar.MONTH, i);
                                t.setDueDate(cl1.getTime());
                                repeatArray.add(t);
                            }
                            break;
                        case "yearly":
                            // until 3 years
                            for (int i = 0; i <= 2; i++) {
                                Task t = (Task) task.clone();
                                Calendar cl1 = Calendar.getInstance();
                                cl1.setTime(task.getDueDate());
                                cl1.add(Calendar.YEAR, i);
                                t.setDueDate(cl1.getTime());
                                repeatArray.add(t);
                            }
                            break;
                        case "never":
                            repeatArray.add(task);
                            break;
                    }
                } else {
                    repeatArray.add(task);
                }

                boolean invalidReminder = false;
                StringBuilder reminderMsg = new StringBuilder();
                if (checkedReminders != null) {
                    final Calendar c2 = Calendar.getInstance();
                    if (!checkedReminders.containsKey("None") && checkedReminders.size() > 0) {
                        for (Task t : repeatArray) {
                            ArrayList<Date> reminders = new ArrayList<>();
                            for (int which : checkedReminders.values()) {
                                switch (which) {
                                    case 1:
                                        c2.setTime(t.getDueDate());
                                        reminders.add(c2.getTime());
                                        break;

                                    case 2:
                                        c2.setTime(t.getDueDate());
                                        c2.add(Calendar.MINUTE, -5);
                                        reminders.add(c2.getTime());
                                        if (c2.getTime().before(checkCal.getTime())) {
                                            invalidReminder = true;
                                            reminderMsg.append("5 mins early,");
                                        }
                                        break;

                                    case 3:
                                        c2.setTime(t.getDueDate());
                                        c2.add(Calendar.MINUTE, -30);
                                        reminders.add(c2.getTime());
                                        if (c2.getTime().before(checkCal.getTime())) {
                                            invalidReminder = true;
                                            reminderMsg.append("30 mins early,");
                                        }
                                        break;

                                    case 4:
                                        c2.setTime(t.getDueDate());
                                        c2.add(Calendar.HOUR_OF_DAY, -1);
                                        reminders.add(c2.getTime());
                                        if (c2.getTime().before(checkCal.getTime())) {
                                            invalidReminder = true;
                                            reminderMsg.append("1 hr early,");
                                        }
                                        break;

                                    case 5:
                                        c2.setTime(t.getDueDate());
                                        c2.add(Calendar.DATE, -1);
                                        reminders.add(c2.getTime());
                                        if (c2.getTime().before(checkCal.getTime())) {
                                            invalidReminder = true;
                                            reminderMsg.append("1 day early,");
                                        }
                                        break;
                                }
                            }
                            if (invalidReminder) {
                                Toast.makeText(this, "Please uncheck " + reminderMsg + " reminders", Toast.LENGTH_SHORT).show();
                                break;
                            } else {
                                if (reminders.size() > 0) {
                                    t.setReminders(reminders);
                                }
                                newRepeatArray.add(t);
                            }
                        }
                    } else {
                        newRepeatArray.addAll(repeatArray);
                    }
                } else {
                    newRepeatArray.addAll(repeatArray);
                }

                if (invalidReminder) {
                    Toast.makeText(this, "Please uncheck " + reminderMsg + " reminders", Toast.LENGTH_SHORT).show();
                } else {

                    try {
                        if (Main.CURRENT_USER_KEY != null) {
                            // /*
                            Query query = fireStore.collection("Users").document(Main.CURRENT_USER_KEY).collection("Tasks").orderBy("taskUniqueId", Query.Direction.DESCENDING).limit(1).limit(1);
                            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        int i = 1;
                                        if (task.getResult().getDocuments().size() > 0) {
                                            Task task1 = task.getResult().getDocuments().get(0).toObject(Task.class);
                                            i = Integer.parseInt(task1.getTaskUniqueId());
                                            i++;
                                        }
                                        addTaskToFirestore(i, newRepeatArray);
                                    }
                                }
                            });

//                            Calendar c = Calendar.getInstance();
//                            if (task.getReminders() != null) {
//                                if (task.getReminders().size() > 0) {
//                                    for (Date d : task.getReminders()) {
//                                        c.setTime(d);
//                                        NotificationScheduler.scheduleNotifyMe(c, task, getApplicationContext());
//                                    }
//                                }
//                            }

                            taskName.setText(null);
                            TaskEditor.this.list.setText(null);
                            TaskEditor.this.dueDate.setText(null);
                            taskReferenceId = "";
                            if (hiddenView.getVisibility() == View.VISIBLE) {
                                TaskEditor.this.dueTime.setText(null);
                                remindMe.setText(null);
                                CHECKED_REMINDER_ITEM = null;
                                checkedReminders.clear();
                                repeatMethod.setText(null);
                                hiddenView.setVisibility(View.GONE);
                            }
                            TaskEditor.this.priority.setText(null);
                            //Toast.makeText(TaskEditor.this, "New task added", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // SAVE NEW TASK
    private void addTaskToFirestore(int i, ArrayList<Task> newRepeatArray) {
        for (final Task task1 : newRepeatArray) {
            task1.setTaskUniqueId(i + "");
            fireStore.collection("Users").document(Main.CURRENT_USER_KEY)
                    .collection("Tasks").add(task1)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference ref) {
                            task1.setDocumentId(ref.getId());
                            new DBInsertTask(getApplicationContext()).execute(task1);
                        }
                    });
        }
        Toast.makeText(this, "Your reminder is created ", Toast.LENGTH_SHORT).show();
    }
    // SAVE NEW TASK

}
