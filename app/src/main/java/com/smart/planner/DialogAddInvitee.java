package com.smart.planner;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;

public class DialogAddInvitee extends DialogFragment {
    private EventEditor eventEditor;

    public DialogAddInvitee(EventEditor ev) {
        eventEditor = ev;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_invitee_box, null);
        final TextInputEditText inviteeName = view.findViewById(R.id.invitee_name);
        final TextInputEditText inviteeNum = view.findViewById(R.id.invitee_number);
        builder.setView(view)
                // Add action buttons
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String name = inviteeName.getEditableText().toString();
                        String number = inviteeNum.getEditableText().toString();
                        if (name != null && !name.trim().equals("")) {
                            if (number != null && !number.trim().equals("")) {
                                eventEditor.addToInvitees(name, number);
                            }else{
                                Toast.makeText(eventEditor, "Please enter invitee contact number", Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            Toast.makeText(eventEditor, "Please enter invitee name", Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DialogAddInvitee.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}
