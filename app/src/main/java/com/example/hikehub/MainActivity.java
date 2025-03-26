package com.example.hikehub;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private MapView mapView;
    private Polyline routeLine;
    private final OkHttpClient client = new OkHttpClient();
    private DatabaseHelper databaseHelper;
    private int hikeId = 3; // Example hike ID, change it based on your data

    private TextView textViewUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Button redirection setup using Utils (Ensure that the Utils.setupButton method works correctly)
        Utils.setupButton(this, R.id.buttonNewHike, NewHikeActivity.class);
        Utils.setupButton(this, R.id.buttonMap, MapActivity.class);
        Utils.setupButton(this, R.id.buttonProfile, ProfileActivity.class);

        // Initialize OpenStreetMap configuration
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        // Initialize Database Helper
        databaseHelper = new DatabaseHelper(this);

        // Set up map view
        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        // TextView for username
        textViewUsername = findViewById(R.id.textViewUsername);

        // Fetch hike details from the database
        fetchHikeDetails(hikeId);
    }

    private void fetchHikeDetails(int hikeId) {
        // Fetch hike details from the database using DatabaseHelper
        Cursor cursor = databaseHelper.getHikeDetails(hikeId);

        if (cursor != null && cursor.moveToFirst()) {
            int startLatIndex = cursor.getColumnIndex("start_latitude");
            int startLonIndex = cursor.getColumnIndex("start_longitude");
            int endLatIndex = cursor.getColumnIndex("end_latitude");
            int endLonIndex = cursor.getColumnIndex("end_longitude");
            int routeTypeIndex = cursor.getColumnIndex("route_type");
            int titleIndex = cursor.getColumnIndex("title");

            if (titleIndex != -1) {
                String hikeTitle = cursor.getString(titleIndex);
                displayTitle(hikeTitle);  // Set the title in the TextView
            }

            if (startLatIndex != -1 && startLonIndex != -1 && endLatIndex != -1 && endLonIndex != -1 && routeTypeIndex != -1) {
                double startLat = cursor.getDouble(startLatIndex);
                double startLon = cursor.getDouble(startLonIndex);
                double endLat = cursor.getDouble(endLatIndex);
                double endLon = cursor.getDouble(endLonIndex);
                String routeType = cursor.getString(routeTypeIndex);

                // Set map view centered on the start point
                GeoPoint startPoint = new GeoPoint(startLat, startLon);
                mapView.getController().setZoom(10.0);
                mapView.getController().setCenter(startPoint);

                // Add markers for start and end points
                addStartEndMarkers(startLat, startLon, endLat, endLon);

                // Fetch the route using OSRM
                fetchRoute(startLat, startLon, endLat, endLon, routeType);

                cursor.close(); // Don't forget to close the cursor
            } else {
                Log.e(TAG, "One or more required columns are missing.");
            }
        } else {
            Log.e(TAG, "Failed to fetch hike details.");
        }
    }

    private void addStartEndMarkers(double startLat, double startLon, double endLat, double endLon) {
        // Start marker
        Marker startMarker = new Marker(mapView);
        startMarker.setPosition(new GeoPoint(startLat, startLon));
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setTitle("Start Point");
        mapView.getOverlays().add(startMarker);

        // End marker
        Marker endMarker = new Marker(mapView);
        endMarker.setPosition(new GeoPoint(endLat, endLon));
        endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        endMarker.setTitle("End Point");
        mapView.getOverlays().add(endMarker);
    }

    private void fetchRoute(double startLat, double startLon, double endLat, double endLon, String routeType) {
        // Construct OSRM route URL
        String url = String.format(
                "http://router.project-osrm.org/route/v1/%s/%f,%f;%f,%f?overview=full&geometries=geojson",
                routeType, startLon, startLat, endLon, endLat
        );

        Log.d(TAG, "Requesting route from OSRM: " + url);

        // Make the request to OSRM
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Route request failed: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Failed to fetch route.", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Route request unsuccessful: " + response.code());
                    return;
                }

                String responseData = response.body().string();
                Log.d(TAG, "OSRM Response: " + responseData);

                runOnUiThread(() -> drawRoute(responseData));
            }
        });
    }

    private void drawRoute(String jsonData) {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();
            JsonArray routes = jsonObject.getAsJsonArray("routes");

            if (routes.size() == 0) {
                Log.e(TAG, "No routes found in response");
                return;
            }

            JsonObject route = routes.get(0).getAsJsonObject();
            JsonArray coordinates = route
                    .getAsJsonObject("geometry")
                    .getAsJsonArray("coordinates");

            List<GeoPoint> geoPoints = new ArrayList<>();
            for (int i = 0; i < coordinates.size(); i++) {
                JsonArray coord = coordinates.get(i).getAsJsonArray();
                double lon = coord.get(0).getAsDouble();
                double lat = coord.get(1).getAsDouble();
                geoPoints.add(new GeoPoint(lat, lon));
            }

            if (routeLine == null) {
                routeLine = new Polyline();
                mapView.getOverlays().add(routeLine);
            }

            routeLine.setPoints(geoPoints);
            routeLine.setColor(Color.RED); // Change to a color visible on your map
            routeLine.setWidth(5.0f);

            mapView.invalidate();
            Log.d(TAG, "Route drawn successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error parsing route JSON: " + e.getMessage());
        }
    }

    private void displayTitle(String title) {
        // Find your TextView (make sure you have it in your layout)
        TextView titleTextView = findViewById(R.id.textViewTitle);
        // Set the title text in the TextView
        titleTextView.setText(title);
    }

}
