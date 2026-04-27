# NearNeed Viva Preparation Guide — Ultimate Edition

> **How to use this guide:** Read it like a textbook. Every concept is explained two ways — the *technical* way (what you'd say to the examiner) and the *layman* way (plain English so you actually understand it). Code snippets are taken straight from the real project files. Memorise the code structure and the "why" behind every choice.

---

## Table of Contents

1. [Android Studio & Project Basics](#1-android-studio--project-basics)
2. [Core Android Concepts](#2-core-android-concepts)
3. [Firebase — The Backend of NearNeed](#3-firebase--the-backend-of-nearneed)
4. [Architecture: MVVM + Repository](#4-architecture-mvvm--repository)
5. [Person 1 — Identity & Trust (Auth, Profile, Verification)](#5-person-1--identity--trust)
6. [Person 2 — Seeker & Demand (Posts, Applications)](#6-person-2--seeker--demand)
7. [Person 3 — Provider & Market Intelligence (Maps, Location)](#7-person-3--provider--market-intelligence)
8. [Person 4 — Engagement & Real-time (Chat, Bookings, Notifications)](#8-person-4--engagement--real-time-systems)
9. [System Integration — How All Modules Connect](#9-system-integration)
10. [AI Chat Feature (Gemini)](#10-ai-chat-feature)
11. [Viva Q&A — Every Question You Could Face](#11-viva-qa)
12. [1-2 Minute Scripts for Each Person](#12-scripts)
13. [Final Revision Checklist](#13-final-revision-checklist)

---

## 1. Android Studio & Project Basics

### 1.1 What is Android Studio?

**Technical:** Android Studio is the official Integrated Development Environment (IDE) for Android app development, built on top of IntelliJ IDEA. It uses Gradle as its build system and compiles Java/Kotlin source code into APK (Android Package) files that run on Android devices.

**Layman:** Think of Android Studio as Microsoft Word, but instead of writing essays, you write code and it converts it into an app that runs on your phone.

---

### 1.2 Project Structure (What Each Folder Means)

```
NearNeed_App/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/nearneed/   ← All .java source files (your actual code)
│   │       ├── res/
│   │       │   ├── layout/                  ← XML files for screen designs (what you SEE)
│   │       │   ├── drawable/                ← Images, icons, shapes
│   │       │   ├── values/
│   │       │   │   ├── strings.xml          ← All text in the app stored here
│   │       │   │   ├── colors.xml           ← Color palette
│   │       │   │   └── themes.xml           ← App-wide styling
│   │       └── AndroidManifest.xml          ← App's "birth certificate" — declares all activities, permissions, services
├── build.gradle (Module)                    ← Lists all dependencies (Firebase, MapLibre, etc.)
└── google-services.json                     ← Firebase config file (secret keys)
```

**Layman:**
- `java/` = The brain of the app (logic)
- `res/layout/` = The face of the app (what the screen looks like)
- `AndroidManifest.xml` = The government ID of the app (registers everything)
- `build.gradle` = Shopping list of libraries the app needs
- `google-services.json` = The password that connects your app to Firebase

---

### 1.3 What is a Gradle Build?

**Technical:** Gradle is a build automation tool. When you press "Run" in Android Studio, Gradle compiles your Java files into `.dex` bytecode, packages resources, and creates an `.apk` file installable on Android.

**Layman:** Gradle is like a chef. You give it raw ingredients (your code + libraries), and it cooks them into the final dish (the app).

---

### 1.4 What is AndroidManifest.xml?

Every Activity, Service, and Permission must be declared here. Without this, Android doesn't know the component exists.

```xml
<!-- Example from NearNeed: declaring an Activity -->
<activity android:name=".OtpEnterActivity" />

<!-- Declaring a Service -->
<service android:name=".NearNeedMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>

<!-- Declaring a Permission -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

**Layman:** If you build a room in your house but don't put it on the floor plan, the city says it doesn't exist. Same here — if you write a Java class but don't declare it in the Manifest, Android ignores it.

---

## 2. Core Android Concepts

### 2.1 Activity — What is it?

**Technical:** An `Activity` is a single, focused screen that a user can interact with. It extends `AppCompatActivity` and has a lifecycle managed by the Android OS. Each Activity is declared in `AndroidManifest.xml` and associated with an XML layout file that defines its UI.

**Layman:** An Activity is one full screen of the app. Just like a website has multiple pages, an Android app has multiple Activities. Each Activity = one screen.

**The Activity Lifecycle (EXTREMELY IMPORTANT for viva):**

```
App opens       App goes to background     App is killed
    ↓                    ↓                      ↓
onCreate()  →  onStart()  →  onResume()  →  onPause()  →  onStop()  →  onDestroy()
                                               ↑
                                    User comes back to app
```

| Lifecycle Method | When it's called | What you do here in NearNeed |
|---|---|---|
| `onCreate()` | Screen is first created | Initialize views (`findViewById`), set up listeners, set the layout (`setContentView`) |
| `onStart()` | Screen becomes visible | Start animations, register listeners |
| `onResume()` | Screen is active and interactive | Start Firestore real-time listeners, refresh data |
| `onPause()` | Screen partially hidden (e.g., dialog opened) | Save draft data, pause video |
| `onStop()` | Screen fully hidden | Remove Firestore listeners to save reads/money |
| `onDestroy()` | Screen is being destroyed | Clean up final resources |

**Example from NearNeed — `OtpEnterActivity.java`:**
```java
// This is called when the screen is first created
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_otp_enter);  // Load the XML layout (the screen design)

    mAuth = FirebaseAuth.getInstance();            // Get Firebase Auth instance

    // Find the UI elements declared in XML by their ID
    btnSendOtp = findViewById(R.id.btnSendOtp);
    etPhone    = findViewById(R.id.etPhone);

    setupListeners();  // Hook up button click actions
}
```

**What `setContentView(R.layout.activity_otp_enter)` means:**
- `R` is a special auto-generated class that maps resource names to IDs
- `R.layout.activity_otp_enter` refers to the file `res/layout/activity_otp_enter.xml`
- This line says "draw this XML file's design on the screen"

**What `findViewById(R.id.btnSendOtp)` means:**
- Finds the button in the XML layout that has `android:id="@+id/btnSendOtp"`
- Returns a Java object you can control (change text, attach click listeners, etc.)

---

### 2.2 Fragment — What is it?

**Technical:** A `Fragment` is a reusable UI component that lives inside an `Activity`. It has its own lifecycle (similar to Activity) and can be added, removed, or replaced at runtime. Fragments share the hosting Activity's lifecycle but can also have their own.

**Layman:** If an Activity is a full web page, a Fragment is a widget or section on that page. For example, a page might have a navigation bar (Fragment) and a content area (another Fragment) — both on the same screen.

**Fragments used in NearNeed:**

| Fragment | Where it lives | What it does |
|---|---|---|
| `MapsFragment` | `MapsActivity` | Shows the MapLibre map with post markers |
| `ProfileFragment` | Navigation tab | Shows user profile in tabbed interface |
| `MessagesFragment` | Navigation | Shows list of chat conversations |
| `BookingsFragment` | `BookingsActivity` | Shows booking cards in a tab |
| `SeekerUpcomingFragment` | `BookingsActivity` | Upcoming jobs for seekers |
| `ProviderUpcomingFragment` | `BookingsActivity` | Upcoming jobs for providers |
| `SeekerOngoingFragment` | `BookingsActivity` | Active/in-progress jobs |
| `HomeFragment` | Home screens | Embedded home content |

**Fragment key difference from Activity:**
- Fragment uses `onCreateView()` instead of `onCreate()` + `setContentView()`
- Fragment needs to inflate (build) its layout:
```java
@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // "inflate" = take the XML design and turn it into real View objects in memory
    return inflater.inflate(R.layout.fragment_messages, container, false);
}
```

---

### 2.3 Intent — Navigating Between Screens

**Technical:** An `Intent` is a messaging object used to request an action from another Android component. An **Explicit Intent** names a specific target class. An **Implicit Intent** describes an action (like "open a camera") and lets Android find any app that can handle it.

**Layman:** An Intent is like a taxi booking request. You tell it exactly where you want to go (Explicit) or just say "I need to go somewhere with a camera" and Android figures out which app to use (Implicit).

**Example — Explicit Intent (most common in NearNeed):**
```java
// After OTP code is sent, go to OtpVerifyActivity and pass data
Intent intent = new Intent(OtpEnterActivity.this, OtpVerifyActivity.class);
intent.putExtra("IS_SIGNUP", isSignup);          // Pass a boolean
intent.putExtra("PHONE_NUMBER", phone);           // Pass the phone number string
intent.putExtra("VERIFICATION_ID", mVerificationId); // Pass Firebase verification ID
startActivity(intent);
finish(); // Close current screen — user can't press Back to return here
```

**Reading the data on the other side (`OtpVerifyActivity.java`):**
```java
mVerificationId = getIntent().getStringExtra("VERIFICATION_ID");
String phone    = getIntent().getStringExtra("PHONE_NUMBER");
boolean isSignup = getIntent().getBooleanExtra("IS_SIGNUP", false); // false = default if not found
```

**Intent Flags — Controlling the Back Stack:**

The "Back Stack" is Android's history of which screens the user visited, like browser history. Pressing the Back button pops the last screen off this stack.

| Flag | What it does | When NearNeed uses it |
|---|---|---|
| `FLAG_ACTIVITY_NEW_TASK` | Start activity in new task | Used during login/logout navigation resets |
| `FLAG_ACTIVITY_CLEAR_TASK` | Clear all previous activities from the stack | After login, user cannot press Back to return to login screen |
| `FLAG_ACTIVITY_CLEAR_TOP` | Remove all activities above the target in the stack | After signup check, go to AccountTypeActivity and remove everything above |

**Example from `MainActivity.java` — Role-based routing after login:**
```java
private void dispatchByRole() {
    String role = RoleManager.getRole(this); // Read role from local storage
    Intent intent;

    if (RoleManager.ROLE_PROVIDER.equals(role)) {
        intent = new Intent(this, HomeProviderActivity.class);
    } else {
        intent = new Intent(this, HomeSeekerActivity.class);
    }

    // FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK together mean:
    // "Start fresh — destroy ALL previous screens so user can't press Back to login"
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    finish();
}
```

---

### 2.4 RecyclerView & Adapter — Displaying Lists

**Technical:** `RecyclerView` is an advanced, flexible `ViewGroup` for displaying large datasets efficiently. It *recycles* off-screen View objects (called ViewHolders) instead of creating new ones, significantly reducing memory consumption and improving scroll performance. An `Adapter` binds data model objects to item View layouts.

**Layman:** Imagine a list of 1,000 contacts on your phone. RecyclerView only actually draws the 8-10 contacts visible on screen. When you scroll down, instead of creating brand-new views for new contacts (slow, wastes memory), it takes the views that just scrolled OFF the top of the screen and reuses them for the new contacts scrolling into view at the bottom. This is called "recycling."

**How a RecyclerView + Adapter works (step by step):**

1. **XML Layout:** You have a `RecyclerView` in your screen's XML
2. **Item Layout:** You create another XML file for ONE item in the list
3. **ViewHolder:** A Java class that holds references to views inside one item
4. **Adapter:** A class with 3 key methods:
   - `onCreateViewHolder()` — inflate (create) one item view
   - `onBindViewHolder()` — fill one item with actual data
   - `getItemCount()` — how many items total

**Adapters used in NearNeed:**

| Adapter | What it displays |
|---|---|
| `NearbyRequestsAdapter` | Gig post cards on the provider dashboard |
| `CommunityVolunteeringAdapter` | Community opportunity cards on provider dashboard |
| `ResponsesAdapter` | List of applicants in `ResponsesActivity` |
| `VolunteersAdapter` | Volunteer list for community posts |
| `BookingsAdapter` | Booking cards in `BookingsActivity` |
| `AiChatAdapter` | AI chat message bubbles |
| `SearchPredictionAdapter` | Location search results |
| `ReviewsAdapter` | User reviews and ratings |
| `NotificationPopupAdapter` | Notification items in the popup |

---

### 2.5 SharedPreferences — Local Data Storage

**Technical:** `SharedPreferences` is Android's built-in mechanism for storing primitive key-value data (String, int, boolean, float, long) persistently on the device. It's stored in an XML file in the app's private directory. It survives app restarts but is cleared on uninstall.

**Layman:** Think of SharedPreferences like a notepad stuck to your fridge. You write small notes (name="Reia", role="SEEKER") on it, and whenever you need them, you read off the notepad — no need to call the server every time.

**How `RoleManager` uses SharedPreferences (full code from `RoleManager.java`):**

```java
public class RoleManager {
    private static final String PREF_NAME = "AppPrefs";  // Name of the "notepad"
    private static final String KEY_ROLE = "user_role";  // The key for the role value

    // Reading the role
    public static String getRole(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        // MODE_PRIVATE = only this app can read this notepad
        return prefs.getString(KEY_ROLE, ROLE_SEEKER); // ROLE_SEEKER is the default if nothing is saved
    }

    // Saving/updating the role
    public static void setRole(Context context, String role) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit(); // Open the notepad for editing
        editor.putString(KEY_ROLE, role);               // Write the role
        editor.putLong(KEY_ROLE_CHANGED_AT, System.currentTimeMillis()); // Track when it changed
        editor.apply(); // Save changes (async, does not block the UI thread)
    }

    // Switching between roles — if SEEKER → PROVIDER, if PROVIDER → SEEKER
    public static void toggleRole(Context context) {
        String currentRole = getRole(context);
        String newRole = ROLE_SEEKER.equals(currentRole) ? ROLE_PROVIDER : ROLE_SEEKER;
        setRole(context, newRole);
    }
}
```

**Why not save everything in SharedPreferences?**
- It can only store simple data types (String, int, boolean)
- For complex objects (lists of posts, chat messages) you need a database (Firestore, Room)
- It can become stale if the server has newer data

---

### 2.6 Singleton Pattern

**Technical:** The Singleton design pattern ensures a class has only **one instance** throughout the application's lifetime, providing a global access point to that instance.

**Layman:** Like there's only one principal in a school — everyone goes to the same one person. There's never two principals at the same time.

**Example from `BookingStateManager.java`:**
```java
public class BookingStateManager {
    // The one and only instance — stored as a static variable
    private static BookingStateManager instance;

    // Private constructor — nobody outside can do "new BookingStateManager()"
    private BookingStateManager() {}

    // The global access point — always returns the same single instance
    public static BookingStateManager getInstance() {
        if (instance == null) instance = new BookingStateManager(); // Create only if it doesn't exist
        return instance;
    }
}
```

**Why it's used:** `BookingStateManager` holds the current status of all bookings in memory. If you created multiple instances, each would have a different view of the state — causing bugs. The Singleton ensures all parts of the app see the same state.

---

### 2.7 Interface & Callback Pattern

**Technical:** An interface in Java defines a contract — a set of method signatures that implementing classes must provide. In Android, interfaces are heavily used as **callbacks** — a way to notify the caller when an asynchronous operation completes (success or failure).

**Layman:** Imagine you ask a friend to order food for you. You give them your phone number (the callback) and say "call me when it arrives." You go do other things. When the food arrives, they call you. That's exactly how callbacks work — you pass a function, and it gets called when the async task finishes.

**Example from `PostRepository.java`:**
```java
// Define the interface (the "contract")
public interface PostListener {
    void onPostsLoaded(List<Post> posts); // Called when data arrives successfully
    void onError(Exception e);            // Called if something goes wrong
}

// The method that uses the callback
public static ListenerRegistration observeUserPosts(String userId, PostListener listener) {
    return db.collection("posts")
        .whereEqualTo("createdBy", userId)
        .addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                listener.onError(e);      // Something failed — call the error method
                return;
            }
            List<Post> posts = ... // parse posts
            listener.onPostsLoaded(posts); // Success — call with the data
        });
}
```

**Example from `GeocodingHelper.java`:**
```java
// Interface for geocoding results
public interface OnGeocodingResultListener {
    void onResults(List<SearchPredictionAdapter.GeocodingResult> results);
}
```

---

### 2.8 Background Threads — Keeping the UI Smooth

**Technical:** Android's UI runs on the **Main Thread** (also called the UI Thread). Any network call, file I/O, or heavy computation on the Main Thread will block it, causing the screen to freeze. Android enforces this by throwing a `NetworkOnMainThreadException`. Background threads (worker threads) handle heavy work; results are posted back to the Main Thread using `Handler(Looper.getMainLooper()).post(...)`.

**Layman:** The Main Thread is like the waiter at a restaurant. If you ask the waiter to go cook the food himself, he can't take other orders meanwhile — the restaurant freezes. Instead, the waiter passes the order to the kitchen (background thread), and when the food is ready, the kitchen alerts the waiter to deliver it.

**Example from `GeocodingHelper.java`:**
```java
public static void performSearch(String query, OnGeocodingResultListener listener) {
    // Start a new background thread for the network calls
    new Thread(() -> {
        List<GeocodingResult> aggregated = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(2); // Wait for BOTH API calls to finish

        // API Call 1 — Photon (runs async in OkHttp's thread pool)
        httpClient.newCall(photonRequest).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response r) {
                // Parse JSON results, add to aggregated list
                latch.countDown(); // Signal that Photon is done
            }
            @Override
            public void onFailure(Call call, IOException e) {
                latch.countDown(); // Even on failure, signal done
            }
        });

        // API Call 2 — Nominatim (also runs async)
        httpClient.newCall(nominatimRequest).enqueue(new Callback() { /* same pattern */ });

        latch.await(4, TimeUnit.SECONDS); // Wait max 4 seconds for both to complete

        // Deduplicate results (remove locations that are < 500 meters apart)
        List<GeocodingResult> finalList = deduplicateByDistance(aggregated);

        // Hand results back to the MAIN THREAD so UI can update safely
        new Handler(Looper.getMainLooper()).post(() -> listener.onResults(finalList));

    }).start(); // Start the background thread
}
```

**Key terms:**
- `CountDownLatch` — A synchronization tool. You set a count (e.g., 2). Each thread calls `countDown()` when done. `await()` blocks until count reaches 0.
- `OkHttp` — A popular HTTP client library for making network requests (API calls)
- `Looper.getMainLooper()` — Reference to the Main Thread's message queue
- `Handler.post(runnable)` — Schedule code to run on a specific thread

---

### 2.9 BottomSheetDialog

**Technical:** A `BottomSheetDialogFragment` or `BottomSheetDialog` slides up from the bottom of the screen, overlaying the current Activity partially. It's part of Material Design and is used for contextual quick actions without full navigation.

**Layman:** Like the share sheet that pops up from the bottom when you share a photo on your phone. It's not a full new screen — it just slides up from the bottom.

**Where NearNeed uses it:**
- `RequestApplyBottomSheet` — Provider applies to a gig post
- `CommunityVolunteerBottomSheet` — Provider volunteers for community post
- `LocationPickerHelper` — Pick a location from search/GPS/presets
- `CompleteBookingDialogFragment` — Final confirmation before completing a booking

---

## 3. Firebase — The Backend of NearNeed

### 3.1 What is Firebase?

**Technical:** Firebase is Google's Backend-as-a-Service (BaaS) platform. It provides a suite of cloud services that replace the need for writing custom server-side code. NearNeed uses: Firebase Authentication, Cloud Firestore, Firebase Storage, and Firebase Cloud Messaging (FCM).

**Layman:** Instead of building your own server from scratch (like building your own electricity plant), Firebase is like the city's electricity grid — you just plug in and use it.

---

### 3.2 Firebase Authentication

**Technical:** Firebase Auth handles user identity. It supports multiple sign-in methods (phone OTP, email, Google, etc.). It manages session tokens (JWT), which persist across app restarts via `FirebaseUser`. The current signed-in user is always accessible via `FirebaseAuth.getInstance().getCurrentUser()`.

**Layman:** Firebase Auth is the security guard at the door. It checks who you are (via OTP), gives you a pass (session token), and from then on, you can go anywhere in the app because you're "logged in."

**Key terms:**
- **OTP (One-Time Password)** — A 6-digit code sent via SMS that proves you own the phone number
- **UID (User ID)** — A unique string Firebase assigns every user (e.g., `"abc123xyz"`). Used as the document ID in Firestore to store user data
- **PhoneAuthCredential** — An object that contains both the verification ID and the OTP code, used to complete sign-in
- **JWT (JSON Web Token)** — The encrypted "pass" Firebase issues after login, automatically attached to all requests

---

### 3.3 Cloud Firestore (The Database)

**Technical:** Firestore is a NoSQL, document-oriented, real-time cloud database. Data is organized in **Collections** (like folders) and **Documents** (like JSON files). Unlike SQL databases, there are no tables or rows — just flexible JSON-like documents. Firestore supports real-time listeners that push data changes to clients instantly.

**Layman:** If a traditional database is like an Excel spreadsheet (rigid rows and columns), Firestore is like a Google Drive folder full of sticky notes. Each sticky note (Document) can have whatever fields it wants, and you can organize notes into folders (Collections).

**Firestore Structure in NearNeed:**

```
Firestore Root
├── users/ (Collection)
│   └── {uid}/ (Document — one per user)
│       ├── name: "Reia"
│       ├── phone: "9876543210"
│       ├── role: "SEEKER"
│       ├── isVerified: true
│       ├── lat: 19.0760
│       └── lng: 72.8777
│
├── posts/ (Collection)
│   └── {postId}/ (Document — one per post)
│       ├── title: "Need plumber"
│       ├── description: "Pipe leaking..."
│       ├── category: "Plumbing"
│       ├── type: "GIG"
│       ├── createdBy: "{uid}"
│       ├── lat: 19.0760
│       └── lng: 72.8777
│
├── applications/ (Collection)
│   └── {applicationId}/ (Document)
│       ├── postId: "{postId}"
│       ├── applicantId: "{uid}"
│       ├── status: "pending" / "accepted" / "rejected"
│       └── message: "I can help..."
│
├── bookings/ (Collection)
│   └── {bookingId}/ (Document)
│       ├── seekerId, providerId
│       ├── status: "pending" / "ongoing" / "completed" / "cancelled"
│       └── postId, amount
│
├── messages/ (Collection — for chat)
│   └── {chatId}/ (Document)
│       └── messages/ (Sub-Collection)
│           └── {messageId}/ (Document)
│               ├── senderId, messageText, timestamp
│               └── imageUri / audioPath (if media)
│
├── chats/ (Collection — inbox metadata)
│   └── {chatId}/
│       ├── participants: [uid1, uid2]
│       ├── lastMessage: "See you at 5"
│       └── lastTimestamp: 1714220000
│
├── reviews/ (Collection)
│   └── {reviewId}/
│       ├── reviewerId, revieweeId, rating, comment
│
└── Users/ (Collection — capital U — for FCM tokens + notifications)
    └── {uid}/
        ├── fcmToken: "..."
        └── notifications/ (Sub-Collection)
            └── {notifId}/
                ├── title, message, timestamp, read: false
```

**Real-time Listener vs. One-Time Fetch:**

| Type | Method | When to use | NearNeed example |
|---|---|---|---|
| Real-time listener | `addSnapshotListener()` | Data changes frequently and UI must update instantly | Chat messages, post lists, notification badge |
| One-time fetch | `.get()` | You only need the data once | Check if user profile exists during signup |

**Example of real-time listener from `ChatRepository.java`:**
```java
public static ListenerRegistration observeMessages(String chatId, MessageListener listener) {
    return FirebaseFirestore.getInstance()
        .collection("messages")   // Go to "messages" collection
        .document(chatId)         // Find the document for this specific chat
        .collection("messages")   // Go to the sub-collection of messages
        .orderBy("timestamp", Query.Direction.ASCENDING) // Sort oldest → newest
        .addSnapshotListener((snapshot, e) -> {
            // This block runs AUTOMATICALLY every time a new message is added
            if (e != null) { listener.onError(e); return; }
            if (snapshot != null) {
                List<ChatMessage> messages = snapshot.toObjects(ChatMessage.class);
                // toObjects() = automatically converts each Firestore document into a ChatMessage Java object
                listener.onMessagesLoaded(messages);
            }
        });
}
```

---

### 3.4 Firebase Storage

**Technical:** Firebase Storage is a cloud object storage service for storing large files (images, audio, video). It is separate from Firestore. Files are uploaded to Storage, which returns a download URL (a permanent HTTPS link) that is then stored in Firestore as a string field.

**Layman:** Firestore is for text and numbers (like a notepad). Firebase Storage is for photos and voice messages (like a hard drive in the cloud). You upload a photo to Storage, get a link, and save that link in Firestore.

**Flow in NearNeed (`StorageRepository`):**
1. User picks an image from their phone gallery
2. `StorageRepository.uploadImage(imageUri, callback)` uploads it to `gs://nearneed.appspot.com/images/{uid}/{timestamp}.jpg`
3. Firebase returns a download URL (e.g., `https://firebasestorage.googleapis.com/...`)
4. This URL is stored in the Firestore post document as `imageUrl`
5. Later, `Glide` library loads the image from this URL into an `ImageView`

---

### 3.5 Firebase Cloud Messaging (FCM) — Push Notifications

**Technical:** FCM is Google's cross-platform messaging solution for sending push notifications to Android, iOS, and web apps. Each device gets a unique **FCM token** — a string identifier. The server sends a notification payload to this token. The `FirebaseMessagingService` subclass handles incoming messages via `onMessageReceived()`.

**Layman:** Like how WhatsApp can notify you of a message even when the app is closed. FCM is the postal service — each phone has an address (FCM token), and Firebase delivers the notification to that exact address.

**Full code from `NearNeedMessagingService.java`:**
```java
public class NearNeedMessagingService extends FirebaseMessagingService {

    // Called when a push notification arrives (even when app is backgrounded)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        String title = "NearNeed Alert";
        String body  = "You have a new update.";

        // FCM messages can come in two forms:
        if (remoteMessage.getNotification() != null) {
            // "Notification" payload — Firebase auto-shows this when app is in background
            title = remoteMessage.getNotification().getTitle();
            body  = remoteMessage.getNotification().getBody();
        } else if (remoteMessage.getData().size() > 0) {
            // "Data" payload — custom key-value map, handled manually
            title = remoteMessage.getData().get("title");
            body  = remoteMessage.getData().get("message");
        }

        showNotification(title, body); // Build and display the Android notification
    }

    // Called when FCM generates a new token for this device (first install or token refresh)
    @Override
    public void onNewToken(@NonNull String token) {
        // Save the new token to Firestore so we can send targeted notifications later
        updateTokenInFirestore(token);
    }

    private void updateTokenInFirestore(String token) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Save token under Users/{uid}/fcmToken
        FirebaseFirestore.getInstance().collection("Users").document(userId)
            .update("fcmToken", token);
    }

    private void showNotification(String title, String body) {
        // NotificationChannel required for Android 8.0+ (Oreo)
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                "nearneed_notifications", "NearNeed Notifications", NotificationManager.IMPORTANCE_DEFAULT
            );
            nm.createNotificationChannel(channel);
        }

        // PendingIntent — when user taps the notification, open MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build the visible notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "nearneed_notifications")
            .setSmallIcon(R.drawable.ic_bell)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true) // Dismiss notification when tapped
            .setContentIntent(pendingIntent);

        nm.notify(0, builder.build()); // Show it
    }
}
```

---

## 4. Architecture: MVVM + Repository

### 4.1 What is MVVM?

**Technical:** MVVM stands for **Model-View-ViewModel**. It is an architectural pattern that separates an application into three layers:
- **Model** — Data and business logic (Firestore documents, Java POJOs, Repository classes)
- **View** — UI (Activity, Fragment, XML layouts)
- **ViewModel** — Mediator that holds UI state, exposes `LiveData`, and calls Repository methods

**Layman:** Think of a restaurant:
- **Model** = The kitchen and ingredients (the data)
- **ViewModel** = The waiter (takes order from customer, passes to kitchen, brings food back)
- **View** = The customer (sees and interacts with the food)

The customer (View) never goes into the kitchen (Model). The waiter (ViewModel) handles all communication.

---

### 4.2 What is LiveData?

**Technical:** `LiveData` is a lifecycle-aware observable data holder. It only updates observers (Activities/Fragments) when they are in an active lifecycle state (`STARTED` or `RESUMED`). This prevents crashes from updating UI of destroyed screens.

**Layman:** LiveData is like a TV subscription. Your TV (Activity) only receives the channel (data) when it's turned on (active). When you turn it off (go to background), the subscription pauses. No crashes, no wasted data.

**Example from `PostViewModel.java`:**
```java
public class PostViewModel extends ViewModel {
    // MutableLiveData = private write access (only ViewModel can change this)
    private final MutableLiveData<List<Post>> userPosts = new MutableLiveData<>(new ArrayList<>());

    // LiveData = public read access (Activity can observe but not change this)
    public LiveData<List<Post>> getUserPosts() {
        return userPosts;
    }

    // When Firestore data arrives, update the LiveData
    public void observeUserPosts(String userId) {
        PostRepository.observeUserPosts(userId, new PostRepository.PostListener() {
            @Override
            public void onPostsLoaded(List<Post> posts) {
                userPosts.setValue(posts); // This triggers all observers automatically
            }
            @Override
            public void onError(Exception e) {
                errorMessage.setValue("Error: " + e.getMessage());
            }
        });
    }
}
```

**In the Activity (observing the LiveData):**
```java
// In HomeSeekerActivity.onCreate()
PostViewModel postViewModel = new ViewModelProvider(this).get(PostViewModel.class);

// "observe" = "subscribe to updates"
// The lambda runs every time userPosts changes
postViewModel.getUserPosts().observe(this, posts -> {
    // This code runs on Main Thread automatically when new posts arrive
    adapter.setPosts(posts);
    adapter.notifyDataSetChanged();
});

// Start fetching posts
postViewModel.observeUserPosts(currentUserId);
```

---

### 4.3 What is a ViewModel and why does it survive configuration changes?

**Technical:** A `ViewModel` is a lifecycle-aware component that stores and manages UI-related data. It survives **configuration changes** (like screen rotation) because it is NOT destroyed when an Activity is recreated. Android retains the ViewModel in a special `ViewModelStore` that outlives Activity instances.

**Layman:** When you rotate your phone, Android destroys and recreates the Activity (like closing and reopening the app). Without ViewModel, all your loaded data disappears and you'd have to fetch it again. ViewModel is like a safe deposit box — the Activity building may be demolished and rebuilt, but the safe box stays intact.

---

### 4.4 The Repository Pattern

**Technical:** The Repository pattern creates a clean abstraction layer between ViewModels and data sources (Firestore, Room, APIs). The ViewModel calls the Repository; the Repository decides where to get data from (network, cache, local DB). The ViewModel never directly touches Firestore.

**Layman:** The Repository is like a data warehouse manager. The waiter (ViewModel) asks for "post data." The manager (Repository) figures out the best way to get it — maybe it's cached locally, maybe it needs to call Firestore. The waiter doesn't care how the manager gets it.

**Full MVVM data flow in NearNeed:**

```
User Action (taps button)
       ↓
View (Activity/Fragment)
       ↓ calls
ViewModel.createPost(post, callback)
       ↓ calls
PostRepository.createPost(post, callback)
       ↓ writes to
Firestore "posts" collection
       ↓ success callback
ViewModel receives success
       ↓ updates
LiveData<Boolean> successFlag
       ↓ observed by
View (Activity) → shows "PostedSuccessfullyActivity"
```

---

## 5. Person 1 — Identity & Trust

### 5.1 What Person 1 Owns

Person 1 is responsible for making sure the user is **who they say they are** and has a **complete, trusted profile** before they can use the app. No other module can function without Person 1's output (a verified, role-assigned user).

**Screens owned:** `LoadingActivity` → `WelcomeActivity` → `OtpEnterActivity` → `OtpVerifyActivity` → `AccountTypeActivity` → `ProfileSetupActivity` → `IdVerificationActivity` → `IdVerifiedActivity` → `ProfileSuccessActivity` → `ProfileActivity` → `EditProfileActivity` / `EditProfileProviderActivity` → `SettingsActivity` → `HelpSupportActivity` → `TermsConditionsActivity`

---

### 5.2 OTP Authentication — Deep Dive

**What is OTP (One-Time Password)?**
A 6-digit code sent via SMS to your phone number. It proves that you physically possess that phone number. Firebase handles the SMS sending via its Phone Auth service.

**Step-by-step OTP Flow:**

**Step 1 — User enters phone number (`OtpEnterActivity.java`):**
```java
private void sendVerificationCode(String phoneNumber) {
    btnSendOtp.setEnabled(false); // Disable button to prevent double-tapping
    btnSendOtp.setText("Sending...");

    PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
        .setPhoneNumber("+91" + phoneNumber) // Add India country code
        .setTimeout(60L, TimeUnit.SECONDS)   // OTP expires in 60 seconds
        .setActivity(this)                   // For reCAPTCHA verification
        .setCallbacks(mCallbacks)            // What to do when Firebase responds
        .build();

    PhoneAuthProvider.verifyPhoneNumber(options); // Send the SMS
}
```

**Step 2 — Firebase responds via callbacks:**
```java
private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
    new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

    @Override
    public void onVerificationCompleted(PhoneAuthCredential credential) {
        // Some devices auto-detect the OTP from SMS — Firebase gives us the credential directly
        signInWithPhoneAuthCredential(credential); // Auto-login
    }

    @Override
    public void onVerificationFailed(FirebaseException e) {
        // SMS sending failed — show error
        Toast.makeText(OtpEnterActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        btnSendOtp.setEnabled(true); // Let user try again
    }

    @Override
    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
        // SMS sent! Store the verificationId and go to OtpVerifyActivity
        mVerificationId = verificationId; // This ID + the code the user types = credential

        Intent intent = new Intent(OtpEnterActivity.this, OtpVerifyActivity.class);
        intent.putExtra("VERIFICATION_ID", mVerificationId);
        intent.putExtra("PHONE_NUMBER", phone);
        intent.putExtra("IS_SIGNUP", isSignup);
        startActivity(intent);
        finish(); // Close this screen — can't go back here
    }
};
```

**Step 3 — User enters 6-digit OTP (`OtpVerifyActivity.java`):**
```java
private void verifySignInCode(String code) {
    // Build the credential from the verification ID (from Step 2) + the code the user typed
    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
    signInWithPhoneAuthCredential(credential);
}

private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
    FirebaseAuth.getInstance().signInWithCredential(credential)
        .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // User is now signed in! Check if they're new or existing
                if (isSignup) {
                    checkIfUserExistsAndRoute(); // Is this phone already registered?
                } else {
                    startActivity(new Intent(this, AccountTypeActivity.class));
                }
            } else {
                Toast.makeText(this, "Wrong code or expired.", Toast.LENGTH_SHORT).show();
            }
        });
}
```

**Step 4 — Check if new user or returning (`checkIfUserExistsAndRoute`):**
```java
private void checkIfUserExistsAndRoute() {
    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    // One-time fetch — check if this UID already has a profile in Firestore
    FirebaseFirestore.getInstance().collection("users").document(uid)
        .get()
        .addOnSuccessListener(doc -> {
            if (doc.exists() && doc.getString("name") != null) {
                // Profile already exists — this is a returning user who tapped "Sign Up" by mistake
                Toast.makeText(this, "Account already exists. Please log in.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, AccountTypeActivity.class));
            } else {
                // Genuinely new user — clear any stale cache and go to profile setup
                UserPrefs.clear(this);
                startActivity(new Intent(this, ProfileInfoActivity.class));
            }
            finish();
        });
}
```

---

### 5.3 Role Management — How NearNeed Switches Between Seeker and Provider

Every user can be both a Seeker (someone who posts requests) and a Provider (someone who fulfills them). The `RoleManager` class persists which mode the user is currently in.

**Full code from `RoleManager.java`:**
```java
public class RoleManager {
    private static final String PREF_NAME = "AppPrefs";
    private static final String KEY_ROLE = "user_role";

    public static final String ROLE_SEEKER   = "SEEKER";
    public static final String ROLE_PROVIDER = "PROVIDER";

    public static String getRole(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_ROLE, ROLE_SEEKER); // Default is SEEKER
    }

    public static void setRole(Context context, String role) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ROLE, role)
            .putLong("role_changed_at", System.currentTimeMillis())
            .apply();
    }

    public static boolean isProvider(Context context) { return ROLE_PROVIDER.equals(getRole(context)); }
    public static boolean isSeeker(Context context)   { return ROLE_SEEKER.equals(getRole(context)); }

    public static void toggleRole(Context context) {
        // Flip: SEEKER → PROVIDER or PROVIDER → SEEKER
        setRole(context, isSeeker(context) ? ROLE_PROVIDER : ROLE_SEEKER);
    }
}
```

**How MainActivity uses it to route:**
```java
private void dispatchByRole() {
    Intent intent;
    if (RoleManager.ROLE_PROVIDER.equals(RoleManager.getRole(this))) {
        intent = new Intent(this, HomeProviderActivity.class);
    } else {
        intent = new Intent(this, HomeSeekerActivity.class);
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    finish();
}
```

---

### 5.4 ID Verification with ML Kit OCR

**What is OCR?** Optical Character Recognition. The phone's camera takes a photo of a document (ID card), and ML Kit reads the text in that image — just like how Google Lens reads text from photos.

**What is ML Kit?** A Google SDK that provides on-device machine learning APIs (text recognition, face detection, barcode scanning, etc.). "On-device" means it processes locally — no data sent to any server.

**Why on-device?** Faster (no network round-trip), works offline, more private.

**Verification flow in `IdVerificationActivity.java`:**
1. User uploads front and back images of their government ID
2. ML Kit reads all text visible in the image
3. Code checks for government-related keywords (e.g., "INDIA", "GOVERNMENT", "AADHAAR")
4. Code checks for ID-pattern formats (e.g., 12-digit Aadhaar number: `\d{4}\s\d{4}\s\d{4}`)
5. If both checks pass → mark user as verified in SharedPreferences AND Firestore

```java
private void runOcrOnImage(Uri imageUri, boolean isFront) {
    try {
        InputImage image = InputImage.fromFilePath(this, imageUri);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        recognizer.process(image)
            .addOnSuccessListener(visionText -> {
                String extractedText = visionText.getText(); // All text found in the image
                boolean hasGovKeywords = checkGovKeywords(extractedText);
                boolean hasIdPatterns  = checkIdPatterns(extractedText);

                if (hasGovKeywords || hasIdPatterns) {
                    // Verification successful
                    isFullyVerified = true;
                    UserPrefs.saveVerified(this, true); // Save locally
                    saveVerifiedToFirestore();           // Save to Firestore
                }
            });
    } catch (IOException e) {
        Toast.makeText(this, "Could not read image", Toast.LENGTH_SHORT).show();
    }
}

private void saveVerifiedToFirestore() {
    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    FirebaseFirestore.getInstance().collection("users").document(uid)
        .update("isVerified", true);
}
```

---

### 5.5 Person 1 — Viva Questions

**Q: What happens if the Firebase OTP SMS never arrives?**
A: The `onVerificationFailed` callback fires with a `FirebaseException`. The app shows a Toast message and re-enables the "Get OTP" button. There's a "Resend" option in `OtpVerifyActivity` that would re-trigger `PhoneAuthProvider.verifyPhoneNumber` (currently shows a Toast as placeholder).

**Q: Why use SharedPreferences for profile data instead of always reading from Firestore?**
A: SharedPreferences reads are synchronous and instant (in-memory). Firestore reads are asynchronous and require network. For frequently accessed data like the user's name, role, and verified status, local cache via `UserPrefs` gives near-instant UI rendering. The trade-off is potential staleness — if the user updates their profile on another device, local cache lags behind until a Firestore listener refreshes it.

**Q: What is the difference between `editor.apply()` and `editor.commit()`?**
A: `apply()` is asynchronous — it writes changes in the background and returns immediately. `commit()` is synchronous — it blocks the current thread until the write is done. For SharedPreferences, always use `apply()` on the Main Thread to avoid UI lag.

---

## 6. Person 2 — Seeker & Demand

### 6.1 What Person 2 Owns

Person 2 enables seekers to create service requests (posts) and manage incoming applications from providers.

**Screens owned:** `HomeSeekerActivity` → `PostOptionsActivity` → `CreatePostActivity` → `CreatePostStep2Activity` → `PostedSuccessfullyActivity` → `MyPostsActivity` → `ResponsesActivity` → `RequestApplyBottomSheet`

---

### 6.2 Post Creation — Two-Step Wizard

**Why two steps?** A gig post has many fields: title, description, category, urgency, images, date, time, location. Cramming all of these onto one screen overwhelms users. Splitting into two steps:
- **Step 1:** Core content (what do you need, which category, add a photo)
- **Step 2:** Logistics (when, where, how urgent, final submit)

**Category Prediction (`CategoryPredictor.java`):**
```java
public class CategoryPredictor {
    // Keyword map: each category → array of related words
    private static final Map<String, String[]> CATEGORY_KEYWORDS = new HashMap<>();
    static {
        CATEGORY_KEYWORDS.put("Cleaning",   new String[]{"clean", "wash", "sweep", "mop", "vacuum"});
        CATEGORY_KEYWORDS.put("Plumbing",   new String[]{"leak", "pipe", "tap", "sink", "drain", "toilet"});
        CATEGORY_KEYWORDS.put("Electrical", new String[]{"light", "fan", "bulb", "wire", "power", "switch"});
        CATEGORY_KEYWORDS.put("IT Help",    new String[]{"computer", "laptop", "wifi", "internet", "software"});
        CATEGORY_KEYWORDS.put("Gardening",  new String[]{"plant", "grass", "mow", "trim", "tree", "lawn"});
        CATEGORY_KEYWORDS.put("Delivery",   new String[]{"pick", "drop", "package", "food", "courier", "fetch"});
    }

    public static String predict(String text) {
        if (text == null || text.trim().isEmpty()) return "";

        String lower = text.toLowerCase(); // Normalize — make all text lowercase
        String bestCategory = "";
        int maxScore = 0;

        for (Map.Entry<String, String[]> entry : CATEGORY_KEYWORDS.entrySet()) {
            int score = 0;
            for (String keyword : entry.getValue()) {
                if (lower.contains(keyword)) score++; // Count keyword matches
            }
            if (score > maxScore) {
                maxScore = score;
                bestCategory = entry.getKey(); // Category with most keyword matches wins
            }
        }

        return bestCategory; // Could be "" if no keywords matched
    }
}
```

**Example:** User types "My tap is leaking and the drain is clogged."
- Plumbing keywords matched: "tap", "leak", "drain" → score = 3
- All other categories: score = 0
- Prediction: "Plumbing" ✓

**Technical term for this approach:** "Keyword frequency-based text classification" — the simplest form of NLP (Natural Language Processing). Not as sophisticated as neural networks but fast, deterministic, and easy to maintain.

---

### 6.3 Application Submission and Accept/Reject

**When a Provider applies to a Post:**

```
Provider taps "Apply" 
    → RequestApplyBottomSheet slides up
    → Provider fills message + budget + payment preference
    → ApplicationViewModel.submitApplication(postId, applicantId, message, budget)
    → ApplicationRepository writes to Firestore "applications" collection
    → Document: {postId, applicantId, message, budget, status: "pending"}
```

**When a Seeker accepts an application:**

```
Seeker opens ResponsesActivity (sees all applicants for their post)
    → Taps "Accept" on a provider card
    → confirmAcceptance(application) is called
    → ApplicationViewModel.updateApplicationStatus(appId, "accepted")
    → BookingViewModel.createBookingFromApplication(application, post)
    → BookingRepository writes to "bookings" collection
    → UI routes to next screen
```

**This is the critical handoff between Person 2 and Person 4:**
Accepting an application creates a Booking — and from that point, Person 4's booking lifecycle takes over.

---

### 6.4 Real-time Listeners for the Seeker Dashboard

```java
// In HomeSeekerActivity
postViewModel.getUserPosts().observe(this, posts -> {
    // Filter into GIG vs COMMUNITY posts
    List<Post> gigs = new ArrayList<>();
    List<Post> communityPosts = new ArrayList<>();
    for (Post post : posts) {
        if ("GIG".equals(post.getType())) gigs.add(post);
        else communityPosts.add(post);
    }
    gigAdapter.setPosts(gigs);
    communityAdapter.setPosts(communityPosts);
});

postViewModel.observeUserPosts(currentUserId); // Start listening
```

**Important:** `observe(this, ...)` passes `this` (the Activity) as the `LifecycleOwner`. LiveData automatically stops delivering updates if the Activity is destroyed, preventing `NullPointerException` crashes.

---

### 6.5 Person 2 — Viva Questions

**Q: What is the difference between a GIG post and a COMMUNITY post?**
A: A GIG post is a paid service request (e.g., "Need an electrician, willing to pay ₹500"). A COMMUNITY post is a volunteer/help request (e.g., "Need someone to help carry groceries for an elderly neighbor"). Both are stored in the same `posts` Firestore collection with a `type` field (`"GIG"` or `"COMMUNITY"`) that distinguishes them.

**Q: How does the submit button become enabled only when all required fields are filled?**
A: Using a method like `updateNextButtonState()` that checks if all required `EditText` fields are non-empty. It's called inside a `TextWatcher` attached to each input field. `TextWatcher.onTextChanged()` fires every time the user types a character, re-evaluating whether all fields pass validation.

**Q: Why use a BottomSheet for the apply action instead of a full screen?**
A: Context preservation. The provider is looking at a post's detail screen. Opening a full new screen to apply would require navigating away. A BottomSheet overlays the current screen, keeping the post details visible while the user fills in apply information. This is a UX best practice for quick, single-task interactions.

---

## 7. Person 3 — Provider & Market Intelligence

### 7.1 What Person 3 Owns

Person 3 enables providers to **discover** nearby opportunities through lists and maps, and manages location intelligence.

**Screens owned:** `HomeProviderActivity` → `MapsActivity` → `MapsFragment` → `GigPostDetailActivity` → `MyEarningsActivity` → `CalendarProviderActivity`

**Helpers owned:** `LocationHelper`, `LocationPickerHelper`, `GeocodingHelper`

---

### 7.2 MapLibre — The Map Library

**What is MapLibre?** An open-source fork of Mapbox GL that renders interactive maps. Unlike Google Maps (which requires paid API keys at scale), MapLibre uses OpenStreetMap tiles and is free.

**Key map lifecycle methods in `MapsFragment.java`:**

```java
// MapsFragment implements OnMapReadyCallback — it promises to implement onMapReady()
public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private MapView mapView;           // The visual map component
    private MapLibreMap mapLibreMap;  // The controller (move camera, add markers, etc.)

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedState); // Important — map has its own lifecycle
        mapView.getMapAsync(this);   // "Call onMapReady() when map is ready"
        return view;
    }

    @Override
    public void onMapReady(@NonNull MapLibreMap map) {
        // The map is fully loaded — safe to interact with it now
        mapLibreMap = map;
        mapLibreMap.setStyle("https://demotiles.maplibre.org/style.json", style -> {
            // Style loaded — add markers, enable location, etc.
            enableLocationComponent(style);
            if (RoleManager.isProvider(getContext())) {
                observeRealTimeDataForProvider(); // Load provider-specific posts
            } else {
                observeRealTimeDataForSeeker();
            }
        });
    }

    // Fragment lifecycle must be forwarded to MapView — very important!
    @Override public void onStart()   { super.onStart();   mapView.onStart(); }
    @Override public void onResume()  { super.onResume();  mapView.onResume(); }
    @Override public void onPause()   { super.onPause();   mapView.onPause(); }
    @Override public void onStop()    { super.onStop();    mapView.onStop(); }
    @Override public void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
}
```

**Adding a marker to the map:**
```java
MarkerOptions markerOptions = new MarkerOptions()
    .position(new LatLng(post.getLat(), post.getLng())) // Latitude, Longitude
    .title(post.getTitle())   // Popup title when tapped
    .snippet(post.getCategory()); // Small text under title

mapLibreMap.addMarker(markerOptions);
```

---

### 7.3 Geocoding — Converting Between Address and Coordinates

**Technical terms:**
- **Geocoding** — Converting a human-readable address (text) into latitude/longitude coordinates
- **Reverse Geocoding** — Converting latitude/longitude coordinates back into a human-readable address
- **Latitude** — North-South position (-90 to +90). Mumbai is ~19.0° N
- **Longitude** — East-West position (-180 to +180). Mumbai is ~72.8° E
- **Photon** — Open-source geocoding API built on OpenStreetMap data (by Komoot)
- **Nominatim** — Another open-source geocoding service by OpenStreetMap Foundation

**Why use TWO geocoding APIs?**
Resilience + quality. If one API is down or has poor results for a specific location, the other might succeed. Results from both are merged and deduplicated.

**Full `GeocodingHelper.performSearch` explained line by line:**
```java
public static void performSearch(String query, OnGeocodingResultListener listener) {
    if (query.trim().length() < 3) {  // Don't search for very short queries
        listener.onResults(new ArrayList<>());
        return;
    }

    new Thread(() -> {  // Start background thread — network calls not allowed on Main Thread
        List<GeocodingResult> aggregated = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(2); // Wait for 2 API calls

        // ===== API Call 1: Photon =====
        String photonUrl = "https://photon.komoot.io/api/?q=" + query + "&limit=5";
        httpClient.newCall(new Request.Builder().url(photonUrl).build())
            .enqueue(new Callback() {
                @Override public void onFailure(Call c, IOException e) { latch.countDown(); }
                @Override public void onResponse(Call c, Response r) {
                    // r.body().string() → Raw JSON string from API
                    // new JSONObject(...) → Parse JSON string into object
                    JSONObject root = new JSONObject(r.body().string());
                    JSONArray features = root.getJSONArray("features"); // Array of locations
                    for (int i = 0; i < features.length(); i++) {
                        JSONObject feat = features.getJSONObject(i);
                        JSONObject props = feat.getJSONObject("properties");
                        JSONObject geom  = feat.getJSONObject("geometry");
                        JSONArray coords = geom.getJSONArray("coordinates"); // [longitude, latitude]

                        // Note: Photon uses [lng, lat] order — counterintuitive!
                        aggregated.add(new GeocodingResult(
                            props.optString("name", ""),
                            props.optString("city", ""),
                            coords.getDouble(1),  // lat = index 1
                            coords.getDouble(0)   // lng = index 0
                        ));
                    }
                    latch.countDown(); // Signal Photon is done
                }
            });

        // ===== API Call 2: Nominatim =====
        String nominatimUrl = "https://nominatim.openstreetmap.org/search?q=" + query + "&format=json&limit=5";
        httpClient.newCall(new Request.Builder().url(nominatimUrl)
            .header("User-Agent", "NearNeedApp") // Nominatim requires a User-Agent header
            .build())
            .enqueue(new Callback() {
                @Override public void onFailure(Call c, IOException e) { latch.countDown(); }
                @Override public void onResponse(Call c, Response r) {
                    JSONArray arr = new JSONArray(r.body().string());
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.getJSONObject(i);
                        // display_name = "Bandra, Mumbai, Maharashtra, India"
                        String[] parts = o.optString("display_name", "").split(",", 2);
                        aggregated.add(new GeocodingResult(
                            parts[0].trim(),
                            parts.length > 1 ? parts[1].trim() : "",
                            o.getDouble("lat"), // Nominatim: "lat" and "lon" keys
                            o.getDouble("lon")
                        ));
                    }
                    latch.countDown(); // Signal Nominatim is done
                }
            });

        latch.await(4, TimeUnit.SECONDS); // Wait max 4 seconds for both

        // ===== Deduplication =====
        // Remove results that are within 500 meters of each other
        // (Both APIs might return the same location)
        List<GeocodingResult> finalList = new ArrayList<>();
        for (GeocodingResult res : aggregated) {
            boolean isDuplicate = false;
            for (GeocodingResult existing : finalList) {
                float[] distanceResult = new float[1];
                // Android's built-in distance calculation between two lat/lng points
                android.location.Location.distanceBetween(
                    res.lat, res.lng, existing.lat, existing.lng, distanceResult
                );
                if (distanceResult[0] < 500) { // Within 500 meters = duplicate
                    isDuplicate = true;
                    break;
                }
            }
            if (!isDuplicate && finalList.size() < 6) finalList.add(res);
        }

        // Post results back to Main Thread
        new Handler(Looper.getMainLooper()).post(() -> listener.onResults(finalList));

    }).start(); // Start the background thread
}
```

---

### 7.4 Nearby Post Filtering — Distance Calculation

**Technical:** `PostRepository.observeNearbyPosts` fetches all posts from Firestore and filters client-side using `android.location.Location.distanceBetween()` to compute the straight-line (Euclidean approximation) distance between the user's coordinates and each post's coordinates.

**Layman:** Firestore can't do "within 5 km" queries natively (without geohash indexing). So we fetch all posts, then in Java we calculate: "How far is this post from the user? If it's within the radius, include it."

**Distance formula used:**
```java
float[] results = new float[1];
android.location.Location.distanceBetween(
    userLat, userLng,    // User's coordinates
    postLat, postLng,    // Post's coordinates
    results              // result[0] = distance in meters
);
if (results[0] / 1000.0 <= radiusKm) { // Convert meters to km
    nearbyPosts.add(post); // Include in the filtered list
}
```

**What is the Haversine formula?** The mathematically correct way to compute great-circle distance between two points on a sphere (the Earth). `Location.distanceBetween` uses this internally.

---

### 7.5 GPS Location — FusedLocationProviderClient

**What is FusedLocationProvider?** An API that combines GPS, Wi-Fi, and cell tower data to provide the most accurate location with the least battery drain.

**Why "Fused"?** GPS alone drains battery fast. Wi-Fi positioning is less accurate but battery-friendly. The "Fusion" automatically picks the best source depending on context (indoors vs. outdoors, charging vs. battery saving).

```java
// In LocationHelper.java
FusedLocationProviderClient fusedClient = LocationServices.getFusedLocationProviderClient(context);
fusedClient.getLastLocation().addOnSuccessListener(location -> {
    if (location != null) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        callback.onLocationReceived(lat, lng); // Pass coordinates to caller
    }
});
```

---

### 7.6 Person 3 — Viva Questions

**Q: Why use two geocoding APIs instead of one?**
A: Reliability and result quality. If Photon's servers are down, Nominatim still provides results. Also, different APIs have different strengths — Photon is better for POIs (points of interest), Nominatim for administrative addresses. Merging and deduplicating both gives the best result set.

**Q: How do you prevent duplicate location results from the two APIs?**
A: The deduplication loop in `GeocodingHelper` computes the distance between every candidate result and all already-accepted results using `Location.distanceBetween()`. If two results are within 500 meters of each other, the second one is considered a duplicate and discarded.

**Q: Why does the map's fragment lifecycle need to be manually forwarded?**
A: MapLibre's `MapView` has its own internal resource management tied to Android lifecycle. If you don't forward `onStart`, `onResume`, `onPause`, `onStop`, `onDestroy` to `mapView`, the map's GPU resources are not properly managed — causing memory leaks, rendering glitches, or crashes.

---

## 8. Person 4 — Engagement & Real-time Systems

### 8.1 What Person 4 Owns

After a match is made (application accepted → booking created), Person 4 takes over. They own real-time chat, booking lifecycle, status transitions, push notifications, and reputation (ratings).

**Screens owned:** `MessagesFragment` → `ChatActivity` → `BookingsActivity` → `UpdateStatusActivity` → `RatingDialog` → `ReviewsActivity` → `NearNeedMessagingService` → `NotificationCenter`

---

### 8.2 Real-time Chat — Deep Dive

**Firestore structure for chat:**
```
messages/            ← Collection
  {chatId}/          ← Document (one per conversation pair)
    messages/        ← Sub-collection
      {msgId1}/      ← Individual message document
        senderId: "uid1"
        messageText: "Hello!"
        timestamp: 1714220000
      {msgId2}/
        senderId: "uid2"
        audioPath: "https://storage.../voice.mp3"
        timestamp: 1714220010

chats/               ← Collection (inbox metadata)
  {chatId}/          ← Document
    participants: ["uid1", "uid2"]
    lastMessage: "Hello!"
    lastTimestamp: 1714220000
```

**Why separate `chats` and `messages` collections?**
- `chats` is for the inbox list — you only need sender, last message preview, and time. Lightweight.
- `messages/{chatId}/messages` stores the full conversation. Loaded only when a specific chat is opened.
- This avoids loading hundreds of messages just to show the inbox.

**Real-time message listener (from `ChatRepository.java`):**
```java
public static ListenerRegistration observeMessages(String chatId, MessageListener listener) {
    return FirebaseFirestore.getInstance()
        .collection("messages")     // Go to messages collection
        .document(chatId)           // Specific chat between two users
        .collection("messages")     // Sub-collection of messages
        .orderBy("timestamp", Query.Direction.ASCENDING) // Oldest first (chat order)
        .addSnapshotListener((snapshot, error) -> {
            if (error != null) { listener.onError(error); return; }
            if (snapshot != null) {
                // snapshot.toObjects() — Firestore maps each document field to Java object fields
                List<ChatMessage> messages = snapshot.toObjects(ChatMessage.class);
                listener.onMessagesLoaded(messages);
            }
        }); // Returns a ListenerRegistration — call .remove() to stop listening
}
```

**Sending a text message:**
```java
public static void sendMessage(String chatId, String senderId, String receiverId, String text, SaveCallback cb) {
    Map<String, Object> messageMap = new HashMap<>();
    messageMap.put("senderId", senderId);
    messageMap.put("messageText", text.trim());
    messageMap.put("timestamp", System.currentTimeMillis()); // Unix timestamp in milliseconds

    FirebaseFirestore.getInstance()
        .collection("messages").document(chatId).collection("messages")
        .add(messageMap)  // .add() auto-generates a document ID
        .addOnSuccessListener(doc -> {
            updateChatMetadata(chatId, senderId, receiverId, text.trim()); // Update inbox preview
            if (cb != null) cb.onSuccess();
        });
}
```

**Sending a media message (image/voice):**
```java
public static void sendMediaMessage(String chatId, String senderId, String receiverId,
                                    String mediaUrl, boolean isVoice, SaveCallback cb) {
    Map<String, Object> messageMap = new HashMap<>();
    messageMap.put("senderId", senderId);
    messageMap.put("messageText", ""); // No text for media messages
    // Differentiate: voice stores in "audioPath", image stores in "imageUri"
    messageMap.put(isVoice ? "audioPath" : "imageUri", mediaUrl);
    messageMap.put("timestamp", System.currentTimeMillis());

    // Same Firestore write as text message
    FirebaseFirestore.getInstance()
        .collection("messages").document(chatId).collection("messages")
        .add(messageMap)
        .addOnSuccessListener(doc -> {
            // Update inbox preview with emoji placeholder
            updateChatMetadata(chatId, senderId, receiverId,
                isVoice ? "🎤 Voice message" : "📷 Image");
            if (cb != null) cb.onSuccess();
        });
}
```

---

### 8.3 Booking Lifecycle — State Machine

**Technical:** A state machine defines a finite set of states and the legal transitions between them. The `BookingStateManager` and `UpdateStatusActivity` implement a state machine for booking progression.

**Layman:** Think of a traffic light. It can only be Red, Yellow, or Green. It transitions Red→Green, Green→Yellow, Yellow→Red. It cannot skip from Red to Yellow directly. A booking works the same way.

**Booking states:**
```
PENDING → ONGOING → COMPLETED
    ↓          ↓
CANCELLED  CANCELLED
```

**`BookingStateManager.java` — In-memory state mirror:**
```java
public class BookingStateManager {
    public static final String STATUS_PENDING   = "pending";
    public static final String STATUS_ONGOING   = "ongoing";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_CANCELLED = "cancelled";

    private static BookingStateManager instance; // Singleton

    // Map from bookingId → current status string
    private final Map<String, String> bookingStatuses = new HashMap<>();

    private BookingStateManager() {}

    public static BookingStateManager getInstance() {
        if (instance == null) instance = new BookingStateManager();
        return instance;
    }

    public void setStatus(String bookingId, String status) {
        bookingStatuses.put(bookingId, status);
    }

    public String getStatus(String bookingId) {
        return bookingStatuses.getOrDefault(bookingId, STATUS_ONGOING);
    }

    public boolean isCompleted(String bookingId) {
        return STATUS_COMPLETED.equals(bookingStatuses.get(bookingId));
    }
}
```

**Why have an in-memory state in addition to Firestore?**
Firestore writes are async — there's a short delay before the change propagates. The in-memory mirror allows the UI to update instantly (responsive feel) while Firestore persists the change in the background. If the app restarts, the state is re-loaded from Firestore.

---

### 8.4 Notification Center — In-App Notifications

**Two types of notifications in NearNeed:**

| Type | Mechanism | When delivered | Example |
|---|---|---|---|
| In-app | Firestore `Users/{uid}/notifications` sub-collection | App is open | "Your application was accepted" |
| Push | FCM via `NearNeedMessagingService` | App backgrounded or killed | "New message from Reia" |

**Full `NotificationCenter.java` — how it works:**
```java
// Send a notification to the current user
public static void addNotification(String title, String message) {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    if (user == null) return;
    sendNotificationToUser(user.getUid(), title, message);
}

// Write notification document to Firestore
public static void sendNotificationToUser(String userId, String title, String message) {
    Map<String, Object> data = new HashMap<>();
    data.put("id", UUID.randomUUID().toString()); // Unique ID for this notification
    data.put("title", title);
    data.put("message", message);
    data.put("timestamp", System.currentTimeMillis());
    data.put("read", false); // Unread by default

    FirebaseFirestore.getInstance()
        .collection("Users").document(userId)
        .collection("notifications")
        .document(data.get("id").toString()) // Use the UUID as document ID
        .set(data);
}

// Live badge — count unread notifications in real-time
public static ListenerRegistration listenUnreadCount(OnCountChanged callback) {
    CollectionReference ref = notifRef(); // Users/{uid}/notifications
    return ref.whereEqualTo("read", false)  // Filter: only unread docs
        .addSnapshotListener((snap, err) -> {
            if (snap != null) callback.onChange(snap.size()); // snap.size() = count of unread
        });
}

// Mark one notification as read
public static void markAsRead(String notificationId) {
    notifRef().document(notificationId).update("read", true);
}

// Delete all notifications (uses a WriteBatch for efficiency)
public static void clearAll() {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    notifRef().get().addOnSuccessListener(snap -> {
        WriteBatch batch = db.batch(); // Group multiple deletes into one atomic operation
        for (DocumentSnapshot doc : snap.getDocuments()) {
            batch.delete(doc.getReference());
        }
        batch.commit(); // Execute all deletes at once
    });
}
```

**What is a `WriteBatch`?** A way to group multiple Firestore write operations into a single network request. Either all succeed or all fail (atomic). Also more efficient than individual writes.

**What is `UUID.randomUUID()`?** Universally Unique Identifier. A 128-bit number that is statistically guaranteed to be unique across all systems and times. Example: `"f47ac10b-58cc-4372-a567-0e02b2c3d479"`.

---

### 8.5 Ratings and Firestore Transactions

**The rating problem:** Two users submit ratings at the exact same moment. Both read the current average (3.5 stars), both add their star count, both write back. One write overwrites the other — the final result is wrong. This is called a **race condition**.

**Solution: Firestore Transaction**

```java
// In RatingDialog.java
private void updateUserRating(String revieweeId, float newRating) {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference userRef = db.collection("users").document(revieweeId);

    db.runTransaction(transaction -> {
        // 1. READ the current document (inside the transaction, so it's locked)
        DocumentSnapshot snapshot = transaction.get(userRef);
        
        float currentAvg   = snapshot.getDouble("averageRating") != null
                             ? snapshot.getDouble("averageRating").floatValue() : 0f;
        long   reviewCount = snapshot.getLong("reviewCount") != null
                             ? snapshot.getLong("reviewCount") : 0L;
        
        // 2. CALCULATE the new average
        long   newCount    = reviewCount + 1;
        float  newAverage  = ((currentAvg * reviewCount) + newRating) / newCount;
        
        // 3. WRITE back — this fails if the document was modified since step 1
        // Firestore retries the transaction automatically if there's a conflict
        transaction.update(userRef, "averageRating", newAverage);
        transaction.update(userRef, "reviewCount",   newCount);
        return null;
    })
    .addOnSuccessListener(result -> Toast.makeText(this, "Review submitted!", Toast.LENGTH_SHORT).show())
    .addOnFailureListener(e -> Toast.makeText(this, "Failed to submit review.", Toast.LENGTH_SHORT).show());
}
```

**Why this prevents the race condition:** The transaction's READ operation takes a snapshot. If the document changes between the READ and WRITE, Firestore detects the conflict and automatically **retries** the entire transaction with the new data. Only when a full cycle (read → compute → write) completes without interference does it commit.

---

### 8.6 Person 4 — Viva Questions

**Q: What is the difference between in-app notifications and push notifications in NearNeed?**
A: In-app notifications are Firestore documents written under `Users/{uid}/notifications`. They're delivered by a real-time Firestore listener when the app is open. Push notifications are FCM messages handled by `NearNeedMessagingService.onMessageReceived()` — these work even when the app is closed. NearNeed uses both: in-app for active users, FCM for background alerts.

**Q: What is a Firestore snapshot listener and why must you remove it?**
A: A snapshot listener (`addSnapshotListener`) keeps a persistent WebSocket connection to Firestore and fires a callback every time the subscribed data changes. If you don't remove the listener when the Activity is destroyed (via `listenerRegistration.remove()`), it continues to consume network resources and may attempt to update a destroyed UI — causing memory leaks and crashes.

**Q: How is the booking lifecycle managed between Seeker and Provider views?**
A: Both seeker and provider have separate fragments (`SeekerUpcomingFragment`, `ProviderUpcomingFragment`) that observe the same `BookingRepository` but with different user ID filters. The `UpdateStatusActivity` handles transitions (pending → ongoing → completed/cancelled). `BookingStateManager` provides an in-memory mirror for instant UI updates, while `BookingRepository.updateBookingStatus()` persists to Firestore.

**Q: What is a WriteBatch and when is it used?**
A: A `WriteBatch` groups multiple Firestore write operations into a single atomic commit. Either all operations succeed or none do. It reduces network round-trips (one request instead of N) and prevents partial state (e.g., some notifications deleted, others not). Used in `NotificationCenter.clearAll()`.

---

## 9. System Integration

### 9.1 End-to-End Application Flow

```
[Person 1] User signs up (OTP → Profile → ID Verification)
                ↓
           Role assigned (SEEKER or PROVIDER) via RoleManager
                ↓
[Person 2] Seeker creates a GIG post (CreatePost → Firestore "posts")
                ↓
[Person 3] Provider discovers post (HomeProviderActivity or MapsFragment)
                ↓
[Person 2] Provider applies (RequestApplyBottomSheet → Firestore "applications")
                ↓
[Person 2] Seeker accepts applicant (ResponsesActivity → BookingViewModel)
                ↓
[Person 4] Booking created (Firestore "bookings", status: "pending")
                ↓
[Person 4] Parties chat (ChatActivity → ChatRepository → Firestore "messages")
                ↓
[Person 4] Status updates: pending → ongoing → completed (UpdateStatusActivity)
                ↓
[Person 4] Payment flow (PaymentFlowActivity → PaymentSuccessActivity)
                ↓
[Person 4] Rating submitted (RatingDialog → Firestore "reviews" + user aggregate)
                ↓
[Person 1] User's reputation updated (averageRating field in Firestore "users")
```

### 9.2 Cross-Module Firebase Collection Dependencies

| Collection | Written by | Read by |
|---|---|---|
| `users` | Person 1 (profile, OTP, verification) | Everyone (to display name, photo, verified badge) |
| `posts` | Person 2 (create post) | Person 2 (my posts), Person 3 (discovery), Person 4 (booking context) |
| `applications` | Person 2 (provider applies) | Person 2 (seeker reviews), Person 4 (create booking) |
| `bookings` | Person 4 (from accepted application) | Person 4 (all booking fragments), Person 2 (seeker sees status) |
| `messages/{chatId}/messages` | Person 4 (send message) | Person 4 (ChatActivity listener) |
| `chats` | Person 4 (metadata update) | Person 4 (MessagesFragment inbox) |
| `reviews` | Person 4 (rating submitted) | Person 1 (profile reputation display) |
| `Users/{uid}/notifications` | Person 4 (NotificationCenter) | Person 4 (badge + popup) |

---

### 9.3 Listener Cleanup — Why It Matters

Every real-time listener opens a network connection. If not removed, it keeps running forever, consuming battery, data, and memory.

**Best practice in NearNeed:**
```java
private ListenerRegistration postsListener;

@Override
protected void onStart() {
    super.onStart();
    // Start listening when screen is visible
    postsListener = PostRepository.observeAllActivePosts(listener);
}

@Override
protected void onStop() {
    super.onStop();
    // Stop listening when screen goes to background
    if (postsListener != null) {
        postsListener.remove();
        postsListener = null;
    }
}
```

**ViewModel's `onCleared()`** is also used for cleanup. Called when the ViewModel is destroyed (user leaves the screen permanently):
```java
@Override
protected void onCleared() {
    super.onCleared();
    if (userPostsListener != null) {
        userPostsListener.remove(); // Remove Firestore listener
        userPostsListener = null;
    }
}
```

---

## 10. AI Chat Feature

### 10.1 What is AiChatActivity?

NearNeed includes an AI-powered assistant that uses the **Gemini API** (Google's multimodal large language model). Users can ask questions in natural language and optionally send images.

**What is an LLM (Large Language Model)?** A neural network trained on massive text data that can understand and generate human-like text. Gemini is Google's equivalent of OpenAI's GPT.

**Technical flow:**

```
User types message (+ optional image)
    ↓
App encodes image as Base64 string (if image selected)
    ↓
OkHttp POST request to Gemini REST API
    ↓
Request body: JSON with "contents" array (text + base64 image)
    ↓
Gemini API responds with generated text
    ↓
Response parsed from JSON
    ↓
RecyclerView updated with AI response bubble
```

**What is Base64?** A way to encode binary data (like an image) as plain text characters. Since JSON only supports text, images must be Base64-encoded to be included in API request bodies.

```java
// Converting an image Bitmap to Base64 string
ByteArrayOutputStream baos = new ByteArrayOutputStream();
bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos); // Compress image
byte[] imageBytes = baos.toByteArray();
String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
```

**Previous conversation history:** The AI chat loads past messages from Firestore and includes them in the API request so the AI remembers context (what was said earlier in the conversation).

---

## 11. Viva Q&A

### 11.1 Architecture & Design Questions

**Q1: What architecture does NearNeed use?**
> MVVM with the Repository pattern. The View layer (Activity/Fragment) observes LiveData from ViewModels. ViewModels call Repository methods. Repositories interact with Firebase (Firestore, Storage, Auth) and external APIs. This provides: (1) separation of concerns, (2) lifecycle safety via LiveData, (3) testability of business logic isolated from UI.

**Q2: Why MVVM over MVC or MVP?**
> In MVC, the Controller (Activity) becomes bloated with both UI and data logic. MVP uses interfaces but still requires Activities to implement them. MVVM with LiveData provides automatic lifecycle awareness — no need to manually unsubscribe. The ViewModel also survives screen rotation, preventing redundant data fetches.

**Q3: What is the difference between `MutableLiveData` and `LiveData`?**
> `MutableLiveData` allows both reading and writing (`setValue`/`postValue`). `LiveData` is read-only. The convention in MVVM: ViewModel exposes only `LiveData` publicly (so Activities can't accidentally modify it), but internally stores `MutableLiveData` to update it. Example in `PostViewModel`: `private MutableLiveData<List<Post>> userPosts` (writable internally) with `public LiveData<List<Post>> getUserPosts()` (read-only externally).

**Q4: What is `postValue` vs `setValue`?**
> `setValue()` must be called on the **Main Thread**. `postValue()` can be called from any thread (it posts the update to the Main Thread internally). In NearNeed's Firestore callbacks (which run on Main Thread), `setValue()` is used. In background thread geocoding results, `Handler(Looper.getMainLooper()).post()` is used instead of `postValue` for manual UI updates.

---

### 11.2 Firebase Questions

**Q5: What is the difference between Firestore and Firebase Realtime Database?**
> Firestore: Document-collection model, richer queries, better scalability, nested sub-collections, offline support. Realtime Database: One giant JSON tree, simpler but harder to query. NearNeed uses Firestore for its structured documents and real-time listeners.

**Q6: How does NearNeed handle Firestore listener memory leaks?**
> `ListenerRegistration` objects (returned by `addSnapshotListener`) are stored as fields. They are removed in `onStop()` (Activity/Fragment) or `onCleared()` (ViewModel). `PostViewModel.onCleared()` removes both `userPostsListener` and `nearbyPostsListener`. This prevents the listener from pushing updates to a destroyed Activity.

**Q7: What is a Firestore Transaction and when is it necessary?**
> A transaction is an atomic read-modify-write operation. Necessary when a value depends on its current state (like a counter or average). In `RatingDialog`, the new average rating = `(currentAvg * count + newRating) / (count + 1)` — this requires reading `currentAvg` and `count` first. Without a transaction, two concurrent rating submissions could both read the old value and produce an incorrect result.

**Q8: Why are there two differently-named collections: `users` (lowercase) and `Users` (uppercase)?**
> This is an inconsistency in the project. `users` (lowercase) stores core profile data. `Users` (uppercase) stores FCM tokens and notification sub-collections. In a production system, these would be unified. During viva: acknowledge the inconsistency, explain what each stores, and note it should be standardized in a future refactor.

**Q9: What is `SetOptions.merge()` in `updateChatMetadata()`?**
> `set(data, SetOptions.merge())` updates only the specified fields and leaves all other fields untouched. Without `merge()`, `set()` would overwrite the entire document with only the provided fields, deleting all other data. `update()` does the same thing but fails if the document doesn't exist. `set()` with `merge()` creates the document if it doesn't exist and updates if it does — the safest choice for chat metadata that might not exist yet.

---

### 11.3 Android Concepts Questions

**Q10: What is ANR and how does NearNeed prevent it?**
> ANR = Application Not Responding. Occurs when the Main Thread is blocked for > 5 seconds (8 seconds for broadcasts). NearNeed prevents ANR by running network operations (geocoding, OkHttp API calls, Firebase Storage uploads) on background threads. Firebase itself also runs all Firestore operations on background threads internally, posting results back to the Main Thread via callbacks.

**Q11: How does RecyclerView improve performance over ListView?**
> RecyclerView enforces the ViewHolder pattern (no `convertView == null` checks needed), recycles views more aggressively, supports multiple layout managers (Linear, Grid, StaggeredGrid), and has built-in item animations. `ListView` creates new View objects for each item if no recycled view is available and requires manual ViewHolder implementation.

**Q12: What is `ViewModelProvider` and how does it create/retrieve a ViewModel?**
> `ViewModelProvider` is a factory that creates ViewModels and stores them in a `ViewModelStore` associated with the Activity/Fragment. `new ViewModelProvider(this).get(PostViewModel.class)` — if a `PostViewModel` already exists for this Activity (e.g., after screen rotation), it returns the existing one. Otherwise, it creates a new instance.

**Q13: What happens when you call `finish()` in an Activity?**
> The Activity is removed from the back stack and destroyed (lifecycle: `onPause → onStop → onDestroy`). The user is taken back to the previous Activity in the stack. In NearNeed, `finish()` is called after navigation (e.g., in `OtpEnterActivity.onCodeSent()`) to prevent users pressing Back and returning to an already-submitted screen.

---

### 11.4 Maps & Location Questions

**Q14: What is `OnMapReadyCallback` and why is it needed?**
> MapLibre's `MapView` initializes asynchronously (loading map tiles, setting up the GL renderer). `mapView.getMapAsync(callback)` registers a callback that fires only when the map is fully ready. Attempting to interact with the map before it's ready (e.g., adding markers) would crash. `MapsFragment implements OnMapReadyCallback` and adds markers in `onMapReady()`.

**Q15: What is the difference between geocoding and reverse geocoding?**
> Geocoding: text address → lat/lng coordinates (user types "Bandra, Mumbai" → gets 19.0550, 72.8388). Used in `GeocodingHelper.performSearch()`. Reverse geocoding: lat/lng → text address (user's GPS gives 19.0550, 72.8388 → display "Bandra, Mumbai"). Used in `LocationPickerHelper` when "Use Current Location" is tapped.

---

### 11.5 Miscellaneous Concept Questions

**Q16: What is the Singleton pattern and where does NearNeed use it?**
> Singleton ensures a class has exactly one instance. `BookingStateManager.getInstance()` uses this pattern — the private constructor prevents external instantiation; `getInstance()` creates the instance on first call and returns the same instance on subsequent calls. This is important because `bookingStatuses` (the Map) must be a single shared source of truth.

**Q17: What is the `CountDownLatch` used for in `GeocodingHelper`?**
> A `CountDownLatch` is a synchronization primitive. `new CountDownLatch(2)` means "I need to wait for 2 things." Each API call (Photon and Nominatim) calls `latch.countDown()` when it finishes (whether success or failure). `latch.await(4, TimeUnit.SECONDS)` blocks the background thread until both calls complete OR 4 seconds pass, whichever comes first. This ensures results from both APIs are collected before deduplication.

**Q18: What is OkHttp and why is it used instead of Android's built-in `HttpURLConnection`?**
> OkHttp is a modern HTTP client library by Square. Compared to `HttpURLConnection`: OkHttp provides a cleaner API, automatic connection pooling (reuses TCP connections), transparent GZIP decompression, response caching, and a simple async `enqueue()` model. `GeocodingHelper` uses OkHttp's async `enqueue()` to make non-blocking network calls from a background thread.

**Q19: What is the Razorpay payment gateway?**
> Razorpay is an Indian payment gateway (like PayPal for India). It handles UPI, credit/debit cards, net banking, and wallets. In NearNeed, the Razorpay Android SDK is integrated in the payment flow. When a booking is completed, the seeker can pay the provider through Razorpay. Razorpay handles the secure transaction — NearNeed only receives a success/failure callback with a payment ID.

**Q20: What is ML Kit and how is it used?**
> ML Kit is Google's on-device machine learning SDK for Android/iOS. "On-device" means all processing happens locally — no data is sent to any server. In NearNeed's `IdVerificationActivity`, `TextRecognition.getClient()` creates an OCR processor. `recognizer.process(InputImage.fromFilePath(...))` extracts all visible text from the ID card image. The extracted text is then checked for government keywords and ID number patterns to determine verification confidence.

---

## 12. Scripts

### 12.1 Full App Script (2 Minutes)

NearNeed is a hyperlocal service marketplace and community platform for Android. The app uses MVVM architecture with Firebase as the backend.

A user starts by entering their phone number — Firebase Phone Auth sends an OTP via SMS. After verification, the user selects a role: Seeker (someone who needs help) or Provider (someone who offers services). The role is persisted in SharedPreferences via `RoleManager`. An optional ID verification step uses ML Kit OCR to extract text from uploaded government ID images.

On the seeker side: the user creates posts — either paid Gig requests or volunteer Community requests — through a two-step wizard. A text-based `CategoryPredictor` auto-suggests the category. Posts are saved to Firestore. Providers discover these posts on a dashboard or via an interactive MapLibre map with real-time markers. Providers apply through a BottomSheet dialog with message, budget, and payment preference.

After the seeker accepts an applicant, a booking is created. The parties chat via real-time Firestore listeners. The booking progresses through states (pending → ongoing → completed) managed by `UpdateStatusActivity` and `BookingStateManager`. Payment is processed through Razorpay. After completion, users rate each other — the rating is updated atomically using Firestore transactions.

Throughout, push notifications are delivered by Firebase Cloud Messaging, and in-app notifications are stored in a Firestore sub-collection with a real-time unread count badge.

---

### 12.2 Person 1 Script (2 Minutes)

My module is Identity and Trust. I own onboarding, authentication, profile setup, and ID verification.

Technically, the entry flow is `LoadingActivity → WelcomeActivity → OtpEnterActivity → OtpVerifyActivity`. I use Firebase Phone Auth — the user enters their phone, Firebase sends an OTP SMS, and my `OtpEnterActivity` uses `PhoneAuthProvider.verifyPhoneNumber()` with callbacks (`onCodeSent`, `onVerificationCompleted`, `onVerificationFailed`). In `OtpVerifyActivity`, I combine the verification ID with the entered code to create a `PhoneAuthCredential` and call `signInWithCredential`.

Post-auth, I check whether this UID already has a Firestore document in `users` — this determines whether they're a new or returning user. New users go through `ProfileInfoActivity` and optionally `IdVerificationActivity`. In ID verification, I use ML Kit's `TextRecognition` to run OCR on uploaded ID images, extract text, and check for government keywords and Aadhaar-style patterns.

Role is stored in SharedPreferences via `RoleManager`. Profile fields are cached in `UserPrefs` for instant reads. The canonical profile lives in Firestore `users/{uid}`. The main value my module provides: every downstream module can assume a verified, role-assigned, profile-complete user without worrying about auth edge cases.

---

### 12.3 Person 2 Script (2 Minutes)

My module is Seeker and Demand Management. I own post creation and applicant management.

The seeker's home screen (`HomeSeekerActivity`) uses two horizontal RecyclerViews — one for the user's own gig posts, one for community posts — both fed by `PostViewModel`'s LiveData. When a seeker wants to create a post, they go through `PostOptionsActivity` (type selection), `CreatePostActivity` (title, description, category with auto-prediction via `CategoryPredictor`, photo upload), and `CreatePostStep2Activity` (date, time, location via `LocationPickerHelper`, urgency). The final save uses `PostViewModel.createPost → PostRepository.createPost → Firestore "posts"`. On success, the user sees `PostedSuccessfullyActivity`.

Providers apply through `RequestApplyBottomSheet` — a BottomSheet dialog — which writes to Firestore `applications`. In `ResponsesActivity`, real-time listeners show all applicants for a post. When the seeker accepts, `ApplicationViewModel.updateApplicationStatus("accepted")` is called, followed by `BookingViewModel.createBookingFromApplication()` — this creates the booking document that Person 4 picks up.

The key design choices: two-step wizard reduces cognitive overload, BottomSheet apply pattern keeps context, and MVVM with real-time listeners keeps dashboards current without polling.

---

### 12.4 Person 3 Script (2 Minutes)

My module is Provider Discovery and Market Intelligence. I own the provider home screen, maps, location, and financial screens.

The provider's dashboard (`HomeProviderActivity`) observes `PostViewModel.observeAllActivePosts()` and splits incoming posts into GIG and COMMUNITY lists rendered by `NearbyRequestsAdapter` and `CommunityVolunteeringAdapter`. For geospatial discovery, `MapsFragment` implements `OnMapReadyCallback` — when the map is ready, it loads role-specific posts and places `MarkerOptions` at each post's lat/lng.

My most technical area is location intelligence. I built `GeocodingHelper.performSearch()`, which fires parallel HTTP requests to Photon (OpenStreetMap) and Nominatim using OkHttp's async `enqueue()`. A `CountDownLatch(2)` waits for both API calls to complete, then deduplication removes results within 500 meters of each other using `Location.distanceBetween()`. Results are posted to the Main Thread via `new Handler(Looper.getMainLooper()).post()`.

For GPS-based location, `LocationHelper` uses `FusedLocationProviderClient` — combining GPS, Wi-Fi, and cell data for optimal accuracy. Nearby post filtering uses client-side distance math: fetch all posts, compute distance from user coordinates to each post, include those within the configured radius.

---

### 12.5 Person 4 Script (2 Minutes)

My module is Engagement and Real-time Systems — everything after a match: chat, bookings, notifications, and ratings.

Real-time chat (`ChatActivity`) uses `ChatRepository.observeMessages(chatId)` — a Firestore snapshot listener on `messages/{chatId}/messages` ordered by timestamp. Every new message triggers the callback, updates the adapter, and scrolls to the latest message. Text sends go to `ChatRepository.sendMessage()`; media (images/voice) go to `StorageRepository.uploadImage/uploadAudio()` first, then `ChatRepository.sendMediaMessage()` saves the download URL. After sending, `updateChatMetadata()` updates the `chats` document for the inbox preview.

Booking lifecycle uses `BookingStateManager` (Singleton, in-memory) as an immediate state mirror, and `BookingRepository.updateBookingStatus()` for Firestore persistence. `UpdateStatusActivity` provides the UI for state transitions: pending → ongoing → completed or cancelled.

For notifications: `NotificationCenter.addNotification()` writes to `Users/{uid}/notifications`. `listenUnreadCount()` provides a live badge count. For push: `NearNeedMessagingService.onMessageReceived()` handles FCM payloads and builds Android notifications via `NotificationCompat.Builder`.

Ratings use Firestore transactions in `RatingDialog.updateUserRating()` — atomic read-modify-write prevents race conditions when multiple reviews are submitted concurrently.

---

## 13. Final Revision Checklist

Use this the night before your viva. For each item, give yourself a score (0 = no idea, 1 = partial, 2 = confident):

### Android Fundamentals
- [ ] Can you explain the Activity lifecycle (all 6 methods) with an example from NearNeed?
- [ ] Can you explain the difference between Activity and Fragment?
- [ ] Can you explain what an Intent is and show the OTP navigation code?
- [ ] Can you explain `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK` and why it's used?
- [ ] Can you explain RecyclerView + Adapter with a real example from the app?
- [ ] Can you explain SharedPreferences with the `RoleManager` code?
- [ ] Can you explain what a BottomSheet is and name 3 places it's used?

### Architecture
- [ ] Can you draw the MVVM flow for post creation (Activity → ViewModel → Repository → Firestore)?
- [ ] Can you explain what LiveData is and why it's lifecycle-aware?
- [ ] Can you explain the difference between `MutableLiveData` and `LiveData`?
- [ ] Can you explain what `onCleared()` does in a ViewModel?

### Firebase
- [ ] Can you explain the Firestore collection structure (all main collections)?
- [ ] Can you explain real-time listeners vs. one-time `.get()` and when to use each?
- [ ] Can you explain what a Firestore transaction is and why `RatingDialog` needs one?
- [ ] Can you explain what FCM is and trace the notification flow?
- [ ] Can you explain what `SetOptions.merge()` does?

### Person-Specific
- [ ] Can you trace the complete OTP auth flow from `OtpEnterActivity` to `ProfileInfoActivity`?
- [ ] Can you explain how `CategoryPredictor.predict()` works (keyword scoring)?
- [ ] Can you explain why `GeocodingHelper` uses TWO APIs and a `CountDownLatch`?
- [ ] Can you explain what `BookingStateManager` is and why an in-memory mirror is needed?
- [ ] Can you explain how `NotificationCenter.listenUnreadCount()` keeps the badge live?

### Tricky Questions
- [ ] What are the trade-offs of client-side nearby filtering vs. server-side geospatial queries?
- [ ] Why might SharedPreferences data be stale? How would you fix it?
- [ ] What is a race condition in ratings and how does a Firestore transaction prevent it?
- [ ] What is an ANR and how does background threading in `GeocodingHelper` prevent it?
- [ ] Why does `MapsFragment` manually forward lifecycle events to `MapView`?

---

*Document generated on 2026-04-27. All code snippets are directly from the NearNeed_App source files.*
