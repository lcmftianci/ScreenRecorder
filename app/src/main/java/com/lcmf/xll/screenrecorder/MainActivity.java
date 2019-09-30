package com.lcmf.xll.screenrecorder;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.lcmf.xll.screenrecorder.SceenShot.PermissionActivity;
import com.lcmf.xll.screenrecorder.SceenShot.RecordService;
import com.lcmf.xll.screenrecorder.SceenShot.ScreenCutService;
import com.lcmf.xll.screenrecorder.SceenShot.ScreenService;
import com.lcmf.xll.screenrecorder.SceenShot.ScreenShot;
import com.lcmf.xll.screenrecorder.SuspendWindow.SuspendWindowService;
import com.lcmf.xll.screenrecorder.ViewFragment.InnerFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import static com.lcmf.xll.screenrecorder.SceenShot.PermissionActivity.REQUEST_MEDIA_PROJECTION;
import static com.lcmf.xll.screenrecorder.ViewFragment.InnerFragment.IsVisible;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

	/*
	*   程序主窗口
	* */

	//定义notification实用的ID
	private static final int NO_CHICKEN =0x3;
	//主页内容成员
	FragmentManager manager;
	FragmentTransaction transaction;
	InnerFragment fragment1;

	//通知栏按钮成员
	public final static String ACTION_BTN = "com.lcmf.xll.btn.screenrecorder.start";
	public final static String INTENT_NAME = "btnid";
	public final static int INTENT_BTN_START = 1;
	public final static int INTENT_BTN_PAUSE = 2;
	public final static int INTENT_BTN_STOP = 3;
	public final static int SCREEN_SHOT = 8;

	//视频录制申请码
	public final static int SCREEN_SHOT_NEW = 123;

	//动态前线申请所需变量
	public final static int STORAGE_REQUEST_CODE = 1;
	public final static int AUDIO_REQUEST_CODE = 2;
	public final static int REQUEST_ALL = 3;
	List<String> permissionList = new ArrayList<>();

	public static int nHaz = 0;
	public final static String TAG = "MainActivity";
	NotificationBroadcastReceiver mReceiver;

	//视频录制需要的变量
	MediaProjection mediaProjection;
	MediaProjectionManager projectionManager;
	VirtualDisplay virtualDisplay;
	int mResultCode;
	Intent mData;
	ImageReader imageReader;
	private RecordService recordService;
	String imageName;
	Bitmap bitmap;
	int width;
	int height;
	int dpi;

	private TextView mTextMessage;

	//计时器
	private Handler stepTimeHandler;
	Calendar mCalendar;
	//String mFormat = "yyyy-MM-dd hh:mm:ss";//yyyy-MM-dd
	//String mFormat = "hh:mm:ss";
	String mFormat = "mm:ss";
	long startTime = 0;
	private Runnable mTicker;
	private TextView stepTimeTV;
	private Button btnTime;
	RemoteViews remoteViews;
	NotificationCompat.Builder mBuilder;
	NotificationManager mNotificationManager;
	private static boolean bRecoded = false;

	private Intent intent = null;
	private int result = 0;

	//fragment界面切换
	private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
			= new BottomNavigationView.OnNavigationItemSelectedListener() {

		@Override
		public boolean onNavigationItemSelected(@NonNull MenuItem item) {
			switch (item.getItemId()) {
				case R.id.navigation_home:
					mTextMessage.setText(R.string.title_home);
					if (fragment1 != null) {
						transaction = manager.beginTransaction();
						transaction.hide(fragment1);
						IsVisible = 0;
						transaction.commit();
					}
					return true;
				case R.id.navigation_dashboard:
					mTextMessage.setText(R.string.title_dashboard);
					if (IsVisible == 0) {
						showTips("---可显示---");
						if (nHaz == 1) {
							transaction = manager.beginTransaction();
							transaction.show(fragment1);
							showTips("---显示fragment---");
						}
						if (nHaz == 0) {
							manager = getSupportFragmentManager();
							transaction = manager.beginTransaction();
							fragment1 = new InnerFragment();
							nHaz = 1;
							transaction.add(R.id.content, fragment1);
							showTips("---可添加---");
						}
						IsVisible = 1;
						transaction.commit();
					}
					return true;
			}
			return false;
		}

	};


	/*创建窗口*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		/*悬浮窗*/
		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
						.setAction("Action", null).show();
			}
		});

		/*NavigationLayout*/
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.setDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

		mTextMessage = (TextView) findViewById(R.id.message);
		BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
		navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

		//通知栏显示
		notification();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			// 使用api11 新加 api
			requestDrawOverLays();
		}

		applyPermission();

		//截屏获取屏幕的信息
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		width = metric.widthPixels;
		height = metric.heightPixels;
		dpi = metric.densityDpi;
		projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
		Intent intent = new Intent(this, RecordService.class);
		bindService(intent, connection, BIND_AUTO_CREATE);
	}


	//截屏服务
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void startIntent(){
		if(intent != null && result != 0){
			((ScreenApplication)getApplication()).setResult(result);
			((ScreenApplication)getApplication()).setIntent(intent);
			Intent intent = new Intent(getApplicationContext(), ScreenService.class);
			showTips("启动服务");
			startService(intent);
		}else{
			startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
			((ScreenApplication)getApplication()).setMediaProjectionManager(projectionManager);
		}
	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		// Handle navigation view item clicks here.
		int id = item.getItemId();
		if (id == R.id.nav_camera) {
			showTips("已打开悬浮窗");
			Intent intent = new Intent(MainActivity.this, SuspendWindowService.class);
			startService(intent);
		} else if (id == R.id.nav_gallery) {
			showTips("gallery");
		} else if (id == R.id.nav_slideshow) {
			//截取Activity
			showTips("截取Activity");
			moveTaskToBack(true);
			String filePath = Environment.getExternalStorageDirectory() + "/DCIM/"
					+ getDateTime() + ".png";
			ScreenShot.shoot(MainActivity.this, new File(filePath));

		} else if (id == R.id.nav_manage) {
			//启动截屏服务
			showTips("正在打开服务");
			startIntent();
		} else if (id == R.id.nav_share) {

		} else if (id == R.id.nav_send) {
			showTips("send");
		}

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	public void showTips(String strInfo) {
		Toast.makeText(MainActivity.this, strInfo, Toast.LENGTH_SHORT).show();
	}

	/*
	* 通知栏函数集合
	* */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregeisterReceiver();
		unbindService(connection);
	}

	private void notification() {
		unregeisterReceiver();
		intiReceiver();

		remoteViews = new RemoteViews(getPackageName(), R.layout.notification);
		remoteViews.setTextViewText(R.id.tv_up, "录屏精灵");
		remoteViews.setTextViewText(R.id.tv_down, "通知栏控制");
		if(bRecoded)
		{
			remoteViews.setViewVisibility(R.id.btn_start, View.GONE);
			remoteViews.setViewVisibility(R.id.btn_stop, View.VISIBLE);
			remoteViews.setViewVisibility(R.id.btn_pause, View.VISIBLE);
			remoteViews.setImageViewResource(R.id.btn_stop, R.drawable.btn_stop);
			remoteViews.setImageViewResource(R.id.btn_pause, R.drawable.btn_pause);
			bRecoded = false;
		}else {
			remoteViews.setViewVisibility(R.id.btn_pause, View.GONE);
			remoteViews.setViewVisibility(R.id.btn_stop, View.GONE);
			remoteViews.setViewVisibility(R.id.btn_start, View.VISIBLE);
			remoteViews.setImageViewResource(R.id.btn_start, R.drawable.btn_play);
			bRecoded = true;
		}
		Intent intentStart = new Intent(ACTION_BTN);
		intentStart.putExtra(INTENT_NAME, INTENT_BTN_START);
		PendingIntent intentStartpi = PendingIntent.getBroadcast(this, 1, intentStart, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.btn_start, intentStartpi);
		Intent intentPause = new Intent(ACTION_BTN);
		intentPause.putExtra(INTENT_NAME, INTENT_BTN_PAUSE);
		PendingIntent intentPausepi = PendingIntent.getBroadcast(this, 2, intentPause, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.btn_pause, intentPausepi);
		Intent intentStop = new Intent(ACTION_BTN);
		intentStop.putExtra(INTENT_NAME, INTENT_BTN_STOP);
		PendingIntent intentStoppi = PendingIntent.getBroadcast(this, 3, intentStop, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.btn_stop, intentStoppi);
		Intent intent2 = new Intent();
		intent2.setClass(this, MainActivity.class);
		intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent intentContent = PendingIntent.getActivity(this, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder = new NotificationCompat.Builder(this);
		mBuilder.setOngoing(true);
		mBuilder.setAutoCancel(false);
		mBuilder.setContent(remoteViews);
		mBuilder.setPriority(Notification.PRIORITY_DEFAULT);
		mBuilder.setWhen(System.currentTimeMillis());
		mBuilder.setTicker("录屏精灵");
		mBuilder.setSmallIcon(R.drawable.id_airport);

		Notification notification = mBuilder.build();
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		notification.contentIntent = intentContent;

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(NO_CHICKEN, notification);
	}
	private void intiReceiver() {
		mReceiver = new NotificationBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION_BTN);
		getApplicationContext().registerReceiver(mReceiver, intentFilter);
	}
	private void unregeisterReceiver() {
		if (mReceiver != null) {
			getApplicationContext().unregisterReceiver(mReceiver);
			mReceiver = null;
		}
	}

	/*通知栏按钮响应*/
	class NotificationBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ACTION_BTN)) {
				int btn_id = intent.getIntExtra(INTENT_NAME, 0);
				switch (btn_id) {
					case INTENT_BTN_START:
						bRecoded = true;
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
							if (recordService.isRunning()) {
								recordService.stopRecord();
							} else {
								Toast.makeText(MainActivity.this, "正在录制视频", Toast.LENGTH_SHORT).show();
								Intent captureIntent = projectionManager.createScreenCaptureIntent();
								startActivityForResult(captureIntent, SCREEN_SHOT_NEW);
							}
						} else
							showTips("安卓系统版本低！");
						notification();
						break;

					case INTENT_BTN_PAUSE:
						bRecoded = false;
						Toast.makeText(MainActivity.this, "从通知栏点登录", Toast.LENGTH_SHORT).show();
						unregeisterReceiver();
						NotificationManager notificationManager = (NotificationManager) MainActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);
						notificationManager.cancel(0);
						break;
					case INTENT_BTN_STOP:
						bRecoded = false;
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
							if (recordService.isRunning()) {
								recordService.stopRecord();
								Toast.makeText(MainActivity.this, "录制结束", Toast.LENGTH_SHORT).show();
							}
						} else
							showTips("安卓系统版本低！");
						notification();
						break;
				}
			}
		}
	}



	/*权限获取*/
	public static int OVERLAY_PERMISSION_REQ_CODE = 1234;
	@TargetApi(Build.VERSION_CODES.M)
	public void requestDrawOverLays() {
		if (!Settings.canDrawOverlays(MainActivity.this)) {
			Toast.makeText(this, "can not DrawOverlays", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + MainActivity.this.getPackageName()));
			startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
		} else {
			// Already hold the SYSTEM_ALERT_WINDOW permission, do addview or something.
		}
	}

	@TargetApi(Build.VERSION_CODES.M)
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
			if (!Settings.canDrawOverlays(this)) {
				// SYSTEM_ALERT_WINDOW permission not granted...
				Toast.makeText(this, "Permission Denieddd by user.Please Check it in Settings", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "Permission Allowed", Toast.LENGTH_SHORT).show();
				// Already hold the SYSTEM_ALERT_WINDOW permission, do addview or something.
			}
		} else if (requestCode == SCREEN_SHOT_NEW) {
			mediaProjection = projectionManager.getMediaProjection(resultCode, data);
			recordService.setMediaProject(mediaProjection);
			recordService.startRecord();
		}else if (requestCode == REQUEST_MEDIA_PROJECTION) {
//			if (resultCode != Activity.RESULT_OK) {
//				return;
//			}else if(data != null && resultCode != 0){
			if(data != null && resultCode != 0){
				result = resultCode;
				intent = data;
				((ScreenApplication)getApplication()).setResult(resultCode);
				((ScreenApplication)getApplication()).setIntent(data);
				Intent intent = new Intent(getApplicationContext(), ScreenService.class);
				showTips("启动服务结果");
				startService(intent);
				finish();
			}
		}
