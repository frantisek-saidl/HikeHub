package com.example.hikehub;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {

    public static void setupButton(Context context, int buttonId, Class<?> targetActivity) {
        Button button = ((AppCompatActivity) context).findViewById(buttonId);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(context, targetActivity);
            context.startActivity(intent);
        });
    }

    // Hash password using SHA-256
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(password.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                hexString.append(String.format("%02x", b));  // Ensures proper padding
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Initialize and return a MapView
    public static MapView createMap(Context context, FrameLayout container, double latitude, double longitude, int zoomLevel) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE));

        MapView mapView = new MapView(context);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(zoomLevel);
        mapView.getController().setCenter(new GeoPoint(latitude, longitude));

        // Add Compass Overlay
        CompassOverlay compassOverlay = new CompassOverlay(context, new InternalCompassOrientationProvider(context), mapView);
        compassOverlay.enableCompass();
        mapView.getOverlays().add(compassOverlay);

        // Add Scale Bar Overlay
        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(mapView);
        scaleBarOverlay.setAlignRight(true);
        mapView.getOverlays().add(scaleBarOverlay);

        // Attach the map to the provided container
        container.addView(mapView);

        return mapView;
    }
}
