package com.example.catnap.fragment;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.catnap.R;

public class ExerciseFragment extends Fragment {

    private TextView tvPhase, tvTimeLeft;
    private View circleIn, circleHold, circleOut;
    private ImageView imgCatBreath;
    private Button btnPlayPause;
    private Button btn1m, btn3m, btn5m;

    private CountDownTimer phaseTimer;
    private ValueAnimator scaleAnimator;
    private String currentPhase = "In"; // In, Hold, Out
    private long totalTimeMillis = 3 * 60 * 1000; // mặc định 3 phút
    private long remainingTimeMillis = totalTimeMillis;
    private boolean isRunning = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise, container, false);

        // Toolbar back
        ImageView btnBack = view.findViewById(R.id.btn_back_exercise);
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        tvPhase = view.findViewById(R.id.tv_phase);
        tvTimeLeft = view.findViewById(R.id.tv_time_left);
        circleIn = view.findViewById(R.id.circle_in);
        circleHold = view.findViewById(R.id.circle_hold);
        circleOut = view.findViewById(R.id.circle_out);
        imgCatBreath = view.findViewById(R.id.img_cat_breath);
        btnPlayPause = view.findViewById(R.id.btn_play_pause);
        btn1m = view.findViewById(R.id.btn_1m);
        btn3m = view.findViewById(R.id.btn_3m);
        btn5m = view.findViewById(R.id.btn_5m);

        updateTimeLeftText();

        // Nút chọn thời lượng
        btn1m.setOnClickListener(v -> setTotalTime(1));
        btn3m.setOnClickListener(v -> setTotalTime(3));
        btn5m.setOnClickListener(v -> setTotalTime(5));

        // Nút play/pause
        btnPlayPause.setOnClickListener(v -> {
            if (isRunning) pauseBreathing();
            else startBreathing();
        });

        return view;
    }

    private void setTotalTime(int minutes) {
        totalTimeMillis = minutes * 60 * 1000L;
        remainingTimeMillis = totalTimeMillis;
        updateTimeLeftText();
        pauseBreathing();
        // Highlight nút
        btn1m.setBackgroundResource(minutes == 1 ? R.drawable.btn_selected : R.drawable.btn_normal);
        btn3m.setBackgroundResource(minutes == 3 ? R.drawable.btn_selected : R.drawable.btn_normal);
        btn5m.setBackgroundResource(minutes == 5 ? R.drawable.btn_selected : R.drawable.btn_normal);
    }

    private void startBreathing() {
        isRunning = true;
        btnPlayPause.setText("Pause");
        startPhase("In", 4); // 4 giây hít vào
    }

    private void pauseBreathing() {
        isRunning = false;
        btnPlayPause.setText("Play");
        if (phaseTimer != null) phaseTimer.cancel();
        if (scaleAnimator != null) scaleAnimator.cancel();
    }

    private void startPhase(String phase, int seconds) {
        currentPhase = phase;
        tvPhase.setText(phase.equals("In") ? "Hít vào..." : phase.equals("Hold") ? "Giữ hơi..." : "Thở ra...");

        // Animation scale
        float startScale = phase.equals("Out") ? 1.5f : 1.0f;
        float endScale = phase.equals("In") ? 1.5f : 1.0f;
        scaleAnimator = ValueAnimator.ofFloat(startScale, endScale);
        scaleAnimator.setDuration(seconds * 1000L);
        scaleAnimator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            imgCatBreath.setScaleX(scale);
            imgCatBreath.setScaleY(scale);
            circleIn.setScaleX(scale);
            circleIn.setScaleY(scale);
            circleHold.setScaleX(scale);
            circleHold.setScaleY(scale);
            circleOut.setScaleX(scale);
            circleOut.setScaleY(scale);
        });
        scaleAnimator.start();

        phaseTimer = new CountDownTimer(seconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Có thể cập nhật progress bar nếu cần
            }

            @Override
            public void onFinish() {
                if (phase.equals("In")) startPhase("Hold", 7);
                else if (phase.equals("Hold")) startPhase("Out", 8);
                else {
                    remainingTimeMillis -= 19000; // 4+7+8=19 giây một chu kỳ
                    updateTimeLeftText();
                    if (remainingTimeMillis > 0) startPhase("In", 4);
                    else {
                        pauseBreathing();
                        tvPhase.setText("Hoàn thành! 😺");
                    }
                }
            }
        }.start();
    }

    private void updateTimeLeftText() {
        int minutes = (int) (remainingTimeMillis / 60000);
        int seconds = (int) ((remainingTimeMillis % 60000) / 1000);
        tvTimeLeft.setText(String.format("%02d:%02d", minutes, seconds));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        pauseBreathing();
    }
}