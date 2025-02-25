package com.example.hikehub;

import android.os.Bundle;
import android.location.Location;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;

public class MapActivity extends AppCompatActivity {
    private MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;

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

        // Request location permission
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            //System.out.println("Latitude: " + location.getLatitude());
                            double latitude = location.getLatitude();
                            //System.out.println("Longitude: " + location.getLongitude());
                            double longitude = location.getLongitude();
                            mapView.getController().setCenter(new GeoPoint(latitude, longitude));
                        }
                    }
                });


        // Setup OSMDroid
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        // Setup MapView
        mapView = findViewById(R.id.mapView); // Get the MapView from the layout file
        mapView.setTileSource(TileSourceFactory.MAPNIK); // Use OpenStreetMap tiles
        mapView.getController().setZoom(15.0); // Set initial zoom level
        mapView.setMultiTouchControls(true); // Enable pinch to zoom

    }
}