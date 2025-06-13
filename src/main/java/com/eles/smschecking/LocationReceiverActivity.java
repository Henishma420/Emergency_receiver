
package com.eles.smschecking;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class LocationReceiverActivity extends AppCompatActivity {

    private MapView mapView;
    private boolean mapInitialized = false;
    private MediaPlayer alarmSound;
    private static final String TAG = "LocationReceiver";
    private Button nextButton;
    private DatabaseReference sosRef;
    private ValueEventListener sosListener;
    private GeoPoint lastReceivedSosLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_receiver);

        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        mapView = findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        nextButton = findViewById(R.id.nextButton);
        GeoPoint defaultPoint = new GeoPoint(0.0, 0.0);
        mapView.getController().setCenter(defaultPoint);
        mapView.getController().setZoom(5.0);

        mapInitialized = true;

        Log.d(TAG, "onCreate called");
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAlarmSound();
                if (lastReceivedSosLocation != null) {
                    Intent intent = new Intent(LocationReceiverActivity.this, RouteActivity.class);
                    intent.putExtra("sos_latitude", lastReceivedSosLocation.getLatitude());
                    intent.putExtra("sos_longitude", lastReceivedSosLocation.getLongitude());
                    startActivity(intent);
                } else {
                    Toast.makeText(LocationReceiverActivity.this, "SOS location not yet received.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        sosRef = database.getReference("sos_requests");

        sosListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "sosRef onDataChange triggered");
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Double latitude = snapshot.child("latitude").getValue(Double.class);
                        Double longitude = snapshot.child("longitude").getValue(Double.class);

                        if (latitude != null && longitude != null && mapInitialized) {
                            lastReceivedSosLocation = new GeoPoint(latitude, longitude); // Store the location
                            mapView.getController().animateTo(lastReceivedSosLocation);
                            mapView.getController().setZoom(15.0);

                            Marker marker = new Marker(mapView);
                            marker.setPosition(lastReceivedSosLocation);
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            mapView.getOverlays().clear();
                            mapView.getOverlays().add(marker);
                            mapView.invalidate();
                            Log.d(TAG, "SOS Location received from Firebase: lat=" + latitude + ", lon=" + longitude);
                            Toast.makeText(LocationReceiverActivity.this, "SOS Received!", Toast.LENGTH_LONG).show();
                            startAlarmSound();


                        } else {
                            Log.d(TAG, "SOS Location data is null or map is not initialized.");
                        }
                    }
                } else {
                    Log.d(TAG, "No SOS signals received.");
                    stopAlarmSound();
                    mapView.getOverlays().clear();
                    mapView.invalidate();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Firebase sos listener cancelled", databaseError.toException());
                Toast.makeText(LocationReceiverActivity.this, "Failed to receive SOS: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        sosRef.addValueEventListener(sosListener);
    }

    private void startAlarmSound() {
        Log.d(TAG, "startAlarmSound() called");
        if (alarmSound == null) {
            alarmSound = MediaPlayer.create(this, R.raw.sos_alarm);
            if (alarmSound != null) {
                alarmSound.setLooping(true);
                alarmSound.start();
                Log.d(TAG, "Alarm started");
            } else {
                Log.e(TAG, "Could not load SOS alarm sound.");
            }
        } else if (!alarmSound.isPlaying()) {
            alarmSound.start();
            Log.d(TAG, "Alarm started");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        // Ensure the SOS listener is active when the activity resumes
        if (sosRef != null && sosListener != null) {
            sosRef.addValueEventListener(sosListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        stopAlarmSound();
        // Remove the SOS listener to prevent unnecessary updates when the activity is in the background
        if (sosRef != null && sosListener != null) {
            sosRef.removeEventListener(sosListener);
        }
    }

    private void stopAlarmSound() {
        if (alarmSound != null && alarmSound.isPlaying()) {
            alarmSound.stop();
            alarmSound.release();
            alarmSound = null;
            Log.d(TAG, "Alarm stopped");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (sosRef != null && sosListener != null) {
            sosRef.removeEventListener(sosListener);
        }
        if (alarmSound != null) {
            alarmSound.release();
        }
    }
}