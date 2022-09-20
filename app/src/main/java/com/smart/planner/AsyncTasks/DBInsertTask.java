package com.smart.planner.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import com.smart.planner.LocalDB.DAO.NotificationReminderDAO;
import com.smart.planner.LocalDB.LocalDB;
import com.smart.planner.LocalDB.NotificationReminder;
import com.smart.planner.POJOs.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DBInsertTask extends AsyncTask<Task, Void, Void> {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private Context context;

    public DBInsertTask(Context context){
        this.context = context;
    }

    @Override
    protected Void doInBackground(Task... tasks) {
        Task task = tasks[0];
        NotificationReminderDAO dao =
                LocalDB.getInstance(context).notificationReminderDAO();
        List<NotificationReminder> rAll = new ArrayList<>();
        for(Date date : task.getReminders()) {
            NotificationReminder nr = new NotificationReminder(
                    task.getDocumentId(),
                    date.getTime(),
                    task.getTaskName(),
                    task.getListName());
            rAll.add(nr);
        }
        dao.insertAll(rAll);
        return null;
    }

}
