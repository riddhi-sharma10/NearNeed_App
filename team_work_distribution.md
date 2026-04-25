# NearNeed: Team Task Distribution & Learning Guide

This document divides the NearNeed application into 4 distinct, balanced domains. It outlines the specific files each person is responsible for, the core features they need to build/maintain, and **the specific technical concepts they must learn and understand** for the Viva.

---

## 👤 Person 1: Auth & Identity Architect
**Core Focus:** User onboarding, security, AI identity verification, and role management.
You are responsible for getting users into the app securely and managing their profiles and dual-role states.

**📚 Key Concepts You Must Learn (Viva Prep):**
*   **Firebase Authentication:** Understand token lifecycles, session persistence, and secure credential storage.
*   **On-Device Machine Learning:** Learn how Google ML Kit processes images (Bitmaps) locally without sending sensitive PII data to a server.
*   **Android SharedPreferences:** Understand how to save local states (like "Is the user currently a Seeker?") so the app remembers them on restart.
*   **Android Intent Flags:** Learn how to manipulate the "Back Stack" (e.g., `FLAG_ACTIVITY_CLEAR_TASK`) so users can't press the "Back" button to bypass the login screen.

**Associated Files & Pages:**
*   **Onboarding:** `LoadingActivity`, `WelcomeActivity`, `MainActivity`
*   **Auth:** `OtpEnterActivity`, `OtpVerifyActivity`, `CreateNewPasswordActivity`
*   **Profiles:** `ProfileSetupActivity`, `ProfileInfoActivity`, `AccountTypeActivity`
*   **Verification:** `IdVerificationActivity`, `IdVerifiedActivity`
*   **Settings/UI:** `ProfileActivity`, `PersonProfileActivity`, `EditProfileActivity`, `EditProfileProviderActivity`
*   **Core Logic:** `RoleManager`, `UserRepository`, `UserPrefs`

---

## 🙋‍♂️ Person 2: Demand Side (Seeker) Lead
**Core Focus:** The process of asking for help, managing active requests, and selecting candidates.
You are responsible for the entire Seeker ecosystem and the AI Category prediction engine.

**📚 Key Concepts You Must Learn (Viva Prep):**
*   **RecyclerViews & DiffUtil:** Understand how Android recycles list items for 60fps scrolling, and how DiffUtil calculates precise list changes instead of reloading the whole screen.
*   **NoSQL Schema Design:** Learn the difference between SQL tables and Firestore Documents. Understand how to flatten data for fast UI reads.
*   **Natural Language Processing (NLP) Basics:** Understand how text classification works (analyzing keywords in a Gig description to auto-suggest categories).
*   **Advanced UI Components:** Learn to implement interactive `BottomSheetDialogs`, dynamic `ChipGroups`, and `TextWatchers` (for live word counting).

**Associated Files & Pages:**
*   **Dashboards:** `HomeSeekerActivity`, `HomeSeekerNoPostsActivity`, `SeekerNavbarController`
*   **Post Creation (Paid):** `CreatePostActivity`, `CreatePostStep2Activity`, `CategoryPredictor`
*   **Post Creation (Free):** `CommunityPostActivity`, `CommunityPostStep2Activity`
*   **Applicant Review:** `ResponsesActivity`, `VolunteersActivity`
*   **UI Components:** `RequestApplyBottomSheet`, `CommunityVolunteerBottomSheet`
*   **Core Logic:** `PostRepository`, `MyPostsActivity`

---

## 🛠️ Person 3: Supply Side (Provider) & Geo-Intelligence Lead
**Core Focus:** Job discovery, Map integration, GPS math, and the Provider's financial wallet.
You are responsible for making sure Providers can find work near them and track their earnings.

**📚 Key Concepts You Must Learn (Viva Prep):**
*   **The Haversine Formula:** You must mathematically understand how to calculate "great-circle" distances on a spherical Earth using Latitude and Longitude.
*   **Geocoding vs. Reverse Geocoding:** Learn how to convert a typed address ("123 Main St") into coordinates (Geocoding), and coordinates into a readable address (Reverse Geocoding).
*   **Map Rendering (MapLibre):** Understand vector maps, camera bounding boxes, and how to plot custom icon markers on a map canvas.
*   **Android Concurrency:** Learn how to execute heavy network tasks (like calling the Photon Geocoding API) on background threads using `ExecutorService` so the UI doesn't freeze (ANR).

**Associated Files & Pages:**
*   **Dashboards:** `HomeProviderActivity`, `NearbyRequestsAdapter`
*   **Geo/Maps:** `MapsActivity`, `MapsFragment`, `LocationHelper`, `GeocodingHelper`, `LocationPickerHelper`
*   **Job Viewing:** `GigPostDetailActivity`, `CommunityPostDetailActivity`, `ProviderJobDetailActivity`
*   **Financials:** `MyEarningsActivity`, `PaymentFlowActivity`, `PaymentSuccessActivity`
*   **Scheduling:** `CalendarProviderActivity`, `AddScheduleActivity`
*   **Core Logic:** `PostRepository` (Radius filtering logic)

---

## 💬 Person 4: Real-time Systems & Lifecycle Manager
**Core Focus:** Chat, Push Notifications, the Booking State Machine, and the Reputation system.
You are responsible for the "Live" aspects of the app after a job has been accepted.

**📚 Key Concepts You Must Learn (Viva Prep):**
*   **WebSockets vs REST APIs:** Understand why polling a server every 5 seconds for messages is terrible, and how Firestore's persistent WebSocket (Snapshot Listeners) solves this.
*   **Firebase Cloud Messaging (FCM):** Learn the lifecycle of a Push Notification token, and how background Services wake up the app when the user receives a message.
*   **Database ACID & Atomic Transactions:** You must be able to explain the "Race Condition." Learn how Firebase Transactions lock documents so that two people rating a provider at the same exact time don't overwrite each other's data.
*   **State Machine Architecture:** Understand how to build logic that strictly enforces transitions (e.g., A job cannot go from "Pending" directly to "Completed" without passing through "In Progress").

**Associated Files & Pages:**
*   **Chat Engine:** `MessagesActivity`, `MessagesFragment`, `ChatActivity`, `ChatRepository`
*   **Notifications:** `NearNeedMessagingService` (FCM), `NotificationCenter`, `DashboardNotificationPopup`
*   **Job Tracking:** `BookingsActivity`, `BookingsFragment`, `BookingsPagerAdapter`
*   **State Machine UIs:** `SeekerOngoingFragment`, `ProviderOngoingFragment`, `SeekerPastFragment`, `ProviderPastFragment`
*   **Status Management:** `UpdateStatusActivity`, `CancellationDetailsActivity`
*   **Trust System:** `ReviewsActivity`, `RatingDialog`, `Review` (Model)
*   **Core Logic:** `BookingRepository` (Transaction logic)
