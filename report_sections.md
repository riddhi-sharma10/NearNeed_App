# NearNeed – Project Report Sections

---

## 0. Other Settings Made for the Project

### 0.1 Android Project Configuration (build.gradle.kts)

The application module is configured with the following SDK targets:

| Setting | Value |
|---|---|
| Namespace | com.example.nearneed |
| Compile SDK | 35 (Android 15) |
| Minimum SDK | 28 (Android 9 Pie) |
| Target SDK | 35 |
| Version Code | 1 |
| Version Name | 1.0 |
| Java Compatibility | Java 17 (source and target) |

**Build features enabled:**

- `viewBinding = true`: Allows direct type-safe access to all XML views without `findViewById()` calls. Every Activity uses a generated binding class (e.g., `ActivityHomeSeekerBinding`) instead of manually finding views.
- `buildConfig = true`: Enables the `BuildConfig` class, which is used to expose API keys injected at compile time.

**API Key Injection:**

Sensitive keys are stored in `local.properties` (which is not committed to version control) and injected into the app at build time as `BuildConfig` fields:

```
MAPTILER_API_KEY  →  BuildConfig.MAPTILER_API_KEY  (used in MapsActivity)
GEMINI_API_KEY    →  BuildConfig.GEMINI_API_KEY    (used in AiChatActivity)
```

This means no API key is hardcoded in Java source files or committed to the repository.

**Plugins applied:**
- `com.android.application` (AGP 9.2.0)
- `com.google.gms.google-services` (4.4.2) — processes `google-services.json` to configure Firebase automatically

---

### 0.2 Dependencies

All Firebase libraries are managed under the Firebase Bill of Materials (BOM 33.8.0), which ensures all Firebase SDKs use compatible versions without manually specifying each one.

**Firebase:**

| Library | Purpose |
|---|---|
| firebase-auth | Phone/email authentication |
| firebase-firestore | Real-time cloud database |
| firebase-storage | Image upload and retrieval |
| firebase-messaging | Push notifications (FCM) |

**Architecture & Lifecycle:**

| Library | Version | Purpose |
|---|---|---|
| androidx.lifecycle:lifecycle-viewmodel | 2.8.7 | ViewModel for MVVM |
| androidx.lifecycle:lifecycle-livedata | 2.8.7 | LiveData observers |
| androidx.room:room-runtime | 2.6.1 | Local SQLite offline cache |
| androidx.room:room-compiler | 2.6.1 | Annotation processor for Room DAOs |

**Maps:**

| Library | Version | Purpose |
|---|---|---|
| org.maplibre.gl:android-sdk | 11.0.0 | Open-source map rendering |
| org.maplibre.gl:android-plugin-annotation-v9 | 3.0.0 | Map markers and annotations |
| com.google.android.gms:play-services-location | 21.3.0 | FusedLocationProviderClient for GPS |

**Networking and Utilities:**

| Library | Version | Purpose |
|---|---|---|
| com.squareup.okhttp3:okhttp | 4.12.0 | HTTP client for Gemini AI API calls |
| com.google.code.gson:gson | 2.10.1 | JSON serialization/deserialization |
| com.github.bumptech.glide:glide | 4.16.0 | Loading images from Firebase Storage URLs into ImageViews |

**ML Kit:**

| Library | Purpose |
|---|---|
| com.google.mlkit:text-recognition | OCR used in ID verification flow |

**UI:**

| Library | Version |
|---|---|
| com.google.android.material | 1.10.0 |
| androidx.appcompat | 1.6.1 |
| androidx.constraintlayout | 2.1.4 |
| androidx.activity | 1.8.0 |

---

### 0.3 AndroidManifest.xml Settings

**Permissions declared:**

| Permission | Reason |
|---|---|
| INTERNET | All Firebase and API calls |
| ACCESS_FINE_LOCATION | GPS coordinates for post location and nearby filtering |
| ACCESS_COARSE_LOCATION | Fallback location when GPS is unavailable |
| READ_MEDIA_IMAGES | Photo selection from gallery (Android 13+) |
| READ_EXTERNAL_STORAGE | Photo selection (Android 12 and below) |
| WRITE_EXTERNAL_STORAGE | Temporary file handling |
| CAMERA | Optional camera capture for post photos and ID scan |
| RECORD_AUDIO | Voice input in chat and AI assistant |

Camera is declared with `android:required="false"`, meaning the app can be installed on devices without a camera.

**Application-level settings:**

