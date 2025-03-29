package com.example.hikehub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private DatabaseHelper databaseHelper;
    private ImageView profileImageView;
    private TextView usernameTextView;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;

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

        sharedPreferences = getSharedPreferences("LoginPreferences", MODE_PRIVATE);
        databaseHelper = new DatabaseHelper(this);
        profileImageView = findViewById(R.id.profileImageView);
        usernameTextView = findViewById(R.id.usernameTextView);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadProfileData();
        loadUserPosts();

        Button editProfileButton = findViewById(R.id.edit_profile_button);
        editProfileButton.setOnClickListener(view -> {
            Intent editProfileIntent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(editProfileIntent);
        });

        Button logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(view -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Toast.makeText(ProfileActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            Intent loginIntent = new Intent(this, StartupActivity.class);
            startActivity(loginIntent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData();
        loadUserPosts();
    }

    private void loadProfileData() {
        String username = sharedPreferences.getString("username", null);
        if (username != null) {
            usernameTextView.setText(username);
            String profilePicturePath = databaseHelper.getProfilePicturePath(username);
            if (profilePicturePath != null) {
                File imgFile = new File(profilePicturePath);
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    profileImageView.setImageBitmap(bitmap);
                } else {
                    Log.e("ProfileActivity", "Profile picture not found at path: " + profilePicturePath);
                    Toast.makeText(this, "Profile picture not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("ProfileActivity", "Profile picture path is null for username: " + username);
            }
        } else {
            Log.e("ProfileActivity", "Username is null in sharedPreferences");
        }
    }

    private void loadUserPosts() {
        String username = sharedPreferences.getString("username", null);
        if (username != null) {
            List<Post> userPosts = databaseHelper.getUserPosts(username);
            postAdapter = new PostAdapter(this, userPosts);
            recyclerView.setAdapter(postAdapter);
        }
    }
}