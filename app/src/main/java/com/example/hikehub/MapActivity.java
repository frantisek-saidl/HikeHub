package com.example.hikehub;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;

public class MapActivity extends AppCompatActivity {
    private MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Marker userMarker;
    private Location lastKnownLocation;

    private final LocationRequest locationRequest =
            new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
                    .setMinUpdateIntervalMillis(2000)
                    .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.getController().setZoom(15.0);
        mapView.setMultiTouchControls(true);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            initializeMap();
            startLocationUpdates();
        }
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void initializeMap() {
        userMarker = new Marker(mapView);
        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        userMarker.setIcon(getResources().getDrawable(R.drawable.user_marker));
        mapView.getOverlays().add(userMarker);

        CompassOverlay compassOverlay = new CompassOverlay(this, mapView);
        compassOverlay.enableCompass();
        mapView.getOverlays().add(compassOverlay);

        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(mapView);
        scaleBarOverlay.setAlignRight(true);
        scaleBarOverlay.getBarPaint().setColor(Color.BLACK);
        mapView.getOverlays().add(scaleBarOverlay);
    }

    private void startLocationUpdates() {
        if (!hasLocationPermission()) return;

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                Location newLocation = locationResult.getLastLocation();
                if (newLocation != null && (lastKnownLocation == null ||
                        newLocation.getLatitude() != lastKnownLocation.getLatitude() ||
                        newLocation.getLongitude() != lastKnownLocation.getLongitude())) {
                    lastKnownLocation = newLocation;
                    updateMarkerLocation(newLocation);
                    updateMarkerRotation(newLocation.getBearing());
                }
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } catch (SecurityException e) {
            Log.e("SecurityException: Location permission denied", e.getMessage());
        }
    }

    private void animateMarkerTo(Marker marker, GeoPoint finalPosition) {
        GeoPoint startPosition = marker.getPosition();
        ValueAnimator animator = ObjectAnimator.ofObject(marker, "position", (TypeEvaluator<GeoPoint>) (fraction, startValue, endValue) -> {
            double lat = startValue.getLatitude() + (endValue.getLatitude() - startValue.getLatitude()) * fraction;
            double lon = startValue.getLongitude() + (endValue.getLongitude() - startValue.getLongitude()) * fraction;
            return new GeoPoint(lat, lon);
        }, startPosition, finalPosition);
        animator.setDuration(1000);
        animator.start();
    }

    private void updateMarkerLocation(Location location) {
        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        animateMarkerTo(userMarker, geoPoint);
        mapView.getController().setCenter(geoPoint);
    }

    private void updateMarkerRotation(float bearing) {
        userMarker.setRotation(bearing);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}