package com.example.samsung_project;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.samsung_project.databinding.FragmentScreenBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class HomeScreen extends Fragment {

    FragmentScreenBinding binding;

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
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentScreenBinding.inflate(inflater, container, false);

        // ================================
        // GPS INIT
        // ================================
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());



        // Привязываем кнопки из layout
        buttons[0] = binding.button1;
        buttons[1] = binding.button2;
        buttons[2] = binding.button3;
        buttons[3] = binding.button4;
        buttons[4] = binding.button5;
        buttons[5] = binding.button6;

        // Обновляем текст всех кнопок при запуске
        updateAllButtons();

        // Назначаем обработчик на каждую кнопку
        for (int i = 0; i < 6; i++) {
            final int index = i;

            buttons[i].setOnClickListener(v -> {
                String savedPkg = requireActivity()
                        .getSharedPreferences("prefs", Context.MODE_PRIVATE)
                        .getString("saved_app_" + index, null);

                if (savedPkg == null) {
                    Intent intent = new Intent(getActivity(), AppChooserActivity.class);
                    intent.putExtra("button_index", index);
                    startActivity(intent);
                } else {
                    showFloatingButton();
                    ButtonFounder.openApp(requireContext(), savedPkg);
                }
            });

            // Долгое нажатие
            buttons[i].setOnLongClickListener(v -> {
                showLongPressMenu(index);
                return true;
            });
        }

        // Показывает сохранённый виджет
        int widgetType = requireActivity()
                .getSharedPreferences("prefs", Context.MODE_PRIVATE)
                .getInt("widget_type", WIDGET_TIME);
        showWidget(widgetType);


        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        requireActivity().stopService(
                new Intent(requireActivity(), FloatingButtonService.class)
        );

        updateAllButtons();

        int widgetType = requireActivity()
                .getSharedPreferences("prefs", Context.MODE_PRIVATE)
                .getInt("widget_type", WIDGET_TIME);

        if (widgetType == WIDGET_SPEED) {
            startSpeedometer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopSpeedometer();
    }

    // Метод обновляет текст всех кнопок
    private void updateAllButtons() {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("prefs", Context.MODE_PRIVATE);
        for (int i = 0; i < 6; i++) {
            String appName = prefs.getString("saved_app_name_" + i, null);
            if (appName != null) {
                buttons[i].setText(appName);
            } else {
                buttons[i].setText("Кнопка " + (i + 1));
            }
        }
    }

    private void showLongPressMenu(int index) {
        String[] options = {"Переименовать", "Сбросить"};
        new AlertDialog.Builder(requireActivity())
                .setTitle("Настройки кнопки")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showRenameDialog(index);
                    }
                    if (which == 1) {
                        requireActivity()
                                .getSharedPreferences("prefs", Context.MODE_PRIVATE)
                                .edit()
                                .remove("saved_app_" + index)
                                .remove("saved_app_name_" + index)
                                .apply();
                        updateAllButtons();
                        Toast.makeText(requireContext(), "Кнопка сброшена", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    // Диалог переименования
    private void showRenameDialog(int index) {
        EditText input = new EditText(requireContext());
        input.setHint("Введите название");

        new AlertDialog.Builder(requireActivity())
                .setTitle("Новое имя кнопки")
                .setView(input)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String newName = input.getText().toString();
                    if (!newName.isEmpty()) {
                        requireActivity()
                                .getSharedPreferences("prefs", Context.MODE_PRIVATE)
                                .edit()
                                .putString("saved_app_name_" + index, newName)
                                .apply();
                        updateAllButtons();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    // Показывает выбранный виджет
    private void showWidget(int type) {
        FrameLayout container = binding.widgetContainer;
        container.removeAllViews();

        if (type == WIDGET_TIME) {
            TextView clock = new TextView(requireContext());
            clock.setTextSize(32);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    java.text.DateFormat timeFormat =
                            java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT);
                    clock.setText(timeFormat.format(new java.util.Date()));
                    new Handler().postDelayed(this, 1000);
                }
            }, 0);
            container.addView(clock);
        }

        if (type == WIDGET_SPEED) {
            speedText = new TextView(requireContext());
            speedText.setTextSize(64);
            speedText.setTypeface(Typeface.DEFAULT_BOLD);
            speedText.setText("0 км/ч");
            container.addView(speedText);
            startSpeedometer();
        }

        if (type == WIDGET_IMAGE) {
            ImageView img = new ImageView(requireContext());
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
        new AlertDialog.Builder(requireActivity())
                .setTitle("Выбери виджет")
                .setItems(options, (dialog, which) -> {
                    requireActivity()
                            .getSharedPreferences("prefs", Context.MODE_PRIVATE)
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

        if (ActivityCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    100);
            return;
        }

        LocationRequest request = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 500)
                .setMinUpdateIntervalMillis(300)
                .setMinUpdateDistanceMeters(1)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                Location location = result.getLastLocation();
                if (location == null) return;
                lastLocationUpdate = SystemClock.elapsedRealtime();
                float speedKmh = location.getSpeed() * 3.6f;
                if (speedKmh < 2.0f) {
                    finalSpeed = 0;
                } else {
                    finalSpeed = speedKmh;
                }
                requireActivity().runOnUiThread(() -> {
                    if (speedText != null) {
                        speedText.setText(((int) finalSpeed) + " км/ч");
                        if (finalSpeed > 120) {
                            speedText.setTextColor(Color.RED);
                        } else if (finalSpeed > 60) {
                            speedText.setTextColor(Color.YELLOW);
                        } else {
                            speedText.setTextColor(Color.WHITE);
                        }
                    }
                });
            }
        };

        fusedLocationClient.requestLocationUpdates(
                request, locationCallback, Looper.getMainLooper());
        startTimeoutCheck();
    }

    private void stopSpeedometer() {
        if (!gpsStarted) return;
        gpsStarted = false;
        if (timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            locationCallback = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSpeedometer();
            } else {
                Toast.makeText(requireContext(),
                        "GPS разрешение отклонено", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startTimeoutCheck() {
        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                if (SystemClock.elapsedRealtime() - lastLocationUpdate > 3000) {
                    finalSpeed = 0;
                }
                requireActivity().runOnUiThread(() -> {
                    if (speedText != null) {
                        speedText.setText(((int) finalSpeed) + " км/ч");
                        speedText.setTextColor(Color.WHITE);
                    }
                });
                timeoutHandler.postDelayed(this, 500);
            }
        };
        timeoutHandler.post(timeoutRunnable);
    }

    private void showFloatingButton() {
        if (!Settings.canDrawOverlays(requireContext())) {
            Intent intent = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + requireActivity().getPackageName()));
            startActivity(intent);
            return;
        }
        requireActivity().startService(
                new Intent(requireActivity(), FloatingButtonService.class));
    }
}