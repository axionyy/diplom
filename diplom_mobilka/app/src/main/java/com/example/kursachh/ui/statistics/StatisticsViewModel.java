package com.example.kursachh.ui.statistics;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Interface.IUser;
import Model.EatingRecord;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatisticsViewModel extends ViewModel {
    private final MutableLiveData<String> imtResult = new MutableLiveData<>("Загрузка...");
    private final MutableLiveData<String> waterNorm = new MutableLiveData<>("Загрузка...");
    private final MutableLiveData<String> caloriesNorm = new MutableLiveData<>("Загрузка...");

    private final MutableLiveData<String> waterConsumed = new MutableLiveData<>("0 мл");
    private final MutableLiveData<String> waterRemaining = new MutableLiveData<>("0 мл");

    public void calculateUserStats(Float height, Float weight, String birthday, Boolean gender) {
        if (height == null || weight == null || birthday == null || gender == null) {
            imtResult.setValue("Данные отсутствуют");
            return;
        }
        // Расчет ИМТ
        calculateIMT(height, weight);
        // Расчет нормы воды
        calculateWaterNorm(weight);
        // Расчет нормы калорий
        calculateCaloriesNorm(weight, height, birthday, gender);
    }

    private void calculateIMT(float height, float weight) {
        if (height <= 0 || weight <= 0) {
            imtResult.setValue("Неверные данные");
            return;
        }

        float heightInMeters = height / 100;
        float imt = weight / (heightInMeters * heightInMeters);

        String category;
        if (imt < 16) category = "Выраж. дефицит";
        else if (imt < 18.5) category = "Недостат. вес";
        else if (imt < 25) category = "Норма";
        else if (imt < 30) category = "Избыт. вес";
        else if (imt < 35) category = "Ожирение 1 ст.";
        else if (imt < 40) category = "Ожирение 2 ст.";
        else category = "Ожирение 3 ст.";

        imtResult.setValue(String.format(Locale.getDefault(), "%s (%.1f)", category, imt));
    }

    private void calculateWaterNorm(float weight) {
        int norm = (int) (weight * 35);
        waterNorm.setValue(norm + " мл");
    }

    private void calculateCaloriesNorm(float weight, float height, String birthday, boolean gender) {
        int age = calculateAge(birthday);
        double calories;

        if (gender) { // female
            calories = 10 * weight + 6.25 * height - 5 * age - 161;
        } else { // male
            calories = 10 * weight + 6.25 * height - 5 * age + 5;
        }

        caloriesNorm.setValue((int) calories + " ккал");
    }

    private int calculateAge(String birthday) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date birthDate = sdf.parse(birthday);
            if (birthDate == null) return 30;

            Calendar today = Calendar.getInstance();
            Calendar birth = Calendar.getInstance();
            birth.setTime(birthDate);

            int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);

            if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            return age;
        } catch (ParseException e) {
            e.printStackTrace();
            return 30;
        }
    }

    public void updateWaterStats(int userId, IUser userService) {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        userService.getEatingRecords(userId, currentDate).enqueue(new Callback<List<EatingRecord>>() {
            @Override
            public void onResponse(Call<List<EatingRecord>> call, Response<List<EatingRecord>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int totalWater = 0;

                    // Считаем только записи о воде (mealType = "water")
                    for (EatingRecord record : response.body()) {
                        if ("water".equals(record.getMealType())) {
                            totalWater += record.getQuantity();
                        }
                    }

                    // Обновляем потребленное количество
                    waterConsumed.setValue(totalWater + " мл");

                    // Вычисляем оставшуюся норму
                    String currentNorm = waterNorm.getValue();
                    if (currentNorm != null && currentNorm.contains(" мл")) {
                        try {
                            int norm = Integer.parseInt(currentNorm.replace(" мл", ""));
                            int remaining = Math.max(0, norm - totalWater);
                            waterRemaining.setValue(remaining + " мл");
                        } catch (NumberFormatException e) {
                            waterRemaining.setValue("Ошибка расчета");
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<EatingRecord>> call, Throwable t) {
                waterConsumed.setValue("Ошибка загрузки");
                waterRemaining.setValue("Ошибка загрузки");
            }
        });
    }

    // Геттеры для LiveData

    // Добавляем геттеры для новых LiveData
    public LiveData<String> getWaterConsumed() { return waterConsumed; }
    public LiveData<String> getWaterRemaining() { return waterRemaining; }
    public LiveData<String> getImtResult() { return imtResult; }
    public LiveData<String> getWaterNorm() { return waterNorm; }
    public LiveData<String> getCaloriesNorm() { return caloriesNorm; }
}