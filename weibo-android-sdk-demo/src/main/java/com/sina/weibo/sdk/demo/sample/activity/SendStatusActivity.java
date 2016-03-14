package com.sina.weibo.sdk.demo.sample.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.demo.R;
import com.sina.weibo.sdk.demo.sample.db.AccessTokenKeeper;
import com.sina.weibo.sdk.demo.sample.db.Constants;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;

public class SendStatusActivity extends Activity {

	private EditText et_status_content;
	private Button sendStatus;
	private Oauth2AccessToken mAccessToken;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.project_write_status);
		et_status_content = (EditText) findViewById(R.id.id_edit_status);
		sendStatus = (Button) findViewById(R.id.id_btn_sendstatus);
		mAccessToken = AccessTokenKeeper
				.readAccessToken(SendStatusActivity.this);
		sendStatus.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				StatusesAPI mStatusesAPI = new StatusesAPI(
						SendStatusActivity.this, Constants.APP_KEY,
						mAccessToken);
				mStatusesAPI.update(et_status_content.getText().toString(),
						null, null, new RequestListener() {

							@Override
							public void onWeiboException(WeiboException arg0) {

							}

							@Override
							public void onComplete(String arg0) {
								// TODO Auto-generated method stub
								Intent intent = new Intent(
										SendStatusActivity.this,
										MainActivity.class);
								startActivity(intent);
								finish();
							}
						});
			}
		});
	}

}
