package com.example.nearneed;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * ReviewsActivity
 * Displays real-time reviews for a given provider (REVIEWEE_USER_ID intent extra).
 * Falls back to the current user if no ID is passed.
 */
public class ReviewsActivity extends AppCompatActivity {

    private RecyclerView rvReviews;
    private ReviewsAdapter adapter;
    private List<Review> reviewList = new ArrayList<>();
    private ListenerRegistration reviewListener;

    private TextView tvAvgRating;
    private TextView tvReviewCount;
    private View emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Title
        TextView tvTitle = findViewById(R.id.tvReviewsTitle);
        String personName = getIntent().getStringExtra("PERSON_NAME");
        if (tvTitle != null && personName != null && !personName.trim().isEmpty()) {
            tvTitle.setText(personName + "'s Reviews");
        }

        // Rating summary views (may not exist in all layout versions — safe null checks)
        tvAvgRating   = findViewById(R.id.tvAvgRating);
        tvReviewCount = findViewById(R.id.tvReviewCount);
        emptyState    = findViewById(R.id.emptyReviewsState);

        // RecyclerView setup
        rvReviews = findViewById(R.id.rvReviews);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReviewsAdapter(reviewList);
        rvReviews.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachReviewListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (reviewListener != null) {
            reviewListener.remove();
            reviewListener = null;
        }
    }

    // ── Firestore real-time listener ─────────────────────────────────────────

    private void attachReviewListener() {
        // Caller can pass the provider's UID to show their reviews.
        // If not provided, fall back to current user.
        String revieweeId = getIntent().getStringExtra("REVIEWEE_USER_ID");
        if (revieweeId == null || revieweeId.isEmpty()) {
            com.google.firebase.auth.FirebaseUser me =
                    com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (me != null) revieweeId = me.getUid();
        }
        if (revieweeId == null) return;

        final String finalRevieweeId = revieweeId;

        reviewListener = FirebaseFirestore.getInstance()
                .collection("reviews")
                .whereEqualTo("revieweeId", finalRevieweeId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    reviewList.clear();
                    double totalRating  = 0;
                    int    ratedCount   = 0;

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Review r = new Review();
                        r.reviewId    = doc.getId();
                        r.reviewerId  = doc.getString("reviewerId");
                        r.revieweeId  = doc.getString("revieweeId");
                        r.bookingId   = doc.getString("bookingId");
                        r.postId      = doc.getString("postId");
                        r.comment     = doc.getString("comment");
                        r.reviewerName = doc.getString("reviewerName");

                        // Support both Double and Long field types
                        Double ratingVal = doc.getDouble("rating");
                        r.rating = ratingVal != null ? ratingVal.floatValue() : 0f;

                        Long createdAt = doc.getLong("createdAt");
                        r.createdAt = createdAt != null ? createdAt : 0L;

                        reviewList.add(r);

                        if (r.rating > 0) {
                            totalRating += r.rating;
                            ratedCount++;
                        }
                    }

                    adapter.notifyDataSetChanged();

                    // Update summary header
                    if (tvAvgRating != null && ratedCount > 0) {
                        tvAvgRating.setText(String.format("%.1f", totalRating / ratedCount));
                    }
                    if (tvReviewCount != null) {
                        tvReviewCount.setText(snapshots.size() + " review" + (snapshots.size() != 1 ? "s" : ""));
                    }
                    if (emptyState != null) {
                        emptyState.setVisibility(reviewList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }
}
