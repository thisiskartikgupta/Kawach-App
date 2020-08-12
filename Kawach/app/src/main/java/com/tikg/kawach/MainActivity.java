package com.tikg.kawach;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Timer;
import java.util.TimerTask;


/**
 * TODO : Please check :
 * TODO : 1. Time given in nextActivityLauncher.postdelayed() is 100 * per increment in loadAnimation(). Negligence of this may cause the Launching of Activity either before loading bar finishes or launching after a delay.
 */
public class MainActivity extends AppCompatActivity {

    private Handler nextActivityLauncher;

    private BluetoothAdapter bluetoothAdapter;

    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    final String PREFS_NAME = "MyPrefsFile";
    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingBarAnimation(); // call for animation to start

        // Checking if the device supports Bluetooth LE or not
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(MainActivity.this, "Bluetooth LE is not supported on the device. Tracking may not work properly.", Toast.LENGTH_SHORT).show();
            Log.i("MainActivity", "Bluetooth LE not supported");
        }

        nextActivityLauncher = new Handler();
        nextActivityLauncher.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(MainActivity.this, PermissionAndInfoActivity.class));
                finish();
            }
        }, 2500);
        // Checking access for location
        int permissionCheck = MainActivity.this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
        permissionCheck += MainActivity.this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");

        // Using SharedPrefs to know whether user has started app for the first time
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        // Bluetooth Adapter initialization
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Initializing FirebaseAuth and FirebaseUser
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        startActivity(new Intent(MainActivity.this, KawachIntroscreenActivity.class));

        if (settings.getBoolean("my_first_time", true)) {
            // First launch case
            Log.i("MainActivity", "First Launch Of App");

            nextActivityLauncher = new Handler();
            nextActivityLauncher.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(MainActivity.this, KawachIntroscreenActivity.class));
                    finish();
                }
            }, 2500);

            // setting first launch to false
            settings.edit().putBoolean("my_first_time", false).apply();

        } else if (!bluetoothAdapter.isEnabled() || permissionCheck == 0) {
            // if either bluetooth or location is not active launch the permission and info Activity
            //launch permission activity after a delay
            Log.i("MainActivity", "PermissionAndInfoActivity Launched");

            nextActivityLauncher = new Handler();
            nextActivityLauncher.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(MainActivity.this, PermissionAndInfoActivity.class));
                    finish();
                }
            }, 2500);

        } else if (mUser != null) {
            // launch Dashboard of the user

            Log.i("MainActivity", "UserDashboardActivity Launched");

            nextActivityLauncher = new Handler();
            nextActivityLauncher.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(MainActivity.this, UserDashboardActivity.class));
                    finish();
                }
            }, 2500);


        } else {
            //if there are no previous logins then head to loginActivity
            // launch Login Activity after a delay

            Log.i("MainActivity", "LoginActivity Launched");

            nextActivityLauncher = new Handler();
            nextActivityLauncher.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(loginIntent);
                    finish();
                }
            }, 2500);
        }

    }

    /**
     * <p> This function should be called as and when the loading bar Animation has to be started. </p>
     */
    private void loadingBarAnimation() {

        progressBar = (ProgressBar) findViewById(R.id.progressBarloading);

        final Timer loadingTimer = new Timer();
        TimerTask loadingTask = new TimerTask() {
            @Override
            public void run() {
                counter++;
                progressBar.setProgress(counter);
                if (counter == 100) loadingTimer.cancel();
            }
        };

        //scheduling the task when to start upon call
        loadingTimer.schedule(loadingTask, 0, 25);
    }

}
