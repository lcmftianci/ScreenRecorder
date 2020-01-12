package net.yrom.screenrecorder;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.util.Random;

public class StartActivity extends Activity {
    private final String TAG = "---SplashActivity---";

    //加载三个动画
    Animation mFadeIn;
    Animation mFadeInScale;
    Animation mFadeOut;

    //图片控件
    ImageView mImageView;

    //背景图片
    View mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash_activity);

        mImageView = (ImageView)findViewById(R.id.image);
        //mView = (View)findViewById(R.id.idview);


        int[] arrImage = {
                R.drawable.cake,
                R.drawable.entrance3,
                R.drawable.sky,
                R.drawable.snow,
                R.drawable.tower,
                R.drawable.cl,
                R.drawable.tt,
                R.drawable.dy
        };
        Random r = new Random();
        int ran1 = r.nextInt(arrImage.length);
        Log.v(TAG, "number:" + String.valueOf(ran1));
        mImageView.setImageResource(arrImage[ran1]);

        //1、先判断版本是不是首次运行
        SharedPreferences mVersion = getSharedPreferences("version", MODE_PRIVATE);
        int nVerTimes = mVersion.getInt("times", 0);
//        if(Integer.compare(nVerTimes, 1) != 0){
            SharedPreferences.Editor editor = getSharedPreferences("version", MODE_PRIVATE).edit();
            editor.putInt("times", 1);
            editor.apply();
//        }else {
//            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
//            startActivity(intent);
//            finish();
//        }

        //打印日志
        Log.v(TAG, "加载Splash动画");

        //2.初始化动画
        initAnim();

        //3.初始化响应
        initListener();
    }

    private void initAnim(){
        mFadeIn = AnimationUtils.loadAnimation(this, R.anim.welcome_fade_in);
        mFadeIn.setDuration(500);
        mFadeInScale = AnimationUtils.loadAnimation(this, R.anim.welcome_fade_in_scale);
        mFadeInScale.setDuration(2000);
        mFadeOut = AnimationUtils.loadAnimation(this, R.anim.welcome_fade_out);
        mFadeOut.setDuration(500);
        mImageView.startAnimation(mFadeIn);
    }

    private void initListener(){
        mFadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mImageView.startAnimation(mFadeInScale);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mFadeInScale.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mImageView.startAnimation(mFadeOut);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }
}
