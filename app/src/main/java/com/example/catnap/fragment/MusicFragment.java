package com.example.catnap.fragment;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.catnap.R;
import com.example.catnap.adapter.SoundAdapter;

import java.util.ArrayList;
import java.util.List;

public class MusicFragment extends Fragment {

    private RecyclerView rvSounds;
    private SoundAdapter soundAdapter;
    private List<SoundItem> soundList;
    private LinearLayout playerBottom;
    private TextView tvPlayerTitle, tvPlayerStatus;
    private ImageView btnPlayPause, btnStop;

    private MediaPlayer mediaPlayer;
    private int currentPlayingPosition = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music, container, false);

        // Nút back
        ImageView btnBack = view.findViewById(R.id.btn_back_music);
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        rvSounds = view.findViewById(R.id.rv_sounds);
        playerBottom = view.findViewById(R.id.player_bottom);
        tvPlayerTitle = view.findViewById(R.id.tv_player_title);
        tvPlayerStatus = view.findViewById(R.id.tv_player_status);
        btnPlayPause = view.findViewById(R.id.btn_play_pause);
        btnStop = view.findViewById(R.id.btn_stop);

        rvSounds.setLayoutManager(new LinearLayoutManager(getContext()));

        // Danh sách âm thanh (thêm raw resource)
        soundList = new ArrayList<>();
        soundList.add(new SoundItem("Mèo Gừ Gừ", "Mèo", "45 phút", R.raw.cat_purr, "pets", false));
//        soundList.add(new SoundItem("Suối chảy róc rách", "Thiên nhiên", "60 phút", R.raw.stream, "water_drop", false));
        soundList.add(new SoundItem("White Noise", "Tập trung", "Loop", R.raw.white_noise, "waves", false));
        soundList.add(new SoundItem("Mưa đêm", "Thiên nhiên", "50 phút", R.raw.rain, "thunderstorm", false));
//        soundList.add(new SoundItem("Rừng xào xạc", "Thư giãn", "55 phút", R.raw.forest, "forest", false));

        soundAdapter = new SoundAdapter(soundList, new SoundAdapter.OnSoundClickListener() {
            @Override
            public void onPlayClick(int position) {
                playSound(position);
            }

            @Override
            public void onPauseClick() {
                pauseSound();
            }
        });

        rvSounds.setAdapter(soundAdapter);

        // Player controls
        btnPlayPause.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    pauseSound();
                } else {
                    mediaPlayer.start();
                    btnPlayPause.setImageResource(R.drawable.ic_pause);
                    tvPlayerStatus.setText("Đang phát...");
                }
            }
        });

        btnStop.setOnClickListener(v -> stopSound());

        return view;
    }

    private void playSound(int position) {
        stopSound(); // Dừng nếu đang phát

        SoundItem sound = soundList.get(position);
        mediaPlayer = MediaPlayer.create(requireContext(), sound.rawResId);
        mediaPlayer.setOnCompletionListener(mp -> stopSound());

        mediaPlayer.start();

        // Update UI
        currentPlayingPosition = position;
        for (SoundItem item : soundList) item.isPlaying = false;
        sound.isPlaying = true;
        soundAdapter.notifyDataSetChanged();

        playerBottom.setVisibility(View.VISIBLE);
        tvPlayerTitle.setText(sound.name);
        tvPlayerStatus.setText("Đang phát...");
        btnPlayPause.setImageResource(R.drawable.ic_pause);
    }

    private void pauseSound() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btnPlayPause.setImageResource(R.drawable.ic_play_arrow);
            tvPlayerStatus.setText("Tạm dừng");
        }
    }

    private void stopSound() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        for (SoundItem item : soundList) item.isPlaying = false;
        soundAdapter.notifyDataSetChanged();

        playerBottom.setVisibility(View.GONE);
        currentPlayingPosition = -1;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopSound();
    }

    // Model
    public static class SoundItem {
        public String name;
        public String category;
        public String duration;
        public int rawResId;
        public String icon;
        public boolean isPlaying;

        public SoundItem(String name, String category, String duration, int rawResId, String icon, boolean isPlaying) {
            this.name = name;
            this.category = category;
            this.duration = duration;
            this.rawResId = rawResId;
            this.icon = icon;
            this.isPlaying = isPlaying;
        }
    }
}