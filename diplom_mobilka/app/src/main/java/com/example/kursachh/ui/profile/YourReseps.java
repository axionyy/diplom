package com.example.kursachh.ui.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.kursachh.AuthManager;
import com.example.kursachh.R;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Interface.IUser;
import Model.Recipe;
import ModelRequest.RecipeCreateRequest;
import RetrofitModels.RetroFit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class YourReseps extends AppCompatActivity {
    private AuthManager authManager;
    private IUser userService;
    private LinearLayout recipesContainer;
    private List<Recipe> recipesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_reseps);

        authManager = new AuthManager(this);
        userService = RetroFit.getClient().create(IUser.class);
        recipesContainer = findViewById(R.id.recipesContainer);

        loadUserRecipes();

        ImageView imageBackResepsToProfile = findViewById(R.id.imageBackYourReseps);
        imageBackResepsToProfile.setOnClickListener(v -> finish());

        Button addRecipeButton = findViewById(R.id.addRecipeButton);
        addRecipeButton.setOnClickListener(v -> showAddRecipeDialog());
    }

    private void loadUserRecipes() {
        int userId = authManager.getUserId();
        if (userId <= 0) return;

        userService.getUserRecipes(userId).enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    recipesList = response.body();

                    // Сортируем по дате обновления (новые сверху)
                    Collections.sort(recipesList, (r1, r2) -> {
                        try {
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                            // Добавляем проверки на null
                            String date1Str = r1.dateCreate != null ? r1.dateCreate : "1970-01-01";
                            String date2Str = r2.dateCreate != null ? r2.dateCreate : "1970-01-01";

                            Date date1 = format.parse(date1Str);
                            Date date2 = format.parse(date2Str);
                            return date2.compareTo(date1); // Убывающая сортировка
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return 0;
                        }
                    });

                    displayRecipes();
                }
            }

            @Override
            public void onFailure(Call<List<Recipe>> call, Throwable t) {
                Toast.makeText(YourReseps.this,
                        "Ошибка загрузки рецептов: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayRecipes() {
        recipesContainer.removeAllViews();

        if (recipesList.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("Нет сохраненных рецептов");
            emptyView.setTextSize(16);
            emptyView.setGravity(Gravity.CENTER);
            recipesContainer.addView(emptyView);
            return;
        }

        for (int i = 0; i < recipesList.size(); i++) {
            Recipe recipe = recipesList.get(i);
            View recipeView = LayoutInflater.from(this)
                    .inflate(R.layout.item_recipe, recipesContainer, false);

            TextView nameView = recipeView.findViewById(R.id.recipeName);
            TextView caloriesView = recipeView.findViewById(R.id.recipeCalories);
            ImageView photoView = recipeView.findViewById(R.id.recipePhoto);
            TextView dateView = recipeView.findViewById(R.id.recipeDate);

            nameView.setText(recipe.name);
            caloriesView.setText(String.format(Locale.getDefault(), "%.1f ккал", recipe.calories));

            // Форматирование даты в ДД.ММ.ГГГГ
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

                Date date = inputFormat.parse(recipe.dateCreate);
                dateView.setText(outputFormat.format(date));
            } catch (ParseException e) {
                dateView.setText(recipe.dateCreate);
            }

            if (recipe.photo != null && !recipe.photo.isEmpty()) {
                Glide.with(this)
                        .load(recipe.photo)
                        .into(photoView);
            } else {
                photoView.setImageResource(R.drawable.placeholder_recipe);
            }

            setupRecipeDeleteButton(recipeView, recipe, i);
            recipesContainer.addView(recipeView);

            recipeView.setOnClickListener(v -> {
                Intent intent = new Intent(YourReseps.this, RecipeDetailActivity.class);
                intent.putExtra("recipe_id", recipe.id);
                intent.putExtra("recipe_name", recipe.name);
                intent.putExtra("recipe_photo", recipe.photo);
                intent.putExtra("recipe_calories", recipe.calories);
                intent.putExtra("recipe_components", recipe.components);
                intent.putExtra("recipe_steps", recipe.steps);
                intent.putExtra("recipe_proteins", recipe.proteins);
                intent.putExtra("recipe_fats", recipe.fats);
                intent.putExtra("recipe_carbs", recipe.carbs);
                intent.putExtra("recipe_date", recipe.dateCreate);
                startActivity(intent);
            });
        }
    }

    private void showAddRecipeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_recipe, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.etRecipeName);
        EditText etCalories = dialogView.findViewById(R.id.etRecipeCalories);
        EditText etPhoto = dialogView.findViewById(R.id.etRecipePhoto);
        EditText etComponents = dialogView.findViewById(R.id.etRecipeComponents);
        EditText etSteps = dialogView.findViewById(R.id.etRecipeSteps);
        EditText etProteins = dialogView.findViewById(R.id.etRecipeProteins);
        EditText etFats = dialogView.findViewById(R.id.etRecipeFats);
        EditText etCarbs = dialogView.findViewById(R.id.etRecipeCarbs);
        Button btnSave = dialogView.findViewById(R.id.btnSaveRecipe);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnSave.setOnClickListener(v -> {
            // Обязательные поля
            String name = etName.getText().toString().trim();
            String caloriesStr = etCalories.getText().toString().trim();
            String components = etComponents.getText().toString().trim();
            String steps = etSteps.getText().toString().trim();

            if (name.isEmpty() || caloriesStr.isEmpty() || components.isEmpty() || steps.isEmpty()) {
                Toast.makeText(this, "Заполните обязательные поля", Toast.LENGTH_SHORT).show();
                return;
            }

            // Необязательные поля
            String photo = etPhoto.getText().toString().trim();
            String proteinsStr = etProteins.getText().toString().trim();
            String fatsStr = etFats.getText().toString().trim();
            String carbsStr = etCarbs.getText().toString().trim();

            try {
                float calories = Float.parseFloat(caloriesStr);
                float proteins = proteinsStr.isEmpty() ? 0 : Float.parseFloat(proteinsStr);
                float fats = fatsStr.isEmpty() ? 0 : Float.parseFloat(fatsStr);
                float carbs = carbsStr.isEmpty() ? 0 : Float.parseFloat(carbsStr);

                int userId = authManager.getUserId();
                RecipeCreateRequest request = new RecipeCreateRequest(
                        name,
                        calories,
                        photo.isEmpty() ? null : photo,
                        components,
                        steps,
                        proteins,
                        fats,
                        carbs
                );

                userService.createRecipe(userId, request).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            // Обновляем достижение за добавление рецепта
                            AchievementsActivity.updateAchievementProgress(YourReseps.this, "recipe_master", 1);

                            Toast.makeText(YourReseps.this, "Рецепт успешно сохранен", Toast.LENGTH_SHORT).show();
                            loadUserRecipes();
                            dialog.dismiss();
                        } else {
                            try {
                                String errorBody = response.errorBody().string();
                                Log.e("API_ERROR", "Ошибка сохранения: " + errorBody);
                                Toast.makeText(YourReseps.this,
                                        "Ошибка сохранения: " + errorBody, Toast.LENGTH_LONG).show();
                            } catch (IOException e) {
                                Toast.makeText(YourReseps.this,
                                        "Ошибка сохранения рецепта", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("API_FAILURE", "Ошибка сети: ", t);
                        Toast.makeText(YourReseps.this,
                                "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Введите корректные числовые значения", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserRecipes(); // Обновляем список при каждом открытии экрана
    }

    private void setupRecipeDeleteButton(View recipeView, Recipe recipe, int position) {
        ImageButton btnDelete = recipeView.findViewById(R.id.btnDeleteRecipe);
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(YourReseps.this)
                    .setTitle("Удаление рецепта")
                    .setMessage("Вы уверены, что хотите удалить рецепт \"" + recipe.name + "\"?")
                    .setPositiveButton("Удалить", (dialog, which) -> deleteRecipe(recipe.id, position))
                    .setNegativeButton("Отмена", null)
                    .show();
        });
    }

    private void deleteRecipe(int recipeId, int position) {
        userService.deleteRecipe(recipeId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Обновляем счетчик рецептов
                    SharedPreferences prefs = getSharedPreferences(
                            "user_" + authManager.getUserId() + "_achievements", MODE_PRIVATE);

                    int currentCount = prefs.getInt("recipe_master", 0);
                    prefs.edit().putInt("recipe_master", Math.max(0, currentCount - 1)).apply();

                    recipesList.remove(position);
                    displayRecipes();
                    Toast.makeText(YourReseps.this, "Рецепт удален", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(YourReseps.this,
                        "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}