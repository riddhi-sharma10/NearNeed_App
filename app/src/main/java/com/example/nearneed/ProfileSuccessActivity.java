package com.example.nearneed;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class ProfileSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_success);

        View successContainer = findViewById(R.id.successContainer);
        TextView tvUserNamePreview = findViewById(R.id.tvUserNamePreview);
        TextView tvJoinedDate = findViewById(R.id.tvJoinedDate);
        com.google.android.material.imageview.ShapeableImageView ivProfilePicturePreview = findViewById(R.id.ivProfilePicturePreview);
        MaterialButton btnDone = findViewById(R.id.btnDone);

        // Sync real-time data
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Set Joined Date from Firebase Metadata
            long creationTimestamp = user.getMetadata().getCreationTimestamp();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault());
            String joinedDate = "Joined " + sdf.format(new java.util.Date(creationTimestamp));
            tvJoinedDate.setText(joinedDate);

            // Fetch name and photo from Firestore for absolute accuracy
            com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .get().addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        if (name != null && !name.isEmpty()) tvUserNamePreview.setText(name);
                        
                        String photoUrl = doc.getString("profileImageUrl");
                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            com.bumptech.glide.Glide.with(this).load(photoUrl).into(ivProfilePicturePreview);
                        }
                    }
                });
        }

        // Apply Pop-in effect
        applyPopAnimation(successContainer);

        btnDone.setOnClickListener(v -> {
            Intent intent = new Intent(this, AccountTypeActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void applyPopAnimation(View view) {
        view.setScaleX(0.7f);
        view.setScaleY(0.7f);
        view.setAlpha(0f);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.7f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.7f, 1.0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1.0f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, alpha);
        set.setDuration(800);
        set.setInterpolator(new OvershootInterpolator(1.4f));
        set.start();
    }
}