- `android:name=".NearNeedApp"`: Points to the custom `Application` class that initialises Firebase and MapLibre at startup.
- `android:allowBackup="true"`: Enables Android's backup mechanism for app data.
- `android:supportsRtl="true"`: Layout mirroring support for right-to-left languages.
- `android:theme="@style/Theme.NearNeed"`: Global app theme applied to all activities.

**Activity-specific settings:**

- `LoadingActivity` is the launcher activity (the entry point of the app), not `MainActivity`.
- `ChatActivity` and `AiChatActivity` both have `android:windowSoftInputMode="adjustResize"`, which pushes the message input field up when the keyboard appears instead of hiding it behind the keyboard.
- `IdVerifiedActivity` and `ProfileSuccessActivity` use `@style/Theme.NearNeed.Transparent`, giving them a transparent background so the previous screen shows through during the animation.
- All other activities have `android:exported="false"`, meaning they cannot be launched by other apps.

**Google Maps API key:**
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_API_KEY_HERE" />
```

**FCM Service:**
```xml
<service android:name=".NearNeedMessagingService" android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```
`NearNeedMessagingService` handles incoming push notification payloads from Firebase Cloud Messaging.

---

### 0.4 Application Class (NearNeedApp.java)

A custom `Application` class is used to run global initialisation once when the app process starts, before any Activity is created.

```java
public class NearNeedApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MapLibre.getInstance(this);       // MapLibre must be initialised before any MapView
        FirebaseApp.initializeApp(this);  // Firebase SDK initialisation
    }
}
```

MapLibre requires application-level initialisation because its native libraries are loaded once per process. If initialised inside an Activity, it would fail or cause crashes when the Activity is recreated on rotation. Firebase similarly requires a one-time setup to read `google-services.json` and configure the SDK. Wrapping Firebase initialisation in a try/catch allows the app to start gracefully even if the configuration file is missing during development.

---

### 0.5 Firebase Project Setup (google-services.json)

The `google-services.json` file is placed in the `app/` directory. It contains the Firebase project ID, API keys, and app identifiers. The `google-services` Gradle plugin reads this file at build time and generates the necessary resource files so Firebase SDKs can find the correct project without any manual configuration in code.

The following Firebase services were enabled in the Firebase Console for this project:
- Authentication (Phone number sign-in)
- Cloud Firestore (with security rules)
- Firebase Storage (with read/write rules)
- Firebase Cloud Messaging

---

### 0.6 Repository Setup (settings.gradle.kts)

```
Repositories: google(), mavenCentral(), gradlePluginPortal()
Project name: NearNeed
Module: :app (single-module project)
```

The version catalog (`gradle/libs.versions.toml`) centralises all dependency versions so they are defined once and referenced by alias across build files, avoiding version conflicts.

---

### 0.7 Resource Files

Android resource files live under `app/src/main/res/` and are separated into folders by type. Each category serves a specific purpose in the app.

---

#### colors.xml

Defines the complete colour palette for the app under a "Sapphire Glass" design theme. All colours are named and grouped into logical categories rather than used as raw hex codes in layout files.

| Group | Purpose | Example |
|---|---|---|
| Sapphire Core | Primary brand colours | `sapphire_primary` = `#1E3A8A` (deep blue) |
| Brand Aliases | Semantic names for primary/secondary/error | `brand_primary`, `brand_error`, `brand_success` |
| Typography | Text hierarchy colours | `text_header`, `text_body_dark`, `text_muted` |
| Surfaces | Background and card colours | `surface_background`, `surface_card` |
| Glassmorphism | Semi-transparent overlays | `glass_white_80`, `glass_border` |
| Feature-specific | Status badge and role colours | `urgent_red`, `community_green`, `provider_status_available` |

Using a central `colors.xml` means any design change (such as updating the brand blue) requires editing one line rather than searching every layout file.

---

#### themes.xml

Defines all app-level style themes and component style overrides.

| Style | Purpose |
|---|---|
| `Base.Theme.NearNeed` | Parent theme inheriting `Theme.Material3.DayNight.NoActionBar`. Sets `colorPrimary`, `colorAccent`, and `colorSecondary` to `brand_primary`. |
| `Theme.NearNeed` | Applied to all Activities via `android:theme` in the manifest. |
| `Theme.NearNeed.Transparent` | Applied to `IdVerifiedActivity` and `ProfileSuccessActivity`. Makes the window background fully transparent with no dim, so the previous screen shows through during the success animation. |
| `BottomSheetDialogTheme` | Overrides the default BottomSheet background to transparent, allowing custom rounded drawable backgrounds to render correctly without a white rectangle behind them. |
| `CategoryChipStyle` | Reusable style for community category chip TextViews (40dp height, 13sp text, `sans-serif-medium`). |
| `AppPreferenceChip` | Style for Material Chip components used in profile setup preference selection (48dp min height, 24dp corner radius). |

