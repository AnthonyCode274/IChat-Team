package com.example.ichat.HauNguyen.SignUp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ichat.HauNguyen.DAO.UserDAO;
import com.example.ichat.HauNguyen.Login.LoginActivity;
import com.example.ichat.R;
import com.example.ichat.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Fragment4_GetPassword extends Fragment {
    private static final String TAG = "Fragment4_GetPassword";
    public EditText txt_Email, edtPassword;
    String email, gender, birthday, username, fullName, password;
    //Firebase
    String userID;
    private Button btnFm5;
    private ProgressBar progressBar_fm5;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore fStore;

    UserDAO userDAO;
    User user_profile;

    public static boolean isValidPassword(String password) {
        //begin
        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);
        return matcher.matches();
        //end
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_fragment4_password, container, false);
        innitView(view);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();


        //loading dialog

        btnFm5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    showProgress();
                    String newPass = edtPassword.getText().toString().trim();
                    if (newPass.length() < 6 || !isValidPassword(newPass)) {
                        Toast.makeText(getActivity(), "Invalid or incorrect password, please try again!", Toast.LENGTH_SHORT).show();
                        hideProgress();
                        return;
                    } else {
                        createUserAuthentication();
                    }
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "It cannot be done!", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error ==/ ");
                }
            }

        });

        return view;
    }

    private void createUserAuthentication() {
        email = txt_Email.getText().toString().trim();
        password = edtPassword.getText().toString().trim();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            mAuth.signOut();
                            user.sendEmailVerification()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(getActivity(), "Verification Email Has been Sent.", Toast.LENGTH_SHORT).show();
                                            sendData();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getActivity(), "Email not sent.", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "onFailure: Email not sent " + e.getMessage());
                                }
                            });

                            //insertDataToFirebaseRealTime();
                            Toast.makeText(getActivity(), "User Created.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getActivity(), LoginActivity.class));
                        } else {
                            hideProgress();
                            Log.e(TAG, "reload", task.getException());
                            Toast.makeText(getActivity(), "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void insertDataToFirebaseRealTime() {
        user = mAuth.getCurrentUser();
        if (user != null) {
            userID = user.getUid();

            HashMap<Object, String> hashMap = new HashMap<>();
            //put info in hasmap
            hashMap.put("email", email);
            hashMap.put("uid", userID);
            hashMap.put("name", fullName); //will add later (e.g. edit profile)
            hashMap.put("gender", gender); //will add later (e.g. edit profile)
            hashMap.put("birthday", birthday); //will add later (e.g. edit profile)
            hashMap.put("username", username); //will add later (e.g. edit profile)
            hashMap.put("onlineStatus", "online"); //will add later (e.g. edit profile)
            hashMap.put("typingTo", "noOne"); //will add later (e.g. edit profile)
            hashMap.put("phone", ""); //will add later (e.g. edit profile)
            hashMap.put("image", ""); //will add later (e.g. edit profile)
            hashMap.put("cover", ""); //will add later (e.g. edit profile)

            //firebase database isntance
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            //path to store user data named "Users"
            DatabaseReference reference = database.getReference("Users");
            //put data within hashmap in database
            reference.child(userID).setValue(hashMap);

        }
    }

    public void addDataFireStore(String fullName, String email, String phone, String username, String birthday, String gender, String password) {
        userID = mAuth.getCurrentUser().getUid();
        DocumentReference documentReference = fStore.collection("users").document(userID);
        Map<String, Object> user = new HashMap<>();
        user.put("fName", fullName);
        user.put("email", email);
        user.put("phone", phone);
        user.put("usn", username);
        user.put("birthday", birthday);
        user.put("gender", gender);
        user.put("password", password);
        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: user Profile is created for " + userID);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: " + e.toString());
            }
        });
    }


    private void sendData() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("Emails_fm5", email);
        bundle.putString("password_fm5", password);
        intent.putExtras(bundle);
        Log.i(TAG, "sendData: "
                + email + "\n"
                + password);
        getActivity().startActivity(intent);
        getActivity().finish();
    }

    private void showProgress() {
        progressBar_fm5.setVisibility(View.VISIBLE);
        btnFm5.setVisibility(View.INVISIBLE);
    }

    private void hideProgress() {
        progressBar_fm5.setVisibility(View.INVISIBLE);
        btnFm5.setVisibility(View.VISIBLE);
    }

    private void innitView(View view) {
        btnFm5 = view.findViewById(R.id.btnFm5);
        txt_Email = view.findViewById(R.id.txt_Email);
        edtPassword = view.findViewById(R.id.txtConfirmPass);
        progressBar_fm5 = view.findViewById(R.id.progressBar_fm5);
    }

}