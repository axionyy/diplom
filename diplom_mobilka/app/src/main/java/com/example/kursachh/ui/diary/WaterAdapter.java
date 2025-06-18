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

public class WaterAdapter extends RecyclerView.Adapter<WaterAdapter.WaterViewHolder> {
    private List<EatingRecord> waterRecords;
    private OnDeleteClickListener onDeleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(EatingRecord record);
    }

    public WaterAdapter(List<EatingRecord> waterRecords) {
        this.waterRecords = waterRecords;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    @NonNull
    @Override
    public WaterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_water, parent, false);
        return new WaterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WaterViewHolder holder, int position) {
        EatingRecord record = waterRecords.get(position);
        holder.amountText.setText(String.format("%d мл", (int)record.getQuantity()));
        holder.dateText.setText(record.getDate());

        holder.btnDelete.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(record);
            }
        });
    }

    @Override
    public int getItemCount() {
        return waterRecords.size();
    }

    public void updateData(List<EatingRecord> newRecords) {
        waterRecords = newRecords;
        notifyDataSetChanged();
    }

    static class WaterViewHolder extends RecyclerView.ViewHolder {
        TextView amountText;
        TextView dateText;
        ImageButton btnDelete;

        public WaterViewHolder(View itemView) {
            super(itemView);
            amountText = itemView.findViewById(R.id.waterAmount);
            dateText = itemView.findViewById(R.id.waterTime);
            btnDelete = itemView.findViewById(R.id.btnDeleteWater);
        }
    }
}