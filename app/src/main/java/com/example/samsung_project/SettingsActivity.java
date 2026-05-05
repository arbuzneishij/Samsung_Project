package com.example.samsung_project;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    Button btnVolume, btnBrightness, btnThemes, btnPermissions, btnHelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        btnVolume = findViewById(R.id.btnVolume);
        btnBrightness = findViewById(R.id.btnBrightness);
        btnThemes = findViewById(R.id.btnThemes);
        btnPermissions = findViewById(R.id.btnPermissions);
        btnHelp = findViewById(R.id.btnHelp);

        //Громкость
        btnVolume.setOnClickListener(v ->
                Toast.makeText(this, "Открыть настройки громкости", Toast.LENGTH_SHORT).show()
        );

        //ркость
        btnBrightness.setOnClickListener(v ->
                Toast.makeText(this, "Открыть настройки яркости", Toast.LENGTH_SHORT).show()
        );

        // Темы
        btnThemes.setOnClickListener(v ->
                Toast.makeText(this, "Открыть выбор темы", Toast.LENGTH_SHORT).show()
        );

        // Разрешения
        btnPermissions.setOnClickListener(v ->
                Toast.makeText(this, "Открыть разрешения", Toast.LENGTH_SHORT).show()
        );

        // Помощь
        btnHelp.setOnClickListener(v ->
                Toast.makeText(this, "Открыть помощь", Toast.LENGTH_SHORT).show()
        );
    }
}