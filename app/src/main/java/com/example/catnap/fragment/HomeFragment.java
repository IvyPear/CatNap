package com.example.catnap.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.catnap.R;
import com.example.catnap.utils.SleepTracker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class HomeFragment extends Fragment {

    private SleepTracker tracker;
    private TextView tvDate, tvGreeting, tvStreak, tvSleepDebt, tvSleepTime, tvWakeTime, tvSleepDuration, tvCurrentTime, tvTip;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable clockRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tracker = new SleepTracker(requireContext());

        tvDate = view.findViewById(R.id.tv_date);
        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvStreak = view.findViewById(R.id.tv_streak);
        tvSleepDebt = view.findViewById(R.id.tv_sleep_debt);
        tvSleepTime = view.findViewById(R.id.tv_sleep_time);
        tvWakeTime = view.findViewById(R.id.tv_wake_time);
        tvSleepDuration = view.findViewById(R.id.tv_sleep_duration);
        tvCurrentTime = view.findViewById(R.id.tv_current_time);
        tvTip = view.findViewById(R.id.tv_tip);

        LinearLayout btnStartSleep = view.findViewById(R.id.btn_start_sleep);
        LinearLayout btnWakeUp = view.findViewById(R.id.btn_wake_up);

        // Đồng hồ live
        clockRunnable = new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                tvCurrentTime.setText(sdf.format(new Date()));
                handler.postDelayed(this, 60000); // Cập nhật mỗi phút
            }
        };
        handler.post(clockRunnable);

        // Random mẹo mỗi ngày
        showDailyTip();

        // Cập nhật data thật
        updateHomeData();

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

        return view;
    }

    private void updateHomeData() {
        Calendar cal = Calendar.getInstance();

        // Ngày tháng
        SimpleDateFormat sdfDate = new SimpleDateFormat("EEEE, dd/MM", Locale.getDefault());
        tvDate.setText(sdfDate.format(cal.getTime()));

        // Chào buổi
        tvGreeting.setText(tracker.getGreetingText() + ", Sen");

        // Streak
        tvStreak.setText(tracker.getCurrentStreak() + " Ngày");

        // Sleep debt
        tvSleepDebt.setText(tracker.getSleepDebtText());

        // Giờ ngủ/dậy
        tvSleepTime.setText(tracker.getLastSleepTimeText());
        tvWakeTime.setText(tracker.getLastWakeTimeText());

        // Thời gian ngủ duration
        tvSleepDuration.setText(tracker.getSleepDurationTodayText());
    }

    private void showDailyTip() {
        // Danh sách mẹo (bạn có thể thêm nhiều hơn)
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

        // Random theo ngày (mỗi ngày 1 mẹo khác)
        Calendar cal = Calendar.getInstance();
        int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        int index = dayOfYear % tips.length; // Đảm bảo mỗi ngày khác mẹo

        if (tvTip != null) {
            tvTip.setText(tips[index]);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(clockRunnable); // Dừng đồng hồ
    }
}