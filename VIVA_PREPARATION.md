# 🎓 NearNeed App - Viva Preparation Guide
## Person 1: Identity & Trust Architect

**Focus Areas:** Authentication → Profile Construction → Security → Real-time Synchronization

---

## 📋 Table of Contents
1. [Entry Flow](#entry-flow)
2. [Authentication Flow](#authentication-flow)
3. [Onboarding Flow](#onboarding-flow)
4. [Profile Hub](#profile-hub)
5. [Support Flow](#support-flow)
6. [Helper Utilities & Models](#helper-utilities--models)
7. [Key Viva Concepts](#key-viva-concepts)
8. [Database Schema](#database-schema)

---

# ENTRY FLOW

## 1️⃣ LoadingActivity.java

### **Purpose**
Acts as a splash screen with animated progress bar and loading messages. Bridges app startup with destination activity based on Firebase authentication state or explicit navigation.

### **Key Concepts**

#### **What?**
- Displays animated logo with pop-in effect
- Shows progress bar with status messages
- Auto-transitions to target activity after completion
- Customizable duration and messages via Intent extras

#### **Why?**
- **Visual Branding:** Gives users time to perceive the app icon during startup
- **App Initialization:** Allows background tasks (Firebase init, permissions check) to complete
- **User Experience:** Smooth transition instead of instant splash-to-home jump

#### **How?**

```java
// Key initialization
progressBar = findViewById(R.id.loadingProgress);
tvStatus = findViewById(R.id.tvStatus);

// Extract navigation parameters from Intent
String targetClassNameRaw = getIntent().getStringExtra(EXTRA_TARGET_CLASS);
statusMessages = getIntent().getStringArrayExtra(EXTRA_STATUS_MESSAGES);
long durationMs = getIntent().getLongExtra(EXTRA_DURATION_MS, 3000L);
```

**Progress Simulation:**
```java
new Thread(() -> {
    while (progressStatus < 100) {
        progressStatus += 1;
        handler.post(() -> {
            progressBar.setProgress(progressStatus);
            updateStatusText(progressStatus);  // Updates status message
        });
        Thread.sleep(stepDelay);  // stepDelay = durationMs / 100
    }
    // After completion, transition to target
    handler.postDelayed(() -> {
        String finalTarget = (mTargetClassName != null) ? 
            mTargetClassName : WelcomeActivity.class.getName();
        // Launch target activity
    }, 0);
}).start();
```

### **Animation Details**

**Logo Animation:**
```xml
<!-- activity_splash.xml -->
<ImageView
    android:id="@+id/ivLoadingLogo"
    android:layout_width="120dp"
    android:layout_height="120dp"
    android:src="@drawable/ic_launcher_foreground"
    android:scaleType="centerInside" />
```

```java
// startLogoPopupAnimation() - Creates pop-in effect
private void startLogoPopupAnimation(ImageView ivLogo) {
    // ObjectAnimator for scale from 0.5 to 1.0
    // OvershootInterpolator for bouncy effect
}
```

**Pulse Animation:**
```java
// startPulseAnimation() - Continuous pulsing
// Uses ValueAnimator on alpha (1.0 → 0.7 → 1.0)
```

### **Intent Extras (Optional)**

| Extra Key | Type | Description |
|-----------|------|-------------|
| `EXTRA_TARGET_CLASS` | String | Fully qualified class name to navigate to |
| `EXTRA_STATUS_MESSAGES` | String[] | Array of status messages to cycle through |
| `EXTRA_STATUS_MESSAGE` | String | Single status message |
| `EXTRA_DURATION_MS` | Long | Total duration of splash in milliseconds |

### **Viva Q&A**

**Q: How does LoadingActivity bridge authentication states?**
```
A: It receives the target class via Intent extras from the previous activity 
(which checked Firebase auth state). If no target is specified, it defaults 
to WelcomeActivity. This allows flexible navigation based on app state.
```

**Q: Why use a separate thread for progress?**
```
A: Main thread handles UI. Blocking it with sleep() would freeze the app. 
By running simulation on a background thread and posting to MainLooper, 
we avoid ANR (Application Not Responding) errors while animating smoothly.
```

**Q: What's the purpose of OvershootInterpolator?**
```
A: Creates a "bounce" effect - the animation overshoots the target value 
slightly, then bounces back. Makes the logo pop-in feel natural and engaging.
```

---

## 2️⃣ WelcomeActivity.java

### **Purpose**
First screen users see after LoadingActivity. Displays auto-rotating slideshow of onboarding images and provides Login/Sign-up entry points.

### **Key Concepts**

#### **What?**
- ViewPager2-based image slideshow with auto-rotation
- Two MaterialButtons (Login / Sign Up)
- Navigation to OtpEnterActivity with IS_SIGNUP flag

#### **Why?**
- **Marketing:** Showcases app benefits with beautiful images
- **User Segmentation:** Distinguishes new users (Sign Up) from returning users (Login)
- **First Impression:** Professional, modern onboarding experience

#### **How?**

**Slideshow Setup:**
```java
vpSlideshow = findViewById(R.id.vpSlideshow);
int[] images = {
    R.drawable.welcome_bg_1,
    R.drawable.welcome_bg_2,
    R.drawable.welcome_bg_3
};

SlideshowAdapter adapter = new SlideshowAdapter(images);
vpSlideshow.setAdapter(adapter);

// Auto-rotate every 3 seconds
slideshowHandler.postDelayed(slideshowRunnable, 3000);

private final Runnable slideshowRunnable = new Runnable() {
    @Override
    public void run() {
        if (vpSlideshow != null) {
            currentSlide = (currentSlide + 1) % 3;  // Cycle through 3 images
            vpSlideshow.setCurrentItem(currentSlide, true);  // true = animate
            slideshowHandler.postDelayed(this, 3000);
        }
    }
};
```

**Login Button:**
```java
btnLogin.setOnClickListener(v -> {
    Intent intent = new Intent(WelcomeActivity.this, OtpEnterActivity.class);
    intent.putExtra("IS_SIGNUP", false);  // Flag for login flow
    startActivity(intent);
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
});
```

**Sign-up Button:**
```java
btnSignUp.setOnClickListener(v -> {
    Intent intent = new Intent(WelcomeActivity.this, OtpEnterActivity.class);
    intent.putExtra("IS_SIGNUP", true);  // Flag for signup flow
    startActivity(intent);
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
});
```

### **Resource Cleanup**

```java
@Override
protected void onDestroy() {
    super.onDestroy();
    slideshowHandler.removeCallbacks(slideshowRunnable);  // Prevent memory leak
}
```

### **Viva Q&A**

**Q: Why use Handler instead of Timer for slideshow?**
```
A: Handler is tied to the MainLooper, ensuring UI updates happen on the 
main thread automatically. Timer is separate, requiring explicit post() to 
main thread. Handler is simpler and safer for Android.
```

**Q: What does modulo (%) operator achieve in slideshow?**
```
A: (currentSlide + 1) % 3 ensures the slide index wraps around:
   0 → 1 → 2 → 0 → 1 → ... (infinite loop)
```

**Q: Why clean up callbacks in onDestroy?**
```
A: If activity is destroyed but callback is posted for later execution, 
it will try to update freed View objects, causing crashes or memory leaks.
```

---

## 3️⃣ MainActivity.java

### **Purpose**
Central dispatcher that routes authenticated users to the correct home screen based on their saved role (Seeker or Provider).

### **Key Concepts**

#### **What?**
- Checks RoleManager for stored user role
- Dispatches to HomeProviderActivity or HomeSeekerActivity
- Clears activity stack to prevent back-navigation to auth screens

#### **Why?**
- **Role-based Navigation:** Different users see different interfaces
- **Security:** Clear task flags prevent accidental back-navigation to login
- **Simplicity:** Single entry point for authenticated users

#### **How?**

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    dispatchByRole();
}

private void dispatchByRole() {
    String role = RoleManager.getRole(this);  // Fetch saved role
    Intent intent;

    if (RoleManager.ROLE_PROVIDER.equals(role)) {
        intent = new Intent(this, HomeProviderActivity.class);
    } else {
        intent = new Intent(this, HomeSeekerActivity.class);
    }

    // FLAG_ACTIVITY_NEW_TASK: Create new task
    // FLAG_ACTIVITY_CLEAR_TASK: Clear all previous activities from task
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    
    startActivity(intent);
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    finish();  // Close MainActivity
}
```

### **Flag Explanation (Critical for Viva)**

| Flag | Effect |
|------|--------|
| `FLAG_ACTIVITY_NEW_TASK` | If task doesn't exist, create it |
| `FLAG_ACTIVITY_CLEAR_TASK` | Clear all activities in task before launching new one |
| Combined | User cannot back-navigate to login screen |

### **Viva Q&A**

**Q: Why clear the task instead of just finishing?**
```
A: If we only finish(), the previous activity (OtpVerifyActivity) is still 
in the back stack. User can back-press and return to login. 
CLEAR_TASK removes the entire auth flow stack.
```

**Q: How does RoleManager persist the role?**
```
A: RoleManager uses SharedPreferences to store the role string. 
SharedPreferences persist across app restarts.
```

---

# AUTHENTICATION FLOW

## 4️⃣ OtpEnterActivity.java

### **Purpose**
Collects phone number and initiates Firebase Phone Authentication flow.

### **Key Concepts**

#### **What?**
- EditText for 10-digit phone number
- Button to send OTP via Firebase
- Uses Firebase Phone Auth with server-side verification
- Callback handling for auto-verification and code-sent events

#### **Why?**
- **Security:** OTP is more secure than passwords
- **Verification:** Ensures user owns the phone number
- **Frictionless:** Firebase handles SMS delivery automatically
- **Speed:** No email setup required

#### **How?**

**Firebase Phone Auth Setup:**
```java
mAuth = FirebaseAuth.getInstance();

private void sendVerificationCode(String phoneNumber) {
    btnSendOtp.setEnabled(false);
    btnSendOtp.setText("Sending...");

    PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber("+91" + phoneNumber)  // India prefix
            .setTimeout(60L, TimeUnit.SECONDS)    // 60-second timeout
            .setActivity(this)
            .setCallbacks(mCallbacks)              // Callback object
            .build();
    
    PhoneAuthProvider.verifyPhoneNumber(options);
}
```

**Callbacks (Three States):**

```java
private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = 
    new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

    // State 1: Instant verification (rare, mainly for pre-registered numbers)
    @Override
    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
        signInWithPhoneAuthCredential(credential);
    }

    // State 2: Verification failed (network error, invalid number, etc.)
    @Override
    public void onVerificationFailed(@NonNull FirebaseException e) {
        Toast.makeText(OtpEnterActivity.this, 
            "Verification failed: " + e.getMessage(), 
            Toast.LENGTH_LONG).show();
        btnSendOtp.setEnabled(true);
        btnSendOtp.setText("Get OTP");
    }

    // State 3: Code successfully sent to phone
    @Override
    public void onCodeSent(@NonNull String verificationId,
                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
        mVerificationId = verificationId;  // Save for later verification
        // Navigate to OtpVerifyActivity
        Intent intent = new Intent(OtpEnterActivity.this, 
            OtpVerifyActivity.class);
        intent.putExtra("VERIFICATION_ID", verificationId);
        intent.putExtra("PHONE", phoneNumber);
        startActivity(intent);
    }
};
```

### **Phone Number Validation**

```java
String phone = etPhone.getText().toString().trim();
if (!phone.matches("\\d{10}")) {
    Toast.makeText(this, "Please enter a valid 10-digit number", 
        Toast.LENGTH_SHORT).show();
    return;
}
```

**Regex Explanation:**
- `\\d` = any digit (0-9)
- `{10}` = exactly 10 digits
- `^` and `$` = start and end of string (implicit)

### **Viva Q&A**

**Q: Why save verificationId in callback?**
```
A: The verification ID is a token that Firebase returns after successfully 
sending the OTP. We need this ID later in OtpVerifyActivity to exchange 
the user-entered OTP code for a PhoneAuthCredential. Without it, Firebase 
can't verify the code.
```

**Q: What happens if onVerificationCompleted is called?**
```
A: Rare, but Firebase can auto-verify if the device has permissions and 
receives the SMS. We directly sign in without user action. User is taken 
to next step.
```

**Q: Why setActivity() instead of using a context?**
```
A: setActivity() is specific to phone auth. It's needed for automatic SMS 
retrieval on Android 6.0+ (app has SMS reading permission). If we pass 
only a Context, Firebase can't access SMS automatically.
```

---

## 5️⃣ OtpVerifyActivity.java (Logic extended from pattern)

### **Purpose**
User enters OTP received via SMS. App verifies it with Firebase using the verificationId from OtpEnterActivity.

### **Key Concepts**

#### **What?**
- OTP input field (typically 6-digit)
- Send button to verify code
- Auto-dismiss keyboard on OTP entry
- Handles verification success/failure

#### **Why?**
- **Validation:** Confirms user has access to the phone number
- **Security:** Server-side OTP prevents tampering

#### **How?**

```java
// After user enters OTP and taps verify
private void verifyOtp(String otp) {
    PhoneAuthCredential credential = 
        PhoneAuthProvider.getCredential(mVerificationId, otp);
    
    mAuth.signInWithCredential(credential)
        .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = task.getResult().getUser();
                // OTP verified! Navigate to password setup
                Intent intent = new Intent(this, 
                    CreateNewPasswordActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, 
                    "OTP verification failed. Try again.", 
                    Toast.LENGTH_SHORT).show();
            }
        });
}
```

---

## 6️⃣ CreateNewPasswordActivity.java

### **Purpose**
User creates a secure password with real-time validation against strength requirements.

### **Key Concepts**

#### **What?**
- Two EditText fields (password + confirm)
- Real-time validation checklist with 5 criteria
- Dynamic button enable/disable based on validation
- Visual feedback (✓ green / ✗ gray)

#### **Why?**
- **Security:** Strong passwords reduce account takeover risk
- **UX:** Real-time feedback guides users without frustration
- **Compliance:** Multiple criteria ensure entropy

#### **How?**

**Password Requirements:**
```java
private boolean hasLength = false;      // 8+ characters
private boolean hasUppercase = false;   // At least one A-Z
private boolean hasLowercase = false;   // At least one a-z
private boolean hasNumber = false;      // At least one 0-9
private boolean hasSpecial = false;     // At least one !@#$%^&*
```

**Real-time Validation:**
```java
etNewPassword.addTextChangedListener(new TextWatcher() {
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        validatePasswordRealTime(s.toString());
    }
});

private void validatePasswordRealTime(String password) {
    // Check length (8+ characters)
    hasLength = password.length() >= 8;
    updateValidationView(tvPasswordLength, hasLength, "8+ characters");

    // Check uppercase
    hasUppercase = password.matches(".*[A-Z].*");
    updateValidationView(tvPasswordUppercase, hasUppercase, "Uppercase letter");

    // Check lowercase
    hasLowercase = password.matches(".*[a-z].*");
    updateValidationView(tvPasswordLowercase, hasLowercase, "Lowercase letter");

    // Check number
    hasNumber = password.matches(".*[0-9].*");
    updateValidationView(tvPasswordNumber, hasNumber, "Number");

    // Check special character
    hasSpecial = password.matches(".*[!@#$%^&*].*");
    updateValidationView(tvPasswordSpecial, hasSpecial, "Special character");

    // Update button state
    updateButtonState();
}
```

**Visual Feedback:**
```java
private void updateValidationView(TextView tv, boolean isMet, String text) {
    if (isMet) {
        tv.setText("✓ " + text);
        tv.setTextColor(getColor(R.color.community_green));  // Green
    } else {
        tv.setText("✗ " + text);
        tv.setTextColor(getColor(R.color.text_muted));       // Gray
    }
}
```

**Button State Management:**
```java
private void updateButtonState() {
    boolean isPasswordValid = hasLength && hasUppercase && 
        hasLowercase && hasNumber && hasSpecial;
    btnUpdatePassword.setEnabled(isPasswordValid);

    if (isPasswordValid) {
        btnUpdatePassword.setBackgroundTintList(
            getColorStateList(R.color.brand_primary));       // Blue
    } else {
        btnUpdatePassword.setBackgroundTintList(
            getColorStateList(R.color.text_muted));          // Gray
    }
}
```

**Password Submission:**
```java
private void validateAndUpdate() {
    String newPass = etNewPassword.getText().toString().trim();
    String confirmPass = etConfirmPassword.getText().toString().trim();

    // Clear previous errors
    tilNewPassword.setError(null);
    tilConfirmPassword.setError(null);

    // Validation checks
    if (TextUtils.isEmpty(newPass)) {
        tilNewPassword.setError("Enter new password");
        return;
    }

    if (!validatePassword(newPass)) {
        tilNewPassword.setError("Password does not meet all requirements");
        return;
    }

    if (TextUtils.isEmpty(confirmPass)) {
        tilConfirmPassword.setError("Confirm your password");
        return;
    }

    if (!newPass.equals(confirmPass)) {
        tilConfirmPassword.setError("Passwords do not match");
        etConfirmPassword.requestFocus();
        return;
    }

    // Success
    Toast.makeText(this, "Password updated successfully!", 
        Toast.LENGTH_SHORT).show();
    finish();
}
```

### **Regex Patterns (Critical for Viva)**

```java
// Uppercase: .*[A-Z].*
// Meaning: Any characters (.*) followed by uppercase letter [A-Z] 
//          followed by any characters (.*)
// Examples: "Test123!" ✓, "test123!" ✗

// Lowercase: .*[a-z].*
// Similar logic but for lowercase [a-z]

// Number: .*[0-9].*
// Any digit

// Special: .*[!@#$%^&*].*
// Any special character from the allowed set
```

### **Viva Q&A**

**Q: Why validate on every keystroke instead of on submit?**
```
A: Real-time validation provides immediate feedback, guiding users without 
frustration. If we validate only on submit, users might type the entire 
password, then get an error and have to retype.
```

**Q: Can we make the requirements dynamic?**
```
A: Yes! The current implementation hardcodes 5 requirements, but you could 
store them in a config and loop through. This makes the validator reusable 
for different password policies.
```

**Q: How does requestFocus() help UX?**
```
A: After setting error on a field, requestFocus() moves the cursor there, 
making it obvious which field needs fixing. User doesn't have to tap it.
```

---

# ONBOARDING FLOW

## 7️⃣ AccountTypeActivity.java

### **Purpose**
User selects whether they want to be a Seeker (client) or Provider (service provider).

### **Key Concepts**

#### **What?**
- Two MaterialCardViews (Seeker card + Provider card)
- Two buttons with corresponding actions
- Role selection → RoleManager persistence
- Role-specific navigation

#### **Why?**
- **Dual Mode:** App supports two distinct user personas
- **Separation:** Different features, UI, and workflows per role
- **Persistence:** Role persists across sessions via SharedPreferences

#### **How?**

**Card Layout:**
```xml
<!-- activity_account_type.xml -->
<com.google.android.material.card.MaterialCardView
    android:id="@+id/cardSeeker"
    android:layout_width="match_parent"
    android:layout_height="200dp"
    android:layout_marginHorizontal="16dp"
    android:layout_marginVertical="12dp"
    app:cardCornerRadius="12dp"
    app:cardElevation="4dp"
    android:clickable="true"
    android:focusable="true"
    android:foreground="?attr/selectableItemBackground">
    
    <LinearLayout android:orientation="vertical">
        <ImageView android:src="@drawable/ic_seeker" />
        <TextView android:text="I want to Hire"
                  android:textSize="18sp"
                  android:textStyle="bold" />
        <TextView android:text="Post jobs and find help" />
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
```

**Role Selection Logic:**
```java
private void completeRegistration(String role) {
    // Save role to SharedPreferences
    String finalRole = "seeker".equals(role) ? 
        RoleManager.ROLE_SEEKER : RoleManager.ROLE_PROVIDER;
    RoleManager.setRole(this, finalRole);

    // Save to Firestore (backend sync)
    Map<String, Object> updates = new HashMap<>();
    updates.put("role", finalRole);
    UserProfileRepository.saveCurrentUserProfile(updates, null);

    // Navigate based on role
    if ("seeker".equals(role)) {
        Intent intent = new Intent(this, HomeSeekerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
            Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    } else {
        // Providers get additional preferences
        Intent intent = new Intent(this, 
            CommunityPreferencesActivity.class);
        intent.putExtra("USER_ROLE", role);
        startActivity(intent);
    }
}
```

**Click Listeners:**
```java
private void setupListeners() {
    btnBack.setOnClickListener(v -> onBackPressed());

    // Both card and button respond to clicks
    cardSeeker.setOnClickListener(v -> completeRegistration("seeker"));
    btnSeekerAction.setOnClickListener(v -> completeRegistration("seeker"));
    
    cardProvider.setOnClickListener(v -> completeRegistration("provider"));
    btnProviderAction.setOnClickListener(v -> completeRegistration("provider"));
}
```

### **Two-Tier Persistence**

```java
// Tier 1: Local (Fast - no network)
RoleManager.setRole(this, finalRole);  // SharedPreferences

// Tier 2: Remote (Sync with other devices)
UserProfileRepository.saveCurrentUserProfile(updates, null);  // Firestore
```

### **Viva Q&A**

**Q: Why save role in both SharedPreferences and Firestore?**
```
A: SharedPreferences is instant, allowing immediate role-based UI changes. 
Firestore sync is for cloud backup and cross-device consistency. 
If user logs in on another device, role is fetched from Firestore.
```

**Q: What does FLAG_ACTIVITY_CLEAR_TASK prevent?**
```
A: Without it, user can back-press through auth flow. With it, the entire 
authentication chain is cleared from the stack, making the home screen the 
new "base" of the app.
```

---

## 8️⃣ ProfileSetupActivity.java

### **Purpose**
Collects user address and location during onboarding. Provides manual search or auto-detection via GPS.

### **Key Concepts**

#### **What?**
- Location search with autocomplete predictions
- Auto-detect location via GPS
- Geocoding (address → lat/lng) using Google's Geocoding API
- Fallback to manual entry

#### **Why?**
- **Location-based Services:** Essential for finding nearby providers/jobs
- **Map Integration:** Enables location markers
- **UX:** Autocomplete speeds up entry vs. manual typing

#### **How?**

**Location Permission Handling:**
```java
private static final int LOCATION_PERMISSION_REQ = 101;
private FusedLocationProviderClient fusedLocationClient;

private void detectRealLocation() {
    // Check if permission is granted
    if (ContextCompat.checkSelfPermission(this, 
        Manifest.permission.ACCESS_FINE_LOCATION) 
        != PackageManager.PERMISSION_GRANTED) {
        
        // Request permission
        ActivityCompat.requestPermissions(this, 
            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
            LOCATION_PERMISSION_REQ);
        return;
    }

    // Get last known location
    fusedLocationClient.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY,
        null).addOnSuccessListener(location -> {
            if (location != null) {
                selectedLat = location.getLatitude();
                selectedLng = location.getLongitude();
                reverseGeocode(selectedLat, selectedLng);  // Convert to address
            }
        });
}
```

**Search Predictions (Real-time):**
```java
etLocationSearch.addTextChangedListener(new TextWatcher() {
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String query = s.toString().trim();
        if (query.isEmpty()) {
            rvSearchPredictions.setVisibility(View.GONE);
            selectedAddress = null;
            return;
        }

        // Debounce: wait 450ms before searching
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }

        searchRunnable = () -> performGeocoding(query);
        searchHandler.postDelayed(searchRunnable, 450);  // 450ms delay
    }
});
```

**Geocoding (Address Lookup):**
```java
private void performGeocoding(String query) {
    // Use OkHttp to call geocoding API
    String url = "https://api.maptiler.com/geocoding/" + 
        URLEncoder.encode(query) + 
        ".json?key=" + BuildConfig.MAPTILER_API_KEY;

    Request request = new Request.Builder().url(url).build();
    
    httpClient.newCall(request).enqueue(new Callback() {
        @Override
        public void onResponse(@NonNull Call call, 
            @NonNull Response response) throws IOException {
            if (response.isSuccessful() && response.body() != null) {
                String jsonData = response.body().string();
                JSONObject json = new JSONObject(jsonData);
                JSONArray features = json.optJSONArray("features");
                
                // Parse predictions
                List<Prediction> predictions = new ArrayList<>();
                for (int i = 0; i < features.length(); i++) {
                    JSONObject feature = features.getJSONObject(i);
                    String placeName = feature.getString("place_name");
                    JSONArray center = 
                        feature.getJSONArray("center");
                    
                    predictions.add(new Prediction(
                        placeName,
                        center.getDouble(0),  // longitude
                        center.getDouble(1)   // latitude
                    ));
                }
                
                // Update RecyclerView on main thread
                runOnUiThread(() -> {
                    searchPredictionAdapter.setData(predictions);
                    rvSearchPredictions.setVisibility(
                        View.VISIBLE);
                });
            }
        }

        @Override
        public void onFailure(@NonNull Call call, 
            @NonNull IOException e) {
            Log.e("Geocoding", "API call failed", e);
        }
    });
}
```

**Reverse Geocoding (Lat/Lng → Address):**
```java
private void reverseGeocode(double lat, double lng) {
    String url = "https://api.maptiler.com/geocoding/" + 
        lng + "," + lat + 
        ".json?key=" + BuildConfig.MAPTILER_API_KEY;

    // Same API call as above, but with reversed order
}
```

**Prediction Selection:**
```java
searchPredictionAdapter = new SearchPredictionAdapter(
    (lat, lng, name) -> {
        // User tapped a prediction
        selectedLat = lat;
        selectedLng = lng;
        selectedAddress = name;
        
        etLocationSearch.setText(name);  // Show selection
        rvSearchPredictions.setVisibility(View.GONE);
        
        // Hide keyboard
        InputMethodManager imm = 
            (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(
            etLocationSearch.getWindowToken(), 0);
    }
);
```

**Save Location to Firestore:**
```java
private void saveLocationToFirestore(String address) {
    Map<String, Object> updates = new HashMap<>();
    updates.put("address", address);
    updates.put("latitude", selectedLat);
    updates.put("longitude", selectedLng);
    updates.put("lastLocationUpdate", 
        System.currentTimeMillis());
    
    UserProfileRepository.saveCurrentUserProfile(updates, null);
}
```

### **Debouncing Explained**

```java
// Without debouncing:
// User types "M" → API call
// User types "u" → API call
// User types "m" → API call
// User types "b" → API call
// Result: 4 unnecessary API calls, rate limit risk, poor performance

// With 450ms debouncing:
// User types "M" → Wait 450ms → Check if new input
// User types "u" before 450ms → Cancel previous, wait 450ms again
// User types "m" before 450ms → Cancel previous, wait 450ms again
// User types "b" before 450ms → Cancel previous, wait 450ms again
// After 450ms of inactivity → Single API call
// Result: Only 1 API call, optimal performance
```

### **Viva Q&A**

**Q: What's the difference between geocoding and reverse geocoding?**
```
A: Geocoding: Address → Latitude/Longitude
   Example: "Mumbai, India" → (19.0760, 72.8777)

   Reverse Geocoding: Latitude/Longitude → Address
   Example: (19.0760, 72.8777) → "Mumbai, India"

Both use the same API with different parameter order.
```

**Q: Why debounce search input?**
```
A: Geocoding API has rate limits (e.g., 100 requests per minute). Without 
debouncing, typing 20 characters = 20 API calls. Debouncing reduces this 
to 1 call per pause, saving quota and improving performance.
```

**Q: How does FusedLocationProviderClient work?**
```
A: It intelligently fuses data from GPS, network provider, and sensors to 
get the most accurate location with lowest battery drain. Better than 
using GPS alone.
```

---

## 9️⃣ IdVerificationActivity.java

### **Purpose**
User uploads front and back of ID (Aadhar, Passport, etc.). App validates with Google ML Kit's Text Recognition to confirm authenticity.

### **Key Concepts**

#### **What?**
- Two image upload cards (Front + Back)
- Google ML Kit TextRecognizer for OCR (Optical Character Recognition)
- Checkbox for terms acceptance
- Clickable terms link → TermsConditionsActivity

#### **Why?**
- **Trust & Safety:** ID verification prevents fraud
- **Compliance:** Required for financial transactions (RBI regulations in India)
- **ML Kit:** On-device processing = faster, private, no server upload

#### **How?**

**Image Selection:**
```java
private static final int REQUEST_PICK_FRONT = 1001;
private static final int REQUEST_PICK_BACK  = 1002;

private void openImagePicker(int requestCode) {
    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    intent.setType("image/*");
    startActivityForResult(
        Intent.createChooser(intent, "Select ID Image"), 
        requestCode);
}

@Override
protected void onActivityResult(int requestCode, int resultCode, 
    Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    
    if (resultCode != RESULT_OK || data == null) return;
    
    Uri imageUri = data.getData();
    if (imageUri == null) return;

    if (requestCode == REQUEST_PICK_FRONT) {
        frontUploaded = true;
        // Show selected image on front card
    } else if (requestCode == REQUEST_PICK_BACK) {
        backUploaded = true;
        // Show selected image on back card
    }

    checkReadyToSubmit();  // Enable submit if both uploaded
}
```

**Google ML Kit Text Recognition (On-device):**
```java
// Initialize TextRecognizer
TextRecognizer recognizer = 
    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

// Process image
InputImage image = InputImage.fromFilePath(context, imageUri);

recognizer.process(image)
    .addOnSuccessListener(visionText -> {
        String detectedText = visionText.getText();
        
        // Validate ID contains expected fields
        boolean hasNumbers = detectedText.matches(".*\\d{6,}.*");
        boolean hasAlpha = detectedText.matches(".*[A-Za-z]{4,}.*");
        
        if (hasNumbers && hasAlpha) {
            isFullyVerified = true;  // ID appears valid
        } else {
            Toast.makeText(this, 
                "Please ensure ID is clearly visible", 
                Toast.LENGTH_SHORT).show();
        }
    })
    .addOnFailureListener(e -> {
        Log.e("MLKit", "Text recognition failed", e);
    });
```

**Terms Link with Clickable Span:**
```java
private void styleTermsText() {
    String fullText = "I agree to the Terms, Conditions and Guidelines";
    SpannableString spannableString = 
        new SpannableString(fullText);
    int start = fullText.indexOf("Terms");
    int end = fullText.length();

    ClickableSpan clickableSpan = new ClickableSpan() {
        @Override
        public void onClick(View widget) {
            startActivity(new Intent(IdVerificationActivity.this, 
                TermsConditionsActivity.class));
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(true);
            ds.setColor(0xFF1E3A8A);  // Dark blue
        }
    };

    spannableString.setSpan(clickableSpan, start, end, 
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

    tvTermsLink.setText(spannableString);
    tvTermsLink.setMovementMethod(LinkMovementMethod.getInstance());
}
```

**Submit Verification:**
```java
btnSubmit.setOnClickListener(v -> {
    // Validate both images are uploaded
    if (!frontUploaded || !backUploaded) {
        Toast.makeText(this, 
            "Please upload both sides of your ID", 
            Toast.LENGTH_SHORT).show();
        return;
    }

    // Validate terms accepted
    if (!cbTerms.isChecked()) {
        Toast.makeText(this, 
            "Please accept the terms and conditions", 
            Toast.LENGTH_SHORT).show();
        return;
    }

    btnSubmit.setEnabled(false);
    btnSubmit.setText("Verifying Authenticity...");

    // Simulate verification (real flow would upload to backend)
    new Handler(Looper.getMainLooper()).postDelayed(() -> {
        isFullyVerified = true;
        btnSubmit.setText("ID Verified Successfully");

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            UserPrefs.saveVerified(this, true);
            saveVerifiedToFirestore();
            
            Intent intent = new Intent(this, 
                IdVerifiedActivity.class);
            startActivity(intent);
        }, 1000);
    }, 2500);  // 2.5-second simulation
});
```

### **ML Kit vs. Backend Processing**

| Aspect | ML Kit (On-device) | Backend Server |
|--------|-------------------|-----------------|
| **Speed** | Instant | Network latency |
| **Privacy** | User data stays local | Data leaves device |
| **Cost** | Included in Android SDK | API costs |
| **Accuracy** | Good for OCR | Can train custom models |
| **Use Case** | Quick validation | Deep fraud detection |

### **Viva Q&A**

**Q: Why use ML Kit instead of sending to backend?**
```
A: ML Kit processes locally on the device, avoiding network latency and 
privacy concerns. For basic ID validation (text detection), it's sufficient. 
For advanced fraud detection, backend can use ML models, but initial 
validation happens on-device.
```

**Q: What does TextRecognition.getClient() return?**
```
A: A TextRecognizer instance configured for Latin text (English, Indian 
languages, etc.). Different options exist for other scripts. Once initialized, 
it can process multiple images.
```

**Q: How does ClickableSpan work differently from regular links?**
```
A: Regular links use <a> tags in HTML. ClickableSpan is Android's way to 
make specific text within a TextView clickable. It's embedded directly in 
the text, not a separate hyperlink.
```

---

## 🔟 IdVerifiedActivity.java

### **Purpose**
Success screen showing ID verification is complete. Navigation to profile selection.

### **Key Concepts**

#### **What?**
- Simple screen with "ID Verified" confirmation
- Continue button → AccountTypeActivity (role selection)

#### **Why?**
- **Confirmation:** Assures user their ID was processed
- **Transition:** Bridges ID verification to role selection

### **Code**

```java
public class IdVerifiedActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_id_verified);

        MaterialButton btnContinue = findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(this, 
                ProfileSuccessActivity.class);
            startActivity(intent);
        });
    }
}
```

---

## 1️⃣1️⃣ ProfileSuccessActivity.java

### **Purpose**
Final onboarding success screen with animation.

### **Key Concepts**

#### **What?**
- Pop-in animation for success icon
- Done button → AccountTypeActivity

#### **Why?**
- **Celebration:** Animates the success moment
- **Engagement:** Visual feedback reinforces completion

### **Code**

```java
public class ProfileSuccessActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_success);

        View successContainer = 
            findViewById(R.id.successContainer);
        MaterialButton btnDone = 
            findViewById(R.id.btnDone);

        applyPopAnimation(successContainer);

        btnDone.setOnClickListener(v -> {
            Intent intent = new Intent(this, 
                AccountTypeActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, 
                android.R.anim.fade_out);
        });
    }

    private void applyPopAnimation(View view) {
        view.setScaleX(0.7f);
        view.setScaleY(0.7f);
        view.setAlpha(0f);

        ObjectAnimator scaleX = 
            ObjectAnimator.ofFloat(view, "scaleX", 0.7f, 1.0f);
        ObjectAnimator scaleY = 
            ObjectAnimator.ofFloat(view, "scaleY", 0.7f, 1.0f);
        ObjectAnimator alpha = 
            ObjectAnimator.ofFloat(view, "alpha", 0f, 1.0f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, alpha);
        set.setDuration(800);
        set.setInterpolator(new OvershootInterpolator(1.4f));
        set.start();  // Animation plays automatically
    }
}
```

### **Animation Breakdown**

```
Initial State:  Scale 0.7x, Alpha 0 (invisible)
     ↓
