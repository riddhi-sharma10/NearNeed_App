package com.example.nearneed;

import android.net.Uri;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.UUID;

/**
 * Repository for managing Firebase Storage operations.
 */
public class StorageRepository {

    public interface UploadCallback {
        void onSuccess(String downloadUrl);
        void onFailure(Exception e);
    }

    /**
     * Upload an image from a local Uri with optional moderation.
     */
    public static void uploadImage(Uri imageUri, String folder, UploadCallback callback) {
        // Step 1: Perform ML Image Moderation
        moderateImage(imageUri, new UploadCallback() {
            @Override
            public void onSuccess(String status) {
                // Image is safe, proceed with upload
                performUpload(imageUri, folder, callback);
            }

            @Override
            public void onFailure(Exception e) {
                if (callback != null) callback.onFailure(new Exception("Image rejected by safety filter: " + e.getMessage()));
            }
        });
    }

    private static void moderateImage(Uri uri, UploadCallback callback) {
        // In a production app, this would use Google Cloud Vision API or a Firebase Extension.
        // For this ML implementation, we simulate the Cloud Vision check.
        // It could also be implemented with ML Kit Object Detection for local pre-filtering.
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            boolean isSafe = true; // Simulation: assume safe for now
            if (isSafe) {
                callback.onSuccess("Safe");
            } else {
                callback.onFailure(new Exception("Inappropriate content detected"));
            }
        }, 800);
    }

    private static void performUpload(Uri imageUri, String folder, UploadCallback callback) {
        if (imageUri == null || callback == null) return;

        String fileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(folder).child(fileName);

        ref.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return ref.getDownloadUrl();
                })
                .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Upload an audio file from a local path.
     */
    public static void uploadAudio(String localPath, UploadCallback callback) {
        if (localPath == null || callback == null) return;

        Uri fileUri = Uri.fromFile(new File(localPath));
        String fileName = UUID.randomUUID().toString() + ".m4a";
        StorageReference ref = FirebaseStorage.getInstance().getReference().child("audio").child(fileName);

        ref.putFile(fileUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return ref.getDownloadUrl();
                })
                .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()))
                .addOnFailureListener(callback::onFailure);
    }
}
