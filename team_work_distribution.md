# NearNeed: Team Task Distribution & Learning Guide

This document divides the NearNeed application into 4 distinct, balanced domains based on the user journey flows. It outlines the specific files each person is responsible for, the pages they must own, and the core technical concepts for the Viva.

---

## 👤 Person 1: Identity & Trust Architect
**Core Flow:** Onboarding -> Authentication -> Profile Construction -> Security.

### 🛣️ Owned Pages & User Flow:
1.  **Entry Flow:** `LoadingActivity` -> `WelcomeActivity` -> `MainActivity` (Entry Router).
2.  **Auth Flow:** `OtpEnterActivity` -> `OtpVerifyActivity` -> `CreateNewPasswordActivity`.
3.  **Onboarding Flow:** `AccountTypeActivity` -> `ProfileSetupActivity` -> `IdVerificationActivity` -> `IdVerifiedActivity` -> `ProfileSuccessActivity`.
4.  **Profile Hub:** `ProfileActivity`, `ProfileFragment`, `PersonProfileActivity`, `ProfileInfoActivity`, `EditProfileActivity`, `EditProfileProviderActivity`.
5.  **Support Flow:** `SettingsActivity`, `HelpSupportActivity`, `TermsConditionsActivity`.

### 🏗️ Underlying Logic & Models:
*   **Models:** `UserProfile`, `UserEntity`, `UserDao`.
*   **Logic:** `RoleManager`, `UserRepository`, `UserPrefs` (LocalStorage), `VerifiedBadgeHelper`.

### 📚 Key Viva Concepts:
*   **Firebase Auth State Persistence:** Explain how `onStart()` checks for an existing user session to skip the login screen.
*   **Local Image Processing:** Explain how Google ML Kit (in `IdVerificationActivity`) detects faces/text locally on the device for security.
*   **Navigation & Flags:** Explain `Intent.FLAG_ACTIVITY_CLEAR_TASK` to prevent users from "backing" into auth screens after logging in.
*   **SharedPreferences:** Explain how `UserPrefs` stores small pieces of data (Role, Name) to avoid frequent database hits.

---

## 🙋‍♂️ Person 2: Seeker & Demand Lead
**Core Flow:** Discovery -> Post Creation -> Candidate Selection -> Response Management.

### 🛣️ Owned Pages & User Flow:
1.  **Seeker Dashboards:** `HomeSeekerActivity`, `HomeSeekerNoPostsActivity`, `SeekerNavbarController`, `HomeFragment`.
2.  **Gig Creation Flow:** `PostOptionsActivity` -> `CreatePostActivity` -> `CreatePostStep2Activity` -> `CategoryPredictor` (AI) -> `PostedSuccessfullyActivity`.
3.  **Community Creation Flow:** `CommunityPostActivity` -> `CommunityPostStep2Activity`.
4.  **Request Management:** `MyPostsActivity`, `RequestDetailActivity`, `GigPostListActivity`.
5.  **Applicant Review:** `ResponsesActivity` -> `VolunteersActivity` -> `VolunteerProfileActivity` -> `CommunityVolunteerDetailActivity`.

### 🏗️ Underlying Logic & Models:
*   **Models:** `Post`, `PostEntity`, `PostDao`, `Response`, `Volunteer`.
*   **Logic:** `PostRepository`, `PostViewModel`, `CategoryPredictor`, `RequestApplyBottomSheet`, `CommunityVolunteerBottomSheet`.

### 📚 Key Viva Concepts:
*   **Dynamic UI Components:** Explain `BottomSheetDialog` implementation for quick applicant reviews.
*   **Recycler Optimization:** Explain how `DashboardGigsAdapter` handles horizontal scrolling and view recycling.
*   **Text Classification:** Explain how the app analyzes job descriptions to suggest categories (Plumbing vs. Gardening) in real-time.
*   **Callback Interfaces:** Explain how `LocationPickerHelper` uses callbacks to pass Lat/Lng back to the posting activities.

---

## 🛠️ Person 3: Provider & Market Intelligence
**Core Flow:** Professional Setup -> Global Discovery -> Scheduling -> Financials.

### 🛣️ Owned Pages & User Flow:
1.  **Provider Onboarding:** `ProfessionalSetupProviderActivity`, `CommunityPreferencesActivity`.
2.  **Provider Dashboard:** `HomeProviderActivity`, `NearbyRequestsAdapter`, `CommunityVolunteeringAdapter`.
3.  **Discovery Flow (Maps):** `MapsActivity`, `MapsFragment`, `LocationPickerHelper`, `GeocodingHelper`.
4.  **Job Detail Flow:** `GigPostDetailActivity`, `CommunityPostDetailActivity`, `ProviderJobDetailActivity`.
5.  **Financials & Schedule:** `MyEarningsActivity`, `PaymentFlowActivity`, `PaymentSuccessActivity`, `CalendarProviderActivity`, `AddScheduleActivity`.

### 🏗️ Underlying Logic & Models:
*   **Models:** `PaymentMethod`, `LocationHelper`, `GeocodingHelper`.
*   **Logic:** `StorageRepository` (Image uploads), `GeocodingHelper` (Photon API), `NearbyRequestsAdapter`, `SearchPredictionAdapter`.

### 📚 Key Viva Concepts:
*   **Map Rendering Architecture:** Explain MapLibre's camera management and how markers are custom-rendered based on job types.
*   **Geocoding vs Reverse Geocoding:** Explain the difference between converting text addresses to Lat/Lng and vice-versa using the Photon API.
*   **Geo-Distance Math:** Explain how the app uses Latitude and Longitude to calculate how far a job is from the Provider's current position.
*   **Multi-Threading:** Explain how background executors prevent the UI from freezing during heavy Map or Geocoding network calls.

---

## 💬 Person 4: Engagement & Real-time Systems
**Core Flow:** Instant Messaging -> Booking Lifecycle -> Status Updates -> Reputation.

### 🛣️ Owned Pages & User Flow:
1.  **Live Interaction:** `MessagesActivity`, `MessagesFragment`, `ChatActivity`, `ChatRepository`.
2.  **Lifecycle Management:** `BookingsActivity`, `BookingsFragment`, `BookingsPagerAdapter`.
3.  **Status Flow:** `SeekerUpcomingFragment`/`ProviderUpcomingFragment` -> `OngoingFragment` -> `UpdateStatusActivity` -> `CompleteBookingDialogFragment`.
4.  **Conflict & Review:** `CancellationDetailsActivity`, `ReviewsActivity`, `RatingDialog`.
5.  **Notification Hub:** `NearNeedMessagingService` (FCM), `NotificationCenter`, `DashboardNotificationPopup`.

### 🏗️ Underlying Logic & Models:
*   **Models:** `Booking`, `BookingEntity`, `BookingDao`, `ChatMessage`, `Review`, `Status`.
*   **Logic:** `BookingRepository`, `BookingViewModel`, `BookingStateManager`, `UpcomingJobManager`, `ChatRepository`, `ChatViewModel`.

### 📚 Key Viva Concepts:
*   **Real-time Snapshot Listeners:** Explain how the app updates chat messages and job statuses instantly without refreshing the page.
*   **State Machine Logic:** Explain how the app enforces the "Active -> In Progress -> Completed" flow and prevents illegal status jumps.
*   **Database Transactions:** Explain how `BookingRepository` prevents "double-booking" or data loss when two users update a job status at the same time.
*   **Push Notification Lifecycle:** Explain how Firebase Cloud Messaging (FCM) tokens are generated and used to target specific users for alerts.