Animation:     Scale 0 → 1.0, Alpha 0 → 1.0 (800ms)
     ↓
Interpolation: OvershootInterpolator (overshoots then bounces back)
     ↓
Final State:   Scale 1.0, Alpha 1.0 (fully visible)
```

---

# PROFILE HUB

## 1️⃣2️⃣ ProfileActivity.java

### **Purpose**
Main profile screen showing user's own profile information. Syncs real-time with Firestore.

### **Key Concepts**

#### **What?**
- Displays current user's profile (name, location, photo)
- Real-time listener on Firestore `Users/{userId}`
- Local cache display while Firestore loads
- Role-based UI (different layouts for Seeker/Provider)

#### **Why?**
- **Instant Display:** Local cache shows data immediately
- **Always Fresh:** Firestore listener updates if data changes elsewhere
- **Offline Support:** Local cache works without network

#### **How?**

**Dual Layout System:**
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    String role = RoleManager.getRole(this);
    if (RoleManager.ROLE_PROVIDER.equals(role)) {
        setContentView(R.layout.layout_profile_provider);  // Provider layout
    } else {
        setContentView(R.layout.layout_profile_seeker);    // Seeker layout
    }

    SeekerNavbarController.bind(this, 
        findViewById(android.R.id.content), 
        SeekerNavbarController.TAB_PROFILE);
    
    ProfileModeSwitcher.bind(this, 
        findViewById(android.R.id.content), role);
    
    bindMenuClicks();
    applyLocalCache();  // Show cached data immediately
}
```

