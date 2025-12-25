package com.example.catnap.fragment;

import android.app.TimePickerDialog;
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
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.catnap.R;
import com.example.catnap.utils.SleepTracker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private SleepTracker tracker;
    private TextView tvDate, tvGreeting, tvStreak, tvSleepDebt, tvSleepTime, tvWakeTime, tvSleepDuration, tvCurrentTime, tvTip;
    private ImageView imgCatGood, imgCatLight, imgCatSevere;
    private View timeContainer;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable clockRunnable;

    // Biến tạm để lưu giờ đang chỉnh
    private Date tempSleepTime = null;
    private Date tempWakeTime = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tracker = new SleepTracker(requireContext());

        // Reset streak mặc định khi khởi động
        tracker.resetDefaultStreak();
        tracker.validateAndFixStreak();

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

        // Tìm 3 avatar
        imgCatGood = view.findViewById(R.id.img_cat_good);
        imgCatLight = view.findViewById(R.id.img_cat_light);
        imgCatSevere = view.findViewById(R.id.img_cat_severe);

        // Tìm container của thời gian
        timeContainer = view.findViewById(R.id.time_container);

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

        // Event 4 nút - GIỮ NGUYÊN
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

        // 2 NÚT NÀY KHÔNG ĐỘNG TỚI - GIỮ NGUYÊN
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

        // CHỈ SỬA PHẦN NÀY: Chỉnh giờ thủ công
        timeContainer.setOnClickListener(v -> {
            showQuickTimePicker();
        });

        // Thêm long click để hướng dẫn
        timeContainer.setOnLongClickListener(v -> {
            Toast.makeText(requireContext(), "Nhấn để chỉnh giờ thủ công", Toast.LENGTH_SHORT).show();
            return true;
        });

        return view;
    }

    // PHƯƠNG THỨC MỚI: Chỉnh giờ đơn giản, nhanh gọn
    private void showQuickTimePicker() {
        // Dialog đơn giản với 2 lựa chọn
        new AlertDialog.Builder(requireContext())
                .setTitle("Chỉnh giờ thủ công")
                .setMessage("Bạn muốn chỉnh giờ nào?")
                .setPositiveButton("Chỉnh giờ ngủ", (dialog, which) -> {
                    openSimpleTimePicker(true);
                })
                .setNegativeButton("Chỉnh giờ dậy", (dialog, which) -> {
                    openSimpleTimePicker(false);
                })
                .setNeutralButton("Chỉnh cả hai", (dialog, which) -> {
                    openBothTimePickers();
                })
                .show();
    }

    private void openSimpleTimePicker(boolean isSleepTime) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // Lấy giờ hiện tại làm mặc định
        if (isSleepTime) {
            // Nếu đã có giờ ngủ trước đó, dùng nó
            String lastSleepText = tracker.getLastSleepTimeText();
            if (!lastSleepText.equals("Chưa có dữ liệu")) {
                try {
                    Date lastSleep = sdf.parse(lastSleepText);
                    if (lastSleep != null) {
                        cal.setTime(lastSleep);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        } else {
            // Nếu đã có giờ dậy trước đó, dùng nó
            String lastWakeText = tracker.getLastWakeTimeText();
            if (!lastWakeText.equals("Chưa có dữ liệu")) {
                try {
                    Date lastWake = sdf.parse(lastWakeText);
                    if (lastWake != null) {
                        cal.setTime(lastWake);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                // Nếu có giờ ngủ, mặc định +8 tiếng
                String lastSleepText = tracker.getLastSleepTimeText();
                if (!lastSleepText.equals("Chưa có dữ liệu")) {
                    try {
                        Date lastSleep = sdf.parse(lastSleepText);
                        if (lastSleep != null) {
                            cal.setTime(lastSleep);
                            cal.add(Calendar.HOUR_OF_DAY, 8);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        TimePickerDialog timePicker = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    cal.set(Calendar.MINUTE, minute);

                    // Đặt ngày là hôm nay (luôn luôn)
                    Calendar today = Calendar.getInstance();
                    cal.set(Calendar.YEAR, today.get(Calendar.YEAR));
                    cal.set(Calendar.MONTH, today.get(Calendar.MONTH));
                    cal.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));

                    if (isSleepTime) {
                        tracker.saveSleepTime(cal.getTime());
                        Toast.makeText(requireContext(), "Đã cập nhật giờ ngủ: " +
                                        sdf.format(cal.getTime()),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        tracker.saveWakeTime(cal.getTime());
                        Toast.makeText(requireContext(), "Đã cập nhật giờ dậy: " +
                                        sdf.format(cal.getTime()),
                                Toast.LENGTH_SHORT).show();
                    }

                    updateHomeData();
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
        );

        timePicker.setTitle(isSleepTime ? "Chọn giờ đi ngủ" : "Chọn giờ dậy");
        timePicker.show();
    }

    private void openBothTimePickers() {
        // Hiển thị dialog chọn giờ ngủ trước
        Calendar sleepCal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // Kiểm tra xem đã có giờ ngủ trước đó chưa
        String lastSleepText = tracker.getLastSleepTimeText();
        if (!lastSleepText.equals("Chưa có dữ liệu")) {
            try {
                Date lastSleep = sdf.parse(lastSleepText);
                if (lastSleep != null) {
                    sleepCal.setTime(lastSleep);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        TimePickerDialog sleepPicker = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    sleepCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    sleepCal.set(Calendar.MINUTE, minute);

                    // Đặt ngày là hôm nay cho giờ ngủ
                    Calendar today = Calendar.getInstance();
                    sleepCal.set(Calendar.YEAR, today.get(Calendar.YEAR));
                    sleepCal.set(Calendar.MONTH, today.get(Calendar.MONTH));
                    sleepCal.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
                    Date sleepTime = sleepCal.getTime();

                    // Sau khi chọn giờ ngủ, hiển thị picker cho giờ dậy
                    Calendar wakeCal = Calendar.getInstance();
                    wakeCal.setTime(sleepTime);
                    wakeCal.add(Calendar.HOUR_OF_DAY, 8); // Mặc định +8 tiếng

                    // Kiểm tra xem đã có giờ dậy trước đó chưa
                    String lastWakeText = tracker.getLastWakeTimeText();
                    if (!lastWakeText.equals("Chưa có dữ liệu")) {
                        try {
                            Date lastWake = sdf.parse(lastWakeText);
                            if (lastWake != null) {
                                wakeCal.setTime(lastWake);
                                // Đặt ngày là hôm nay cho giờ dậy cũ
                                wakeCal.set(Calendar.YEAR, today.get(Calendar.YEAR));
                                wakeCal.set(Calendar.MONTH, today.get(Calendar.MONTH));
                                wakeCal.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    TimePickerDialog wakePicker = new TimePickerDialog(
                            requireContext(),
                            (view2, hourOfDay2, minute2) -> {
                                wakeCal.set(Calendar.HOUR_OF_DAY, hourOfDay2);
                                wakeCal.set(Calendar.MINUTE, minute2);

                                // Đặt ngày là hôm nay cho giờ dậy mới
                                wakeCal.set(Calendar.YEAR, today.get(Calendar.YEAR));
                                wakeCal.set(Calendar.MONTH, today.get(Calendar.MONTH));
                                wakeCal.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
                                Date wakeTime = wakeCal.getTime();

                                // KIỂM TRA LOGIC: Nếu giờ dậy < giờ ngủ
                                if (wakeTime.before(sleepTime)) {
                                    // Tự động chuyển giờ dậy sang ngày hôm sau
                                    Calendar nextDay = Calendar.getInstance();
                                    nextDay.setTime(wakeTime);
                                    nextDay.add(Calendar.DAY_OF_YEAR, 1);
                                    wakeTime = nextDay.getTime();

                                    Toast.makeText(requireContext(),
                                            "Giờ dậy được tự động chuyển sang ngày hôm sau",
                                            Toast.LENGTH_SHORT).show();
                                }

                                // Lưu cả hai
                                tracker.saveSleepTime(sleepTime);
                                tracker.saveWakeTime(wakeTime);

                                // Tính và hiển thị tổng thời gian
                                long duration = wakeTime.getTime() - sleepTime.getTime();
                                long hours = duration / (60 * 60 * 1000);
                                long minutes = (duration % (60 * 60 * 1000)) / (60 * 1000);

                                Toast.makeText(requireContext(),
                                        String.format("Đã lưu! Tổng thời gian ngủ: %dh %02dm", hours, minutes),
                                        Toast.LENGTH_LONG).show();

                                updateHomeData();
                            },
                            wakeCal.get(Calendar.HOUR_OF_DAY),
                            wakeCal.get(Calendar.MINUTE),
                            true
                    );

                    wakePicker.setTitle("Chọn giờ dậy");
                    wakePicker.show();
                },
                sleepCal.get(Calendar.HOUR_OF_DAY),
                sleepCal.get(Calendar.MINUTE),
                true
        );

        sleepPicker.setTitle("Chọn giờ đi ngủ");
        sleepPicker.show();
    }

    // CẬP NHẬT DỮ LIỆU TRANG CHỦ
    private void updateHomeData() {
        Calendar cal = Calendar.getInstance();

        // Ngày tháng
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd/MM", Locale.getDefault());
        tvDate.setText(dateFormat.format(cal.getTime()));

        // Lời chào
        tvGreeting.setText(tracker.getGreetingText());

        // STREAK - CHỈ HIỂN THỊ NẾU CÓ DỮ LIỆU THỰC
        int streak = tracker.getCurrentStreak();
        if (streak > 0) {
            tvStreak.setText(streak + " ngày liên tiếp");
        } else {
            tvStreak.setText("Bắt đầu chuỗi ngủ ngon của bạn!");
        }

        // Sleep debt
        tvSleepDebt.setText(tracker.getSleepDebtText());

        // Giờ ngủ và dậy
        tvSleepTime.setText(tracker.getLastSleepTimeText());
        tvWakeTime.setText(tracker.getLastWakeTimeText());

        // Tổng thời gian ngủ hôm nay
        long durationMs = tracker.getSleepDurationForDate(new Date());
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