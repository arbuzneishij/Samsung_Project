package com.example.samsung_project;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    Button btnVolume, btnBrightness, btnThemes, btnPermissions, btnHelp, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        btnVolume = findViewById(R.id.btnVolume);
        btnBrightness = findViewById(R.id.btnBrightness);
        btnThemes = findViewById(R.id.btnThemes);
        btnPermissions = findViewById(R.id.btnPermissions);
        btnHelp = findViewById(R.id.btnHelp);
        btnBack = findViewById(R.id.btnBack);

        // Громкость - открывает настройки звука
        btnVolume.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_SOUND_SETTINGS);
            startActivity(intent);
        });

        // Яркость - открывает настройки дисплея
        btnBrightness.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_DISPLAY_SETTINGS);
            startActivity(intent);
        });

        // Темы - открывает настройки
        btnThemes.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_DISPLAY_SETTINGS);
            startActivity(intent);

        });

        // Разрешения - открывает настройки разрешений приложения
        btnPermissions.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(android.net.Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        });

        // Помощь
        btnHelp.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, HelpActivity.class));
            finish();
        });

        // Назад
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, MainActivity.class));
            finish();
        });
    }
}