package com.smart.planner;

import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.smart.planner.Classes.NetworkUtils;
import com.smart.planner.Classes.PatternUtils;
import com.smart.planner.POJOs.List;
import com.smart.planner.POJOs.User;

public class SignUp extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private FirebaseFirestoreSettings firestoreSettings ;
    private FirebaseAuth auth;

    private LinearLayout rootLayout;
    private LinearLayout progressLayout;
    private Button signUpButton;
    private TextView haveAccountText;
    private EditText userName, userEmail, userPassword, userRetypePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Main.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initComponents();
    }

    private void initComponents() {
        signUpButton = findViewById(R.id.sign_up);
        haveAccountText = findViewById(R.id.IHaveAnAccountBtn);
        userName = findViewById(R.id.et_name);
        userEmail = findViewById(R.id.et_email);
        userPassword = findViewById(R.id.et_pw);
        userRetypePassword = findViewById(R.id.et_retype_pw);
        rootLayout = findViewById(R.id.consLayout);
//        progressLayout = findViewById(R.id.progress_layout);

        firestore = FirebaseFirestore.getInstance();
        firestoreSettings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(firestoreSettings);

        auth = FirebaseAuth.getInstance();

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });

        haveAccountText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), SignIn.class));
                finish();
            }
        });
    }

    private void signUp() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        // sign up process
        final String name = userName.getText().toString().trim();
        final String email = userEmail.getText().toString().trim();
        final String password = userPassword.getText().toString().trim();
        final String retype_password = userRetypePassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Snackbar.make(rootLayout, "Please enter your name", Snackbar.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(email)) {
            Snackbar.make(rootLayout, "Please enter email", Snackbar.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            Snackbar.make(rootLayout, "Please enter password", Snackbar.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(retype_password) || !password.equals(retype_password)) {
            Snackbar.make(rootLayout, "Please confirm your password", Snackbar.LENGTH_SHORT).show();
        } else if (password.length() < 6) {
            Snackbar.make(rootLayout, "Password too short", Snackbar.LENGTH_SHORT).show();
        } else if (!PatternUtils.isValidEmailAddress(email)) {
            Snackbar.make(rootLayout, "Invalid email address", Snackbar.LENGTH_SHORT).show();
        } else if (!NetworkUtils.isNetworkAvailable(getApplicationContext())) {
            Snackbar.make(rootLayout, "Connect to the internet", Snackbar.LENGTH_SHORT).show();
        } else {
//            progressLayout.setVisibility(View.VISIBLE);
            // sign up firebase autharation
            auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(final AuthResult authResult) {
                    // firebase firestore database for current user
                    firestore.collection("Users").document(auth.getCurrentUser().getUid())
                            .collection("Profile").document("LoginData")
                            .set(new User(name, email, password)).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            firestore.collection("Users").document(auth.getCurrentUser().getUid())
                                    .collection("Lists").add(new List("Inbox", "#0199ff"))
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Snackbar.make(rootLayout, "Inbox created", Snackbar.LENGTH_SHORT).show();
                                        }
                                    });
                            // add Inbox List
                            ((EditText) findViewById(R.id.et_name)).setText(null);
                            ((EditText) findViewById(R.id.et_email)).setText(null);
                            ((EditText) findViewById(R.id.et_pw)).setText(null);
                            ((EditText) findViewById(R.id.et_retype_pw)).setText(null);
                            Snackbar.make(rootLayout, "Register Successfully", Snackbar.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Snackbar.make(rootLayout, "Failed " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                }
            });
//            progressLayout.setVisibility(View.GONE);
        }
    }

    private boolean verifyEmailAddress(String email) {

        return true;
    }

}
