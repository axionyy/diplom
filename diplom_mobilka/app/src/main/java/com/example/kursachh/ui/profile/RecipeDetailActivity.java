package com.example.kursachh.ui.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.kursachh.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import Interface.IUser;
import ModelRequest.RecipeUpdateRequest;
import RetrofitModels.RetroFit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeDetailActivity extends AppCompatActivity {

    private int recipeId;
    private IUser userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        userService = RetroFit.getClient().create(IUser.class);

        // Инициализация элементов интерфейса
        initViews();
    }

    private void initViews() {
        // Кнопка назад
        ImageView backButton = findViewById(R.id.imageBackRecipeDetail);
        backButton.setOnClickListener(v -> finish());

        // Получаем данные из Intent
        Intent intent = getIntent();
        recipeId = getIntent().getIntExtra("recipe_id", -1);
        String recipeName = getIntent().getStringExtra("recipe_name");
        String recipePhoto = getIntent().getStringExtra("recipe_photo");
        float recipeCalories = getIntent().getFloatExtra("recipe_calories", 0);
        String recipeComponents = getIntent().getStringExtra("recipe_components");
        String recipeSteps = getIntent().getStringExtra("recipe_steps");
        float recipeProteins = getIntent().getFloatExtra("recipe_proteins", 0);
        float recipeFats = getIntent().getFloatExtra("recipe_fats", 0);
        float recipeCarbs = getIntent().getFloatExtra("recipe_carbs", 0);
        String recipeDate = getIntent().getStringExtra("recipe_date");


        // Инициализация элементов
        TextView nameView = findViewById(R.id.recipeDetailName);
        ImageView photoView = findViewById(R.id.recipeDetailPhoto);
        TextView caloriesView = findViewById(R.id.recipeDetailCalories);
        TextView componentsView = findViewById(R.id.recipeDetailComponents);
        TextView stepsView = findViewById(R.id.recipeDetailSteps);
        TextView proteinsView = findViewById(R.id.recipeDetailProteins);
        TextView fatsView = findViewById(R.id.recipeDetailFats);
        TextView carbsView = findViewById(R.id.recipeDetailCarbs);
        TextView dateView = findViewById(R.id.recipeDetailDate);


        // Установка данных
        nameView.setText(recipeName);
        caloriesView.setText(String.format("Калории: %.1f ккал", recipeCalories));
        componentsView.setText(String.format("Ингредиенты:\n%s", recipeComponents));
        stepsView.setText(String.format("Шаги приготовления:\n%s", recipeSteps));
        proteinsView.setText(String.format("Белки: %.1f г", recipeProteins));
        fatsView.setText(String.format("Жиры: %.1f г", recipeFats));
        carbsView.setText(String.format("Углеводы: %.1f г", recipeCarbs));

        // Форматирование и отображение даты
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            Date date = inputFormat.parse(recipeDate);
            dateView.setText(String.format("Дата создания: %s", outputFormat.format(date)));
        } catch (ParseException e) {
            dateView.setText(String.format("Дата создания: %s", recipeDate));
        }

        // Загрузка изображения
        loadRecipeImage(photoView, recipePhoto);

        // Кнопка редактирования
        Button editButton = findViewById(R.id.editRecipeButton);
        editButton.setOnClickListener(v -> showEditDialog(
                recipeName, recipePhoto, recipeCalories,
                recipeComponents, recipeSteps,
                recipeProteins, recipeFats, recipeCarbs
        ));
    }

    private void loadRecipeImage(ImageView imageView, String photoUrl) {
        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.placeholder_recipe)
                    .error(R.drawable.placeholder_recipe)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.placeholder_recipe);
        }
    }

    private void showEditDialog(String name, String photo, float calories,
                                String components, String steps,
                                float proteins, float fats, float carbs) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_recipe, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.etEditRecipeName);
        EditText etPhoto = dialogView.findViewById(R.id.etEditRecipePhoto);
        EditText etCalories = dialogView.findViewById(R.id.etEditRecipeCalories);
        EditText etComponents = dialogView.findViewById(R.id.etEditRecipeComponents);
        EditText etSteps = dialogView.findViewById(R.id.etEditRecipeSteps);
        EditText etProteins = dialogView.findViewById(R.id.etEditRecipeProteins);
        EditText etFats = dialogView.findViewById(R.id.etEditRecipeFats);
        EditText etCarbs = dialogView.findViewById(R.id.etEditRecipeCarbs);
        Button btnSave = dialogView.findViewById(R.id.btnSaveEditRecipe);

        // Заполняем поля текущими значениями
        etName.setText(name);
        etPhoto.setText(photo);
        etCalories.setText(String.valueOf(calories));
        etComponents.setText(components);
        etSteps.setText(steps);
        etProteins.setText(String.valueOf(proteins));
        etFats.setText(String.valueOf(fats));
        etCarbs.setText(String.valueOf(carbs));

        AlertDialog dialog = builder.create();
        dialog.show();

        btnSave.setOnClickListener(v -> {
            // Получаем новые значения
            String newName = etName.getText().toString().trim();
            String newPhoto = etPhoto.getText().toString().trim();
            String newCaloriesStr = etCalories.getText().toString().trim();
            String newComponents = etComponents.getText().toString().trim();
            String newSteps = etSteps.getText().toString().trim();
            String newProteinsStr = etProteins.getText().toString().trim();
            String newFatsStr = etFats.getText().toString().trim();
            String newCarbsStr = etCarbs.getText().toString().trim();

            // Валидация
            if (newName.isEmpty() || newComponents.isEmpty() || newSteps.isEmpty() || newCaloriesStr.isEmpty()) {
                Toast.makeText(this, "Заполните обязательные поля", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                float newCalories = Float.parseFloat(newCaloriesStr);
                float newProteins = Float.parseFloat(newProteinsStr);
                float newFats = Float.parseFloat(newFatsStr);
                float newCarbs = Float.parseFloat(newCarbsStr);

                // Создаем запрос на обновление
                RecipeUpdateRequest request = new RecipeUpdateRequest(
                        newName, newCalories, newPhoto.isEmpty() ? null : newPhoto,
                        newComponents, newSteps, newProteins, newFats, newCarbs
                );

                // Отправляем запрос на сервер
                userService.updateRecipe(recipeId, request).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(RecipeDetailActivity.this,
                                    "Рецепт успешно обновлен", Toast.LENGTH_SHORT).show();
                            // Обновляем данные на экране
                            updateRecipeDetails(
                                    newName, newPhoto, newCalories,
                                    newComponents, newSteps,
                                    newProteins, newFats, newCarbs
                            );
                            dialog.dismiss();
                        } else {
                            Toast.makeText(RecipeDetailActivity.this,
                                    "Ошибка обновления рецепта", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(RecipeDetailActivity.this,
                                "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Введите корректные числовые значения", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRecipeDetails(String name, String photo, float calories,
                                     String components, String steps,
                                     float proteins, float fats, float carbs) {
        TextView nameView = findViewById(R.id.recipeDetailName);
        ImageView photoView = findViewById(R.id.recipeDetailPhoto);
        TextView caloriesView = findViewById(R.id.recipeDetailCalories);
        TextView componentsView = findViewById(R.id.recipeDetailComponents);
        TextView stepsView = findViewById(R.id.recipeDetailSteps);
        TextView proteinsView = findViewById(R.id.recipeDetailProteins);
        TextView fatsView = findViewById(R.id.recipeDetailFats);
        TextView carbsView = findViewById(R.id.recipeDetailCarbs);

        nameView.setText(name);
        caloriesView.setText(String.format("Калории: %.1f ккал", calories));
        componentsView.setText(String.format("Ингредиенты:\n%s", components));
        stepsView.setText(String.format("Шаги приготовления:\n%s", steps));
        proteinsView.setText(String.format("Белки: %.1f г", proteins));
        fatsView.setText(String.format("Жиры: %.1f г", fats));
        carbsView.setText(String.format("Углеводы: %.1f г", carbs));

        if (photo != null && !photo.isEmpty()) {
            Glide.with(this)
                    .load(photo)
                    .placeholder(R.drawable.placeholder_recipe)
                    .error(R.drawable.placeholder_recipe)
                    .into(photoView);
        } else {
            photoView.setImageResource(R.drawable.placeholder_recipe);
        }
    }
}