// else if(requestCode == SCREEN_SHOT){
//				if(resultCode == RESULT_OK){
//					mResultCode = resultCode;
//					mData = data;
//					setUpMediaProjection();
//					setUpVirtualDisplay();
//					startCapture();
//				}
//			}
	}



	/*
	* 截屏方式一
	*   View view = activity.getWindow().getDecorView();
		//      Enables or disables the drawing cache
        view.setDrawingCacheEnabled(true);
		//      will draw the view in a bitmap
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        //		WindowManager windowManager = getWindowManager();
//		Display display = windowManager.getDefaultDisplay();
//		int w = display.getWidth();
//		int h = display.getHeight();
		//get_root_view(MainActivity.this).setVisibility(View.GONE);
	* */


	/*截屏功能*/
	private void GetandSaveCurrentImage() {
		//隐藏程序窗口
		//1.构建Bitmap
		WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		int w = windowManager.getDefaultDisplay().getWidth();
		int h = windowManager.getDefaultDisplay().getHeight();
		Bitmap Bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

		//2.获取屏幕
		View decorview = this.getWindow().getDecorView();
		decorview.setDrawingCacheEnabled(true);
		Bmp = decorview.getDrawingCache();

		String SavePath = getSDCardPath() + "/ScreenCap/ScreenImage";
		//3.保存Bitmap
		try {
			File path = new File(SavePath);
			//获取当前时间
			String strDate = getDateTime();

			//文件
			String filepath = SavePath + "/Screen" + "-" + getDateTime() + ".png";
			File file = new File(filepath);
			if (!path.exists()) {
				path.mkdirs();
			}
			if (!file.exists()) {
				file.createNewFile();
			}

			FileOutputStream fos = null;
			fos = new FileOutputStream(file);
			if (null != fos) {
				Bmp.compress(Bitmap.CompressFormat.PNG, 90, fos);
				fos.flush();
				fos.close();
				Toast.makeText(MainActivity.this, "截屏文件保存至" + SavePath + "下", Toast.LENGTH_LONG).show();
				//get_root_view(MainActivity.this).setVisibility(View.VISIBLE);
			}

		} catch (Exception e) {
			e.printStackTrace();
			//get_root_view(MainActivity.this).setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 获取SDCard的目录路径功能
	 */
	private String getSDCardPath() {
		File sdcardDir = null;
		//判断SDCard是否存在
		boolean sdcardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		if (sdcardExist) {
			sdcardDir = Environment.getExternalStorageDirectory();
		}
		return sdcardDir.toString();
	}

	/*权限申请函数*/
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case REQUEST_ALL:
				if (grantResults.length > 0) {
					for (int result : grantResults) {
						if (result != PackageManager.PERMISSION_DENIED) {
							Toast.makeText(MainActivity.this, "同意以上权限", Toast.LENGTH_SHORT).show();
						}
					}

				} else {
					Toast.makeText(MainActivity.this, "权限申请失败", Toast.LENGTH_SHORT).show();
				}
				break;
			default:
				break;
		}
	}

	/*
		创建隐藏与现实view的函数
		get_root_view(context).setVisibility(View.GONE);  //影藏 view
		get_root_view(context).setVisibility(View.VISIBLE);  //显示view
	*/
	private static View get_root_view(Activity context) {
		return ((ViewGroup) context.findViewById(android.R.id.content)).getChildAt(0);
	}

	/*
		获取当前系统时间
	 */
	public static String getDateTime() {
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String date = sDateFormat.format(new java.util.Date());
		return date;
	}

