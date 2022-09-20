package com.smart.planner;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.smart.planner.Adapters.GroceryItemViewAdapter;
import com.smart.planner.POJOs.LocationReminder;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BottomSheetAddLocationReminder extends BottomSheetDialogFragment {

    private View mView;
    private Button mAddListItem, mAddReminder;
    private TextView mCancel;
    private EditText mReminderTitle, mNewGrocery;
    private RelativeLayout mRelativeLayout;
    private RecyclerView mRecycleView;

    protected GroceryItemViewAdapter groceryItemsViewAdapter;
    private RecyclerView.LayoutManager recycleViewLayoutManager;

    FirebaseFirestore firestore;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_add_location_reminder, null);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mView = view;
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();

        recycleViewLayoutManager = new LinearLayoutManager(getActivity());
        mRecycleView.setHasFixedSize(true);
        mRecycleView.setLayoutManager(recycleViewLayoutManager);
        groceryItemsViewAdapter = new GroceryItemViewAdapter(new ArrayList<String>());
        mRecycleView.setAdapter(groceryItemsViewAdapter);

        mAddListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ADD GROCERY ITEMS
                if (!mNewGrocery.getText().toString().trim().matches("")) {
                    groceryItemsViewAdapter.addGroceryItem(mNewGrocery.getEditableText().toString());
                    mNewGrocery.setText(null);
                }
                // ADD GROCERY ITEMS
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        mAddReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mReminderTitle.getText().toString().trim().matches("")) {
                    addLocationReminder();
                }
            }
        });

    }

    private void addLocationReminder() {
        Bundle bundle = getArguments();
        if (!bundle.isEmpty()) {
            String title = mReminderTitle.getText().toString().trim();
            if (!title.matches("")) {
                LocationReminder locReminder = new LocationReminder(
                        title,
                        bundle.getDouble("placeLatitude"),
                        bundle.getDouble("placeLongitude")
                );
                locReminder.setGroceryList((ArrayList<String>) groceryItemsViewAdapter.getGroceryList());

                firestore.collection("Users").document(Main.CURRENT_USER_KEY)
                        .collection("LocationReminder").add(locReminder)
                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                mNewGrocery.setText(null);
                                groceryItemsViewAdapter.clearGroceryList();
                                Toast.makeText(getContext(), "Location Reminder Added !", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

            } else {
                Toast.makeText(getContext(), "Please write what do you want to remind ?", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Location not properly selected !", Toast.LENGTH_SHORT).show();
        }
    }

    private void init() {
        mAddListItem = mView.findViewById(R.id.add_list_item);
        mAddReminder = mView.findViewById(R.id.add_reminder);
        mCancel = mView.findViewById(R.id.cancel_reminder);
        mReminderTitle = mView.findViewById(R.id.reminder_title);
        mNewGrocery = mView.findViewById(R.id.new_grocery);
        mRelativeLayout = mView.findViewById(R.id.recycler_view_container);
        mRecycleView = mView.findViewById(R.id.recycler_view);
        firestore = FirebaseFirestore.getInstance();
    }

}
