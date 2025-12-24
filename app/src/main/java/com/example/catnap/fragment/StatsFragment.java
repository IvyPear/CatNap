package com.example.catnap.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.catnap.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class StatsFragment extends Fragment {

    private BarChart barChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        // Nút back (nếu cần, hoặc để trống vì bottom nav)
//        ImageView btnBack = view.findViewById(R.id.btn_back_stats);
//        if (btnBack != null) {
//            btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
//        }

        barChart = view.findViewById(R.id.bar_chart);

        setupBarChart();

        return view;
    }

    private void setupBarChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, 4f));    // T2
        entries.add(new BarEntry(1f, 6.5f));  // T3
        entries.add(new BarEntry(2f, 5.5f));  // T4
        entries.add(new BarEntry(3f, 8.5f));  // T5 (hôm nay - highlight)
        entries.add(new BarEntry(4f, 7f));    // T6
        entries.add(new BarEntry(5f, 3f));    // T7
        entries.add(new BarEntry(6f, 7.5f));  // CN

        BarDataSet dataSet = new BarDataSet(entries, "Giờ ngủ");
        dataSet.setColors(Color.parseColor("#7f13ec"), Color.parseColor("#9d4bf2")); // tím gradient
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBorders(false);

        // XAxis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.parseColor("#ab9db9"));
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"T2", "T3", "T4", "T5", "T6", "T7", "CN"}));

        // YAxis
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisLeft().setAxisMaximum(10f);
        barChart.getAxisLeft().setTextColor(Color.parseColor("#ab9db9"));
        barChart.getAxisRight().setEnabled(false);

        // Highlight ngày hiện tại (T5)
        barChart.highlightValue(3f, 0);

        barChart.animateY(1000);
        barChart.invalidate();
    }
}