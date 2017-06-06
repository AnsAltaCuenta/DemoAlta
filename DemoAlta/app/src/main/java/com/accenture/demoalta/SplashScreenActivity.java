package com.accenture.demoalta;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by karen.torres on 01/06/2017.
 */

public class SplashScreenActivity extends Activity{
    private static final long SPLASH_SCREEN_DELAY = 3000;
    private static final String TAG = SplashScreenActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);

        Log.d(TAG,"Splash inicio");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.splash_screen);

        Log.d(TAG,"Splash Time");
        TimerTask screen = new TimerTask() {
            @Override
            public void run() {
                Intent screenIntent = new Intent().setClass(
                        SplashScreenActivity.this, MainActivity.class);
                startActivity(screenIntent);
                Log.d(TAG,"Splash finish");
                finish();
            }
        };
        Timer tm = new Timer();
        tm.schedule(screen, SPLASH_SCREEN_DELAY);
        Log.d(TAG,"Splash fin");

    }
}
