package com.sina.weibo.sdk.demo.sample.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.demo.R;
import com.sina.weibo.sdk.demo.sample.db.AccessTokenKeeper;
import com.sina.weibo.sdk.demo.sample.db.Constants;
import com.sina.weibo.sdk.demo.sample.activity.adapter.MyStatusAdapter;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.StatusList;
import com.sina.weibo.sdk.utils.LogUtil;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;


@EActivity(R.layout.project_user)
public class UserActivity extends AppCompatActivity {

    @ViewById(R.id.txt_perinfo_uname)
    TextView text_user_name;

    @ViewById(R.id.txt_perinfo_describtion)
    TextView text_user_description;

    @ViewById(R.id.txt_perinfo_guanzhu)
    TextView text_user_mention;

    @ViewById(R.id.txt_perinfo_fens)
    TextView text_user_follower;

    @ViewById(R.id.id_txt_userstatuses)
    TextView text_user_status;

    @ViewById(R.id.img_perinfo_head)
    ImageView image_head;

    @ViewById(R.id.listview_statuses)
    ListView mListView;

    @AfterViews
    void initView() {
        initUserDate();
    }


    // 获取ListView微博所需信息
    private ArrayList<Status> mStatusList;
    /**
     * 当前 Token 信息
     */
    private Oauth2AccessToken mAccessToken;
    /**
     * 用于获取微博信息流等操作的API
     */
    private StatusesAPI mStatusesAPI;

    /**
     * 用于存放要显示的Item
     */
    private StatusList mStatuses;

    private MyStatusAdapter mStatusAdapter;

    public static void actionStart(Context context, String statusUserUID,
                                   String profile_image_url, String name, String description,
                                   int followers_count, int friends_count, int statuses_count) {
        Intent intent = new Intent(context, UserActivity_.class);
        intent.putExtra("followers_count", followers_count);
        intent.putExtra("friends_count", friends_count);
        intent.putExtra("statuses_count", statuses_count);
        intent.putExtra("name", name);
        intent.putExtra("description", description);
        intent.putExtra("profile_image_url", profile_image_url);
        context.startActivity(intent);
    }


    private void initUserDate() {


        String name;
        int friends_count;
        String description;
        String userHeadUri;
        int followers_count;
        int statuses_count;

        //得到传入数据
        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        description = intent.getStringExtra("description");
        userHeadUri = intent.getStringExtra("profile_image_url");
        followers_count = intent.getIntExtra("followers_count", 0);
        friends_count = intent.getIntExtra("friends_count", 0);
        statuses_count = intent.getIntExtra("statuses_count", 0);
        // 获取当前已保存过的 Token
        mAccessToken = AccessTokenKeeper.readAccessToken(this);
        // 对statusAPI实例化
        mStatusesAPI = new StatusesAPI(this, Constants.APP_KEY, mAccessToken);
        text_user_name.setText(name);
        text_user_description.setText(description);
        text_user_mention.setText("关注 " + friends_count);
        text_user_follower.setText("粉丝 " + followers_count);
        text_user_status.setText("微博 " + statuses_count);
        // 网络操作
        Picasso.with(UserActivity.this).load(userHeadUri).into(image_head);

        if (mAccessToken != null && mAccessToken.isSessionValid()) {
            mStatusesAPI.userTimeline(name, 0L, 0L, 15, 1, false, 0, false,
                    new RequestListener() {
                        @Override
                        public void onWeiboException(WeiboException arg0) {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void onComplete(String response) {
                            Toast.makeText(UserActivity.this, "个人信息加载完成",
                                    Toast.LENGTH_SHORT).show();
                            // TODO Auto-generated method stub
                            if (!TextUtils.isEmpty(response)) {
                                LogUtil.i("TAG", response);
                                if (true) {
                                    // 调用 StatusList#parse 解析字符串成微博列表对象
                                    StatusList statuses = StatusList
                                            .parse(response);
                                    mStatusList = statuses.statusList;
                                    mStatusAdapter = new MyStatusAdapter(
                                            UserActivity.this,
                                            R.layout.project_weibo_item,
                                            mStatusList);
                                    mListView.setAdapter(mStatusAdapter);
                                    mStatusAdapter.notifyDataSetChanged();

                                } else {
                                    Toast.makeText(UserActivity.this, response,
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });


        }

    }
}
