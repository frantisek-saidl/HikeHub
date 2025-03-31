package com.example.hikehub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.io.IOException;

public class NewHikeActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageViewHike;
    private String hikeImagePath;
    private EditText editTextTitle, editTextDescription, editTextStartLat, editTextStartLon, editTextEndLat, editTextEndLon;
    private Spinner spinnerProfile;
    private Button buttonSaveHike;
    private MapView mapView;
    private Marker startMarker, endMarker;
    private boolean isPlacingStartMarker = true;

    // Method to initialize the activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_hike);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imageViewHike = findViewById(R.id.imageViewHike);
        Button buttonSelectImage = findViewById(R.id.buttonSelectImage);
        buttonSaveHike = findViewById(R.id.buttonSaveHike);
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextStartLat = findViewById(R.id.editTextStartLat);
        editTextStartLon = findViewById(R.id.editTextStartLon);
        editTextEndLat = findViewById(R.id.editTextEndLat);
        editTextEndLon = findViewById(R.id.editTextEndLon);
        spinnerProfile = findViewById(R.id.spinnerProfile);
        mapView = findViewById(R.id.mapView);

        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);
        mapView.getController().setCenter(new GeoPoint(48.8583, 2.2944));

        buttonSelectImage.setOnClickListener(v -> openImageSelector());
        setupSaveButton();
        setupMapMarkers();
        setupMapClickListener();
    }

    // Method to open the image selector
    private void openImageSelector() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Method to handle the result of the image selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageViewHike.setImageBitmap(bitmap);
                hikeImagePath = getPathFromUri(imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to get the file path from the URI
    private String getPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(columnIndex);
            cursor.close();
            return path;
        }
        return null;
    }

    // Method to set up the save button
    private void setupSaveButton() {
        buttonSaveHike.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString().trim();
            String description = editTextDescription.getText().toString().trim();
            String profile = spinnerProfile.getSelectedItem().toString();

            String startLatStr = editTextStartLat.getText().toString().trim();
            String startLonStr = editTextStartLon.getText().toString().trim();
            String endLatStr = editTextEndLat.getText().toString().trim();
            String endLonStr = editTextEndLon.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(NewHikeActivity.this, "Title is required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (description.isEmpty()) {
                Toast.makeText(NewHikeActivity.this, "Description is required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (startLatStr.isEmpty() || startLonStr.isEmpty() || endLatStr.isEmpty() || endLonStr.isEmpty()) {
                Toast.makeText(NewHikeActivity.this, "Both start and end coordinates are required", Toast.LENGTH_SHORT).show();
                return;
            }

            double startLat, startLon, endLat, endLon;
            try {
                startLat = Double.parseDouble(startLatStr);
                startLon = Double.parseDouble(startLonStr);
                endLat = Double.parseDouble(endLatStr);
                endLon = Double.parseDouble(endLonStr);
            } catch (NumberFormatException e) {
                Toast.makeText(NewHikeActivity.this, "Please enter valid coordinates", Toast.LENGTH_SHORT).show();
                return;
            }

            placeMarker(startLat, startLon, true);
            placeMarker(endLat, endLon, false);

            SharedPreferences sharedPreferences = getSharedPreferences("LoginPreferences", MODE_PRIVATE);
            int userId = sharedPreferences.getInt("userId", -1);

            if (userId == -1) {
                Toast.makeText(NewHikeActivity.this, "User ID not found", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseHelper dbHelper = new DatabaseHelper(this);
            long hikeId = dbHelper.insertHike(userId, title, description, startLat, startLon, endLat, endLon, profile, hikeImagePath);

            if (hikeId != -1) {
                Toast.makeText(this, "Hike saved!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error saving hike!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to set up the map markers
    private void setupMapMarkers() {
        startMarker = new Marker(mapView);
        endMarker = new Marker(mapView);
        mapView.getOverlays().add(startMarker);
        mapView.getOverlays().add(endMarker);
    }

    private void placeMarker(double lat, double lon, boolean isStart) {
        GeoPoint point = new GeoPoint(lat, lon);
        Marker marker = isStart ? startMarker : endMarker;
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setIcon(getResources().getDrawable(isStart ? R.drawable.start_marker : R.drawable.end_marker));
        marker.setDraggable(true);
        marker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(Marker marker) {}

            @Override
            public void onMarkerDragEnd(Marker marker) {
                GeoPoint newPosition = marker.getPosition();
                if (isStart) {
                    editTextStartLat.setText(String.valueOf(newPosition.getLatitude()));
                    editTextStartLon.setText(String.valueOf(newPosition.getLongitude()));
                } else {
                    editTextEndLat.setText(String.valueOf(newPosition.getLatitude()));
                    editTextEndLon.setText(String.valueOf(newPosition.getLongitude()));
                }
            }

            // Method to handle marker drag
            @Override
            public void onMarkerDragStart(Marker marker) {}
        });
        mapView.invalidate();
    }

    // Method to set up the map click listener
    private void setupMapClickListener() {
        mapView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                GeoPoint geoPoint = (GeoPoint) mapView.getProjection().fromPixels((int) event.getX(), (int) event.getY());
                if (isPlacingStartMarker) {
                    placeMarker(geoPoint.getLatitude(), geoPoint.getLongitude(), true);
                    editTextStartLat.setText(String.valueOf(geoPoint.getLatitude()));
                    editTextStartLon.setText(String.valueOf(geoPoint.getLongitude()));
                    isPlacingStartMarker = false;
                } else {
                    placeMarker(geoPoint.getLatitude(), geoPoint.getLongitude(), false);
                    editTextEndLat.setText(String.valueOf(geoPoint.getLatitude()));
                    editTextEndLon.setText(String.valueOf(geoPoint.getLongitude()));
                    isPlacingStartMarker = true;
                }
            }
            return false;
        });
    }
}