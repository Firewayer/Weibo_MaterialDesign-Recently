package com.sina.weibo.sdk.demo.sample.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sina.weibo.sdk.demo.R;

import java.text.SimpleDateFormat;
import java.util.Date;


public class ReFreshListView extends ListView implements OnScrollListener {
	View header;// 顶部布局文件；
	View footer;//顶部布局文件
	int headerHeight;// 顶部布局文件的高度；
	int firstVisibleItem;// 当前第一个可见的item的位置；
	int lastVisibleItem; //最后一个可见的item的位置
	int totalItemCount = 0; //总的Item数量
	int scrollState;// listview 当前滚动状态；
	boolean isRemark;// 标记，当前是在listview最顶端摁下的；
	boolean isLast; //正在加载
	int startY;// 摁下时的Y值；
	ILoadMoreListener mLoadMoreListener;
	
	int state;// 当前的状态；
	final int NONE = 0;// 正常状态；
	final int PULL = 1;// 提示下拉状态；
	final int RELEASE = 2;// 提示释放状态；
	final int REFRESH = 3;// 刷新状态；
	IReflashListener iReflashListener;//刷新数据的接口
	public ReFreshListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initView(context);
	}

	public ReFreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initView(context);
	}

	public ReFreshListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initView(context);
	}

	/**
	 * 初始化界面，添加顶部布局文件到 listview
	 * 
	 * @param context
	 */
	private void initView(Context context) {
		//header
		LayoutInflater inflater = LayoutInflater.from(context);  
		header = inflater.inflate(R.layout.header_layout, null); //得到inflater实例
		measureView(header);
		headerHeight = header.getMeasuredHeight();
		topPadding(-headerHeight);
		this.addHeaderView(header);
		this.setOnScrollListener(this);
		
		
		//footer
		LayoutInflater footerInflater = LayoutInflater.from(context);
		footer = footerInflater.inflate(R.layout.footer_layout, null);
		footer.findViewById(R.id.id_load_footer).setVisibility(View.GONE);
		measureView(footer);
		this.addFooterView(footer);
		this.setOnScrollListener(this);
		
		
	}

	/**
	 * 通知父布局，占用的宽，高；
	 * 
	 * @param view
	 */
	private void measureView(View view) {
		ViewGroup.LayoutParams p = view.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);   //宽为Match_parent，高为Wrap_content
		}
		int width = ViewGroup.getChildMeasureSpec(0, 0, p.width);  //第三个为子布局的宽度，第一个参数为左右边距padding，第二个为内边距margin
		int height;		
		int tempHeight = p.height;
		if (tempHeight > 0) {
			height = MeasureSpec.makeMeasureSpec(tempHeight,
					MeasureSpec.EXACTLY);
		} else {
			height = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}
		view.measure(width, height);
	}

	/**
	 * 设置header 布局 上边距；
	 * 
	 * @param topPadding
	 */
	private void topPadding(int topPadding) {
		header.setPadding(header.getPaddingLeft(), topPadding,
				header.getPaddingRight(), header.getPaddingBottom());
		header.invalidate();
	}
	

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		this.firstVisibleItem = firstVisibleItem;
		this.lastVisibleItem = firstVisibleItem + visibleItemCount;
		this.totalItemCount = totalItemCount;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if(totalItemCount ==lastVisibleItem && scrollState ==SCROLL_STATE_IDLE){
			
			if (!isLast) {
				isLast =true;
				footer.findViewById(R.id.id_load_footer).setVisibility(View.VISIBLE);
				//加载更多，数据操作
				mLoadMoreListener.onLoadMore();
			}
		}
		// TODO Auto-generated method stub
		this.scrollState = scrollState;	

	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (firstVisibleItem == 0) {
				isRemark = true;
				startY = (int) ev.getY();
			}
			break;

		case MotionEvent.ACTION_MOVE:
			onMove(ev);
			break;
		case MotionEvent.ACTION_UP:
			if (state == RELEASE) {
				state = REFRESH;
				// 加载最新数据；
				reFreshViewByState();
				iReflashListener.onReflash();
			} else if (state == PULL) {
				state = NONE;
				isRemark = false;
				reFreshViewByState();
			}
			break;
		}
		return super.onTouchEvent(ev);
	}

	/**
	 * 判断移动过程操作；
	 * 
	 * @param ev
	 */
	private void onMove(MotionEvent ev) {
		if (!isRemark) {
			return;
		}
		int tempY = (int) ev.getY();
		int space = tempY - startY;
		int topPadding = space - headerHeight;
		switch (state) {
		case NONE:
			if (space > 0) {
				state = PULL;
				reFreshViewByState();
			}
			break;
		case PULL:
			topPadding(topPadding);
			if (space > headerHeight*2
					&& scrollState == SCROLL_STATE_TOUCH_SCROLL) {
				state = RELEASE;
				reFreshViewByState();
			}else if(space <= headerHeight*2 && space>0){
				state = PULL;
				reFreshViewByState();
			} else{
				state = NONE;
				isRemark = false;
				reFreshViewByState();
			}
			break;
		case RELEASE:
			topPadding(topPadding);
			if (space <= headerHeight*2) {
				state = PULL;
				reFreshViewByState();
			} 
			break;
		}
	}

	/**
	 * 根据当前状态，改变界面显示；
	 */
	private void reFreshViewByState() {
		TextView tip = (TextView) header.findViewById(R.id.tip);
		ImageView arrow = (ImageView) header.findViewById(R.id.arrow);
		ProgressBar progress = (ProgressBar) header.findViewById(R.id.progress);
		RotateAnimation anim = new RotateAnimation(0, 180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		anim.setDuration(500);
		anim.setFillAfter(true);
		RotateAnimation anim1 = new RotateAnimation(180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		anim1.setDuration(500);
		anim1.setFillAfter(true);
		switch (state) {
		case NONE:
			arrow.clearAnimation();
			topPadding(-headerHeight);
			break;

		case PULL:
			arrow.setVisibility(View.VISIBLE);
			progress.setVisibility(View.GONE);
			tip.setText("下拉可以刷新！");
			arrow.clearAnimation();
			arrow.setAnimation(anim);
			break;
		case RELEASE:
			arrow.setVisibility(View.VISIBLE);
			progress.setVisibility(View.GONE);
			tip.setText("松开可以刷新！");
			arrow.clearAnimation();
			arrow.setAnimation(anim);
			break;
		case REFRESH:
			topPadding(50);
			arrow.setVisibility(View.GONE);
			progress.setVisibility(View.VISIBLE);
			tip.setText("正在刷新...");
			arrow.clearAnimation();
			break;
		}
	}

	/**
	 * 刷新模块
	 */
	public void reflashComplete() {
		state = NONE;
		isRemark = false;
		reFreshViewByState();
		TextView lastupdatetime = (TextView) header
				.findViewById(R.id.lastupdate_time);
		SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 hh:mm:ss");
		Date date = new Date(System.currentTimeMillis());
		String time = format.format(date);
		lastupdatetime.setText(time);
	}
	
	public void setRefreshInterface(IReflashListener iReflashListener){
		this.iReflashListener = iReflashListener;
	}
	
	/**
	 * 刷新数据接口
	 * @author Administrator
	 */
	public interface IReflashListener{
		public void onReflash();
	}
	//加载更多模块
	
	//加载完毕
	public void loadMoreComplete(){
		isLast = false;
		footer.findViewById(R.id.id_load_footer).setVisibility(View.GONE);
	}
	
	
	public void setLoadMoreInterface(ILoadMoreListener mLoadMoreListener){
		this.mLoadMoreListener = mLoadMoreListener;
	}
	//加载更多数据的回调接口
	public interface ILoadMoreListener{
		public void onLoadMore();
	}
}
