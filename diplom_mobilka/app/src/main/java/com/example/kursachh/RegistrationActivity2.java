package com.example.kursachh;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

import Interface.IUser;
import Model.User;
import ModelRequest.UserRegister;
import RetrofitModels.RetroFit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrationActivity2 extends AppCompatActivity {

    private static final String TAG = "RegistrationActivity2";
    private String login, password, name, surname;
    private IUser userService;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration2);
        Log.d(TAG, "onCreate: Activity started");

        // Инициализация Retrofit и сервиса
        userService = RetroFit.getClient().create(IUser.class);

        Intent intent = getIntent();
        login = intent.getStringExtra("login");
        password = intent.getStringExtra("password");
        name = intent.getStringExtra("name");
        surname = intent.getStringExtra("surname");

        Log.d(TAG, "onCreate: Received data - login: " + login + ", name: " + name + ", surname: " + surname);

        ImageButton selectAgeButton = findViewById(R.id.idSelectAgeButton);
        SeekBar seekBarHeight = findViewById(R.id.seekBarHeight);
        SeekBar seekBarWeight = findViewById(R.id.seekBarWeight);
        TextView ageValueText = findViewById(R.id.idAgeValueText);
        TextView textViewValueHeight = findViewById(R.id.seekBarHeightValue);
        TextView textViewValueWeight = findViewById(R.id.seekBarWeightValue);

        final Calendar defaultCalendar = Calendar.getInstance();
        defaultCalendar.set(1950, Calendar.JANUARY, 1);

        int defaultYear = defaultCalendar.get(Calendar.YEAR);
        int defaultMonth = defaultCalendar.get(Calendar.MONTH);
        int defaultDay = defaultCalendar.get(Calendar.DAY_OF_MONTH);

        ageValueText.setText(defaultDay + "-" + (defaultMonth + 1) + "-" + defaultYear);
        Log.d(TAG, "onCreate: Set default date: " + ageValueText.getText());

        selectAgeButton.setOnClickListener(v -> {
            Log.d(TAG, "selectAgeButton: Date picker button clicked");
            final Calendar c = Calendar.getInstance();

            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Получить текущую дату для ограничения
            c.set(year, month, day);
            long today = c.getTimeInMillis();

            @SuppressLint("SetTextI18n") DatePickerDialog datePickerDialog = new DatePickerDialog
                    (RegistrationActivity2.this, R.style.CustomDatePickerDialog,
                            (view, year1, monthOfYear, dayOfMonth) ->
                                    ageValueText.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year1),
                            year, month, day);
            // Установить максимальную дату для выбора
            datePickerDialog.getDatePicker().setMaxDate(today);

            datePickerDialog.show();
        });

        seekBarHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewValueHeight.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Handle start tracking touch
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Handle stop tracking touch
            }
        });

        seekBarWeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewValueWeight.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Handle start tracking touch
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Handle stop tracking touch
            }
        });
    }

    public void RegistrationBack1(View v) {
        Log.d(TAG, "RegistrationBack1: Going back to RegistrationActivity");
        Intent intent = new Intent(RegistrationActivity2.this, RegistrationActivity.class);
        startActivity(intent);
    }

    public void ReturnAutorization(View v) {
        Log.d(TAG, "ReturnAutorization: Attempting to register user");
        TextView ageValueText = findViewById(R.id.idAgeValueText);
        TextView textViewValueHeight = findViewById(R.id.seekBarHeightValue);
        TextView textViewValueWeight = findViewById(R.id.seekBarWeightValue);
        RadioGroup radioGroupGender = findViewById(R.id.radioGroupGender);

        String age = ageValueText.getText().toString();
        String height = textViewValueHeight.getText().toString();
        String weight = textViewValueWeight.getText().toString();

        RadioButton radioFemale = findViewById(R.id.radioFemale);
        boolean gender = radioFemale.isChecked(); // Если выбран женский пол, то gender = true, иначе false
        Log.d(TAG, "ReturnAutorization: gender selected - " + (gender ? "female" : "male"));

        if (age.isEmpty() || height.isEmpty() || weight.isEmpty()) {
            Log.w(TAG, "ReturnAutorization: Empty fields detected");
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "ReturnAutorization: All fields filled, creating UserRegister object");
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

            registerUser(userRegister);
        }
    }

    private void registerUser(UserRegister userRegister) {
        Log.d(TAG, "registerUser: Attempting to register user");
        Call<User> call = userService.registerUser(userRegister);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse: Registration successful");
                    Toast.makeText(RegistrationActivity2.this, "Регистрация завершена", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegistrationActivity2.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Log.e(TAG, "onResponse: Registration failed. Code: " + response.code());
                    Toast.makeText(RegistrationActivity2.this, "Регистрация неуспешная", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "onFailure: Network error during registration", t);
                Toast.makeText(RegistrationActivity2.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
