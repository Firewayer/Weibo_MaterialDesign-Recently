/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sina.weibo.sdk.demo.sample.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jaeger.ninegridimageview.NineGridImageView;
import com.jaeger.ninegridimageview.NineGridImageViewAdapter;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.demo.R;
import com.sina.weibo.sdk.demo.sample.Config.ZoomTransformation;
import com.sina.weibo.sdk.demo.sample.activity.adapter.MyNineGridImageViewAdapter;
import com.sina.weibo.sdk.demo.sample.db.AccessTokenKeeper;
import com.sina.weibo.sdk.demo.sample.db.Constants;
import com.sina.weibo.sdk.demo.sample.util.MyApplication;
import com.sina.weibo.sdk.demo.sample.util.Utils;
import com.sina.weibo.sdk.demo.sample.widget.ZoomImageView;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.StatusList;
import com.sina.weibo.sdk.openapi.models.User;
import com.sina.weibo.sdk.utils.LogUtil;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.co.senab.photoview.PhotoViewAttacher;

public class TestFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, ZoomImageView.IZoomViewListener {

    /**
     * 用于显示微博Item
     */

    RecyclerView mListViewStatus;
    SimpleStatusRecyclerViewAdapter mSimpleStatusRecyclerViewAdapter;
    ProgressDialog progressDialog;
    SwipeRefreshLayout mSwipeRefreshLayout;
    //    LinearLayoutManager mLinearLayoutManager;
    private ArrayList<Status> mStatusList;
    LinearLayoutManager mLinearLayoutManager;
    int lastVisibleItemPosition;
    boolean isLoading;
    PhotoViewAttacher mAttacher;
    /**
     * 当前 Token 信息
     */
    private Oauth2AccessToken mAccessToken;
    static AlertDialog alertDialog;
    ZoomImageView.IZoomViewListener iZoomViewListener;
    /**
     * 用于获取微博信息流等操作的API
     */

