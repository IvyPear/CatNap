package com.example.catnap.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class WakeUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Báo thức reo
        Toast.makeText(context, "Dậy thôi! Giờ ngủ bù đã hết! 😴✨", Toast.LENGTH_LONG).show();

        // Có thể phát âm thanh, mở app, v.v.
    }
}