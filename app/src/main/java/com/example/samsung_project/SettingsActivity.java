package com.example.samsung_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        // Инициализация кнопок из XML
        Button btnVolume = findViewById(R.id.btnVolume);
        Button btnBrightness = findViewById(R.id.btnBrightness);
        Button btnThemes = findViewById(R.id.btnThemes);
        Button btnPermissions = findViewById(R.id.btnPermissions);
        Button btnHelp = findViewById(R.id.btnHelp);

        // Установка обработчиков нажатий
        btnVolume.setOnClickListener(v -> openCategory("volume"));
        btnBrightness.setOnClickListener(v -> openCategory("brightness"));
        btnThemes.setOnClickListener(v -> openCategory("themes"));
        btnPermissions.setOnClickListener(v -> openCategory("permissions"));
        btnHelp.setOnClickListener(v -> openCategory("help"));
    }

    // Переход в категорию настроек(заглушка)
    private void openCategory(String category) {
        Intent intent = new Intent(this, SettingsCategoryActivity.class);
        intent.putExtra("category", category);
        startActivity(intent);
    }
}