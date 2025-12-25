package com.example.catnap.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.catnap.R;
import com.example.catnap.utils.SleepTracker;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class StatsFragment extends Fragment {

    private BarChart barChart;
    private LineChart lineChart;
    private TextView tvAvgSleep, tvTotalHours, tvSleepScore, tvEfficiency, tvWakeTime, tvSleepTime, tvTip;
    private TextView tabWeek, tabMonth, tabYear;

    private SleepTracker tracker;
    private int currentMode = 0; // 0: Tuần (Bar), 1: Tháng (Line), 2: Năm (Line)

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        barChart = view.findViewById(R.id.bar_chart);
        lineChart = view.findViewById(R.id.line_chart); // Thêm LineChart vào XML (xem dưới)
        tvAvgSleep = view.findViewById(R.id.tv_avg_sleep);
        tvTotalHours = view.findViewById(R.id.tv_total_month);
        tvSleepScore = view.findViewById(R.id.tv_sleep_score);
        tvEfficiency = view.findViewById(R.id.tv_efficiency);
        tvWakeTime = view.findViewById(R.id.tv_wake_time);
        tvSleepTime = view.findViewById(R.id.tv_sleep_time);
        tvTip = view.findViewById(R.id.tv_tip);

        tabWeek = view.findViewById(R.id.tab_week);
        tabMonth = view.findViewById(R.id.tab_month);
        tabYear = view.findViewById(R.id.tab_year);

        tracker = new SleepTracker(requireContext());

        setupCharts();
        setupTabListeners();
        updateChartAndStats(currentMode);

        return view;
    }

    private void setupCharts() {
        // Setup BarChart (Tuần)
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBorders(false);
        barChart.setTouchEnabled(false);

        XAxis xBar = barChart.getXAxis();
        xBar.setPosition(XAxis.XAxisPosition.BOTTOM);
        xBar.setDrawGridLines(false);
        xBar.setGranularity(1f);
        xBar.setTextColor(Color.WHITE);

        barChart.getAxisLeft().setTextColor(Color.WHITE);
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisLeft().setAxisMaximum(12f);
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);

        // Setup LineChart (Tháng/Năm)
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setDrawBorders(false);
        lineChart.setTouchEnabled(false);

        XAxis xLine = lineChart.getXAxis();
        xLine.setPosition(XAxis.XAxisPosition.BOTTOM);
        xLine.setDrawGridLines(false);
        xLine.setGranularity(1f);
        xLine.setTextColor(Color.WHITE);

        lineChart.getAxisLeft().setTextColor(Color.WHITE);
        lineChart.getAxisLeft().setAxisMinimum(0f);
        lineChart.getAxisLeft().setAxisMaximum(12f);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
    }

    private void setupTabListeners() {
        tabWeek.setOnClickListener(v -> {
            currentMode = 0;
            highlightTab(tabWeek);
            updateChartAndStats(currentMode);
        });

        tabMonth.setOnClickListener(v -> {
            currentMode = 1;
            highlightTab(tabMonth);
            updateChartAndStats(currentMode);
        });

        tabYear.setOnClickListener(v -> {
            currentMode = 2;
            highlightTab(tabYear);
            updateChartAndStats(currentMode);
        });
    }

    private void highlightTab(TextView selectedTab) {
        tabWeek.setBackgroundResource(R.drawable.btn_normal);
        tabMonth.setBackgroundResource(R.drawable.btn_normal);
        tabYear.setBackgroundResource(R.drawable.btn_normal);

        tabWeek.setTextColor(getResources().getColor(R.color.text_secondary));
        tabMonth.setTextColor(getResources().getColor(R.color.text_secondary));
        tabYear.setTextColor(getResources().getColor(R.color.text_secondary));

        selectedTab.setBackgroundResource(R.drawable.btn_selected);
        selectedTab.setTextColor(getResources().getColor(R.color.text_primary));
    }

    private void updateChartAndStats(int mode) {
        ArrayList<Entry> lineEntries = new ArrayList<>();
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        int days = (mode == 0) ? 7 : (mode == 1) ? 30 : 365;

        float totalHours = 0;
        int countDaysWithData = 0;

        // Lấy data từ ngày xa → gần (trái → phải: cũ → mới)
        for (int i = days - 1; i >= 0; i--) {
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, -i);
            Date date = cal.getTime();

            long durationMs = tracker.getSleepDurationForDate(date);
            float hours = durationMs / 3600000f;

            barEntries.add(new BarEntry(days - 1 - i, hours));
            lineEntries.add(new Entry(days - 1 - i, hours));

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
            labels.add(sdf.format(date));

            if (hours > 0) {
                totalHours += hours;
                countDaysWithData++;
            }
        }

        // Chọn chart tùy mode
        if (mode == 0) { // Tuần - BarChart
            lineChart.setVisibility(View.GONE);
            barChart.setVisibility(View.VISIBLE);

            BarDataSet dataSet = new BarDataSet(barEntries, "Giờ ngủ");
            dataSet.setColors(Color.parseColor("#7f13ec"), Color.parseColor("#9d4bf2"));
            dataSet.setValueTextColor(Color.WHITE);
            dataSet.setValueTextSize(12f);

            BarData barData = new BarData(dataSet);
            barData.setBarWidth(0.6f);

            barChart.setData(barData);
            barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
            barChart.highlightValue(days - 1, 0); // Highlight hôm nay
            barChart.animateY(1200);
            barChart.invalidate();
        } else { // Tháng/Năm - LineChart
            barChart.setVisibility(View.GONE);
            lineChart.setVisibility(View.VISIBLE);

            LineDataSet dataSet = new LineDataSet(lineEntries, "Giờ ngủ");
            dataSet.setColor(Color.parseColor("#7f13ec"));
            dataSet.setCircleColor(Color.WHITE);
            dataSet.setLineWidth(2.5f);
            dataSet.setCircleRadius(5f);
            dataSet.setDrawCircleHole(false);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Sóng mượt
            dataSet.setValueTextColor(Color.WHITE);
            dataSet.setValueTextSize(12f);
            dataSet.setDrawValues(false); // Ẩn số trên đường nếu nhiều điểm

            LineData lineData = new LineData(dataSet);

            lineChart.setData(lineData);
            lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
            lineChart.highlightValue(days - 1, 0);
            lineChart.animateY(1200);
            lineChart.invalidate();
        }

        // Thống kê
        float avg = countDaysWithData > 0 ? totalHours / countDaysWithData : 0;
        tvAvgSleep.setText(String.format("%.1fh", avg));
        tvTotalHours.setText(String.format("%.0fh", totalHours));

        float score = 100 - (8 - avg) * 10;
        score = Math.max(0, Math.min(100, score));
        tvSleepScore.setText(String.format("%.0f", score));

        float efficiency = (avg / 8f) * 100;
        tvEfficiency.setText(String.format("%.0f%%", efficiency));

        tvSleepTime.setText(tracker.getLastSleepTimeText());
        tvWakeTime.setText(tracker.getLastWakeTimeText());

        if (avg < 6f) {
            tvTip.setText("Meow! Bạn ngủ hơi ít gần đây. Thử đi ngủ sớm hơn 30 phút tối nay nhé? 💤");
        } else if (avg >= 7.5f) {
            tvTip.setText("Meow! Chuỗi ngủ ngon đang lên! Giữ vững nhé, bạn tuyệt vời lắm! ✨");
        } else {
            tvTip.setText("Meow! Giấc ngủ ổn, nhưng có thể tốt hơn. Thử tắt đèn sớm hơn chút xíu nha! 😽");
        }
    }
}