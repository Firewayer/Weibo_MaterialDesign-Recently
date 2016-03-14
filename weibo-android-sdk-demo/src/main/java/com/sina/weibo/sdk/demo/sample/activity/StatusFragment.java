package com.sina.weibo.sdk.demo.sample.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.demo.R;
import com.sina.weibo.sdk.demo.sample.db.AccessTokenKeeper;
import com.sina.weibo.sdk.demo.sample.db.Constants;
import com.sina.weibo.sdk.demo.sample.activity.adapter.MyStatusAdapter;
import com.sina.weibo.sdk.demo.sample.widget.ReFreshListView;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.StatusList;
import com.sina.weibo.sdk.utils.LogUtil;

import java.util.ArrayList;

/**
 * Created by wayne on 2015/7/26.
 */

public class StatusFragment extends Fragment implements AdapterView.OnItemClickListener, ReFreshListView.IReflashListener, ReFreshListView.ILoadMoreListener {


    private ArrayList<Status> mStatusList;

    /**
     * 当前 Token 信息
     */
    private Oauth2AccessToken mAccessToken;
    /**
     * 用于获取微博信息流等操作的API
     */
    private StatusesAPI mStatusesAPI;
    /** 用于显示微博Item */
//     private ListView mListViewStatusStatus ;
    /**
     * 用于存放要显示的Item
     */
    private StatusList mStatuses;


    private ReFreshListView mListViewStatus;

    private MyStatusAdapter mStatusAdapter;

    ProgressDialog progressDialog;

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
//        View view = inflater.inflate(R.layout.project_statusfragment, container, false);
        mListViewStatus = (ReFreshListView) inflater.inflate(R.layout.test_listview, container, false);

//        mListViewStatus = (ReFreshListView) view.findViewById(R.id.id_lv_status);
        mListViewStatus.setRefreshInterface(this);
        mListViewStatus.setLoadMoreInterface(this);
        mListViewStatus.setOnItemClickListener(this);
        progressDialog = new ProgressDialog((MainActivity) getActivity());
        progressDialog.setMessage("正在获取用户信息，请稍候...");
        progressDialog.show();
        mStatusesAPI.friendsTimeline(0L, 0L, 10, 1, false, 0, false,
                mListener);
        return mListViewStatus;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Status status = mStatusList.get(position - 1);
        if (status.pic_urls != null) {

            com.sina.weibo.sdk.demo.sample.activity.CommentsActivity.actionStart(getActivity(), status.idstr,
                    status.user.name, status.created_at, status.text,
                    status.user.profile_image_url, status.pic_urls,
                    String.valueOf(status.attitudes_count),
                    String.valueOf(status.comments_count),
                    String.valueOf(status.reposts_count));
        } else {
            CommentsActivity.actionStart(getActivity(), status.idstr,
                    status.user.name, status.created_at, status.text,
                    status.user.profile_image_url, null,
                    String.valueOf(status.attitudes_count),
                    String.valueOf(status.comments_count),
                    String.valueOf(status.reposts_count));
        }

    }

    @Override
    public void onLoadMore() {
        Long maxId = Long
                .parseLong(mStatusList.get(mStatusList.size() - 1).idstr);
        mStatusesAPI.friendsTimeline(0L, maxId - 1L, 20, 1, false, 0, false,
                new RequestListener() {

                    @Override
                    public void onWeiboException(WeiboException arg0) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onComplete(String response) {
                        if (!TextUtils.isEmpty(response)) {
                            Log.d("Json", response);
                            if (response.startsWith("{\"statuses\"")) {
                                // 调用 StatusList#parse 解析字符串成微博列表对象
                                StatusList statuses = StatusList
                                        .parse(response);
                                mStatusList.addAll(statuses.statusList);
                                mStatusAdapter.notifyDataSetChanged();
                                mListViewStatus.loadMoreComplete();
                            }
                        }
                    }
                });
    }


    private void setReflashData() {
        if (mAccessToken != null && mAccessToken.isSessionValid()) {
            mStatusesAPI.friendsTimeline(0L, 0L, 30, 1, false, 0, false,
                    mListener);
        }
    }

    @Override
    public void onReflash() {
        progressDialog.setMessage("正在加载首页，请稍候...");
        progressDialog.show();
        setReflashData();

    }

    public void loadAT() {
        progressDialog.setMessage("正在加载@，请稍候...");
        progressDialog.show();
        mStatusesAPI.mentions(0L, 0L, 10, 1,
                StatusesAPI.AUTHOR_FILTER_ALL,
                StatusesAPI.SRC_FILTER_ALL, 0, false,
                mListener);
    }


    private RequestListener mListener = new RequestListener() {
        @Override
        public void onComplete(String response) {
            progressDialog.dismiss();
            mListViewStatus.reflashComplete();
            if (!TextUtils.isEmpty(response)) {
                LogUtil.i("TAG", response);
                if (response.startsWith("{\"statuses\"")) {  //微博列表-JSON
                    // 调用 StatusList#parse 解析字符串成微博列表对象
                    StatusList statuses = StatusList.parse(response);
                    mStatusList = statuses.statusList;
                    mStatusAdapter = new MyStatusAdapter( getActivity(),
                            R.layout.project_weibo_item, mStatusList);
                    mListViewStatus.setAdapter(mStatusAdapter);
                    mStatusAdapter.notifyDataSetChanged();

                } else {
                    Toast.makeText(getActivity(), response,
                            Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {

        }
    };


}
