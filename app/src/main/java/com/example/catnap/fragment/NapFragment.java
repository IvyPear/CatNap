package com.example.catnap.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.catnap.R;

public class NapFragment extends Fragment {

    private TextView tvTimer;
    private Button btnStartPause;
    private Button btn15, btn20, btn30;

    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 20 * 60 * 1000; // mặc định 20 phút
    private boolean isRunning = false;

    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nap, container, false);

        // Toolbar back
        ImageView btnBack = view.findViewById(R.id.btn_back_nap);
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        tvTimer = view.findViewById(R.id.tv_timer);
        btnStartPause = view.findViewById(R.id.btn_start_pause);
        btn15 = view.findViewById(R.id.btn_15);
        btn20 = view.findViewById(R.id.btn_20);
        btn30 = view.findViewById(R.id.btn_30);

        // SharedPreferences để lưu thời gian ngủ bù
        prefs = requireContext().getSharedPreferences("CatNapPrefs", Context.MODE_PRIVATE);

        updateTimerText();

        // Nút chọn thời gian
        btn15.setOnClickListener(v -> setTime(15 * 60 * 1000));
        btn20.setOnClickListener(v -> setTime(20 * 60 * 1000));
        btn30.setOnClickListener(v -> setTime(30 * 60 * 1000));

        // Nút bắt đầu/tạm dừng
        btnStartPause.setOnClickListener(v -> {
            if (isRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        return view;
    }

    private void setTime(long millis) {
        timeLeftInMillis = millis;
        updateTimerText();
        pauseTimer(); // Dừng nếu đang chạy
        // Highlight nút
        btn15.setBackgroundResource(millis == 15*60*1000 ? R.drawable.btn_selected : R.drawable.btn_normal);
        btn20.setBackgroundResource(millis == 20*60*1000 ? R.drawable.btn_selected : R.drawable.btn_normal);
        btn30.setBackgroundResource(millis == 30*60*1000 ? R.drawable.btn_selected : R.drawable.btn_normal);
    }

    private void startTimer() {
        isRunning = true;
        btnStartPause.setText("Tạm dừng");
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                isRunning = false;
                btnStartPause.setText("Bắt đầu ngủ");
                Toast.makeText(requireContext(), "Ngủ bù xong rồi, sen ơi! 🌙", Toast.LENGTH_LONG).show();
                // Lưu thời gian ngủ bù (tạm)
                prefs.edit().putLong("last_nap_time", System.currentTimeMillis()).apply();
                // Có thể thêm rung hoặc âm báo thức nhẹ ở đây
            }
        }.start();
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isRunning = false;
        btnStartPause.setText("Bắt đầu ngủ");
    }

    private void updateTimerText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        tvTimer.setText(String.format("%02d:%02d", minutes, seconds));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        pauseTimer();
    }
}