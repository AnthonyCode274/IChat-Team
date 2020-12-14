package com.example.ichat.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ichat.HauNguyen.Login.LoginActivity;
import com.example.ichat.R;
import com.example.ichat.adapter.AdapterPosts;
import com.example.ichat.models.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class Fragment_EditProfile extends Fragment {
    private TextView tvSave;
    private EditText edtEmail, edtUsername, edtFullname, edtPhoneNumber, edtBirthday;
    private RadioGroup radioGroup;
    private RadioButton male, female, other;
    private ProgressBar progress_bar_fm3;
    private CardView btn_edit_avatarProfile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_edit_profile, container, false);
        innitView(view);



        return view;
    }


    public String doGender() {
        String gen = null;
        if (male.isChecked()){
            gen = male.getText().toString();
        }else if (female.isChecked()){
            gen = female.getText().toString();
        }else if (other.isChecked()){
            gen = other.getText().toString();
        }
        return gen;
    }

    public void hideProgress(){
        progress_bar_fm3.setVisibility(View.GONE);

    }

    public void showProgress(){
        progress_bar_fm3.setVisibility(View.VISIBLE);

    }

    private void innitView(View view) {
        radioGroup = view.findViewById(R.id.radio_group);
        male = view.findViewById(R.id.rdo_male_edit);
        female = view.findViewById(R.id.rdo_female_edit);
        other = view.findViewById(R.id.rdo_others_edit);
        progress_bar_fm3 = view.findViewById(R.id.progress_bar_fm3);
        btn_edit_avatarProfile = view.findViewById(R.id.btn_edit_avatarProfile);
        tvSave = view.findViewById(R.id.tvSaveUpdateProfile);
    }

}