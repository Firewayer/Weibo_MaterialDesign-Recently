package com.sina.weibo.sdk.demo.sample.util;

import android.app.Application;
import android.content.Context;

import com.sina.weibo.sdk.demo.sample.db.DatabaseHelper;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

public class MyApplication extends Application {

	private static Context context;
	private static DatabaseHelper dbHelper;
	private static OkHttpClient okHttpClient;
	@Override
	public void onCreate() {
		super.onCreate();
		context = getApplicationContext();
		dbHelper = new DatabaseHelper();
		okHttpClient = new OkHttpClient();
		okHttpClient.setConnectTimeout(8, TimeUnit.SECONDS);
		Picasso.Builder builder = new Picasso.Builder(this);
		builder.downloader(new OkHttpDownloader(okHttpClient));
		Picasso built = builder.build();
		built.setIndicatorsEnabled(true);
		built.setLoggingEnabled(true);
		Picasso.setSingletonInstance(built);
	}

	public static Context getContext() {
		return context;
	}
	static DatabaseHelper getDatabaseHelper(){
		return dbHelper;
	}

}
