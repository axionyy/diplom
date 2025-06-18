package com.example.kursachh.ui.diary;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kursachh.AuthManager;
import com.example.kursachh.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Interface.IUser;
import Model.EatingRecord;
import RetrofitModels.RetroFit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WaterActivity extends AppCompatActivity {
    private static final String TAG = "WaterActivity";
    private LinearLayout waterContainer;
    private List<EatingRecord> waterRecords = new ArrayList<>();
    private TextView totalAmountView;
    private WaterAdapter waterAdapter; // Добавляем адаптер

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water);

        setupViews();
        setupAdapter();
        loadWaterRecords();
    }

    private void setupAdapter() {
        waterAdapter = new WaterAdapter(waterRecords);
        waterAdapter.setOnDeleteClickListener(record -> deleteWaterRecord(record));
    }

    private void setupViews() {
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        Button addWaterButton = findViewById(R.id.addWaterButton);
        addWaterButton.setOnClickListener(v -> showAddWaterDialog());

        waterContainer = findViewById(R.id.waterContainer);
        totalAmountView = findViewById(R.id.totalAmount);

        // Заменяем LinearLayout на RecyclerView
        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(waterAdapter);
        waterContainer.addView(recyclerView);
    }

    private void showAddWaterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить прием воды");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_water, null);
        builder.setView(dialogView);

        EditText amountInput = dialogView.findViewById(R.id.amountInput);
        EditText dateInput = dialogView.findViewById(R.id.dateInput);
        EditText timeInput = dialogView.findViewById(R.id.timeInput);

        // Устанавливаем текущие дату и время
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        dateInput.setText(dateFormat.format(new Date()));
        timeInput.setText(timeFormat.format(new Date()));

        builder.setPositiveButton("Добавить", (dialog, which) -> {
            try {
                int amount = Integer.parseInt(amountInput.getText().toString());
                String date = dateInput.getText().toString();
                String time = timeInput.getText().toString();

                if (amount <= 0) {
                    Toast.makeText(this, "Количество должно быть положительным", Toast.LENGTH_SHORT).show();
                    return;
                }

                addWaterRecord(amount, date, time);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Введите корректное количество", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void addWaterRecord(int amount, String date, String time) {
        AuthManager authManager = new AuthManager(this);
        int userId = authManager.getUserId();

        // Отправляем только дату (без времени) в формате "yyyy-MM-dd"
        String dateForDb = date;

        EatingRecord record = new EatingRecord(
                userId,
                0, // food_id = 0 для воды
                dateForDb,
                "water",
                (float) amount
        );

        IUser userApi = RetroFit.getClient().create(IUser.class);
        Call<EatingRecord> call = userApi.createEatingRecord(record);

        call.enqueue(new Callback<EatingRecord>() {
            @Override
            public void onResponse(Call<EatingRecord> call, Response<EatingRecord> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(WaterActivity.this, "Прием воды сохранен", Toast.LENGTH_SHORT).show();
                    loadWaterRecords();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Ошибка сохранения: " + errorBody);
                        Toast.makeText(WaterActivity.this,
                                "Ошибка: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<EatingRecord> call, Throwable t) {
                Log.e(TAG, "Ошибка сети", t);
                Toast.makeText(WaterActivity.this,
                        "Сетевая ошибка: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadWaterRecords() {
        AuthManager authManager = new AuthManager(this);
        int userId = authManager.getUserId();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        IUser userApi = RetroFit.getClient().create(IUser.class);
        Call<List<EatingRecord>> call = userApi.getEatingRecords(userId, currentDate);

        call.enqueue(new Callback<List<EatingRecord>>() {
            @Override
            public void onResponse(Call<List<EatingRecord>> call, Response<List<EatingRecord>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    waterRecords = filterWaterRecords(response.body());
                    // Убедимся, что дата в правильном формате
                    for (EatingRecord record : waterRecords) {
                        if (record.getDate() != null && record.getDate().contains(" ")) {
                            record.setDate(record.getDate().split(" ")[0]); // Оставляем только дату
                        }
                    }
                    displayWaterRecords();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Ошибка загрузки: " + errorBody);
                        Toast.makeText(WaterActivity.this,
                                "Ошибка загрузки: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<EatingRecord>> call, Throwable t) {
                Log.e(TAG, "Ошибка сети при загрузке", t);
                Toast.makeText(WaterActivity.this,
                        "Сетевая ошибка при загрузке", Toast.LENGTH_LONG).show();
            }
        });
    }

    private List<EatingRecord> filterWaterRecords(List<EatingRecord> records) {
        List<EatingRecord> filtered = new ArrayList<>();
        for (EatingRecord record : records) {
            if ("water".equals(record.getMealType())) {
                filtered.add(record);
            }
        }
        return filtered;
    }

    private void displayWaterRecords() {
        waterContainer.removeAllViews();

        if (waterRecords.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("Нет записей о приемах воды");
            emptyView.setTextSize(16);
            emptyView.setGravity(Gravity.CENTER);
            waterContainer.addView(emptyView);
            totalAmountView.setText("Всего: 0 мл");
            return;
        }

        // Сортируем по дате (новые сверху)
        Collections.sort(waterRecords, (r1, r2) -> r2.getDate().compareTo(r1.getDate()));

        int totalAmount = 0;
        for (EatingRecord record : waterRecords) {
            totalAmount += record.getQuantity();
            addWaterRecordToView(record);
        }

        totalAmountView.setText(String.format("Всего: %d мл", totalAmount));
    }

    private void addWaterRecordToView(EatingRecord record) {
        View waterView = LayoutInflater.from(this)
                .inflate(R.layout.item_water, waterContainer, false);

        TextView typeView = waterView.findViewById(R.id.waterType);
        TextView amountView = waterView.findViewById(R.id.waterAmount);
        TextView dateView = waterView.findViewById(R.id.waterTime);
        ImageButton btnDelete = waterView.findViewById(R.id.btnDeleteWater);

        typeView.setText("Вода");
        amountView.setText(String.format("%d мл", (int)record.getQuantity()));

        // Форматируем дату для отображения
        try {
            String dateToDisplay = record.getDate(); // Уже содержит только дату
            dateView.setText(dateToDisplay);
        } catch (Exception e) {
            dateView.setText("--.--.----");
        }

        // Добавляем обработчик удаления
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(WaterActivity.this)
                    .setTitle("Удаление записи")
                    .setMessage("Вы уверены, что хотите удалить запись о приеме воды?")
                    .setPositiveButton("Удалить", (dialog, which) -> deleteWaterRecord(record))
                    .setNegativeButton("Отмена", null)
                    .show();
        });

        waterContainer.addView(waterView);
    }


    private void deleteWaterRecord(EatingRecord record) {
        AuthManager authManager = new AuthManager(this);
        int userId = authManager.getUserId();

        IUser userApi = RetroFit.getClient().create(IUser.class);
        Call<Void> call = userApi.deleteEatingRecord(record.getId());

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Удаляем запись из списка
                    waterRecords.removeIf(r -> r.getId() == record.getId());
                    // Обновляем отображение
                    displayWaterRecords();
                    Toast.makeText(WaterActivity.this, "Запись удалена", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Toast.makeText(WaterActivity.this,
                                "Ошибка удаления: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(WaterActivity.this,
                        "Сетевая ошибка: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Network error", t);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWaterRecords();
    }
}