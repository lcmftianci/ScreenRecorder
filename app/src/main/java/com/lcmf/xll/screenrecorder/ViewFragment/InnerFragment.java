package com.lcmf.xll.screenrecorder.ViewFragment;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.lcmf.xll.screenrecorder.R;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/7/16 0016.
 */

public class InnerFragment extends Fragment{

	public static int IsVisible = 0;

//	private Button btn1;
//	private Button btn2;
//	private Button btn3;
//	Fragment fragment1;
//		@Override
//		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//			View view =  inflater.inflate(R.layout.fragment_inner_vedio, container, false);
//			btn1 = (Button)view.findViewById(R.id.id_top1);
//			btn1.setOnClickListener(this);
//			btn2 = (Button)view.findViewById(R.id.id_top2);
//			btn2.setOnClickListener(this);
//			btn3 = (Button)view.findViewById(R.id.id_top3);
//			btn3.setOnClickListener(this);
//			fragment1 = new FragmentVideo();
//			switchFragment(fragment1);
//			return view;
//		}
//
//	/**
//	 * Called when a view has been clicked.
//	 *
//	 * @param v The view that was clicked.
//	 */
//	@Override
//	public void onClick(View v) {
//		switch (v.getId()){
//			case R.id.id_top1:
//				Toast.makeText(getActivity().getApplicationContext(), "你好", Toast.LENGTH_SHORT).show();
//				Fragment fragment2 = new FragmentScreen();
//				switchFragment(fragment2);
//				break;
//			case R.id.id_top2:
//				Toast.makeText(getActivity().getApplicationContext(), "你好11", Toast.LENGTH_SHORT).show();
//				fragment1 = new FragmentVideo();
//				switchFragment(fragment1);
//				break;
//			case R.id.id_top3:
//				Fragment fragment3 = new FragmentPicture();
//				switchFragment(fragment3);
//			default:
//				break;
//		}
//	}
//
//	private void switchFragment(Fragment fragment){
//		FragmentManager manager = getActivity().getSupportFragmentManager();
//		FragmentTransaction transaction = manager.beginTransaction();
//		transaction.replace(R.id.id_content, fragment);
//		transaction.commit();
//	}

	//图片
	private TextView pictureTextView;
	//电影
	private TextView movieTextView;
	//音乐
	private TextView musicTextView;

	//实现Tab滑动效果
	private ViewPager mViewPager;

	//动画图片
	private ImageView cursor;

	//动画图片偏移量
	private int offset = 0;
	private int position_one;
	private int position_two;

	//动画图片宽度
	private int bmpW;

	//当前页卡编号
	private int currIndex = 0;

	//存放Fragment
	private ArrayList<Fragment> fragmentArrayList;

	//管理Fragment
	private FragmentManager fragmentManager;

	public Context context;

	public static final String TAG = "Fragment";

	private View view;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		view =  inflater.inflate(R.layout.fragment_inner_vedio, container, false);
		context = getActivity();

		//初始化TextView
		InitTextView();

		//初始化ImageView
		InitImageView();

		//初始化Fragment
		InitFragment();

