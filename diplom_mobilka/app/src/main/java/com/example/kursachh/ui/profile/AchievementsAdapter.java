package com.example.kursachh.ui.profile;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kursachh.R;

import java.util.List;

import Model.Achievement;

public class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.ViewHolder> {
    private List<Achievement> achievements;
    private int userId;

    public AchievementsAdapter(List<Achievement> achievements, int userId) {
        this.achievements = achievements;
        this.userId = userId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_achievement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Achievement achievement = achievements.get(position);
        Context context = holder.itemView.getContext();

        SharedPreferences prefs = context.getSharedPreferences(
                "user_" + userId + "_achievements", MODE_PRIVATE);

        int progress = prefs.getInt(achievement.getId(), 0);
        int target = achievement.getTarget();

        holder.title.setText(achievement.getTitle());
        holder.description.setText(achievement.getDescription());
        holder.progressBar.setMax(target);
        holder.progressBar.setProgress(progress);
        holder.image.setImageResource(achievement.getImageResId());

        if (progress >= target) {
            holder.itemView.setAlpha(1f);
            holder.lockIcon.setVisibility(View.GONE);
        } else {
            holder.itemView.setAlpha(0.6f);
            holder.lockIcon.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return achievements.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView description;
        ProgressBar progressBar;
        ImageView lockIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.achievement_image);
            title = itemView.findViewById(R.id.achievement_title);
            description = itemView.findViewById(R.id.achievement_description);
            progressBar = itemView.findViewById(R.id.progressBar);
            lockIcon = itemView.findViewById(R.id.achievement_lock);
        }
    }
}
