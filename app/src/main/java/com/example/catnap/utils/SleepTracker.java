package com.example.catnap.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SleepTracker {

    private static final String PREF_NAME = "SleepPrefs";
    private static final String KEY_SLEEP_TIME = "sleepTime_";
    private static final String KEY_WAKE_TIME = "wakeTime_";
    private static final String KEY_STREAK = "streak";
    private static final long TARGET_SLEEP_MS = 8 * 60 * 60 * 1000; // 8 giờ

    private final SharedPreferences prefs;

    public SleepTracker(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSleepTime(Date sleepTime) {
        String dateKey = getDateKey(sleepTime);
        prefs.edit().putLong(KEY_SLEEP_TIME + dateKey, sleepTime.getTime()).apply();
        updateStreak();
    }

    public void saveWakeTime(Date wakeTime) {
        String dateKey = getDateKey(wakeTime);
        prefs.edit().putLong(KEY_WAKE_TIME + dateKey, wakeTime.getTime()).apply();
        updateStreak();
    }

    public long getSleepDurationToday() {
        String dateKey = getDateKey(new Date());
        long sleepTime = prefs.getLong(KEY_SLEEP_TIME + dateKey, 0);
        long wakeTime = prefs.getLong(KEY_WAKE_TIME + dateKey, 0);
        return wakeTime > sleepTime ? wakeTime - sleepTime : 0;
    }

    public String getSleepDurationTodayText() {
        long durationMs = getSleepDurationToday();
        if (durationMs == 0) return "Chưa ngủ";
        long hours = durationMs / (60 * 60 * 1000);
        long minutes = (durationMs % (60 * 60 * 1000)) / (60 * 1000);
        return hours + "h " + minutes + "m";
    }

    public float getSleepDebtToday() {
        long slept = getSleepDurationToday();
        long debt = TARGET_SLEEP_MS - slept;
        return debt > 0 ? (float) debt / (60 * 60 * 1000) : 0;
    }

    public String getSleepDebtText() {
        float debt = getSleepDebtToday();
        int streak = getCurrentStreak();
        if (debt == 0) {
            return "Ngủ đủ rồi! 😴✨\nChuỗi: " + streak + " ngày 🔥";
        }
        return String.format("Còn thiếu %.1f giờ ngủ bù 💤\nChuỗi: %d ngày 🔥", debt, streak);
    }

    public int getCurrentStreak() {
        return prefs.getInt(KEY_STREAK, 0);
    }

    public String getLastSleepTimeText() {
        String dateKey = getDateKey(new Date());
        long sleepTime = prefs.getLong(KEY_SLEEP_TIME + dateKey, 0);
        if (sleepTime == 0) return "Chưa có dữ liệu";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(sleepTime));
    }

    public String getLastWakeTimeText() {
        String dateKey = getDateKey(new Date());
        long wakeTime = prefs.getLong(KEY_WAKE_TIME + dateKey, 0);
        if (wakeTime == 0) return "Chưa có dữ liệu";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(wakeTime));
    }

    private void updateStreak() {
        Calendar today = Calendar.getInstance();
        Calendar yesterday = (Calendar) today.clone();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);

        String todayKey = getDateKey(today.getTime());
        String yesterdayKey = getDateKey(yesterday.getTime());

        long todaySleep = prefs.getLong(KEY_SLEEP_TIME + todayKey, 0);
        long yesterdaySleep = prefs.getLong(KEY_SLEEP_TIME + yesterdayKey, 0);

        int currentStreak = prefs.getInt(KEY_STREAK, 0);

        if (todaySleep > 0 && yesterdaySleep > 0) {
            currentStreak++;
        } else if (todaySleep == 0) {
            currentStreak = 0;
        }

        prefs.edit().putInt(KEY_STREAK, currentStreak).apply();
    }

    private String getDateKey(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date);
    }

    public String getGreetingText() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 12) return "Chào buổi sáng";
        if (hour >= 12 && hour < 18) return "Chào buổi chiều";
        return "Chào buổi tối";
    }
}