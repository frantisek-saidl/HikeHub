package com.example.hikehub;

import android.content.Context;
import android.content.Intent;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


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

        public static String hashPassword(String password) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hashedBytes = digest.digest(password.getBytes());

                StringBuilder hexString = new StringBuilder();
                for (byte b : hashedBytes) {
                    hexString.append(Integer.toHexString(0xff & b));
                }
                return hexString.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
