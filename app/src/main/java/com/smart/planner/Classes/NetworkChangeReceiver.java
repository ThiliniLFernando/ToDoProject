package com.smart.planner.Classes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;


public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (NetworkUtils.isNetworkAvailable(context)){

        }else {
            Toast.makeText(context, "Not Connected", Toast.LENGTH_SHORT).show();
        }
    }
}
