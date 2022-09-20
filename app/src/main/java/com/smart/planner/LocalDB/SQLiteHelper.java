package com.smart.planner.LocalDB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {

    public SQLiteHelper(Context context){
        super(context,"Planner_DB",null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {

        database.execSQL("CREATE TABLE Profile (\n" +
                "    userKey TEXT PRIMARY KEY\n" +
                "                 NOT NULL,\n" +
                "    img     BLOB\n" +
                ")");

    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int i, int i1) {
        // if version change
        System.out.println("On Upgrade");
        database.execSQL("DROP TABLE Profile");
        onCreate(database);
    }
}
