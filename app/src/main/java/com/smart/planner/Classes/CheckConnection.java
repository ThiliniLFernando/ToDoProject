package com.smart.planner.Classes;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.smart.planner.POJOs.List;
import com.smart.planner.LocalDB.SQLiteHelper;

import java.util.TimerTask;

public class CheckConnection extends TimerTask {
    private Context context ;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance() ;
    private DatabaseReference databaseReference = firebaseDatabase.getReference() ;

    private SQLiteHelper sqLiteHelper ;
    private SQLiteDatabase sqLiteDatabase;

    private Cursor cursorList ;
    private Cursor cursorTask ;

    SharedPreferences preferences ;
    String CURRENT_USER_KEY ;

    List list ;

    public CheckConnection(Context context){
        this.context = context ;
    }

    @Override
    public void run() {
        if (NetworkUtils.isNetworkAvailable(context)){
            System.out.println("Connected");
            preferences = PreferenceManager.getDefaultSharedPreferences(context);
            CURRENT_USER_KEY = preferences.getString("current_user_KEY", null);
            if (CURRENT_USER_KEY != null){
                System.out.println(CURRENT_USER_KEY);
                sqLiteHelper = new SQLiteHelper(context);
                sqLiteDatabase = sqLiteHelper.getWritableDatabase();
                cursorList = sqLiteDatabase.rawQuery("SELECT * FROM list WHERE isSync = '0'",null);
                while (cursorList.moveToNext()) {
                    final String listName = DatabaseUtils.sqlEscapeString(cursorList.getString(cursorList.getColumnIndex("listName")));
                    databaseReference.child("lists").child(CURRENT_USER_KEY)
                            .child(listName)
                            .setValue(cursorList.getString(cursorList.getColumnIndex("listColor")))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    sqLiteDatabase.execSQL("UPDATE list SET isSync = '1' WHERE listName = "+listName+" ");
                                }
                            });
                }
            }
        }else {
            System.out.println("Not Connected");
        }
    }
}
