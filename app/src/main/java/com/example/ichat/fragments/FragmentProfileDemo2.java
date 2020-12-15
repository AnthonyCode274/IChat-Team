package com.example.ichat.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import com.example.ichat.R;

public class FragmentProfileDemo2 extends Fragment {
    private ImageView imgCover, imgAvatar;
    private LinearLayout btnHistory, btnPhoto, btnFriends;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.customprofile2, container, false);
    }
}
