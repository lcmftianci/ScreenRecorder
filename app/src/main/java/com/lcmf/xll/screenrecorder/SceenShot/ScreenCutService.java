package com.lcmf.xll.screenrecorder.SceenShot;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.lcmf.xll.screenrecorder.ScreenApplication;

import java.io.File;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;

import static com.lcmf.xll.screenrecorder.MainActivity.TAG;

/**
 * Created by Administrator on 2017/7/27 0027.
 */

public class ScreenCutService extends Service {
	private MediaProjection mediaProjection;
	private ImageReader mImageReader;
	private String nameImage;
	private VirtualDisplay virtualDisplay;
	private String strDate;
	private MediaProjectionManager mMediaProjectionManager;

	private int windowHeight = 0;
	private int dpi;
	private int windowWidth = 0;
	private Intent mResultData;
	private DisplayMetrics metrics = null;
	private WindowManager mWindowManager1 = null;
	private int mResultCode;
	String pathImage = getSDCardPath() + "/ScreenCap/ScreenImage";

	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
	/**
	 * 获取SDCard的目录路径功能
	 */
	private static String getSDCardPath() {
		File sdcardDir = null;
		//判断SDCard是否存在
		boolean sdcardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		if (sdcardExist) {
			sdcardDir = Environment.getExternalStorageDirectory();
		}
		return sdcardDir.toString();
	}

	public void setConfig(int width, int height, int dpi) {
		this.windowWidth = width;
		this.windowHeight = height;
		this.dpi = dpi;
	}

	@TargetApi(Build.VERSION_CODES.M)
	private void createVirtualEnvironment(){
		dateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
		strDate = dateFormat.format(new java.util.Date());
		pathImage = Environment.getExternalStorageDirectory().getPath()+"/Pictures/";
		nameImage = pathImage+strDate+".png";
		mMediaProjectionManager = (MediaProjectionManager)getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
		mWindowManager1 = (WindowManager)getApplication().getSystemService(Context.WINDOW_SERVICE);
		windowWidth = mWindowManager1.getDefaultDisplay().getWidth();
		windowHeight = mWindowManager1.getDefaultDisplay().getHeight();
		metrics = new DisplayMetrics();
		mWindowManager1.getDefaultDisplay().getMetrics(metrics);
		dpi = metrics.densityDpi;
		mImageReader = ImageReader.newInstance(windowWidth, windowHeight, 0x1, 2); //ImageFormat.RGB_565

		Log.i(TAG, "prepared the virtual environment");
	}

	@TargetApi(Build.VERSION_CODES.M)
	private void startCapture(){
		strDate = dateFormat.format(new java.util.Date());
		nameImage = pathImage+strDate+".png";

		Image image = mImageReader.acquireLatestImage();
		int width = image.getWidth();
		int height = image.getHeight();
		final Image.Plane[] planes = image.getPlanes();
		final ByteBuffer buffer = planes[0].getBuffer();
		int pixelStride = planes[0].getPixelStride();
		int rowStride = planes[0].getRowStride();
		int rowPadding = rowStride - pixelStride * width;
		Bitmap bitmap = Bitmap.createBitmap(width+rowPadding/pixelStride, height, Bitmap.Config.ARGB_8888);
		bitmap.copyPixelsFromBuffer(buffer);
		bitmap = Bitmap.createBitmap(bitmap, 0, 0,width, height);
		image.close();
		Log.i(TAG, "image data captured");
	}

	//获取MediaProjection
	public void setMediaProject(MediaProjection project) {
		mediaProjection = project;
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return new RecordBinder();
	}

	public class RecordBinder extends Binder {
		public ScreenCutService getRecordService() {
			return ScreenCutService.this;
		}
	}

	@TargetApi(Build.VERSION_CODES.M)
	private void virtualDisplay(){
		WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		windowWidth = windowManager.getDefaultDisplay().getWidth();
		windowHeight = windowManager.getDefaultDisplay().getHeight();
		virtualDisplay = mediaProjection.createVirtualDisplay("screen-mirror",
				windowWidth, windowHeight, dpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
				mImageReader.getSurface(), null, null);
		Log.i(TAG, "virtual displayed");
	}

	@TargetApi(Build.VERSION_CODES.M)
	public void setUpMediaProjection(){
		mResultData = ((ScreenApplication)getApplication()).getIntent();
		mResultCode = ((ScreenApplication)getApplication()).getResult();
		mMediaProjectionManager = ((ScreenApplication)getApplication()).getMediaProjectionManager();
		mediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData);
		Log.i(TAG, "mMediaProjection defined");
	}
}
