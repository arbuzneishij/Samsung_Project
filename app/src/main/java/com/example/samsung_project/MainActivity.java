package com.example.samsung_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class MainActivity extends AppCompatActivity {

    // Массив для хранения 6 кнопок
    private Button[] buttons = new Button[6];

    public static final int WIDGET_TIME = 0;
    public static final int WIDGET_SPEED = 1;
    public static final int WIDGET_IMAGE = 2;

    // GPS клиент
    private FusedLocationProviderClient fusedLocationClient;

    // Callback получения GPS
    private LocationCallback locationCallback;

    // Текст спидометра
    private TextView speedText;

    // Сглаженная скорость
    private float finalSpeed = 0;


    // Запущен ли GPS
    private boolean gpsStarted = false;


    private long lastLocationUpdate = 0;
    private final Handler timeoutHandler = new Handler();
    private Runnable timeoutRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // ================================
        // GPS INIT
        // ================================

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

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

            final int index = i;

            buttons[i].setOnClickListener(v -> {

                String savedPkg = getSharedPreferences("prefs", MODE_PRIVATE)
                        .getString("saved_app_" + index, null);

                if (savedPkg == null) {

                    Intent intent =
                            new Intent(
                                    MainActivity.this,
                                    AppChooserActivity.class
                            );

                    intent.putExtra("button_index", index);

                    startActivity(intent);

                } else {
                    showFloatingButton();
                    ButtonFounder.openApp(
                            MainActivity.this,
                            savedPkg
                    );
                }
            });

            // Долгое нажатие
            buttons[i].setOnLongClickListener(v -> {

                showLongPressMenu(index);
                return true;
            });
        }

        // Показывает сохранённый виджет
        int widgetType = getSharedPreferences(
                "prefs",
                MODE_PRIVATE
        ).getInt("widget_type", WIDGET_TIME);

        showWidget(widgetType);

        // Кнопка настроек
        btnSettings.setOnClickListener(v -> {
            startActivity(
                    new Intent(
                            this,
                            SettingsActivity.class
                    )
            );
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        stopService(
                new Intent(this, FloatingButtonService.class)
        );

        updateAllButtons();


        int widgetType = getSharedPreferences(
                "prefs",
                MODE_PRIVATE
        ).getInt("widget_type", WIDGET_TIME);

        if (widgetType == WIDGET_SPEED) {
            startSpeedometer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopSpeedometer();
    }

    // Метод обновляет текст всех кнопок
    private void updateAllButtons() {

        for (int i = 0; i < 6; i++) {

            String appName = getSharedPreferences("prefs", MODE_PRIVATE)
                    .getString("saved_app_name_" + i, null);

            if (appName != null) {

                buttons[i].setText(appName);

            } else {

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
                        showRenameDialog(index);
                    }

                    if (which == 1) {

                        getSharedPreferences("prefs", MODE_PRIVATE)
                                .edit()
                                .remove("saved_app_" + index)
                                .remove("saved_app_name_" + index)
                                .apply();

                        updateAllButtons();

                        Toast.makeText(
                                this,
                                "Кнопка сброшена",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .show();
    }

    // Диалог переименования
    private void showRenameDialog(int index) {

        EditText input = new EditText(this);
        input.setHint("Введите название");

        new AlertDialog.Builder(this)
                .setTitle("Новое имя кнопки")
                .setView(input)
                .setPositiveButton("Сохранить", (dialog, which) -> {

                    String newName =
                            input.getText().toString();

                    if (!newName.isEmpty()) {

                        getSharedPreferences(
                                "prefs",
                                MODE_PRIVATE
                        )
                                .edit()
                                .putString(
                                        "saved_app_name_" + index,
                                        newName
                                )
                                .apply();

                        updateAllButtons();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    // Показывает выбранный виджет
    private void showWidget(int type) {

        FrameLayout container =
                findViewById(R.id.widgetContainer);

        container.removeAllViews();

        // Часы
        if (type == WIDGET_TIME) {

            TextView clock = new TextView(this);
            clock.setTextSize(32);

            new android.os.Handler().postDelayed(
                    new Runnable() {

                        @Override
                        public void run() {

                            java.text.DateFormat timeFormat =
                                    java.text.DateFormat.getTimeInstance(
                                            java.text.DateFormat.SHORT
                                    );

                            clock.setText(
                                    timeFormat.format(
                                            new java.util.Date()
                                    )
                            );

                            new android.os.Handler()
                                    .postDelayed(this, 1000);
                        }
                    },
                    0
            );

            container.addView(clock);
        }

        // Спидометр
        if (type == WIDGET_SPEED) {

            speedText = new TextView(this);

            speedText.setTextSize(64);
            speedText.setTypeface(Typeface.DEFAULT_BOLD);
            speedText.setText("0 км/ч");

            container.addView(speedText);

            startSpeedometer();
        }

        // Картинка
        if (type == WIDGET_IMAGE) {

            ImageView img = new ImageView(this);

            img.setImageResource(R.drawable.my_image);

            img.setScaleType(
                    ImageView.ScaleType.CENTER_CROP
            );

            container.addView(img);
        }

        container.setOnLongClickListener(v -> {

            showWidgetChooser();

            return true;
        });
    }

    private void showWidgetChooser() {
        //выбирать виджет
        String[] options = {
                "Время",
                "Спидометр",
                "Картинка"
        };

        new AlertDialog.Builder(this)
                .setTitle("Выбери виджет")
                .setItems(options, (dialog, which) -> {

                    getSharedPreferences(
                            "prefs",
                            MODE_PRIVATE
                    )
                            .edit()
                            .putInt("widget_type", which)
                            .apply();

                    showWidget(which);
                })
                .show();
    }

    private void startSpeedometer() {

        if (gpsStarted) return;

        gpsStarted = true;
        //проверка на разрешения
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    100
            );

            return;
        }
        //запрос местоположения
        LocationRequest request =
                new LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        500
                )
                        .setMinUpdateIntervalMillis(300)
                        .setMinUpdateDistanceMeters(1)
                        .build();

        locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(
                    @NonNull LocationResult result
            ) {



                Location location =
                        result.getLastLocation();

                if (location == null) return;

                lastLocationUpdate = SystemClock.elapsedRealtime();

                float speedKmh = location.getSpeed() * 3.6f;




                if (speedKmh < 2.0f) {
                    finalSpeed = 0;
                } else {
                    finalSpeed = speedKmh;
                }

                runOnUiThread(() -> {

                    speedText.setText(
                            ((int) finalSpeed)
                                    + " км/ч"
                    );

                    if (finalSpeed > 120) {

                        speedText.setTextColor(
                                Color.RED
                        );

                    } else if (finalSpeed > 60) {

                        speedText.setTextColor(
                                Color.YELLOW
                        );

                    } else {

                        speedText.setTextColor(
                                Color.BLACK
                        );
                    }
                });
            }
        };

        fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
        );
        startTimeoutCheck();
    }

    private void stopSpeedometer() {

        if (!gpsStarted) return;

        gpsStarted = false;

        if (timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }

        if (locationCallback != null) {

            fusedLocationClient.removeLocationUpdates(
                    locationCallback
            );

            locationCallback = null;
        }
    }

    // Разрешение GPS
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == 100) {

            if (grantResults.length > 0
                    &&
                    grantResults[0]
                            == PackageManager.PERMISSION_GRANTED) {

                startSpeedometer();

            } else {

                Toast.makeText(
                        this,
                        "GPS разрешение отклонено",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    private void startTimeoutCheck() {
        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                // Если GPS молчит больше 3 секунд — обнуляем
                if (SystemClock.elapsedRealtime() - lastLocationUpdate > 3000) {
                    finalSpeed = 0;
                }

                // Обновляем UI в любом случае
                runOnUiThread(() -> {
                    if (speedText != null) {
                        speedText.setText(((int) finalSpeed) + " км/ч");
                        speedText.setTextColor(Color.BLACK);
                    }
                });

                // Планируем следующую проверку
                timeoutHandler.postDelayed(this, 500);
            }
        };
        timeoutHandler.post(timeoutRunnable);
    }
    private void showFloatingButton() {

        if (!Settings.canDrawOverlays(this)) {

            Intent intent = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())
            );

            startActivity(intent);

            return;
        }

        startService(
                new Intent(this, FloatingButtonService.class)
        );
    }
}