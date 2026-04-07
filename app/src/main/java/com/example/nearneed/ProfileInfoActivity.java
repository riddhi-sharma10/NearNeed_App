package com.example.nearneed;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class ProfileInfoActivity extends AppCompatActivity {

    private EditText etFullName;
    private EditText etBio;
    private TextView tvBioCount;
    private MaterialButton btnContinue;
    private ImageButton btnBack;
    private ImageView ivProfilePicture;
    private FrameLayout profilePicContainer;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    ivProfilePicture.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_info);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etBio = findViewById(R.id.etBio);
        tvBioCount = findViewById(R.id.tvBioCount);
        btnContinue = findViewById(R.id.btnContinue);
        btnBack = findViewById(R.id.btnBack);
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        profilePicContainer = findViewById(R.id.profilePicContainer);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        profilePicContainer.setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        etBio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s != null ? s.length() : 0;
                tvBioCount.setText(length + "/150");
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        btnContinue.setOnClickListener(v -> {
            String name = etFullName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, getString(R.string.txt_please_enter_your_name), Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new android.content.Intent(ProfileInfoActivity.this, ProfileSetupActivity.class));
        });
    }
}
