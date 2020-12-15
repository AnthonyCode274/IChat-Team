package com.example.ichat.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ichat.HauNguyen.Login.LoginActivity;
import com.example.ichat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Fragment_Setting extends Fragment {
    LinearLayout llBack;
    RelativeLayout btn_editProfile, rlt_changePassword, rlt_logout;
    FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment__setting, container, false);
        innitView(view);

        mAuth = FirebaseAuth.getInstance();

        llBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content, new ProfileFragment(), "TAG")
                        .addToBackStack(null)
                        .commit();
            }
        });

        btn_editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content, new Fragment_EditProfile(), "TAG")
                        .addToBackStack(null)
                        .commit();
            }
        });

        rlt_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                checkUserStatus();
            }
        });



        return view;
    }

    private void checkUserStatus() {
        //get current user
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here
            //set email of logged in user
            //mProfileTv.setText(user.getEmail());
        } else {
            //user not signed in, go to main acitivity
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }

    private void innitView(View view) {
        llBack = view.findViewById(R.id.lnl_backSetting);
        btn_editProfile = view.findViewById(R.id.btn_editProfile);
        rlt_changePassword = view.findViewById(R.id.rlt_changePassword);
        rlt_logout = view.findViewById(R.id.rlt_logout);
    }
}