package com.smart.planner;

import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.smart.planner.Adapters.AttachmentAdapter;
import com.smart.planner.Adapters.ContactAdapter;
import com.smart.planner.Dialogs.DatePickerFragment;
import com.smart.planner.Dialogs.TimePickerFragment;
import com.smart.planner.POJOs.Contact;
import com.smart.planner.POJOs.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

public class EventEditor extends AppCompatActivity {

    private static final int PICK_FILE_RESULT_CODE = 12;
    private static final int PICK_CONTACT = 199;
    private String chooseColorCode = "#dedede";
    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd");
    private static SimpleDateFormat dateFormatter1 = new SimpleDateFormat("yyyy/MM/dd hh:mm a");
    private static SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat dTime2 = new SimpleDateFormat("hh:mm a");

    private static ColorPicker colorPicker;

    private Switch remind_me_switch;
    private EditText eventEditor_title, eventEditor_note;
    private TextView invitation, eventEditor_startTime, eventEditor_dueDate, eventEditor_endTime, eventEditor_location, eventEditor_addInvitees, eventEditor_addAttachments;
    private ImageButton eventEditor_colorChooser;
    private LinearLayout parentView, eventEditor_reminder_spinner, startTime_linear, endTime_linear;
    private AlertDialog optionChooseInvitee;
    private Spinner remindType;

    DatePickerFragment datePickerFragment;
    TimePickerFragment timePickerFragment;

    EventEditor eventEditor;
    private String docKey;