    MyNineGridImageViewAdapter nineGridImageViewAdapter = new MyNineGridImageViewAdapter() {


        @Override
        protected void onItemImageClick(Context context, int index, List<String> list) {
            showLargeImg(context, list.get(index).replace("thumbnail", "large"));
        }
    };
    private StatusesAPI mStatusesAPI;
    private StatusList mStatuses;
    private RequestListener mListener = new RequestListener() {
        @Override
        public void onComplete(String response) {
            progressDialog.dismiss();
            mSwipeRefreshLayout.setRefreshing(false);
            if (!TextUtils.isEmpty(response)) {
                LogUtil.i("TAG", response);
                if (response.startsWith("{\"statuses\"")) {
                    // 调用 StatusList#parse 解析字符串成微博列表对象
                    StatusList statuses = StatusList.parse(response);
                    mStatusList = statuses.statusList;
                    mSimpleStatusRecyclerViewAdapter.updateData(mStatusList);
                    mSimpleStatusRecyclerViewAdapter.notifyDataSetChanged();
                    mListViewStatus.scrollToPosition(0);
                }
            } else {
                Toast.makeText(getActivity(), R.string.load_err,
                        Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onWeiboException(WeiboException arg0) {
            progressDialog.dismiss();
            mSwipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getActivity(), R.string.network_err,
                    Toast.LENGTH_LONG).show();


        }
    };

    public void saveData(ArrayList<Status> statusList) {
        SQLiteDatabase databaseHelper = Utils.getDatabaseHelper().getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        for (Status status : statusList) {
//            contentValues.put("id", Long.parseLong(status.idstr));
            contentValues.put("name", status.user.name);
            contentValues.put("gender", status.user.gender);
            contentValues.put("location", status.user.location);
            contentValues.put("description", status.user.description);
            contentValues.put("followers_count", status.user.followers_count);
            contentValues.put("friends_count", status.user.friends_count);
            contentValues.put("statuses_count", status.user.statuses_count);
            contentValues.put("main_content", status.text);
            contentValues.put("created_at", status.created_at);
            contentValues.put("source", status.source);
            if (status.retweeted_status != null) {
                contentValues.put("sub_status", status.retweeted_status != null ? 1 : 0);
                contentValues.put("sub_name", status.retweeted_status.user.name);
                contentValues.put("sub_content", status.retweeted_status.text);
            }
        }
        databaseHelper.insertWithOnConflict("Status", null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
        databaseHelper.close();

    }

    public List<Status> getData() {
        SQLiteDatabase databaseHelper = Utils.getDatabaseHelper().getReadableDatabase();
        List<Status> mList = new ArrayList<>();
        Cursor cursor = databaseHelper.query("status", null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            User mainUser = new User();
            Status status = new Status();
            status.user = mainUser;

            status.user.name = cursor.getString(cursor.getColumnIndex("name"));
            status.user.gender = cursor.getString(cursor.getColumnIndex("gender"));
            status.user.location = cursor.getString(cursor.getColumnIndex("location"));
            status.user.followers_count = cursor.getInt(cursor.getColumnIndex("followers_count"));
            status.user.friends_count = cursor.getInt(cursor.getColumnIndex("friends_count"));
            status.user.statuses_count = cursor.getInt(cursor.getColumnIndex("statuses_count"));
            status.text = cursor.getString(cursor.getColumnIndex("main_content"));
            status.created_at = cursor.getString(cursor.getColumnIndex("created_at"));
            status.source = cursor.getString(cursor.getColumnIndex("source"));
            if (cursor.getInt(cursor.getColumnIndex("sub_status")) == 1) {
                Status retweeted = new Status();
                User subUser = new User();
                retweeted.user = subUser;
                status.retweeted_status = retweeted;
                status.retweeted_status.user.name = cursor.getString(cursor.getColumnIndex("sub_name"));
                status.retweeted_status.text = cursor.getString(cursor.getColumnIndex("sub_content"));
            }
            mList.add(status);
        }
        cursor.close();
        databaseHelper.close();
        return mList;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        initData();

    }

    private void initData() {
        // 获取当前已保存过的 Token
        mAccessToken = AccessTokenKeeper.readAccessToken(getActivity());
        // 对statusAPI实例化
        mStatusesAPI = new StatusesAPI(getActivity(), Constants.APP_KEY, mAccessToken);


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        iZoomViewListener = this;
        View view = inflater.inflate(R.layout.fragment_cheese_list, container, false);
        mListViewStatus = (RecyclerView) view.findViewById(R.id.recyclerview);
        mListViewStatus.setItemAnimator(new DefaultItemAnimator());
        mSimpleStatusRecyclerViewAdapter = new SimpleStatusRecyclerViewAdapter(getActivity(), R.layout.project_weibo_item, mStatusList);
        mLinearLayoutManager = new LinearLayoutManager(mListViewStatus.getContext());
        mListViewStatus.setLayoutManager(mLinearLayoutManager);
        mListViewStatus.setAdapter(mSimpleStatusRecyclerViewAdapter);
        mListViewStatus.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE && mLinearLayoutManager.findLastVisibleItemPosition() + 1 >= mSimpleStatusRecyclerViewAdapter.getItemCount() && mStatusList != null && mStatusList.size() >= 1) {
                    if (!isLoading) {
                        isLoading = true;
                        mSwipeRefreshLayout.setRefreshing(true);
                        onLoadMore();
                    }
                }
            }


        });
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.id_sfreshlayout);
        mSwipeRefreshLayout.setRefreshing(false);
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("正在获取用户信息，请稍候...");
        progressDialog.show();
        mStatusesAPI.friendsTimeline(0L, 0L, 10, 1, false, 0, false,
                mListener);


        return view;
    }

    private void onLoadMore() {

        Long maxId = Long
                .parseLong(mStatusList.get(mStatusList.size() - 1).idstr);
        Log.d("idstr", maxId + "");
        mStatusesAPI.friendsTimeline(0L, maxId - 1L, 20, 1, false, 0, false,
                new RequestListener() {

                    @Override
                    public void onWeiboException(WeiboException arg0) {
                        Toast.makeText(Utils.getApplicationContext(), R.string.network_err, Toast.LENGTH_LONG).show();
                        mSwipeRefreshLayout.setRefreshing(false);
                        isLoading = false;
                        mSimpleStatusRecyclerViewAdapter.notifyItemRemoved(mLinearLayoutManager.getItemCount());
                    }

                    @Override
                    public void onComplete(String response) {
                        if (!TextUtils.isEmpty(response)) {
                            Log.d("Json", response);
                            if (response.startsWith("{\"statuses\"")) {
                                // 调用 StatusList#parse 解析字符串成微博列表对象
                                StatusList statuses = StatusList
                                        .parse(response);
                                if (statuses.statusList != null) {
                                    mStatusList.addAll(statuses.statusList);
                                    isLoading = false;
                                    mSimpleStatusRecyclerViewAdapter.notifyDataSetChanged();
                                    mSwipeRefreshLayout.setRefreshing(false);
                                } else {
                                    Toast.makeText(MyApplication.getContext(), "微博接口限制，只能加载这么多了···sry", Toast.LENGTH_SHORT).show();
                                    isLoading = false;
                                    mSwipeRefreshLayout.setRefreshing(false);
                                    mSimpleStatusRecyclerViewAdapter.notifyItemRemoved(mLinearLayoutManager.getItemCount());
                                }
                            }
                        }
                    }
                });


    }

    @Override
    public void onRefresh() {
        mStatusesAPI.friendsTimeline(0L, 0L, 10, 1, false, 0, false,
                mListener);
    }

    @Override
    public void onTap() {
        alertDialog.cancel();
    }

    public class SimpleStatusRecyclerViewAdapter
            extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final TypedValue mTypedValue = new TypedValue();
        private Context mContext;
        private int mBackground;
        private List<Status> mValues;
        private int resourceId;
        public static final int TYPE_ITEM = 0;
        public static final int TYPE_FOOTER = 1;

        public SimpleStatusRecyclerViewAdapter(Context context, int itemResourceId, List<Status> items) {
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            this.mContext = context;
            mBackground = mTypedValue.resourceId;
            mValues = (items != null) ? items : new ArrayList<Status>();
            this.resourceId = itemResourceId;
        }

        public Status getValueAt(int position) {
            return mValues.get(position);
        }

        public boolean updateData(List<Status> statuses) {
            if (statuses != null) {
                mValues = statuses;
                return true;
            }
            return false;
        }

        @Override
        public int getItemViewType(int position) {
            if (position + 1 == mValues.size()) {
                return SimpleStatusRecyclerViewAdapter.TYPE_FOOTER;
            } else {
                return SimpleStatusRecyclerViewAdapter.TYPE_ITEM;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == SimpleStatusRecyclerViewAdapter.TYPE_ITEM) {
                View view = LayoutInflater.from(mContext)
                        .inflate(resourceId, parent, false);
                view.setBackgroundResource(mBackground);
                return new ItemViewHolder(view);
            } else {
                View view = LayoutInflater.from(mContext).inflate(R.layout.footer_layout, parent, false);
                view.setBackgroundResource(mBackground);
                return new FooterViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof ItemViewHolder) {
                ItemViewHolder holder0 = (ItemViewHolder) holder;
                Log.d("TAG", holder0.toString() + holder0.iv_subContent);
                final Status status = mValues.get(position);
                //主微博
                // 主微博
                String uriUserHead = mValues.get(position).user.profile_image_url;
                Picasso.with(getActivity()).load(uriUserHead).into(holder0.iv_userhead);

                holder0.iv_userhead.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UserActivity_.actionStart(getActivity(), status.user.idstr,
                                status.user.profile_image_url, status.user.name,
                                status.user.description, status.user.followers_count,
                                status.user.friends_count, status.user.statuses_count);
                    }
                });

                if (mValues.get(position).pic_urls != null) {
                    holder0.statusPic.setVisibility(View.VISIBLE);
                    holder0.statusPic.setAdapter(nineGridImageViewAdapter);
                    holder0.statusPic.setImagesData(mValues.get(position).pic_urls);
//                    holder0.statusPic.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            showLargeImg(mContext, mValues.get(position).original_pic);
//                        }
//                    });

                } else {
                    holder0.statusPic.setVisibility(View.GONE); // 一条属性，解决无中生有
                }

                holder0.btnPinglun.setOnClickListener(new View.OnClickListener() {
                    // 评论方法示例
                    @Override
                    public void onClick(View v) {
                        if (mValues.get(position).pic_urls != null) {
                            CommentsActivity.actionStart(getActivity(), mValues.get(position).idstr,
                                    mValues.get(position).user.name, mValues.get(position).created_at, mValues.get(position).text,
                                    mValues.get(position).user.profile_image_url, mValues.get(position).pic_urls,
                                    String.valueOf(mValues.get(position).attitudes_count),
                                    String.valueOf(mValues.get(position).comments_count),
                                    String.valueOf(mValues.get(position).reposts_count));
                        } else {
                            CommentsActivity.actionStart(getActivity(), mValues.get(position).idstr,
                                    mValues.get(position).user.name, mValues.get(position).created_at, mValues.get(position).text,
                                    mValues.get(position).user.profile_image_url, null,
                                    String.valueOf(mValues.get(position).attitudes_count),
                                    String.valueOf(mValues.get(position).comments_count),
                                    String.valueOf(mValues.get(position).reposts_count));
                        }
                    }
                });

                if (mValues.get(position).retweeted_status != null) { // Visibility属性设置，解决子微博内容偶尔消失的Bug
                    holder0.subLayout.setVisibility(View.VISIBLE);
                    holder0.textSubContent
                            .setText(setTextColor(mValues.get(position).retweeted_status.text));
                    if (mValues.get(position).retweeted_status.pic_urls != null) {
                        final String pic_url = mValues.get(position).retweeted_status.bmiddle_pic;
                        Picasso.with(getActivity()).load(pic_url).resize(200, 200)
                                .centerCrop().into(holder0.iv_subContent);
                        holder0.iv_subContent.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showLargeImg(mContext, pic_url);
                            }
                        });
                        holder0.iv_subContent.setVisibility(View.VISIBLE);

                    } else {
                        holder0.iv_subContent.setVisibility(View.GONE);
                    }
                } else {
                    holder0.subLayout.setVisibility(View.GONE);
                }

                if (mValues.get(position).user.verified == true)
                    holder0.iv_isv.setVisibility(View.VISIBLE);
                else
                    holder0.iv_isv.setVisibility(View.GONE);


                //view内容设置
                holder0.textUserName.setText(mValues.get(position).user.name);
                holder0.textContent.setText(setTextColor(mValues.get(position).text));
                holder0.numberOfDianzan.setText(String
                        .valueOf(mValues.get(position).attitudes_count));
                holder0.numberOfpinglun.setText(String
                        .valueOf(mValues.get(position).comments_count));
                holder0.numberOfzhuanfa
                        .setText(String.valueOf(mValues.get(position).reposts_count));
                holder0.textResource.setText(" " + Html.fromHtml(mValues.get(position).source));
                holder0.textTime.setText(dealTime(mValues.get(position).created_at));
                holder0.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MyApplication.getContext(), "点击了Item", Toast.LENGTH_SHORT).show();
                    }
                });

            }

        }


        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder {

            View mView;

            // 主微博
            ImageView iv_userhead;
            ImageView iv_isv;
            ImageButton btnUserHead;
            NineGridImageView statusPic;
            ImageButton btnStatusImage;
            ImageButton btnPinglun;
            TextView textUserName;
            TextView textContent;
            TextView numberOfDianzan;
            TextView numberOfpinglun;
            TextView numberOfzhuanfa;
            TextView textResource;
            TextView textTime;

            // 子微博
            LinearLayout subLayout;
            ImageView iv_subContent;
            TextView textSubContent; // 子微博内容

            public ItemViewHolder(View view) {
                super(view);
                mView = view;
                subLayout = (LinearLayout) view
                        .findViewById(R.id.lyt_wb_item_sublayout);

                textSubContent = (TextView) view
                        .findViewById(R.id.txt_wb_item_subcontent);
                iv_subContent = (ImageView) view
                        .findViewById(R.id.img_wb_item_content_subpic);

                btnUserHead = (ImageButton) view
                        .findViewById(R.id.id_btn_userhead);

                iv_userhead = (ImageView) view
                        .findViewById(R.id.img_wb_item_head);

                statusPic = (NineGridImageView) view
                        .findViewById(R.id.img_wb_item_content);

                ImageButton btnStatusImage = (ImageButton) view
                        .findViewById(R.id.id_btn_statusImage);

                textUserName = (TextView) view
                        .findViewById(R.id.txt_wb_item_uname);
                textContent = (TextView) view
                        .findViewById(R.id.txt_wb_item_content);
                numberOfDianzan = (TextView) view
                        .findViewById(R.id.txt_wb_item_unlike);
                numberOfpinglun = (TextView) view
                        .findViewById(R.id.txt_wb_item_comment);
                numberOfzhuanfa = (TextView) view
                        .findViewById(R.id.txt_wb_item_redirect);
                textResource = (TextView) view
                        .findViewById(R.id.txt_wb_item_from);
                textTime = (TextView) view
                        .findViewById(R.id.txt_wb_item_time);
                iv_isv = (ImageView) view
                        .findViewById(R.id.img_wb_item_V);

                btnPinglun = (ImageButton) view
                        .findViewById(R.id.txt_wb_item_comment_img);
            }

        }

        public class FooterViewHolder extends RecyclerView.ViewHolder {


            public FooterViewHolder(View view) {
                super(view);
            }
        }
    }

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

    @SuppressWarnings("deprecation")
    public String dealTime(String time)// 用于处理微博的发布时间
    {
        Date now = new Date();
        long lnow = now.getTime() / 1000;

        long ldate = Date.parse(time) / 1000;
        Date date = new Date(ldate);

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
