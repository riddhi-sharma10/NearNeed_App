# Person 3 Viva Preparation Guide (Detailed)

## 1. Person 3 Scope in NearNeed

Person 3 owns the **Provider + Market Intelligence** flow. This means your work is focused on helping providers:

- set up a professional profile,
- discover nearby gigs/community requests,
- use location and maps intelligently,
- review job details,
- manage earnings/payment views,
- manage schedule/calendar.

### Owned pages/components

1. `ProfessionalSetupProviderActivity`
2. `CommunityPreferencesActivity`
3. `HomeProviderActivity`
4. `NearbyRequestsAdapter`
5. `CommunityVolunteeringAdapter`
6. `MapsActivity`
7. `MapsFragment`
8. `LocationPickerHelper`
9. `GeocodingHelper`
10. `SearchPredictionAdapter`
11. `GigPostDetailActivity`
12. `CommunityPostDetailActivity`
13. `ProviderJobDetailActivity`
14. `MyEarningsActivity`
15. `PaymentFlowActivity`
16. `ProcessingPaymentActivity`
17. `PaymentSuccessActivity`
18. `CalendarProviderActivity`
19. `AddScheduleActivity`
20. `PaymentMethod` (model)
21. `LocationHelper` (utility)
22. `StorageRepository` (shared media upload logic used in app architecture)

---

## 2. Why Person 3 Features Were Implemented

### Business reasons

- Providers need a **complete professional identity** (skills, experience, time slots) before getting quality jobs.
- A provider needs **market visibility**: nearby demand (gigs + community requests) in list and map forms.
- Location intelligence improves conversion: users are more likely to accept jobs if they can see proximity and context.
- Payment and earnings pages increase trust by making money flow transparent.
- Calendar/scheduling reduces missed work and supports professional reliability.

### Technical reasons

- Build role-based architecture where one app supports Seeker and Provider personas.
- Keep UI responsive with async geocoding/network/listeners.
- Use Firestore real-time listeners for live post availability.
- Use ViewModel/Repository to separate UI and data logic for maintainability.

---

## 3. End-to-End Person 3 User Journey

1. Provider sets community preferences and professional profile.
2. Provider lands on Home Provider dashboard.
3. Dashboard shows nearby gigs/community cards via RecyclerView adapters.
4. Provider can open full map view for discovery and location-based exploration.
5. Provider checks detailed gig/community screens and applies/volunteers.
6. Provider views earnings summary and transaction-like activity.
7. Payment screens show checkout + success simulation flow.
8. Provider manages upcoming tasks via calendar and add-schedule screens.

---

## 4. Page-by-Page Detailed Explanation

## 4.1 CommunityPreferencesActivity

### What this page does

- Collects what help/skills the user can offer to the community.
- Lets user toggle notification preferences (in-app help notifications and SMS).
- Supports adding custom skill chips dynamically.
- Routes providers to professional setup next.

### Why implemented

- Community module needs structured volunteer capability data.
- Dynamic custom skill input avoids limiting users to fixed options.

### Frontend implementation

- Uses `ChipGroup` for selectable skills.
- `MaterialSwitch` toggles for notification preferences.
- `TextInputLayout`/`TextInputEditText` for "Other" skill input.
- Hero/section card design with Material Cards.

### Backend/data implementation

- Saves selected data to `SharedPreferences` (`NearNeedPrefs`) as:
  - `user_offers_csv`
  - `pref_help_notifications`
  - `pref_sms_notifications`
- Merges old and new skills via `LinkedHashSet` to avoid duplicates.

### Key logic highlights

- Validation requires at least one skill.
- If `+ Other` is checked, non-empty custom input is enforced.
- Dynamically creates new chips at runtime and inserts before `+ Other`.

---

## 4.2 ProfessionalSetupProviderActivity

### What this page does

- Captures professional setup details:
  - service categories,
  - experience level,
  - available days,
  - preferred time slots,
  - start/end times,
  - terms acceptance.
- Shows success overlay after valid submission.

### Why implemented

- Provider matching quality depends on capability metadata.
- Availability/time constraints are needed for realistic booking and scheduling.

### Frontend implementation

- Chip-like toggle UI built with `TextView` + background drawables.
- `TimePickerDialog` for start/end time selection.
- Section-level inline validation errors (`tvCategoryError`, etc.).
- Full-screen success overlay with action buttons.

