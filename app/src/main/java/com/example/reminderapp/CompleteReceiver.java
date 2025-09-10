package com.example.reminderapp;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.app.PendingIntent;

import java.util.HashMap;
import java.util.Map;

public class CompleteReceiver extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 100;
    private static final int REMINDER_REQUEST_CODE = 100;

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Reminder Completed!", Toast.LENGTH_SHORT).show();

        // Cancel the alarm
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            Intent alarmIntent = new Intent(context, ReminderReceiver.class);
            PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(
                    context,
                    REMINDER_REQUEST_CODE,  // same request code
                    alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            alarmManager.cancel(alarmPendingIntent);
        }

        // Cancel the notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(NOTIFICATION_ID);
        }


        saveReminderEvent("Reminder Completed");
    }

    private void saveReminderEvent(String action) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // Get database reference
        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid())
                .child("reminderEvents");

        // Create event object
        Map<String, Object> event = new HashMap<>();
        event.put("action", action);
        event.put("timestamp", System.currentTimeMillis());

        // Push event to Realtime Database
        dbRef.push().setValue(event)
                .addOnSuccessListener(aVoid -> {
                    Log.d("RealtimeDB", "Event saved: " + action);
                })
                .addOnFailureListener(e -> {
                    Log.e("RealtimeDB", "Error saving event", e);
                });
    }
}
