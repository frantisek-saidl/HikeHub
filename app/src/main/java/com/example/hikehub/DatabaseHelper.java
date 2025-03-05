package com.example.hikehub;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "HikeHub.db";
    private static final int DATABASE_VERSION = 1;
    private final Context context;

    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE IF NOT EXISTS users (" +
                    "idusers INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT NOT NULL UNIQUE, " +
                    "password_hash TEXT NOT NULL, " +
                    "profile_picture BLOB, " +
                    "bio TEXT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";

    private static final String CREATE_TABLE_HIKES =
            "CREATE TABLE IF NOT EXISTS hikes (" +
                    "idhikes INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "users_idusers INTEGER NOT NULL, " +
                    "title TEXT NOT NULL, " +
                    "description TEXT, " +
                    "length_km REAL, " +
                    "elevation_gain_m INTEGER, " +
                    "highest_point_m INTEGER, " +
                    "start_latitude REAL NOT NULL, " +
                    "start_longitude REAL NOT NULL, " +
                    "end_latitude REAL NOT NULL, " +
                    "end_longitude REAL NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (users_idusers) REFERENCES users(idusers) ON DELETE CASCADE" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_HIKES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS hikes");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    public long insertHike(int userId, String title, String description, double length, int elevationGain, int highestPoint, String startLocation, String endLocation) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put("users_idusers", userId);
            values.put("title", title);
            values.put("description", description);
            values.put("length_km", length);
            values.put("elevation_gain_m", elevationGain);
            values.put("highest_point_m", highestPoint);
            values.put("start_location", startLocation);
            values.put("end_location", endLocation);

            return db.insert("hikes", null, values);
        }
    }

    public String registerUser(String username, String passwordHash, String bio) {
        try (SQLiteDatabase db = this.getWritableDatabase();
             Cursor cursor = db.rawQuery("SELECT username FROM users WHERE username = ?", new String[]{username})) {

            if (cursor.moveToFirst()) {
                return "Username already exists";
            }

            ContentValues values = new ContentValues();
            values.put("username", username);
            values.put("password_hash", passwordHash);
            values.put("bio", bio);

            long newRowId = db.insert("users", null, values);
            if (newRowId != -1) {
                SharedPreferences sharedPreferences = getSharedPreferences("LoginPreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", username);
                editor.putString("passwordHash", passwordHash);
                editor.apply();
                return "User registered successfully";
            } else {
                return "Error occurred during registration";
            }
        }
    }

    public boolean checkPassword(String username, String passwordHash) {
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery("SELECT password_hash FROM users WHERE username = ?", new String[]{username})) {

            int columnIndex = cursor.getColumnIndex("password_hash");
            if (columnIndex != -1 && cursor.moveToFirst()) {
                SharedPreferences sharedPreferences = getSharedPreferences("LoginPreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", username);
                editor.putString("passwordHash", passwordHash);
                editor.apply();
                String storedPasswordHash = cursor.getString(columnIndex);
                return storedPasswordHash.equals(passwordHash);
            }
            return false;
        }
    }

    private SharedPreferences getSharedPreferences(String loginPreferences, int modePrivate) {
        return context.getSharedPreferences(loginPreferences, modePrivate);
    }
}