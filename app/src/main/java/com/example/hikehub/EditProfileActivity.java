package com.example.hikehub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private SharedPreferences sharedPreferences;
    private DatabaseHelper databaseHelper;
    private EditText editTextFirstName;
    private EditText editTextLastName;
    private EditText editTextUserName;
    private ImageView profileImageView;
    private Uri profileImageUri;

    // Method to initialize the activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPreferences = getSharedPreferences("LoginPreferences", MODE_PRIVATE);
        databaseHelper = new DatabaseHelper(this);

        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextUserName = findViewById(R.id.editTextUserName);
        profileImageView = findViewById(R.id.profileImageView);

        String username = sharedPreferences.getString("username", null);
        if (username != null) {
            editTextUserName.setText(username);
            editTextFirstName.setText(databaseHelper.getFirstName(username));
            editTextLastName.setText(databaseHelper.getLastName(username));
            String profilePicturePath = databaseHelper.getProfilePicturePath(username);
            if (profilePicturePath != null) {
                profileImageView.setImageBitmap(BitmapFactory.decodeFile(profilePicturePath));
            }
        }

        Button addProfilePicButton = findViewById(R.id.profile_pic_button);
        addProfilePicButton.setOnClickListener(view -> openImageChooser());

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(view -> {
            String newFirstName = editTextFirstName.getText().toString();
            String newLastName = editTextLastName.getText().toString();
            String newUserName = editTextUserName.getText().toString();

            databaseHelper.updateUserProfile(username, newFirstName, newLastName, newUserName);

            if (profileImageUri != null) {
                String profilePicturePath = saveProfilePicture(profileImageUri);
                databaseHelper.updateProfilePicturePath(newUserName, profilePicturePath);
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("username", newUserName);
            editor.apply();

            Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    // Method to open the image chooser
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Method to handle the result of the image chooser
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            profileImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), profileImageUri);
                profileImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to save the profile picture to internal storage
    private String saveProfilePicture(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File profilePicFile = new File(getFilesDir(), "profile_pic.jpg");
            FileOutputStream outputStream = new FileOutputStream(profilePicFile);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            return profilePicFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}