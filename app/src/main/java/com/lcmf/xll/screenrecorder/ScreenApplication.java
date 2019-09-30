package com.lcmf.xll.screenrecorder;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;

import com.lcmf.xll.screenrecorder.SceenShot.RecordService;

/**
 * Created by Administrator on 2017/7/21 0021.
 */

public class ScreenApplication extends Application {
	private static ScreenApplication application;

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		application = this;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// 启动 Marvel service
		startService(new Intent(this, RecordService.class));
	}

	public static ScreenApplication getInstance() {
		return application;
	}

	private int result;
	private Intent intent;
	private MediaProjectionManager mMediaProjectionManager;

	public int getResult(){
		return result;
	}

	public Intent getIntent(){
		return intent;
	}

	public MediaProjectionManager getMediaProjectionManager(){
		return mMediaProjectionManager;
	}

	public void setResult(int result1){
		this.result = result1;
	}

	public void setIntent(Intent intent1){
		this.intent = intent1;
	}

	public void setMediaProjectionManager(MediaProjectionManager mMediaProjectionManager){
		this.mMediaProjectionManager = mMediaProjectionManager;
	}
}
