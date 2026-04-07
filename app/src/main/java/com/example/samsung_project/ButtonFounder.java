package com.example.samsung_project;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class ButtonFounder {
    private static final String TAG = "ButtonFounder";

    public static boolean openApp(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();

        // Попытка получить стандартный launch intent
        Intent launchIntent = pm.getLaunchIntentForPackage(packageName);

        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(launchIntent);
                Log.i(TAG, "Started app by launch intent: " + packageName);
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error starting launch intent", e);
                Toast.makeText(context, "Не удалось запустить приложение: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        // Если launchIntent == null — попробуем найти лаунчер-активности вручную
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent.setPackage(packageName);

        List<ResolveInfo> list = pm.queryIntentActivities(mainIntent, 0);

        if (list != null && !list.isEmpty()) {
            ResolveInfo ri = list.get(0);
            String className = ri.activityInfo.name;
            Intent explicit = new Intent();
            explicit.setClassName(packageName, className);
            explicit.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(explicit);
                Log.i(TAG, "Started app by explicit activity: " + packageName + "/" + className);
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error starting explicit activity", e);
                Toast.makeText(context, "Не удалось запустить Activity: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        // Ничего не найдено
        Log.w(TAG, "App not found or has no launcher activity: " + packageName);
        Toast.makeText(context, "Приложение не найдено", Toast.LENGTH_SHORT).show();
        return false;
    }
}