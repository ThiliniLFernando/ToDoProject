package com.smart.planner.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import com.smart.planner.LocalDB.DAO.NotificationReminderDAO;
import com.smart.planner.LocalDB.LocalDB;

public class DBDeleteTask extends AsyncTask<String,Void,Void> {

    private Context context ;

    public DBDeleteTask(Context context){
        this.context = context;
    }

    @Override
    protected Void doInBackground(String... strings) {
        String docId = strings[0];
        NotificationReminderDAO dao =
                LocalDB.getInstance(context).notificationReminderDAO();
        dao.deleteByDocId(docId);
        return null;
    }
}