---

#### typography.xml

Centralises all text appearance styles so font families are not hardcoded in individual layout files.

| Font constant | Mapped to | Used for |
|---|---|---|
| `font_family_heading` | `sans-serif-medium` | Titles, section headers, bold labels |
| `font_family_hero` | `sans-serif-black` | Large display text on landing screens |
| `font_family_forms` | `sans-serif` | Input labels, form fields, body text |
| `font_family_description` | `sans-serif-light` | Subtitles, helper text, descriptions |

Named styles such as `AppHeading`, `AppHeading.Large`, `AppFormText`, and `TextAppearance.NearNeed.SectionLabel` are applied via `style=` attributes in layouts. Changing a font globally requires editing only this file.

---

#### dimens.xml

Stores global spacing and dimension constants to keep UI measurements consistent.

| Dimension | Value | Used for |
|---|---|---|
| `stroke_width_standard` | 1dp | Default card and chip border width |
| `stroke_width_thick` | 2dp | Selected card or chip border width |
| `chip_min_height` | 48dp | Minimum touch target for chip components |
| `chip_stroke_width` | 1dp | Chip border in unselected state |

---

#### strings.xml

All user-facing text in the app is defined here rather than hardcoded in layout XML or Java files. This is required for Android internationalisation support and also keeps copy changes in one place. The file contains strings for:

- Authentication screens (OTP, phone number entry, verification prompts)
- Profile setup flow (4-step wizard labels and button text)
- ID verification screen (accepted ID types, instructions)
- Seeker and provider home screen labels and empty state messages
- Booking status labels (ongoing, past, upcoming empty state text)
- Payment screen text (transaction ID, recipient, receipt labels)
- Navigation tab labels (Home, Map, Messages, Community)
- Community preferences and emergency notification settings

---

#### drawable/ (XML Drawables)

The drawable folder contains over 100 custom XML drawable files. These are used instead of image assets for UI backgrounds, borders, and shapes because XML drawables scale perfectly to any screen density without quality loss.

They are named with a consistent prefix convention:

| Prefix | Purpose | Examples |
|---|---|---|
| `bg_` | Background shape drawables | `bg_chat_incoming`, `bg_bottom_sheet_rounded`, `bg_circle_blue_solid` |
| `sel_` | State-list drawables (pressed/selected/default) | `sel_community_chip`, `sel_chip_bg_primary` |
| `ic_` | Vector icon drawables | Navigation and action icons |

Key drawables used in the seeker flow:
- `bg_bottom_sheet_rounded`: Rounded top corners for all BottomSheetDialog backgrounds
- `bg_community_badge`, `bg_urgent_badge`, `bg_paid_badge`: Coloured pill shapes for post status labels in `MyPostsActivity`
- `bg_dashed_border`: Used around the photo upload area in `CreatePostActivity`
- `bg_glass_card`, `bg_glass_main`: Semi-transparent frosted glass card backgrounds used on dashboard screens
- `bg_chat_incoming`, `bg_chat_outgoing`: Bubble shapes for chat messages with different corner radii depending on sender

---

#### color/ (Color State Lists)

The `res/color/` folder contains XML color state list files used by interactive components to change colour based on state (selected, pressed, checked, unchecked) without writing Java code.

| File | Used by |
|---|---|
| `nav_item_color.xml` | Bottom navigation tab icon and text (active/inactive states) |
| `sel_chip_bg_primary.xml` | Chip background for filter chips (selected = brand blue, unselected = transparent) |
| `sel_chip_stroke_primary.xml` | Chip border colour state list |
| `sel_chip_text_primary.xml` | Chip text colour state list |
| `sel_gender_chip_bg.xml` | Gender selection chip background in profile setup |
| `sel_switch_thumb_primary.xml` | Material Switch thumb colour (on/off states) |

---

#### anim/

Contains `scale_in_tick.xml`, a scale animation used on the success tick icon in `IdVerifiedActivity` and `ProfileSuccessActivity`. The tick scales from 0 to 1 over a short duration, giving a visual confirmation effect when identity verification or profile creation completes.

---

#### xml/

