package com.sina.weibo.sdk.demo.sample.Config;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.sina.weibo.sdk.demo.sample.widget.ZoomImageView;
import com.squareup.picasso.Transformation;

/**
 * Created by Firewayer on 2016/2/29.
 */
public class ZoomTransformation implements Transformation {

    @Override
    public Bitmap transform(Bitmap bitmap) {
        Bitmap newBitmap = null;
        // 自定义变换
        if (bitmap.getHeight() > 500) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int newWidth = 400;
            int newHeight = 500;
            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width,
                    height, matrix, true);
            if (bitmap != null && !bitmap.isRecycled()) { // 将旧图片回收
                bitmap.recycle();
            }
        } else {
            newBitmap = bitmap;
        }

        return newBitmap;
    }

    @Override
    public String key() { // 将用作cache的key
        return "key";
    }
}
