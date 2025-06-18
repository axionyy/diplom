package com.example.kursachh.ui.diary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kursachh.R;
import java.util.List;

import Model.FoodItem;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {
    private List<FoodItem> foodItems;

    public FoodAdapter(List<FoodItem> foodItems) {
        this.foodItems = foodItems;
    }

    @Override
    public FoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FoodViewHolder holder, int position) {
        FoodItem item = foodItems.get(position);
        holder.nameText.setText(item.getNameFood());
        holder.caloriesText.setText(String.format("%.1f ккал", item.getCallories()));
    }

    @Override
    public int getItemCount() {
        return foodItems.size();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView caloriesText;

        public FoodViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.foodName);
            caloriesText = itemView.findViewById(R.id.foodCalories);
        }
    }
}