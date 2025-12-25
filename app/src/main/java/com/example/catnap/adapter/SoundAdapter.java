package com.example.catnap.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.catnap.R;
import com.example.catnap.fragment.MusicFragment.SoundItem;

import java.util.List;

public class SoundAdapter extends RecyclerView.Adapter<SoundAdapter.SoundVH> {

    private final List<SoundItem> sounds;
    private final OnSoundClickListener listener;

    public interface OnSoundClickListener {
        void onPlayClick(int position);
        void onPauseClick();
    }

    public SoundAdapter(List<SoundItem> sounds, OnSoundClickListener listener) {
        this.sounds = sounds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SoundVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sound, parent, false);
        return new SoundVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SoundVH holder, int position) {
        SoundItem sound = sounds.get(position);

        holder.tvName.setText(sound.name);
        holder.tvCategory.setText(sound.isPlaying ? "Đang phát • " + sound.duration : sound.category);
        holder.tvIcon.setText(sound.icon); // Material Symbol (text)

        holder.btnPlayPause.setImageResource(sound.isPlaying ? R.drawable.ic_pause : R.drawable.ic_play_arrow);

        holder.itemView.setOnClickListener(v -> {
            if (sound.isPlaying) {
                listener.onPauseClick();
            } else {
                listener.onPlayClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sounds.size();
    }

    static class SoundVH extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvIcon;
        ImageView btnPlayPause;

        SoundVH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_sound_name);
            tvCategory = itemView.findViewById(R.id.tv_sound_category);
            tvIcon = itemView.findViewById(R.id.tv_sound_icon);
            btnPlayPause = itemView.findViewById(R.id.btn_play_pause_sound);
        }
    }
}