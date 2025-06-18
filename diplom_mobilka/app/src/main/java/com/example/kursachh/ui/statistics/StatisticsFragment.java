package com.example.kursachh.ui.statistics;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.kursachh.AuthManager;
import com.example.kursachh.databinding.FragmentStatisticsBinding;

import Interface.IUser;
import Model.User;
import RetrofitModels.RetroFit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatisticsFragment extends Fragment {

    private FragmentStatisticsBinding binding;
    private StatisticsViewModel statisticsViewModel;
    private AuthManager authManager;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        authManager = new AuthManager(context); // Инициализируем authManager здесь
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        statisticsViewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);

        // Настройка наблюдателей
        setupObservers();

        // Загрузка данных пользователя
        loadUserData();

        return root;
    }

    private void setupObservers() {
        statisticsViewModel.getImtResult().observe(getViewLifecycleOwner(),
                result -> binding.numberIMT.setText(result));

        statisticsViewModel.getWaterNorm().observe(getViewLifecycleOwner(),
                norm -> binding.numberWater.setText(norm));

        statisticsViewModel.getCaloriesNorm().observe(getViewLifecycleOwner(),
                norm -> binding.numberCalloriesNorm.setText(norm));

        statisticsViewModel.getWaterConsumed().observe(getViewLifecycleOwner(),
                consumed -> binding.waterConsumed.setText(consumed));

        statisticsViewModel.getWaterRemaining().observe(getViewLifecycleOwner(),
                remaining -> binding.waterRemaining.setText(remaining));
    }

    private void loadUserData() {
        if (authManager == null) {
            authManager = new AuthManager(requireContext());
        }

        int userId = authManager.getUserId();
        Log.d("StatisticsFragment", "User ID: " + userId);

        if (userId == -1) {
            showError("Пользователь не авторизован");
            return;
        }

        IUser userApi = RetroFit.getClient().create(IUser.class);

        // Загружаем данные пользователя
        Call<User> call = userApi.getUser(userId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    Log.d("StatisticsFragment", "User data: " + user.toString());

                    if (user.getHeight() == null || user.getWeight() == null ||
                            user.getBirthday() == null || user.isGender() == null) {
                        showError("Неполные данные пользователя");
                        return;
                    }

                    // Расчет всех показателей
                    statisticsViewModel.calculateUserStats(
                            user.getHeight(),
                            user.getWeight(),
                            user.getBirthday(),
                            user.isGender()
                    );

                    // Обновляем данные о потребленной воде
                    statisticsViewModel.updateWaterStats(userId, userApi);
                } else {
                    showError("Ошибка загрузки данных");
                    Log.e("Statistics", "Ошибка ответа: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                showError("Нет соединения");
                Log.e("Statistics", "Ошибка сети", t);
            }
        });
    }

    private void showError(String message) {
        binding.numberIMT.setText(message);
        binding.numberWater.setText(message);
        binding.numberCalloriesNorm.setText(message);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (authManager != null && statisticsViewModel != null) {
            int userId = authManager.getUserId();
            if (userId != -1) {
                IUser userApi = RetroFit.getClient().create(IUser.class);
                statisticsViewModel.updateWaterStats(userId, userApi);
            }
        }
    }
}