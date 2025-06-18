package com.example.kursachh.ui.statistics;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.kursachh.AuthManager;
import com.example.kursachh.R;
import com.example.kursachh.databinding.FragmentStatisticsBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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
        authManager = new AuthManager(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        statisticsViewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);

        setupObservers();
        setupDateSelector();
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

        statisticsViewModel.getCaloriesConsumed().observe(getViewLifecycleOwner(),
                consumed -> binding.caloriesConsumedNum.setText(consumed));

        statisticsViewModel.getCaloriesRemaining().observe(getViewLifecycleOwner(),
                remaining -> binding.caloriesRemainingNum.setText(remaining));
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                R.style.CustomDatePickerDialog,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);

                    String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            .format(selectedDate.getTime());
                    statisticsViewModel.setSelectedDate(formattedDate);

                    if (isToday(selectedDate)) {
                        binding.selectedDateText.setText("Сегодня");
                    } else {
                        binding.selectedDateText.setText(
                                new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                                        .format(selectedDate.getTime()));
                    }

                    loadConsumptionData();
                },
                year, month, day);

        datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
        datePickerDialog.show();
    }

    private boolean isToday(Calendar date) {
        Calendar today = Calendar.getInstance();
        return date.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                date.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                date.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH);
    }

    private void setupDateSelector() {
        binding.calendarButton.setOnClickListener(v -> showDatePickerDialog());
        binding.selectedDateText.setText("Сегодня");
    }

    private void loadUserData() {
        if (authManager == null) {
            authManager = new AuthManager(requireContext());
        }

        int userId = authManager.getUserId();
        if (userId == -1) {
            showError("Пользователь не авторизован");
            return;
        }

        IUser userApi = RetroFit.getClient().create(IUser.class);
        Call<User> call = userApi.getUser(userId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    if (user.getHeight() == null || user.getWeight() == null ||
                            user.getBirthday() == null || user.isGender() == null) {
                        showError("Неполные данные пользователя");
                        return;
                    }

                    statisticsViewModel.calculateUserStats(
                            user.getHeight(),
                            user.getWeight(),
                            user.getBirthday(),
                            user.isGender()
                    );

                    loadConsumptionData();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                showError("Нет соединения");
            }
        });
    }

    private void loadConsumptionData() {
        if (authManager != null) {
            int userId = authManager.getUserId();
            if (userId != -1) {
                IUser userApi = RetroFit.getClient().create(IUser.class);
                statisticsViewModel.loadWaterData(userId, userApi);
                statisticsViewModel.loadCaloriesData(userId, userApi);
            }
        }
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
}