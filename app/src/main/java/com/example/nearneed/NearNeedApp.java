package com.example.nearneed;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;

public class NearNeedApp extends Application {

    private static final String TAG = "NearNeedApp";
    private static boolean firebaseReady = false;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            FirebaseApp.initializeApp(this);
            firebaseReady = !FirebaseApp.getApps(this).isEmpty();
            Log.d(TAG, "Firebase initialized: " + firebaseReady);
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization failed — google-services.json missing", e);
            firebaseReady = false;
        }
    }

    public static boolean isFirebaseReady() {
        return firebaseReady;
    }
}
