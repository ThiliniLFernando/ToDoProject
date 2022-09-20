package com.smart.planner.Dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.TextView;

import com.smart.planner.Classes.PatternUtils;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private TextView date;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

    public DatePickerFragment(TextView date) {
        this.date = date;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar calendar = Calendar.getInstance();
        if (!date.getText().toString().isEmpty() && PatternUtils.isValidDate(date.getText())) {
            try {
                Date d = dateFormat.parse(date.getText().toString());
                assert d != null;
                calendar.setTime(d);
            } catch (Exception exp) {
                exp.printStackTrace();
            }
        }
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getActivity(),
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        return datePickerDialog;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        date.setText(dateFormat.format(calendar.getTime()));
    }
}
