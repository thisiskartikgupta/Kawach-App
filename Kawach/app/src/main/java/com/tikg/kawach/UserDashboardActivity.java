package com.tikg.kawach;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserDashboardActivity extends AppCompatActivity {

    private Button btnSignOut, btnStats, btnAssessYourself, btnCheckYourData, btnHelpAndSuggestions;
    private TextView introText;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);



        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();


        btnSignOut = findViewById(R.id.btnSignOut);
        btnStats = findViewById(R.id.btnStats);
        btnAssessYourself = findViewById(R.id.btnAssessYourself);
        btnCheckYourData = findViewById(R.id.btnShops);
        btnHelpAndSuggestions = findViewById(R.id.btnHelpAndSuggestions);

        introText = findViewById(R.id.introText);

        // start BDS Service
       startService(new Intent(UserDashboardActivity.this, BluetoothDetectorService.class));

        // Get Name from Firestore
        FirebaseFirestore.getInstance().collection("Personal Profiles").document(mUser.getPhoneNumber()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if(documentSnapshot.exists() && documentSnapshot != null) {
                        introText.setText("Welcome " + documentSnapshot.getString("Name"));
                    }
                    else {
                        Toast.makeText(UserDashboardActivity.this, "Error in getting Snapshot  :",Toast.LENGTH_LONG).show();

                    }
                }
                else {
                    Toast.makeText(UserDashboardActivity.this, "Error in getting doc Ref :" + task.getException(),Toast.LENGTH_LONG).show();
                }
            }
        });

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(UserDashboardActivity.this, LoginActivity.class));
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mUser == null) {
            Intent loginIntent = new Intent(UserDashboardActivity.this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
        }
    }
}

