package com.example.reminderapp;


import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Show system splash (Android 12+) or fallback (older versions)
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // User already logged in → Go to MainActivity
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        } else {
            // Not logged in → Go to LoginActivity
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        }

        // Finish splash so user cannot return to it
        finish();
    }
}