- `backup_rules.xml`: Controls which data is included in Android Auto Backup (currently default, no custom exclusions).
- `data_extraction_rules.xml`: Controls data extraction rules for Android 12 and above, used alongside `backup_rules.xml`.

---

## 1. Introduction of the Application

NearNeed is an Android-based mobile application designed to connect people who need local services or assistance with people in their vicinity who are willing to provide those services. The platform operates on a two-role model: a Seeker, who posts a request for help or a paid gig, and a Provider, who browses available posts and applies to the ones they can fulfil. A single user account can switch between both roles, allowing the same person to be a seeker for one task and a provider for another.

The application supports two types of posts. A Gig Post is a paid service request where the seeker specifies a budget, urgency level, preferred date and time, and location. A Community Post is a volunteer-based request where the seeker needs help without monetary exchange, such as medical assistance, food distribution, or transport during an emergency. Providers can apply to either type, and seekers review the applicants before accepting one.

Once a seeker accepts an applicant, a booking is created on the platform. The two parties can communicate through the in-app chat system and coordinate the rest of the work directly. The application also includes a map view that displays active posts as location pins, allowing providers to visually discover nearby opportunities without scrolling through a list.

The backend is built entirely on Firebase, using Firestore for real-time data, Firebase Authentication for user identity, and Firebase Storage for image uploads. The Android frontend follows the MVVM architectural pattern with a local Room database for offline caching. The application was developed as a team project, with different members responsible for distinct modules including the seeker flow, provider flow, maps, chat, and bookings.

---

## 2. Motivation of the Application

The idea behind NearNeed came from observing a gap that exists in most urban and semi-urban neighbourhoods. When someone needs a plumber, an electrician, or even just a hand moving furniture, the usual options are either to call a large aggregator platform with long wait times and high service fees, or to rely entirely on word of mouth. Neither approach works well at a hyperlocal level, particularly for smaller or more immediate tasks.

At the same time, many people in the same locality have relevant skills or free time but no easy way to offer them. A retired electrician, a college student willing to do deliveries, or a neighbour with a car who can help with transport during an emergency has no platform to make their availability known to people nearby.

Existing platforms like UrbanClap (Urban Company) focus on verified professional services with fixed pricing, which excludes informal help and community-based assistance. Platforms like OLX or Facebook Marketplace are not designed for service requests and lack the structured flow of posting, applying, and booking.

NearNeed was built to fill this gap by creating a lightweight, location-aware platform where the friction of finding and offering help is reduced to posting a request and reviewing who responds. The community volunteering feature was added specifically because hyperlocal platforms have a strong opportunity to support non-monetary help during situations like medical emergencies, natural events, or resource shortages, which no existing mainstream app addresses in a structured way.

The motivation was also technical. The project was an opportunity to work with a real-time, multi-user system involving geolocation, asynchronous data flows, Firebase integration, and a dual-role user model, all of which present challenges that go beyond standard tutorial-level Android development.

---

## 3. Use of the Application

NearNeed is intended for use by individuals within a city or locality who either need help with a task or are looking for short-term work or volunteering opportunities nearby.

**For Seekers:**
A seeker opens the app and creates a post describing what they need. They select a category (Cleaning, Plumbing, Electrical, Delivery, and others), set their urgency level, pick a preferred date and time, and drop a location pin. They can attach photos of the problem or the site. Once the post is live, it appears on the map and in the provider feed for nearby users. The seeker then reviews incoming applications, each of which shows the applicant's name, rating, proposed budget, payment preference, and a message. The seeker accepts one applicant, which creates a booking and opens a chat thread with the provider.

For community posts, the flow is similar but the seeker instead specifies how many volunteers are needed and the nature of the assistance required. Interested volunteers can express interest with a message, and the seeker manages the slots.

**For Providers:**
A provider switches to provider mode from the home screen and sees a feed of active posts near their location, both as a list and as pins on a map. They can filter by category or search by keyword. Tapping a post shows the full details, and they can apply by entering a message, proposing a budget, and selecting a payment method. After a seeker accepts their application, the booking appears in their bookings tab and they can coordinate through chat.

**Shared Features:**
Both roles share access to the chat module, the bookings tab which tracks all current and past activity, and the profile screen. The role switch is instant and requires no logout, making the app flexible for users who operate in both capacities depending on the situation.

---

## 4. Future Functionalities

### 1.1 In-App Payment with Escrow

