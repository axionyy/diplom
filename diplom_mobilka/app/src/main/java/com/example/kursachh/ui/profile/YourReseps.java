package com.example.kursachh.ui.profile;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kursachh.R;

public class YourReseps extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_reseps);

        ImageView imageBackResepsToProfile = findViewById(R.id.imageBackYourReseps);
        imageBackResepsToProfile.setOnClickListener(v -> {
            finish(); // Просто закрываем активность
        });
    }
}
