package com.example.kursachh;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);

        authManager = new AuthManager(this);
        dataManager = new DataManager(this);

        if (authManager.isLoggedIn()) {
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
                        if (response.isSuccessful()){
                            User user = response.body();
                            authManager.setLoggedIn(true);
                            dataManager.saveData(user.toString()); // Сохраняем данные пользователя
                            Log.d("Authorization", "Login successful");
                            Toast.makeText(MainActivity.this, "Вы вошли в аккаунт!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, NavigationRun.class);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            Log.e("Authorization", "Login failed with response code: " + response.code());
                            Toast.makeText(MainActivity.this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Log.e("Authorization", "Network error: " + t.getMessage());
                        Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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