package com.example.kursachh.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.kursachh.DataManager;
import com.example.kursachh.AuthManager;
import com.example.kursachh.MainActivity;
import com.example.kursachh.R;

public class ProfileFragment extends Fragment {

    private AuthManager authManager;
    private DataManager dataManager;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    private ProfileViewModel mViewModel;

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
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        // TODO: Use the ViewModel
    }

}
