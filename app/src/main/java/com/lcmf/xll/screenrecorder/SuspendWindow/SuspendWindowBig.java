package com.lcmf.xll.screenrecorder.SuspendWindow;

import android.os.Environment;
import android.widget.LinearLayout;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.lcmf.xll.screenrecorder.R;
import com.lcmf.xll.screenrecorder.SceenShot.ScreenShot;
import com.lcmf.xll.screenrecorder.ScreenApplication;

import java.io.File;

import static com.lcmf.xll.screenrecorder.MainActivity.getDateTime;

/**
 * Created by Administrator on 2017/7/16 0016.
 */

public class SuspendWindowBig extends LinearLayout {
	/**
	 * 记录大悬浮窗的宽度
	 */
	public static int viewWidth;

	/**
	 * 记录大悬浮窗的高度
	 */
	public static int viewHeight;

	public SuspendWindowBig(final Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.float_window_big, this);
		View view = findViewById(R.id.big_window_layout);
		viewWidth = view.getLayoutParams().width;
		viewHeight = view.getLayoutParams().height;
		Button close = (Button) findViewById(R.id.close);
		Button back = (Button) findViewById(R.id.back);
		close.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 点击关闭悬浮窗的时候，移除所有悬浮窗，并停止Service
				SuspendWindowManager.removeBigWindow(context);
				SuspendWindowManager.removeSmallWindow(context);
				Intent intent = new Intent(getContext(), SuspendWindowService.class);
				context.stopService(intent);
			}
		});
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 点击返回的时候，移除大悬浮窗，创建小悬浮窗
				SuspendWindowManager.removeBigWindow(context);
				SuspendWindowManager.createSmallWindow(context);
			}
		});
	}


}
