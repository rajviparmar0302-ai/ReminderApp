package com.example.reminderapp;

import android.os.Bundle;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AboutFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d("AboutFragment", "About page opened!");
        Log.d("AboutFragment", "AboutFragment loaded");
        return inflater.inflate(R.layout.fragment_about, container, false);
    }
}
