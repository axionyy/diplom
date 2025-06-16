package com.example.kursachh.ui.profile;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kursachh.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        ImageView backButton = findViewById(R.id.imageBackOutSettings);
        backButton.setOnClickListener(v -> finish());
    }
}