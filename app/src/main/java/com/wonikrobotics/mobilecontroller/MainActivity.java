package com.wonikrobotics.mobilecontroller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class MainActivity extends Activity {
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
        ImageView logo = (ImageView) findViewById(R.id.woniklogo);
        ImageView robotics = (ImageView) findViewById(R.id.robotics);
        logo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        robotics.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        handler = new Handler();
        handler.postDelayed(goOver, 3000);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

}
