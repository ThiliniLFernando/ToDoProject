package com.smart.planner.Classes;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

public class NetworkUtils {
    public static int TYPE_WIFI = 1 ;
    public static int TYPE_MOBILE_DATA = 2 ;
    public static int TYPE_NOT_CONNECTED = 3 ;
    public static boolean STATUS = false ;

    public static int checkNetworkStatus(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        Network network = cm.getActiveNetwork();
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
        if (capabilities != null){
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)){
                return TYPE_WIFI ;
            }else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)){
                return TYPE_MOBILE_DATA ;
            }
        }
        return TYPE_NOT_CONNECTED ;
    }

    public static boolean isNetworkAvailable(Context context){
        int i = NetworkUtils.checkNetworkStatus(context);
        if (i==NetworkUtils.TYPE_MOBILE_DATA || i==NetworkUtils.TYPE_WIFI){
            STATUS = true ;
        }else {
            STATUS = false ;
        }
        return STATUS ;
    }




}
