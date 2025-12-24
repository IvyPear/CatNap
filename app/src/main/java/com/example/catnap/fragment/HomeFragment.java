package com.example.catnap.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.catnap.R;
import com.example.catnap.utils.SleepTracker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private SleepTracker tracker;
    private TextView tvDate, tvGreeting, tvStreak, tvSleepDebt, tvSleepTime, tvWakeTime, tvSleepDuration, tvCurrentTime, tvTip;
    private ImageView imgCatGood, imgCatLight, imgCatSevere;  // 3 avatar

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable clockRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tracker = new SleepTracker(requireContext());

        // Tìm TextView
        tvDate = view.findViewById(R.id.tv_date);
        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvStreak = view.findViewById(R.id.tv_streak);
        tvSleepDebt = view.findViewById(R.id.tv_sleep_debt);
        tvSleepTime = view.findViewById(R.id.tv_sleep_time);
        tvWakeTime = view.findViewById(R.id.tv_wake_time);
        tvSleepDuration = view.findViewById(R.id.tv_sleep_duration);
        tvCurrentTime = view.findViewById(R.id.tv_current_time);
        tvTip = view.findViewById(R.id.tv_tip);

        // Tìm 3 avatar (thêm vào XML của bạn)
        imgCatGood = view.findViewById(R.id.img_cat_good);
        imgCatLight = view.findViewById(R.id.img_cat_light);
        imgCatSevere = view.findViewById(R.id.img_cat_severe);

        CardView btnStartSleep = view.findViewById(R.id.btn_start_sleep);
        CardView btnWakeUp = view.findViewById(R.id.btn_wake_up);
        CardView btnNap = view.findViewById(R.id.btn_nap);
        CardView btnBreathing = view.findViewById(R.id.btn_breathing);

        // Đồng hồ live
        clockRunnable = new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                tvCurrentTime.setText(sdf.format(new Date()));
                handler.postDelayed(this, 60000);
            }
        };
        handler.post(clockRunnable);

        showDailyTip();

        updateHomeData();

        // Event 4 nút
        btnStartSleep.setOnClickListener(v -> {
            tracker.saveSleepTime(new Date());
            updateHomeData();
            Toast.makeText(requireContext(), "Đã lưu giờ đi ngủ! Ngủ ngon nhé 😴", Toast.LENGTH_SHORT).show();
        });

        btnWakeUp.setOnClickListener(v -> {
            tracker.saveWakeTime(new Date());
            updateHomeData();
            Toast.makeText(requireContext(), "Dậy thôi! Hôm nay ngủ ngon lắm nha 🌞", Toast.LENGTH_SHORT).show();
        });

        btnNap.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new NapFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnBreathing.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ExerciseFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void updateHomeData() {
        Calendar cal = Calendar.getInstance();

        tvDate.setText(new SimpleDateFormat("EEEE, dd/MM", Locale.getDefault()).format(cal.getTime()));
        tvGreeting.setText(tracker.getGreetingText());

        tvStreak.setText(tracker.getCurrentStreak() + " ngày liên tiếp");
        tvSleepDebt.setText(tracker.getSleepDebtText());
        tvSleepTime.setText(tracker.getLastSleepTimeText());
        tvWakeTime.setText(tracker.getLastWakeTimeText());

        long durationMs = tracker.getSleepDurationToday();
        if (durationMs == 0) {
            tvSleepDuration.setText("Chưa ngủ");
        } else {
            long hours = durationMs / (60 * 60 * 1000);
            long minutes = (durationMs % (60 * 60 * 1000)) / (60 * 1000);
            tvSleepDuration.setText(hours + "h " + minutes + "m");
        }

        // Hiển thị avatar phù hợp
        showSleepAvatar(durationMs);
    }

    private void showSleepAvatar(long durationMs) {
        float hours = durationMs / (60f * 60 * 1000);

        // Ẩn hết trước
        imgCatGood.setVisibility(View.GONE);
        imgCatLight.setVisibility(View.GONE);
        imgCatSevere.setVisibility(View.GONE);

        if (durationMs == 0) {
            // Chưa ngủ: Có thể hiển thị avatar buồn hoặc mặc định
            imgCatSevere.setVisibility(View.VISIBLE);
        } else if (hours >= 7.5) {
            imgCatGood.setVisibility(View.VISIBLE);  // Ngủ tốt
        } else if (hours >= 5) {
            imgCatLight.setVisibility(View.VISIBLE);  // Thiếu nhẹ
        } else {
            imgCatSevere.setVisibility(View.VISIBLE);  // Thiếu trầm trọng
        }
    }

    private void showDailyTip() {
        String[] tips = {
                "Ngủ trước 23h giúp cải thiện chất lượng giấc ngủ đáng kể!",
                "Tắt điện thoại 30 phút trước khi ngủ để não thư giãn.",
                "Uống một ly nước ấm + mật ong giúp ngủ ngon hơn.",
                "Thử hít thở 4-7-8: hít 4s, giữ 7s, thở 8s.",
                "Nghe tiếng mưa rơi hoặc mèo gừ gừ để dễ chìm vào giấc ngủ.",
                "Giữ phòng ngủ mát mẻ (18-22°C) là bí quyết ngủ sâu.",
                "Tránh cà phê sau 14h để không bị khó ngủ.",
                "Viết nhật ký 5 phút trước khi ngủ giúp xả stress.",
                "Tập yoga nhẹ 10 phút trước giờ ngủ cực kỳ hiệu quả.",
                "Ngủ đủ 7-9h mỗi ngày giúp bạn khỏe mạnh hơn!"
        };

        Calendar cal = Calendar.getInstance();
        int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        int index = dayOfYear % tips.length;

        if (tvTip != null) {
            tvTip.setText(tips[index]);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(clockRunnable);
    }
}