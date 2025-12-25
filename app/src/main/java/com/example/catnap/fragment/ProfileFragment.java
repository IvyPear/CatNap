package com.example.catnap.fragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.catnap.R;
import com.example.catnap.utils.SleepTracker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private SleepTracker tracker;
    private SharedPreferences prefs;

    // Views
    private TextView tvUserName, tvSleepGoal, tvXp, tvLevel;
    private TextView tvStreakDays, tvTotalHours;
    private TextView tvTargetHours, tvBedtimeGoal, tvWakeupGoal;
    private TextView tvBedtimeReminder, tvWakeupReminder;
    private ProgressBar progressLevel;
    private ImageView imgAvatar;
    private Switch switchBedtimeReminder, switchWakeupReminder;
    private ImageButton btnEditTarget, btnEditBedtime, btnEditWakeup, btnChangeAvatar;

    // Prefs keys
    private static final String PREFS_NAME = "ProfilePrefs";
    private static final String KEY_TARGET_HOURS = "target_hours";
    private static final String KEY_BEDTIME_GOAL = "bedtime_goal";
    private static final String KEY_WAKEUP_GOAL = "wakeup_goal";
    private static final String KEY_SELECTED_AVATAR = "selected_avatar";
    private static final String KEY_BEDTIME_REMINDER = "bedtime_reminder";
    private static final String KEY_WAKEUP_REMINDER = "wakeup_reminder";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tracker = new SleepTracker(requireContext());
        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Init views
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvSleepGoal = view.findViewById(R.id.tv_sleep_goal);
        tvXp = view.findViewById(R.id.tv_xp);
        tvLevel = view.findViewById(R.id.tv_level);
        progressLevel = view.findViewById(R.id.progress_level);
        tvStreakDays = view.findViewById(R.id.tv_streak_days);
        tvTotalHours = view.findViewById(R.id.tv_total_hours);
        tvTargetHours = view.findViewById(R.id.tv_target_hours);
        tvBedtimeGoal = view.findViewById(R.id.tv_bedtime_goal);
        tvWakeupGoal = view.findViewById(R.id.tv_wakeup_goal);
        tvBedtimeReminder = view.findViewById(R.id.tv_bedtime_reminder);
        tvWakeupReminder = view.findViewById(R.id.tv_wakeup_reminder);
        switchBedtimeReminder = view.findViewById(R.id.switch_bedtime_reminder);
        switchWakeupReminder = view.findViewById(R.id.switch_wakeup_reminder);
        btnEditTarget = view.findViewById(R.id.btn_edit_target);
        btnEditBedtime = view.findViewById(R.id.btn_edit_bedtime);
        btnEditWakeup = view.findViewById(R.id.btn_edit_wakeup);
        btnChangeAvatar = view.findViewById(R.id.btn_change_avatar);
        imgAvatar = view.findViewById(R.id.img_avatar);

        // Frame avatar clickable
        view.findViewById(R.id.frame_avatar).setOnClickListener(v -> showAvatarSelectionDialog());

        loadProfileData();

        // Listeners
        btnEditTarget.setOnClickListener(v -> showTargetHoursDialog());
        btnEditBedtime.setOnClickListener(v -> showTimePickerDialog(true));
        btnEditWakeup.setOnClickListener(v -> showTimePickerDialog(false));
        btnChangeAvatar.setOnClickListener(v -> showAvatarSelectionDialog());

        switchBedtimeReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_BEDTIME_REMINDER, isChecked).apply();
            updateReminderText();
        });

        switchWakeupReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_WAKEUP_REMINDER, isChecked).apply();
            updateReminderText();
        });

        // Action buttons
        view.findViewById(R.id.btn_export_data).setOnClickListener(v -> exportSleepData());
        view.findViewById(R.id.btn_save_stats_image).setOnClickListener(v -> saveStatsAsImage());
        view.findViewById(R.id.btn_clear_data).setOnClickListener(v -> showClearDataDialog());

        return view;
    }

    private void loadProfileData() {
        // User name
        tvUserName.setText("Người dùng CatNap");

        // Streak
        int streak = tracker.getCurrentStreak();
        tvStreakDays.setText(String.valueOf(streak));

        // Total hours (30 ngày)
        float totalHours = 0;
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < 30; i++) {
            long duration = tracker.getSleepDurationForDate(cal.getTime());
            totalHours += duration / 3600000f;
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }
        tvTotalHours.setText(String.format("%.0f", totalHours));

        // Mục tiêu
        int target = prefs.getInt(KEY_TARGET_HOURS, 8);
        tvSleepGoal.setText("Mục tiêu: " + target + "h/ngày");
        tvTargetHours.setText(target + " giờ/ngày");

        // Giờ lý tưởng
        tvBedtimeGoal.setText(prefs.getString(KEY_BEDTIME_GOAL, "23:00"));
        tvWakeupGoal.setText(prefs.getString(KEY_WAKEUP_GOAL, "06:30"));

        // Reminders
        boolean bedtimeOn = prefs.getBoolean(KEY_BEDTIME_REMINDER, true);
        boolean wakeupOn = prefs.getBoolean(KEY_WAKEUP_REMINDER, true);
        switchBedtimeReminder.setChecked(bedtimeOn);
        switchWakeupReminder.setChecked(wakeupOn);
        tvBedtimeReminder.setText(bedtimeOn ? tvBedtimeGoal.getText() + " hàng ngày" : "Tắt");
        tvWakeupReminder.setText(wakeupOn ? tvWakeupGoal.getText() + " hàng ngày" : "Tắt");

        // Avatar
        String avatar = prefs.getString(KEY_SELECTED_AVATAR, "ic_cat_good");
        int resId = getResources().getIdentifier(avatar, "drawable", requireContext().getPackageName());
        if (resId != 0) imgAvatar.setImageResource(resId);

        // Level & XP (10 XP/ngày streak)
        int xp = streak * 10;
        int level = xp / 100 + 1;
        int progress = xp % 100;
        tvLevel.setText("Lv." + level);
        tvXp.setText(progress + "/100 XP");
        progressLevel.setProgress(progress);

        // Animation level up nếu vừa lên cấp
        if (progress == 0 && xp > 0) animateLevelUp(tvLevel);
    }

    private void updateReminderText() {
        boolean bedtimeOn = switchBedtimeReminder.isChecked();
        boolean wakeupOn = switchWakeupReminder.isChecked();
        tvBedtimeReminder.setText(bedtimeOn ? tvBedtimeGoal.getText() + " hàng ngày" : "Tắt");
        tvWakeupReminder.setText(wakeupOn ? tvWakeupGoal.getText() + " hàng ngày" : "Tắt");
    }

    private void showTargetHoursDialog() {
        String[] options = {"6 giờ", "7 giờ", "8 giờ", "9 giờ", "10 giờ"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Mục tiêu giờ ngủ mỗi ngày")
                .setItems(options, (dialog, which) -> {
                    int hours = 6 + which;
                    prefs.edit().putInt(KEY_TARGET_HOURS, hours).apply();
                    tvTargetHours.setText(hours + " giờ/ngày");
                    tvSleepGoal.setText("Mục tiêu: " + hours + "h/ngày");
                    Toast.makeText(requireContext(), "Đã đặt mục tiêu: " + hours + "h/ngày", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showTimePickerDialog(boolean isBedtime) {
        Calendar cal = Calendar.getInstance();
        String current = isBedtime ? prefs.getString(KEY_BEDTIME_GOAL, "23:00") : prefs.getString(KEY_WAKEUP_GOAL, "06:30");
        String[] parts = current.split(":");
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
        cal.set(Calendar.MINUTE, Integer.parseInt(parts[1]));

        TimePickerDialog picker = new TimePickerDialog(requireContext(), (view, hour, minute) -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
            if (isBedtime) {
                prefs.edit().putString(KEY_BEDTIME_GOAL, time).apply();
                tvBedtimeGoal.setText(time);
            } else {
                prefs.edit().putString(KEY_WAKEUP_GOAL, time).apply();
                tvWakeupGoal.setText(time);
            }
            updateReminderText();
            Toast.makeText(requireContext(), "Đã lưu giờ " + (isBedtime ? "đi ngủ" : "thức dậy") + ": " + time, Toast.LENGTH_SHORT).show();
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);

        picker.setTitle(isBedtime ? "Giờ đi ngủ lý tưởng" : "Giờ thức dậy mục tiêu");
        picker.show();
    }

    private void showAvatarSelectionDialog() {
        String[] avatars = {"Mèo Ngủ Ngon", "Mèo Thiếu Ngủ Nhẹ", "Mèo Thiếu Ngủ Nặng"};
        int[] resIds = {R.drawable.ic_cat_good, R.drawable.ic_cat_light, R.drawable.ic_cat_severe};

        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn avatar mèo")
                .setItems(avatars, (dialog, which) -> {
                    String avatarName = "ic_cat_" + (which == 0 ? "good" : which == 1 ? "light" : "severe");
                    prefs.edit().putString(KEY_SELECTED_AVATAR, avatarName).apply();
                    imgAvatar.setImageResource(resIds[which]);
                    animateLevelUp(imgAvatar);
                    Toast.makeText(requireContext(), "Đã chọn: " + avatars[which], Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void animateLevelUp(View view) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.4f, 1f),
                ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.4f, 1f),
                ObjectAnimator.ofFloat(view, "alpha", 1f, 0.7f, 1f)
        );
        set.setDuration(800);
        set.start();
    }

    private void showClearDataDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa toàn bộ dữ liệu?")
                .setMessage("Thao tác này sẽ xóa hết lịch sử ngủ, streak, mục tiêu và thiết lập.\n\nBạn chắc chứ?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    prefs.edit().clear().apply();
                    Toast.makeText(requireContext(), "Đã xóa toàn bộ dữ liệu!", Toast.LENGTH_SHORT).show();
                    loadProfileData();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Placeholder cho xuất CSV và lưu ảnh (bạn có thể mở rộng sau)
    private void exportSleepData() {
        Toast.makeText(requireContext(), "Tính năng xuất dữ liệu CSV (sắp ra mắt)", Toast.LENGTH_SHORT).show();
    }

    private void saveStatsAsImage() {
        Toast.makeText(requireContext(), "Tính năng lưu ảnh thống kê (sắp ra mắt)", Toast.LENGTH_SHORT).show();
    }
}