**Local Cache Display:**
```java
private void applyLocalCache() {
    String name = UserPrefs.getName(this);
    String location = UserPrefs.getLocation(this);
    String photoUriStr = UserPrefs.getPhotoUri(this);

    setName(name);
    setLocation(location);

    if (photoUriStr != null) {
        ImageView ivPhoto = 
            findViewById(R.id.iv_profile_picture);
        TextView tvInitials = 
            findViewById(R.id.tv_profile_initials);
        
        if (ivPhoto != null) {
            // Glide handles both local (content://) and 
            // remote (https://) URIs
            Glide.with(this).load(Uri.parse(photoUriStr))
                .circleCrop()
                .into(ivPhoto);
            ivPhoto.setVisibility(View.VISIBLE);
            if (tvInitials != null) {
                tvInitials.setVisibility(View.GONE);
            }
        }
    }

    // Sync local data to cloud (for backup)
    syncToUsersCollection(name, photoUriStr, location, 
        UserPrefs.isVerified(this));
}
```

**Real-time Firestore Listener:**
```java
@Override
protected void onStart() {
    super.onStart();
    FirebaseUser user = 
        FirebaseAuth.getInstance().getCurrentUser();
    if (user == null) return;

    // Listen to this specific user's document
    profileListener = FirebaseFirestore.getInstance()
            .collection("Users").document(user.getUid())
            .addSnapshotListener((snapshot, error) -> {
                if (error != null || snapshot == null || 
                    !snapshot.exists()) {
                    return;
                }
                applySnapshot(snapshot);  // Update UI
            });
}

private void applySnapshot(DocumentSnapshot snapshot) {
    String name = snapshot.getString("fullName");
    String location = snapshot.getString("location");
    String photoUrl = snapshot.getString("photoUrl");
    Boolean verified = snapshot.getBoolean("isVerified");

    // Persist to local cache
    if (name != null && !name.isEmpty()) {
        UserPrefs.saveName(this, name);
    }
    if (location != null && !location.isEmpty()) {
        UserPrefs.saveLocation(this, location);
    }
    if (photoUrl != null && !photoUrl.isEmpty()) {
        UserPrefs.savePhotoUri(this, photoUrl);
    }
    if (verified != null) {
        UserPrefs.saveVerified(this, verified);
    }

    // Update UI with fresh data
    setName(name);
    setLocation(location);
    setPhoto(photoUrl);
}
```

