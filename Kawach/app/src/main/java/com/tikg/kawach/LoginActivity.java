package com.tikg.kawach;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private EditText countryCode, phoneNumber, OTP;
    private TextView ErrorText1, ErrorText2;
    private Button btnRequestCode, btnVerify;
    private ProgressBar progressBar1, progressBar2;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private String sAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        countryCode = findViewById(R.id.editTextcountrycode);
        phoneNumber = findViewById(R.id.editTextphone);
        OTP = findViewById(R.id.editTextOTP);

        ErrorText1 = findViewById(R.id.errorText1);
        ErrorText2 = findViewById(R.id.errorText2);

        btnRequestCode = findViewById(R.id.btnSendVerificationCode);
        btnVerify = findViewById(R.id.btnVerify);

        progressBar1 = findViewById(R.id.progressBar);
        progressBar2 = findViewById(R.id.progressBar2);

        progressBar1.setVisibility(View.INVISIBLE);
        progressBar2.setVisibility(View.INVISIBLE);

        OTP.setEnabled(false);
        OTP.setVisibility(View.INVISIBLE);
        btnVerify.setEnabled(false);
        btnVerify.setVisibility(View.INVISIBLE);

        ErrorText1.setText("");
        ErrorText2.setText("");

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        btnRequestCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String countrycode = countryCode.getText().toString();
                String phonenumber = phoneNumber.getText().toString();

                if (phonenumber.isEmpty()) {
                    countryCode.setError("Never saw an empty phone number.");
                }
                if (countrycode.isEmpty()) {
                    countryCode.setError("Never visited a country having no country code.");
                } else if (phonenumber.length() != 10) {
                    countryCode.setError("Do you really use that number ?");
                }
                String PHONENUMBER = "+" + countrycode + phonenumber;
                if (phonenumber.length() == 10 && countrycode.length() == 2) {
                    // sending verification code
                    progressBar1.setVisibility(View.VISIBLE);
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            PHONENUMBER,
                            60,
                            TimeUnit.SECONDS,
                            LoginActivity.this,
                            mCallbacks
                    );

                } else {
                    ErrorText1.setText("Please fill form correctly");

                }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                Toast.makeText(LoginActivity.this, " Code Sent", Toast.LENGTH_SHORT).show();
                signInwithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(LoginActivity.this, " Code Sending failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                    }
                }, 10000);
                sAuth = s;
                OTP.setVisibility(View.VISIBLE);
                OTP.setEnabled(true);
                btnVerify.setVisibility(View.VISIBLE);
                btnVerify.setEnabled(true);
                Toast.makeText(LoginActivity.this, " Code Sent", Toast.LENGTH_SHORT).show();
                progressBar1.setVisibility(View.GONE);
                //phoneNumber.setEnabled(false);
                //countryCode.setEnabled(false);
                btnRequestCode.setText("Resend Verification Code");
            }
        };

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = OTP.getText().toString();
                if (otp.isEmpty()) {
                    OTP.setError("We will never send you an empty OTP");
                } else {
                    progressBar2.setVisibility(View.VISIBLE);
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(sAuth, otp);
                    signInwithPhoneAuthCredential(credential);
                }
            }
        });


    }

    private void signInwithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();
                            Toast.makeText(LoginActivity.this, "Verification Successful ", Toast.LENGTH_SHORT).show();
                            //Ensuring that the user does not have to fill profile details the next time he logs in
                            startActivity(new Intent(LoginActivity.this, UserDashboardActivity.class));
                            FirebaseFirestore.getInstance().collection("Personal Profiles").document(mUser.getPhoneNumber()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot documentSnapshot = task.getResult();
                                        if (documentSnapshot.exists() && documentSnapshot != null) {
                                            if (documentSnapshot.getString("Profile").compareTo("FILLED") == 0) {
                                                startActivity(new Intent(LoginActivity.this, UserDashboardActivity.class));
                                                finish();
                                            } else {
                                                Intent DashboardIntent = new Intent(LoginActivity.this, ProfileInfoActivity.class);
                                                startActivity(DashboardIntent);
                                            }
                                        }

                                    } else {
                                        Log.d("LoginActivity", "Failed to fetch Document Snapshot..");
                                    }
                                }
                            });

                        } else {
                            ErrorText2.setText("Verifcation Failed" + task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {

                                Toast.makeText(LoginActivity.this, "We got an error ", Toast.LENGTH_SHORT).show();
                                btnVerify.setText("Try Again");
                            }
                        }
                    }
                });
    }
}

