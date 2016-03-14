package com.sina.weibo.sdk.demo.sample.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.demo.R;
import com.sina.weibo.sdk.demo.sample.db.AccessTokenKeeper;
import com.sina.weibo.sdk.demo.sample.db.Constants;
import com.sina.weibo.sdk.exception.WeiboException;

public class WelcomeActivity extends Activity {

	private static final String TAG = "weibosdk";

	/** 显示认证后的信息，如 AccessToken */

	private AuthInfo mAuthInfo;

	/** 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能 */
	private Oauth2AccessToken mAccessToken;

	/** 注意：SsoHandler 仅当 SDK 支持 SSO 时有效 */
	private SsoHandler mSsoHandler;

	private ImageView weibo_logo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.project_activity_welcome);
		mAuthInfo = new AuthInfo(this, Constants.APP_KEY,
				Constants.REDIRECT_URL, Constants.SCOPE);
		mSsoHandler = new SsoHandler(WelcomeActivity.this, mAuthInfo);
		weibo_logo = (ImageView) findViewById(R.id.weibo_logo);

		// 为logo图片设置alpha动画效果，并设置监听器监听动画结束时跳转
		AlphaAnimation animation = new AlphaAnimation(0.1f, 1.0f);
		animation.setDuration(500);
		animation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation arg0) {
				mAccessToken = AccessTokenKeeper
						.readAccessToken(WelcomeActivity.this);
				if (mAccessToken != null && mAccessToken.isSessionValid()) {
					Intent intent = new Intent(WelcomeActivity.this,
							com.sina.weibo.sdk.demo.sample.activity.MainActivity.class);
					startActivity(intent);
				} else {
					mSsoHandler.authorizeWeb(new AuthListener());
				}
				finish();
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
			}

			@Override
			public void onAnimationStart(Animation arg0) {
			}

		});
		weibo_logo.setAnimation(animation);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

	/**
	 * 微博认证授权回调类。 1. SSO 授权时，需要在 {@link #onActivityResult} 中调用
	 * {@link SsoHandler#authorizeCallBack} 后， 该回调才会被执行。 2. 非 SSO
	 * 授权时，当授权结束后，该回调就会被执行。 当授权成功后，请保存该 access_token、expires_in、uid 等信息到
	 * SharedPreferences 中。
	 */
	class AuthListener implements WeiboAuthListener {

		@Override
		public void onComplete(Bundle values) {
			// 从 Bundle 中解析 Token
			mAccessToken = Oauth2AccessToken.parseAccessToken(values);
			// 从这里获取用户输入的 电话号码信息
			String phoneNum = mAccessToken.getPhoneNum();
			if (mAccessToken.isSessionValid()) {
				// 保存 Token 到 SharedPreferences
				AccessTokenKeeper.writeAccessToken(WelcomeActivity.this,
						mAccessToken);
				Intent intent = new Intent(WelcomeActivity.this,
						com.sina.weibo.sdk.demo.sample.activity.MainActivity.class);
				startActivity(intent);
			} else {
				// 以下几种情况，您会收到 Code：
				// 1. 当您未在平台上注册的应用程序的包名与签名时；
				// 2. 当您注册的应用程序包名与签名不正确时；
				// 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
				String code = values.getString("code");
				String message = getString(R.string.weibosdk_demo_toast_auth_failed);
				if (!TextUtils.isEmpty(code)) {
					message = message + "\nObtained the code: " + code;
				}
				Toast.makeText(WelcomeActivity.this, message, Toast.LENGTH_LONG)
						.show();
			}
		}

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onWeiboException(WeiboException arg0) {
			// TODO Auto-generated method stub

		}
	}

}