		//初始化ViewPager
		InitViewPager();
		return view;
	}

	/**
	 * 初始化头标
	 */
	private void InitTextView(){

		//图片头标
		pictureTextView = (TextView)view.findViewById(R.id.picture_text);
		//电影头标
		movieTextView = (TextView)view.findViewById(R.id.movie_text);
		//音乐头标
		musicTextView = (TextView)view.findViewById(R.id.music_text);

		//添加点击事件
		pictureTextView.setOnClickListener(new MyOnClickListener(0));
		movieTextView.setOnClickListener(new MyOnClickListener(1));
		musicTextView.setOnClickListener(new MyOnClickListener(2));
	}

	@Override
	public void onResume() {
		/**
		 * 设置为竖屏
		 */
		if(getActivity().getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
			getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		super.onResume();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (hidden) {
			System.out.println("不可见");
			IsVisible = 0;
		} else {
			System.out.println("当前可见");
			IsVisible = 1;
		}
	}

	/**
	 * 初始化页卡内容区
	 */
	private void InitViewPager() {

		mViewPager = (ViewPager)view.findViewById(R.id.vPager);
		mViewPager.setAdapter(new MFragmentPagerAdapter(fragmentManager, fragmentArrayList));

		//让ViewPager缓存2个页面
		mViewPager.setOffscreenPageLimit(2);

		//设置默认打开第一页
		mViewPager.setCurrentItem(0);

		//将顶部文字恢复默认值
		resetTextViewTextColor();
		pictureTextView.setTextColor(getResources().getColor(R.color.main_top_tab_color_2));

		//设置viewpager页面滑动监听事件
		mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
	}

	/**
	 * 初始化动画
	 */
	private void InitImageView() {
		cursor = (ImageView)view.findViewById(R.id.cursor);
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

		// 获取分辨率宽度
		int screenW = dm.widthPixels;

		bmpW = (screenW/3);

		//设置动画图片宽度
		setBmpW(cursor, bmpW);
		offset = 0;

		//动画图片偏移量赋值
		position_one = (int) (screenW / 3.0);
		position_two = position_one * 2;
	}

	/**
	 * 初始化Fragment，并添加到ArrayList中
	 */
	private void InitFragment(){
		fragmentArrayList = new ArrayList<Fragment>();
		fragmentArrayList.add(new FragmentPicture());
		fragmentArrayList.add(new FragmentScreen());
		fragmentArrayList.add(new FragmentVideo());
		fragmentManager = getActivity().getSupportFragmentManager();
	}

	/**
	 * 头标点击监听
	 * @author weizhi
	 * @version 1.0
	 */
	public class MyOnClickListener implements View.OnClickListener{
		private int index = 0 ;
		public MyOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			mViewPager.setCurrentItem(index);
		}
	}

	/**
	 * 页卡切换监听
	 * @author weizhi
	 * @version 1.0
	 */
	public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener{

		@Override
		public void onPageSelected(int position) {
			Animation animation = null ;
			switch (position){

				//当前为页卡1
				case 0:
					//从页卡1跳转转到页卡2
					if(currIndex == 1){
						animation = new TranslateAnimation(position_one, 0, 0, 0);
						resetTextViewTextColor();
						pictureTextView.setTextColor(getResources().getColor(R.color.main_top_tab_color_2));
					}else if(currIndex == 2){//从页卡1跳转转到页卡3
						animation = new TranslateAnimation(position_two, 0, 0, 0);
						resetTextViewTextColor();
						pictureTextView.setTextColor(getResources().getColor(R.color.main_top_tab_color_2));
					}
					break;

				//当前为页卡2
				case 1:
					//从页卡1跳转转到页卡2
					if (currIndex == 0) {
						animation = new TranslateAnimation(offset, position_one, 0, 0);
						resetTextViewTextColor();
						movieTextView.setTextColor(getResources().getColor(R.color.main_top_tab_color_2));
					} else if (currIndex == 2) { //从页卡1跳转转到页卡2
						animation = new TranslateAnimation(position_two, position_one, 0, 0);
						resetTextViewTextColor();
						movieTextView.setTextColor(getResources().getColor(R.color.main_top_tab_color_2));
					}
					break;

				//当前为页卡3
				case 2:
					//从页卡1跳转转到页卡2
					if (currIndex == 0) {
						animation = new TranslateAnimation(offset, position_two, 0, 0);
						resetTextViewTextColor();
						musicTextView.setTextColor(getResources().getColor(R.color.main_top_tab_color_2));
					} else if (currIndex == 1) {//从页卡1跳转转到页卡2
						animation = new TranslateAnimation(position_one, position_two, 0, 0);
						resetTextViewTextColor();
						musicTextView.setTextColor(getResources().getColor(R.color.main_top_tab_color_2));
					}
					break;
			}
			currIndex = position;

			animation.setFillAfter(true);// true:图片停在动画结束位置
			animation.setDuration(300);
			cursor.startAnimation(animation);

		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

		}

		@Override
		public void onPageScrollStateChanged(int state) {

		}
	};

	/**
	 * 设置动画图片宽度
	 * @param mWidth
	 */
	private void setBmpW(ImageView imageView,int mWidth){
		ViewGroup.LayoutParams para;
		para = imageView.getLayoutParams();
		para.width = mWidth;
		imageView.setLayoutParams(para);
	}

	/**
	 * 将顶部文字恢复默认值
	 */
	private void resetTextViewTextColor(){

		pictureTextView.setTextColor(getResources().getColor(R.color.main_top_tab_color));
		movieTextView.setTextColor(getResources().getColor(R.color.main_top_tab_color));
		musicTextView.setTextColor(getResources().getColor(R.color.main_top_tab_color));
	}

}