### Backend/data implementation

- Persists selected IDs to `SharedPreferences` (`ProviderProfile`):
  - categories (`StringSet` of selected view IDs),
  - experience (`int` view ID),
  - days (`StringSet`),
  - timeSlots (`StringSet`).

### Key logic highlights

- Multi-select toggles for categories/days/slots.
- Single-select for experience.
- If terms checkbox not checked, submission blocked.
- Navigates to `MainActivity` with task-clearing flags on success.

---

## 4.3 HomeProviderActivity

### What this page does

- Acts as provider dashboard.
- Shows greeting, notifications badge, search, nearby requests, community volunteering cards.
- Allows role toggle between Seeker and Provider.
- Launches Maps and location picker.

### Why implemented

- Providers need a central operational hub with immediate demand visibility.
- Search/filter improves discoverability when demand volume grows.

### Frontend implementation

- Dashboard sections in scroll layout:
  - greeting + location section,
  - search bar,
  - earnings/stats cards,
  - schedule preview,
  - two horizontal RecyclerViews.
- Notification icon with unread badge.

### Backend/data implementation

- Uses `PostViewModel` and `UserViewModel`.
- Observes real-time posts using `observeAllActivePosts()`.
- Splits post stream into `GIG` and `COMMUNITY` lists and feeds corresponding adapters.
- Real-time notification badge via `NotificationCenter.listenUnreadCount(...)`.
- Caches greeting name through `UserPrefs`.

### Key logic highlights

- Role switch writes role through `RoleManager` and restarts task stack.
- Search binding uses `DashboardSearchHelper.bindProviderSearch(...)`.
- Location picker callback updates saved location in user profile layer.

---

## 4.4 NearbyRequestsAdapter

### What this component does

- Binds nearby gig posts into card list (`item_request_card`).
- Supports in-memory search filter.

### Why implemented

- Needed for efficient rendering of scrollable nearby gig cards.
- Keeps dashboard list concerns separate from activity.

### Frontend implementation

- RecyclerView Adapter + ViewHolder pattern.
- Card shows title, distance label, short description, and "View" button.

### Backend/data implementation

- Adapter itself is UI-only; receives `List<Post>` from ViewModel observer.
- On button click sends intent extras to `RequestDetailActivity`.

### Key logic highlights

- Maintains `allPosts` and `posts` for reversible filtering.

---

## 4.5 CommunityVolunteeringAdapter

### What this component does

- Displays community posts in horizontal cards.
- Changes CTA label by role:
  - Provider: "Volunteer"
  - Seeker: "View Responses"

### Why implemented

- Supports dual persona behavior with same UI card surface.

### Frontend implementation

- RecyclerView card width customized (~300dp) for horizontal carousel feel.
- Card fields: title, postedBy, description, action button.

### Backend/data implementation

- Sends relevant post data through intent extras to `CommunityPostDetailActivity`.

### Key logic highlights

- Filter checks title/postedBy/description/location.

---

## 4.6 MapsActivity + MapsFragment

### What this page does

- `MapsActivity` is host container.
- `MapsFragment` provides real map functionality for both roles with role-dependent layout.
- For providers: discovery map + marker info card + search predictions + recenter.

### Why implemented

- Location is core to hyperlocal service matching.
- Map gives high-context decision support beyond list cards.

### Frontend implementation

- Uses MapLibre `MapView` (Open map stack).
- Provider map UI:
  - glass search bar,
  - optional bottom sheet,
  - floating job detail card,
  - map markers with custom drawn bitmaps.
- Marker icon rendering done via `Bitmap` + `Canvas` + `Paint`.

### Backend/data implementation

- Real-time posts via `PostViewModel.getNearbyPosts()` and `observeAllActivePosts()`.
- Seeker mode additionally observes providers from `UserProfileRepository.observeAllProviders(...)`.
- Map marker metadata stored in local maps (`markerDataMap`, `jobToMarkerMap`).
- Permission flow via `ActivityResultLauncher` for `ACCESS_FINE_LOCATION`.

### Key logic highlights

