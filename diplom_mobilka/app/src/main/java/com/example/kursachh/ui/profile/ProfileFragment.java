package com.example.kursachh.ui.profile;

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

    private AuthManager authManager;
    private DataManager dataManager;
    private ProfileViewModel mViewModel;

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
            dataManager.saveData(null); // Очищаем данные пользователя
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        Button buttonRefactorProfile = view.findViewById(R.id.refactorProfileButton);
        buttonRefactorProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RefactorYourProfile.class);
            startActivity(intent);
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

        // Получаем ID пользователя из авторизации
        int userId = getUserIdFromAuth();

        if (userId != -1) {
            fetchUserData(userId);
        }
    }

    private int getUserIdFromAuth() {
        int userId = authManager.getUserId();
        if (userId <= 0) {
            Log.e("ProfileFragment", "Invalid user ID: " + userId);
            Toast.makeText(getContext(), "Ошибка авторизации", Toast.LENGTH_SHORT).show();
            // Перенаправляем на экран авторизации
            startActivity(new Intent(getActivity(), MainActivity.class));
            requireActivity().finish();
        }
        return userId;
    }

    private void fetchUserData(int userId) {
        Log.d("ProfileFragment", "Attempting to fetch data for user ID: " + userId);

        if (userId <= 0) {
            Log.e("ProfileFragment", "Invalid user ID, cannot fetch data");
            return;
        }

        IUser userService = RetroFit.getClient().create(IUser.class);
        Call<User> call = userService.getUser(userId);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    Log.d("ProfileFragment", "User data received: " + user.id);
                    updateProfileUI(user);
                } else {
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
        TextView loginUser = getView().findViewById(R.id.loginUser);
        TextView nameUser = getView().findViewById(R.id.nameUser);
        TextView birthdayUser = getView().findViewById(R.id.birthdayUser);

        if (loginUser != null && nameUser != null && birthdayUser != null) {
            loginUser.setText(user.login);
            nameUser.setText(user.name);
            birthdayUser.setText(user.birthday);
        } else {
            Log.e("ProfileFragment", "One or more TextViews are null");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        // TODO: Use the ViewModel
    }
}