**Cleanup:**
```java
@Override
protected void onStop() {
    super.onStop();
    if (profileListener != null) {
        profileListener.remove();  // Stop listening
        profileListener = null;
    }
}
```

### **Two-Tier Architecture**

```
┌─────────────────────────┐
│   Firestore Cloud       │
│   (Remote Source)       │
└────────────┬────────────┘
             │
             │ addSnapshotListener (Real-time)
             ↓
┌─────────────────────────┐
│   ProfileActivity       │  applySnapshot() → Updates UI
│   (Observer)            │
└────────┬────────────────┘
         │
         │ UserPrefs.save*()
         ↓
┌─────────────────────────┐
│   SharedPreferences     │
│   (Local Cache)         │
└─────────────────────────┘
         ↑
         │
         │ UserPrefs.get*()
         │
    ┌────┴────┐
    │ applyLocalCache()
    │ (Next launch)
    └──────────┘
```

### **Viva Q&A**

**Q: Why cache data locally if Firestore is the source of truth?**
```
A: Network requests take 100-500ms. Showing cached data instantly while 
listening for updates gives the impression of a fast app. Without caching, 
users see blank screens after launching, waiting for network.
```

**Q: Can multiple listeners cause memory leaks?**
```
A: Yes! If onStart() creates a new listener every time (without removing 
old ones), multiple listeners accumulate. Removing in onStop() prevents this.
```

