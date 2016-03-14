package com.sina.weibo.sdk.demo.sample.Config;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import com.squareup.picasso.Transformation;

/**
 * Created by Firewayer on 2016/2/29.
 */
public class CircleTransformation implements Transformation {
    @Override
    public Bitmap transform(Bitmap bitmap) {
        Bitmap output = null;
        float roundPx = 15.8f;
        // 自定义变换
        output = Bitmap.createBitmap(bitmap.getWidth(), bitmap

                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(),
                bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        if (bitmap != null && !bitmap.isRecycled()) { // 将旧图片回收
            bitmap.recycle();
        }
        return output;
    }

    @Override
    public String key() { // 将用作cache的key
        return "key";
    }
}
