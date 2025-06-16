package com.example.kursachh.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.kursachh.AuthManager;
import com.example.kursachh.DataManager;
import com.example.kursachh.MainActivity;
import com.example.kursachh.R;

import java.io.IOException;

import Interface.IUser;
import Model.User;
import RetrofitModels.RetroFit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private static final int EDIT_PROFILE_REQUEST = 1;
    private AuthManager authManager;
    private DataManager dataManager;
    private ProfileViewModel mViewModel;
    private Button settingsButton;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authManager = new AuthManager(requireContext());
        dataManager = new DataManager(requireContext());

        ImageView imageView = view.findViewById(R.id.exitImageButton);
        imageView.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show();
            authManager.setLoggedIn(false);
            dataManager.saveData(null);
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        Button buttonRefactorProfile = view.findViewById(R.id.refactorProfileButton);
        buttonRefactorProfile.setOnClickListener(v -> {
            int userId = authManager.getUserId();
            if (userId > 0) {
                Intent intent = new Intent(getActivity(), RefactorYourProfile.class);
                intent.putExtra("user_id", userId);
                startActivityForResult(intent, EDIT_PROFILE_REQUEST);
            }
        });

        Button buttonRefactorWeight = view.findViewById(R.id.refactorWeightUserButton);
        buttonRefactorWeight.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RefactorYourWeight.class);
            startActivity(intent);
        });

        Button buttonYourReseps = view.findViewById(R.id.resepsUsersButton);
        buttonYourReseps.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), YourReseps.class);
            startActivity(intent);
        });

        loadUserData();

        // Обработчик кнопки настроек
        settingsButton = view.findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> {
            try {
                Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(settingsIntent);
            } catch (Exception e) {
                Log.e("ProfileFragment", "Error starting SettingsActivity", e);
                Toast.makeText(getContext(), "Ошибка открытия настроек", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_PROFILE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null && data.hasExtra("updated_user")) {
                User updatedUser = (User) data.getSerializableExtra("updated_user");
                if (updatedUser != null) {
                    // Обновляем UI и сохраняем данные
                    updateProfileUI(updatedUser);
                    dataManager.saveData(updatedUser);
                }
            }
            // Всегда обновляем данные с сервера для актуальности
            loadUserData();
        }
    }

    private void loadUserData() {
        int userId = authManager.getUserId();
        if (userId <= 0) {
            // Заменяем handleInvalidUser() на конкретную реализацию
            Log.e("ProfileFragment", "Invalid user ID");
            Toast.makeText(getContext(), "Ошибка авторизации", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), MainActivity.class));
            requireActivity().finish();
            return;
        }

        // Показываем сохраненные данные, если они есть
        User savedUser = dataManager.getData();
        if (savedUser != null) {
            updateProfileUI(savedUser);
        }

        // Загружаем свежие данные с сервера
        IUser userService = RetroFit.getClient().create(IUser.class);
        userService.getUser(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    updateProfileUI(user);
                    dataManager.saveData(user);
                } else {
                    // Заменяем handleErrorResponse(response) на конкретную реализацию
                    Log.e("ProfileFragment", "Failed to fetch user: " + response.code());
                    try {
                        Log.e("ProfileFragment", "Error body: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("ProfileFragment", "Network error", t);
            }
        });
    }

    private void updateProfileUI(User user) {
        View view = getView();
        if (view == null) return;

        TextView loginUser = view.findViewById(R.id.loginUser);
        TextView nameUser = view.findViewById(R.id.nameUser);
        TextView birthdayUser = view.findViewById(R.id.birthdayUser);

        if (loginUser != null && nameUser != null && birthdayUser != null) {
            loginUser.setText(user.login != null ? user.login : "");
            nameUser.setText(user.name != null ? user.name : "");

            // Форматирование даты рождения (если нужно)
            if (user.birthday != null) {
                try {
                    String[] dateParts = user.birthday.split("-");
                    if (dateParts.length == 3) {
                        birthdayUser.setText(String.format("%s.%s.%s",
                                dateParts[2], dateParts[1], dateParts[0]));
                    } else {
                        birthdayUser.setText(user.birthday);
                    }
                } catch (Exception e) {
                    birthdayUser.setText(user.birthday);
                }
            } else {
                birthdayUser.setText("");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Обновляем данные при каждом возвращении на фрагмент
        loadUserData();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }
}