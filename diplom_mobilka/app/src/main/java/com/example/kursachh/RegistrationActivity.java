package com.example.kursachh;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

import Interface.IUser;
import Model.User;
import ModelRequest.UserLogin;
import RetrofitModels.RetroFit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrationActivity extends AppCompatActivity {
    private static final String TAG = "RegistrationActivity";
    private IUser userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        Log.d(TAG, "onCreate: Activity started");

        // Инициализация Retrofit и сервиса
        userService = RetroFit.getClient().create(IUser.class);
    }

    public void Autorization(View v) {
        Log.d(TAG, "Autorization: Switching to MainActivity");
        startActivity(new Intent(this, MainActivity.class));
    }

    public void RegistrationNextPage2(View v) {
        Log.d(TAG, "RegistrationNextPage2: Button clicked");
        EditText loginRegistration = findViewById(R.id.loginInputRegistration);
        EditText passwordRegistration = findViewById(R.id.passwordInputRegistration);
        EditText nameRegistration = findViewById(R.id.nameInputRegistration);
        EditText surnameRegistration = findViewById(R.id.surnameInputRegistration);

        String login = loginRegistration.getText().toString().trim();
        String password = passwordRegistration.getText().toString().trim();
        String name = nameRegistration.getText().toString().trim();
        String surname = surnameRegistration.getText().toString().trim();

        Log.d(TAG, "RegistrationNextPage2: login=" + login + ", name=" + name + ", surname=" + surname);
        if (login.isEmpty() || password.isEmpty() || name.isEmpty() || surname.isEmpty()) {
            Log.w(TAG, "RegistrationNextPage2: Empty fields detected");
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
        } else {
            checkLoginAvailability(login, password, name, surname);
        }

        // Проверяем доступность логина
        Log.d(TAG, "RegistrationNextPage2: Checking login availability");
        userService.checkLoginAvailability(login).enqueue(new Callback<Map<String, Boolean>>() {
            @Override
            public void onResponse(Call<Map<String, Boolean>> call, Response<Map<String, Boolean>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean isAvailable = response.body().get("available");
                    Log.d(TAG, "onResponse: Login availability check - " + isAvailable);
                    if (isAvailable) {
                        Log.d(TAG, "onResponse: Login available, proceeding to next step");
                        proceedToNextStep(login);
                    } else {
                        Log.w(TAG, "onResponse: Login already taken");
                        Toast.makeText(RegistrationActivity.this,
                                "Логин уже занят", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "onResponse: Failed to check login availability. Code: " + response.code());
                    Toast.makeText(RegistrationActivity.this,
                            "Ошибка проверки логина", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Boolean>> call, Throwable t) {
                Log.e(TAG, "onFailure: Network error while checking login", t);
                Toast.makeText(RegistrationActivity.this,
                        "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void proceedToNextStep(String login) {
        Log.d(TAG, "proceedToNextStep: Preparing data for next activity");
        EditText passwordRegistration = findViewById(R.id.passwordInputRegistration);
        EditText nameRegistration = findViewById(R.id.nameInputRegistration);
        EditText surnameRegistration = findViewById(R.id.surnameInputRegistration);

        String password = passwordRegistration.getText().toString().trim();
        String name = nameRegistration.getText().toString().trim();
        String surname = surnameRegistration.getText().toString().trim();

        if (password.isEmpty() || name.isEmpty() || surname.isEmpty()) {
            Log.w(TAG, "proceedToNextStep: Empty fields in proceedToNextStep");
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "proceedToNextStep: Starting RegistrationActivity2");
        Intent intent = new Intent(this, RegistrationActivity2.class);
        intent.putExtra("login", login);
        intent.putExtra("password", password);
        intent.putExtra("name", name);
        intent.putExtra("surname", surname);
        startActivity(intent);
    }

    private void checkLoginAvailability(String login, String password, String name, String surname) {
        UserLogin userLogin = new UserLogin(login, password);
        Call<User> call = userService.loginUser(userLogin);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Пользователь с таким логином уже существует
                    Toast.makeText(RegistrationActivity.this, "Логин уже занят, попробуйте другой", Toast.LENGTH_SHORT).show();
                } else {
                    // Логин доступен, переходим на следующую страницу
                    Intent intent = new Intent(RegistrationActivity.this, RegistrationActivity2.class);
                    intent.putExtra("login", login);
                    intent.putExtra("password", password);
                    intent.putExtra("name", name);
                    intent.putExtra("surname", surname);
                    startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(RegistrationActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
                Log.e("RegistrationActivity", "Network error", t);
            }
        });
    }
}
