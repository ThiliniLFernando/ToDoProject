package com.smart.planner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private LinearLayout parentView;
    private Button resetBtn ;
    private EditText email;

    private FirebaseAuth auth ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        initComponent();
    }

    private void initComponent() {
        parentView = findViewById(R.id.parent_view);
        resetBtn = findViewById(R.id.reset_email_send);
        email = findViewById(R.id.email_id);

        auth = FirebaseAuth.getInstance();

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendResetEmail();
            }
        });
    }

    private void sendResetEmail() {
        String emailId = email.getText().toString();
        if(emailId != null && !emailId.trim().equals("")){
            auth.sendPasswordResetEmail(emailId).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(
                                getApplicationContext(),
                                "Success! Please check your emails.",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Failed! "+e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }).addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled() {
                    Toast.makeText(
                            getApplicationContext(),
                            "Canceled! ",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Toast.makeText(
                    getApplicationContext(),
                    "Please enter your registered email address to send reset email.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}