package com.example.kursachh.ui.profile;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kursachh.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        ImageView backButton = findViewById(R.id.imageBackOutSettings);
        backButton.setOnClickListener(v -> finish());

        Button supportButton = findViewById(R.id.supportButton);
        supportButton.setOnClickListener(v -> sendSupportEmail());

        Button achievementsButton = findViewById(R.id.achievementsButton);
        achievementsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AchievementsActivity.class);
            startActivity(intent);
        });
    }

    private void sendSupportEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"NytriGidsup@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Поддержка приложения НутриГид");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Опишите вашу проблему...");

        try {
            startActivity(Intent.createChooser(emailIntent, "Отправить письмо через..."));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Нет установленных почтовых клиентов", Toast.LENGTH_SHORT).show();
        }
    }
}