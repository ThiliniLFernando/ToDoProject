package com.smart.planner;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.gson.Gson;
import com.smart.planner.Classes.NetworkUtils;
import com.smart.planner.POJOs.User;

public class SignIn extends AppCompatActivity {

    private static String CURRENT_USER_KEY ;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore ;
    private FirebaseFirestoreSettings firestoreSettings;

    private ConstraintLayout rootLayout, progressLayout;
    private Button signInBtn;
    private TextView progressMessage,createNewAccount,frgtPw;

    Activity thisActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Main.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        initComponents();
    }

    private void initComponents() {
        rootLayout = findViewById(R.id.consLayoutSignIn);
        //progressLayout = findViewById(R.id.signInWaitingScreen);
        signInBtn = findViewById(R.id.sign_inBtn);
        createNewAccount = findViewById(R.id.createNewAccount);
        progressMessage = findViewById(R.id.progress_message);
        frgtPw = findViewById(R.id.forgotPw);
        thisActivity = this;

        // fire base
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firestoreSettings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(firestoreSettings);

        // Sign In On Click
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        // create new account Button on click
        createNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // sign up page
                startActivity(new Intent(getApplicationContext(), SignUp.class));
                finish();
            }
        });

        frgtPw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),ForgotPassword.class));

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void signIn() {
        // sign in process
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        final String email = ((EditText) findViewById(R.id.signin_email)).getText().toString().trim();
        final String password = ((EditText) findViewById(R.id.signin_password)).getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            Snackbar.make(rootLayout, "Please enter your name", Snackbar.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            Snackbar.make(rootLayout, "Please enter password", Snackbar.LENGTH_SHORT).show();
        } else if (!NetworkUtils.isNetworkAvailable(getApplicationContext())) {
            Snackbar.make(rootLayout, "Connect to the internet", Snackbar.LENGTH_SHORT).show();
        } else {
            // sign in
            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            // set layout visibility
//                            progressLayout.setVisibility(View.VISIBLE);
//                            rootLayout.setVisibility(View.INVISIBLE);
//                            progressMessage.setText("Sign In...");

                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            final SharedPreferences.Editor editor = preferences.edit();

                            SignIn.CURRENT_USER_KEY = authResult.getUser().getUid();

                            firestore.collection("Users").document(SignIn.CURRENT_USER_KEY)
                                    .collection("Profile").document("LoginData").get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot snapshot) {
                                            User user = snapshot.toObject(User.class);
                                            Gson gson = new Gson();
                                            //progressMessage.setText("Fetching User Data...");

                                            SharedPreferences thisSharedPreference = getSharedPreferences("com.planer.todoproject.MainActivityPref", MODE_PRIVATE);
                                            thisSharedPreference.edit().putInt("MainOpeningCount", 0).apply();

                                            editor.putString("current_user_object", gson.toJson(user));
                                            editor.putString("current_user_KEY", SignIn.CURRENT_USER_KEY);
                                            editor.putBoolean("first_time_app_open", true);
                                            editor.putInt("theme", Main.DEFAULT);
                                            editor.putBoolean("isDarkTheme",false);
                                            editor.apply();


                                            //scheduleQuarterAlarm();
                                            //progressLayout.setVisibility(View.GONE);
                                            startActivity(new Intent(SignIn.this, SplashScreen.class));
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Snackbar.make(rootLayout, "Sign In Failed with " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Snackbar.make(rootLayout, "Sign In Failed with " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}



