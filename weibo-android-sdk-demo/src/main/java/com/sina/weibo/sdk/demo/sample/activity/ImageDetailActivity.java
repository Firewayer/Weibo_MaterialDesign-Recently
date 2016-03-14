package com.sina.weibo.sdk.demo.sample.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import com.sina.weibo.sdk.demo.R;
import com.sina.weibo.sdk.demo.sample.util.Utils;
import com.sina.weibo.sdk.demo.sample.widget.ZoomImageView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by Firewayer on 2016/3/1.
 */
public class ImageDetailActivity extends AppCompatActivity{
    private ZoomImageView zoomImageView;
    public static void actionStart(Context context, String url){
        Intent intent = new Intent(context, ImageDetailActivity.class);
        intent.putExtra("pic_url", url);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.img_large);
        zoomImageView = (ZoomImageView) findViewById(R.id.id_iv_large);
        Picasso.with(Utils.getApplicationContext()).load(getIntent().getStringExtra("pic_url")).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                zoomImageView.setImageBitmap(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });



    }
}
