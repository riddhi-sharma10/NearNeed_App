package com.example.nearneed;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom dialog for submitting user reviews and ratings.
 */
public class RatingDialog {

    public interface RatingCallback {
        void onSubmitted();
        void onCancelled();
    }

    public static void show(Context context, String bookingId, String postId, String revieweeId, RatingCallback callback) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_rating);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);
        EditText etComment = dialog.findViewById(R.id.etReviewComment);
        TextView btnSubmit = dialog.findViewById(R.id.btnSubmitReview);
        TextView btnCancel = dialog.findViewById(R.id.btnMaybeLater);

        btnSubmit.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            if (rating == 0) {
                Toast.makeText(context, "Please select a rating", Toast.LENGTH_SHORT).show();
                return;
            }

            String comment = etComment.getText().toString().trim();
            submitReview(bookingId, postId, revieweeId, rating, comment, new RatingCallback() {
                @Override
                public void onSubmitted() {
                    dialog.dismiss();
                    if (callback != null) callback.onSubmitted();
                }

                @Override
                public void onCancelled() {
                    // Not used here
                }
            });
        });

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            if (callback != null) callback.onCancelled();
        });

        dialog.show();
    }

    private static void submitReview(String bookingId, String postId, String revieweeId, float rating, String comment, RatingCallback callback) {
        String reviewerId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        if (reviewerId.isEmpty()) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch reviewer's name first so it's stored with the review document
        db.collection("users").document(reviewerId).get()
                .addOnSuccessListener(reviewerSnap -> {
                    String reviewerName = "Anonymous";
                    if (reviewerSnap.exists()) {
                        String n = reviewerSnap.getString("fullName");
                        if (n == null || n.isEmpty()) n = reviewerSnap.getString("name");
                        if (n != null && !n.isEmpty()) reviewerName = n;
                    }

                    final String finalName = reviewerName;

                    // Build review data map (not POJO) to include reviewerName
                    java.util.Map<String, Object> reviewData = new java.util.HashMap<>();
                    reviewData.put("bookingId",    bookingId);
                    reviewData.put("postId",       postId);
                    reviewData.put("reviewerId",   reviewerId);
                    reviewData.put("revieweeId",   revieweeId);
                    reviewData.put("rating",       (double) rating);
                    reviewData.put("comment",      comment);
                    reviewData.put("reviewerName", finalName);
                    reviewData.put("createdAt",    System.currentTimeMillis());

                    db.collection("reviews").add(reviewData)
                            .addOnSuccessListener(docRef -> {
                                updateUserRating(revieweeId, rating);
                                callback.onSubmitted();
                            })
                            .addOnFailureListener(e -> callback.onCancelled());
                })
                .addOnFailureListener(e -> callback.onCancelled());
    }

    private static void updateUserRating(String userId, float newRating) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        com.google.firebase.firestore.DocumentReference userRef = db.collection("users").document(userId);

        db.runTransaction(transaction -> {
            com.google.firebase.firestore.DocumentSnapshot snapshot = transaction.get(userRef);
            double totalRating = 0;
            long reviewCount  = 0;

            if (snapshot.contains("totalRating"))  totalRating  = snapshot.getDouble("totalRating");
            if (snapshot.contains("reviewCount"))  reviewCount  = snapshot.getLong("reviewCount");

            totalRating += newRating;
            reviewCount++;

            double avg = totalRating / reviewCount;

            transaction.update(userRef, "totalRating",   totalRating);
            transaction.update(userRef, "reviewCount",   reviewCount);
            // Write both field names so all UI readers get the value
            transaction.update(userRef, "averageRating", avg);
            transaction.update(userRef, "rating",        avg);

            return null;
        });
    }
}
