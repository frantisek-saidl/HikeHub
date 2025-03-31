package com.example.hikehub;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "HikeHub.db";
    private static final int DATABASE_VERSION = 7;
    private final Context context;

    // SQL statement to create the users table
    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE IF NOT EXISTS users (" +
                    "idusers INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT NOT NULL UNIQUE, " +
                    "password_hash TEXT NOT NULL, " +
                    "profile_picture_path TEXT, " +
                    "first_name TEXT, " +
                    "last_name TEXT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";

    // SQL statement to create the hikes table
    private static final String CREATE_TABLE_HIKES =
            "CREATE TABLE IF NOT EXISTS hikes (" +
                    "idhikes INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "users_idusers INTEGER NOT NULL, " +
                    "title TEXT NOT NULL, " +
                    "description TEXT, " +
                    "picture_path TEXT, " +
                    "start_latitude REAL NOT NULL, " +
                    "start_longitude REAL NOT NULL, " +
                    "end_latitude REAL NOT NULL, " +
                    "end_longitude REAL NOT NULL, " +
                    "route_type TEXT NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (users_idusers) REFERENCES users(idusers) ON DELETE CASCADE" +
                    ");";

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    // Called when the database is created for the first time
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_HIKES);
    }

    // Called when the database version is incremented
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS hikes");
        db.execSQL("DROP TABLE IF EXISTS users");
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginPreferences", Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
        onCreate(db);
    }

    // Method to insert a new hike into the database
    public long insertHike(int userId, String title, String description, double startLatitude, double startLongitude, double endLatitude, double endLongitude, String routeType, String picturePath) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put("users_idusers", userId);
            values.put("title", title);
            values.put("description", description);
            values.put("start_latitude", startLatitude);
            values.put("start_longitude", startLongitude);
            values.put("end_latitude", endLatitude);
            values.put("end_longitude", endLongitude);
            values.put("route_type", routeType);
            values.put("picture_path", picturePath);
            return db.insert("hikes", null, values);
        }
    }

    // Method to register a new user
    public String registerUser(String username, String passwordHash) {
        try (SQLiteDatabase db = this.getWritableDatabase();
             Cursor cursor = db.rawQuery("SELECT username FROM users WHERE username = ?", new String[]{username})) {

            if (cursor.moveToFirst()) {
                cursor.close();
                return "Username already exists";
            }
            cursor.close();

            ContentValues values = new ContentValues();
            values.put("username", username);
            values.put("password_hash", passwordHash);

            long newRowId = db.insert("users", null, values);
            if (newRowId != -1) {
                SharedPreferences sharedPreferences = context.getSharedPreferences("LoginPreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", username);
                editor.putInt("userId", (int) newRowId);
                editor.apply();
                return "User registered successfully";
            } else {
                return "Error occurred during registration";
            }
        }
    }

    // Method to check if password matches with database
    public boolean checkPassword(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT password_hash FROM users WHERE username = ?", new String[]{username});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String storedPasswordHash = cursor.getString(cursor.getColumnIndexOrThrow("password_hash"));
                cursor.close();
                return storedPasswordHash.equals(password);
            }
            cursor.close();
        }
        return false;
    }

    //Method to get all hikes from the database
    public Cursor getAllHikes() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM hikes ORDER BY created_at DESC";
        return db.rawQuery(query, null);
    }

    // Method to get hike details for a specific hike based on the hike ID
    public Cursor getHikeDetails(int hikeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT hikes.title, hikes.description, hikes.picture_path, hikes.created_at, users.username, " +
                "hikes.start_latitude, hikes.start_longitude, hikes.end_latitude, hikes.end_longitude, hikes.route_type " +
                "FROM hikes " +
                "JOIN users ON hikes.users_idusers = users.idusers " +
                "WHERE hikes.idhikes = ?";
        Log.d("DatabaseHelper", "Executing query: " + query + " with ID: " + hikeId);

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(hikeId)});

        if (cursor != null && cursor.moveToFirst()) {
            Log.d("DatabaseHelper", "Data found for hikeId: " + hikeId);
        } else {
            Log.e("DatabaseHelper", "No data found for hikeId: " + hikeId);
        }

        return cursor;
    }

    // Method to get the profile picture path for a specific user
    public String getProfilePicturePath(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT profile_picture_path FROM users WHERE username = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow("profile_picture_path");
                if (columnIndex != -1) {
                    String profilePicturePath = cursor.getString(columnIndex);
                    cursor.close();
                    return profilePicturePath;
                }
            }
            cursor.close();
        }
        return null;
    }

    // Method to get the users first name
    public String getFirstName(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT first_name FROM users WHERE username = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow("first_name");
                if (columnIndex != -1) {
                    String firstName = cursor.getString(columnIndex);
                    cursor.close();
                    return firstName;
                }
            }
            cursor.close();
        }
        return null;
    }

    // Method to get the user's last name
    public String getLastName(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT last_name FROM users WHERE username = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow("last_name");
                if (columnIndex != -1) {
                    String lastName = cursor.getString(columnIndex);
                    cursor.close();
                    return lastName;
                }
            }
            cursor.close();
        }
        return null;
    }

    // Method to update the user's profile information
    public void updateUserProfile(String oldUsername, String newFirstName, String newLastName, String newUserName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("first_name", newFirstName);
        values.put("last_name", newLastName);
        values.put("username", newUserName);
        db.update("users", values, "username = ?", new String[]{oldUsername});
    }

    // Method to get the user's posts
    public List<Post> getUserPosts(String username) {
        List<Post> posts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT hikes.idhikes, hikes.title, hikes.picture_path " +
                "FROM hikes " +
                "JOIN users ON hikes.users_idusers = users.idusers " +
                "WHERE users.username = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{username})) {
            if (cursor != null) {
                int idIndex = cursor.getColumnIndexOrThrow("idhikes");
                int titleIndex = cursor.getColumnIndexOrThrow("title");
                int picturePathIndex = cursor.getColumnIndexOrThrow("picture_path");

                while (cursor.moveToNext()) {
                    int id = cursor.getInt(idIndex);
                    String title = cursor.getString(titleIndex);
                    String picturePath = cursor.getString(picturePathIndex);

                    if (id > 0) {
                        posts.add(new Post(id, title, picturePath));
                    } else {
                        Log.e("DatabaseHelper", "Invalid post ID: " + id);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error fetching user posts", e);
        }

        return posts;
    }

    // Method to update the user's profile picture path
    public void updateProfilePicturePath(String username, String profilePicturePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("profile_picture_path", profilePicturePath);
        db.update("users", values, "username = ?", new String[]{username});
    }

    // Method to get the user's ID
    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT idusers FROM users WHERE username = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow("idusers"));
                Log.i("DatabaseHelper", "User ID for " + username + ": " + userId);
                cursor.close();
                return userId;
            }
            cursor.close();
        }
        return -1;
    }
}