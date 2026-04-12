package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class AddScheduleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_schedule);

        ImageButton btnBack = findViewById(R.id.btnBack);
        MaterialButton btnAddSchedule = findViewById(R.id.btnAddSchedule);

        btnBack.setOnClickListener(v -> finish());

        btnAddSchedule.setOnClickListener(v -> {
            Toast.makeText(this, "Schedule Item Added Successfully", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
