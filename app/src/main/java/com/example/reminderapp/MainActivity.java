package com.example.reminderapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.internal.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.widget.ArrayAdapter;

public class MainActivity extends AppCompatActivity {

    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private EditText etInterval;
    private Button btnStart, btnStop;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private ListView listViewReminders;
    private ArrayList<String> reminders;
    private ArrayAdapter<String> adapter;

    private static final int REMINDER_REQUEST_CODE = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        etInterval = findViewById(R.id.etInterval);
        btnStart = findViewById(R.id.startReminder);
        btnStop = findViewById(R.id.stopReminder);
        listViewReminders = findViewById(R.id.listViewReminders);

        // Initialize ArrayList & Adapter
        reminders = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, reminders);
        listViewReminders.setAdapter(adapter);

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

        // Setup Realtime Database Reference
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
//            mDatabase = FirebaseDatabase.getInstance().getReference();

//            Map<String, Object> updates = new HashMap<>();
//            updates.put("name", user.getDisplayName());
//            updates.put("email", user.getEmail());
//
//            FirebaseDatabase.getInstance().getReference().child("users")
//                    .child(user.getUid())
//                    .child("userDetails")
//                    .updateChildren(updates);
            databaseRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(user.getUid())
                    .child("reminders");

            loadReminderHistory();
        } else {
            Toast.makeText(this, "User not signed in!", Toast.LENGTH_SHORT).show();
        }
    }

    private void startReminder() {
        String intervalText = etInterval.getText().toString().trim();

        if (intervalText.isEmpty()) {
            Toast.makeText(this, "Please enter interval in minutes", Toast.LENGTH_SHORT).show();
            return;
        }

        int intervalMinutes = Integer.parseInt(intervalText);
        long triggerTime = System.currentTimeMillis() + (intervalMinutes * 60L * 1000L);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    setExactAlarm(triggerTime);
                } else {
                    Toast.makeText(this, "Enable exact alarms in settings.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }
            } else {
                setExactAlarm(triggerTime);
            }

            saveReminderToRealtimeDB(intervalMinutes, triggerTime);

        } catch (SecurityException e) {
            Toast.makeText(this, "Permission denied for exact alarms", Toast.LENGTH_SHORT).show();
        }
    }

    private void setExactAlarm(long triggerTime) {
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
    }

    private void saveReminderToRealtimeDB(int intervalMinutes, long triggerTime) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || databaseRef == null) return;
//        try {
//            mDatabase.child("test").setValue(user);
//        } catch (Exception e) {
//            Toast.makeText(this, "Test save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
//        }
        String reminderId = databaseRef.push().getKey();

        Map<String, Object> reminder = new HashMap<>();
        reminder.put("intervalMinutes", intervalMinutes);
        reminder.put("triggerTime", triggerTime);

        if (reminderId != null) {
            databaseRef.child(reminderId).setValue(reminder)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this, "Reminder saved!", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to save reminder", Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void stopReminder() {
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Toast.makeText(this, "Reminder stopped", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadReminderHistory() {
        if (databaseRef == null) return;

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                reminders.clear();
                for (DataSnapshot doc : snapshot.getChildren()) {

                    Long triggerTime = doc.child("triggerTime").getValue(Long.class);
                    Integer intervalMinutes = doc.child("intervalMinutes").getValue(Integer.class);

                    if (triggerTime != null && intervalMinutes != null) {
                        reminders.add("Reminder: " + intervalMinutes + " min, at " + new Date(triggerTime));
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load reminders", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
