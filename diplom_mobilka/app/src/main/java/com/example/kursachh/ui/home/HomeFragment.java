package com.example.kursachh.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.kursachh.CustomDialogFragment;
import com.example.kursachh.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textrecepts;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        binding.resept1.setOnClickListener(v -> showCustomDialog());
        binding.resept2.setOnClickListener(v -> showCustomDialog());
        binding.resept3.setOnClickListener(v -> showCustomDialog());

        return root;
    }

    public void showCustomDialog()  {
        CustomDialogFragment dialogFragment = CustomDialogFragment.newInstance("Рецепт", "Подробное приготовление, КБЖУ");
        dialogFragment.show(getParentFragmentManager(), "customDialog");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
