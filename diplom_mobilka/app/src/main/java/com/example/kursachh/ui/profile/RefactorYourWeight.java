package com.example.kursachh.ui.profile;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kursachh.AuthManager;
import com.example.kursachh.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Interface.IUser;
import Model.User;
import Model.WeightRecord;
import ModelRequest.UserUpdate;
import ModelRequest.WeightRecordRequest;
import RetrofitModels.RetroFit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RefactorYourWeight extends AppCompatActivity {
    private static final String PREFS_NAME = "WeightPrefs";
    private static final String KEY_WEIGHT_RECORDS = "weight_records";
    private static final String KEY_INITIAL_WEIGHT = "initial_weight";

    private AuthManager authManager;
    private TextView initialWeightValue;
    private LinearLayout weightRecordsContainer;
    private float initialWeight;
    private float currentWeight;
    private SharedPreferences sharedPreferences;
    private List<WeightRecord> weightRecords = new ArrayList<>();
    private IUser userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refactor_weight);

        authManager = new AuthManager(this);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userService = RetroFit.getClient().create(IUser.class);

        initialWeightValue = findViewById(R.id.UserWeightValue);
        weightRecordsContainer = findViewById(R.id.weightRecordsContainer);

        Button refactorWeightButton = findViewById(R.id.refactorWeightButton);
        refactorWeightButton.setOnClickListener(v -> showWeightInputDialog());

        ImageView imageBackRefInfToProfile = findViewById(R.id.imageBackRefInf);
        imageBackRefInfToProfile.setOnClickListener(v -> finish());

        loadInitialWeight();
        loadCurrentWeight();
        loadSavedRecords();
        loadWeightHistoryFromServer();
    }

    private void loadInitialWeight() {
        initialWeight = sharedPreferences.getFloat(KEY_INITIAL_WEIGHT, 0f);
        initialWeightValue.setText(String.format(Locale.getDefault(), "%.1f кг", initialWeight));
    }

    private void loadCurrentWeight() {
        int userId = authManager.getUserId();
        if (userId <= 0) return;

        userService.getUser(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    currentWeight = user.weight;

                    // Сохраняем начальный вес при первом запуске
                    if (initialWeight == 0f) {
                        initialWeight = currentWeight;
                        sharedPreferences.edit().putFloat(KEY_INITIAL_WEIGHT, initialWeight).apply();
                        initialWeightValue.setText(String.format(Locale.getDefault(), "%.1f кг", initialWeight));
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("API", "Failed to load user weight", t);
            }
        });
    }

    private void showWeightInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_weight_input, null);
        builder.setView(dialogView);

        EditText etDate = dialogView.findViewById(R.id.etDate);
        EditText etWeight = dialogView.findViewById(R.id.etWeight);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        // Установка текущей даты по умолчанию
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        etDate.setText(sdf.format(Calendar.getInstance().getTime()));

        // Добавляем обработчик для выбора даты
        etDate.setOnClickListener(v -> showDatePicker(etDate));

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        dialog.show();

        btnSave.setOnClickListener(v -> {
            String date = etDate.getText().toString();
            String weightStr = etWeight.getText().toString();

            if (!isValidDate(date)) {
                etDate.setError("Используйте формат дд.мм.гггг");
                return;
            }

            try {
                float newWeight = Float.parseFloat(weightStr);
                int userId = authManager.getUserId();

                // 1. Сохраняем в историю на сервере
                saveWeightHistoryToServer(userId, date, newWeight);

                // 2. Сохраняем локально
                WeightRecord newRecord = new WeightRecord(date, newWeight);
                weightRecords.add(0, newRecord);
                saveRecords();

                // 3. Обновляем текущий вес пользователя
                updateUserWeight(userId, newWeight);

                // 4. Обновляем отображение
                displayAllRecords();

                dialog.dismiss();
                Toast.makeText(this, "Данные сохранены", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                etWeight.setError("Введите корректный вес");
            }
        });
    }

    private void saveWeightHistoryToServer(int userId, String date, float weight) {
        String apiDate = convertDateToApiFormat(date);
        WeightRecordRequest request = new WeightRecordRequest(apiDate, weight);

        userService.createWeightRecord(userId, request).enqueue(new Callback<WeightRecord>() {
            @Override
            public void onResponse(Call<WeightRecord> call, Response<WeightRecord> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeightRecord serverRecord = response.body();
                    // Находим и обновляем локальную запись
                    for (int i = 0; i < weightRecords.size(); i++) {
                        WeightRecord localRecord = weightRecords.get(i);
                        if (localRecord.getDate().equals(date) &&
                                localRecord.getWeight() == weight &&
                                localRecord.getId() == 0) {

                            // Создаем новую запись с правильным ID
                            WeightRecord updatedRecord = new WeightRecord(
                                    serverRecord.getId(),
                                    localRecord.getDate(),
                                    localRecord.getWeight()
                            );

                            // Заменяем старую запись
                            weightRecords.set(i, updatedRecord);
                            saveRecords();
                            displayAllRecords();
                            break;
                        }
                    }
                } else {
                    Log.e("API", "Error saving weight history: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<WeightRecord> call, Throwable t) {
                Log.e("API", "Failed to save weight history", t);
            }
        });
    }

    private void updateUserWeight(int userId, float newWeight) {
        UserUpdate updateData = new UserUpdate(newWeight);

        userService.updateUser(userId, updateData).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    currentWeight = newWeight;
                } else {
                    Log.e("API", "Error updating user weight: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("API", "Failed to update user weight", t);
            }
        });
    }

    private void loadWeightHistoryFromServer() {
        int userId = authManager.getUserId();
        if (userId <= 0) return;

        userService.getWeightHistory(userId).enqueue(new Callback<List<WeightRecord>>() {
            @Override
            public void onResponse(Call<List<WeightRecord>> call, Response<List<WeightRecord>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<WeightRecord> serverRecords = response.body();
                    Log.d("LOAD", "Получено записей с сервера: " + serverRecords.size());

                    // Обновляем локальные данные
                    weightRecords.clear();
                    for (WeightRecord record : serverRecords) {
                        weightRecords.add(new WeightRecord(
                                record.getId(), // Добавляем ID записи
                                convertApiDateToDisplay(record.getDate()),
                                record.getWeight()
                        ));
                    }
                    saveRecords();
                    displayAllRecords();

                    // Обновляем текущий вес
                    if (!weightRecords.isEmpty()) {
                        currentWeight = weightRecords.get(0).getWeight();
                    }
                } else {
                    String errorMsg = "Ошибка загрузки: " + response.code();
                    Log.e("LOAD", errorMsg);
                    Toast.makeText(RefactorYourWeight.this, errorMsg, Toast.LENGTH_SHORT).show();
                    // Показываем локальные данные при ошибке
                    loadSavedRecords();
                }
            }

            @Override
            public void onFailure(Call<List<WeightRecord>> call, Throwable t) {
                String errorMsg = "Ошибка сети: " + t.getMessage();
                Log.e("LOAD", errorMsg, t);
                Toast.makeText(RefactorYourWeight.this, errorMsg, Toast.LENGTH_SHORT).show();
                // Показываем локальные данные при ошибке
                loadSavedRecords();
            }
        });
    }

    private String convertDateToApiFormat(String displayDate) {
        try {
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return apiFormat.format(displayFormat.parse(displayDate));
        } catch (Exception e) {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Calendar.getInstance().getTime());
        }
    }

    private String convertApiDateToDisplay(String apiDate) {
        try {
            SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            return displayFormat.format(apiFormat.parse(apiDate));
        } catch (Exception e) {
            return new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    .format(Calendar.getInstance().getTime());
        }
    }

    private void loadSavedRecords() {
        String json = sharedPreferences.getString(KEY_WEIGHT_RECORDS, null);
        if (json != null) {
            Type type = new TypeToken<List<WeightRecord>>() {}.getType();
            weightRecords = new Gson().fromJson(json, type);
            displayAllRecords();
        }
    }

    private void saveRecords() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = new Gson().toJson(weightRecords);
        editor.putString(KEY_WEIGHT_RECORDS, json);
        editor.apply();
    }

    private void displayAllRecords() {
        weightRecordsContainer.removeAllViews();

        // Сортируем записи по дате (новые сверху)
        Collections.sort(weightRecords, (r1, r2) -> {
            try {
                SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                Date date1 = format.parse(r1.getDate());
                Date date2 = format.parse(r2.getDate());
                return date2.compareTo(date1);
            } catch (ParseException e) {
                return 0;
            }
        });

        for (int i = 0; i < weightRecords.size(); i++) {
            addWeightRecordView(weightRecords.get(i), i);
        }
    }

    private void addWeightRecordView(WeightRecord record, int position) {
        View recordView = LayoutInflater.from(this).inflate(R.layout.item_weight_record, null);

        TextView dateView = recordView.findViewById(R.id.dateUpdateWeight);
        TextView weightView = recordView.findViewById(R.id.updateWeightValue1);
        ImageButton btnDelete = recordView.findViewById(R.id.btnDeleteRecord);

        dateView.setText(record.getDate());
        weightView.setText(String.format(Locale.getDefault(), "%.1f кг", record.getWeight()));

        btnDelete.setOnClickListener(v -> {
            if (position == 0) {
                Toast.makeText(this, "Нельзя удалить актуальный вес", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Удаление записи")
                    .setMessage("Вы уверены, что хотите удалить запись от " + record.getDate() + "?")
                    .setPositiveButton("Удалить", (dialog, which) -> {
                        deleteWeightRecordFromServer(record.getId(), position);
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        });

        weightRecordsContainer.addView(recordView);
    }

    private void deleteWeightRecordFromServer(int recordId, int position) {
        if (recordId <= 0) {
            Toast.makeText(this, "Ошибка: неверный ID записи", Toast.LENGTH_SHORT).show();
            return;
        }

        // Добавим логгирование перед отправкой запроса
        Log.d("DELETE", "Попытка удаления записи с ID: " + recordId);

        userService.deleteWeightRecord(recordId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("DELETE", "Запись успешно удалена на сервере");

                    // Удаляем из локального списка
                    weightRecords.remove(position);
                    saveRecords();
                    displayAllRecords();

                    Toast.makeText(RefactorYourWeight.this, "Запись удалена", Toast.LENGTH_SHORT).show();
                } else {
                    String errorMsg = "Ошибка сервера: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (IOException e) {
                            Log.e("DELETE", "Ошибка чтения errorBody", e);
                        }
                    }
                    Log.e("DELETE", errorMsg);
                    Toast.makeText(RefactorYourWeight.this, "Ошибка при удалении: " + errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                String errorMsg = "Ошибка сети: " + t.getMessage();
                Log.e("DELETE", errorMsg, t);
                Toast.makeText(RefactorYourWeight.this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // При возврате на экран обновляем данные
        loadWeightHistoryFromServer();
    }

    private boolean isValidDate(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            sdf.setLenient(false);
            Date parsedDate = sdf.parse(date);

            // Проверяем что дата не в будущем
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            return !parsedDate.after(cal.getTime());
        } catch (Exception e) {
            return false;
        }
    }

    private void showDatePicker(EditText etDate) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                R.style.CustomDatePickerDialog,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.getDefault(),
                            "%02d.%02d.%04d", dayOfMonth, monthOfYear + 1, year1);
                    etDate.setText(selectedDate);
                },
                year, month, day
        );

        datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
        datePickerDialog.show();
    }
}