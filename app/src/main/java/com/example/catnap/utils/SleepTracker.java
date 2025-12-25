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
    private static final String KEY_LAST_STREAK_DAY = "last_streak_day";
    private static final long TARGET_SLEEP_MS = 8 * 60 * 60 * 1000; // 8 giờ

    private final SharedPreferences prefs;

    public SleepTracker(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        validateAndFixStreak();
    }

    // LƯU DỮ LIỆU THEO NGÀY
    public void saveSleepTime(Date sleepTime) {
        String dateKey = getDateKey(sleepTime);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(KEY_SLEEP_TIME + dateKey, sleepTime.getTime());
        editor.apply();
    }

    public void saveWakeTime(Date wakeTime) {
        String dateKey = getDateKey(wakeTime);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(KEY_WAKE_TIME + dateKey, wakeTime.getTime());

        long sleepTime = prefs.getLong(KEY_SLEEP_TIME + dateKey, 0);
        if (sleepTime > 0) {
            long duration = wakeTime.getTime() - sleepTime;
            if (duration > 0) {
                checkAndUpdateStreak(dateKey, duration);
            }
        }

        editor.apply();
    }

    // LẤY DỮ LIỆU THEO NGÀY
    public long getSleepDurationForDate(Date date) {
        String dateKey = getDateKey(date);
        long sleepTime = prefs.getLong(KEY_SLEEP_TIME + dateKey, 0);
        long wakeTime = prefs.getLong(KEY_WAKE_TIME + dateKey, 0);

        if (sleepTime > 0 && wakeTime > sleepTime) {
            return wakeTime - sleepTime;
        }
        return 0;
    }

    public long getSleepTimeForDate(Date date) {
        String dateKey = getDateKey(date);
        return prefs.getLong(KEY_SLEEP_TIME + dateKey, 0);
    }

    public long getWakeTimeForDate(Date date) {
        String dateKey = getDateKey(date);
        return prefs.getLong(KEY_WAKE_TIME + dateKey, 0);
    }

    // STREAK LOGIC
    private void checkAndUpdateStreak(String todayKey, long duration) {
        float hours = duration / (3600000f);

        if (hours >= 5f) {
            updateStreak(todayKey);
        } else {
            resetStreak();
        }
    }

    private void updateStreak(String todayKey) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_YEAR, -1);
        String yesterdayKey = getDateKey(cal.getTime());

        int currentStreak = prefs.getInt(KEY_STREAK, 0);
        String lastStreakDay = prefs.getString(KEY_LAST_STREAK_DAY, "");

        if (currentStreak == 0) {
            currentStreak = 1;
            prefs.edit()
                    .putInt(KEY_STREAK, currentStreak)
                    .putString(KEY_LAST_STREAK_DAY, todayKey)
                    .apply();
            return;
        }

        long yesterdaySleep = getSleepDurationForDate(cal.getTime());

        if (yesterdaySleep > 0) {
            float yesterdayHours = yesterdaySleep / (3600000f);
            if (yesterdayHours >= 5f) {
                currentStreak++;
                prefs.edit()
                        .putInt(KEY_STREAK, currentStreak)
                        .putString(KEY_LAST_STREAK_DAY, todayKey)
                        .apply();
            } else {
                currentStreak = 1;
                prefs.edit()
                        .putInt(KEY_STREAK, currentStreak)
                        .putString(KEY_LAST_STREAK_DAY, todayKey)
                        .apply();
            }
        } else {
            currentStreak = 1;
            prefs.edit()
                    .putInt(KEY_STREAK, currentStreak)
                    .putString(KEY_LAST_STREAK_DAY, todayKey)
                    .apply();
        }
    }

    private void resetStreak() {
        prefs.edit()
                .putInt(KEY_STREAK, 0)
                .putString(KEY_LAST_STREAK_DAY, "")
                .apply();
    }

    // GET CURRENT STREAK
    public int getCurrentStreak() {
        int streak = prefs.getInt(KEY_STREAK, 0);

        if (streak == 0) {
            return 0;
        }

        String lastStreakDay = prefs.getString(KEY_LAST_STREAK_DAY, "");
        if (lastStreakDay.isEmpty()) {
            resetStreak();
            return 0;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date lastDate = sdf.parse(lastStreakDay);

            long duration = getSleepDurationForDate(lastDate);
            if (duration == 0) {
                resetStreak();
                return 0;
            }

            return validateStreakContinuity(streak, lastDate);

        } catch (Exception e) {
            resetStreak();
            return 0;
        }
    }

    private int validateStreakContinuity(int streak, Date lastDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(lastDate);

        int validDays = 0;

        for (int i = 0; i < streak; i++) {
            Date checkDate = cal.getTime();
            long duration = getSleepDurationForDate(checkDate);

            if (duration > 0) {
                float hours = duration / (3600000f);
                if (hours >= 5f) {
                    validDays++;
                } else {
                    break;
                }
            } else {
                break;
            }

            cal.add(Calendar.DAY_OF_YEAR, -1);
        }

        if (validDays < streak) {
            prefs.edit()
                    .putInt(KEY_STREAK, validDays)
                    .apply();
            return validDays;
        }

        return streak;
    }

    // CÁC METHOD HIỆN CÓ
    public String getSleepDurationTodayText() {
        long durationMs = getSleepDurationForDate(new Date());
        if (durationMs == 0) return "Chưa ngủ";
        long hours = durationMs / (60 * 60 * 1000);
        long minutes = (durationMs % (60 * 60 * 1000)) / (60 * 1000);
        return hours + "h " + minutes + "m";
    }

    public float getSleepDebtToday() {
        long slept = getSleepDurationForDate(new Date());
        long debt = TARGET_SLEEP_MS - slept;
        return debt > 0 ? (float) debt / (60 * 60 * 1000) : 0;
    }

    public String getSleepDebtText() {
        float debt = getSleepDebtToday();
        int streak = getCurrentStreak();

        if (debt == 0 && streak > 0) {
            return "Ngủ đủ rồi! 😴✨\nChuỗi: " + streak + " ngày 🔥";
        } else if (debt == 0) {
            return "Ngủ đủ rồi! 😴✨";
        } else if (streak > 0) {
            return String.format("Còn thiếu %.1f giờ ngủ bù 💤\nChuỗi: %d ngày 🔥", debt, streak);
        } else {
            return String.format("Còn thiếu %.1f giờ ngủ bù 💤", debt);
        }
    }

    public String getLastSleepTimeText() {
        long sleepTime = getSleepTimeForDate(new Date());
        if (sleepTime == 0) return "Chưa có dữ liệu";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(sleepTime));
    }

    public String getLastWakeTimeText() {
        long wakeTime = getWakeTimeForDate(new Date());
        if (wakeTime == 0) return "Chưa có dữ liệu";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(wakeTime));
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

    // PHƯƠNG THỨC ĐỂ XÓA STREAK MẶC ĐỊNH
    public void resetDefaultStreak() {
        prefs.edit()
                .remove(KEY_STREAK)
                .remove(KEY_LAST_STREAK_DAY)
                .apply();
    }

    // PHƯƠNG THỨC KIỂM TRA VÀ FIX STREAK
    public void validateAndFixStreak() {
        int currentStreak = prefs.getInt(KEY_STREAK, 0);
        if (currentStreak > 0) {
            getCurrentStreak();
        }
    }

    // ========== THÊM CÁC METHOD CHO CALENDAR FRAGMENT ==========

    // Lấy tổng số giờ ngủ trong một tháng
    public float getTotalSleepHoursForMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        float totalHours = 0;

        for (int day = 1; day <= maxDay; day++) {
            cal.set(year, month, day);
            long duration = getSleepDurationForDate(cal.getTime());
            totalHours += duration / (3600000f);
        }

        return totalHours;
    }

    // Lấy số ngày ngủ ngon trong tháng (≥ 7.5 giờ)
    public int getGoodSleepDaysForMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        int goodDays = 0;

        for (int day = 1; day <= maxDay; day++) {
            cal.set(year, month, day);
            long duration = getSleepDurationForDate(cal.getTime());
            float hours = duration / (3600000f);

            if (hours >= 7.5f) {
                goodDays++;
            }
        }

        return goodDays;
    }

    // Lấy số ngày ngủ muộn/ngủ ít trong tháng (< 5 giờ)
    public int getLateSleepDaysForMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        int lateDays = 0;

        for (int day = 1; day <= maxDay; day++) {
            cal.set(year, month, day);
            long duration = getSleepDurationForDate(cal.getTime());
            float hours = duration / (3600000f);

            if (duration > 0 && hours < 5f) {
                lateDays++;
            }
        }

        return lateDays;
    }

    // Lấy số ngày có dữ liệu ngủ trong tháng
    public int getDaysWithDataForMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        int daysWithData = 0;

        for (int day = 1; day <= maxDay; day++) {
            cal.set(year, month, day);
            long duration = getSleepDurationForDate(cal.getTime());

            if (duration > 0) {
                daysWithData++;
            }
        }

        return daysWithData;
    }

    // Lấy số ngày không có dữ liệu trong tháng
    public int getDaysWithoutDataForMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        int daysWithoutData = 0;

        for (int day = 1; day <= maxDay; day++) {
            cal.set(year, month, day);
            long duration = getSleepDurationForDate(cal.getTime());

            if (duration == 0) {
                daysWithoutData++;
            }
        }

        return daysWithoutData;
    }
}