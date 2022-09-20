package com.smart.planner;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.fragment.app.DialogFragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.smart.planner.POJOs.List;
import com.smart.planner.LocalDB.SQLiteHelper;

import java.util.HashMap;
import java.util.Map;

public class DialogNewList extends DialogFragment {

    final private static int REQUEST_CODE = 100;
    public View thisView;
    // color code
    public String selected_color_code;
    // sqlite database
    SQLiteHelper sqLiteHelper;
    SQLiteDatabase sqLiteDatabase;
    // fire store
    FirebaseFirestore firestore;

    private String CURRENT_USER_KEY;

    private TextView newList_title;
    private EditText newList_name;
    private TextView newList_colorPicker;
    private Dialog thisDialog;
    private ColorPicker colorPicker;
    private DialogNewList dialogNewList;

    // shared preferences
    private SharedPreferences preferences;

//    private GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
//            new int[]{0xffff, 0xffff});

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        thisView = inflater.inflate(R.layout.dialog_add_new_list, null);
        dialogNewList = this;
        builder.setView(thisView);
        initComponents(thisView, builder);
        thisDialog = builder.create();
        return thisDialog;
    }

    private void initComponents(View view, AlertDialog.Builder builder) {
        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        CURRENT_USER_KEY = preferences.getString("current_user_KEY", null);
        firestore = FirebaseFirestore.getInstance();

        selected_color_code = "#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.color_picker_default));
        sqLiteHelper = new SQLiteHelper(getContext());
        sqLiteDatabase = sqLiteHelper.getWritableDatabase();

        newList_name = view.findViewById(R.id.new_list_name);
        newList_colorPicker = view.findViewById(R.id.new_list_colorPicker);
        newList_title = view.findViewById(R.id.new_list_title);

        // color picker button code
        newList_colorPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (colorPicker == null) {
                    colorPicker = new ColorPicker();
                    colorPicker.setTargetFragment(dialogNewList, DialogNewList.REQUEST_CODE);
                }
                colorPicker.show(getParentFragmentManager(), "color_picker");
            }
        });

        // Bundle Access
        final Bundle bundle = getArguments();
        if (bundle != null) {
            builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (CURRENT_USER_KEY != null && bundle.getString("list_document_id", null) != null) {
                        String newList = newList_name.getText().toString().trim();
                        if (!TextUtils.isEmpty(newList)) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("listName", newList);
                            map.put("listColor", DialogNewList.this.selected_color_code);
                            firestore.collection("Users").document(CURRENT_USER_KEY)
                                    .collection("Lists")
                                    .document(bundle.getString("list_document_id"))
                                    .update(map);
                            dismiss();
                        }
                    } else {

                    }
                }
            });

            newList_title.setText("Update List");
            newList_name.setText(bundle.getString("list_name"));
            DialogNewList.this.selected_color_code = bundle.getString("list_color");

            //((TextView)view.findViewById(R.id.new_list_colorPicker)).getCompoundDrawables()[0].setTint(Color.parseColor(bundle.getString("list_color")));
            //System.out.println("Color "+bundle.getString("list_color"));
        } else {
            builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String newList = newList_name.getText().toString().trim();
                    // Add new list
                    if (CURRENT_USER_KEY != null) {
                        firestore.collection("Users").document(CURRENT_USER_KEY)
                                .collection("Lists").add(
                                new List(newList, DialogNewList.this.selected_color_code));
                        dismiss();
                    } else {
                        // sqlite
                        if (!TextUtils.isEmpty(newList)) {
                            sqLiteDatabase.execSQL("INSERT INTO list (\n" +
                                    "                     listName,\n" +
                                    "                     listColor,\n" +
                                    "                     isSync\n" +
                                    "                 )\n" +
                                    "                 VALUES (\n" +
                                    "                     " + DatabaseUtils.sqlEscapeString(newList) + ",\n" +
                                    "                     '" + DialogNewList.this.selected_color_code + "',\n" +
                                    "                     '" + 0 + "'\n" +
                                    "                 )");
                            thisDialog.dismiss();
                            if (getTargetFragment() != null) {
                                if (getTargetFragment() instanceof FragmentList) {
                                    //((FragmentList)getTargetFragment()).loadListData();
                                }
                            }
                        }
                    }
                }
            });
        }

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                thisDialog.dismiss();
            }
        });
    }
}
