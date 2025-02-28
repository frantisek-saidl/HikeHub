package com.example.hikehub;

import android.os.Bundle;

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

        // Initialize buttons and set their onClick listeners
        Utils.setupButton(this, R.id.buttonMap, MapActivity.class);
        Utils.setupButton(this, R.id.buttonSignUp, SignUpActivity.class);
        Utils.setupButton(this, R.id.buttonLogin, LoginActivity.class);
        Utils.setupButton(this, R.id.buttonProfile, ProfileActivity.class);
        Utils.setupButton(this, R.id.buttonNewHike, NewHikeActivity.class);
    }
}
