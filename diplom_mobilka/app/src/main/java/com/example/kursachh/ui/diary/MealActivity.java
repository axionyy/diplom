package com.example.kursachh.ui.diary;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Interface.IUser;
import Model.EatingRecord;
import Model.FoodItem;
import RetrofitModels.RetroFit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MealActivity extends AppCompatActivity {
    private static final String TAG = "MealActivity";
    public static final String MEAL_TYPE = "meal_type";

    private RecyclerView mealRecyclerView;
    private MealAdapter mealAdapter;
    private List<EatingRecord> mealRecords = new ArrayList<>();
    private TextView totalCaloriesView;
    private String currentMealType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal);

        currentMealType = getIntent().getStringExtra(MEAL_TYPE);
        if (currentMealType == null) {
            finish();
            return;
        }

        setupViews();
        setupRecyclerView();
        loadEatingRecords();
    }

    private void setupRecyclerView() {
        mealAdapter = new MealAdapter(mealRecords);
        mealAdapter.setOnDeleteClickListener(this::deleteMealRecord);
        mealRecyclerView.setAdapter(mealAdapter);
    }

    private void setupViews() {
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        Button addCustomFoodButton = findViewById(R.id.addCustomFoodButton);
        addCustomFoodButton.setOnClickListener(v -> showAddCustomFoodDialog());

        Button addFoodButton = findViewById(R.id.addFoodButton);
        addFoodButton.setOnClickListener(v -> showAddFoodDialog());

        totalCaloriesView = findViewById(R.id.totalCalories);
        mealRecyclerView = findViewById(R.id.foodList);
        mealRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        setTitle(currentMealType);
    }

    private void showAddCustomFoodDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить новый продукт");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_custom_food, null);
        builder.setView(dialogView);

        EditText nameInput = dialogView.findViewById(R.id.nameInput);
        EditText caloriesInput = dialogView.findViewById(R.id.caloriesInput);
        EditText proteinsInput = dialogView.findViewById(R.id.proteinsInput);
        EditText fatsInput = dialogView.findViewById(R.id.fatsInput);
        EditText carbsInput = dialogView.findViewById(R.id.carbsInput);

        builder.setPositiveButton("Добавить", (dialog, which) -> {
            try {
                String name = nameInput.getText().toString();
                float calories = Float.parseFloat(caloriesInput.getText().toString());
                float proteins = Float.parseFloat(proteinsInput.getText().toString());
                float fats = Float.parseFloat(fatsInput.getText().toString());
                float carbs = Float.parseFloat(carbsInput.getText().toString());

                if (name.isEmpty()) {
                    Toast.makeText(this, "Введите название", Toast.LENGTH_SHORT).show();
                    return;
                }

                addCustomFood(name, calories, proteins, fats, carbs);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Проверьте правильность ввода чисел", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void showAddFoodDialog() {
        // Сначала нужно получить список продуктов из API
        IUser userApi = RetroFit.getClient().create(IUser.class);
        Call<List<FoodItem>> call = userApi.searchFoodItems(""); // Пустой запрос для получения всех продуктов

        call.enqueue(new Callback<List<FoodItem>>() {
            @Override
            public void onResponse(Call<List<FoodItem>> call, Response<List<FoodItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<FoodItem> foodItems = response.body();
                    showFoodSelectionDialog(foodItems);
                } else {
                    Toast.makeText(MealActivity.this, "Ошибка загрузки продуктов", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<FoodItem>> call, Throwable t) {
                Log.e(TAG, "Ошибка загрузки продуктов", t);
                Toast.makeText(MealActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showFoodSelectionDialog(List<FoodItem> foodItems) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите продукт");

        // Создаем массив названий продуктов
        String[] foodNames = new String[foodItems.size()];
        for (int i = 0; i < foodItems.size(); i++) {
            foodNames[i] = foodItems.get(i).getNameFood();
        }

        builder.setItems(foodNames, (dialog, which) -> {
            // При выборе продукта открываем диалог добавления приема пищи
            showAddFoodConsumptionDialog(foodItems.get(which).getId());
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void showAddFoodConsumptionDialog(int foodId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить прием пищи");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_food, null);
        builder.setView(dialogView);

        EditText quantityInput = dialogView.findViewById(R.id.quantityInput);
        EditText dateInput = dialogView.findViewById(R.id.dateInput);
        EditText timeInput = dialogView.findViewById(R.id.timeInput);

        // Устанавливаем текущие дату и время
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        dateInput.setText(dateFormat.format(new Date()));
        timeInput.setText(timeFormat.format(new Date()));

        builder.setPositiveButton("Добавить", (dialog, which) -> {
            try {
                float quantity = Float.parseFloat(quantityInput.getText().toString());
                String date = dateInput.getText().toString();
                String time = timeInput.getText().toString();
                String mealType = getIntent().getStringExtra(MEAL_TYPE);

                if (quantity <= 0) {
                    Toast.makeText(this, "Количество должно быть положительным", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Проверяем формат времени
                if (!time.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                    Toast.makeText(this, "Введите время в формате ЧЧ:ММ", Toast.LENGTH_SHORT).show();
                    return;
                }

                addEatingRecord(foodId, quantity, date, time, mealType);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Введите корректное количество", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void addCustomFood(String name, float callories, float proteins, float fats, float carbs) {
        // Проверка на валидность данных
        if (name.isEmpty()) {
            Toast.makeText(this, "Введите название продукта", Toast.LENGTH_SHORT).show();
            return;
        }

        if (callories <= 0 || proteins < 0 || fats < 0 || carbs < 0) {
            Toast.makeText(this, "Значения должны быть положительными", Toast.LENGTH_SHORT).show();
            return;
        }

        // Создаем объект FoodItem
        FoodItem foodItem = new FoodItem();
        foodItem.setNameFood(name);
        foodItem.setCallories(callories);
        foodItem.setProteins(proteins);
        foodItem.setFats(fats);
        foodItem.setCarbohydrates(carbs);

        IUser userApi = RetroFit.getClient().create(IUser.class);
        Call<FoodItem> call = userApi.createFoodItem(foodItem);

        call.enqueue(new Callback<FoodItem>() {
            @Override
            public void onResponse(Call<FoodItem> call, Response<FoodItem> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(MealActivity.this, "Продукт успешно добавлен", Toast.LENGTH_SHORT).show();
                    // Показываем диалог для добавления этого продукта в прием пищи
                    showAddFoodDialog(response.body().getId());
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Ошибка сервера: " + errorBody);
                        Toast.makeText(MealActivity.this, "Ошибка сервера: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Log.e(TAG, "Ошибка чтения errorBody", e);
                        Toast.makeText(MealActivity.this, "Ошибка: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<FoodItem> call, Throwable t) {
                Log.e(TAG, "Ошибка сети", t);
                Toast.makeText(MealActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddFoodDialog(int foodId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить прием пищи");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_food, null);
        builder.setView(dialogView);

        EditText quantityInput = dialogView.findViewById(R.id.quantityInput);
        EditText dateInput = dialogView.findViewById(R.id.dateInput);
        EditText timeInput = dialogView.findViewById(R.id.timeInput);

        // Устанавливаем текущие дату и время
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        dateInput.setText(dateFormat.format(new Date()));
        timeInput.setText(timeFormat.format(new Date()));

        builder.setPositiveButton("Добавить", (dialog, which) -> {
            try {
                float quantity = Float.parseFloat(quantityInput.getText().toString());
                String date = dateInput.getText().toString();
                String time = timeInput.getText().toString();
                String mealType = getIntent().getStringExtra(MEAL_TYPE);

                if (quantity <= 0) {
                    Toast.makeText(this, "Количество должно быть положительным", Toast.LENGTH_SHORT).show();
                    return;
                }

                addEatingRecord(foodId, quantity, date, time, mealType);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Введите корректное количество", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void addEatingRecord(int foodId, float quantity, String date, String time, String mealType) {
        AuthManager authManager = new AuthManager(this);
        int userId = authManager.getUserId();

        // Форматируем дату и время в правильный формат
        String dateTime = date + " " + time + ":00"; // Формат: "YYYY-MM-DD HH:MM:00"

        EatingRecord record = new EatingRecord(
                userId,
                foodId,
                dateTime,
                mealType.toLowerCase(),
                quantity
        );

        // Устанавливаем нулевые значения для калорий (они будут рассчитаны на сервере)
        record.setCallories(0);
        record.setProteins(0);
        record.setFats(0);
        record.setCarbohydrates(0);

        IUser userApi = RetroFit.getClient().create(IUser.class);
        Call<EatingRecord> call = userApi.createEatingRecord(record);

        call.enqueue(new Callback<EatingRecord>() {
            @Override
            public void onResponse(Call<EatingRecord> call, Response<EatingRecord> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MealActivity.this, "Приём пищи сохранён", Toast.LENGTH_SHORT).show();
                    loadEatingRecords();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error (" + response.code() + ")";
                        Log.e(TAG, "Ошибка сервера: " + errorBody);
                        Toast.makeText(MealActivity.this,
                                "Ошибка: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<EatingRecord> call, Throwable t) {
                Log.e(TAG, "Ошибка сети: ", t);
                Toast.makeText(MealActivity.this,
                        "Сетевая ошибка: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadEatingRecords() {
        AuthManager authManager = new AuthManager(this);
        int userId = authManager.getUserId();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        IUser userApi = RetroFit.getClient().create(IUser.class);
        Call<List<EatingRecord>> call = userApi.getEatingRecords(userId, currentDate);

        call.enqueue(new Callback<List<EatingRecord>>() {
            @Override
            public void onResponse(Call<List<EatingRecord>> call, Response<List<EatingRecord>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mealRecords = filterRecordsByMealType(response.body(), currentMealType.toLowerCase());
                    calculateAndDisplayTotals();
                    mealAdapter.updateData(mealRecords);
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Toast.makeText(MealActivity.this, "Ошибка загрузки: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<EatingRecord>> call, Throwable t) {
                Log.e(TAG, "Ошибка загрузки", t);
                Toast.makeText(MealActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<EatingRecord> filterRecordsByMealType(List<EatingRecord> records, String mealType) {
        List<EatingRecord> filtered = new ArrayList<>();
        for (EatingRecord record : records) {
            if (mealType.equalsIgnoreCase(record.getMealType())) {
                filtered.add(record);
            }
        }
        return filtered;
    }

    private void calculateAndDisplayTotals() {
        float totalCalories = 0;
        float totalProteins = 0;
        float totalFats = 0;
        float totalCarbs = 0;

        for (EatingRecord record : mealRecords) {
            totalCalories += record.getCallories() != 0 ? record.getCallories() : 0;
            totalProteins += record.getProteins() != 0 ? record.getProteins() : 0;
            totalFats += record.getFats() != 0 ? record.getFats() : 0;
            totalCarbs += record.getCarbohydrates() != 0 ? record.getCarbohydrates() : 0;
        }

        String summary = String.format(Locale.getDefault(),
                "Всего: %.1f ккал\nБ:%.1fг Ж:%.1fг У:%.1fг",
                totalCalories, totalProteins, totalFats, totalCarbs);

        totalCaloriesView.setText(summary);
    }

    private void deleteMealRecord(EatingRecord record) {
        AuthManager authManager = new AuthManager(this);
        int userId = authManager.getUserId();

        IUser userApi = RetroFit.getClient().create(IUser.class);
        Call<Void> call = userApi.deleteEatingRecord(record.getId());

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    mealRecords.remove(record);
                    mealAdapter.updateData(mealRecords);
                    calculateAndDisplayTotals();
                    Toast.makeText(MealActivity.this, "Запись удалена", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Toast.makeText(MealActivity.this,
                                "Ошибка удаления: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MealActivity.this,
                        "Сетевая ошибка: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Network error", t);
            }
        });
    }

    private void updateFoodList(List<EatingRecord> records) {
        // Обновляем RecyclerView
        RecyclerView foodList = findViewById(R.id.foodList);
        // foodList.getAdapter().submitList(records);
    }
}