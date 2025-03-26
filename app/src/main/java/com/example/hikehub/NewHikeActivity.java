package com.example.hikehub;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.events.MapEventsReceiver;

public class NewHikeActivity extends AppCompatActivity {

    private MapView mapView;
    private Marker startMarker, endMarker;
    private boolean selectingStart = true; // True: selecting start, False: selecting end

    private EditText editTextStartLat, editTextStartLon, editTextEndLat, editTextEndLon, editTextTitle, editTextDescription;
    private Spinner spinnerProfile;
    private Button buttonSaveHike, buttonResetMarkers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_hike);

        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        // Initialize UI components
        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(12.0);

        editTextStartLat = findViewById(R.id.editTextStartLat);
        editTextStartLon = findViewById(R.id.editTextStartLon);
        editTextEndLat = findViewById(R.id.editTextEndLat);
        editTextEndLon = findViewById(R.id.editTextEndLon);
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        spinnerProfile = findViewById(R.id.spinnerProfile);
        buttonSaveHike = findViewById(R.id.buttonSaveHike);
        buttonResetMarkers = findViewById(R.id.buttonResetMarkers);

        setupMapInteraction();
        setupSaveButton();
        setupResetButton();  // Set up the reset button listener
    }

    private void setupMapInteraction() {
        MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
                if (selectingStart) {
                    setStartMarker(geoPoint);
                    selectingStart = false; // Next tap will set end marker
                    Toast.makeText(NewHikeActivity.this, "Now select end point", Toast.LENGTH_SHORT).show();
                } else {
                    setEndMarker(geoPoint);
                }
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint geoPoint) {
                return false;
            }
        };

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(mapEventsReceiver);
        mapView.getOverlays().add(mapEventsOverlay);
    }

    private void setStartMarker(GeoPoint geoPoint) {
        if (startMarker == null) {
            startMarker = new Marker(mapView);
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            startMarker.setDraggable(true);
            startMarker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {}
                @Override
                public void onMarkerDrag(Marker marker) {}
                @Override
                public void onMarkerDragEnd(Marker marker) {
                    updateStartCoordinates(marker.getPosition());
                }
            });
            mapView.getOverlays().add(startMarker);
        }
        startMarker.setPosition(geoPoint);
        updateStartCoordinates(geoPoint);
        mapView.invalidate();
    }

    private void setEndMarker(GeoPoint geoPoint) {
        if (endMarker == null) {
            endMarker = new Marker(mapView);
            endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            endMarker.setDraggable(true);
            endMarker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {
                    updateStartCoordinates(marker.getPosition());
                }
                @Override
                public void onMarkerDrag(Marker marker) {}
                @Override
                public void onMarkerDragEnd(Marker marker) {
                    updateEndCoordinates(marker.getPosition());
                }
            });
            mapView.getOverlays().add(endMarker);
        }
        endMarker.setPosition(geoPoint);
        updateEndCoordinates(geoPoint);
        mapView.invalidate();
    }

    private void updateStartCoordinates(GeoPoint geoPoint) {
        editTextStartLat.setText(String.valueOf(geoPoint.getLatitude()));
        editTextStartLon.setText(String.valueOf(geoPoint.getLongitude()));
    }

    private void updateEndCoordinates(GeoPoint geoPoint) {
        editTextEndLat.setText(String.valueOf(geoPoint.getLatitude()));
        editTextEndLon.setText(String.valueOf(geoPoint.getLongitude()));
    }
    private void setupSaveButton() {
        buttonSaveHike.setOnClickListener(v -> {
            // Get the input values
            String title = editTextTitle.getText().toString().trim();
            String description = editTextDescription.getText().toString().trim();
            String profile = spinnerProfile.getSelectedItem().toString();

            // Get the latitude and longitude for start and end points
            String startLatStr = editTextStartLat.getText().toString().trim();
            String startLonStr = editTextStartLon.getText().toString().trim();
            String endLatStr = editTextEndLat.getText().toString().trim();
            String endLonStr = editTextEndLon.getText().toString().trim();

            // Validate input fields
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

            // Validate that the coordinates are valid numbers
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

            // All validations passed, proceed to save the hike
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            long hikeId = dbHelper.insertHike(1, title, description, 0, 0, 0, startLat, startLon, endLat, endLon,profile);

            if (hikeId != -1) {
                Toast.makeText(this, "Hike saved!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error saving hike!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupResetButton() {
        buttonResetMarkers.setOnClickListener(v -> resetMarkers());
    }

    private void resetMarkers() {
        // Remove the markers from the map and reset input fields
        if (startMarker != null) {
            mapView.getOverlays().remove(startMarker);
            startMarker = null;
        }
        if (endMarker != null) {
            mapView.getOverlays().remove(endMarker);
            endMarker = null;
        }

        // Clear coordinates in the EditText fields
        editTextStartLat.setText("");
        editTextStartLon.setText("");
        editTextEndLat.setText("");
        editTextEndLon.setText("");

        selectingStart = true; // Reset to selecting start
        Toast.makeText(this, "Markers reset", Toast.LENGTH_SHORT).show();
    }
}

