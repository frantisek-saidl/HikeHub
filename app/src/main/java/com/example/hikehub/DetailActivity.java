package com.example.hikehub;

import android.database.Cursor;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.api.IMapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.util.GeoPoint;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private MapView mapView;
    private DatabaseHelper dbHelper;
    private int hikeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        dbHelper = new DatabaseHelper(this);
        hikeId = getIntent().getIntExtra("hike_id", -1); // Default to -1 if no ID is passed

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Initialize OSMDroid map
        mapView = findViewById(R.id.mapview);
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        // Get hike details
        Cursor cursor = dbHelper.getHikeDetails(hikeId);
        if (cursor != null && cursor.moveToFirst()) {
            double startLatitude = cursor.getDouble(cursor.getColumnIndex("start_latitude"));
            double startLongitude = cursor.getDouble(cursor.getColumnIndex("start_longitude"));
            double endLatitude = cursor.getDouble(cursor.getColumnIndex("end_latitude"));
            double endLongitude = cursor.getDouble(cursor.getColumnIndex("end_longitude"));

            GeoPoint startPoint = new GeoPoint(startLatitude, startLongitude);
            IMapController mapController = mapView.getController();
            mapController.setZoom(10);
            mapController.setCenter(startPoint);

            // Fetch and display route
            fetchRoute(startLatitude, startLongitude, endLatitude, endLongitude);
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    private void fetchRoute(double startLat, double startLon, double endLat, double endLon) {
        try {
            // OSRM API URL
            String urlString = String.format("http://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?overview=full&geometries=geojson",
                    startLon, startLat, endLon, endLat);

            // Send HTTP request
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Parse the response and extract route coordinates
            parseRoute(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to fetch route", Toast.LENGTH_SHORT).show();
        }
    }

    private void parseRoute(String response) {
        try {
            // Simple parsing of the JSON response (OSRM API returns GeoJSON)
            int coordinatesStartIndex = response.indexOf("coordinates\":") + 14;
            int coordinatesEndIndex = response.indexOf("]}]}", coordinatesStartIndex);
            String coordinatesString = response.substring(coordinatesStartIndex, coordinatesEndIndex);

            String[] coordinatesArray = coordinatesString.split("],\\[");

            List<GeoPoint> routePoints = new ArrayList<>();
            for (String coordinate : coordinatesArray) {
                String[] coords = coordinate.replace("[", "").replace("]", "").split(",");
                double lon = Double.parseDouble(coords[0]);
                double lat = Double.parseDouble(coords[1]);
                routePoints.add(new GeoPoint(lat, lon));
            }

            // Create polyline from route points and add it to the map
            Polyline polyline = new Polyline();
            for (GeoPoint point : routePoints) {
                polyline.addPoint(point);
            }
            mapView.getOverlayManager().add(polyline);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing route data", Toast.LENGTH_SHORT).show();
        }
    }
}
