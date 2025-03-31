package com.example.hikehub;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {

    // This method sets up a button to start a new activity when clicked
    public static void setupButton(final Activity activity, int buttonId, final Class<?> targetActivity) {
        View button = activity.findViewById(buttonId);
        if (button instanceof Button || button instanceof ImageButton) {
            button.setOnClickListener(v -> {
                Intent intent = new Intent(activity, targetActivity);
                activity.startActivity(intent);
            });
        } else {
            throw new IllegalArgumentException("View with ID " + buttonId + " is not a Button or ImageButton");
        }
    }

    // This method hashes a password using SHA-256
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(password.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

}
