package com.example.hikehub;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_READ_MEDIA_IMAGES = 1;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Utils.setupButton(this, R.id.newHike, NewHikeActivity.class);
        Utils.setupButton(this, R.id.profile, ProfileActivity.class);
        Utils.setupButton(this, R.id.map, MapActivity.class);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(this, postList);
        recyclerView.setAdapter(postAdapter);

        checkPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPostsFromDatabase();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        REQUEST_READ_MEDIA_IMAGES);
            } else {
                loadPostsFromDatabase();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_READ_MEDIA_IMAGES);
            } else {
                loadPostsFromDatabase();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_MEDIA_IMAGES) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadPostsFromDatabase();
            } else {
                Log.e("MainActivity", "Permission denied to read media images");
            }
        }
    }

    private void loadPostsFromDatabase() {
        postList.clear();
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Cursor cursor = dbHelper.getAllHikes();

        if (cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex("idhikes");
                int titleIndex = cursor.getColumnIndex("title");
                int picturePathIndex = cursor.getColumnIndex("picture_path");

                if (idIndex != -1 && titleIndex != -1 && picturePathIndex != -1) {
                    int id = cursor.getInt(idIndex);
                    String title = cursor.getString(titleIndex);
                    String picturePath = cursor.getString(picturePathIndex);
                    Log.d("MainActivity", "ID: " + id + ", Title: " + title + ", PicturePath: " + picturePath);
                    postList.add(new Post(id, title, picturePath));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        postAdapter.notifyDataSetChanged();
    }
}