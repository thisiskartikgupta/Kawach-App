package com.tikg.kawach;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class PermissionAndInfoActivity extends AppCompatActivity {

    private TextView ErrorText;
    private ProgressBar progress;
    private Switch bluetoothSwitch, locationSwitch;
    private CheckBox checkboxTC;
    private Button btnContinue;

    public BluetoothAdapter bluetoothAdapter;

    private static final String TAG = "PermissionAndInfo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_and_info);

        progress = findViewById(R.id.progress);
        ErrorText = findViewById(R.id.errorText);
        bluetoothSwitch = findViewById(R.id.BluetoothSwitch);
        locationSwitch = findViewById(R.id.LocationSwitch);
        checkboxTC = findViewById(R.id.checkboxAgree);
        btnContinue = findViewById(R.id.btnContinue);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Setting defaults states for the switches, checkboxes and buttons
        bluetoothSwitch.setChecked(false);
        locationSwitch.setChecked(false);
        btnContinue.setEnabled(false);
        checkboxTC.setChecked(false);
        checkboxTC.setEnabled(false);
        ErrorText.setVisibility(View.INVISIBLE);

        //checking if bluetooth or location already enabled
        if (bluetoothAdapter.isEnabled() && bluetoothAdapter.isDiscovering())
            bluetoothSwitch.setChecked(true);

        int permissionCheck = PermissionAndInfoActivity.this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
        permissionCheck += PermissionAndInfoActivity.this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
        if (permissionCheck != 0) locationSwitch.setChecked(true);

        bluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked && !locationSwitch.isChecked()) {
                    progress.setProgress(50);
                    ErrorText.setVisibility(View.VISIBLE);
                    ErrorText.setText("Please check Location Permission ");
                    checkboxTC.setEnabled(false);

                    Log.i(TAG,"Bluetooth Enabled.");

                    Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(enableBTIntent);
                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                    startActivity(discoverableIntent);

                }

                if (isChecked && locationSwitch.isChecked()) {
                    progress.setProgress(100);
                    ErrorText.setText("");
                    checkboxTC.setEnabled(true);

                    Log.i(TAG,"Bluetooth Enabled.");

                    Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(enableBTIntent);
                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                    startActivity(discoverableIntent);
                }

                if (!isChecked && !locationSwitch.isChecked()) {
                    progress.setProgress(0);
                    ErrorText.setText("Please check all permissions");
                    checkboxTC.setEnabled(false);

                    Log.i(TAG,"Bluetooth Disabled.");
                }

                if (!isChecked && locationSwitch.isChecked()) {
                    progress.setProgress(50);
                    ErrorText.setText("Please check Bluetooth Permission");
                    checkboxTC.setEnabled(false);
                    Log.i(TAG,"Bluetooth Disabled.");
                }

            }

        });

        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked && !bluetoothSwitch.isChecked()) {
                    progress.setProgress(50);
                    ErrorText.setVisibility(View.VISIBLE);
                    ErrorText.setText("Please check Bluetooth Permission ");
                    checkboxTC.setEnabled(false);

                    Log.i(TAG,"Location Enabled.");

                }

                if (isChecked && bluetoothSwitch.isChecked()) {
                    progress.setProgress(100);
                    ErrorText.setText("");
                    checkboxTC.setEnabled(true);

                    int permissionCheck = PermissionAndInfoActivity.this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
                    permissionCheck += PermissionAndInfoActivity.this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
                    if (permissionCheck != 0) {

                        PermissionAndInfoActivity.this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
                    }
                    Log.i(TAG,"Location Enabled.");
                }

                if (!isChecked && !bluetoothSwitch.isChecked()) {
                    progress.setProgress(0);
                    ErrorText.setText("Please check all permissions");
                    checkboxTC.setEnabled(false);
                    Log.i(TAG,"Location Disabled.");
                }

                if (!isChecked && bluetoothSwitch.isChecked()) {
                    progress.setProgress(50);
                    ErrorText.setText("Please check Location Permission");
                    checkboxTC.setEnabled(false);
                    Log.i(TAG,"Location Disabled.");
                }

            }

        });

        checkboxTC.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked && bluetoothSwitch.isChecked() && locationSwitch.isChecked()) {
                    btnContinue.setEnabled(true);
                    checkboxTC.setEnabled(true);
                }

                if (!isChecked && bluetoothSwitch.isChecked() && locationSwitch.isChecked()) {
                    btnContinue.setEnabled(false);
                    checkboxTC.setEnabled(true);
                }

                if (isChecked && (!bluetoothSwitch.isChecked() || !locationSwitch.isChecked())) {
                    btnContinue.setEnabled(false);
                    checkboxTC.setChecked(false);
                    checkboxTC.setEnabled(false);
                }

            }

        });

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkboxTC.isChecked() && bluetoothSwitch.isChecked() && locationSwitch.isChecked() && checkboxTC.isEnabled()) {
                    Toast.makeText(PermissionAndInfoActivity.this, "Success !!", Toast.LENGTH_SHORT).show();
                    btnContinue.setEnabled(true);

                    Intent startLoginActivity = new Intent(getApplicationContext(), LoginActivity.class);
                    //   startLoginActivity.putExtra("DeviceList", DeviceList);
                    startActivity(startLoginActivity);
                    finish();
                } else {
                    btnContinue.setEnabled(false);
                }

            }

        });

    }

}
