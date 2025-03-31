package com.example.hikehub;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DetailActivity extends AppCompatActivity {

    private TextView postTitle;
    private TextView hikeDescription;
    private TextView creatorUsername;
    private TextView createdAt;
    private ImageView postImage;
    private MapView mapView;

    // Method to initialize the activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        postTitle = findViewById(R.id.hikeTitle);
        hikeDescription = findViewById(R.id.hikeDescription);
        creatorUsername = findViewById(R.id.textView);
        createdAt = findViewById(R.id.createdAt);
        postImage = findViewById(R.id.hikeImage);
        mapView = findViewById(R.id.mapView);

        int postId = getIntent().getIntExtra("postId", -1);
        if (postId != -1) {
            loadPostDetails(postId);
        } else {
            Log.e("DetailActivity", "Invalid post ID");
        }
    }

    // Method to load post details from the database
    private void loadPostDetails(int postId) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Cursor cursor = dbHelper.getHikeDetails(postId);

        if (cursor != null && cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
            String picturePath = cursor.getString(cursor.getColumnIndexOrThrow("picture_path"));
            String createdAtText = cursor.getString(cursor.getColumnIndexOrThrow("created_at"));
            String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
            double startLat = cursor.getDouble(cursor.getColumnIndexOrThrow("start_latitude"));
            double startLon = cursor.getDouble(cursor.getColumnIndexOrThrow("start_longitude"));
            double endLat = cursor.getDouble(cursor.getColumnIndexOrThrow("end_latitude"));
            double endLon = cursor.getDouble(cursor.getColumnIndexOrThrow("end_longitude"));
            String routeType = cursor.getString(cursor.getColumnIndexOrThrow("route_type"));

            postTitle.setText(title);
            hikeDescription.setText(description);
            creatorUsername.setText("Created by: " + username);
            createdAt.setText("Created at: " + createdAtText);

            if (picturePath != null) {
                File imgFile = new File(picturePath);
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    postImage.setImageBitmap(bitmap);
                } else {
                    postImage.setImageResource(R.drawable.map);
                    Log.e("DetailActivity", "Image file not found: " + picturePath);
                }
            } else {
                postImage.setImageResource(R.drawable.map);
            }

            fetchRoute(startLat, startLon, endLat, endLon, routeType);
        } else {
            Log.e("DetailActivity", "No data found for postId: " + postId);
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    // Method to fetch the route from the OSRM API
    private void fetchRoute(double startLat, double startLon, double endLat, double endLon, String routeType) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://router.project-osrm.org/route/v1/" + routeType + "/" + startLon + "," + startLat + ";" + endLon + "," + endLat + "?overview=full&geometries=geojson";

        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("DetailActivity", "Failed to fetch route", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        JSONArray routes = jsonResponse.getJSONArray("routes");
                        if (routes.length() > 0) {
                            JSONObject route = routes.getJSONObject(0);
                            JSONObject geometry = route.getJSONObject("geometry");
                            JSONArray coordinates = geometry.getJSONArray("coordinates");

                            List<GeoPoint> geoPoints = new ArrayList<>();
                            for (int i = 0; i < coordinates.length(); i++) {
                                JSONArray coord = coordinates.getJSONArray(i);
                                double lon = coord.getDouble(0);
                                double lat = coord.getDouble(1);
                                geoPoints.add(new GeoPoint(lat, lon));
                            }

                            runOnUiThread(() -> {
                                Polyline polyline = new Polyline();
                                polyline.setPoints(geoPoints);
                                polyline.setColor(getResources().getColor(R.color.polyline));
                                mapView.getOverlays().add(polyline);

                                Marker startMarker = new Marker(mapView);
                                GeoPoint startPoint = new GeoPoint(startLat, startLon);
                                startMarker.setPosition(startPoint);
                                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                                startMarker.setIcon(getResources().getDrawable(R.drawable.start_marker));
                                startMarker.setTitle("Start Point");
                                mapView.getOverlays().add(startMarker);

                                Marker endMarker = new Marker(mapView);
                                GeoPoint endPoint = new GeoPoint(endLat, endLon);
                                endMarker.setPosition(endPoint);
                                endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                                endMarker.setIcon(getResources().getDrawable(R.drawable.end_marker));
                                endMarker.setTitle("End Point");
                                mapView.getOverlays().add(endMarker);

                                mapView.post(() -> {
                                    mapView.getController().animateTo(startPoint);
                                    mapView.getController().setZoom(17.0);
                                });
                            });

                        }
                    } catch (Exception e) {
                        Log.e("DetailActivity", "Failed to parse route response", e);
                    }
                } else {
                    Log.e("DetailActivity", "Failed to fetch route: " + response.message());
                }
            }
        });
    }
}