- Initializes MapLibre before map view creation.
- Select/deselect marker icon state change.
- Dynamic info card content from marker metadata.
- Geocoder-based text search in provider map (`Geocoder.getFromLocationName`).
- Lifecycle-safe map forwarding (`onStart/onResume/.../onDestroyView`).

---

## 4.7 LocationPickerHelper

### What this component does

- Opens reusable bottom sheet for selecting location:
  - saved rows (home/work/recent),
  - current GPS location,
  - search predictions.

### Why implemented

- Common location UX reused across screens reduces duplication.

### Frontend implementation

- `BottomSheetDialog` with custom layout.
- Search input + RecyclerView predictions.
- Loading indicator during current-location fetch.

### Backend/data/network implementation

- Uses `FusedLocationProviderClient.getCurrentLocation(...)`.
- Reverse geocodes current lat/lng with Android `Geocoder`.
- Search delegates to `GeocodingHelper.performSearch(...)`.
- Callback interface returns selected text + lat/lng to caller.

---

## 4.8 GeocodingHelper + SearchPredictionAdapter

### What these components do

- Convert user text query into coordinate suggestions.
- Display deduplicated suggestions list.

### Why implemented

- Better search quality by querying **two sources** and merging results.

### Backend/network implementation

- Uses `OkHttp` async calls to:
  - Photon API (`photon.komoot.io`),
  - Nominatim (`openstreetmap.org/search`).
- Uses `CountDownLatch` to await both responses with timeout.
- Deduplicates close results (<500m) using `Location.distanceBetween`.
- Posts results back to main thread via `Handler(Looper.getMainLooper())`.

### Frontend implementation

- `SearchPredictionAdapter` binds primary/secondary text rows.
- Click callback returns selected lat/lng and display name.

---

## 4.9 GigPostDetailActivity

### What this page does

- Displays full gig details (title, category, budget, distance, duration, location, description).
- Opens applicants list via `ResponsesActivity`.

### Why implemented

- Card previews are not enough for provider decision; full detail screen is required.

### Frontend implementation

- Toolbar + nested scroll + fixed bottom CTA button.
- Budget emphasized with highlighted card.

### Backend/data implementation

- Currently populated from intent extras passed by previous screens.

---

## 4.10 CommunityPostDetailActivity

### What this page does

- Displays community request details.
- Role-based CTA:
  - provider => opens volunteer bottom sheet,
  - seeker => opens volunteers list with slot limit.

### Why implemented

- Same content must serve two interaction roles.

### Frontend implementation

- Community badge, poster info, date/time row, location block, slots card, description.

### Backend/data implementation

- Reads intent extras and applies verification badge through `VerifiedBadgeHelper`.
- Parses slot count using regex to derive integer limits.

---

## 4.11 ProviderJobDetailActivity

### What this page does

- Shows job details from provider perspective and lets provider apply.

### Why implemented

- Dedicated provider-centric detail page simplifies apply action flow.

### Frontend implementation

- Similar structure to gig detail with fixed "Apply for this Gig" CTA.

### Backend/data implementation

- Uses `RequestApplyBottomSheet` to submit/prepare application flow.

---

## 4.12 MyEarningsActivity

### What this page does

- Shows provider total earned amount and activity list.
- Computes totals based on completed applications only.

### Why implemented

- Financial transparency increases trust and retention.

### Frontend implementation

- Gradient background, summary card, transaction RecyclerView.
- Status-based color coding (`COMPLETED`, `PENDING`, `FAILED`).

### Backend/data implementation

- Observes `ApplicationViewModel.getUserApplications()`.
- Aggregation logic:
  - iterate user applications,
  - include amount in total only when status is `COMPLETED`.

### Key logic highlights

- `formatDate` converts timestamp to readable label.
- Transaction item icon varies by post type.

---

## 4.13 PaymentFlowActivity -> ProcessingPaymentActivity -> PaymentSuccessActivity

### What these pages do

- `PaymentFlowActivity`: order summary + platform fee + phone validation.
- `ProcessingPaymentActivity`: animated intermediate processing state.
- `PaymentSuccessActivity`: final success/receipt-style summary.

### Why implemented

- Multi-step payment UX gives confidence and avoids abrupt transitions.

### Frontend implementation

- Stepper progress UI in payment flow.
- Secure payment info card and phone input.
- Success screen with transaction details and action buttons.

### Backend/payment implementation

