package com.sina.weibo.sdk.demo.sample.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.sina.weibo.sdk.demo.sample.util.Utils;

/**
 * Created by Firewayer on 2016/2/29.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String CREATE_BOOK = "create table status (" + "id integer primary key autoincrement, " + "name text, " + "gender text, " + "location text, " + "description text, " + "followers_count integer, " + "friends_count integer, " + "statuses_count integer, " + "main_content text, " + "created_at text, " + "source text, " + "sub_status integer, " + "sub_name text, " + "sub_content text)";
    public static int VERSION = 1;

    public static String DATABASE_NAME = "Status.db";
    public DatabaseHelper() {
        super(Utils.getApplicationContext(), DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_BOOK);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
