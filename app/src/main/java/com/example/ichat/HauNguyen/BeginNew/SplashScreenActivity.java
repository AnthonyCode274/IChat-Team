package com.example.ichat.HauNguyen.BeginNew;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ichat.DashboardActivity;
import com.example.ichat.HauNguyen.FragmentHome;
import com.example.ichat.R;


public class SplashScreenActivity extends AppCompatActivity {
    Animation sideAnim;
    private static int SPLASH_SCREEN_TIMEOUT = 2000;
    ImageView imgLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Hooks
        imgLogo = findViewById(R.id.img_logo);

        //Animations
        sideAnim = AnimationUtils.loadAnimation(this, R.anim.open_logo);

        //set animation
        imgLogo.setAnimation(sideAnim);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (restorePrefData()){
                        startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                        finish();
                    }else {
                        startActivity(new Intent(getApplicationContext(), OnBroading.class));
                        finish();
                    }
                } catch (Exception e) {
                    Log.d("TAG", "Error Run SplashScreenActivity == " + e.toString());
                }
                finish();
            }
        }, SPLASH_SCREEN_TIMEOUT);

    }

    private boolean restorePrefData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs", MODE_PRIVATE);
        Boolean isIntroActivityOpnendBefore = pref.getBoolean("isOnlyOne_OnBroading", false);
        return isIntroActivityOpnendBefore;
    }

}