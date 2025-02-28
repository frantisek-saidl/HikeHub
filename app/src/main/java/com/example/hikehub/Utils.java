package com.example.hikehub;

import android.content.Context;
import android.content.Intent;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Utils {

    public static void setupButton(Context context, int buttonId, Class<?> targetActivity) {
        Button button = ((AppCompatActivity) context).findViewById(buttonId);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(context, targetActivity);
            context.startActivity(intent);
        });
    }
}