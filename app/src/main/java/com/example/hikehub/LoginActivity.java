package com.example.hikehub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper; // Database instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the database helper
        dbHelper = new DatabaseHelper(this);

        // Get the input fields
        EditText inputUsername = findViewById(R.id.inputUsername);
        EditText inputPassword = findViewById(R.id.inputPassword);
        Button buttonSubmit = findViewById(R.id.buttonSubmit);

        // Check if the username is already saved in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPreferences", MODE_PRIVATE);
        String storedUsername = sharedPreferences.getString("username", null);

        // If a username exists in SharedPreferences, skip the login process and go to MainActivity
        if (storedUsername != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("username", storedUsername);
            startActivity(intent);
            finish(); // Close LoginActivity
            return; // Prevent the rest of the code from executing
        }

        // Button click listener for login
        buttonSubmit.setOnClickListener(v -> {
            // Extract user inputs
            String username = inputUsername.getText().toString().trim();
            String password = inputPassword.getText().toString();

            // Validate inputs
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Both fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Hash the input password before checking (use the same hashing function you used during registration)
            String hashedPassword = Utils.hashPassword(password);

            // Check if the username and password are correct
            if (dbHelper.checkPassword(username, hashedPassword)) {
                // Successful login
                Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                // Save the username and password hash in SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", username);
                editor.putString("passwordHash", hashedPassword);
                editor.apply();

                // Navigate to MainActivity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
                finish(); // Close the login activity
            } else {
                // Incorrect username or password
                Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