//	moveTaskToBack(true);

	/*
	*
	* 截屏方法二
	* */
//	@TargetApi(Build.VERSION_CODES.M)
//	private void startCapture() {
//		SystemClock.sleep(1000);
//		imageName = System.currentTimeMillis() + ".png";
//		Image image = imageReader.acquireNextImage();
//		if (image == null) {
//			Log.e(TAG, "image is null.");
//			return;
//		}
//		int width = image.getWidth();
//		int height = image.getHeight();
//		final Image.Plane[] planes = image.getPlanes();
//		final ByteBuffer buffer = planes[0].getBuffer();
//		int pixelStride = planes[0].getPixelStride();
//		int rowStride = planes[0].getRowStride();
//		int rowPadding = rowStride - pixelStride * width;
//		bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
//		bitmap.copyPixelsFromBuffer(buffer);
//		image.close();
//
//		String SavePath = getSDCardPath()+"/ScreenCap/ScreenImage";
//		Log.v("---MainActivity---", SavePath);
//		//3.保存Bitmap
//		try {
//			File path = new File(SavePath);
//			//获取当前时间
//			String strDate = getDateTime();
//
//			//文件
//			String filepath = SavePath + "/Screen" + "-" + getDateTime()+".png";
//			Log.v("---MainActivity---", filepath);
//			File file = new File(filepath);
//			if(!path.exists()){
//				path.mkdirs();
//			}
//			if (!file.exists()) {
//				file.createNewFile();
//				Log.v("---MainActivity---", "创建文件");
//			}
//
//			FileOutputStream fos = null;
//			fos = new FileOutputStream(file);
//			if (null != fos) {
//				bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
//				fos.flush();
//				fos.close();
//				Toast.makeText(MainActivity.this, "截屏文件保存至" + SavePath + "下", Toast.LENGTH_LONG).show();
//				//get_root_view(MainActivity.this).setVisibility(View.VISIBLE);
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			//get_root_view(MainActivity.this).setVisibility(View.VISIBLE);
//		}
//
////		if (bitmap != null) {
////			imageView.setImageBitmap(bitmap);
////		}
//	}
//	@TargetApi(Build.VERSION_CODES.M)
//	private void setUpVirtualDisplay() {
//		imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1);
////		mediaProjection.createVirtualDisplay("ScreenShout",
////				width,height,dpi,
////				DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
////				imageReader.getSurface(),null,null);
//
//				mediaProjection.createVirtualDisplay("ScreenShout",
//				width,height,dpi,
//				DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
//				imageReader.getSurface(),null,null);
//	}
//	@TargetApi(Build.VERSION_CODES.M)
//	private void setUpMediaProjection(){
//		mediaProjection = projectionManager.getMediaProjection(mResultCode,mData);
//	}
//	@TargetApi(Build.VERSION_CODES.M)
//	public void StartScreenShot() {
//		startActivityForResult(projectionManager.createScreenCaptureIntent(),SCREEN_SHOT);
//	}

	//动态申请权限函数
	public void applyPermission() {
		if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			//ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
			permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
		}
		if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
				!= PackageManager.PERMISSION_GRANTED) {
			//ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECORD_AUDIO}, AUDIO_REQUEST_CODE);
			permissionList.add(Manifest.permission.RECORD_AUDIO);
		}

		if (!permissionList.isEmpty()) {
			String[] permissions = permissionList.toArray(new String[permissionList.size()]);
			ActivityCompat.requestPermissions(MainActivity.this, permissions, REQUEST_ALL);
		}
	}


	/*录屏实现*/
	private ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			RecordService.RecordBinder binder = (RecordService.RecordBinder) service;
			recordService = binder.getRecordService();
			recordService.setConfig(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}
	};

	public static final int UPDATE_TEXT = 331;

	//多线程用于更新通知栏显示的时间
