package com.smart.planner.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import com.smart.planner.LocalDB.DAO.NotificationReminderDAO;
import com.smart.planner.LocalDB.LocalDB;
import com.smart.planner.LocalDB.NotificationReminder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DBUpdateTask extends AsyncTask<HashMap<String,Object>,Void,Void> {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private Context context ;

    public DBUpdateTask(Context context){
        this.context = context;
    }

    @Override
    protected Void doInBackground(HashMap<String, Object>... maps) {
        HashMap<String,Object> map = maps[0];
        if (map != null){
            NotificationReminderDAO dao =
                    LocalDB.getInstance(context).notificationReminderDAO();
            ArrayList<Date> dates = (ArrayList<Date>) map.get("reminders");
            for (Date date : dates){
                dao.updateReminderByDocId(
                        (String) map.get("documentId"),
                        (String) map.get("taskName"),
                        date.getTime(),
                        "");
            }
        }
        return null;
    }
}
