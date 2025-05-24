package com.example.kursachh;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class CustomDialogFragment extends DialogFragment {

    // Фабричный метод для создания экземпляра диалога с параметрами
    public static CustomDialogFragment newInstance(String title, String message) {
        CustomDialogFragment fragment = new CustomDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Получаем параметры из аргументов
        String title = getArguments() != null ? getArguments().getString("title") : "Заголовок";
        String message = getArguments() != null ? getArguments().getString("message") : "Сообщение";

        // Создаем и возвращаем диалог
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("ОК", (dialog, which) -> dialog.dismiss())
                .create();
    }
}
