package com.example.kursachh.ui.profile;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kursachh.AuthManager;
import com.example.kursachh.R;

import java.util.ArrayList;
import java.util.List;

import Model.Achievement;

public class AchievementsActivity extends AppCompatActivity {
    private AchievementsAdapter adapter;
    private List<Achievement> achievements = new ArrayList<>();
    private static final String CHANNEL_ID = "achievements_channel";
    private AuthManager authManager;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        authManager = new AuthManager(this);
        currentUserId = authManager.getUserId();

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.achievementsRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        initAchievements();
        adapter = new AchievementsAdapter(achievements, currentUserId);
        recyclerView.setAdapter(adapter);

        loadUserProgress();
        createNotificationChannel();
    }

    private void initAchievements() {
        achievements.add(new Achievement(
                "first_entry",
                "Первый шаг",
                "Выполните первый вход в приложение",
                1,
                R.drawable.ic_trophy));

        achievements.add(new Achievement(
                "food_diary_expert",
                "Эксперт дневника питания",
                "Добавьте 10 записей в дневник питания",
                10,
                R.drawable.ic_trophy));

        achievements.add(new Achievement(
                "recipe_master",
                "Мастер рецептов",
                "Добавьте 5 рецептов",
                5,
                R.drawable.ic_trophy));

        achievements.add(new Achievement(
                "weekly_streak",
                "Недельный марафон",
                "Заходите в приложение 7 дней подряд",
                7,
                R.drawable.ic_trophy));
    }

    private void loadUserProgress() {
        SharedPreferences prefs = getSharedPreferences(
                "user_" + currentUserId + "_achievements", MODE_PRIVATE);

        for (Achievement achievement : achievements) {
            int progress = prefs.getInt(achievement.getId(), 0);
            achievement.setProgress(progress);
            achievement.setUnlocked(progress >= achievement.getTarget());
        }

        adapter.notifyDataSetChanged();
    }

    private static void showAchievementUnlockedNotification(Context context, String achievementId) {
        try {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Achievements",
                        NotificationManager.IMPORTANCE_DEFAULT);
                manager.createNotificationChannel(channel);
            }

            String title = "Новое достижение!";
            String text = "";

            switch (achievementId) {
                case "first_entry":
                    text = "Вы выполнили первый вход в приложение";
                    break;
                case "recipe_master":
                    text = "Вы добавили 5 рецептов";
                    break;
                case "weekly_streak":
                    text = "Вы заходили в приложение 7 дней подряд";
                    break;
                case "food_diary_expert":
                    text = "Вы добавили 10 записей в дневник питания";
                    break;
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_trophy)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            manager.notify(achievementId.hashCode(), builder.build());
        } catch (SecurityException e) {
            // Игнорируем, если нет разрешения на уведомления
            Log.e("Achievements", "No notification permission", e);
        }
    }

    public static void updateAchievementProgress(Context context, String achievementId, int progress) {
        AuthManager authManager = new AuthManager(context);
        int userId = authManager.getUserId();
        if (userId <= 0) return;

        SharedPreferences prefs = context.getSharedPreferences(
                "user_" + userId + "_achievements", MODE_PRIVATE);

        int currentProgress = prefs.getInt(achievementId, 0);
        int newProgress = currentProgress + progress;
        int target = getTargetForAchievement(achievementId);

        boolean wasUnlocked = currentProgress >= target;
        boolean nowUnlocked = newProgress >= target;

        prefs.edit().putInt(achievementId, newProgress).apply();

        if (!wasUnlocked && nowUnlocked) {
            showAchievementUnlockedNotification(context, achievementId);
        }
    }

    private static int getTargetForAchievement(String achievementId) {
        switch (achievementId) {
            case "first_entry": return 1;
            case "recipe_master": return 5;
            case "weekly_streak": return 7;
            case "food_diary_expert": return 10;
            default: return 1;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Достижения",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Уведомления о получении достижений");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}