- Razorpay SDK imported and preloaded (`Checkout.preload`).
- In current code, `startPayment()` **simulates success** after phone validation (demo/test behavior).
- Amount calculation includes 5% platform fee.
- Success screen generates random transaction ID using UUID.

### Important viva note

- Payment callback methods exist (`onPaymentSuccess`, `onPaymentError`), but current implementation routes both to success for demo convenience.

---

## 4.14 CalendarProviderActivity

### What this page does

- Provides provider scheduling dashboard:
  - month switch,
  - day selection,
  - add schedule navigation.

### Why implemented

- Provider reliability depends on visible and manageable schedule.

### Frontend implementation

- Custom week selector with selected/unselected style transforms.
- Timeline/task cards for daily agenda visualization.
- Top add icon and in-timeline add button both open add-schedule screen.

### Backend/data implementation

- Current state is local UI state (month index + selected day).
- No persistent calendar write yet in this activity.

---

## 4.15 AddScheduleActivity

### What this page does

- Creates a new schedule item with date/time and task metadata fields.

### Why implemented

- Quick task insertion for provider planning.

### Frontend implementation

- Client relation card + task detail inputs.
- Date picker and time picker fields.
- Chip toggles between premium/community task types.

### Backend/data implementation

- Current implementation provides local feedback (`Toast`) and returns.
- Persistent backend write is not yet implemented here.

---

## 5. Shared Backend/Data Architecture Used by Person 3 Pages

## 5.1 PostViewModel + PostRepository

- `PostViewModel` exposes lifecycle-safe `LiveData<List<Post>>`.
- `PostRepository` handles Firestore + Room offline cache.
- Real-time listeners:
  - all active posts,
  - nearby posts (client-side distance filter),
  - user posts.
- Spatial filtering uses Haversine in repository (`calculateDistance`).

## 5.2 ApplicationViewModel + ApplicationRepository

- Used by earnings and application flows.
- Supports observe/submit/update/accept/reject/complete actions.
- Firestore collection: `applications`.
- Sends notifications on key status transitions.

## 5.3 UserProfileRepository + RoleManager + UserPrefs

- Role-based behavior across provider pages comes from `RoleManager` + preferences.
- Profile cache path includes SharedPreferences and Room for instant UI.

## 5.4 StorageRepository

- Shared media upload repository to Firebase Storage.
- Includes placeholder moderation stage before upload.
- Demonstrates repository layering and async upload callbacks.

---

## 6. Frontend Technologies Used (Person 3)

- XML layout system with Material Components.
- RecyclerView + custom adapters/viewholders.
- NestedScrollView and card-based dashboard composition.
- BottomSheetDialog for reusable location picker.
- Dynamic drawables/tints/stateful chip backgrounds.
- AppBar + toolbar navigation patterns.

---

## 7. Backend/Cloud/Infra Touchpoints (Person 3)

- Firebase Firestore:
  - `posts` live stream,
  - `applications` for earnings and status.
- Firebase Storage via `StorageRepository`.
- Firebase notification calls through `NotificationCenter` hooks.
- Map stack:
  - MapLibre rendering,
  - external geocoding providers (Photon + Nominatim),
  - Android Geocoder for local search/reverse geocode.
- Location stack:
  - FusedLocationProviderClient.

---

## 8. Concepts and Meanings (Important Viva Glossary)

## 8.1 RecyclerView

A high-performance Android list component that reuses item views instead of creating new views for every row, improving speed and memory efficiency.

## 8.2 Adapter

A bridge between data and RecyclerView UI. It tells RecyclerView how to create and bind item views.

## 8.3 ViewHolder

Object that caches references to item subviews (TextView/ImageView/Button) to avoid repeated `findViewById` calls.

## 8.4 ViewModel

Lifecycle-aware class that stores and manages UI data. Survives configuration changes (like rotation).

## 8.5 LiveData

Observable data holder. UI observes it and gets automatic updates when data changes.

## 8.6 Repository Pattern

Architectural pattern that centralizes data operations (Firestore/Room/network) away from UI classes.

## 8.7 Firestore Snapshot Listener

Real-time listener that pushes updates whenever documents change in Firestore.

## 8.8 Room Database

SQLite abstraction layer for local/offline caching with DAOs and entities.