    private ArrayList<Uri> attachments = new ArrayList<>();
    private ArrayList<Contact> invitee_list = new ArrayList<>();
    private AttachmentAdapter adapter;
    private ContactAdapter contactAdapter;
    private RecyclerView files, invitees;
    private StaggeredGridLayoutManager recycleViewLayoutManager, inviteesRecycleViewManager;
    private Toolbar toolbar_event;
    private Spinner inviteTime;
    private StorageReference listRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_event);

        eventEditor = this;
        parentView = findViewById(R.id.eventEditor_parent);
        toolbar_event = findViewById(R.id.event_toolbar);
        eventEditor_title = findViewById(R.id.eventEditor_title);
        startTime_linear = findViewById(R.id.start_time_linear);
        endTime_linear = findViewById(R.id.end_time_linear);
        remind_me_switch = findViewById(R.id.remind_me_switch);
        eventEditor_dueDate = findViewById(R.id.eventEditor_dueDate);
        eventEditor_startTime = findViewById(R.id.eventEditor_start_time);
        eventEditor_endTime = findViewById(R.id.eventEditor_end_time);
        eventEditor_colorChooser = findViewById(R.id.color_chooser_dot);
        eventEditor_reminder_spinner = findViewById(R.id.reminder_spinner);
        eventEditor_location = findViewById(R.id.eventEditor_location);
        eventEditor_note = findViewById(R.id.eventEditor_note);
        eventEditor_addInvitees = findViewById(R.id.eventEditor_addInvitees);
        eventEditor_addAttachments = findViewById(R.id.eventEditor_addAttachments);
        files = findViewById(R.id.file_listView);
        invitees = findViewById(R.id.invitees_recycle_view);
        remindType = findViewById(R.id.spinner);
        invitation = findViewById(R.id.eventEditor_invitationMsg);
        inviteTime = findViewById(R.id.eventEditor_inviteTimer);

        setSupportActionBar(toolbar_event);
        getSupportActionBar().setTitle("New Event");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // attachment adapter setting
        recycleViewLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        recycleViewLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        files.setHasFixedSize(true);
        files.setLayoutManager(recycleViewLayoutManager);
        adapter = new AttachmentAdapter(attachments, EventEditor.this);
        files.setAdapter(adapter);

        // invitees adapter setting
        inviteesRecycleViewManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        inviteesRecycleViewManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        invitees.setHasFixedSize(true);
        invitees.setLayoutManager(inviteesRecycleViewManager);
        contactAdapter = new ContactAdapter(invitee_list, EventEditor.this);
        invitees.setAdapter(contactAdapter);

        // choose color
        colorPicker = new ColorPicker();
        eventEditor_colorChooser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (colorPicker != null) {
                    colorPicker = new ColorPicker();
                }
                colorPicker.show(getSupportFragmentManager(), "color_picker");
            }
        });

        eventEditor_dueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerFragment = new DatePickerFragment(eventEditor_dueDate);
                datePickerFragment.show(getSupportFragmentManager(), "Date_Picker_Fragment");
            }
        });

        startTime_linear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timePickerFragment = new TimePickerFragment(eventEditor_startTime);
                timePickerFragment.show(getSupportFragmentManager(), "Time_Picker_Fragment");
            }
        });

        endTime_linear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timePickerFragment = new TimePickerFragment(eventEditor_endTime);
                timePickerFragment.show(getSupportFragmentManager(), "Time_Picker_Fragment");
            }
        });

        invitation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogAddInvitation invitee = new DialogAddInvitation(EventEditor.this);
                Bundle bundle = new Bundle();
                bundle.putString("invitation", invitation.getText() + "");
                invitee.setArguments(bundle);
                invitee.show(getSupportFragmentManager(), "invitationDialog");
            }
        });

        remind_me_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    eventEditor_reminder_spinner.setVisibility(View.VISIBLE);
                } else if (!isChecked) {
                    eventEditor_reminder_spinner.setVisibility(View.GONE);
                }
            }
        });

        eventEditor_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(view.getContext(), LocationActivity.class));
            }
        });

        eventEditor_addInvitees.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(eventEditor);
                final String[] choices = new String[]{
                        "Add Invitee",
                        "Choose from Contact List"
                };
                builder.setSingleChoiceItems(
                        choices,
                        -1,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String selectedItem = Arrays.asList(choices).get(i);
                                if (selectedItem.equals("Add Invitee")) {
                                    addInvitee();
                                } else if (selectedItem.equals("Choose from Contact List")) {
                                    chooseContactList();
                                }
                            }
                        }
                );
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                optionChooseInvitee = builder.create();
                optionChooseInvitee.show();
            }
        });

        eventEditor_addAttachments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
                chooseFile.setType("*/*");
                chooseFile = Intent.createChooser(chooseFile, "Choose a file");
                startActivityForResult(chooseFile, PICK_FILE_RESULT_CODE);
            }
        });

        docKey = getIntent().getStringExtra("EventId");
        if (docKey != null) {
            getSupportActionBar().setTitle("Event");
            retrieveEventData(docKey);
        }

    }

    public void retrieveEventData(final String docKey) {
        FirebaseFirestore fireStore = FirebaseFirestore.getInstance();
        if (Main.CURRENT_USER_KEY != null) {
            fireStore.collection("Users").document(Main.CURRENT_USER_KEY)
                    .collection("Tasks").document(docKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                    Event event = snapshot.toObject(Event.class);
                    eventEditor_title.setText(event.getEventTitle());
                    eventEditor_dueDate.setText(dateFormatter.format(event.getDueDate()));
                    eventEditor_startTime.setText(dTime2.format(event.getStartTime()));
                    eventEditor_endTime.setText(dTime2.format(event.getEndTime()));
                    if (event.isReminderOn()) {
                        eventEditor_reminder_spinner.setVisibility(View.VISIBLE);
                        remind_me_switch.setChecked(true);
                    } else {
                        eventEditor_reminder_spinner.setVisibility(View.GONE);
                        remind_me_switch.setChecked(false);
                    }
                    int selection = 0;
                    int minuteCount = (int) ((event.getStartTime().getTime() - event.getReminderTime().getTime()) / 60000);
                    if (minuteCount == 5) {
                        selection = 1;
                    } else if (minuteCount == 30) {
                        selection = 2;
                    } else if (minuteCount == 60) {
                        selection = 3;
                    } else if (minuteCount == 1440) {
                        selection = 4;
                    }
                    remindType.setSelection(selection);
                    eventEditor_location.setText(event.getLocation());
                    eventEditor_note.setText(event.getNote());
                    invitation.setText(event.getInvitation());

                    for (Contact in : event.getInvitees()) {
                        addToInvitees(in.getName(), in.getPhone());
                    }

                    listRef = FirebaseStorage.getInstance().getReference().child(Main.CURRENT_USER_KEY + "/" + docKey + "/attachments");
                    listRef.listAll()
                            .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                                @Override
                                public void onSuccess(ListResult listResult) {
                                    for (StorageReference item : listResult.getItems()) {
                                        item.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                attachments.add(uri);
                                                adapter.notifyDataSetChanged();
                                            }
                                        });
                                    }

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(eventEditor, e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });

                }
            });


        }
    }

    private void chooseContactList() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        startActivityForResult(intent, PICK_CONTACT);
        optionChooseInvitee.dismiss();
    }

    private void addInvitee() {
        DialogAddInvitee invitee = new DialogAddInvitee(EventEditor.this);
        invitee.show(getSupportFragmentManager(), "inviteeDialog");
        optionChooseInvitee.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (docKey != null) {
            getMenuInflater().inflate(R.menu.editor_menu_update, menu);
        }else {
            getMenuInflater().inflate(R.menu.editor_menu, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save:
                saveNewEvent();
                break;
            case R.id.action_update:
                updateEventData();
                break;
            case R.id.action_remove:
                removeEvent();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void removeEvent() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this)
                        .setMessage(R.string.event_remove_message)
                        .setTitle(R.string.event_remove_title)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (Main.CURRENT_USER_KEY != null) {
                                    if (docKey != null) {
                                        FirebaseFirestore.getInstance().collection("Users").document(Main.CURRENT_USER_KEY)
                                                .collection("Tasks").document(docKey).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(eventEditor, "Remove Event", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            }
                        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateEventData() {
        Toast.makeText(eventEditor, "Update Event", Toast.LENGTH_SHORT).show();
        if (docKey != null) {
            try {
                String title = eventEditor_title.getText().toString();
                if (title != null && !title.trim().equals("")) {
                    String dueDate = eventEditor_dueDate.getText().toString();
                    if (dueDate != null && !dueDate.trim().equals("")) {
                        String startTime = eventEditor_startTime.getText().toString();
                        if (startTime != null && !startTime.trim().equals("")) {
                            String endTime = eventEditor_endTime.getText().toString();
                            if (endTime != null && !endTime.trim().equals("")) {
                                Calendar today = Calendar.getInstance();
                                Calendar dueCal = Calendar.getInstance();
                                dueCal.setTime(dateFormatter.parse(dueDate));
                                if (today.before(dueCal) || DateUtils.isToday(dueCal.getTimeInMillis())) {
                                    Calendar startT = Calendar.getInstance();
                                    startT.setTime(dateFormatter1.parse(dueDate + " " + startTime));
                                    Calendar endT = Calendar.getInstance();
                                    endT.setTime(dateFormatter1.parse(dueDate + " " + endTime));
                                    if (startT.before(endT)) {
                                        boolean remindMe = remind_me_switch.isChecked();
                                        int remindType = this.remindType.getSelectedItemPosition();
                                        boolean reminderOk = true;
                                        Calendar remindT = Calendar.getInstance();
                                        remindT.setTime(dateFormatter1.parse(dueDate + " " + startTime));

                                        if (remindMe) {
                                            switch (remindType) {
                                                case 1:
                                                    remindT.add(Calendar.MINUTE, -5);
                                                    break;
                                                case 2:
                                                    remindT.add(Calendar.MINUTE, -30);
                                                    break;
                                                case 3:
                                                    remindT.add(Calendar.HOUR_OF_DAY, -1);
                                                    break;
                                                case 4:
                                                    remindT.add(Calendar.DATE, -1);
                                                    break;
                                            }

                                            if (today.after(remindT)) {
                                                reminderOk = false;
                                            }
                                        }

                                        if (reminderOk) {
                                            int inviteType = this.inviteTime.getSelectedItemPosition();
                                            boolean inviteTimeOk = true;
                                            Calendar inviteT = Calendar.getInstance();
                                            inviteT.setTime(startT.getTime());
                                            switch (inviteType) {
                                                case 0:
                                                    inviteT.add(Calendar.HOUR_OF_DAY, -1);
                                                    break;
                                                case 1:
                                                    inviteT.add(Calendar.HOUR_OF_DAY, -6);
                                                    break;
                                                case 2:
                                                    inviteT.add(Calendar.DATE, -1);
                                                    break;
                                                case 3:
                                                    inviteT.add(Calendar.DATE, -30);
                                                    break;
                                            }

                                            if (inviteT.before(today)) {
                                                inviteTimeOk = false;
                                            } else {
                                                if (inviteT.after(startT)) {
                                                    inviteTimeOk = false;
                                                }
                                            }

                                            if (inviteTimeOk) {
                                                String note = eventEditor_note.getText().toString();
                                                String color = getChooseColorCode();
                                                String location = eventEditor_location.getText().toString();
                                                String invite = invitation.getText() + "";
                                                if (Main.CURRENT_USER_KEY != null) {

                                                    final ArrayList<String> delRefList = new ArrayList<>();
                                                    FirebaseStorage.getInstance().getReference()
                                                            .child(Main.CURRENT_USER_KEY + "/" + docKey + "/attachments").listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                                                        @Override
                                                        public void onSuccess(ListResult listResult) {
                                                            for (StorageReference ref : listResult.getItems()) {
                                                                for (Uri uri : attachments) {
                                                                    if (!uri.getPath().equals(ref.getDownloadUrl())) {
                                                                        delRefList.add(ref.getName());
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    });

                                                    for (String delFile : delRefList) {
                                                        FirebaseStorage.getInstance().getReference()
                                                                .child(Main.CURRENT_USER_KEY + "/" + docKey + "/attachments/" + delFile).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {

                                                            }
                                                        });
                                                    }

                                                    HashMap<String, Object> upEvent = new HashMap<>();
                                                    upEvent.put("dueDate", dueCal.getTime());
                                                    upEvent.put("startTime", startT.getTime());
                                                    upEvent.put("endTime", endT.getTime());
                                                    upEvent.put("reminderOn", remindMe);
                                                    upEvent.put("reminderTime", remindT.getTime());
                                                    upEvent.put("colorCode", color);
                                                    upEvent.put("note", note);
                                                    upEvent.put("location", location);
                                                    upEvent.put("invitees", invitee_list);
                                                    upEvent.put("inviteTime", inviteT.getTime());
                                                    upEvent.put("invitation", invite);

                                                    FirebaseFirestore.getInstance().collection("Users").document(Main.CURRENT_USER_KEY)
                                                            .collection("Tasks").document(docKey).update(upEvent).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            final boolean allUpload = true;
                                                            if (attachments != null && attachments.size() > 0) {
                                                                int i = 1;
                                                                String extension = "jpg";
                                                                for (Uri uri : attachments) {
                                                                    if (!uri.getPath().startsWith("/v0/b/planner-49228.appspot.com/")) {
                                                                        extension = getApplicationContext().getContentResolver().getType(uri).split("/")[1];
                                                                        Toast.makeText(EventEditor.this, "", Toast.LENGTH_SHORT).show();
                                                                        FirebaseStorage.getInstance().getReference()
                                                                                .child(Main.CURRENT_USER_KEY + "/" + docKey + "/attachments/" + i + "." + extension)
                                                                                .putFile(uri).addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                //allUpload = false;
                                                                            }
                                                                        });
                                                                        i++;
                                                                    }
                                                                }
                                                            }

                                                            if (allUpload) {
                                                                clearEventInterface();
                                                                Snackbar.make(parentView, "Event saved successfully", Snackbar.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    });
                                                }

                                            } else {
                                                Snackbar.make(parentView, "Unable to invite people in given time.Please enter valid invite time.", Snackbar.LENGTH_LONG).show();
                                            }
                                        } else {
                                            Snackbar.make(parentView, "Unable to remind ", Snackbar.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Snackbar.make(parentView, "incorrect time was scheduled", Snackbar.LENGTH_LONG).show();
                                    }
                                } else {
                                    Snackbar.make(parentView, "You cannot schedule event for previous date", Snackbar.LENGTH_LONG).show();
                                }
                            } else {
                                Snackbar.make(parentView, "Please enter start time to update the Event", Snackbar.LENGTH_LONG).show();
                            }
                        } else {
                            Snackbar.make(parentView, "Please enter start time to update the Event", Snackbar.LENGTH_LONG).show();
                        }
                    } else {
                        Snackbar.make(parentView, "Please enter due date to update the Event", Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    Snackbar.make(parentView, "Please enter title to update the Event", Snackbar.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Snackbar.make(parentView, e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        }
    }


    private void saveNewEvent() {
        try {
            String title = eventEditor_title.getText().toString();
            if (title != null && !title.trim().equals("")) {
                String dueDate = eventEditor_dueDate.getText().toString();
                if (dueDate != null && !dueDate.trim().equals("")) {
                    String startTime = eventEditor_startTime.getText().toString();
                    if (startTime != null && !startTime.trim().equals("")) {
                        String endTime = eventEditor_endTime.getText().toString();
                        if (endTime != null && !endTime.trim().equals("")) {
                            Calendar today = Calendar.getInstance();
                            Calendar dueCal = Calendar.getInstance();
                            dueCal.setTime(dateFormatter.parse(dueDate));
                            if (today.before(dueCal) || DateUtils.isToday(dueCal.getTimeInMillis())) {
                                Calendar startT = Calendar.getInstance();
                                startT.setTime(dateFormatter1.parse(dueDate + " " + startTime));
                                Calendar endT = Calendar.getInstance();
                                endT.setTime(dateFormatter1.parse(dueDate + " " + endTime));
                                if (startT.before(endT)) {
                                    boolean remindMe = remind_me_switch.isChecked();
                                    int remindType = this.remindType.getSelectedItemPosition();
                                    boolean reminderOk = true;
                                    Calendar remindT = Calendar.getInstance();
                                    remindT.setTime(dateFormatter1.parse(dueDate + " " + startTime));

                                    if (remindMe) {
                                        switch (remindType) {
                                            case 1:
                                                remindT.add(Calendar.MINUTE, -5);
                                                break;
                                            case 2:
                                                remindT.add(Calendar.MINUTE, -30);
                                                break;
                                            case 3:
                                                remindT.add(Calendar.HOUR_OF_DAY, -1);
                                                break;
                                            case 4:
                                                remindT.add(Calendar.DATE, -1);
                                                break;
                                        }

                                        if (today.after(remindT)) {
                                            reminderOk = false;
                                        }
                                    }

                                    if (reminderOk) {
                                        int inviteType = this.inviteTime.getSelectedItemPosition();
                                        boolean inviteTimeOk = true;
                                        Calendar inviteT = Calendar.getInstance();
                                        inviteT.setTime(startT.getTime());
                                        switch (inviteType) {
                                            case 0:
                                                inviteT.add(Calendar.HOUR_OF_DAY, -1);
                                                break;
                                            case 1:
                                                inviteT.add(Calendar.HOUR_OF_DAY, -6);
                                                break;
                                            case 2:
                                                inviteT.add(Calendar.DATE, -1);
                                                break;
                                            case 3:
                                                inviteT.add(Calendar.DATE, -30);
                                                break;
                                        }

                                        if (inviteT.before(today)) {
                                            inviteTimeOk = false;
                                        } else {
                                            if (inviteT.after(startT)) {
                                                inviteTimeOk = false;
                                            }
                                        }

                                        if (inviteTimeOk) {
                                            String note = eventEditor_note.getText().toString();
                                            String color = getChooseColorCode();
                                            String location = eventEditor_location.getText().toString();
                                            String invite = invitation.getText() + "";
                                            if (Main.CURRENT_USER_KEY != null) {
                                                Event event = new Event(title);
                                                event.setDueDate(dueCal.getTime());
                                                event.setStartTime(startT.getTime());
                                                event.setEndTime(endT.getTime());
                                                event.setReminderOn(remindMe);
                                                event.setReminderTime(remindT.getTime());
                                                event.setColorCode(color);
                                                event.setNote(note);
                                                event.setLocation(location);
                                                event.setInvitees(invitee_list);
                                                event.setInviteTime(inviteT.getTime());
                                                event.setInvitation(invite);
                                                FirebaseFirestore.getInstance().collection("Users").document(Main.CURRENT_USER_KEY)
                                                        .collection("Tasks").add(event).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference reference) {
                                                        final boolean allUpload = true;
                                                        if (attachments != null && attachments.size() > 0) {
                                                            int i = 1;
                                                            String extension = "jpg";
                                                            for (Uri uri : attachments) {
                                                                extension = getApplicationContext().getContentResolver().getType(uri).split("/")[1];
                                                                Toast.makeText(EventEditor.this, "", Toast.LENGTH_SHORT).show();
                                                                FirebaseStorage.getInstance().getReference()
                                                                        .child(Main.CURRENT_USER_KEY + "/" + reference.getId().trim() + "/attachments/" + i + "." + extension)
                                                                        .putFile(uri).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        //allUpload = false;
                                                                    }
                                                                });
                                                                i++;
                                                            }
                                                        }

                                                        if (allUpload) {
                                                            clearEventInterface();
                                                            Snackbar.make(parentView, "Event saved successfully", Snackbar.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                            }

                                        } else {
                                            Snackbar.make(parentView, "Unable to invite people in given time.Please enter valid invite time.", Snackbar.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Snackbar.make(parentView, "Unable to remind ", Snackbar.LENGTH_LONG).show();
                                    }
                                } else {
                                    Snackbar.make(parentView, "incorrect time was scheduled", Snackbar.LENGTH_LONG).show();
                                }
                            } else {
                                Snackbar.make(parentView, "You cannot schedule event for previous date", Snackbar.LENGTH_LONG).show();
                            }
                        } else {
                            Snackbar.make(parentView, "Please enter start time to save the Event", Snackbar.LENGTH_LONG).show();
                        }
                    } else {
                        Snackbar.make(parentView, "Please enter start time to save the Event", Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    Snackbar.make(parentView, "Please enter due date to save the Event", Snackbar.LENGTH_LONG).show();
                }
            } else {
                Snackbar.make(parentView, "Please enter title to save the Event", Snackbar.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Snackbar.make(parentView, e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    private void clearEventInterface() {
        eventEditor_title.setText(null);
        eventEditor_dueDate.setText(null);
        eventEditor_startTime.setText("08:00 AM");
        eventEditor_endTime.setText("10:00 PM");
        eventEditor_reminder_spinner.setVisibility(View.GONE);
        remind_me_switch.setChecked(false);
        eventEditor_location.setText(null);
        attachments.clear();
        adapter.notifyDataSetChanged();
        eventEditor_note.setText(null);
        invitee_list.clear();
        invitation.setText(null);
        inviteTime.setSelection(0);
        contactAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == PICK_FILE_RESULT_CODE) {
            ClipData clipData = data.getClipData();
            if (clipData != null) {
                int count = clipData.getItemCount();
                int currentItem = 0;
                while (count > currentItem) {
                    Uri uri = clipData.getItemAt(currentItem).getUri();
                    String src = uri.getPath();
                    attachments.add(uri);
                    currentItem++;
                }
            } else {
                Uri uri = data.getData();
                attachments.add(uri);
            }
            adapter.notifyDataSetChanged();
        } else if (resultCode == RESULT_OK && requestCode == 29) {
            Uri contactData = data.getData();
            String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};

        } else if (resultCode == RESULT_OK && requestCode == PICK_CONTACT) {
            if (data != null) {
                Uri uri = data.getData();

                if (uri != null) {
                    Cursor c = null;
                    try {
                        c = getContentResolver().query(uri, new String[]{
                                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},
                                null, null, null);

                        if (c != null && c.moveToFirst()) {
                            String number = c.getString(0);
                            String name = c.getString(1);
                            addToInvitees(name, number);
                        }
                    } finally {
                        if (c != null) {
                            c.close();
                        }
                    }
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void addToInvitees(String name, String number) {
        invitee_list.add(new Contact(name, number));
        contactAdapter.notifyDataSetChanged();
    }

    public void setChooseColorCode(String chooseColorCode) {
        this.chooseColorCode = chooseColorCode;
    }

    public String getChooseColorCode() {
        return chooseColorCode;
    }

    public void setInvitation(String text) {
        invitation.setText(text);
    }
}
