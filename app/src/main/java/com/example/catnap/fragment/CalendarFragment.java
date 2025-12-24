package com.example.catnap.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.catnap.R;
import com.example.catnap.utils.SleepTracker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private GridView gridCalendar;
    private TextView tvMonthYear, tvStreak, tvTotalHours, tvGoodDays, tvLateDays;
    private SleepTracker tracker;

    private static final String TAG = "CalendarDebug";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        gridCalendar = view.findViewById(R.id.grid_calendar);
        tvMonthYear = view.findViewById(R.id.tv_month_year);
        tvStreak = view.findViewById(R.id.tv_streak);
        tvTotalHours = view.findViewById(R.id.tv_total_hours);
        tvGoodDays = view.findViewById(R.id.tv_good_days);
        tvLateDays = view.findViewById(R.id.tv_late_days);

        tracker = new SleepTracker(requireContext());

        setupCalendar();

        return view;
    }

    private void setupCalendar() {
        Calendar currentCal = Calendar.getInstance();

        // Tháng năm hiện tại (tiếng Việt)
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", new Locale("vi", "VN"));
        tvMonthYear.setText(monthYearFormat.format(currentCal.getTime()).toUpperCase());

        // Chuỗi streak
        tvStreak.setText(tracker.getCurrentStreak() + " ngày liên tiếp ngủ ngon 🔥");

        // Danh sách ngày
        ArrayList<String> days = new ArrayList<>();

        // Tên ngày tuần
        String[] weekDays = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        for (String day : weekDays) {
            days.add(day);
        }

        // Calendar cho tháng hiện tại, ngày 1
        Calendar monthCal = (Calendar) currentCal.clone();
        monthCal.set(Calendar.DAY_OF_MONTH, 1);

        // Tính offset: số ô trống trước ngày 1 (T2 là cột 0)
        int firstDayOfWeek = monthCal.get(Calendar.DAY_OF_WEEK); // 1=CN, 2=T2, ..., 7=T7
        int offset = (firstDayOfWeek == Calendar.SUNDAY) ? 6 : firstDayOfWeek - Calendar.MONDAY;
        Log.d(TAG, "Offset khoảng trống: " + offset + " (firstDayOfWeek=" + firstDayOfWeek + ")");

        for (int i = 0; i < offset; i++) {
            days.add("");
        }

        // Thêm ngày trong tháng
        int maxDay = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int day = 1; day <= maxDay; day++) {
            days.add(String.valueOf(day));
        }

        Log.d(TAG, "Tổng số item trong GridView: " + days.size());

        // Adapter tùy chỉnh
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, days) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setGravity(android.view.Gravity.CENTER);
                textView.setTextColor(getResources().getColor(R.color.text_primary));
                textView.setPadding(16, 24, 16, 24);
                textView.setTextSize(16);

                // Tên ngày tuần
                if (position < 7) {
                    textView.setTextColor(getResources().getColor(R.color.text_secondary));
                    textView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                    return textView;
                }

                // Ngày thực tế
                String dayText = textView.getText().toString();
                if (dayText.isEmpty()) {
                    textView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                    return textView;
                }

                int dayOfMonth;
                try {
                    dayOfMonth = Integer.parseInt(dayText);
                } catch (NumberFormatException e) {
                    textView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                    return textView;
                }

                // Calendar cho ngày này
                Calendar dayCal = (Calendar) monthCal.clone();
                dayCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                // Lấy data ngủ
                long sleepTime = tracker.getSleepTimeForDate(dayCal.getTime());
                long wakeTime = tracker.getWakeTimeForDate(dayCal.getTime());
                long durationMs = wakeTime > sleepTime ? wakeTime - sleepTime : 0;

                Log.d(TAG, "Ngày " + dayOfMonth + ": durationMs = " + durationMs);

                // Tô màu theo trạng thái
                if (durationMs == 0) {
                    textView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                    textView.setTextColor(getResources().getColor(R.color.text_secondary));
                } else {
                    float hours = durationMs / (3600000f);

                    if (hours >= 7.5f) {
                        textView.setBackgroundResource(R.drawable.calendar_good_day);
                        textView.setTextColor(android.graphics.Color.WHITE);
                    } else if (hours >= 5f) {
                        textView.setBackgroundResource(R.drawable.calendar_light_day);
                        textView.setTextColor(android.graphics.Color.WHITE);
                    } else {
                        textView.setBackgroundResource(R.drawable.calendar_bad_day);
                        textView.setTextColor(android.graphics.Color.WHITE);
                    }
                }

                // Hôm nay
                Calendar today = Calendar.getInstance();
                if (dayOfMonth == today.get(Calendar.DAY_OF_MONTH) &&
                        dayCal.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                        dayCal.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
                    textView.setBackgroundResource(R.drawable.calendar_today);
                    textView.setTextColor(android.graphics.Color.WHITE);
                }

                return textView;
            }
        };

        gridCalendar.setAdapter(adapter);

        // Thống kê tháng
        updateMonthlyStats();
    }

    private void updateMonthlyStats() {
        Calendar cal = Calendar.getInstance();
        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        long totalMs = 0;
        int goodDays = 0;
        int lateDays = 0;

        for (int day = 1; day <= maxDay; day++) {
            cal.set(Calendar.DAY_OF_MONTH, day);
            long sleepTime = tracker.getSleepTimeForDate(cal.getTime());
            long wakeTime = tracker.getWakeTimeForDate(cal.getTime());
            long durationMs = wakeTime > sleepTime ? wakeTime - sleepTime : 0;

            totalMs += durationMs;

            if (durationMs > 0) {
                float hours = durationMs / (3600000f);
                if (hours >= 7.5f) goodDays++;

                Calendar wakeCal = Calendar.getInstance();
                wakeCal.setTimeInMillis(wakeTime);
                if (wakeCal.get(Calendar.HOUR_OF_DAY) >= 6) {
                    lateDays++;
                }
            }
        }

        long totalHours = totalMs / 3600000;
        tvTotalHours.setText(totalHours + " giờ");
        tvGoodDays.setText(goodDays + " ngày");
        tvLateDays.setText(lateDays + " ngày");
    }
}