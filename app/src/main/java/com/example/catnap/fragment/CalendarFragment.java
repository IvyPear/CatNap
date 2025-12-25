package com.example.catnap.fragment;

import android.graphics.Color;
import android.os.Bundle;
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
import java.util.Date;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private GridView gridCalendar;
    private TextView tvMonthYear, tvStreak, tvTotalHours, tvGoodDays, tvLateDays;
    private SleepTracker tracker;
    private Calendar currentMonth;

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
        currentMonth = Calendar.getInstance();

        setupCalendar();

        return view;
    }

    private void setupCalendar() {
        // Tháng năm hiện tại
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", new Locale("vi", "VN"));
        String monthYear = monthYearFormat.format(currentMonth.getTime());
        monthYear = monthYear.substring(0, 1).toUpperCase() + monthYear.substring(1);
        tvMonthYear.setText(monthYear);

        // Chuỗi streak
        int streak = tracker.getCurrentStreak();
        if (streak > 0) {
            tvStreak.setText(streak + " ngày liên tiếp ngủ ngon 🔥");
        } else {
            tvStreak.setText("Bắt đầu chuỗi ngủ ngon của bạn! ✨");
        }

        // Danh sách ngày
        ArrayList<String> days = new ArrayList<>();

        // Tên ngày tuần
        String[] weekDays = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        for (String day : weekDays) {
            days.add(day);
        }

        // Calendar cho tháng hiện tại, ngày 1
        Calendar monthCal = (Calendar) currentMonth.clone();
        monthCal.set(Calendar.DAY_OF_MONTH, 1);

        // Tính offset
        int firstDayOfWeek = monthCal.get(Calendar.DAY_OF_WEEK);
        int offset;
        if (firstDayOfWeek == Calendar.SUNDAY) {
            offset = 6;
        } else {
            offset = firstDayOfWeek - Calendar.MONDAY;
        }

        // Thêm ô trống trước ngày đầu tiên
        for (int i = 0; i < offset; i++) {
            days.add("");
        }

        // Thêm ngày trong tháng
        int maxDay = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int day = 1; day <= maxDay; day++) {
            days.add(String.valueOf(day));
        }

        // Adapter tùy chỉnh
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), R.layout.item_calendar_day, days) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    view = LayoutInflater.from(getContext()).inflate(R.layout.item_calendar_day, parent, false);
                }

                TextView textView = view.findViewById(R.id.tv_day);
                View indicator = view.findViewById(R.id.sleep_indicator);

                // Tên ngày tuần
                if (position < 7) {
                    String dayName = getItem(position);
                    textView.setText(dayName);
                    textView.setTextColor(getResources().getColor(R.color.text_secondary));
                    textView.setBackgroundColor(Color.TRANSPARENT);
                    indicator.setVisibility(View.GONE);
                    return view;
                }

                // Ô trống hoặc ngày thực tế
                String dayText = getItem(position);
                if (dayText == null || dayText.isEmpty()) {
                    textView.setText("");
                    textView.setBackgroundColor(Color.TRANSPARENT);
                    indicator.setVisibility(View.GONE);
                    return view;
                }

                // Chuyển từ position sang ngày tháng
                int dayOfMonth;
                try {
                    dayOfMonth = Integer.parseInt(dayText);
                } catch (NumberFormatException e) {
                    textView.setText("");
                    textView.setBackgroundColor(Color.TRANSPARENT);
                    indicator.setVisibility(View.GONE);
                    return view;
                }

                // Tạo Calendar cho ngày này
                Calendar dayCal = Calendar.getInstance();
                dayCal.set(Calendar.YEAR, currentMonth.get(Calendar.YEAR));
                dayCal.set(Calendar.MONTH, currentMonth.get(Calendar.MONTH));
                dayCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                dayCal.set(Calendar.HOUR_OF_DAY, 0);
                dayCal.set(Calendar.MINUTE, 0);
                dayCal.set(Calendar.SECOND, 0);
                dayCal.set(Calendar.MILLISECOND, 0);

                // Lấy dữ liệu ngủ THỰC TẾ
                long sleepDurationMs = tracker.getSleepDurationForDate(dayCal.getTime());

                // Kiểm tra nếu là hôm nay
                Calendar today = Calendar.getInstance();
                boolean isToday = (dayCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        dayCal.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                        dayCal.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH));

                // Hiển thị số ngày
                textView.setText(String.valueOf(dayOfMonth));
                textView.setBackgroundColor(Color.TRANSPARENT);

                // Xử lý màu sắc và indicator
                if (sleepDurationMs == 0) {
                    // Không có dữ liệu ngủ
                    textView.setTextColor(getResources().getColor(R.color.text_secondary));
                    indicator.setVisibility(View.GONE);

                    // Ngày quá khứ không có dữ liệu
                    if (dayCal.before(today)) {
                        textView.setTextColor(Color.parseColor("#888888"));
                    }

                    // Hôm nay chưa có dữ liệu
                    if (isToday) {
                        textView.setTextColor(getResources().getColor(R.color.primary));
                        textView.setBackgroundResource(R.drawable.bg_today_empty);
                    }
                } else {
                    // CÓ dữ liệu ngủ THỰC
                    float hours = sleepDurationMs / (3600000f);

                    // Đặt màu chữ trắng
                    textView.setTextColor(Color.WHITE);

                    // Đặt màu nền theo chất lượng giấc ngủ
                    if (hours >= 7.5f) {
                        textView.setBackgroundResource(R.drawable.bg_good_sleep);
                    } else if (hours >= 5f) {
                        textView.setBackgroundResource(R.drawable.bg_medium_sleep);
                    } else {
                        textView.setBackgroundResource(R.drawable.bg_bad_sleep);
                    }

                    // Hiển thị indicator
                    indicator.setVisibility(View.VISIBLE);

                    // Hôm nay có dữ liệu
                    if (isToday) {
                        textView.setBackgroundResource(R.drawable.bg_today_filled);
                    }
                }

                return view;
            }
        };

        gridCalendar.setAdapter(adapter);

        // Cập nhật thống kê tháng
        updateMonthlyStats();
    }

    private void updateMonthlyStats() {
        int year = currentMonth.get(Calendar.YEAR);
        int month = currentMonth.get(Calendar.MONTH);

        // Lấy dữ liệu thống kê từ SleepTracker
        float totalHours = tracker.getTotalSleepHoursForMonth(year, month);
        int goodDays = tracker.getGoodSleepDaysForMonth(year, month);
        int lateDays = tracker.getLateSleepDaysForMonth(year, month);

        // Hiển thị thống kê
        tvTotalHours.setText(String.format("%.0f giờ", totalHours));
        tvGoodDays.setText(goodDays + " ngày");
        tvLateDays.setText(lateDays + " ngày");

        // Debug log
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
        String monthStr = sdf.format(cal.getTime());

        System.out.println("CalendarFragment - Thống kê tháng " + monthStr + ":");
        System.out.println("- Tổng giờ: " + totalHours + "h");
        System.out.println("- Ngày ngủ ngon: " + goodDays);
        System.out.println("- Ngày ngủ muộn: " + lateDays);
    }
}