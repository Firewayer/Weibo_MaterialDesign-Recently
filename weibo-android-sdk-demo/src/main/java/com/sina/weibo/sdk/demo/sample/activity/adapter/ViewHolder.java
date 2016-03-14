package com.sina.weibo.sdk.demo.sample.activity.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jaeger.ninegridimageview.NineGridImageView;
import com.sina.weibo.sdk.demo.sample.util.MyApplication;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

public class ViewHolder {

	public static final int TYPE_IMAGE = 1;
	public static final int TYPE_LAYOUT = 2;
	public static final int TYPE_NINE = 3;


	private SparseArray<View> mViews;
	private int mPosition; //ConvertView能复用，但position是时刻变化的
	public int getPosition() {
		return mPosition;
	}

	private View mConvertView;
	
	public ViewHolder(Context context, ViewGroup parent, int layoutId,
			int position) {
		this.mPosition = position;
		this.mViews = new SparseArray<View>();
		mConvertView = LayoutInflater.from(context).inflate(layoutId, parent, false);
		mConvertView.setTag(this);
	}

	/*
	 * 因为要判断是否复用convertView,所以选择使用静态方法的作为入口方法，提前对convertView是否为空的判断
	 */

	public static ViewHolder get(Context context, View convertView,
			ViewGroup parent, int layoutId, int position) {
		if(convertView == null){
			return new ViewHolder(context, parent, layoutId, position);
		}else{
			ViewHolder holder = (ViewHolder) convertView.getTag();
			holder.mPosition = position;
			return holder;
		}
	}

	/**
	 * 通过ViewId获取控件	
	 * @param viewId
	 * @return
	 */
	
	
	public <T extends View> T getView(int viewId){
		View view = mViews.get(viewId);
		
		if(view == null){
			view = mConvertView.findViewById(viewId);
			mViews.put(viewId, view);
		}
		
		return (T)view;
	}
	
	
	public View getConvertView() {
		return mConvertView;
	}




	public ViewHolder setVisiblity(int viewId, int viewType, int visibilityType){
		switch (viewType){
			case TYPE_IMAGE:
				ImageView iv = getView(viewId);
				iv.setVisibility(visibilityType);
				break;

			case TYPE_NINE:
				NineGridImageView nineGridImageView = getView(viewId);
				nineGridImageView.setVisibility(visibilityType);
				break;
			case TYPE_LAYOUT:
				LinearLayout linearLayout = getView(viewId);
				linearLayout.setVisibility(visibilityType);
				break;

			default:
		}

		return this;
	}
	/**
	 * 设置TextView
	 * @param viewId
	 * @param text
	 * @return
	 */
	public ViewHolder setText(int viewId, CharSequence text){
		TextView tv = getView(viewId);
		tv.setText(text);
		return this;
	}
	
	
	/**
	 * 设置ImageView
	 * @param viewId
	 * @param resId
	 * @return
	 */
	public ViewHolder setImageResource(int viewId, int resId){
		ImageView iv = getView(viewId);
		iv.setImageResource(resId);
		return this;
		
	}

	/**
	 * 通过Bitmap流设置ImageView
	 * @param viewId
	 * @param bitmap
	 * @return
	 */
	public ViewHolder setImageBitmap(int viewId, Bitmap bitmap){
		ImageView iv = getView(viewId);
		iv.setImageBitmap(bitmap);
		return this;
		
	}
	
	
	/**
	 * 用单例模式加载网络或本地图片
	 * @param viewId
	 * @param url
	 * @return
	 */
	public ViewHolder setImageURI(int viewId, String url){
		ImageView iv = getView(viewId);
		Picasso.with(MyApplication.getContext()).load(url)
				.into(iv);
		return this;
		
	}

	public ViewHolder setImageURI(int viewId, String url, Transformation transformation){
		ImageView iv = getView(viewId);
		Picasso.with(MyApplication.getContext()).load(url)
				.transform(transformation).into(iv);
		return this;

	}
	
	
	
}