**Q: How does Glide distinguish between local and remote URIs?**
```
A: Content URIs start with "content://" (local storage). 
HTTP(S) URIs start with "https://" (remote). Glide detects the scheme 
and loads accordingly.
```

---

## 1️⃣3️⃣ PersonProfileActivity.java

### **Purpose**
View another user's profile (Provider for Seeker, or Seeker for Provider). Uses real-time listener to sync profile data.

### **Key Concepts**

#### **What?**
- Displays user passed via Intent extras (PERSON_USER_ID, PERSON_NAME, etc.)
- Fallback to Firestore if local data is incomplete
- Real-time listener on Firestore `users/{personUserId}`
- Verified badge via VerifiedBadgeHelper
- Avatar selection based on gender
- Clickable reviews → ReviewsActivity

#### **Why?**
- **Flexibility:** Intent extras for quick display, Firestore for always-fresh data
- **Trust:** Verified badge visible immediately
- **Engagement:** Reviews are clickable for deeper dive

#### **How?**

**Intent Extras Reception:**
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_person_profile);

    // Fallback data from Intent extras
    String name = readExtra("PERSON_NAME", 
        "NearNeed User");
    String email = readExtra("PERSON_EMAIL", 
        "user@nearneed.app");
    String phone = readExtra("PERSON_PHONE", 
        "+91 98765 43210");
    String gender = readExtra("PERSON_GENDER", 
        "Not specified");
    String experience = readExtra("PERSON_EXPERIENCE", 
        "3 years");
    String rating = readExtra("PERSON_RATING", "4.7");
    String reviews = readExtra("PERSON_REVIEWS", 
        "100 reviews");
    String bio = readExtra("PERSON_BIO", 
        "Active NearNeed member...");

    personUserId = getIntent().getStringExtra(
        "PERSON_USER_ID");
    fallbackName = name;
    fallbackReviews = reviews;

    // Display fallback data immediately
    TextView tvName = findViewById(R.id.tvName);
    tvName.setText(name);
    
    // Apply verified badge
    VerifiedBadgeHelper.apply(this, tvName, 
        getIntent().getBooleanExtra(
            "IS_VERIFIED", false));
    
    ((TextView) findViewById(R.id.tvEmail))
        .setText(email);
    ((TextView) findViewById(R.id.tvPhone))
        .setText(phone);
    ((TextView) findViewById(R.id.tvGender))
        .setText(gender);
    ((TextView) findViewById(R.id.tvExperience))
        .setText(experience);
    ((TextView) findViewById(R.id.tvRating))
        .setText(rating + " ★");
    ((TextView) findViewById(R.id.tvReviews))
        .setText(reviews);
    ((TextView) findViewById(R.id.tvBio))
        .setText(bio);

    // Set avatar based on gender
    ImageView ivProfile = 
        findViewById(R.id.ivProfile);
    if (ivProfile != null) {
        ivProfile.setImageResource(
            avatarForGender(gender));
    }

    // Make reviews and rating clickable
    View.OnClickListener openReviews = v -> {
        Intent intent = new Intent(
            PersonProfileActivity.this, 
            ReviewsActivity.class);
        intent.putExtra("PERSON_NAME", 
            tvName.getText().toString());
        startActivity(intent);
    };

    View reviewsChip = 
        findViewById(R.id.llReviewsChip);
    View ratingRow = 
        findViewById(R.id.llRatingRow);
    
    if (reviewsChip != null) {
        reviewsChip.setOnClickListener(
            openReviews);
    }
    if (ratingRow != null) {
        ratingRow.setOnClickListener(
            openReviews);
    }
}

private String readExtra(String key, 
    String defaultValue) {
    String value = getIntent().getStringExtra(key);
    return value != null ? value : defaultValue;
}

private int avatarForGender(String gender) {
    if ("Male".equals(gender)) {
        return R.drawable.avatar_male;
    } else if ("Female".equals(gender)) {
        return R.drawable.avatar_female;
    }
    return R.drawable.avatar_default;
}
```

**Real-time Profile Sync:**
```java
@Override
protected void onStart() {
    super.onStart();
    subscribeRealtimeProfile();
}

private void subscribeRealtimeProfile() {
    if (personUserId == null || 
        personUserId.trim().isEmpty()) {
        return;
    }

    profileListener = 
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(personUserId)
            .addSnapshotListener((snapshot, error) -> {
                if (error != null) {
                    Log.e("PersonProfile", 
                        "Error listening to profile", 
                        error);
                    return;
                }

                if (snapshot != null && 
                    snapshot.exists()) {
                    applyLiveProfile(snapshot);
                }
            });
}

private void applyLiveProfile(
    DocumentSnapshot snapshot) {
    String name = 
        snapshot.getString("fullName");
    String email = 
        snapshot.getString("email");
    String phone = 
        snapshot.getString("phone");
    String gender = 
        snapshot.getString("gender");
    String experience = 
        snapshot.getString("experience");
    String rating = 
        snapshot.getString("rating");
    String reviews = 
        snapshot.getString("reviews");
    String bio = 
        snapshot.getString("bio");
    String profileImageUrl = 
        snapshot.getString("profileImage");
    Boolean isVerified = 
        snapshot.getBoolean("isVerified");

    // Update UI with live data
    TextView tvName = 
        findViewById(R.id.tvName);
    if (tvName != null && name != null) {
        tvName.setText(name);
        VerifiedBadgeHelper.apply(this, tvName, 
            isVerified != null && isVerified);
    }

    if (email != null) {
        ((TextView) findViewById(
            R.id.tvEmail)).setText(email);
    }
    // ... (update other fields)

    // Update profile image
    if (profileImageUrl != null && 
        !profileImageUrl.isEmpty()) {
        Glide.with(this)
            .load(profileImageUrl)
            .circleCrop()
            .into((ImageView) 
                findViewById(R.id.ivProfile));
    }
}

