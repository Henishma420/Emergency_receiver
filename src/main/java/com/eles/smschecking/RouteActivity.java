package com.eles.smschecking;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.ArrayList;
import java.util.List;

public class RouteActivity extends AppCompatActivity {

    private MapView mapView;
    private static final String TAG = "RouteActivity";
    private GeoPoint sosLocation;

    private double driverLat = 8.3022;
    private double driverLon = 77.2231;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_route);

        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        mapView = findViewById(R.id.routeMap);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        Log.d(TAG, "onCreate called");

        double sosLatitude = getIntent().getDoubleExtra("sos_latitude", Double.NaN);
        double sosLongitude = getIntent().getDoubleExtra("sos_longitude", Double.NaN);

        if (!Double.isNaN(sosLatitude) && !Double.isNaN(sosLongitude)) {
            sosLocation = new GeoPoint(sosLatitude, sosLongitude);
            displayRouteWithRoute();
        } else {
            Log.e(TAG, "SOS latitude or longitude not received from Intent.");
        }
    }

    private void displayRouteWithRoute() {
        Log.d(TAG, "displayRouteWithRoute() called");
        if (sosLocation != null) {
            Log.d(TAG, "SOS Location is: lat=" + sosLocation.getLatitude() + ", lon=" + sosLocation.getLongitude());
            Log.d(TAG, "Driver Location is: lat=" + driverLat + ", lon=" + driverLon);
            GeoPoint driverPoint = new GeoPoint(driverLat, driverLon);

            Marker sosMarker = new Marker(mapView);
            sosMarker.setPosition(sosLocation);
            sosMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            sosMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.marker_default));
            mapView.getOverlays().add(sosMarker);
            Log.d(TAG, "SOS marker added.");

            Marker driverMarker = new Marker(mapView);
            driverMarker.setPosition(driverPoint);
            driverMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(driverMarker);
            Log.d(TAG, "Driver marker added.");

            getRoute(driverPoint, sosLocation);

            IMapController mapController = mapView.getController();
            mapController.setZoom(12.0);
            mapController.animateTo(new GeoPoint(
                    (sosLocation.getLatitude() + driverLat) / 2,
                    (sosLocation.getLongitude() + driverLon) / 2
            ));
            Log.d(TAG, "Map centered.");

        } else {
            Log.d(TAG, "SOS location not received via Intent.");
        }
    }

    private void getRoute(GeoPoint startPoint, GeoPoint endPoint) {
        String coordinates = startPoint.getLongitude() + "," + startPoint.getLatitude() + ";" +
                endPoint.getLongitude() + "," + endPoint.getLatitude();
        String overview = "full";
        String geometries = "geojson";
        String baseUrl = "http://router.project-osrm.org/";

        Gson gson = new GsonBuilder().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        OSRMService service = retrofit.create(OSRMService.class);
        Call<RouteResponse> call = service.getRoute(coordinates, overview, geometries);

        call.enqueue(new Callback<RouteResponse>() {
            @Override
            public void onResponse(Call<RouteResponse> call, Response<RouteResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().getRoutes().isEmpty()) {
                    RouteResponse routeResponse = response.body();
                    List<List<Double>> coordinates = routeResponse.getRoutes().get(0).getGeometry().getCoordinates();

                    List<GeoPoint> routePoints = new ArrayList<>();
                    for (List<Double> coord : coordinates) {
                        double lon = coord.get(0);
                        double lat = coord.get(1);
                        routePoints.add(new GeoPoint(lat, lon));
                    }

                    drawRoute(routePoints);
                } else {
                    Toast.makeText(RouteActivity.this, "Failed to get route", Toast.LENGTH_SHORT).show();
                    Log.e("OSRM Error", "Response was not successful or body was null: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<RouteResponse> call, Throwable t) {
                Toast.makeText(RouteActivity.this, "Error connecting to routing service", Toast.LENGTH_SHORT).show();
                Log.e("OSRM Error", "Network error: " + t.getMessage());
            }
        });
    }

    private void drawRoute(List<GeoPoint> routePoints) {
        if (routePoints.isEmpty()) {
            Log.e("Draw Route", "No points to draw!");
            return;
        }

        Polyline roadOverlay = new Polyline();
        roadOverlay.setPoints(routePoints);
        roadOverlay.setColor(Color.RED);
        roadOverlay.setWidth(7f);
        mapView.getOverlays().add(roadOverlay);

        Log.d("Draw Route", "Route drawn with " + routePoints.size() + " points.");

        mapView.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
}