package com.wonikrobotics.pathfinder.mc.mobilecontroller;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
    ImageView logo, robotics;
    LinearLayout loading;
    Handler handler;
    Runnable goOver = new Runnable() {
        @Override
        public void run() {
            Intent selectRobot = new Intent(MainActivity.this, SelectRobot.class);
            selectRobot.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
            startActivity(selectRobot);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loading = (LinearLayout) findViewById(R.id.loading_layout);
        logo = (ImageView) findViewById(R.id.woniklogo);
        robotics = (ImageView) findViewById(R.id.robotics);
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inSampleSize = 2;
        Bitmap rawLogo = BitmapFactory.decodeResource(getResources(), R.drawable.logo, option);
        Bitmap rawText = BitmapFactory.decodeResource(getResources(), R.drawable.robotics, option);
        logo.setImageBitmap(rawLogo);
        robotics.setImageBitmap(rawText);
        robotics.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        logo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));

        handler = new Handler();
        handler.postDelayed(goOver, 3000);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handler.removeCallbacks(goOver);
    }

}
