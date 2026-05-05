package com.example.samsung_project;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // Массив для хранения 6 кнопок
    private Button[] buttons = new Button[6];


    public static final int WIDGET_TIME = 0;
    public static final int WIDGET_SPEED = 1;
    public static final int WIDGET_IMAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnSettings = findViewById(R.id.buttonSettings);

        // Привязываем кнопки из layout
        buttons[0] = findViewById(R.id.button1);
        buttons[1] = findViewById(R.id.button2);
        buttons[2] = findViewById(R.id.button3);
        buttons[3] = findViewById(R.id.button4);
        buttons[4] = findViewById(R.id.button5);
        buttons[5] = findViewById(R.id.button6);

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

            //Долгое нажатие
            buttons[i].setOnLongClickListener(v -> {

                showLongPressMenu(index);
                return true;
            });

            //выбор виджета
            int widgetType = getSharedPreferences("prefs", MODE_PRIVATE)
                    .getInt("widget_type", WIDGET_TIME);

            showWidget(widgetType);
        }

        //Назначаем обработчик на кнопку настроек
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
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
    private void showLongPressMenu(int index) {

        String[] options = {"Переименовать", "Сбросить"};

        new AlertDialog.Builder(this)
                .setTitle("Настройки кнопки")
                .setItems(options, (dialog, which) -> {

                    if (which == 0) {
                        // Переименование
                        showRenameDialog(index);
                    }

                    if (which == 1) {
                        // Сброс конкретной кнопки
                        getSharedPreferences("prefs", MODE_PRIVATE)
                                .edit()
                                .remove("saved_app_" + index)
                                .remove("saved_app_name_" + index)
                                .apply();

                        updateAllButtons();

                        Toast.makeText(this, "Кнопка сброшена", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void showRenameDialog(int index) {

        EditText input = new EditText(this);
        input.setHint("Введите название");

        new AlertDialog.Builder(this)
                .setTitle("Новое имя кнопки")
                .setView(input)
                .setPositiveButton("Сохранить", (dialog, which) -> {

                    String newName = input.getText().toString();

                    if (!newName.isEmpty()) {

                        //Сохраняем новое имя
                        getSharedPreferences("prefs", MODE_PRIVATE)
                                .edit()
                                .putString("saved_app_name_" + index, newName)
                                .apply();

                        updateAllButtons();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showWidget(int type) {

        FrameLayout container = findViewById(R.id.widgetContainer);
        container.removeAllViews();
        container.setOnClickListener(v ->
                Toast.makeText(this, "Удерживайте для выбора виджета", Toast.LENGTH_SHORT).show()
        );
        //часы
        if (type == WIDGET_TIME) {
            TextView clock = new TextView(this);
            clock.setTextSize(32);

            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    java.text.DateFormat timeFormat =
                            java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT);
                    clock.setText(timeFormat.format(new java.util.Date()));

                    new android.os.Handler().postDelayed(this, 1000);
                }
            }, 0);

            container.addView(clock);
            container.setOnLongClickListener(v -> {
                showWidgetChooser();
                return true;
            });
        }

        //спидометр
        if (type == WIDGET_SPEED) {
            TextView speed = new TextView(this);
            speed.setTextSize(32);
            speed.setText("0 км/ч");

            // Потом добавить gps
            container.addView(speed);
        }

        //логотип
        if (type == WIDGET_IMAGE) {
            ImageView img = new ImageView(this);
            img.setImageResource(R.drawable.my_image);
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);

            container.addView(img);
        }

        container.setOnLongClickListener(v -> {
            showWidgetChooser();
            return true;
        });
    }

    private void showWidgetChooser() {

        String[] options = {"Время", "Спидометр", "Картинка"};

        new AlertDialog.Builder(this)
                .setTitle("Выбери виджет")
                .setItems(options, (dialog, which) -> {

                    getSharedPreferences("prefs", MODE_PRIVATE)
                            .edit()
                            .putInt("widget_type", which)
                            .apply();

                    showWidget(which);
                })
                .show();
    }
}