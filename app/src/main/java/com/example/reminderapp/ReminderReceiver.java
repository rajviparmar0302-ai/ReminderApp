package com.example.reminderapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.app.AlarmManager;

import java.util.HashMap;
import java.util.Map;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "REMINDER_CHANNEL";
    private static final int NOTIFICATION_ID = 100;
    private static final int REMINDER_REQUEST_CODE = 100;
    private static final String TAG = "ReminderReciever";

    @Override
    public void onReceive(Context context, Intent intent) {
//        int intervalMinutes = intent.getIntExtra("intervalMinutes", 0);
//
//        // Debug
//        Toast.makeText(context, "Reminder Triggered! Interval: " + intervalMinutes + " min", Toast.LENGTH_SHORT).show();
//        Log.d("ReminderReceiver", "Reminder Triggered with interval: " + intervalMinutes);

        saveReminderEvent(context, "Reminder Triggered");



//        long newTime = System.currentTimeMillis() + (2 * 60 * 1000);


//        int intervalIntent = dataManager.getintervalIntent(intervalMinutes);

        Intent reminderIntent = new Intent(context, ReminderReceiver.class);
        int intervalMinutes = intent.getIntExtra("intervalMinutes",-1);
        Log.i(TAG, "Interval Minutes : "+intervalMinutes); // Info message

        Toast.makeText(context, "Reminder Triggered! Interval: " + intervalMinutes + " min", Toast.LENGTH_SHORT).show();

        long newTime = System.currentTimeMillis() + (intervalMinutes * 60 * 1000);
        reminderIntent.putExtra("intervalMinutes", intervalMinutes);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                REMINDER_REQUEST_CODE,
                reminderIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        newTime,
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        newTime,
                        pendingIntent
                );
            }
        }

        // --- Create Notification Channel ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Reminder Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // --- Snooze Action ---
        Intent snoozeIntent = new Intent(context, SnoozeReceiver.class);
        snoozeIntent.putExtra("intervalMinutes", intervalMinutes);
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // --- Complete Action ---
        Intent completeIntent = new Intent(context, CompleteReceiver.class);
        PendingIntent completePendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                completeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // --- Build Notification ---
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Reminder")
                .setContentText("It's time for your reminder!")
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_snooze, "Snooze", snoozePendingIntent)
                .addAction(R.drawable.ic_done, "Complete", completePendingIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, builder.build());
        }
    }


    private void saveReminderEvent(Context context, String action){
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    if (user == null) {
        return; // not signed in
    }

    // Get reference to Firebase Realtime Database
    DatabaseReference dbRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(user.getUid())
            .child("reminderEvents");

    // Create reminder event
    Map<String, Object> event = new HashMap<>();
    event.put("action", action);
    event.put("timestamp", System.currentTimeMillis());

    // Save event
    dbRef.push().setValue(event)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(context, "Event saved!", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(context, "Error saving event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
}}

