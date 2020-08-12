package com.tikg.kawach;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class BluetoothDetectorService extends Service {

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private BluetoothAdapter bluetoothAdapter;
    String selfMacAddress;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        FirebaseFirestore.getInstance().collection("Personal Profiles").document(mUser.getPhoneNumber()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists() && documentSnapshot != null) {
                        Log.d("BDS", "Got MAC address");
                        selfMacAddress = documentSnapshot.getString("MAC");
                    }
                }
            }
        });


        if (bluetoothAdapter.isEnabled()) {
            bluetoothLeScanner.startScan(scanCallback);
        }
        return START_STICKY;
    }

    ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (MACExistsInDatabase(result.getDevice().getAddress().toString())) {
                AddMACtoDB(selfMacAddress, result.getDevice().getAddress().toString());
                UpdateInfectionStatus(selfMacAddress, result.getDevice().getAddress().toString());
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    boolean MACExistsInDatabase(final String checkMAC) {

        //ExistsFlag bool for existence of a MAC in database
        final boolean[] ExistsFlag = {false};

        //checking MAC existence by getting DocumentSnapshot . If snapshot is not available, document does not exists.
        FirebaseFirestore.getInstance().collection("Bluetooth Connections").document(checkMAC).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    ExistsFlag[0] = true;
                    Log.d("BDS", checkMAC + " address exists in database");
                } else Log.d("BDS", checkMAC + " address not found in database");
            }
        });

        return ExistsFlag[0];
    }

    void AddMACtoDB(final String sourceMAC, final String foundMAC) {

        final int[] NumberOfEntries = {0};

        // get number of entries
        FirebaseFirestore.getInstance().collection("Bluetooth Connections").document(sourceMAC).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists() && documentSnapshot != null) {
                        NumberOfEntries[0] = Integer.parseInt(documentSnapshot.getString("NumberOfEntries"));
                    }
                } else {
                    //Toast.makeText(UserDashboardActivity.this, "Error in getting doc Ref :" + task.getException(),Toast.LENGTH_LONG).show();
                }
            }
        });

        // add new address as NumberOfEntries +1
        Map<String, Object> entry = new HashMap<>();
        entry.put(foundMAC, "Entry " + Integer.toString(NumberOfEntries[0] + 1));
        entry.put("NumberOfEntries", NumberOfEntries[0] + 1);
        FirebaseFirestore.getInstance().collection("Bluetooth Connections").document(sourceMAC).set(entry, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d("BDS", foundMAC + " added to document " + sourceMAC);
                } else {
                    Log.d("BDS", "Adding to document " + sourceMAC + "failed");
                }
            }
        });
    }

    public void UpdateInfectionStatus(String selfMacAddress, String foundMacAddress) {

        final String selfStatus, foundStatus, selfPhoneNumber, foundPhoneNumber;

        selfPhoneNumber = getPhoneNumberFromMAC(selfMacAddress);
        selfStatus = getInfectionStatusFromPhoneNumber(selfPhoneNumber);

        foundPhoneNumber = getPhoneNumberFromMAC(foundMacAddress);
        foundStatus = getInfectionStatusFromPhoneNumber(foundPhoneNumber);

        /* I I = I I
         * I S = I S
         * I N = I S / CaseI
         * S I = S I
         * S S = S S
         * S N = S S / II
         * N I = S I / III
         * N S = S S / IV
         * N N = N N
         * N = Not Infected , S = Suspected, I =Infected
         */

        if (selfStatus.equals("INFECTED") && foundStatus.equals("NOT INFECTED")) {
            // I
            setInfectionStatusByPhoneNumber(foundPhoneNumber, "SUSPECTED");
        } else if (selfStatus.equals("SUSPECTED") && foundStatus.equals("NOT INFECTED")) {
            //II
            setInfectionStatusByPhoneNumber(foundPhoneNumber, "SUSPECTED");
        } else if (selfStatus.equals("NOT INFECTED") && foundStatus.equals("INFECTED")) {
            ///III
            setInfectionStatusByPhoneNumber(selfPhoneNumber, "SUSPECTED");
        } else if (selfStatus.equals("NOT INFECTED") && foundStatus.equals("SUSPECTED")) {
            //IV
            setInfectionStatusByPhoneNumber(selfPhoneNumber, "SUSPECTED");
        }

    }

    private String getPhoneNumberFromMAC(String MAC) {
        final String[] PhoneNumber = new String[1];
        FirebaseFirestore.getInstance().collection("Bluetooth Connection").document(selfMacAddress).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    PhoneNumber[0] = task.getResult().getString("Phone Number").toString();
                }
            }
        });
        return PhoneNumber[0];
    }

    private String getInfectionStatusFromPhoneNumber(String PhoneNumber) {

        final String[] InfectionStatus = new String[1];
        FirebaseFirestore.getInstance().collection("Personal Profile").document(PhoneNumber).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    InfectionStatus[0] = task.getResult().getString("Phone Number").toString();
                }
            }
        });
        return InfectionStatus[0];
    }

    private void setInfectionStatusByPhoneNumber(String PhoneNumber, String NewStatus) {
        Map<String, Object> updateOfStatus = new HashMap<>();
        updateOfStatus.put("Infection Status :", NewStatus);
        FirebaseFirestore.getInstance().collection("Personal Profiles").document(PhoneNumber).set(updateOfStatus, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d("BDS", "Update of status successful");
                }
            }
        });
    }
}