//	private Handler handler = new Handler() {
//		@Override
//		public void handleMessage(Message msg) {
//			switch (msg.what) {
//				case UPDATE_TEXT:
//					//这里尽心UI操作	//显示录制时间
//					remoteViews.setTextViewText(R.id.btn_stop, "停止录制");
//					if (!bRecoded) {
//						bRecoded = true;
//						remoteViews.setTextViewText(R.id.btn_stop, "停止录制");
//						// 清零 开始计时
//						remoteViews.setTextViewText(R.id.id_timer, "00:00:00");
//						if (mCalendar == null) {
//							mCalendar = Calendar.getInstance();
//							TimeZone tz = TimeZone.getTimeZone("GMT");//GMT+8
//							mCalendar.setTimeZone(tz);
//							mCalendar.get(Calendar.HOUR_OF_DAY);//24小时制
//						}
//						stepTimeHandler = new Handler();
//						startTime = System.currentTimeMillis();
//						mTicker = new Runnable() {
//							public void run() {
//								//这个减出来的日期是1970年的  时间格式不能出现00:00:00 12:00:00
//								long showTime = System.currentTimeMillis() - startTime;
//								Log.i(TAG, showTime + "");
//								mCalendar.setTimeInMillis(showTime + 13 * 3600000 + 1000);
//								String content = (String) DateFormat.format(mFormat, mCalendar);
//								remoteViews.setTextViewText(R.id.id_timer, content);
//								long now = SystemClock.uptimeMillis();
//								long next = now + (1000 - now % 1000);
//								stepTimeHandler.postAtTime(mTicker, next);
//							}
//						};
//						//启动计时线程，定时更新
//						mTicker.run();
//					} else {
//						bRecoded = false;
//						remoteViews.setTextViewText(R.id.btn_stop, "开始录制");
//						//停止计时 Remove any pending posts of Runnable r that are in the message queue.
//						stepTimeHandler.removeCallbacks(mTicker);
//					}
//					break;
//				default:
//					break;
//			}
//			//super.handleMessage(msg);
//		}
//	};
}
