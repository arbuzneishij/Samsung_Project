package com.example.samsung_project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

class AppAdapter extends ArrayAdapter<AppChooserActivity.AppItem> {

    public AppAdapter(AppCompatActivity context, List<AppChooserActivity.AppItem> apps) {
        super(context, 0, apps);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        // Если View ещё не создан — создаём
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_app, parent, false);
        }

        // Получаем текущий элемент
        AppChooserActivity.AppItem item = getItem(position);

        ImageView icon = convertView.findViewById(R.id.appIcon);
        TextView name = convertView.findViewById(R.id.appName);

        // Заполняем данные
        if (item != null) {
            icon.setImageDrawable(item.icon);
            name.setText(item.label);
        }

        return convertView;
    }
}