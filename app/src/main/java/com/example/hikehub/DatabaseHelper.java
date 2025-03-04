package com.example.hikehub;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "HikeHub.db";
    private static final int DATABASE_VERSION = 1;

    // SQL query to create the users table
    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE IF NOT EXISTS users (" +
                    "idusers INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT NOT NULL UNIQUE, " +
                    "password_hash TEXT NOT NULL, " +
                    "profile_picture BLOB, " +  // SQLite uses BLOB for binary data
                    "bio TEXT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";

    // SQL query to create the hikes table
    private static final String CREATE_TABLE_HIKES =
            "CREATE TABLE IF NOT EXISTS hikes (" +
                    "idhikes INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "users_idusers INTEGER NOT NULL, " +
                    "title TEXT NOT NULL, " +
                    "description TEXT, " +
                    "length_km REAL, " +  // Supports up to 99,999.99 km
                    "elevation_gain_m INTEGER, " +  // Supports elevations up to 16.7 million meters
                    "highest_point_m INTEGER, " +
                    "start_latitude REAL NOT NULL, " +  // Latitude for start location
                    "start_longitude REAL NOT NULL, " + // Longitude for start location
                    "end_latitude REAL NOT NULL, " +    // Latitude for end location
                    "end_longitude REAL NOT NULL, " +   // Longitude for end location
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (users_idusers) REFERENCES users(idusers) ON DELETE CASCADE" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the users and hikes tables
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_HIKES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the old tables if they exist and create new ones
        db.execSQL("DROP TABLE IF EXISTS hikes");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    // Insert a hike into the hikes table
    public long insertHike(int userId, String title, String description, double length, int elevationGain, int highestPoint, String startLocation, String endLocation) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("users_idusers", userId);
        values.put("title", title);
        values.put("description", description);
        values.put("length_km", length);
        values.put("elevation_gain_m", elevationGain);
        values.put("highest_point_m", highestPoint);
        values.put("start_location", startLocation);
        values.put("end_location", endLocation);

        // Insert the new hike and get the new row ID
        long newRowId = db.insert("hikes", null, values);
        db.close();
        return newRowId;  // Return the ID of the newly inserted row
    }

    // Insert User with validation for username uniqueness
    public String registerUser(String username, String passwordHash, String bio) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if the username already exists
        String query = "SELECT username FROM users WHERE username = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});

        if (cursor != null && cursor.moveToFirst()) {
            // Username already exists, return a failure message
            cursor.close();
            db.close();
            return "Username already exists";
        }

        // Username does not exist, proceed to insert the user
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password_hash", passwordHash);
        values.put("bio", bio);

        // Insert the new row and get the new row ID
        long newRowId = db.insert("users", null, values);
        cursor.close();
        db.close();

        if (newRowId != -1) {
            return "User registered successfully";
        } else {
            return "Error occurred during registration";
        }
    }

    // Check password during login
    public boolean checkPassword(String username, String passwordHash) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to get the stored password hash for the username
        String query = "SELECT password_hash FROM users WHERE username = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});

        boolean isPasswordCorrect = false;

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                // Get the index of the password_hash column
                int columnIndex = cursor.getColumnIndex("password_hash");

                if (columnIndex != -1) {  // Make sure the column exists
                    String storedPasswordHash = cursor.getString(columnIndex);
                    isPasswordCorrect = storedPasswordHash.equals(passwordHash);
                } else {
                    // Log an error or handle the case where the column doesn't exist
                    Log.e("DatabaseHelper", "password_hash column not found.");
                }
            }
            cursor.close();
        }

        db.close();
        return isPasswordCorrect;  // Return whether the passwords match
    }
}
