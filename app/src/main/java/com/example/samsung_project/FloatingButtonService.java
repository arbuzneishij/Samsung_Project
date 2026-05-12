package com.example.samsung_project;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;

public class FloatingButtonService extends Service {

    private WindowManager windowManager;
    private ImageView floatingButton;

    private WindowManager.LayoutParams params;

    private int initialX;
    private int initialY;

    private float initialTouchX;
    private float initialTouchY;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        floatingButton = new ImageView(this);

        floatingButton.setImageResource(android.R.drawable.ic_menu_revert);

        int layoutFlag;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        params = new WindowManager.LayoutParams(
                140,
                140,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 300;

        windowManager.addView(floatingButton, params);

        floatingButton.setOnTouchListener(new View.OnTouchListener() {

            private long pressTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:

                        pressTime = System.currentTimeMillis();

                        initialX = params.x;
                        initialY = params.y;

                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();

                        return true;

                    case MotionEvent.ACTION_MOVE:

                        params.x = initialX +
                                (int) (event.getRawX() - initialTouchX);

                        params.y = initialY +
                                (int) (event.getRawY() - initialTouchY);

                        windowManager.updateViewLayout(floatingButton, params);

                        return true;

                    case MotionEvent.ACTION_UP:

                        long clickDuration =
                                System.currentTimeMillis() - pressTime;

                        // Если это клик, а не перетаскивание
                        if (clickDuration < 200) {

                            v.performClick();

                            Intent intent =
                                    new Intent(
                                            FloatingButtonService.this,
                                            MainActivity.class
                                    );

                            intent.addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK
                            );

                            startActivity(intent);
                        }

                        return true;
                }

                return false;
            }
        });

        // Важно для accessibility и lint
        floatingButton.setOnClickListener(v -> {
            // Уже обрабатывается через performClick()
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (floatingButton != null) {
            windowManager.removeView(floatingButton);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}