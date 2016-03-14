package com.sina.weibo.sdk.demo.sample.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;

/**
 * Created by Firewayer on 2016/3/12.
 */
public class FullDialog extends Dialog {
    public FullDialog(Context context, int themeResId) {
        super(context, themeResId);
        setOwnerActivity((Activity) context);
    }
}
