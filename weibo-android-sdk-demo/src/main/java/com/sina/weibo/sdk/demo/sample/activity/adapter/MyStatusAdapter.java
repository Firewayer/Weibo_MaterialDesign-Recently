package com.sina.weibo.sdk.demo.sample.activity.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import com.jaeger.ninegridimageview.NineGridImageView;
import com.sina.weibo.sdk.demo.R;
import com.sina.weibo.sdk.demo.sample.activity.CommentsActivity;
import com.sina.weibo.sdk.demo.sample.activity.UserActivity_;
import com.sina.weibo.sdk.openapi.models.Status;
import com.squareup.picasso.Transformation;

import java.util.Date;
import java.util.List;

/**
 * Created by wayne on 2015/8/1.
 */


public class MyStatusAdapter extends CommonAdapter<Status> {


    Context mContext;

    public MyStatusAdapter(Context context, int textResourceId, List<Status> datas) {
        super(context, textResourceId, datas);
        this.mContext = context;
    }

    @Override
    public void setView(ViewHolder holder, final Status status) {
        //主微博内容
        /**
         * User头像变换参数
         */
        Transformation userTransformation = new Transformation() {
            @Override
            public Bitmap transform(Bitmap bitmap) {
                Bitmap output = null;
                float roundPx = 8.8f;
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
        };

        holder.setImageURI(R.id.img_wb_item_head, status.user.profile_image_url, userTransformation);
        holder.getView(R.id.id_btn_userhead).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                UserActivity_.actionStart(mContext, status.user.idstr,
                        status.user.profile_image_url, status.user.name,
                        status.user.description, status.user.followers_count,
                        status.user.friends_count, status.user.statuses_count);
            }
        });



        if (status.pic_urls != null) {
            NineGridImageView nineGridImageView = holder.getView(R.id.img_wb_item_content);
            nineGridImageView.setAdapter(new MyNineGridImageViewAdapter() {
                @Override
                protected void onItemImageClick(Context context, int index, List<String> list) {
//ShowLargePicture;
                }
            });
            nineGridImageView.setImagesData(status.pic_urls);
            holder.setVisiblity(R.id.img_wb_item_content, ViewHolder.TYPE_NINE, View.VISIBLE);
        } else {
            holder.setVisiblity(R.id.img_wb_item_content, ViewHolder.TYPE_NINE, View.GONE);// 一条属性，解决无中生有
        }


        holder.getView(R.id.txt_wb_item_comment_img).setOnClickListener(new View.OnClickListener() {
            // 评论方法示例
            @Override
            public void onClick(View v) {
                if (status.pic_urls != null) {
                    CommentsActivity.actionStart(mContext, status.idstr,
                            status.user.name, status.created_at, status.text,
                            status.user.profile_image_url, status.pic_urls,
                            String.valueOf(status.attitudes_count),
                            String.valueOf(status.comments_count),
                            String.valueOf(status.reposts_count));
                } else {
                    CommentsActivity.actionStart(mContext, status.idstr,
                            status.user.name, status.created_at, status.text,
                            status.user.profile_image_url, null,
                            String.valueOf(status.attitudes_count),
                            String.valueOf(status.comments_count),
                            String.valueOf(status.reposts_count));
                }
            }
        });

        holder.setText(R.id.txt_wb_item_unlike, String
                .valueOf(status.attitudes_count));
        holder.setText(R.id.txt_wb_item_comment, String
                .valueOf(status.comments_count));
        holder.setText(R.id.txt_wb_item_redirect,String.valueOf(status.reposts_count));
       holder.setText(R.id.txt_wb_item_from, " " + Html.fromHtml(status.source));
       holder.setText(R.id.txt_wb_item_time, dealTime(status.created_at));
        holder.setText(R.id.txt_wb_item_content,setTextColor(status.text));
        holder.setText(R.id.txt_wb_item_uname,status.user.name);

        //子微博内容
        if (status.retweeted_status != null) { // Visibility属性设置，解决子微博内容偶尔消失的Bug
            holder.setVisiblity(R.id.lyt_wb_item_sublayout, ViewHolder.TYPE_LAYOUT, View.VISIBLE).setText(R.id.txt_wb_item_subcontent, setTextColor(status.retweeted_status.text));
            if (status.retweeted_status.pic_urls != null) {
                holder.setImageURI(R.id.img_wb_item_content_subpic, status.retweeted_status.thumbnail_pic).setVisiblity(R.id.img_wb_item_content_subpic, ViewHolder.TYPE_IMAGE, View.VISIBLE);
            } else {
                holder.setVisiblity(R.id.img_wb_item_content_subpic, ViewHolder.TYPE_IMAGE, View.GONE);
            }
        } else {
            holder.setVisiblity(R.id.lyt_wb_item_sublayout, ViewHolder.TYPE_LAYOUT, View.GONE);
        }

    }


    @SuppressWarnings("deprecation")
    public String dealTime(String time)// 用于处理微博的发布时间
    {
        Date now = new Date();
        long lnow = now.getTime() / 1000;
        long ldate = Date.parse(time) / 1000;
//        Date date = new Date(ldate);
        if ((lnow - ldate) < 60)
            return (lnow - ldate) + "秒前";
        else if ((lnow - ldate) < 60 * 60)
            return ((lnow - ldate) / 60) + "分钟前";
        else
            return time.substring(4, 16);
    }


    public SpannableStringBuilder setTextColor(String str) {
        // 将用户名和话题名变成蓝色
        int bstart = 0;
        int bend = 0;
        int fstart = 0;
        int fend = 0;
        int a = 0;
        int b = 0;
        int c = 0;
        SpannableStringBuilder style = new SpannableStringBuilder(str);
        while (true) {
            bstart = str.indexOf("@", bend);
            a = str.indexOf(" ", bstart);
            c = str.indexOf(":", bstart);
            a = a < c ? a : c;
            if (bstart < 0) {
                break;
            } else {
                if (a < 0) {
                    break;
                } else {
                    bend = a;
                }
                style.setSpan(new ForegroundColorSpan(0xFF0099ff), bstart, a,
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }
        }
        while (true) {
            fstart = str.indexOf("#", fend);
            b = str.indexOf("#", fstart + 1);
            if (fstart < 0) {
                break;
            } else {
                if (b < 0) {
                    break;
                } else {
                    fend = b + 1;
                }
                style.setSpan(new ForegroundColorSpan(0xFF0099ff), fstart,
                        fend, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }
        }
        return style;
    }
}