## 8.9 SharedPreferences

Lightweight key-value local storage for small user settings/state.

## 8.10 Intent Extras

Bundle values passed from one Activity to another.

## 8.11 Activity Lifecycle

States of an Android screen (`onCreate`, `onStart`, `onResume`, `onPause`, `onStop`, `onDestroy`) used for proper resource management.

## 8.12 BottomSheetDialog

Modal sheet from the bottom, used for compact contextual interactions (like location picking).

## 8.13 MapLibre

Open-source map rendering SDK (alternative to Google Maps) used for vector maps, camera, markers, interactions.

## 8.14 Geocoding

Converting text address/place names to coordinates (lat/lng).

## 8.15 Reverse Geocoding

Converting coordinates (lat/lng) back into readable address text.

## 8.16 Haversine Formula

Mathematical formula to compute great-circle distance between two points on Earth from lat/lng.

## 8.17 Callback Interface

Custom interface used to return async results (e.g., selected location) from helper to caller.

## 8.18 FusedLocationProviderClient

Google Play Services API to fetch device location efficiently using multiple sensors/providers.

## 8.19 CountDownLatch

Concurrency utility that waits until multiple async tasks complete before proceeding.

## 8.20 Handler + Looper

Android mechanism for posting work/results to specific threads, especially main UI thread.

## 8.21 Debouncing

Delaying search execution until user stops typing briefly to reduce excessive network/API calls.

## 8.22 Role-Based UI

Changing screen behavior and CTA text based on current user role (SEEKER vs PROVIDER).

## 8.23 Material Design Components

Google UI components (MaterialButton, MaterialCardView, Chip, TextInputLayout) for consistent modern UI.

## 8.24 Razorpay SDK

Payment gateway SDK for handling checkout and payment callbacks in Android apps.

## 8.25 Notification Badge Sync

Real-time update of unread count and visibility on dashboard icon.

---

## 9. What Exactly Person 3 Has Done (Concise Viva Summary)

- Built provider onboarding forms and preference capture.
- Implemented provider dashboard with searchable, role-aware cards.
- Implemented map discovery stack with MapLibre and custom markers.
- Integrated location picker with GPS + geocoding predictions.
- Built provider detail pages for gig/community jobs.
- Implemented earnings aggregation from application statuses.
- Implemented payment UI flow with processing and success states.
- Built scheduling/calendar UI and add-schedule interaction.
- Connected provider pages with shared architecture (ViewModel, Repository, Firestore listeners, local caching patterns).

---

## 10. Viva-Ready Justification Lines (Use these in answers)

- "We implemented role-based screen behavior so one codebase supports both demand-side and supply-side journeys with minimal duplication."
- "We used ViewModel + LiveData to keep UI reactive and lifecycle-safe while reading real-time Firestore data."
- "We used RecyclerView adapters to keep list rendering memory-efficient for growing nearby post data."
- "Map discovery was implemented with MapLibre plus custom marker rendering so providers can quickly interpret category and budget context visually."
- "Geocoding and reverse geocoding are separated by purpose: search text to coordinates, and GPS coordinates back to readable addresses."
- "Distance filtering uses Haversine math client-side because native Firestore geo-query support is limited in this implementation."

---

## 11. Current Limitations You Can Honestly Mention in Viva

- Payment flow currently simulates success in demo mode instead of full Razorpay transaction lifecycle.
- Some schedule features are UI-first and not yet persisted to backend.
- Some map detail actions show placeholders/toasts instead of full booking execution from map card.
- Nearby filtering in repository is client-side and can be optimized later with dedicated geo-indexing.

These are acceptable for an academic project if you explain roadmap clearly.

---

## 12. Quick 60-Second Pitch for Examiner

"My module is Provider & Market Intelligence. I designed the provider journey from profile setup to earning and scheduling. On the frontend, I implemented modern Material dashboards, RecyclerView card systems, map overlays, and payment/success interfaces. On the backend side, I connected these pages with ViewModel-Repository architecture, Firestore real-time listeners, and local caching patterns. I also implemented geospatial intelligence: geocoding, reverse geocoding, and distance filtering to show relevant nearby jobs. Overall, my work turns raw requests into actionable, location-aware opportunities for providers while maintaining usability, responsiveness, and role-based behavior."