Currently the app records a payment method preference (Cash or UPI) but no actual transaction takes place inside the app. Adding a payment gateway such as Razorpay with an escrow mechanism would mean the seeker's money is held by the platform at the time of booking confirmation and released to the provider only after the seeker marks the job as complete. This would solve the single biggest trust problem on both sides of the platform: seekers fear paying for work that never gets done, and providers fear doing work without guaranteed payment. It also enables the platform to automatically deduct a commission before releasing the payout, making the monetization model functional without any manual intervention.

### 1.2 Full Booking Lifecycle with Live Provider Tracking

Right now a booking is created as a single Firestore document with status "confirmed" and nothing changes after that. There is no way for the seeker to know whether the provider is on the way, whether the job has started, or when it is done. Adding a proper status pipeline (Confirmed, En Route, In Progress, Completed, Cancelled) combined with the provider streaming their live GPS coordinates to Firestore during the En Route phase would let the seeker track the provider on the map in real time, exactly like a cab booking. This would significantly reduce the anxiety and follow-up calls that happen between acceptance and arrival, and it would give the platform a clear completion event to trigger payment release and review prompts.

### 1.3 Firebase Cloud Messaging for Background Notifications

The entire app currently relies on Firestore real-time listeners for updates, which only work while the app is open and in the foreground. A seeker has no way to know someone applied to their post unless they open the app and check. Integrating Firebase Cloud Messaging would allow the server to push a notification to the device the moment a relevant event occurs, whether that is a new applicant, a booking acceptance, an incoming chat message, or a status change. For a two-sided marketplace this is not an optional enhancement but a core requirement for the app to function in real usage, because users will simply uninstall an app that requires them to manually refresh to see activity.

### 1.4 AI-Based Provider-Seeker Matching

The current discovery model is entirely passive: providers scroll through nearby posts and manually apply to ones that interest them. A matching system would analyse a provider's completed booking history, their accepted categories, their location pattern, and their rating, and then automatically surface the most relevant open posts to them as ranked recommendations. On the seeker side, when a post is created, the system could proactively notify a shortlist of nearby high-rated providers in the matching category rather than waiting for them to discover it organically. This would reduce the time between posting and receiving the first application, which is the metric that most determines whether a seeker stays on the platform or abandons it.

### 1.5 Provider Verification and Trust System

The `VerifiedBadgeHelper` class in the app applies a visual badge to provider profiles, but the badge is not connected to any actual verification process. There is currently no way for a seeker to know whether a provider has been checked in any meaningful way. Building a verification flow where providers submit a government ID or a skill certificate, which goes through an admin review queue before the badge is awarded, would make the badge credible. Beyond ID verification, adding a post-completion rating and review system (the current reviews in `VolunteerProfileActivity` are hardcoded placeholders) would create a reputation layer where providers with consistently good work naturally rise in visibility and providers with complaints are flagged. For a platform that sends strangers to each other's homes, trust infrastructure is the most important long-term investment.

---

## 2. Plan to Monetize the App

### 2.1 Commission on Completed Bookings

The most direct revenue model is taking a percentage cut (typically 10 to 15 percent) from each booking that is completed and paid through the app. This requires in-app payment integration (see Section 1.1). The commission is deducted before the provider receives their payout, so it is invisible to the seeker and automatic for the provider.

This model works because neither party pays upfront. Revenue only occurs on successful transactions, which aligns the platform's incentive with actually delivering value.

### 2.2 Featured Listings for Providers

Providers could pay a small flat fee to have their profile or service listing appear at the top of the seeker's nearby results or on the map as a highlighted pin. The existing `NearbyRequestsAdapter` and map marker system can support a priority rank field on the provider or post document.

This is low-friction for providers since they only pay when they want visibility, not on every transaction.

### 2.3 Premium Provider Subscription

A monthly subscription tier for providers could offer benefits such as appearing in more search results, getting notified of new posts before free-tier providers, or being able to apply to more posts per day. A free tier with a daily application limit would create a natural incentive to upgrade.

The risk with subscriptions is that early-stage providers may not see enough volume to justify paying before the platform has enough seekers. This model works better once the user base is established.

### 2.4 Convenience Fee on Seekers

A small flat fee (such as 10 to 20 rupees) charged to the seeker at the time of booking confirmation can cover platform operating costs without a large percentage cut. This is common in food delivery and ride-hailing apps. Seekers generally accept this if the fee is displayed transparently at checkout.

### 2.5 Community Post Sponsorship

