package com.example.kursachh;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import Model.User;

public class DataManager {
    private static final String PREFS_NAME = "data_prefs";
    private static final String KEY_USER_DATA = "user_data";

    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public DataManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // Сохраняем объект User
    public void saveData(User user) {
        if (user == null) {
            sharedPreferences.edit().remove(KEY_USER_DATA).apply();
            return;
        }
        String userJson = gson.toJson(user);
        sharedPreferences.edit().putString(KEY_USER_DATA, userJson).apply();
    }

    // Получаем объект User
    public User getData() {
        String userJson = sharedPreferences.getString(KEY_USER_DATA, null);
        if (userJson != null) {
            return gson.fromJson(userJson, User.class);
        }
        return null;
    }

    // Очищаем сохраненные данные
    public void clearData() {
        sharedPreferences.edit().remove(KEY_USER_DATA).apply();
    }
}