package com.example.samsung_project;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.samsung_project.databinding.FragmentButtonsBinding;


public class Buttons extends Fragment {
    FragmentButtonsBinding binding;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentButtonsBinding.inflate(getLayoutInflater());

        binding.btnWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });

        binding.btnBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
            }
        });

        binding.btnApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_ALL_APPS);

                startActivity(intent);

            }
        });

        binding.btnMobileInternet.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_DATA_USAGE_SETTINGS));
            }
        });

        binding.btnSettings.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.S)
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), SettingsActivity.class));
            }
        });


        return binding.getRoot();


    }

}

