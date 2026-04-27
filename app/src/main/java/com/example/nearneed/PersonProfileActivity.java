package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class PersonProfileActivity extends AppCompatActivity {

    private ListenerRegistration profileListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getOnBackPressedDispatcher().hasEnabledCallbacks()) {
                getOnBackPressedDispatcher().onBackPressed();
            } else {
                finish();
            }
        });

        // Wire up reviews/rating tap — name comes from the live TextView
        TextView tvName = findViewById(R.id.tvName);
        View reviewsChip = findViewById(R.id.llReviewsChip);
        View ratingRow = findViewById(R.id.llRatingRow);
        View.OnClickListener openReviews = v -> {
            Intent intent = new Intent(PersonProfileActivity.this, ReviewsActivity.class);
            intent.putExtra("PERSON_NAME",
                tvName != null && tvName.getText() != null ? tvName.getText().toString() : "");
            startActivity(intent);
        };
        if (reviewsChip != null) reviewsChip.setOnClickListener(openReviews);
        if (ratingRow != null) ratingRow.setOnClickListener(openReviews);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startFirestoreListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (profileListener != null) {
            profileListener.remove();
            profileListener = null;
        }
    }

    private void startFirestoreListener() {
        // Prefer an explicit userId passed by the caller; fall back to current user.
        String userId = getIntent().getStringExtra("PERSON_USER_ID");
        if (userId == null || userId.isEmpty()) {
            FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
            if (current != null) userId = current.getUid();
        }
        if (userId == null) return;

        final boolean isCurrentUser = isCurrentUser(userId);

        profileListener = FirebaseFirestore.getInstance()
            .collection(DbConstants.COL_USERS).document(userId)
            .addSnapshotListener((snapshot, error) -> {
                if (error != null || snapshot == null || !snapshot.exists()) return;
                applySnapshot(snapshot, isCurrentUser);
            });
    }

    private boolean isCurrentUser(String userId) {
        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        return current != null && current.getUid().equals(userId);
    }

    private void applySnapshot(DocumentSnapshot snapshot, boolean isCurrentUser) {
        String name        = DbConstants.getSafeName(snapshot);
        String photoUrl    = snapshot.getString("photoUrl");
        String bio         = snapshot.getString("bio");
        String phone       = snapshot.getString("phone");
        String gender      = snapshot.getString("gender");
        String experience  = snapshot.getString("experience");
        Boolean verified   = snapshot.getBoolean("isVerified");
        Double rating      = snapshot.getDouble("rating");
        Long reviewCount   = snapshot.getLong("reviewCount");
        String location    = snapshot.getString("location");

        // Email: authoritative source is Firebase Auth for the current user
        String email;
        if (isCurrentUser) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            email = (user != null && user.getEmail() != null)
                ? user.getEmail()
                : snapshot.getString("email");
        } else {
            email = snapshot.getString("email");
        }

        // ── Name + verified badge ──
        TextView tvName = findViewById(R.id.tvName);
        if (tvName != null && name != null && !name.isEmpty()) {
            tvName.setText(name);
            VerifiedBadgeHelper.apply(this, tvName, Boolean.TRUE.equals(verified));
        }

        // ── Verified chip visibility ──
        LinearLayout llVerified = findViewById(R.id.llVerifiedBadge);
        if (llVerified != null) {
            llVerified.setVisibility(Boolean.TRUE.equals(verified) ? View.VISIBLE : View.GONE);
        }

        // ── Text fields ──
        setText(R.id.tvEmail,      email);
        setText(R.id.tvPhone,      phone);
        setText(R.id.tvGender,     gender);
        setText(R.id.tvExperience, experience);
        setText(R.id.tvBio,        bio);
        setText(R.id.tvLocation,   location);

        if (rating != null) {
            setText(R.id.tvRating, String.format("%.1f", rating));
        }
        if (reviewCount != null) {
            setText(R.id.tvReviews, reviewCount + " reviews");
        }

        // ── Profile photo ──
        ImageView ivProfile = findViewById(R.id.ivProfile);
        if (ivProfile != null) {
            String imageToLoad = (photoUrl != null && !photoUrl.isEmpty()) 
                ? photoUrl 
                : DbConstants.getCatAvatarUrl(snapshot.getId());
                
            Glide.with(this)
                .load(imageToLoad)
                .placeholder(R.drawable.avatar_alex)
                .error(R.drawable.avatar_alex)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                .circleCrop()
                .into(ivProfile);
        }
    }

    // ── Helpers ──

    private void setText(int viewId, String text) {
        if (text == null || text.isEmpty()) return;
        TextView tv = findViewById(viewId);
        if (tv != null) tv.setText(text);
    }


}
