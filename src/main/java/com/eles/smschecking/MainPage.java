package com.eles.smschecking;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainPage extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Button driver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainpage_act);

        driver = findViewById(R.id.driver);
        driver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainPage.this, DisplayAcceptedRequestActivity.class);
                startActivity(intent);
            }
        });

        Button sosButton = findViewById(R.id.sos_button);
        sosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainPage.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainPage.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                } else {
                    triggerSOS();
                    startLocationUpdateService(); // Start the service.
                    startLocationReceiverActivity();
                }
            }
        });
    }

    private void startLocationUpdateService() {
        Intent serviceIntent = new Intent(this, LocationUpdateService.class);
        startService(serviceIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                triggerSOS();
                startLocationReceiverActivity();
            } else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void triggerSOS() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference sosRef = database.getReference("sos_signal");

        // This ensures we don't delete existing data!
        sosRef.child("triggered").setValue(true);
        sosRef.child("timestamp").setValue(System.currentTimeMillis());
    }

    private void startLocationReceiverActivity() {
        Intent mapIntent = new Intent(this, LocationReceiverActivity.class);
        startActivity(mapIntent);
    }
}
