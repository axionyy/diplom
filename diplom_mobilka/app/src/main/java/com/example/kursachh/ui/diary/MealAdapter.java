package com.example.kursachh.ui.diary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kursachh.R;

import java.util.List;

import Model.EatingRecord;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {
    private List<EatingRecord> mealRecords;
    private OnDeleteClickListener onDeleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(EatingRecord record);
    }

    public MealAdapter(List<EatingRecord> mealRecords) {
        this.mealRecords = mealRecords;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        EatingRecord record = mealRecords.get(position);

        holder.foodNameText.setText(record.getFoodName());
        holder.caloriesText.setText(String.format("%.1f ккал", record.getCallories()));
        holder.proteinsText.setText(String.format("Б: %.1fг", record.getProteins()));
        holder.fatsText.setText(String.format("Ж: %.1fг", record.getFats()));
        holder.carbsText.setText(String.format("У: %.1fг", record.getCarbohydrates()));
        holder.quantityText.setText(String.format("%.1f г", record.getQuantity()));

        holder.btnDelete.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(record);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mealRecords.size();
    }

    public void updateData(List<EatingRecord> newRecords) {
        mealRecords = newRecords;
        notifyDataSetChanged();
    }

    static class MealViewHolder extends RecyclerView.ViewHolder {
        TextView foodNameText;
        TextView caloriesText;
        TextView proteinsText;
        TextView fatsText;
        TextView carbsText;
        TextView quantityText;
        ImageButton btnDelete;

        public MealViewHolder(View itemView) {
            super(itemView);
            foodNameText = itemView.findViewById(R.id.foodName);
            caloriesText = itemView.findViewById(R.id.foodCalories);
            proteinsText = itemView.findViewById(R.id.foodProteins);
            fatsText = itemView.findViewById(R.id.foodFats);
            carbsText = itemView.findViewById(R.id.foodCarbs);
            quantityText = itemView.findViewById(R.id.foodQuantity);
            btnDelete = itemView.findViewById(R.id.btnDeleteMeal);
        }
    }
}