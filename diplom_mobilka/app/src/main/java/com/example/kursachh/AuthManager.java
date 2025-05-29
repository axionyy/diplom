package com.example.kursachh;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class AuthManager {
    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id"; // Ключ для хранения ID пользователя

    private SharedPreferences sharedPreferences;

    public AuthManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_LOGGED_IN, false);
    }

    public void setLoggedIn(boolean loggedIn) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_LOGGED_IN, loggedIn);
        editor.apply();
    }

    public void saveUserId(int userId) {
        if (userId <= 0) {
            Log.e("AuthManager", "Attempt to save invalid user ID: " + userId);
            return;
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_USER_ID, userId);
        editor.apply();
        Log.d("AuthManager", "User ID saved: " + userId);
    }

    public int getUserId() {
        int userId = sharedPreferences.getInt(KEY_USER_ID, -1);
        Log.d("AuthManager", "Retrieved user ID: " + userId);
        return userId;
    }

}
