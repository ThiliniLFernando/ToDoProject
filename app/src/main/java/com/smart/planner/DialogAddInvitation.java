package com.smart.planner;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.HashSet;

import io.opencensus.internal.StringUtils;

public class DialogAddInvitation extends DialogFragment {

    private EventEditor eventEditor;
    private TextView title,date,sTime,eTime,invitee,note,location;
    private EditText editText ;

    public DialogAddInvitation(EventEditor ev) {
        eventEditor = ev;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle args) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_invitation_box, null);
        initComponents(view,getArguments());
        builder.setView(view)
                // Add action buttons
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        eventEditor.setInvitation(editText.getText().toString());
                        DialogAddInvitation.this.getDialog().cancel();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DialogAddInvitation.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    private void initComponents(View v,Bundle args) {
        title = v.findViewById(R.id.hashcode_eventTitle);
        date = v.findViewById(R.id.hashcode_dueDate);
        sTime = v.findViewById(R.id.hashcode_startTime);
        eTime = v.findViewById(R.id.hashcode_endTime);
        location = v.findViewById(R.id.hashcode_location);
        invitee = v.findViewById(R.id.hashcode_invitee);
        note = v.findViewById(R.id.hashcode_note);
        editText = v.findViewById(R.id.editTextTextMultiLine);

        if (args != null){
            String str = (String) args.get("invitation");
            char[] chars = str.toCharArray();
            boolean colored = false;
            for (int i = 0 ; i < chars.length; i++){
                if (chars[i] == '#'){
                    modifyInvitation(chars[i]+"");
                    colored = true;
                }else if(chars[i] == '_'){
                    modifyInvitation(chars[i]+"");
                    colored = false;
                }else {
                    if (colored){
                        modifyInvitation(chars[i]+"");
                    }else {
                        editText.append(chars[i] + "");
                    }
                }
            }
        }else {
            editText.append("No Bundle Data");
        }

        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modifyInvitation(title.getText().toString()+" ");
            }
        });

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modifyInvitation(date.getText().toString()+" ");
            }
        });

        sTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modifyInvitation(sTime.getText().toString()+" ");
            }
        });

        eTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modifyInvitation(eTime.getText().toString()+" ");
            }
        });

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modifyInvitation(location.getText().toString()+" ");
            }
        });

        invitee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modifyInvitation(invitee.getText().toString()+" ");
            }
        });

        note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modifyInvitation(note.getText().toString()+" ");
            }
        });

    }

    private void modifyInvitation(String nStr) {
        Spannable specifyW = new SpannableString(nStr);
        specifyW.setSpan(new ForegroundColorSpan(Color.BLUE), 0, nStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        editText.append(specifyW);
    }
}
