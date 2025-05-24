package com.example.kursachh.ui.profile;

import android.content.Intent;
import android.view.View;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kursachh.R;

public class RefactorYourProfile extends AppCompatActivity {

    private EditText loginEdit;
    private EditText nameEdit;
    private EditText birthdayEdit;
    private EditText heightEdit;
    private EditText passwordRefNow;
    private EditText passwordRefNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refator_profile);

        loginEdit = findViewById(R.id.refactorLoginEdit);
        nameEdit = findViewById(R.id.refactorNameEdit);
        birthdayEdit = findViewById(R.id.refactorBirthdayEdit);
        heightEdit = findViewById(R.id.refactorHeightEdit);
        passwordRefNow = findViewById(R.id.passwordRefactorNow);
        passwordRefNext = findViewById(R.id.passwordRefactorNext);
        Button applyButton = findViewById(R.id.applyRefactorsButton);

        applyButton.setOnClickListener(v -> {
            if (isPasswordFieldTouched()) {
                if (passwordRefNow.getText().toString().trim().isEmpty()) {
                    Toast.makeText(RefactorYourProfile.this, "Введите используемый пароль", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RefactorYourProfile.this, "Данные сохранены", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(RefactorYourProfile.this, "Данные сохранены", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isPasswordFieldTouched() {
        return !passwordRefNext.getText().toString().trim().isEmpty();
    }

    private boolean areAllFieldsFilled() {
        return  !loginEdit.getText().toString().trim().isEmpty() &&
                !nameEdit.getText().toString().trim().isEmpty() &&
                !birthdayEdit.getText().toString().trim().isEmpty() &&
                !heightEdit.getText().toString().trim().isEmpty() &&
                !passwordRefNow.getText().toString().trim().isEmpty() &&
                !passwordRefNext.getText().toString().trim().isEmpty();
    }

    public void RefBack1(View v) {
        Intent intent = new Intent(RefactorYourProfile.this, ProfileFragment.class);
        startActivity(intent);
    }
}
