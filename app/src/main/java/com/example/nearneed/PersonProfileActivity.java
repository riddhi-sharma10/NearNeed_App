package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class PersonProfileActivity extends AppCompatActivity {

    private ListenerRegistration profileListener;
    private String personUserId;
    private String personName = "User";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        // Accept both key names for backward compatibility
        personUserId = getIntent().getStringExtra("PERSON_USER_ID");
        if (personUserId == null || personUserId.isEmpty()) {
            personUserId = getIntent().getStringExtra("user_id");
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getOnBackPressedDispatcher().hasEnabledCallbacks()) {
                getOnBackPressedDispatcher().onBackPressed();
            } else {
                finish();
            }
        });

        // Wire up reviews tap — name comes from the live TextView
        TextView tvName = findViewById(R.id.tvName);
        View btnReviews = findViewById(R.id.btnReviewsPerson);
        View.OnClickListener openReviews = v -> {
            Intent intent = new Intent(PersonProfileActivity.this, ReviewsActivity.class);
            intent.putExtra("PERSON_NAME",
                tvName != null && tvName.getText() != null ? tvName.getText().toString() : "");
            startActivity(intent);
        };
        if (btnReviews != null) btnReviews.setOnClickListener(openReviews);

        // Message button — only shown for other users, wired up after userId is resolved
        MaterialButton btnMessage = findViewById(R.id.btnMessagePerson);
        if (btnMessage != null) {
            boolean isOtherUser = personUserId != null && !personUserId.isEmpty() && !isCurrentUser(personUserId);
            btnMessage.setVisibility(isOtherUser ? View.VISIBLE : View.GONE);
            if (isOtherUser) {
                btnMessage.setOnClickListener(v -> openChatWithPerson());
            }
        }
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
        String userId = personUserId;
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
        String name      = DbConstants.getSafeName(snapshot);
        String photoUrl  = snapshot.getString("photoUrl");
        String bio       = snapshot.getString("bio");
        String phone     = snapshot.getString("phone");
        String gender    = snapshot.getString("gender");
        Boolean verified = snapshot.getBoolean("isVerified");
        Double rating    = snapshot.getDouble("rating");
        Long reviewCount = snapshot.getLong("reviewCount");
        String location  = snapshot.getString("location");

        if (name != null && !name.isEmpty()) {
            personName = name;
            // Update button label to "Message <Name>"
            MaterialButton btnMessage = findViewById(R.id.btnMessagePerson);
            if (btnMessage != null && btnMessage.getVisibility() == View.VISIBLE) {
                btnMessage.setText("Message " + name);
            }
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
        setText(R.id.tvPhone,    phone);
        setText(R.id.tvGender,   gender);
        setText(R.id.tvBio,      bio);

        TextView tvLocation = findViewById(R.id.tvLocation);
        if (tvLocation != null) {
            tvLocation.setText(location != null && !location.isEmpty() ? location : "Location not provided");
        }

        // Hide rows if incomplete
        View llPhoneRow = findViewById(R.id.llPhoneRow);
        if (llPhoneRow != null) {
            llPhoneRow.setVisibility(phone != null && !phone.isEmpty() ? View.VISIBLE : View.GONE);
        }

        View llGenderRow = findViewById(R.id.llGenderRow);
        if (llGenderRow != null) {
            llGenderRow.setVisibility(gender != null && !gender.isEmpty() ? View.VISIBLE : View.GONE);
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
                .centerCrop()
                .into(ivProfile);
        }
    }

    private void openChatWithPerson() {
        if (personUserId == null || personUserId.isEmpty()) return;
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("CHAT_USER_ID", personUserId);
        intent.putExtra("CHAT_NAME", personName != null ? personName : "User");
        startActivity(intent);
    }

    // ── Helpers ──

    private void setText(int viewId, String text) {
        if (text == null || text.isEmpty()) return;
        TextView tv = findViewById(viewId);
        if (tv != null) tv.setText(text);
    }


}
