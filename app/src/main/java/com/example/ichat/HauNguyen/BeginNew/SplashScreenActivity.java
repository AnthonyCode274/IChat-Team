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
import com.example.ichat.HauNguyen.Login.LoginActivity;
import com.example.ichat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class SplashScreenActivity extends AppCompatActivity {
    private static int SPLASH_SCREEN_TIMEOUT = 2000;
    Animation sideAnim;
    ImageView imgLogo;
    FirebaseUser user;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mAuth = FirebaseAuth.getInstance();
        checkUserStatus();
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
                    if (restorePrefData()) {
                        startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                        finish();
                    } else {
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

    private void checkUserStatus() {
        try {
            //get current user
            user = mAuth.getCurrentUser();
            if (user != null) {
                startActivity(new Intent(SplashScreenActivity.this, DashboardActivity.class));
                finish();
            }
        } catch (Exception e) {
            //user not signed in, go to main acitivity
            startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
            finish();
        }

    }


    private boolean restorePrefData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs", MODE_PRIVATE);
        Boolean isIntroActivityOpnendBefore = pref.getBoolean("isOnlyOne_OnBroading", false);
        return isIntroActivityOpnendBefore;
    }

}