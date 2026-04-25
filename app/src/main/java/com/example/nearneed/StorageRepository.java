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
     * Upload an image from a local Uri.
     */
    public static void uploadImage(Uri imageUri, String folder, UploadCallback callback) {
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
