package com.bms.electricenergymeter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity  extends AppCompatActivity {

    private static final long SPLASH_DELAY = 3000; // 延迟时间（毫秒）

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            // 在延迟后跳转到主界面
            Intent intent = new Intent(SplashActivity .this, MainActivity.class);
            startActivity(intent);
            finish(); // 关闭启动活动，避免用户返回到启动画面
        }, SPLASH_DELAY);
    }
}