package com.sina.weibo.sdk.demo.sample.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public abstract class CommonAdapter<T> extends BaseAdapter {
	protected Context mContext;
	protected List<T> mDatas;
	protected LayoutInflater mInflater;
	protected int resourceId;

	public CommonAdapter(Context context, int textResourceId, List<T> datas) {
		this.mContext = context;
		this.mDatas = datas;
		this.resourceId = textResourceId;
		mInflater = LayoutInflater.from(context);

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mDatas.size();
	}

	@Override
	public T getItem(int position) {
		// TODO Auto-generated method stub
		return mDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public  View getView(int position, View convertView,
			ViewGroup parent){
		ViewHolder holder = ViewHolder.get(mContext, convertView, parent,
				resourceId, position);

		setView(holder, getItem(position));
		return holder.getConvertView();
	}
	
	
	public abstract void setView(ViewHolder holder, T t);
}
