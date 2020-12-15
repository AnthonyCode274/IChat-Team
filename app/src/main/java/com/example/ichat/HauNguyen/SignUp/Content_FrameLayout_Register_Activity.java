package com.example.ichat.HauNguyen.SignUp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ichat.DashboardActivity;
import com.example.ichat.HauNguyen.Login.LoginActivity;
import com.example.ichat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class Content_FrameLayout_Register_Activity extends AppCompatActivity {
    private static final String TAG = "Content_Layout_Controller";
    private ImageView img1;
    private Button tvSignIn;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_fragment_register);
        innitView();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");

        //start first fragment (fragment1)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fr_layout_register, new Fragment4_GetPassword()).commit();
        }

        tvSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Content_FrameLayout_Register_Activity.this.finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    private void checkUserStatus() {
        try {
            //get current user
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
            } else {
                //user not signed in, go to main acitivity
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        }catch (Exception e){
            Toast.makeText(this, "User is not register!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

    }


    private void innitView() {
        img1 = findViewById(R.id.img1);
        tvSignIn = findViewById(R.id.tvSignIn);


    }
}