Organizations or NGOs running community drives could pay to have their community posts promoted to the top of the community feed or sent as push notifications to nearby users. This is particularly relevant given the community volunteering module already in the app.

---

## 3. Conclusion

### What We Built

NearNeed is an Android application that connects people who need help with local tasks (seekers) to people willing to provide that help (providers). The core features we implemented include gig and community post creation, a map-based discovery view, real-time chat, a bookings system, and an applicant review workflow.

### Technical Learnings

Working with Firestore's real-time listeners was one of the most significant technical learning experiences of this project. We had to manage listener lifecycles carefully, removing registrations in `onCleared()` and `onStop()` to prevent memory leaks. Understanding when a LiveData observer fires versus when a Firestore snapshot arrives took deliberate debugging.

Implementing the offline-first pattern using Room alongside Firestore taught us why apps need a local cache. During testing without internet, the app showed stale data from Room immediately, which felt more production-grade than a loading spinner. The `PostEntity` and `fromPost()`/`toPost()` conversion pattern also gave us practical experience with data normalization across two storage layers.

The MVVM architecture enforced a discipline of keeping Activities thin. Early in development, some data-fetching logic was written directly inside Activity methods. Refactoring it into ViewModels and the Repository layer made the code much easier to follow and debug, especially when multiple screens needed to observe the same data.

RecyclerView optimization became important when we introduced horizontal scrolling dashboards. Understanding that `onBindViewHolder()` is called on recycled views, and that view inflation only happens once per ViewHolder, helped us avoid common bugs like stale click listeners persisting across recycled items.

### Design Challenges

Handling role switching between seeker and provider on a single account required careful thought. Both home screens (HomeSeekerActivity and HomeProviderActivity) share the same Firestore post collection, but the filters, adapters, and UI components differ significantly. A toggle button in the navbar handles the switch but required clearing the Activity back stack properly using `FLAG_ACTIVITY_CLEAR_TOP`.

The multi-step post creation flow required passing data between activities via Intent extras. Keeping this data consistent across Step 1 and Step 2, especially for photo URIs which are only valid within the app's session, required careful handling. We also had to ensure that if the user pressed Back from Step 2, Step 1 restored its state correctly.

### Challenges and What We Would Do Differently

Integration between team members' modules required agreeing on shared data models early. In practice, the `Application` model went through two iterations because the seeker side and provider side had different field requirements that were not aligned at the start. This caused some rework. In a future project, we would define shared models in a separate document before anyone writes code.

The CategoryPredictor's keyword approach is functional but limited. Given more time, we would explore using a lightweight on-device text classifier trained on service descriptions, which would handle synonyms and misspellings better.

Overall, the project gave us hands-on experience with the full stack of an Android application from database design to UI polish, and introduced us to the real complexity of building a two-sided marketplace.

---

## 4. Resource References

### Official Documentation

- Android Developer Documentation. *Activities, Fragments, Intents, RecyclerView, ViewModel, LiveData, Room.* https://developer.android.com/docs

- Firebase Documentation. *Cloud Firestore, Firebase Authentication, Firebase Storage, Real-time Listeners.* https://firebase.google.com/docs

- Material Design 3. *Components: BottomSheet, MaterialCardView, MaterialButton, Slider, ChipGroup.* https://m3.material.io/components

- Google Maps SDK for Android. *Markers, Camera, Location APIs.* https://developers.google.com/maps/documentation/android-sdk

### Books and Guides

- Philipp Lackner. *Android Development with Kotlin and Firebase.* YouTube channel used for ViewModel and Repository pattern walkthroughs.

- Coding in Flow. *RecyclerView, Room Database, and MVVM Architecture.* YouTube channel used for understanding adapter patterns and database integration.

- Android Jetpack documentation for understanding `ActivityResultContracts` for image picking and permission handling.

### Community Resources

- Stack Overflow. Questions and answers related to Firestore real-time listeners, RecyclerView item decoration, BottomSheetDialogFragment lifecycle, and LocationServices permission handling.

- GitHub. Open-source Android projects used to understand practical MVVM implementations and Firestore pagination patterns.

- Firebase community forums. Used to resolve issues with Firestore query ordering, composite index requirements, and offline persistence configuration.

### Libraries Used

- Firebase BOM (Bill of Materials) for version-consistent Firebase SDK dependencies.
- Google Play Services for FusedLocationProviderClient and geocoding.
- Glide (image loading library) for loading user profile photos and post images from Firebase Storage URLs.
- Material Components for Android (com.google.android.material) for all Material Design UI components.
