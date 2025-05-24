package com.example.kursachh;

import android.content.Context;
import android.content.SharedPreferences;

public class DataManager {
    private static final String PREFS_NAME = "data_prefs";
    private static final String KEY_DATA = "data";

    private SharedPreferences sharedPreferences;

    public DataManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveData(String data) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_DATA, data);
        editor.apply();
    }

    public String getData() {
        return sharedPreferences.getString(KEY_DATA, null);
    }
}
