package com.example.kursachh;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kursachh.ui.profile.AchievementsActivity;

import Interface.IUser;
import Model.User;
import ModelRequest.UserLogin;
import RetrofitModels.RetroFit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private AuthManager authManager;
    private DataManager dataManager;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);

        // Проверка разрешений для уведомлений (для Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                // Запрашиваем разрешение, но продолжаем работу даже если его не дали
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        authManager = new AuthManager(this);
        dataManager = new DataManager(this);
        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);

        if (authManager.isLoggedIn()) {
            int userId = authManager.getUserId();
            SharedPreferences userPrefs = getSharedPreferences("user_" + userId, MODE_PRIVATE);

            // Проверка ежедневного входа
            if (!userPrefs.getBoolean("logged_today", false)) {
                AchievementsActivity.updateAchievementProgress(this, "weekly_streak", 1);
                userPrefs.edit()
                        .putBoolean("logged_today", true)
                        .putLong("last_login", System.currentTimeMillis())
                        .apply();
            }

            Intent intent = new Intent(this, NavigationRun.class);
            startActivity(intent);
            finish();
        }

        EditText loginEditText = findViewById(R.id.loginInputAutorization);
        EditText passwordEditText = findViewById(R.id.passwordInputAutorization);
        Button buttonSigIn = findViewById(R.id.enterProfileAutorization);

        buttonSigIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String login = loginEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (login.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Введите логин и пароль", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Создание UserLogin
                UserLogin userLogin = new UserLogin(login, password);
                // Создание RetroFit
                Retrofit retrofit = RetroFit.getClient();
                IUser userService = retrofit.create(IUser.class);
                Call<User> call = userService.loginUser(userLogin);
                call.enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            User user = response.body();
                            Log.d("Authorization", "User data received - ID: " + user.id + ", Name: " + user.name);

                            // Сохраняем данные
                            authManager.setLoggedIn(true);
                            authManager.saveUserId(user.id);
                            dataManager.saveData(user);

                            Log.d("Authorization", "Saved user ID: " + authManager.getUserId()); // Проверка сохранения

                            // Проверка первого входа
                            SharedPreferences userPrefs = getSharedPreferences("user_" + user.id, MODE_PRIVATE);
                            if (!userPrefs.getBoolean("first_entry", false)) {
                                AchievementsActivity.updateAchievementProgress(MainActivity.this, "first_entry", 1);
                                userPrefs.edit().putBoolean("first_entry", true).apply();
                            }

                            // Переход на главный экран
                            Intent intent = new Intent(MainActivity.this, NavigationRun.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.e("Authorization", "Login failed: " + response.code());
                            Toast.makeText(MainActivity.this, "Ошибка авторизации", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Log.e("Authorization", "Network error", t);
                        Toast.makeText(MainActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void Registration(View v) {
        Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
        startActivity(intent);
    }

}