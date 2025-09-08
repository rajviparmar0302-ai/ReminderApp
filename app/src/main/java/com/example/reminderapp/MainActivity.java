package com.example.reminderapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;

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
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseUser user;



    private static final int REMINDER_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        etInterval = findViewById(R.id.etInterval);
        btnStart = findViewById(R.id.startReminder);
        btnStop = findViewById(R.id.stopReminder);
        listViewReminders = findViewById(R.id.listViewReminders);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

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


        if (user != null) {
            // Toolbar setup
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this,
                    drawerLayout,
                    toolbar,
                    R.string.open,
                    R.string.close
            );
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
            DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
            NavigationView navigationView = findViewById(R.id.nav_view);
//            MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
//
//            topAppBar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

//            NavigationView navigationView = findViewById(R.id.navigation_view);
            navigationView.setNavigationItemSelectedListener(item -> {
                Fragment selectedFragment = null;

                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (id == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                } else if (id == R.id.nav_settings) {
                    selectedFragment = new SettingsFragment();
                } else if (id == R.id.nav_about) {
                    selectedFragment = new AboutFragment();

                } else if (id == R.id.nav_logout) {
                    Toast.makeText(this, "Logout clicked", Toast.LENGTH_SHORT).show();
                    drawerLayout.closeDrawers();
                    return true;
                }

                if (selectedFragment != null) {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.content_frame, selectedFragment);
                    transaction.commit();
                }

                drawerLayout.closeDrawers();
                return true;
            });


            // Set user details in header
            View headerView = navigationView.getHeaderView(0);
            TextView userName = headerView.findViewById(R.id.userName);
            TextView userEmail = headerView.findViewById(R.id.userEmail);
            ImageView userImage = headerView.findViewById(R.id.userImage);

            userName.setText(user.getDisplayName() != null ? user.getDisplayName() : "No Name");
            userEmail.setText(user.getEmail());

            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .placeholder(R.drawable.ic_user_placeholder) // add a default placeholder in drawable
                        .into(userImage);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
                }
            }
            // Handle menu item clicks
            navigationView.setNavigationItemSelectedListener(item -> {
                if (item.getItemId() == R.id.nav_logout) {
                    mAuth.signOut();
                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(i);
                    finish();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });

            // Firebase database
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
        long intervalMilli = intervalMinutes * 60L * 1000L;
        long triggerTime = System.currentTimeMillis() + intervalMilli;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    setExactAlarm(triggerTime, intervalMilli);
                } else {
                    Toast.makeText(this, "Enable exact alarms in settings.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }
            } else {
                setExactAlarm(triggerTime, intervalMilli);
            }

            saveReminderToRealtimeDB(intervalMinutes, triggerTime);

        } catch (SecurityException e) {
            Toast.makeText(this, "Permission denied for exact alarms", Toast.LENGTH_SHORT).show();
        }
    }

    private void setExactAlarm(long triggerTime, long intervalMilli) {
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                intervalMilli,
                pendingIntent
        );
        Toast.makeText(this, "Reminder set successfully!", Toast.LENGTH_SHORT).show();
    }

    private void saveReminderToRealtimeDB(int intervalMinutes, long triggerTime) {
        if (user == null || databaseRef == null) return;

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
