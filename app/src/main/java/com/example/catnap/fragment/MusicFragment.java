package com.example.catnap.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music, container, false);

        // Nút back
        ImageView btnBack = view.findViewById(R.id.btn_back_music);
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        rvSounds = view.findViewById(R.id.rv_sounds);
        rvSounds.setLayoutManager(new LinearLayoutManager(getContext()));

        // Data tạm hard-code
        soundList = new ArrayList<>();
        soundList.add(new SoundItem("Mèo Gừ Gừ", "Mèo", "45 phút", "pets", false));
        soundList.add(new SoundItem("Suối chảy róc rách", "Thiên nhiên", "60 phút", "water_drop", false));
        soundList.add(new SoundItem("White Noise", "Tập trung", "Loop", "waves", false));
        soundList.add(new SoundItem("Mưa đêm", "Thiên nhiên", "50 phút", "thunderstorm", false));
        soundList.add(new SoundItem("Rừng xào xạc", "Thư giãn", "55 phút", "forest", false));

        soundAdapter = new SoundAdapter(soundList, new SoundAdapter.OnSoundClickListener() {
            @Override
            public void onPlayClick(int position) {
                // Xử lý play/pause - tạm đổi trạng thái
                for (SoundItem item : soundList) item.isPlaying = false;
                soundList.get(position).isPlaying = true;
                soundAdapter.notifyDataSetChanged();

                // Hiển thị player bottom
                LinearLayout playerBottom = view.findViewById(R.id.player_bottom);
                playerBottom.setVisibility(View.VISIBLE);

                TextView tvTitle = view.findViewById(R.id.tv_player_title);
                tvTitle.setText(soundList.get(position).name);
            }

            @Override
            public void onPauseClick() {
                // Tắt player
                for (SoundItem item : soundList) item.isPlaying = false;
                soundAdapter.notifyDataSetChanged();

                LinearLayout playerBottom = view.findViewById(R.id.player_bottom);
                playerBottom.setVisibility(View.GONE);
            }
        });

        rvSounds.setAdapter(soundAdapter);

        return view;
    }

    // Model tạm
    public static class SoundItem {
        public String name;
        public String category;
        public String duration;
        public String icon;
        public boolean isPlaying;

        SoundItem(String name, String category, String duration, String icon, boolean isPlaying) {
            this.name = name;
            this.category = category;
            this.duration = duration;
            this.icon = icon;
            this.isPlaying = isPlaying;
        }
    }
}