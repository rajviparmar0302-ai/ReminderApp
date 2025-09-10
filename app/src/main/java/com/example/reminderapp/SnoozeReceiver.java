package com.example.reminderapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import android.content.Context;
import android.content.Intent;
public class SnoozeReceiver extends BroadcastReceiver {

    private static final int REMINDER_REQUEST_CODE = 100;
    private static final String TAG = "SnoozeReciever";

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Reminder Snoozed for 5 mins", Toast.LENGTH_SHORT).show();

        // Reschedule alarm for +5 minutes
        long snoozeTime = System.currentTimeMillis() + (5 * 60 * 1000);
        Intent reminderIntent = new Intent(context, ReminderReceiver.class);

        int intervalMinutes = intent.getIntExtra("intervalMinutes",-1);
        Log.i(TAG, "Interval Minutes : "+intervalMinutes); // Info message
//        Toast.makeText(context, "Reminder Triggered! Interval: " + intervalMinutes + " min", Toast.LENGTH_SHORT).show();
        long time = System.currentTimeMillis() + (intervalMinutes * 60 * 1000);
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
                        snoozeTime,
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent
                );
            }
        }

        // Save event in Firebase
        saveReminderEvent("Reminder Snoozed (5 mins)");
    }

//

    private void saveReminderEvent(String action) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid())
                .child("reminderEvents");

        Map<String, Object> event = new HashMap<>();
        event.put("action", action);
        event.put("timestamp", System.currentTimeMillis());

        dbRef.push().setValue(event)
                .addOnSuccessListener(aVoid ->
                        Log.d("RealtimeDB", "Event saved: " + action))
                .addOnFailureListener(e ->
                        Log.e("RealtimeDB", "Error saving event", e));
    }
}
