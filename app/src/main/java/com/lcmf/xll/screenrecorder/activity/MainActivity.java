package com.lcmf.xll.screenrecorder.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.lcmf.xll.screenrecorder.R;
import com.lcmf.xll.screenrecorder.ScUtil;
import com.lcmf.xll.screenrecorder.tinyutil.ScreenUtil;

public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_CODE = 123;

    //检测相关全县
    public static void checkPermission(AppCompatActivity activity) {
        //如果api 大于23就检测权限
        if (Build.VERSION.SDK_INT >= 23) {
            //check录音，读取通讯录，读写内存卡权限
            int checkPermission =
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
                            + ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE)
                            + ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            + ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
            //如果没有权限，就动态申请一下
            if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                //动态申请
                ActivityCompat.requestPermissions(activity, new String[]{
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                return;
            } else {
                return;
            }
        }
        return;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){
            try {
                ScreenUtil.setUpData(resultCode, data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            ScUtil.show(this,"您拒绝了录屏请求");
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_activity_main);
        checkPermission(this);  //检测权限
    }
}
