package com.example.hikehub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ProfileActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("LoginPreferences", MODE_PRIVATE);

        // Get the username from SharedPreferences (if available)
        String username = sharedPreferences.getString("username", null);

        // Optionally display the username in your UI (e.g., TextView)
        // TextView usernameTextView = findViewById(R.id.usernameTextView);
        // usernameTextView.setText("Hello, " + username);

        // Logout button setup
        Button logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(view -> {
            // Clear SharedPreferences to log the user out
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();  // Clears all data in SharedPreferences
            editor.apply();  // Apply changes

            // Optionally, show a message
            Toast.makeText(ProfileActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            // Navigate back to the Login Activity
            Intent loginIntent = new Intent(ProfileActivity.this, StartupActivity.class);
            startActivity(loginIntent);
            finish();  // Finish the current activity to prevent user from coming back
        });
    }
}