@Override
protected void onStop() {
    super.onStop();
    if (profileListener != null) {
        profileListener.remove();
        profileListener = null;
    }
}
```

### **Viva Q&A**

**Q: Why pass data via Intent extras if we fetch from Firestore anyway?**
```
A: Intent extras provide instant display while Firestore loads. Without 
them, users see blank screens for 100-500ms. With intent data, they see 
content immediately, and Firestore updates if data changed elsewhere.

This is called "optimistic UI" — assume data is fresh, update if needed.
```

**Q: How does VerifiedBadgeHelper work?**
```
A: It adds a verified icon (R.drawable.ic_verified_small) as a compound 
drawable on the right side of a TextView. If isVerified is false, 
it removes the drawable. Safe to call multiple times.
```

---

## 1️⃣4️⃣ EditProfileActivity.java

### **Purpose**
Edit own profile: bio, profile photo, name (read-only). Photos are uploaded to Firebase Storage.

### **Key Concepts**

#### **What?**
- EditText for bio (150-character limit with counter)
- Image picker → Firebase Storage upload
- Real-time character count
- Name, gender displayed as read-only (profile locked after onboarding)
- Loading spinner during upload

#### **Why?**
- **Customization:** Users personalize their profiles
- **Trust:** Photo builds credibility
- **Limit Bio:** Encourages concise descriptions
- **Read-only Name:** Prevents fraud via name changes

#### **How?**

**Image Picker Setup:**
```java
private ActivityResultLauncher<Intent> imagePickerLauncher;
private Uri pendingPhotoUri = null;

// Register in onCreate()
imagePickerLauncher = registerForActivityResult(
    new ActivityResultContracts.StartActivityForResult(),
    result -> {
        if (result.getResultCode() == Activity.RESULT_OK && 
            result.getData() != null) {
            
            Uri selectedImage = 
                result.getData().getData();
            if (selectedImage != null) {
                pendingPhotoUri = selectedImage;
                
                // Display selected image
                if (ivProfilePhoto != null) {
                    ivProfilePhoto.setImageURI(
                        selectedImage);
                    ivProfilePhoto.setVisibility(
                        View.VISIBLE);
                }
                if (tvProfileInitials != null) {
                    tvProfileInitials.setVisibility(
                        View.GONE);
                }
            }
        }
    }
);

// Open picker when user taps photo
View photoContainer = 
    findViewById(R.id.layoutProfilePhoto);
if (photoContainer != null) {
    photoContainer.setOnClickListener(v -> 
        openImagePicker());
}

