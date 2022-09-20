package com.smart.planner.Dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private static SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
    private TextView textView ;

    public TimePickerFragment(TextView view){
        textView = view ;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getActivity(),
                this,
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE),
                DateFormat.is24HourFormat(getActivity()));

        return timePickerDialog ;
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        textView.setText(timeFormat.format(c.getTime()));
    }
}
