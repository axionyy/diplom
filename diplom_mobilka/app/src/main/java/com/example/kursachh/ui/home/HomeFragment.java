package com.example.kursachh.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.kursachh.CustomDialogFragment;
import com.example.kursachh.R;
import com.example.kursachh.databinding.FragmentHomeBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Interface.IUser;
import Model.Recipe;
import RetrofitModels.RetroFit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private IUser userService;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textrecepts;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        userService = RetroFit.getClient().create(IUser.class);
        loadAllRecipes();

        return root;
    }

    private void loadAllRecipes() {
        userService.getAllRecipes().enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayRecipes(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Recipe>> call, Throwable t) {
                // Обработка ошибки
            }
        });
    }

    private void displayRecipes(List<Recipe> recipes) {
        binding.recipesContainer.removeAllViews();

        for (Recipe recipe : recipes) {
            View recipeView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_recipe_home, binding.recipesContainer, false);

            TextView nameView = recipeView.findViewById(R.id.recipeName);
            TextView caloriesView = recipeView.findViewById(R.id.recipeCalories);
            ImageView photoView = recipeView.findViewById(R.id.recipePhoto);
            TextView dateView = recipeView.findViewById(R.id.recipeDate);
            LinearLayout ingredientsLayout = recipeView.findViewById(R.id.ingredientsLayout);

            nameView.setText(recipe.name);
            caloriesView.setText(String.format(Locale.getDefault(), "%.1f ккал", recipe.calories));

            // Форматирование даты
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

            // Разбиваем компоненты на строки и добавляем в layout
            String[] ingredients = recipe.components.split("\n");
            for (String ingredient : ingredients) {
                if (!ingredient.trim().isEmpty()) {
                    TextView ingredientView = new TextView(getContext());
                    ingredientView.setText("· " + ingredient.trim());
                    ingredientView.setTextSize(15);
                    ingredientsLayout.addView(ingredientView);
                }
            }

            recipeView.setOnClickListener(v -> showRecipeDetails(recipe));

            binding.recipesContainer.addView(recipeView);
        }
    }

    private void showRecipeDetails(Recipe recipe) {
        CustomDialogFragment dialogFragment = CustomDialogFragment.newInstance(
                recipe.name,
                "Калории: " + recipe.calories + " ккал\n\n" +
                        "Ингредиенты:\n" + recipe.components + "\n\n" +
                        "Шаги приготовления:\n" + recipe.steps
        );
        dialogFragment.show(getParentFragmentManager(), "recipeDetails");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}