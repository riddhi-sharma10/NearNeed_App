package com.example.nearneed;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import androidx.lifecycle.ViewModelProvider;
import com.google.firebase.auth.FirebaseAuth;
import android.net.Uri;

public class CreatePostStep2Activity extends AppCompatActivity {

    private TextView tvNotesCharCount, tvDisplayDate, tvDisplayTime, tvLocationSearch;
    private EditText etAdditionalNotes;
    private MaterialButton btnPostRequest;
    
    private List<MaterialCardView> urgencyCards = new ArrayList<>();
    private List<TextView> urgencyTexts = new ArrayList<>();
    private List<ImageView> urgencyIcons = new ArrayList<>();
    private String selectedUrgency = "Urgent"; // Default
    private ArrayList<String> selectedImageUris = new ArrayList<>();
    
    private String postType, title, description, category;
    private PostViewModel postViewModel;
    private Double selectedLat = 28.4595; // Default Gurgaon
    private Double selectedLng = 77.0266;
    private Long selectedScheduledDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post_step2);

        if (getIntent() != null) {
            postType = getIntent().getStringExtra("post_type");
            title = getIntent().getStringExtra("title");
            description = getIntent().getStringExtra("description");
            category = getIntent().getStringExtra("category");
            selectedImageUris = getIntent().getStringArrayListExtra("selected_images");
            if (selectedImageUris == null) selectedImageUris = new ArrayList<>();
        }

        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);

        initViews();
        setupListeners();
        updatePostButtonState();
    }

    private void initViews() {
        etAdditionalNotes = findViewById(R.id.etAdditionalNotes);
        tvNotesCharCount = findViewById(R.id.tvNotesCharCount);
        tvDisplayDate = findViewById(R.id.tvDisplayDate);
        tvDisplayTime = findViewById(R.id.tvDisplayTime);
        tvLocationSearch = findViewById(R.id.tvLocationSearch);
        btnPostRequest = findViewById(R.id.btnPostRequest);



        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        

        findViewById(R.id.llLocationSearch).setOnClickListener(v -> {
            LocationPickerHelper.show(this, (displayText, lat, lng) -> {
                String loc = displayText;
                if (loc.startsWith("DELIVER TO: ")) {
                    loc = loc.substring("DELIVER TO: ".length());
                }
                tvLocationSearch.setText(loc);
                tvLocationSearch.setTextColor(Color.parseColor("#111827"));
                selectedLat = lat;
                selectedLng = lng;
                updatePostButtonState();
            });
        });

        findViewById(R.id.containerSetDate).setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                selectedScheduledDate = selection;
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(selection);
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                String selectedDateString = sdf.format(calendar.getTime());
                tvDisplayDate.setText(selectedDateString);
                tvDisplayDate.setTextColor(Color.parseColor("#111827"));
                updatePostButtonState();
            });

            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });

        findViewById(R.id.containerSetTime).setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(12)
                    .setMinute(0)
                    .setTitleText("Select Time Window")
                    .build();

            timePicker.addOnPositiveButtonClickListener(dialog -> {
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();
                String amPm = (hour >= 12) ? "PM" : "AM";
                int displayHour = (hour > 12) ? hour - 12 : (hour == 0 ? 12 : hour);
                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, minute, amPm);
                
                // Typically post creation might want a window, but a single time is a good start. 
                // Let's just set the selected time.
                tvDisplayTime.setText(selectedTime);
                tvDisplayTime.setTextColor(Color.parseColor("#111827"));
                updatePostButtonState();
            });

            timePicker.show(getSupportFragmentManager(), "TIME_PICKER");
        });

        etAdditionalNotes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s.length();
                tvNotesCharCount.setText(length + "/250");
                if (length >= 250) {
                    tvNotesCharCount.setTextColor(Color.RED);
                } else {
                    tvNotesCharCount.setTextColor(Color.parseColor("#94A3B8"));
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnPostRequest.setOnClickListener(v -> savePost());
    }

    private void savePost() {
        btnPostRequest.setEnabled(false);
        btnPostRequest.setText("Uploading Media...");

        List<String> downloadUrls = new ArrayList<>();
        if (selectedImageUris.isEmpty()) {
            finalizeSavePost(downloadUrls);
        } else {
            uploadImagesRecursively(0, downloadUrls);
        }
    }

    private void uploadImagesRecursively(int index, List<String> downloadUrls) {
        if (index >= selectedImageUris.size()) {
            finalizeSavePost(downloadUrls);
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
                // If one fails, we can either stop or continue. Let's continue for now.
                uploadImagesRecursively(index + 1, downloadUrls);
            }
        });
    }

    private void finalizeSavePost(List<String> imageUrls) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null 
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "anonymous";
        
        Post post = new Post();
        post.createdBy = userId;
        post.type = (postType != null && postType.equalsIgnoreCase("community")) ? "COMMUNITY" : "GIG";
        post.title = title;
        post.description = description;
        post.category = category;
        post.location = tvLocationSearch.getText().toString();
        post.urgency = selectedUrgency;
        post.preferredDate = tvDisplayDate.getText().toString();
        post.scheduledDate = selectedScheduledDate;
        post.preferredTime = tvDisplayTime.getText().toString();
        post.additionalNotes = etAdditionalNotes.getText().toString().trim();
        post.timestamp = System.currentTimeMillis();
        post.status = "active";
        post.imageUrls = imageUrls; // Assuming Post model has this field
        
        post.latitude = selectedLat; 
        post.longitude = selectedLng;

        btnPostRequest.setText("Posting...");

        postViewModel.createPost(post, new PostRepository.SaveCallback() {
            @Override
            public void onSuccess(String postId) {
                Intent intent = new Intent(CreatePostStep2Activity.this, PostedSuccessfullyActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                btnPostRequest.setEnabled(true);
                btnPostRequest.setText("Post Request");
                Toast.makeText(CreatePostStep2Activity.this, "Failed to post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectUrgency(int index) {
        for (int i = 0; i < urgencyCards.size(); i++) {
            urgencyCards.get(i).setStrokeColor(Color.parseColor("#E2E8F0"));
            urgencyCards.get(i).setCardBackgroundColor(Color.WHITE);
            urgencyTexts.get(i).setTextColor(Color.parseColor("#111827"));
            urgencyIcons.get(i).setImageTintList(ColorStateList.valueOf(Color.parseColor("#1E3A8A")));
        }

        MaterialCardView selected = urgencyCards.get(index);
        selected.setStrokeColor(Color.TRANSPARENT);
        selected.setCardBackgroundColor(Color.parseColor("#1E3A8A"));
        urgencyTexts.get(index).setTextColor(Color.WHITE);
        urgencyIcons.get(index).setImageTintList(ColorStateList.valueOf(Color.WHITE));

        selectedUrgency = urgencyTexts.get(index).getText().toString();
        updatePostButtonState();
    }

    private void updatePostButtonState() {
        boolean isDateSet = !tvDisplayDate.getText().toString().contains("Set Date");
        boolean isTimeSet = !tvDisplayTime.getText().toString().contains("Set Time");
        boolean isLocationSet = !tvLocationSearch.getText().toString().contains("Search address");
        
        boolean isEnabled = isDateSet && isTimeSet && isLocationSet;
        btnPostRequest.setEnabled(isEnabled);
        
        if (isEnabled) {
            btnPostRequest.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1E3A8A")));
            btnPostRequest.setAlpha(1.0f);
        } else {
            btnPostRequest.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#CBD5E1")));
            btnPostRequest.setAlpha(0.6f);
        }
    }
}

