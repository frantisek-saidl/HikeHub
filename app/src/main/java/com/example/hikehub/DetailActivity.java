package com.example.hikehub;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class DetailActivity extends AppCompatActivity {

    private TextView postTitle;
    private TextView hikeDescription;
    private TextView creatorUsername;
    private TextView createdAt;
    private ImageView postImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        postTitle = findViewById(R.id.hikeTitle);
        hikeDescription = findViewById(R.id.hikeDescription);
        creatorUsername = findViewById(R.id.textView);
        createdAt = findViewById(R.id.createdAt);
        postImage = findViewById(R.id.hikeImage);

        int postId = getIntent().getIntExtra("postId", -1);
        if (postId != -1) {
            loadPostDetails(postId);
        } else {
            Log.e("DetailActivity", "Invalid post ID");
        }
    }

    private void loadPostDetails(int postId) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Cursor cursor = dbHelper.getHikeDetails(postId);

        if (cursor != null && cursor.moveToFirst()) {
            int titleIndex = cursor.getColumnIndex("title");
            int descriptionIndex = cursor.getColumnIndex("description");
            int picturePathIndex = cursor.getColumnIndex("picture_path");
            int createdAtIndex = cursor.getColumnIndex("created_at");
            int usernameIndex = cursor.getColumnIndex("username");

            if (titleIndex != -1 && descriptionIndex != -1 && picturePathIndex != -1 && createdAtIndex != -1 && usernameIndex != -1) {
                String title = cursor.getString(titleIndex);
                String description = cursor.getString(descriptionIndex);
                String picturePath = cursor.getString(picturePathIndex);
                String createdAtText = cursor.getString(createdAtIndex);
                String username = cursor.getString(usernameIndex);

                postTitle.setText(title);
                hikeDescription.setText(description);
                creatorUsername.setText("Created by: " + username);
                createdAt.setText("Created at: " + createdAtText);

                File imgFile = new File(picturePath);
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    postImage.setImageBitmap(bitmap);
                } else {
                    Log.e("DetailActivity", "Image file not found: " + picturePath);
                }
            } else {
                Log.e("DetailActivity", "Column not found in cursor");
            }
        } else {
            Log.e("DetailActivity", "Cursor is null or empty");
        }
        if (cursor != null) {
            cursor.close();
        }
    }
}