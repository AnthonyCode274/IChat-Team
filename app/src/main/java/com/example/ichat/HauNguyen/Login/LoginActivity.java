package com.example.ichat.HauNguyen.Login;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Fade;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;

import com.example.ichat.DashboardActivity;
import com.example.ichat.HauNguyen.DAO.UserDAO;
import com.example.ichat.HauNguyen.ForgotPassword.Content_ForgotPassWord_Activity;
import com.example.ichat.HauNguyen.Model.User_Profile;
import com.example.ichat.HauNguyen.SignUp.Content_FrameLayout_Register_Activity;
import com.example.ichat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    String email, gender, birthday, username, fullName, password, Uid;
    private ImageView logoView;
    private EditText edtEmail, edtPass;
    private final TextWatcher passwordWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            edtEmail.setError(null);
            edtPass.setError(null);
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String getText = edtEmail.getText().toString();

            if (Patterns.EMAIL_ADDRESS.matcher(getText).matches()) {
                edtEmail.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_mail_outline_24px, 0, R.drawable.ic_done_24px, 0);
            } else {
                edtEmail.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_mail_outline_24px, 0, 0, 0);
            }
        }
    };
    private Button btnLogins;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private UserDAO userDAO;
    private User_Profile user_profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        innitView();
        mAuth = FirebaseAuth.getInstance();

        animationLogo();
        hideProgressBar();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            email = bundle.getString("Emails_fm5");
            password = bundle.getString("password_fm5");
            username = bundle.getString("username_fm5");
            fullName = bundle.getString("fullname_fm5");
            birthday = bundle.getString("birthday_fm5");
            gender = bundle.getString("gender_fm5");
            Log.i(TAG, "LoginActivity get All data: "
                    + email + "\n"
                    + password + "\n"
                    + gender + "\n"
                    + birthday + "\n"
                    + username + "\n"
                    + fullName);
        }

        edtEmail.addTextChangedListener(passwordWatcher);

        btnLogins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressBar();
                if (!validateEmail() | !validatePassword()) {
                    hideProgressBar();
                    return;
                } else {
                    //check email user

                    mAuth.fetchSignInMethodsForEmail(edtEmail.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                    boolean isNewUser = task.getResult().getSignInMethods().isEmpty();
                                    if (isNewUser) {
                                        // Người dùng không tồn tại trong firebase
                                        Toast.makeText(LoginActivity.this, "User is not registered!", Toast.LENGTH_SHORT).show();
                                        hideProgressBar();
                                        return;
                                    } else {
                                        // người dùng đã tồn tại trong firebase
                                        Log.e("TAG", "Is Old User!");
                                        signInWithEmailAndPassword(edtEmail.getText().toString(), edtPass.getText().toString());
                                        hideProgressBar();
                                    }
                                }
                            });
                }
            }
        });

    }

    private void signInWithEmailAndPassword(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            //Log.w("TAG", "signInWithEmail:failed", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed or a problem occurred!", Toast.LENGTH_SHORT).show();
                            hideProgressBar();
                        } else {
                            checkIfEmailVerified();
                        }
                        // ...
                    }
                });
    }

    private void checkIfEmailVerified() {
        user = mAuth.getCurrentUser();
        if (user != null) {
            if (user.isEmailVerified()) {
                // user is verified, so you can finish this activity or send user to activity which you want.
                Toast.makeText(LoginActivity.this, "Email has been verified! :)", Toast.LENGTH_SHORT).show();
                insertDataToFirebaseRealTime();
                loginSuccessfully();
                hideProgressBar();
            } else {
                // email is not verified, so just prompt the message to the user and restart this activity.
                // NOTE: don't forget to log out the user.
                mAuth.signOut();
                Toast.makeText(LoginActivity.this, "Authentication failed :(", Toast.LENGTH_SHORT).show();
                hideProgressBar();
                //restart this activity
            }
        } else {
            Toast.makeText(this, "User is not registered!", Toast.LENGTH_SHORT).show();
        }
    }

    private void insertDataToFirebaseRealTime() {
        user = mAuth.getCurrentUser();
        if (user != null) {
            Uid = user.getUid();

            HashMap<Object, String> hashMap = new HashMap<>();
            //put info in hasmap
            hashMap.put("email", email);
            hashMap.put("uid", Uid);
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
            reference.child(Uid).setValue(hashMap);

        }
    }

    private void loginSuccessfully() {
        startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
        LoginActivity.this.finish();
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        btnLogins.setVisibility(View.GONE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        btnLogins.setVisibility(View.VISIBLE);
    }

    private boolean validatePassword() {
        String passwordInput = edtPass.getText().toString().trim();
        if (passwordInput.isEmpty()) {
            edtPass.setError("Field can't be empty");
            return false;
        } else {
            edtPass.setError(null);
            return true;
        }
    }

    private boolean validateEmail() {
        String emailInput = edtEmail.getText().toString().trim();
        if (emailInput.isEmpty()) {
            edtEmail.setError("Field can't be empty");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            edtEmail.setError("Please enter a valid email address");
            return false;
        } else {
            edtEmail.setError(null);

            return true;
        }
    }

    private void animationLogo() {
        Fade fade = new Fade();
        View decor = getWindow().getDecorView();
        fade.excludeTarget(decor.findViewById(R.id.action_bar_container), true);
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);
        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);
    }

    public void logo(View view) {
        // Create an alert builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(null);
        builder.setCancelable(false);
        // set the custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.custom_dialog_authentication, null);
        builder.setView(customLayout);
        // add a button
        final TextView tvTime = customLayout.findViewById(R.id.tvTime);
        final TextView tvTitle = customLayout.findViewById(R.id.cp_title);
        final ProgressBar progressBar = customLayout.findViewById(R.id.cp_pbar);
        new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                tvTitle.setText("Your information will be deleted after 60s," + "\n" + "please verify..");
                tvTime.setText(millisUntilFinished / 1000 + "");
            }

            public void onFinish() {
                progressBar.setVisibility(View.GONE);
                tvTitle.setVisibility(View.GONE);
                tvTime.setTextColor(getResources().getColor(R.color.colorRed));
                tvTime.setText("End time" + "\n" + "Your information has been deleted");

            }
        }.start();

        AlertDialog dialog;
        // create and show
        // the alert dialog
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // send data from the
                // AlertDialog to the Activity
                dialog.dismiss();
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    private void innitView() {
        //Hooks
        logoView = findViewById(R.id.logoView);
        edtEmail = findViewById(R.id.txtEmail_login);
        edtPass = findViewById(R.id.txtPassword_login);
        btnLogins = findViewById(R.id.btnLogins);
        progressBar = findViewById(R.id.progress_login);
    }

    public void register(View view) {
        Intent intent = new Intent(getApplicationContext(), Content_FrameLayout_Register_Activity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat optionsCompatLogo =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                            LoginActivity.this,
                            logoView, Objects.requireNonNull(ViewCompat.getTransitionName(logoView)));
            startActivity(intent, optionsCompatLogo.toBundle());
            overridePendingTransition(R.anim.anim_fadein, R.anim.anim_fadeout);
        } else {
            startActivity(intent);
        }
    }

    public void forgotpass(View view) {
        Intent intent = new Intent(getApplicationContext(), Content_ForgotPassWord_Activity.class);
        ActivityOptionsCompat optionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                        LoginActivity.this,
                        btnLogins, Objects.requireNonNull(ViewCompat.getTransitionName(btnLogins)));

        startActivity(intent, optionsCompat.toBundle());
        overridePendingTransition(R.anim.anim_fadein, R.anim.anim_fadeout);
    }
}