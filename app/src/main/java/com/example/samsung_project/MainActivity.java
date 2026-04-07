package com.example.samsung_project;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button btnOpenApp; // кнопка открытия приложения

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOpenApp = findViewById(R.id.buttonOpenApp);
        Button btnReset = findViewById(R.id.buttonReset);

        // Обновляем текст кнопки при запуске
        updateButtonText();

        // Нажатие на кнопку открытия приложения
        btnOpenApp.setOnClickListener(v -> {

            // Получаем сохранённый packageName
            String savedPkg = getSharedPreferences("prefs", MODE_PRIVATE)
                    .getString("saved_app", null);

            if (savedPkg == null) {
                // Если ничего не выбрано — открываем список приложений
                Intent i = new Intent(MainActivity.this, AppChooserActivity.class);
                startActivity(i);
            } else {

                // Проверяем разрешение на overlay (плавающая кнопка)
                if (Settings.canDrawOverlays(this)) {

                    // Запускаем сервис плавающей кнопки
                    //startService(new Intent(this, FloatingButtonService.class));

                    // Открываем выбранное приложение
                    ButtonFounder.openApp(MainActivity.this, savedPkg);

                } else {
                    // Запрашиваем разрешение
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    startActivity(intent);
                }
            }
        });

        // Кнопка сброса выбора
        btnReset.setOnClickListener(v -> {

            // Удаляем сохранённые данные
            getSharedPreferences("prefs", MODE_PRIVATE)
                    .edit()
                    .remove("saved_app")
                    .remove("saved_app_name")
                    .apply();

            // Обновляем текст кнопки
            updateButtonText();

            Toast.makeText(MainActivity.this, "Выбор сброшен", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Останавливаем сервис при возврате
        stopService(new Intent(this, FloatingButtonService.class));

        // Обновляем кнопку (например после выбора приложения)
        updateButtonText();
    }

    // Метод обновления текста кнопки
    private void updateButtonText() {

        // Получаем сохранённое имя приложения
        String appName = getSharedPreferences("prefs", MODE_PRIVATE)
                .getString("saved_app_name", null);

        if (appName != null) {
            // Если приложение выбрано — показываем его имя
            btnOpenApp.setText(appName);
        } else {
            // Если нет — стандартный текст
            btnOpenApp.setText("Открыть приложение");
        }
    }
}