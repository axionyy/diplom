package com.example.kursachh.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kursachh.AuthManager;
import com.example.kursachh.R;
import com.google.gson.Gson;

import java.io.IOException;

import Interface.IUser;
import Model.User;
import ModelRequest.UserUpdate;
import ModelRequest.VerifyPasswordRequest;
import ModelRequest.VerifyPasswordResponse;
import RetrofitModels.RetroFit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RefactorYourProfile extends AppCompatActivity {

    private EditText loginEdit, nameEdit, birthdayEdit, heightEdit, passwordRefNow, passwordRefNext;
    private AuthManager authManager;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refator_profile);

        authManager = new AuthManager(this);
        initViews();

        // Проверяем, есть ли данные в Intent
        if (getIntent().hasExtra("user_data")) {
            currentUser = (User) getIntent().getSerializableExtra("user_data");
            updateUIWithUserData();
        }

        loadUserData();

        Button applyButton = findViewById(R.id.applyRefactorsButton);
        applyButton.setOnClickListener(v -> updateUserData());
    }

    private void initViews() {
        loginEdit = findViewById(R.id.refactorLoginEdit);
        nameEdit = findViewById(R.id.refactorNameEdit);
        birthdayEdit = findViewById(R.id.refactorBirthdayEdit);
        heightEdit = findViewById(R.id.refactorHeightEdit);
        passwordRefNow = findViewById(R.id.passwordRefactorNow);
        passwordRefNext = findViewById(R.id.passwordRefactorNext);
    }

    private void loadUserData() {
        int userId = authManager.getUserId();
        if (userId <= 0) {
            Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        RetroFit.getClient().create(IUser.class).getUser(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    updateUIWithUserData();
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("LoadUserError", "Error: " + errorBody);
                        Toast.makeText(RefactorYourProfile.this,
                                "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("LoadUserError", "Network error", t);
                Toast.makeText(RefactorYourProfile.this,
                        "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUIWithUserData() {
        if (currentUser == null) return;

        runOnUiThread(() -> {
            loginEdit.setText(currentUser.login != null ? currentUser.login : "");
            nameEdit.setText(currentUser.name != null ? currentUser.name : "");

            // Форматирование даты рождения
            if (currentUser.birthday != null) {
                try {
                    String[] dateParts = currentUser.birthday.split("-");
                    if (dateParts.length == 3) {
                        birthdayEdit.setText(String.format("%s.%s.%s",
                                dateParts[2], dateParts[1], dateParts[0]));
                    }
                } catch (Exception e) {
                    birthdayEdit.setText(currentUser.birthday);
                }
            } else {
                birthdayEdit.setText("");
            }

            // Отображение роста - исправленная версия
            if (currentUser.height != null) {
                heightEdit.setText(String.valueOf(currentUser.height)); // Убрано .intValue()
            } else {
                heightEdit.setText("");
            }
        });
    }

    private boolean validateFields() {
        // Проверка формата даты
        String birthday = birthdayEdit.getText().toString().trim();
        if (!birthday.isEmpty() && !birthday.matches("^\\d{2}\\.\\d{2}\\.\\d{4}$")) {
            Toast.makeText(this, "Дата должна быть в формате ДД.ММ.ГГГГ", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Проверка роста
        String heightStr = heightEdit.getText().toString().trim();
        if (!heightStr.isEmpty()) {
            try {
                Float.parseFloat(heightStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Рост должен быть числом", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        // Проверка паролей
        String newPassword = passwordRefNext.getText().toString().trim();
        String currentPassword = passwordRefNow.getText().toString().trim();

        if (!newPassword.isEmpty() && currentPassword.isEmpty()) {
            Toast.makeText(this, "Введите текущий пароль", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (currentPassword.isEmpty() && !newPassword.isEmpty()) {
            Toast.makeText(this, "Введите новый пароль", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!newPassword.isEmpty() && newPassword.length() < 6) {
            Toast.makeText(this, "Новый пароль должен быть не менее 6 символов", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void updateUserData() {
        if (!validateFields()) {
            return;
        }

        String newPassword = passwordRefNext.getText().toString().trim();
        String currentPassword = passwordRefNow.getText().toString().trim();

        // Если меняется пароль, проверяем текущий
        if (!newPassword.isEmpty()) {
            verifyCurrentPassword(currentPassword, newPassword);
        } else {
            updateProfileData(null);
        }
    }

    private void verifyCurrentPassword(String currentPassword, String newPassword) {
        VerifyPasswordRequest request = new VerifyPasswordRequest(
                authManager.getUserId(),
                currentPassword
        );

        RetroFit.getClient().create(IUser.class)
                .verifyPassword(request)
                .enqueue(new Callback<VerifyPasswordResponse>() {
                    @Override
                    public void onResponse(Call<VerifyPasswordResponse> call, Response<VerifyPasswordResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().isValid()) {
                                updateProfileData(newPassword);
                            } else {
                                Toast.makeText(RefactorYourProfile.this,
                                        "Неверный текущий пароль", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            try {
                                String errorBody = response.errorBody().string();
                                Log.e("VerifyPassword", "Server error: " + errorBody);
                                Toast.makeText(RefactorYourProfile.this,
                                        "Ошибка сервера при проверке пароля", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<VerifyPasswordResponse> call, Throwable t) {
                        Log.e("VerifyPassword", "Network error", t);
                        Toast.makeText(RefactorYourProfile.this,
                                "Ошибка сети при проверке пароля", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateProfileData(String newPassword) {
        try {
            // Преобразование даты в формат БД
            String birthdayDbFormat = null;
            String birthdayDisplay = birthdayEdit.getText().toString().trim();
            if (!birthdayDisplay.isEmpty()) {
                String[] dateParts = birthdayDisplay.split("\\.");
                if (dateParts.length == 3) {
                    birthdayDbFormat = String.format("%s-%s-%s",
                            dateParts[2], dateParts[1], dateParts[0]);
                }
            }

            // Получение значения роста
            Float heightValue = null;
            String heightStr = heightEdit.getText().toString().trim();
            if (!heightStr.isEmpty()) {
                try {
                    heightValue = Float.parseFloat(heightStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Некорректное значение роста", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Создание запроса на обновление
            UserUpdate updateRequest = new UserUpdate(
                    loginEdit.getText().toString().trim(),
                    nameEdit.getText().toString().trim(),
                    currentUser.surname,
                    heightValue,
                    birthdayDbFormat,
                    newPassword
            );

            // Отправка запроса
            sendUpdateRequest(updateRequest);

        } catch (Exception e) {
            Log.e("UpdateProfile", "Error", e);
            Toast.makeText(this, "Ошибка обновления данных", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendUpdateRequest(UserUpdate updateRequest) {
        Log.d("UpdateRequest", "Sending: " + new Gson().toJson(updateRequest));

        RetroFit.getClient().create(IUser.class)
                .updateUser(authManager.getUserId(), updateRequest)
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            currentUser = response.body();
                            Toast.makeText(RefactorYourProfile.this,
                                    "Данные успешно обновлены", Toast.LENGTH_SHORT).show();
                            updateUIWithUserData();
                        } else {
                            try {
                                String errorBody = response.errorBody().string();
                                Log.e("UpdateError", "Server error: " + errorBody);
                                Toast.makeText(RefactorYourProfile.this,
                                        "Ошибка сервера: " + errorBody, Toast.LENGTH_LONG).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Log.e("UpdateError", "Network error", t);
                        Toast.makeText(RefactorYourProfile.this,
                                "Ошибка сети: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void RefBack1(View v) {
        // Создаем Intent для возврата данных
        Intent returnIntent = new Intent();
        returnIntent.putExtra("updated_user", currentUser); // Передаем обновленного пользователя
        setResult(RESULT_OK, returnIntent);
        finish(); // Закрываем активность
    }
}