package com.example.kursachh.ui.diary;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.kursachh.databinding.FragmentDiaryBinding;

public class DiaryFragment extends Fragment {

    private FragmentDiaryBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDiaryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Обработка нажатий на кнопки
        binding.waterAdding.setOnClickListener(v -> openWaterActivity());
        binding.breakfastAdding.setOnClickListener(v -> openMealActivity("Завтрак"));
        binding.lunchAdding.setOnClickListener(v -> openMealActivity("Обед"));
        binding.dinnerAdding.setOnClickListener(v -> openMealActivity("Ужин"));
        binding.snackAdding.setOnClickListener(v -> openMealActivity("Перекус"));

        return root;
    }

    private void openWaterActivity() {
        Intent intent = new Intent(getActivity(), WaterActivity.class);
        startActivity(intent);
    }

    private void openMealActivity(String mealType) {
        Intent intent = new Intent(getActivity(), MealActivity.class);
        intent.putExtra(MealActivity.MEAL_TYPE, mealType);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}