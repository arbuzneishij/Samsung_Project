package com.example.samsung_project;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ListView;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AppChooserActivity extends AppCompatActivity {

    // Класс модели приложения (что храним для каждого приложения)
    static class AppItem {
        String label;        // название приложения
        String packageName;  // package name (уникальный идентификатор)
        Drawable icon;       // иконка приложения



        AppItem(String label, String packageName, Drawable icon) {
            this.label = label;
            this.packageName = packageName;
            this.icon = icon;
        }

        @NonNull
        @Override
        public String toString() {
            return label; // отображается в списке
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Создаём ListView программно
        ListView listView = new ListView(this);
        setContentView(listView);

        PackageManager pm = getPackageManager();

        // Получаем список всех установленных приложений
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);

        List<AppItem> items = new ArrayList<>();

        for (ApplicationInfo app : apps) {

            // Проверяем: можно ли запустить приложение
            Intent launchIntent = pm.getLaunchIntentForPackage(app.packageName);
            if (launchIntent == null) continue;

            // Получаем название приложения
            String label = pm.getApplicationLabel(app).toString();

            // Получаем иконку
            Drawable icon;
            try {
                icon = pm.getApplicationIcon(app.packageName);
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }

            // Добавляем в список
            items.add(new AppItem(label, app.packageName, icon));
        }

        // Сортируем приложения по имени (по алфавиту)
        items.sort(Comparator.comparing(a -> a.label.toLowerCase()));

        // Устанавливаем адаптер
        AppAdapter adapter = new AppAdapter(this, items);
        listView.setAdapter(adapter);

        // получаем индекс кнопки
        int buttonIndex = getIntent().getIntExtra("button_index", -1);

        // Обработка нажатия на элемент списка
        listView.setOnItemClickListener((parent, view, position, id) -> {

            AppItem selected = items.get(position);

            getSharedPreferences("prefs", MODE_PRIVATE)
                    .edit()
                    .putString("saved_app_" + buttonIndex, selected.packageName)
                    .putString("saved_app_name_" + buttonIndex, selected.label)
                    .apply();

            finish();
        });
    }
}