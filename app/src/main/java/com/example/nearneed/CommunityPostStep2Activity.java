package com.example.nearneed;

import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class CommunityPostStep2Activity extends AppCompatActivity {

    private int volunteerCount = 12;
    private PostViewModel postViewModel;
    private String title = "";
    private String description = "";
    private String category = "";
    private String postType = "community";
    private ArrayList<String> selectedImageUris = new ArrayList<>();

    private android.widget.TextView tvCount;
    private android.widget.TextView tvSelectedDate;
    private android.widget.TextView tvSelectedTime;
    private EditText searchEdit;
    private android.view.View successOverlay;
    private MaterialButton btnContinueStep2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_post_step2);

        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);

        if (getIntent() != null) {
            String incomingType = getIntent().getStringExtra("post_type");
            if (incomingType != null) {
                postType = incomingType;
            }
            String incomingTitle = getIntent().getStringExtra("title");
            String incomingDescription = getIntent().getStringExtra("description");
            String incomingCategory = getIntent().getStringExtra("category");
            if (incomingTitle != null) title = incomingTitle;
            if (incomingDescription != null) description = incomingDescription;
            if (incomingCategory != null) category = incomingCategory;

            ArrayList<String> images = getIntent().getStringArrayListExtra("selected_images");
            if (images != null) selectedImageUris = images;
        }

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tvCount = findViewById(R.id.tvVolunteerCount);
        if (tvCount != null) {
            tvCount.setText(String.valueOf(volunteerCount));
        }

        ViewContainerFinder.bindClick(findViewById(R.id.btnPlus), v -> {
            volunteerCount++;
            if (tvCount != null) tvCount.setText(String.valueOf(volunteerCount));
        });
        ViewContainerFinder.bindClick(findViewById(R.id.btnMinus), v -> {
            if (volunteerCount > 1) {
                volunteerCount--;
                if (tvCount != null) tvCount.setText(String.valueOf(volunteerCount));
            }
        });

        com.google.android.material.switchmaterial.SwitchMaterial switchLimit = findViewById(R.id.switchLimitVolunteers);
        if (switchLimit != null) {
            switchLimit.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String msg = isChecked ? "Volunteer limit enabled" : "Volunteer limit disabled";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            });
        }

        ViewContainerFinder.bindClick(findViewById(R.id.layoutDate), v -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            android.app.DatePickerDialog dpd = new android.app.DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                tvSelectedDate = findViewById(R.id.tvSelectedDate);
                if (tvSelectedDate != null) {
                    tvSelectedDate.setText((month + 1) + "/" + dayOfMonth + "/" + year);
                    tvSelectedDate.setTextColor(0xFF0F172A);
                }
            }, cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH));
            dpd.show();
        });

        ViewContainerFinder.bindClick(findViewById(R.id.layoutTime), v -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            android.app.TimePickerDialog tpd = new android.app.TimePickerDialog(this, (view, hourOfDay, minute) -> {
                tvSelectedTime = findViewById(R.id.tvSelectedTime);
                String ampm = hourOfDay >= 12 ? "PM" : "AM";
                int displayHour = hourOfDay > 12 ? hourOfDay - 12 : (hourOfDay == 0 ? 12 : hourOfDay);
                if (tvSelectedTime != null) {
                    tvSelectedTime.setText(String.format("%02d:%02d %s", displayHour, minute, ampm));
                    tvSelectedTime.setTextColor(0xFF0F172A);
                }
            }, cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE), false);
            tpd.show();
        });

        searchEdit = findViewById(R.id.searchEditText);
        DashboardSearchHelper.bindMapSearchShortcut(searchEdit, this);

        successOverlay = findViewById(R.id.layout_success_overlay);
        btnContinueStep2 = findViewById(R.id.btnContinueStep2);
        if (btnContinueStep2 != null) {
            btnContinueStep2.setOnClickListener(v -> saveCommunityPost());
        }

        ViewContainerFinder.bindClick(findViewById(R.id.btnSuccessClose), v -> {
            if (successOverlay != null) successOverlay.setVisibility(android.view.View.GONE);
        });

        ViewContainerFinder.bindClick(findViewById(R.id.btnBackHome), v -> {
            android.content.Intent intent = new android.content.Intent(this, HomeProviderActivity.class);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    private void saveCommunityPost() {
        if (btnContinueStep2 != null) {
            btnContinueStep2.setEnabled(false);
            btnContinueStep2.setText("Saving...");
        }

        List<String> downloadUrls = new ArrayList<>();
        if (selectedImageUris == null || selectedImageUris.isEmpty()) {
            finalizeSaveCommunityPost(downloadUrls);
            return;
        }

        uploadImagesRecursively(0, downloadUrls);
    }

    private void uploadImagesRecursively(int index, List<String> downloadUrls) {
        if (index >= selectedImageUris.size()) {
            finalizeSaveCommunityPost(downloadUrls);
            return;
        }

        Uri uri = Uri.parse(selectedImageUris.get(index));
        StorageRepository.uploadImage(uri, "posts", new StorageRepository.UploadCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                downloadUrls.add(downloadUrl);
                uploadImagesRecursively(index + 1, downloadUrls);
            }

            @Override
            public void onFailure(Exception e) {
                uploadImagesRecursively(index + 1, downloadUrls);
            }
        });
    }

    private void finalizeSaveCommunityPost(List<String> imageUrls) {
        Post post = new Post();
        post.type = "COMMUNITY";
        post.title = title;
        post.description = description;
        post.category = category;
        post.location = searchEdit != null ? searchEdit.getText().toString().trim() : "";
        post.preferredDate = tvSelectedDate != null ? tvSelectedDate.getText().toString() : "";
        post.preferredTime = tvSelectedTime != null ? tvSelectedTime.getText().toString() : "";
        post.timestamp = System.currentTimeMillis();
        post.status = "active";
        post.imageUrls = imageUrls;
        post.volunteersNeeded = volunteerCount;
        post.slots = volunteerCount;
        post.slotsFilled = 0;

        postViewModel.createPost(post, new PostRepository.SaveCallback() {
            @Override
            public void onSuccess(String postId) {
                if (btnContinueStep2 != null) {
                    btnContinueStep2.setText("Posted");
                }
                if (successOverlay != null) {
                    successOverlay.setVisibility(android.view.View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (btnContinueStep2 != null) {
                    btnContinueStep2.setEnabled(true);
                    btnContinueStep2.setText("Continue");
                }
                Toast.makeText(CommunityPostStep2Activity.this, "Failed to post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

final class ViewContainerFinder {
    private ViewContainerFinder() {}

    static void bindClick(android.view.View view, android.view.View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }
}
