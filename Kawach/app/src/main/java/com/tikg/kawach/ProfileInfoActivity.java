package com.tikg.kawach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ProfileInfoActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private EditText Name, State, Country, MAC; //Phone;
    private Button btnSubmit;
    private ProgressBar progressBar;
    String MACAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_info);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        Name = findViewById(R.id.editTextName);
        State = findViewById(R.id.editTextState);
        Country = findViewById(R.id.editTextCountry);
        MAC = findViewById(R.id.editTextMAC);
        progressBar = findViewById(R.id.progressBar4);
        progressBar.setVisibility(View.INVISIBLE);
        btnSubmit = findViewById(R.id.btnSubmit);

        MACAddress = MAC.getText().toString();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (Name.getText().toString().isEmpty()) {
                    Name.setError("It would be hard to call you with that name.");
                } else if (Country.getText().toString().isEmpty()) {
                    Country.setError("We never saw that place. Will you guide us someday ?");
                } else if (State.getText().toString().isEmpty()) {
                    State.setError("We don't know that State. Our team would love to visit that place.");
                } else {
                    Map<String, Object> profile = new HashMap<>();
                    profile.put("Infection Status", "Not Infected");
                    profile.put("Name", Name.getText().toString());
                    profile.put("Country", Country.getText().toString());
                    profile.put("State", State.getText().toString());
                    profile.put("Profile Filled", "FILLED");
                    profile.put("MAC",MAC.getText().toString());
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("Personal Profiles").document(mUser.getPhoneNumber()).set(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ProfileInfoActivity.this, "Update Successful", Toast.LENGTH_SHORT).show();

                                progressBar.setVisibility(View.GONE);
                                startActivity(new Intent(ProfileInfoActivity.this, UserDashboardActivity.class));
                                finish();
                            } else {
                                Toast.makeText(ProfileInfoActivity.this, "Update Failed ", Toast.LENGTH_SHORT).show();
                            }
                        }

                    });

                    Map <String,Object> BCData = new HashMap<>();
                    BCData.put("NumberOfEntries","0");
                    BCData.put("Phone Number",mUser.getPhoneNumber().toString);
                    db.collection(("Bluetooth Connections")).document(MAC.getText().toString()).set(BCData, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Log.d("ProfileInfoActivity","Bluetooth Profile created.");
                            }
                        }
                    });
                }
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mUser == null) {
            Intent loginIntent = new Intent(ProfileInfoActivity.this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
        }
    }
}