private void openImagePicker() {
    Intent intent = new Intent(
        Intent.ACTION_GET_CONTENT);
    intent.setType("image/*");
    imagePickerLauncher.launch(intent);
}
```

**Bio Character Counter:**
```java
if (etBio != null && tvBioCount != null) {
    etBio.addTextChangedListener(
        new TextWatcher() {
            @Override
            public void onTextChanged(
                CharSequence s, int start, 
                int before, int count) {
                int charCount = 
                    s != null ? s.length() : 0;
                
                tvBioCount.setText(charCount + 
                    "/150 characters");
                
                // Change color near limit
                if (charCount >= 130) {
                    tvBioCount.setTextColor(
                        getColor(R.color.warning));
                } else if (charCount > 150) {
                    tvBioCount.setTextColor(
                        getColor(R.color.error));
                } else {
                    tvBioCount.setTextColor(
                        getColor(R.color.text_muted));
                }
            }

            @Override
            public void beforeTextChanged(
                CharSequence s, int start, 
                int count, int after) {}

            @Override
            public void afterTextChanged(
                Editable s) {}
        });
}
```

**Save Profile with Photo Upload:**
```java
btnSave.setOnClickListener(v -> {
    String bioText = etBio != null ? 
        etBio.getText().toString().trim() : "";
    
    if (bioText.length() > 150) {
        Toast.makeText(this, 
            "Bio exceeds 150 characters", 
            Toast.LENGTH_SHORT).show();
        return;
    }

    // Show loading spinner
    showLoadingDialog("Updating profile...");

    Map<String, Object> updates = 
        new HashMap<>();
    updates.put("bio", bioText);

    // If user selected a new photo, upload it
    if (pendingPhotoUri != null) {
        uploadPhotoToStorage(pendingPhotoUri, 
            newPhotoUrl -> {
                updates.put("profileImage", 
                    newPhotoUrl);
                
                // Save profile with new photo URL
                UserProfileRepository
                    .saveCurrentUserProfile(
                        updates, error -> {
                    dismissLoadingDialog();
                    if (error != null) {
                        Toast.makeText(
                            EditProfileActivity.this, 
                            "Failed to update", 
                            Toast.LENGTH_SHORT)
                            .show();
                    } else {
                        Toast.makeText(
                            EditProfileActivity.this, 
                            "Profile updated", 
                            Toast.LENGTH_SHORT)
                            .show();
                        finish();
                    }
                });
            });
    } else {
        // No new photo, just save bio
        UserProfileRepository
            .saveCurrentUserProfile(
                updates, error -> {
            dismissLoadingDialog();
            if (error != null) {
                Toast.makeText(
                    EditProfileActivity.this, 
                    "Failed to update", 
                    Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(
                    EditProfileActivity.this, 
                    "Profile updated", 
                    Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
});
```

**Photo Upload to Firebase Storage:**
```java
private void uploadPhotoToStorage(Uri photoUri, 
    OnPhotoUrlReady onReady) {
    
    FirebaseAuth auth = 
        FirebaseAuth.getInstance();
    if (auth.getCurrentUser() == null) return;

    String userId = auth.getCurrentUser().getUid();
    String fileName = "profile_" + 
        System.currentTimeMillis() + ".jpg";
    
    StorageReference storageRef = 
        FirebaseStorage.getInstance()
            .getReference()
            .child("profiles/" + userId + 
                "/" + fileName);

    storageRef.putFile(photoUri)
        .addOnSuccessListener(taskSnapshot -> {
            // Get download URL
            storageRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    onReady.onReady(
                        uri.toString());
                })
                .addOnFailureListener(e -> {
                    Log.e("Storage", 
                        "Failed to get URL", e);
                });
        })
        .addOnFailureListener(e -> {
            Log.e("Storage", 
                "Upload failed", e);
            Toast.makeText(
                EditProfileActivity.this, 
                "Photo upload failed", 
                Toast.LENGTH_SHORT).show();
        });
}

interface OnPhotoUrlReady {
    void onReady(String photoUrl);
}
```

### **ActivityResultContracts vs. deprecated startActivityForResult()**

```java
// OLD WAY (deprecated)
@Override
protected void onActivityResult(int requestCode, 
    int resultCode, Intent data) {
    // Handle result
}

// NEW WAY (recommended)
private ActivityResultLauncher<Intent> launcher = 
    registerForActivityResult(
        new ActivityResultContracts
            .StartActivityForResult(),
        result -> {
            // Handle result
        });

// NEW is better because:
// 1. Type-safe (compiler checks Intent type)
// 2. Lifecycle-aware (no callback after destroy)
// 3. Permission handling built-in
```

### **Viva Q&A**

**Q: Why upload to Firebase Storage instead of saving base64 to Firestore?**
```
A: Base64 inflates data size. A 1MB image becomes 1.3MB as base64 string. 
Firestore charges per read/write operation, and large documents slow sync. 
Storage is cheaper for binary files, and URL-based access is cleaner.
```

**Q: How does the callback chain (onReady) work?**
```
A: It's a closure pattern:
1. uploadPhotoToStorage() starts, accepts callback
2. Firebase upload completes
3. getDownloadUrl() returns URL
4. Callback (onReady) fires with URL
5. Caller receives URL and saves it to Firestore
```

---

## 1️⃣5️⃣ EditProfileProviderActivity.java

### **Purpose**
Provider-specific profile editing: services, specialization, hourly rate, availability schedule.

### **Key Concepts**

#### **What?**
- Multi-step form (Step 1, Step 2, potentially more)
- Service selection (Plumbing, Electrical, Cleaning, etc.)
- Hourly rate input
- Availability scheduling

#### **Why?**
- **Specialization:** Providers list what they do
- **Pricing:** Clients see rates upfront
- **Calendar:** Scheduling feature for bookings

### **Note**

This file follows similar patterns to EditProfileActivity but with provider-specific fields. The implementation is modular, using:
- ViewFlipper or fragments for step navigation
- Material Design components (Chip, Slider, etc.)
- Real-time Firestore sync

---

## 1️⃣6️⃣ ProfileInfoActivity.java

### **Purpose**
May be a detail view or editing screen for specific profile sections (address, contact info, etc.).

### **Key Concepts**

This typically mirrors EditProfileActivity pattern but for individual fields rather than the full profile.

---

# SUPPORT FLOW

## 1️⃣7️⃣ SettingsActivity.java

### **Purpose**
User preferences: notifications, privacy, language, logout.

### **Key Concepts**

#### **What?**
- Toggle settings (notifications, location sharing)
- Language selection
- Logout button
- Account deletion

#### **Why?**
- **Control:** Users manage permissions
- **Transparency:** Privacy settings visible
- **Safety:** Easy logout from shared devices

---

## 1️⃣8️⃣ HelpSupportActivity.java

### **Purpose**
FAQ, contact support, bug reporting.

### **Key Concepts**

#### **What?**
- Expandable FAQ section
- Contact form for support tickets
- Chat with support (if integrated)

### **Why?**
- **Self-Service:** FAQs reduce support load
- **Feedback:** Bug reports help improve app

---

## 1️⃣9️⃣ TermsConditionsActivity.java

### **Purpose**
Displays app terms, privacy policy, and community guidelines.

### **Key Concepts**

#### **What?**
- Scrollable TextView with full text
- WebView rendering if content is HTML/Markdown
- Accept/Decline buttons (if in flow)

#### **Why?**
- **Legal:** Compliance with laws
- **Transparency:** Users understand policies

---

# HELPER UTILITIES & MODELS

## 2️⃣0️⃣ RoleManager.java

### **Purpose**
Centralized SharedPreferences management for user role (Seeker/Provider).

### **Key Concepts**

#### **What?**
- Static methods for role get/set/toggle
- Constants for role values
- Timestamp tracking when role changed

#### **Why?**
- **Single Source of Truth:** All role references use RoleManager
- **Centralization:** Easy to change persistence logic later
- **Type Safety:** Constants prevent typos

#### **How?**

```java
public class RoleManager {
    private static final String PREF_NAME = "AppPrefs";
    private static final String KEY_ROLE = "user_role";

    public static final String ROLE_SEEKER = "SEEKER";
    public static final String ROLE_PROVIDER = "PROVIDER";

    public static String getRole(Context context) {
        SharedPreferences prefs = 
            context.getSharedPreferences(
                PREF_NAME, 
                Context.MODE_PRIVATE);
        return prefs.getString(KEY_ROLE, 
            ROLE_SEEKER);  // Default to SEEKER
    }

    public static void setRole(Context context, 
        String role) {
        SharedPreferences prefs = 
            context.getSharedPreferences(
                PREF_NAME, 
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = 
            prefs.edit();
        editor.putString(KEY_ROLE, role);
        editor.putLong(KEY_ROLE_CHANGED_AT, 
            System.currentTimeMillis());
        editor.apply();  // Asynchronous write
    }

    public static boolean isProvider(
        Context context) {
        return ROLE_PROVIDER.equals(
            getRole(context));
    }

    public static boolean isSeeker(
        Context context) {
        return ROLE_SEEKER.equals(
            getRole(context));
    }

    public static void toggleRole(
        Context context) {
        String currentRole = getRole(context);
        String newRole = ROLE_SEEKER.equals(
            currentRole) ? ROLE_PROVIDER : 
            ROLE_SEEKER;
        setRole(context, newRole);
    }
}
```

### **Usage Throughout App**

```java
// Check role before showing UI
if (RoleManager.isProvider(this)) {
    // Show provider-specific features
}

// Set role during onboarding
RoleManager.setRole(this, RoleManager.ROLE_PROVIDER);

// Switch between modes
RoleManager.toggleRole(this);
```

### **Viva Q&A**

**Q: Why use static methods instead of singleton?**
```
A: Static methods are simpler for a utility class. Singleton adds overhead. 
For RoleManager (stateless), statics are idiomatic Android.
```

**Q: What does apply() do vs. commit()?**
```
A: commit() writes synchronously (blocks main thread, rare)
   apply() writes asynchronously (queued, doesn't block)
   
Always use apply() to avoid ANR errors.
```

---

## 2️⃣1️⃣ UserPrefs.java

### **Purpose**
SharedPreferences wrapper for profile-related data (name, location, photo, verified status).

### **Key Concepts**

#### **What?**
- Getter/setter methods for profile fields
- Prefix all keys with "profile_" to avoid conflicts
- One-line calls: `UserPrefs.getName(context)`

#### **Why?**
- **Encapsulation:** Hides SharedPreferences complexity
- **Consistency:** All profile prefs use same pattern
- **Easy Refactor:** Change storage (e.g., to Room database) without updating call sites

#### **How?**

```java
public final class UserPrefs {
    private static final String PREFS = 
        "UserProfile";
    public static final String KEY_NAME = 
        "profile_name";
    public static final String KEY_PHOTO_URI = 
        "profile_photo_uri";
    public static final String KEY_LOCATION = 
        "profile_location";
    public static final String KEY_VERIFIED = 
        "profile_verified";

    private UserPrefs() {}  // Prevent instantiation

    private static SharedPreferences prefs(
        Context ctx) {
        return ctx.getApplicationContext()
            .getSharedPreferences(PREFS, 
                Context.MODE_PRIVATE);
    }

    public static void saveName(Context ctx, 
        String name) {
        prefs(ctx).edit()
            .putString(KEY_NAME, name)
            .apply();
    }

    public static String getName(Context ctx) {
        return prefs(ctx).getString(
            KEY_NAME, "");
    }

    // ... (similar pattern for other fields)
}
```

### **Usage**

```java
// Save
UserPrefs.saveName(this, "John Doe");
UserPrefs.saveLocation(this, "Mumbai, India");

// Retrieve
String name = UserPrefs.getName(this);
boolean isVerified = UserPrefs.isVerified(this);
```

### **Viva Q&A**

**Q: Why make UserPrefs final?**
```
A: Prevents subclassing. Since all methods are static, there's no reason 
to extend this class. Making it final protects the design intent.
```

---

## 2️⃣2️⃣ VerifiedBadgeHelper.java

### **Purpose**
Attaches or removes a verified badge (✓ icon) on any TextView.

### **Key Concepts**

#### **What?**
- Static method `apply(context, textView, isVerified)`
- Uses compound drawables (right side of text)
- Handles density for scaling icon size

#### **Why?**
- **Reusable:** Can apply badge to any TextView
- **Consistent:** Same badge used everywhere
- **Scalable:** Automatically sizes based on device density

#### **How?**

```java
public final class VerifiedBadgeHelper {
    private static final int BADGE_DP = 16;
    private static final int PADDING_DP = 4;

    private VerifiedBadgeHelper() {}

    public static void apply(Context ctx, 
        TextView tv, boolean isVerified) {
        if (tv == null) return;
        
        if (!isVerified) {
            // Remove badge
            tv.setCompoundDrawablesRelative(
                null, null, null, null);
            return;
        }

        // Get badge icon
        Drawable badge = 
            ContextCompat.getDrawable(ctx, 
                R.drawable.ic_verified_small);
        if (badge == null) return;

        // Scale to density
        float density = 
            ctx.getResources()
                .getDisplayMetrics().density;
        int sizePx = Math.round(
            BADGE_DP * density);
        int padPx = Math.round(
            PADDING_DP * density);

        // Set bounds for drawable
        badge.setBounds(0, 0, sizePx, sizePx);

        // Add padding between text and icon
        tv.setCompoundDrawablePadding(padPx);

        // Place icon on right side
        tv.setCompoundDrawablesRelative(
            null,      // left
            null,      // top
            badge,     // right
            null);     // bottom
    }
}
```

### **Usage**

```java
// Add badge if verified
VerifiedBadgeHelper.apply(this, tvName, 
    user.isVerified());

// Remove badge if not verified
VerifiedBadgeHelper.apply(this, tvName, false);

// Safe to call multiple times
VerifiedBadgeHelper.apply(this, tvName, 
    currentVerificationStatus);
```

### **Compound Drawables Explained**

```
TextView Layout with Compound Drawables:

┌─────────────────────────────────────────────┐
│  [icon]  "John Doe"  [badge_icon]  [icon]   │
│   left      text       right       bottom    │
└─────────────────────────────────────────────┘

setCompoundDrawablesRelative(left, top, right, bottom)
```

### **Viva Q&A**

**Q: Why use density instead of hard-coded pixels?**
```
A: Screens have different DPI (dots per inch):
   - Low DPI (ldpi): 120 dpi
   - Medium DPI (mdpi): 160 dpi (baseline)
   - High DPI (hdpi): 240 dpi
   - Extra High DPI (xhdpi): 320 dpi

A 16px badge looks tiny on high-DPI screens. 
Multiplying by density ensures consistent visual size across devices.

Example: 16dp * 2.0 (xhdpi) = 32 pixels on screen
```

---

# KEY VIVA CONCEPTS

## **Concept 1: Firebase Auth State Persistence**

### **Question**
"How does the app automatically log users in after app restart without storing passwords?"

### **Answer**

Firebase Auth stores auth state locally in secure storage. When `onCreate()` checks `FirebaseAuth.getInstance().getCurrentUser()`:

```java
FirebaseUser user = 
    FirebaseAuth.getInstance()
        .getCurrentUser();

if (user != null) {
    // User is already authenticated
    // Navigate to home based on role
    Intent intent = new Intent(this, 
        MainActivity.class);
    startActivity(intent);
} else {
    // User is not authenticated
    // Navigate to login
    Intent intent = new Intent(this, 
        WelcomeActivity.class);
    startActivity(intent);
}
```

**Firebase does the heavy lifting:**
1. Stores auth tokens securely (not passwords)
2. Refreshes expired tokens automatically
3. Persists user ID across sessions
4. Clears on logout

---

## **Concept 2: Local Image Processing with Google ML Kit**

### **Question**
"How does the app verify IDs locally without sending images to a server?"

### **Answer**

Google ML Kit runs TextRecognition (OCR) on-device:

```java
TextRecognizer recognizer = 
    TextRecognition.getClient(
        TextRecognizerOptions.DEFAULT_OPTIONS);

InputImage image = 
    InputImage.fromFilePath(context, imageUri);

recognizer.process(image)
    .addOnSuccessListener(visionText -> {
        String extractedText = 
            visionText.getText();
        
        // Validate ID structure
        if (extractedText.contains("ID") || 
            extractedText.matches(".*\\d{6,}.*")) {
            // ID appears valid
        }
    });
```

**Advantages:**
- **Privacy:** Images never leave the device
- **Speed:** No network latency
- **Cost:** No API charges
- **Offline:** Works without network

**Limitation:**
- Cannot detect fraud (morphed documents, fakes)
- Only validates text presence

---

## **Concept 3: Navigation Flags Prevent Auth Backdoor**

### **Question**
"How do we prevent users from back-pressing into login screens after authentication?"

### **Answer**

Use `FLAG_ACTIVITY_CLEAR_TASK`:

```java
Intent intent = new Intent(this, 
    HomeProviderActivity.class);

// Clear entire task stack
intent.addFlags(
    Intent.FLAG_ACTIVITY_NEW_TASK | 
    Intent.FLAG_ACTIVITY_CLEAR_TASK);

startActivity(intent);
```

**What this does:**

```
Before:
[LoginActivity] → [OtpEnterActivity] → [OtpVerifyActivity]
                                             ↓ (startActivity)

After with CLEAR_TASK:
[HomeProviderActivity]

(All previous activities are destroyed)

User presses back → App exits
(No navigation back to login)
```

---

## **Concept 4: SharedPreferences vs. Firestore**

### **Question**
"Why store data in both SharedPreferences and Firestore?"

### **Answer**

**SharedPreferences (Local, Fast)**
- Used by RoleManager and UserPrefs
- Instant reads (no network)
- Persists across app restarts
- Gives UI instant display (skeleton screens)

**Firestore (Cloud, Source of Truth)**
- Real-time sync across devices
- Backup if local data is lost
- Enables cross-device sign-in
- Server-side validation

**Pattern:**
```java
// Load local cache first
applyLocalCache();  // Instant display

// Listen to Firestore
onStart() {
    addSnapshotListener(...);  // Real-time updates
}
```

---

## **Concept 5: Real-time Listeners and Memory Leaks**

### **Question**
"How do we prevent memory leaks from Firestore listeners?"

### **Answer**

**Problem:** If listener is never removed, it keeps reference to destroyed Activity, preventing garbage collection.

**Solution:** Remove listener in `onStop()` or `onDestroy()`:

```java
private ListenerRegistration profileListener;

@Override
protected void onStart() {
    super.onStart();
    profileListener = 
        FirebaseFirestore.getInstance()
            .collection("Users")
            .document(userId)
            .addSnapshotListener((snapshot, error) -> {
                // Update UI
            });
}

@Override
protected void onStop() {
    super.onStop();
    if (profileListener != null) {
        profileListener.remove();  // Detach listener
        profileListener = null;
    }
}
```

**Lifecycle:**
```
onStart() → Listener attached → [Updates fire as data changes]
                                       ↓
                                   onStop() → Listener removed → [No more updates]
                                   Activity destroyed → No memory leak
```

---

## **Concept 6: TextWatcher for Real-time Validation**

### **Question**
"How do we provide real-time feedback as users type?"

### **Answer**

```java
etNewPassword.addTextChangedListener(
    new TextWatcher() {
        @Override
        public void beforeTextChanged(
            CharSequence s, int start, 
            int count, int after) {
            // Called before text changes
        }

        @Override
        public void onTextChanged(
            CharSequence s, int start, 
            int before, int count) {
            // Called as text changes (use this)
            validatePasswordRealTime(s.toString());
        }

        @Override
        public void afterTextChanged(
            Editable s) {
            // Called after text changes
        }
    });

private void validatePasswordRealTime(
    String password) {
    // Check requirements
    // Update UI elements
}
```

---

## **Concept 7: Intent Extras for Data Passing**

### **Question**
"How do we pass data between Activities?"

### **Answer**

```java
// In source Activity
Intent intent = new Intent(this, 
    PersonProfileActivity.class);
intent.putExtra("PERSON_USER_ID", userId);
intent.putExtra("PERSON_NAME", "John Doe");
intent.putExtra("IS_VERIFIED", true);
startActivity(intent);

// In destination Activity
String personUserId = 
    getIntent().getStringExtra(
        "PERSON_USER_ID");
String name = 
    getIntent().getStringExtra(
        "PERSON_NAME");
boolean verified = 
    getIntent().getBooleanExtra(
        "IS_VERIFIED", false);
```

---

## **Concept 8: Glide for Image Loading**

### **Question**
"How does the app load images from both local storage and remote URLs?"

### **Answer**

Glide auto-detects URI scheme:

```java
// Local (content://)
Glide.with(this)
    .load("content://com.example/image.jpg")
    .circleCrop()
    .into(imageView);

// Remote (https://)
Glide.with(this)
    .load("https://firebasestorage.../image.jpg")
    .circleCrop()
    .into(imageView);

// File path
Glide.with(this)
    .load(new File("/sdcard/image.jpg"))
    .circleCrop()
    .into(imageView);
```

---

# DATABASE SCHEMA

## **Firestore Collection: users/{userId}**

```json
{
  "userId": "auth_uid_here",
  "fullName": "John Doe",
  "email": "john@example.com",
  "phone": "+91-9876543210",
  "gender": "Male",
  "role": "PROVIDER",
  "address": "Mumbai, India",
  "latitude": 19.0760,
  "longitude": 72.8777,
  "photoUrl": "https://firebasestorage.../photo.jpg",
  "bio": "Experienced plumber with 5 years background",
  "isVerified": true,
  "idFrontUrl": "https://firebasestorage.../id_front.jpg",
  "idBackUrl": "https://firebasestorage.../id_back.jpg",
  "experience": "5 years",
  "rating": 4.8,
  "reviewCount": 150,
  "createdAt": 1704067200000,
  "updatedAt": 1704067200000
}
```

## **SharedPreferences: AppPrefs**

```
Key: user_role
Value: "PROVIDER"

Key: role_changed_at
Value: 1704067200000
```

## **SharedPreferences: UserProfile**

```
Key: profile_name
Value: "John Doe"

Key: profile_location
Value: "Mumbai, India"

Key: profile_photo_uri
Value: "content://com.example/image.jpg"

Key: profile_verified
Value: true
```

---

# SUMMARY TABLE

| Page | Purpose | Key Concepts |
|------|---------|--------------|
| LoadingActivity | Splash screen with progress | Animation, Handler, custom transitions |
| WelcomeActivity | Slideshow + Login/SignUp | ViewPager2, runnable, Handler |
| MainActivity | Role-based dispatch | RoleManager, Intent flags |
| OtpEnterActivity | Phone auth | Firebase PhoneAuthProvider, regex validation |
| CreateNewPasswordActivity | Password setup | TextWatcher, regex patterns, real-time validation |
| AccountTypeActivity | Role selection | Persistent SharedPreferences, Firestore sync |
| ProfileSetupActivity | Location collection | FusedLocationProviderClient, Geocoding API, debouncing |
| IdVerificationActivity | ID upload & validation | ML Kit TextRecognition, image picker, SpannableString |
| IdVerifiedActivity | Success screen | Simple transition |
| ProfileSuccessActivity | Onboarding complete | Pop animation, OvershootInterpolator |
| ProfileActivity | View own profile | Real-time Firestore listener, local cache, Glide |
| PersonProfileActivity | View other's profile | Intent extras + Firestore sync, verified badge |
| EditProfileActivity | Edit bio + photo | ActivityResultLauncher, Firebase Storage upload, character counter |
| RoleManager | Role persistence | SharedPreferences wrapper, static methods |
| UserPrefs | Profile data cache | SharedPreferences wrapper, encapsulation |
| VerifiedBadgeHelper | Badge attachment | Compound drawables, density scaling |

---

## **Final Notes for Viva**

1. **Architecture:** Two-tier (local cache + cloud sync) ensures fast UI and always-fresh data
2. **Security:** Passwords never stored locally, ID images processed on-device
3. **UX:** Debouncing, real-time validation, animations, and instant local cache display
4. **Code Quality:** Utility classes (RoleManager, UserPrefs, VerifiedBadgeHelper) for reusability
5. **Firebase:** Auth (no password storage), Firestore (real-time sync), Storage (image hosting)
6. **Permissions:** Graceful handling with ActivityResultContracts
7. **Memory:** Listener cleanup in onStop() prevents leaks
8. **Navigation:** Intent flags prevent navigation backdoors

---

**Good luck with your viva! 🎓**
