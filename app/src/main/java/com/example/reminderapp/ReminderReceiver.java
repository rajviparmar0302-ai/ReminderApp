package com.example.reminderapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import java.util.HashMap;
import java.util.Map;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "REMINDER_CHANNEL";
    private static final int NOTIFICATION_ID = 100;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Debug: Check trigger
        Toast.makeText(context, "Reminder Triggered!", Toast.LENGTH_SHORT).show();
        Log.d("ReminderReceiver", "Reminder Triggered");

        // Save event in Firestore
        saveReminderEvent(context, "Reminder Triggered");

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
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // --- Complete Action ---
        Intent completeIntent = new Intent(context, CompleteReceiver.class);
        PendingIntent completePendingIntent = PendingIntent.getBroadcast(
                context,
                2,
                completeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // --- Build Notification ---
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Reminder")
                .setContentText("It's time for your reminder!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_snooze, "Snooze", snoozePendingIntent)
                .addAction(R.drawable.ic_done, "Complete", completePendingIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    private void saveReminderEvent(Context context, String action) {
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

