# Phase 6: Advanced Functional Features Implementation Plan

This phase focuses on transitioning NearNeed from a static prototype to a context-aware, secure, and production-ready platform.

## Goal
Implement real-time location intelligence, push notifications, and a robust trust/reputation system.

---

## Step 18: Real-Time GPS & Permissions
- **Feature**: Live location tracking.
- **Action**: Replace static coordinates (`28.4595, 77.0266`) with the device's real GPS location.
- **Key Files**:
    - `LocationHelper.java`: (New) Utility for `FusedLocationProviderClient`.
    - `MainActivity.java`: Handle runtime permission requests for `ACCESS_FINE_LOCATION`.

## Step 19: Proximity Search & Radius Filtering
- **Feature**: Radius search (5km/10km/25km).
- **Action**: Update repositories to filter Firestore results based on geographic distance from the user.
- **Key Files**:
    - `PostRepository.java`: Add distance calculation logic (Haversine formula).
    - `PostViewModel.java`: Support observing posts within a specific radius.

## Step 20: Push Notifications (FCM)
- **Feature**: Alerts when the app is closed.
- **Action**: Integrate Firebase Cloud Messaging.
- **Key Files**:
    - `NearNeedMessagingService.java`: (New) Extends `FirebaseMessagingService`.
    - `UserRepository.java`: Store and update the user's `fcmToken`.
    - `NotificationCenter.java`: Trigger push alerts in addition to in-app notifications.

## Step 21: Advanced Identity Verification
- **Feature**: Document upload & Trust badges.
- **Action**: Implement ID photo upload to Firebase Storage and admin approval logic.
- **Key Files**:
    - `IdVerificationActivity.java`: Integrate with `StorageRepository`.
    - `PersonProfileActivity.java`: Display "Verified" sapphire badge.

## Step 22: Review & Rating System
- **Feature**: Post-job reputation loop.
- **Action**: Trigger a rating popup upon job completion and aggregate scores.
- **Key Files**:
    - `Review.java`: (New) Data model for reviews.
    - `RatingDialog.java`: (New) Custom UI for rating providers/seekers.
    - `UserRepository.java`: Added methods to update average rating and total review count.

---

## Technical Considerations
- **Battery Optimization**: Use `PRIORITY_BALANCED_POWER_ACCURACY` for location updates to save battery.
- **Data Usage**: Cache geocoded addresses locally to reduce API calls.
- **Security**: Ensure ID documents are stored in a protected Storage bucket accessible only to admins.
