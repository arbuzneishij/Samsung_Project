package com.example.samsung_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // Массив для хранения 6 кнопок
    private Button[] buttons = new Button[6];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Привязываем кнопки из layout
        buttons[0] = findViewById(R.id.button1);
        buttons[1] = findViewById(R.id.button2);
        buttons[2] = findViewById(R.id.button3);
        buttons[3] = findViewById(R.id.button4);
        buttons[4] = findViewById(R.id.button5);
        buttons[5] = findViewById(R.id.button6);

        // Кнопка сброса
        Button btnReset = findViewById(R.id.buttonReset);

        // Обновляем текст всех кнопок при запуске
        updateAllButtons();

        // Назначаем обработчик на каждую кнопку
        for (int i = 0; i < 6; i++) {

            final int index = i; // сохраняем индекс кнопки

            buttons[i].setOnClickListener(v -> {

                // Получаем сохранённый packageName для этой кнопки
                String savedPkg = getSharedPreferences("prefs", MODE_PRIVATE)
                        .getString("saved_app_" + index, null);

                if (savedPkg == null) {
                    // Если приложение не назначено — открываем выбор
                    Intent intent = new Intent(MainActivity.this, AppChooserActivity.class);

                    // Передаём индекс кнопки
                    intent.putExtra("button_index", index);

                    startActivity(intent);
                } else {
                    // Если уже назначено — запускаем приложение
                    ButtonFounder.openApp(MainActivity.this, savedPkg);
                }
            });
        }

        // Обработчик кнопки "Сброс"
        btnReset.setOnClickListener(v -> {

            // Получаем редактор SharedPreferences
            var editor = getSharedPreferences("prefs", MODE_PRIVATE).edit();

            // Удаляем данные всех 6 кнопок
            for (int i = 0; i < 6; i++) {
                editor.remove("saved_app_" + i);
                editor.remove("saved_app_name_" + i);
            }

            editor.apply();

            // Обновляем UI
            updateAllButtons();

            //создаём уведомление о сбросе всех кнопок
            Toast.makeText(this, "Все кнопки сброшены", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Обновляем кнопки после возврата из выбора приложения
        updateAllButtons();
    }

    // Метод обновляет текст всех кнопок
    private void updateAllButtons() {

        for (int i = 0; i < 6; i++) {

            // Получаем сохранённое имя приложения
            String appName = getSharedPreferences("prefs", MODE_PRIVATE)
                    .getString("saved_app_name_" + i, null);

            if (appName != null) {
                // Если назначено — показываем имя
                buttons[i].setText(appName);
            } else {
                // Если нет — стандартный текст
                buttons[i].setText("Кнопка " + (i + 1));
            }
        }
    }
}