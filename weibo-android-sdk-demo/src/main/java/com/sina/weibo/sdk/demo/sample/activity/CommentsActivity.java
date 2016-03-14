package com.sina.weibo.sdk.demo.sample.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jaeger.ninegridimageview.NineGridImageView;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.demo.R;
import com.sina.weibo.sdk.demo.sample.activity.adapter.MyNineGridImageViewAdapter;
import com.sina.weibo.sdk.demo.sample.db.AccessTokenKeeper;
import com.sina.weibo.sdk.demo.sample.activity.adapter.CommentsAdapter;
import com.sina.weibo.sdk.demo.sample.db.Constants;
import com.sina.weibo.sdk.demo.sample.widget.ZoomImageView;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.CommentsAPI;
import com.sina.weibo.sdk.openapi.models.Comment;
import com.sina.weibo.sdk.openapi.models.CommentList;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class CommentsActivity extends Activity implements ZoomImageView.IZoomViewListener{
    private ArrayList<Comment> mComments;
    private CommentsAdapter mCommentsAdapter;
    private ListView mListView;
    private TextView mTextView;
    /** 当前 Token 信息 */
    private Oauth2AccessToken mAccessToken;
    /** 微博评论接口 */
    private CommentsAPI mCommentsAPI;

    private String statusId;
    String numberOfpinglun;
    int intNumberOfpinglun;

    private EditText writeComment;
    private TextView txt_zhuanfa;
    private TextView txt_pinglun;
    private AlertDialog alertDialog;
    private ZoomImageView.IZoomViewListener iZoomViewListener;
    MyNineGridImageViewAdapter nineGridImageViewAdapter = new MyNineGridImageViewAdapter(){


        @Override
        protected void onItemImageClick(Context context, int index, List<String> list) {
            showLargeImg(context, list.get(index).replace("thumbnail", "large"));
        }
    };
    public void showLargeImg(final Context context, final String url) {
        View view = LayoutInflater.from(context).inflate(R.layout.img_large, null);
        final ZoomImageView zoomImageView = (ZoomImageView) view.findViewById(R.id.id_iv_large);
        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.id_pb_load);
        alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setView(view);
        alertDialog.show();
        progressBar.setVisibility(View.VISIBLE);

        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Log.d("图片", "成功" + bitmap.toString());
                progressBar.setVisibility(View.GONE);
                zoomImageView.setImageBitmap(bitmap, iZoomViewListener);
                zoomImageView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                Log.d("图片", "onPrepareLoad");
            }
        };
        zoomImageView.setTag(target);
        Picasso.with(context).load(url).into(target);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    public static void actionStart(Context context, String statusId,
                                   String name, String time, String content, String uriHead,
                                   List<String> uriContent, String numberOfdianzan, String numberOfpinglun,
                                   String numberOfzhuanfa) {
        Intent intent = new Intent(context, CommentsActivity.class);
        intent.putExtra("statusId", statusId);
        intent.putExtra("statusName", name);
        intent.putExtra("statusTime", time);
        intent.putExtra("statusContent", content);
        intent.putExtra("statusUriHead", uriHead);
        intent.putExtra("uriContent", (Serializable) uriContent);
        intent.putExtra("numberOfdianzan", numberOfdianzan);
        intent.putExtra("numberOfpinglun", numberOfpinglun);
        intent.putExtra("numberOfzhuanfa", numberOfzhuanfa);

        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_comments);
        init();
    }

    private void init() {
        iZoomViewListener = this;
        mAccessToken = AccessTokenKeeper.readAccessToken(this);
        mCommentsAPI = new CommentsAPI(this, Constants.APP_KEY, mAccessToken);
        Intent intent = getIntent();
        final String statusId = intent.getStringExtra("statusId");
        String statusUriHead = intent.getStringExtra("statusUriHead");
        List<String> statusUriContent = (List<String>) intent.getSerializableExtra("uriContent");
        String statusName = intent.getStringExtra("statusName");
        String statusTime = intent.getStringExtra("statusTime");
        String statusContent = intent.getStringExtra("statusContent");
        String numberOfdianzan = intent.getStringExtra("numberOfdianzan");
        numberOfpinglun = intent.getStringExtra("numberOfpinglun");
        String numberOfzhuanfa = intent.getStringExtra("numberOfzhuanfa");


        // 原微博部分
        TextView name = (TextView) findViewById(R.id.txt_wb_item_uname);
        TextView time = (TextView) findViewById(R.id.txt_wb_item_time);
        TextView content = (TextView) findViewById(R.id.txt_wb_item_content);
        mListView = (ListView) findViewById(R.id.id_list_comments);
        name.setText(statusName);
        time.setText(statusTime);
        content.setText(statusContent);
        LinearLayout subStatusLayout = (LinearLayout) findViewById(R.id.lyt_wb_item_sublayout);
        subStatusLayout.setVisibility(View.GONE);

        // number
        TextView txt_dianzan = (TextView) findViewById(R.id.txt_wb_item_unlike);
        txt_pinglun = (TextView) findViewById(R.id.txt_wb_item_comment);
        txt_zhuanfa = (TextView) findViewById(R.id.txt_wb_item_redirect);
        txt_dianzan.setText(numberOfdianzan);
        txt_pinglun.setText(numberOfpinglun);
        txt_zhuanfa.setText(numberOfzhuanfa);

        ImageView imageHead = (ImageView) findViewById(R.id.img_wb_item_head);
        Picasso.with(CommentsActivity.this).load(statusUriHead).into(imageHead);

        // 评论列表
        if (Integer.valueOf(numberOfpinglun).intValue() != 0) {
            // 获取微博评论信息接口

            if (mAccessToken != null && mAccessToken.isSessionValid()) {
                mCommentsAPI.show(Long.parseLong(statusId), 0L, 0L, 15, 1,
                        CommentsAPI.AUTHOR_FILTER_ALL, mListener);
            }
        }
        NineGridImageView imageContent = (NineGridImageView) findViewById(R.id.img_wb_item_content);
        if (statusUriContent != null) {
            imageContent.setVisibility(View.VISIBLE);
            imageContent.setAdapter(nineGridImageViewAdapter);
            imageContent.setImagesData(statusUriContent);
    }else{
            imageContent.setVisibility(View.GONE);
        }

        // 写评论部分
        writeComment = (EditText) findViewById(R.id.id_edit_comment);
        Button sendComment = (Button) findViewById(R.id.id_btn_comment_send);
        sendComment.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mCommentsAPI.create(writeComment.getText().toString(),
                        Long.parseLong(statusId), false, new RequestListener() {

                            @Override
                            public void onWeiboException(WeiboException arg0) {
                            }

                            @Override
                            public void onComplete(String arg0) {
                                String statusId = getIntent().getStringExtra(
                                        "statusId");
                                if (Integer.valueOf(numberOfpinglun).intValue() != 0) {
                                    intNumberOfpinglun = Integer
                                            .parseInt(getIntent()
                                                    .getStringExtra(
                                                            "numberOfpinglun")) + 1;

                                } else {
                                    intNumberOfpinglun = 1;
                                }
                                writeComment.setText("");
                                txt_pinglun.setText(String
                                        .valueOf(intNumberOfpinglun));
                                mCommentsAPI.show(Long.parseLong(statusId), 0L,
                                        0L, 20, 1,
                                        CommentsAPI.AUTHOR_FILTER_ALL,
                                        mListener);
                            }

                        });
            }
        });

    }

    private RequestListener mListener = new RequestListener() {
        @Override
        public void onComplete(String response) {
            if (!TextUtils.isEmpty(response)) {
                CommentList comments = CommentList.parse(response);
                mComments = comments.commentList;

                mCommentsAdapter = new CommentsAdapter(CommentsActivity.this,
                        R.layout.project_comments_item, mComments);
                mListView.setAdapter(mCommentsAdapter);
                mCommentsAdapter.notifyDataSetChanged();

            }
        }

        @Override
        public void onWeiboException(WeiboException arg0) {
        }
    };


    @Override
    public void onTap() {
        alertDialog.cancel();
    }
}
