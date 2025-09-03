package com.example.reminderapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private EditText etInterval;
    private Button btnStart, btnStop;

    private static final int REMINDER_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etInterval = findViewById(R.id.etInterval);
        btnStart = findViewById(R.id.startReminder);
        btnStop = findViewById(R.id.stopReminder);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, ReminderReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(
                this,
                REMINDER_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        btnStart.setOnClickListener(v -> startReminder());
        btnStop.setOnClickListener(v -> stopReminder());
    }

    private void startReminder() {
        String intervalText = etInterval.getText().toString().trim();

        if (intervalText.isEmpty()) {
            Toast.makeText(this, "Please enter interval in minutes", Toast.LENGTH_SHORT).show();
            return;
        }

        int intervalMinutes = Integer.parseInt(intervalText);
        long triggerTime = System.currentTimeMillis() + (intervalMinutes * 60L * 1000L);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                scheduleAlarm(triggerTime);
            } else {
                Toast.makeText(this, "Exact alarms not permitted. Enable in settings.", Toast.LENGTH_LONG).show();
            }
        } else {
            scheduleAlarm(triggerTime);
        }
    }

    private void scheduleAlarm(long triggerTime) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
            }

            Toast.makeText(this, "Reminder set successfully!", Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, "Permission denied for exact alarms", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopReminder() {
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Toast.makeText(this, "Reminder stopped", Toast.LENGTH_SHORT).show();
        }
    }
}
