package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.ListenerRegistration;

public class CommunityPostDetailActivity extends AppCompatActivity {

    private boolean isProvider;
    private String postId;
    private TextView tvTitle;
    private TextView tvDescription;
    private TextView tvPostedBy;
    private TextView tvPostedTime;
    private TextView tvLocation;
    private TextView tvSlotsAvailable;
    private MaterialButton btnVolunteer;
    private ListenerRegistration postListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_post_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvTitle = findViewById(R.id.tv_community_title);
        tvDescription = findViewById(R.id.tv_community_description);
        tvPostedBy = findViewById(R.id.tv_posted_by);
        tvPostedTime = findViewById(R.id.tv_posted_time);
        tvLocation = findViewById(R.id.tv_community_location);
        tvSlotsAvailable = findViewById(R.id.tv_slots_available);
        btnVolunteer = findViewById(R.id.btn_volunteer);

        Intent intent = getIntent();
        String postId = intent.getStringExtra("post_id");
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        String postedBy = intent.getStringExtra("postedBy");
        String postedTime = intent.getStringExtra("postedTime");
        String location = intent.getStringExtra("location");
        String slots = intent.getStringExtra("slots");
        boolean posterVerified = intent.getBooleanExtra("IS_POSTER_VERIFIED", false);
        isProvider = RoleManager.isProvider(this);
        this.postId = postId;

        tvTitle.setText(title != null ? title : "Community Need");
        tvDescription.setText(description != null ? description : "No description available");
        tvPostedBy.setText(postedBy != null ? postedBy : "Posted by someone");
        VerifiedBadgeHelper.apply(this, tvPostedBy, posterVerified);
        tvPostedTime.setText(postedTime != null ? postedTime : "Recently");
        tvLocation.setText(location != null ? location : "Location unknown");
        tvSlotsAvailable.setText(slots != null ? slots : "Volunteers needed");

        btnVolunteer.setText(isProvider ? "Apply" : "View Applicants");

        btnVolunteer.setOnClickListener(v -> {
            if (isProvider) {
                new CommunityVolunteerBottomSheet()
                    .show(getSupportFragmentManager(), "community_volunteer_sheet");
                return;
            }

            String postTitle = tvTitle.getText().toString();
            int maxSlots = parseMaxSlots(tvSlotsAvailable.getText().toString());

            Intent volunteersIntent = new Intent(CommunityPostDetailActivity.this, VolunteersActivity.class);
            volunteersIntent.putExtra("post_id", postId);
            volunteersIntent.putExtra("post_title", postTitle);
            volunteersIntent.putExtra("max_slots", maxSlots);
            volunteersIntent.putExtra("is_seeker", true);
            startActivity(volunteersIntent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (postId == null || postId.trim().isEmpty()) {
            return;
        }

        postListener = PostRepository.observePostById(postId, new PostRepository.PostListener() {
            @Override
            public void onPostsLoaded(java.util.List<Post> posts) {
                if (posts == null || posts.isEmpty()) {
                    return;
                }

                Post post = posts.get(0);
                applyPost(post);
            }

            @Override
            public void onError(Exception e) {
                // Keep the last visible values if live sync fails.
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (postListener != null) {
            postListener.remove();
            postListener = null;
        }
    }

    private void applyPost(Post post) {
        if (post == null) {
            return;
        }

        if (tvTitle != null && post.title != null) {
            tvTitle.setText(post.title);
        }
        if (tvDescription != null && post.description != null && !post.description.isEmpty()) {
            tvDescription.setText(post.description);
        }
        if (tvPostedBy != null && post.postedBy != null && !post.postedBy.isEmpty()) {
            tvPostedBy.setText(post.postedBy);
        }
        if (tvPostedTime != null && post.preferredDate != null && !post.preferredDate.isEmpty()) {
            tvPostedTime.setText(post.preferredDate);
        }
        if (tvLocation != null && post.location != null && !post.location.isEmpty()) {
            tvLocation.setText(post.location);
        }
        if (tvSlotsAvailable != null) {
            tvSlotsAvailable.setText(formatSlots(post));
        }
    }

    private String formatSlots(Post post) {
        Integer totalSlots = post.volunteersNeeded != null ? post.volunteersNeeded : post.slots;
        Integer filledSlots = post.slotsFilled != null ? post.slotsFilled : 0;

        if (totalSlots == null) {
            return "Volunteers needed";
        }

        int remaining = Math.max(totalSlots - filledSlots, 0);
        return remaining + "/" + totalSlots + " slots";
    }

    private int parseMaxSlots(String slotsText) {
        if (slotsText == null) {
            return 5;
        }

        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\d+").matcher(slotsText);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group());
            } catch (NumberFormatException ignored) {
                // Fall back to default below.
            }
        }
        return 5;
    }
}
