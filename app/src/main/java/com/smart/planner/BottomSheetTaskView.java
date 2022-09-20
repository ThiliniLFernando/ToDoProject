package com.smart.planner;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.smart.planner.AsyncTasks.DBDeleteTask;
import com.smart.planner.AsyncTasks.DBUpdateTask;
import com.smart.planner.Dialogs.DatePickerFragment;
import com.smart.planner.Dialogs.TimePickerFragment;
import com.smart.planner.LocalDB.NotificationReminder;
import com.smart.planner.POJOs.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BottomSheetTaskView extends BottomSheetDialogFragment {

    private BottomSheetTaskView mThis;
    private EditText task_name;
    private TextView task_cat, task_date, task_time, task_priority, task_remind_me;
    private Switch task_remind_on;
    private LinearLayout task_remind_linear;
    private Button update_task,remove_task;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    private FirebaseFirestore fireStore;

    private String[] listNameArray;
    private String[] reminderItems;
    private int CHECKED_LIST_ITEM = 0;
    private int CHECKED_PRIORITY_ITEM = 0;
    private boolean[] CHECKED_REMINDER_ITEM = null;
    private Map<String, Integer> checkedReminders = new HashMap<>();

    {
        retrieveListData();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View bottomSheet = inflater.inflate(R.layout.bottom_sheet_task_view, container, false);
        initComponents(bottomSheet);
        mThis = this;
        return bottomSheet;
    }

    private void initComponents(View view) {
        task_name = view.findViewById(R.id.taskName);
        task_cat = view.findViewById(R.id.taskCat);
        task_date = view.findViewById(R.id.taskTime);
        task_time = view.findViewById(R.id.taskDate);
        task_priority = view.findViewById(R.id.taskPriority);
        task_remind_me = view.findViewById(R.id.taskRemindMe);
        task_remind_linear = view.findViewById(R.id.taskRemindLinear);
        update_task = view.findViewById(R.id.updateTask);
        remove_task = view.findViewById(R.id.removeTask);

        Bundle arguments = getArguments();
        final Task task = (Task) arguments.getSerializable("taskObj");
        task_name.setText(task.getTaskName());
        task_cat.setText(task.getListName());
        task_date.setText(dateFormat.format(task.getDueDate()));
        task_time.setText(timeFormat.format(task.getDueDate()));

        reminderItems = getResources().getStringArray(R.array.reminder_list);

        if (task.getPriority() != null) {
            task_priority.setText(task.getPriority());
        }

        String reminders = "";
        if (task.getReminders() != null) {
            for (Date d : task.getReminders()) {
                int l = (int) ((task.getDueDate().getTime() - d.getTime()) / 60000);
                switch (l) {
                    case 0:
                        if (!reminders.isEmpty())
                            reminders += ",";
                        reminders += "On Time ";
                        break;
                    case 5:
                        if (!reminders.isEmpty())
                            reminders += ",";
                        reminders += "5 mins early ";
                        break;
                    case 30:
                        if (!reminders.isEmpty())
                            reminders += ",";
                        reminders += "30 mins early ";
                        break;
                    case 60:
                        if (!reminders.isEmpty())
                            reminders += ",";
                        reminders += "1 hr early ";
                        break;
                    case 1440:
                        if (!reminders.isEmpty())
                            reminders += ",";
                        reminders += "1 day early ";
                        break;
                }
            }
        }
        task_remind_me.setText(reminders);

        // open date picker dialog
        task_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker = new DatePickerFragment(task_date);
                datePicker.show(getChildFragmentManager(), "date_picker");
            }
        });

        // open time picker dialog
        task_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timePicker = new TimePickerFragment(task_time);
                timePicker.show(getChildFragmentManager(), "time_picker");
            }
        });

        task_cat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listNameArray != null) {
                    createAlertDialog(listNameArray, task_cat, CHECKED_LIST_ITEM).show();
                }
            }
        });

        task_priority.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAlertDialog(getResources().getStringArray(R.array.priority_list), task_priority, CHECKED_PRIORITY_ITEM)
                        .show();
            }
        });

        task_remind_me.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewReminderDialog();
            }
        });

        //UPDATE TASK BUTTON
        update_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateTask(task);
            }
        });
        //UPDATE TASK BUTTON

        remove_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeTask(task);
            }
        });

    }

    // UPDATE TASK METHOD
    private void updateTask(Task task) {
        String document_id = task.getDocumentId();
        ArrayList<Date> reminders = new ArrayList<>();

        Map<String, Object> updated_task = new HashMap<>();

        if (!TextUtils.isEmpty(task_name.getText().toString())) {
            updated_task.put("taskName", task_name.getText().toString());
        }

        updated_task.put("listName", task_cat.getText().toString());

        if (!TextUtils.isEmpty(task_priority.getText().toString())) {
            updated_task.put("priority", task_priority.getText().toString());
        }

        Calendar calendar = Calendar.getInstance();

        Calendar checkCal = Calendar.getInstance();
        checkCal.setTimeInMillis(System.currentTimeMillis());
        checkCal.set(Calendar.SECOND, 00);

        try {
            calendar.setTime(simpleDateFormat.parse(task_date.getText().toString() + " " + task_time.getText().toString()));
            updated_task.put("dueDate", calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        calendar.set(Calendar.SECOND, 00);

        boolean invalidReminder = false;
        StringBuilder reminderMsg = new StringBuilder();
        if (checkedReminders != null) {
            final Calendar c2 = Calendar.getInstance();
            if (!checkedReminders.containsKey("None") && checkedReminders.size() > 0) {
                for (int which : checkedReminders.values()) {
                    switch (which) {
                        case 1:
                            c2.setTime(calendar.getTime());
                            reminders.add(c2.getTime());
                            break;

                        case 2:
                            c2.setTime(calendar.getTime());
                            c2.add(Calendar.MINUTE, -5);
                            reminders.add(c2.getTime());
                            if (c2.getTime().before(checkCal.getTime())) {
                                invalidReminder = true;
                                reminderMsg.append("5 mins early,");
                            }
                            break;

                        case 3:
                            c2.setTime(calendar.getTime());
                            c2.add(Calendar.MINUTE, -30);
                            reminders.add(c2.getTime());
                            if (c2.getTime().before(checkCal.getTime())) {
                                invalidReminder = true;
                                reminderMsg.append("30 mins early,");
                            }
                            break;

                        case 4:
                            c2.setTime(calendar.getTime());
                            c2.add(Calendar.HOUR_OF_DAY, -1);
                            reminders.add(c2.getTime());
                            if (c2.getTime().before(checkCal.getTime())) {
                                invalidReminder = true;
                                reminderMsg.append("1 hr early,");
                            }
                            break;

                        case 5:
                            c2.setTime(calendar.getTime());
                            c2.add(Calendar.DATE, -1);
                            reminders.add(c2.getTime());
                            if (c2.getTime().before(checkCal.getTime())) {
                                invalidReminder = true;
                                reminderMsg.append("1 day early,");
                            }
                            break;
                    }
                }
            }
        }

        if (invalidReminder) {
            Toast.makeText(getContext(), "Please uncheck " + reminderMsg + " reminders", Toast.LENGTH_SHORT).show();
        } else {
            if (reminders.size() > 0) {
                updated_task.put("reminders", reminders);
            }
            final HashMap<String, Object> updated_task2 = new HashMap<>(updated_task);
            updated_task2.put("documentId",document_id);
            final DocumentReference reference = fireStore.collection("Users").document(Main.CURRENT_USER_KEY)
                    .collection("Tasks")
                    .document(document_id);
            Query query = fireStore.collection("Users").document(Main.CURRENT_USER_KEY).collection("Tasks").whereEqualTo("taskUniqueId", task.getTaskUniqueId());
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        final List<DocumentSnapshot> list = task.getResult().getDocuments();
                        if (list.size() > 0) {
                            if (list.size() > 1) {
                                new AlertDialog.Builder(getContext())
                                        .setTitle("Update Reminders")
                                        .setMessage("Do you want to rename all the repeat reminders ?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                for (DocumentSnapshot snapshot : list) {
                                                    HashMap<String, Object> ob = (HashMap<String, Object>) snapshot.getData();
                                                    ob.put("taskName", task_name.getText().toString());
                                                    ob.put("documentId", snapshot.getId());
                                                    fireStore.collection("Users").document(Main.CURRENT_USER_KEY)
                                                            .collection("Tasks")
                                                            .document(snapshot.getId()).update(ob);
                                                    new DBUpdateTask(getContext()).execute(ob);
                                                }
                                                Toast.makeText(getContext(), "Reminder successfully updated.", Toast.LENGTH_SHORT).show();
                                                mThis.dismiss();
                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                new DBUpdateTask(getContext()).execute(updated_task2);
                                                reference.update(updated_task2)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                                                                Toast.makeText(getContext(), "Reminder successfully updated.", Toast.LENGTH_SHORT).show();
                                                                mThis.dismiss();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                mThis.dismiss();
                                                            }
                                                        });
                                            }
                                        })
                                        .show();
                            } else {
                                new DBUpdateTask(getContext()).execute(updated_task2);
                                reference.update(updated_task2)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                                                Toast.makeText(getContext(), "Reminder successfully updated.", Toast.LENGTH_SHORT).show();
                                                mThis.dismiss();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }
                }
            });
        }
    }
    // UPDATE TASK METHOD

    private void viewReminderDialog() {
        if (CHECKED_REMINDER_ITEM == null) {
            CHECKED_REMINDER_ITEM = new boolean[reminderItems.length];
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
                            Toast.makeText(getContext(), "Cannot ann reminders more than two", Toast.LENGTH_SHORT).show();
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
                    task_remind_me.setText(reminderTxt);
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

    private void retrieveListData() {
        fireStore = FirebaseFirestore.getInstance();
        if (Main.CURRENT_USER_KEY != null) {
            fireStore.collection("Users").document(Main.CURRENT_USER_KEY)
                    .collection("Lists").addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void removeTask(final Task task) {
        final FirebaseFirestore fireStore = FirebaseFirestore.getInstance();
        final String id = task.getTaskUniqueId();
        final String documentId = task.getDocumentId();
        if (id != null && !id.trim().equals("")) {
            Query query = fireStore.
                    collection("Users").
                    document(Main.CURRENT_USER_KEY).
                    collection("Tasks").
                    whereEqualTo("taskUniqueId", id);
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull final com.google.android.gms.tasks.Task<QuerySnapshot> t) {
                    if (t.isSuccessful()) {
                        final List<DocumentSnapshot> list = t.getResult().getDocuments();
                        if (list.size() > 0) {
                            if (list.size() > 1) {
                                new android.app.AlertDialog.Builder(getContext())
                                        .setTitle("Remove Confirmation")
                                        .setMessage("Do you want to remove all the repeat tasks ?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                for (DocumentSnapshot snapshot : list) {
                                                    fireStore.collection("Users").document(Main.CURRENT_USER_KEY).collection("Tasks").document(snapshot.getId()).delete();
                                                    new DBDeleteTask(getContext()).execute(snapshot.getId());
                                                }
                                                Toast.makeText(getContext(), "Your reminder is deleted.", Toast.LENGTH_SHORT).show();
                                                mThis.dismiss();
                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                fireStore.collection("Users").document(Main.CURRENT_USER_KEY).collection("Tasks").document(documentId).delete()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                                                                new DBDeleteTask(getContext()).execute(documentId);
                                                                Toast.makeText(getContext(), "Your reminder is deleted.", Toast.LENGTH_SHORT).show();
                                                                mThis.dismiss();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }
                                        })
                                        .show();
                            } else {
                                fireStore.collection("Users").document(Main.CURRENT_USER_KEY).collection("Tasks").document(documentId).delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                                                Toast.makeText(getContext(), "Your reminder is deleted.", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }
                }
            });

        } else {
            Toast.makeText(getContext(), "Id is null or empty.", Toast.LENGTH_SHORT).show();
        }
    }

    // create Alert Dialog
    private AlertDialog createAlertDialog(final String[] stringArray, final TextView view, final int checkedItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}

