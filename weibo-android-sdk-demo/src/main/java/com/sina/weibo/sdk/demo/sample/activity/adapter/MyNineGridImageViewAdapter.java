package com.sina.weibo.sdk.demo.sample.activity.adapter;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.jaeger.ninegridimageview.NineGridImageViewAdapter;
import com.sina.weibo.sdk.openapi.models.Status;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Firewayer on 2016/3/7.
 */
public abstract class MyNineGridImageViewAdapter extends NineGridImageViewAdapter<String> {
    @Override
    protected void onDisplayImage(Context context, ImageView imageView, String s) {
        Picasso.with(context).load(s.replace("thumbnail", "bmiddle")).into(imageView);
        Log.d("图片地址", s);
    }

    @Override
    abstract protected void onItemImageClick(Context context, int index, List<String> list);
}
