package com.example.catnap.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.catnap.R;
import com.github.lzyzsd.circleprogress.CircleProgress;

public class ProfileFragment extends Fragment {

    private CircleProgress circleProgress;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        circleProgress = view.findViewById(R.id.circle_progress);

        // Tạm set XP
        circleProgress.setProgress(85); // 85/100 XP

        return view;
    }
}