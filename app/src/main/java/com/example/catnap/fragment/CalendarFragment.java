package com.example.catnap.fragment;

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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private GridView gridCalendar;
    private TextView tvMonthYear, tvStreak, tvTotalHours, tvGoodDays, tvLateDays;

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

        setupCalendar();

        return view;
    }

    private void setupCalendar() {
        Calendar calendar = Calendar.getInstance();

        // Cập nhật tháng năm hiện tại
        String monthYear = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " " + calendar.get(Calendar.YEAR);
        tvMonthYear.setText(monthYear);

        // Danh sách ngày
        ArrayList<String> days = new ArrayList<>();

        // Tên ngày tuần (bắt đầu từ Thứ 2)
        String[] weekDays = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        for (String day : weekDays) {
            days.add(day);
        }

        // Ngày trong tháng
        int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= maxDay; i++) {
            days.add(String.valueOf(i));
        }

        // Adapter tùy chỉnh
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, days) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);

                // Style chung
                textView.setGravity(android.view.Gravity.CENTER);
                textView.setTextColor(getResources().getColor(R.color.text_primary));
                textView.setPadding(16, 24, 16, 24);
                textView.setTextSize(16);

                // Nếu là tên ngày tuần (7 ô đầu)
                if (position < 7) {
                    textView.setTextColor(getResources().getColor(R.color.text_secondary));
                    textView.setBackgroundColor(android.R.color.transparent);
                    return textView;
                }

                // Ngày thực tế
                int day = position - 6; // position 7 = ngày 1

                // Reset background
                textView.setBackgroundColor(android.R.color.transparent);

                // Ngày ngủ ngon (tím đậm)
                if (day == 12 || day == 14 || day == 15 || day == 17 || day == 18 || day == 19 || day == 20) {
                    textView.setBackgroundResource(R.drawable.calendar_good_day);
                    textView.setTextColor(android.R.color.white);
                }

                // Ngày ngủ ít (tím nhạt)
                if (day == 13 || day == 16 || day == 21) {
                    textView.setBackgroundResource(R.drawable.calendar_bad_day);
                }

                // Hôm nay (23/12/2025)
                if (day == calendar.get(Calendar.DAY_OF_MONTH)) {
                    textView.setBackgroundResource(R.drawable.calendar_today);
                    textView.setTextColor(android.R.color.white);
                }

                return textView;
            }
        };

        gridCalendar.setAdapter(adapter);

        // Thống kê tạm
        tvStreak.setText("12 ngày liên tiếp ngủ ngon 🔥");
        tvTotalHours.setText("168 giờ");
        tvGoodDays.setText("22 ngày");
        tvLateDays.setText("5 ngày");
    }
}