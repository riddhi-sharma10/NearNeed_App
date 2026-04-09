package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class IdVerifiedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_id_verified);

        MaterialButton btnContinue = findViewById(R.id.btnContinue);

        btnContinue.setOnClickListener(v -> {
            // End of ID Verification. Go to Profile Success.
            Intent intent = new Intent(this, ProfileSuccessActivity.class);
            startActivity(intent);
        });
    }
}
