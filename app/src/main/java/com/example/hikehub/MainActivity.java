package com.example.hikehub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Set padding based on system bar insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize buttons and set their onClick listeners in a loop
        setupButton(R.id.button_map, MapActivity.class);
        setupButton(R.id.button_sign_up, SignUpActivity.class);
        setupButton(R.id.button_login, LoginActivity.class);
        setupButton(R.id.button_profile, ProfileActivity.class);
        setupButton(R.id.button_new_hike, NewHikeActivity.class);
    }

    // Helper method to set up buttons with their respective intents
    private void setupButton(int buttonId, Class<?> targetActivity) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, targetActivity);
            startActivity(intent);
        });
    }
}
