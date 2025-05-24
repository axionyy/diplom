package com.example.kursachh;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import Interface.IUser;
import Model.User;
import ModelRequest.UserRegister;
import RetrofitModels.RetroFit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.util.ArrayList;
import java.util.List;

public class RegistrationActivity3 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration3);
    }

    public void RegistrationBack2(View v) {
        Intent intent = new Intent(RegistrationActivity3.this, RegistrationActivity2.class);
        startActivity(intent);
    }

    public void ReturnAutorization(View v) {
        Log.d("RegistrationActivity3", "ReturnAutorization called");

        Intent intent = getIntent();
        String login = intent.getStringExtra("login");
        String password = intent.getStringExtra("password");
        String name = intent.getStringExtra("name");
        String surname = intent.getStringExtra("surname");
        String age = intent.getStringExtra("age");
        String height = intent.getStringExtra("height");
        String weight = intent.getStringExtra("weight");
        String lifestyle = intent.getStringExtra("lifestyle");
        boolean gender = intent.getBooleanExtra("gender", false);

        List<Integer> allergies = new ArrayList<>();
        List<Integer> preferences = new ArrayList<>();

        // Получение данных из CheckBox для аллергий
        if (((CheckBox) findViewById(R.id.allergyCheck1)).isChecked()) allergies.add(1);
        if (((CheckBox) findViewById(R.id.allergyCheck2)).isChecked()) allergies.add(2);
        if (((CheckBox) findViewById(R.id.allergyCheck3)).isChecked()) allergies.add(3);
        if (((CheckBox) findViewById(R.id.allergyCheck4)).isChecked()) allergies.add(4);
        if (((CheckBox) findViewById(R.id.allergyCheck5)).isChecked()) allergies.add(5);
        if (((CheckBox) findViewById(R.id.allergyCheck6)).isChecked()) allergies.add(6);
        if (((CheckBox) findViewById(R.id.allergyCheck7)).isChecked()) allergies.add(7);

        // Получение данных из CheckBox для предпочтений
        if (((CheckBox) findViewById(R.id.preferenceCheck1)).isChecked()) preferences.add(1);
        if (((CheckBox) findViewById(R.id.preferenceCheck2)).isChecked()) preferences.add(2);
        if (((CheckBox) findViewById(R.id.preferenceCheck3)).isChecked()) preferences.add(3);
        if (((CheckBox) findViewById(R.id.preferenceCheck4)).isChecked()) preferences.add(4);
        if (((CheckBox) findViewById(R.id.preferenceCheck5)).isChecked()) preferences.add(5);

        UserRegister userRegister = new UserRegister(
                login,
                password,
                name,
                surname,
                Float.parseFloat(height),
                Float.parseFloat(weight),
                gender,
                age
        );

        Log.d("RegistrationActivity3", "Registering user with data: " + userRegister.toString());

        Retrofit retrofit = RetroFit.getClient();
        IUser userService = retrofit.create(IUser.class);
        Call<User> call = userService.registerUser(userRegister);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Log.d("RegistrationActivity3", "Registration successful");
                    Toast.makeText(RegistrationActivity3.this, "Вы зарегистрированы!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegistrationActivity3.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Log.e("RegistrationActivity3", "Registration failed with response code: " + response.code());
                    if (response.code() == 400) {
                        Toast.makeText(RegistrationActivity3.this, "Логин уже существует", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RegistrationActivity3.this, "Регистрация не удалась", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("RegistrationActivity3", "Network error: " + t.getMessage());
                Toast.makeText(RegistrationActivity3.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getLifestyleId(String lifestyle) {
        switch (lifestyle) {
            case "Активный":
                return 1;
            case "Сидячий":
                return 2;
            case "Лежачий":
                return 3;
            case "Смешанный":
                return 4;
            default:
                return 1;
        }
    }
}
