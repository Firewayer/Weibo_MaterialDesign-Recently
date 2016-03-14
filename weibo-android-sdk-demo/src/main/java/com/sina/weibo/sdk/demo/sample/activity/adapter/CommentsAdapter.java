package com.sina.weibo.sdk.demo.sample.activity.adapter;

import android.content.Context;

import com.sina.weibo.sdk.demo.R;
import com.sina.weibo.sdk.openapi.models.Comment;

import java.util.List;

public class CommentsAdapter extends CommonAdapter<Comment> {
    public CommentsAdapter(Context context, int textResourceId, List<Comment> datas) {
        super(context, textResourceId, datas);
    }

    @Override
    public void setView(ViewHolder holder, Comment comment) {
        holder.setImageURI(R.id.img_comment_item_head, comment.user.profile_image_url);
        holder.setText(R.id.txt_comment_item_uname, comment.user.name)
				.setText(R.id.txt_comment_item_day, comment.created_at)
				.setText(R.id.txt_comment_item_content, comment.text);
    }
}
