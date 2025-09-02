package com.example.reminderapp;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int REMINDER_REQ_CODE = 1010;

    private EditText etInterval;
    private Button btnStart, btnStop;

    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etInterval = findViewById(R.id.etInterval);
        btnStart = findViewById(R.id.btnStart);
        btnStop  = findViewById(R.id.btnStop);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Ask for notification permission on Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 900);
            }
        }

        btnStart.setOnClickListener(v -> startReminder());
        btnStop.setOnClickListener(v -> stopReminder());
    }

    private void startReminder() {
        String input = etInterval.getText().toString().trim();
        if (input.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_enter_interval), Toast.LENGTH_SHORT).show();
            return;
        }

        int intervalMinutes;
        try {
            intervalMinutes = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.invalid_number), Toast.LENGTH_SHORT).show();
            return;
        }

        if (intervalMinutes <= 0) {
            Toast.makeText(this, getString(R.string.interval_must_be_gt_zero), Toast.LENGTH_SHORT).show();
            return;
        }

        long intervalMs = intervalMinutes * 60_000L;

        Intent intent = new Intent(this, ReminderReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(
                this,
                REMINDER_REQ_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Cancel any existing alarm first
        alarmManager.cancel(alarmIntent);

        long firstTrigger = System.currentTimeMillis() + intervalMs;

        // Repeating alarm (may be inexact during Doze; fine for simple reminders)
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                firstTrigger,
                intervalMs,
                alarmIntent
        );

        Toast.makeText(this,
                getString(R.string.reminder_started, intervalMinutes),
                Toast.LENGTH_SHORT).show();
    }

    private void stopReminder() {
        if (alarmIntent == null) {
            // recreate the same PendingIntent so cancel works even after process death
            Intent intent = new Intent(this, ReminderReceiver.class);
            alarmIntent = PendingIntent.getBroadcast(
                    this,
                    REMINDER_REQ_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
        }
        alarmManager.cancel(alarmIntent);
        alarmIntent.cancel();
        Toast.makeText(this, R.string.reminder_stopped, Toast.LENGTH_SHORT).show();
    }
}
