package com.eles.smschecking;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DisplayAcceptedRequestActivity extends AppCompatActivity {

    private TextView txtDriverInfo;
    private Button btnCallDriver;
    private String TAG = "DisplayAcceptedRequest";

    private String phoneNumber = null;
    private static final int CALL_PERMISSION_REQUEST_CODE = 1;

    private String acceptedRequestId;
    private String driverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request);

        txtDriverInfo = findViewById(R.id.txtDriverInfo);
        Button btnShowDriver = findViewById(R.id.btnShowDriver);
        btnCallDriver = findViewById(R.id.btnCallDriver);

        btnShowDriver.setOnClickListener(v -> searchForAcceptedRequest());

        btnCallDriver.setOnClickListener(v -> {
            if (phoneNumber != null) {
                makePhoneCall(phoneNumber);
            } else {
                Toast.makeText(this, "Phone number not loaded yet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchForAcceptedRequest() {
        DatabaseReference sosRef = FirebaseDatabase.getInstance().getReference("sos_requests");

        sosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean found = false;

                for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                    String status = requestSnapshot.child("status").getValue(String.class);
                    if ("accepted".equalsIgnoreCase(status)) {
                        acceptedRequestId = requestSnapshot.getKey();
                        driverId = requestSnapshot.child("driverId").getValue(String.class);
                        if (driverId == null) driverId = "4537D";
                        found = true;
                        break;
                    }
                }

                if (found && acceptedRequestId != null) {
                    listenForDriverDetails(driverId);
                } else {
                    Toast.makeText(DisplayAcceptedRequestActivity.this, "No accepted requests yet.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading requests: " + error.getMessage());
            }
        });
    }

    private void listenForDriverDetails(String driverId) {
        DatabaseReference driverRef = FirebaseDatabase.getInstance()
                .getReference("drivers")
                .child(driverId);

        driverRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot driverSnapshot) {
                String name = driverSnapshot.child("name").getValue(String.class);
                phoneNumber = driverSnapshot.child("phoneNumber").getValue(String.class);
                String vehicle = driverSnapshot.child("vehicle_number").getValue(String.class);
                Integer ageVal = driverSnapshot.child("age").getValue(Integer.class);

                name = (name != null) ? name : "N/A";
                phoneNumber = (phoneNumber != null) ? phoneNumber : "N/A";
                vehicle = (vehicle != null) ? vehicle : "N/A";
                String age = (ageVal != null) ? String.valueOf(ageVal) : "N/A";

                String info = "🚗 Driver Name: " + name + "\n"
                        + "📞 Phone: " + phoneNumber + "\n"
                        + "🔢 Vehicle No: " + vehicle + "\n"
                        + "🎂 Age: " + age;

                txtDriverInfo.setText(info);
                txtDriverInfo.setVisibility(View.VISIBLE);
                btnCallDriver.setVisibility(View.VISIBLE);

                Log.d(TAG, "Driver info loaded:\n" + info);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Driver load failed: " + error.getMessage());
            }
        });
    }

    private void makePhoneCall(String number) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE}, CALL_PERMISSION_REQUEST_CODE);
        } else {
            startActivity(callIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CALL_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (phoneNumber != null) {
                    makePhoneCall(phoneNumber);
                }
            } else {
                Toast.makeText(this, "Call permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
