package com.sina.weibo.sdk.demo.sample.util;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.sina.weibo.sdk.demo.sample.db.DatabaseHelper;

/**
 * Created by Firewayer on 2016/2/28.
 */
public class Utils {

    public static DatabaseHelper getDatabaseHelper() {
        return MyApplication.getDatabaseHelper();
    }

    public static Context getApplicationContext(){
        return MyApplication.getContext();
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }


}
