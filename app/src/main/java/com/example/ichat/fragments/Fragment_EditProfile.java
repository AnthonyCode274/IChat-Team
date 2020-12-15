package com.example.ichat.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ichat.HauNguyen.DAO.UserDAO;
import com.example.ichat.HauNguyen.Login.LoginActivity;
import com.example.ichat.R;
import com.example.ichat.adapter.AdapterPosts;
import com.example.ichat.models.Post;
import com.example.ichat.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class Fragment_EditProfile extends Fragment {
    private TextView tvSave;
    private EditText edtUsername, edtFullname, edtPhoneNumber, edtBirthday;
    private RadioGroup radioGroup;
    private RadioButton male, female, other;
    private ProgressBar progress_bar_fm3;
    private LinearLayout btn_exit_update_profile;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    //image pick constants
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    //Firebase query
    private DatabaseReference mDatabase;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    StorageReference storageReference;
    //path where images of user profile and cover will be stored
    String uid;
    UserDAO userDAO;
    User users;

    //String getData from firebase
    private String emailFB, userNameFB, fullNameFB, phoneNumberFB, birthdayFB, genderFB;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_edit_profile, container, false);
        innitView(view);
        //--------------Query Firebase
        //init firebase
        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = firebaseDatabase.getReference("Users");
        storageReference = getInstance().getReference(); //firebase storage reference



        //--------------Image avatar preferment START
        Query query = mDatabase.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //checkc until required data get
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //get data
                    fullNameFB = "" + ds.child("name").getValue();
                    userNameFB = "" + ds.child("username").getValue();
                    emailFB = "" + ds.child("email").getValue();
                    phoneNumberFB = "" + ds.child("phone").getValue();
                    genderFB = "" + ds.child("gender").getValue();
                    birthdayFB = "" + ds.child("birthday").getValue();

                    //set data
                    edtUsername.setText(userNameFB);
                    edtFullname.setText(fullNameFB);
                    edtPhoneNumber.setText(phoneNumberFB);
                    edtBirthday.setText(birthdayFB);
                    setGenders(genderFB);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });        //--------------ENT


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                doGender();
            }
        });

        tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usernames = edtUsername.getText().toString().trim();
                String fullnames = edtFullname.getText().toString().trim();
                String phones = edtPhoneNumber.getText().toString().trim();
                String birthdays = edtBirthday.getText().toString().trim();
                String genders = doGender();

                Toast.makeText(getActivity(), fullNameFB + "\n" + userNameFB + "\n" + emailFB + "\n" + phoneNumberFB + "\n" + genderFB + "\n" + birthdayFB + "\n", Toast.LENGTH_SHORT).show();

                HashMap<String, Object> result = new HashMap<>();
                result.put("name", fullnames);
                result.put("username", usernames);
                result.put("gender", genders);
                result.put("birthday", birthdays);
                mDatabase.child(user.getUid()).updateChildren(result)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //updated, dismiss progress
                                Toast.makeText(getActivity(), "Update Successfully!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //failed, dismiss progress, get and show error message
                                Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        checkUserStatus();

        btn_exit_update_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content, new Fragment_Setting(), "TAG")
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }

    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here
            //set email of logged in user
            //mProfileTv.setText(user.getEmail());
            uid = user.getUid();
        } else {
            //user not signed in, go to main acitivity
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }


    public void setGenders(String genders){
        if (genders.equals("Male")){
            male.setChecked(true);
        }else if (genders.equals("Female")){
            female.setChecked(true);
        }else {
            other.setChecked(true);
        }
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
        tvSave = view.findViewById(R.id.tvSaveUpdateProfile);
        edtUsername = view.findViewById(R.id.txt_edit_username);
        edtBirthday = view.findViewById(R.id.txt_edit_birthday);
        edtFullname = view.findViewById(R.id.txt_edit_fullname);
        edtPhoneNumber = view.findViewById(R.id.txt_edit_phoneNumber);
        radioGroup = view.findViewById(R.id.radio_group_edit_profile);
        male = view.findViewById(R.id.rdo_male_edit);
        female = view.findViewById(R.id.rdo_female_edit);
        other = view.findViewById(R.id.rdo_others_edit);
        progress_bar_fm3 = view.findViewById(R.id.progressBar_edit_profile);
        btn_exit_update_profile = view.findViewById(R.id.btn_exit_update_profile);
    }

}