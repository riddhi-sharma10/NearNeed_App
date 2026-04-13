# NearNeed2 Viva Guide - Complete Onboarding Flow
## PART 3: BASIC INFO → LOCATION → ACCOUNT TYPE

---

## **SCREEN 6: BASIC INFO - ProfileInfoActivity**

### **1. SCREEN PURPOSE**
- **Why it exists:** SIGNUP-ONLY screen - collects foundational user information
- **What happens:** User uploads profile picture, enters name/bio/DOB, and selects gender
- **User action:** Fill profile info → Click "Continue" → Go to location setup

**Why only for signup users?** Login users skip this because they already provided info during their initial signup.

### **2. JAVA LOGIC EXPLANATION**

**Modern Image Picking with ActivityResultContracts:**
```java
// This replaces deprecated startActivityForResult()
private ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
    registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
        // This callback fires when user selects an image
        if (uri != null) {
            ivProfilePicture.setImageURI(uri);  // Display selected image
        }
    });

// When user clicks profile photo area:
flProfilePhoto.setOnClickListener(v -> {
    pickMedia.launch(new PickVisualMediaRequest.Builder()
        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
        .build());
    // Opens device's native image picker, filters to images only
});
```

**Real-Time Bio Character Counter:**
```java
etBio.addTextChangedListener(new TextWatcher() {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Called before changes - don't need this
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Called while text is being changed - update counter in real-time
        int length = s != null ? s.length() : 0;
        tvBioCount.setText(length + "/150");  // Shows: "23/150"
    }

    @Override
    public void afterTextChanged(Editable s) {
        // Called after text fully changed - don't need this
    }
});
```

**DatePicker Integration:**
```java
etDob.setOnClickListener(v -> showDatePicker());

private void showDatePicker() {
    Calendar calendar = Calendar.getInstance();  // Current date/time
    int year = calendar.get(Calendar.YEAR);      // 2024
    int month = calendar.get(Calendar.MONTH);    // 0-11 (January = 0)
    int day = calendar.get(Calendar.DAY_OF_MONTH);

    // Open date picker dialog
    DatePickerDialog datePickerDialog = new DatePickerDialog(
        this,
        (view, year1, month1, dayOfMonth) -> {
            // User selected a date
            String date = String.format(Locale.getDefault(), 
                "%02d/%02d/%04d",  // Format: 15/03/2024
                dayOfMonth, 
                month1 + 1,        // month1 is 0-11, so add 1 for display
                year1);
            etDob.setText(date);
        },
        year, month, day
    );
    datePickerDialog.show();
}
```

**Smart Scroll to Show Bio Field:**
```java
etBio.setOnFocusChangeListener((v, hasFocus) -> {
    if (hasFocus) {  // When bio field gets focus (user taps it)
        // Delay by 300ms to let keyboard appear first
        v.postDelayed(() -> {
            scrollView.smoothScrollTo(0, etBio.getBottom() + 100);
            // Scroll view so bio field is visible above keyboard
        }, 300);
    }
});
```

**Validation & Navigation:**
```java
btnContinue.setOnClickListener(v -> {
    String name = etFullName.getText().toString().trim();
    
    // Only require name to be filled
    if (name.isEmpty()) {
        Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
        return;
    }
    
    // All validation passed - proceed to location setup
    Intent intent = new Intent(ProfileInfoActivity.this, ProfileSetupActivity.class);
    startActivity(intent);
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
});
```

### **3. DESIGN DECISION REASONING**

**Why ActivityResultContracts instead of startActivityForResult()?**

| Approach | Pros | Cons | Status |
|----------|------|------|--------|
| **ActivityResultContracts** | Type-safe, no request codes, modern | Slightly verbose | CHOSEN ✓ (Android 11+) |
| startActivityForResult() | Familiar, works everywhere | Deprecated since Android 11, error-prone | Old approach |
| Custom file picker | Full control | Lots of boilerplate | Over-engineering |

**Why real-time bio counter?**
- Shows users how much space they have left (150 char limit)
- Prevents surprise errors ("character limit exceeded")
- Encourages concise bios
- No submit validation needed for bio length (counter prevents overflow)

**Why this order: Photo → Name → Bio → DOB → Gender?**
- Photo first: most engaging, visual feedback immediately
- Name next: most important field
- Bio: optional, lower priority
- DOB: important but less frequent to change
- Gender: least critical, can be skipped

**Why only name is required?**
- Photo, bio, DOB, gender are optional for UX fluidity
- Minimum viable profile: just a name
- User can complete profile later if desired
- Reduces friction in signup flow

**Why DatePickerDialog instead of text input?**
- Prevents invalid dates (Feb 30th, etc.)
- Native Android behavior - users expect this
- Accessible for users with visual impairments
- Reduces typos compared to manual typing

### **4. DATA FLOW**

```
OtpVerifyActivity (IS_SIGNUP=true)
    ↓
ProfileInfoActivity → User fills:
    • Profile picture (optional)
    • Full name (required)
    • Bio (optional, 150 char limit)
    • Date of birth (optional)
    • Gender (optional)
    ↓
Click Continue (only name required)
    ↓
Intent → ProfileSetupActivity (Location)
```

**Data storage:** None at this point. Data would be sent to backend after completing entire signup flow or saved to SharedPreferences for later submission.

### **5. EDGE CASE HANDLING**

| Edge Case | Behavior | Code |
|-----------|----------|------|
| User doesn't select profile photo | Defaults to placeholder image | `if (uri != null)` - only updates if selected |
| User enters 200 chars in bio | Counter shows "200/150" | Allowed (no hard limit in code) - could add max validation |
| User selects future date as DOB | Accepted (no validation) | **BUG:** Should reject future dates |
| Keyboard covers bio input | Auto-scrolls bio field up | `scrollView.smoothScrollTo()` handles this |
| User types name then deletes all | Empty string fails validation | Toast shows, user required to re-enter |
| User rotates phone mid-input | Data lost (no Bundle save) | **BUG:** Should save state in `onSaveInstanceState()` |
| User navigates back to OTP | Returns to OtpVerifyActivity | Loses unsaved profile data |

### **6. VIVA QUESTIONS & ANSWERS**

**Q: Why use ActivityResultContracts instead of the older startActivityForResult()?**
A: "ActivityResultContracts is the modern, type-safe approach recommended by Google since Android 11. startActivityForResult() requires managing request codes and is error-prone. ActivityResultContracts handles all that automatically and is clearer to read."

**Q: Explain the real-time bio counter logic.**
A: "A TextWatcher monitors the bio field. Every time text changes, the onTextChanged() method fires and we update the counter to show current length out of 150. User sees real-time feedback (e.g., '45/150'). This prevents surprise 'character limit exceeded' errors."

**Q: Why does the date picker add 1 to month1?**
A: "DatePickerDialog returns month as 0-11 (0=January, 11=December), but users expect 1-12. So we add 1 when displaying to the user. If user selected March (2), we show 03 in the date field, not 02."

**Q: What happens if user enters name, then deletes it and tries to continue?**
A: "The validation check `if (name.isEmpty())` catches it and shows a Toast message. The button click doesn't proceed to the next screen. User must re-enter the name."

**Q: Why only require name and not other fields?**
A: "To reduce friction in the signup flow. Profile photo and bio are nice-to-haves that enhance the profile but aren't essential to start using the app. Name is required because it's needed for identification and messaging other users. This balances completeness with user convenience."

**Q: How would you improve this screen?**
A: "(1) Add date validation - reject future dates, (2) Add bio char limit enforcement (disable typing at 150), (3) Save form state if user navigates away, (4) Add profile photo crop tool, (5) Cache profile photo URI in case of rotation, (6) Add optional fields labels so users know they can skip them, (7) Email verification if email was provided during signup."

---

## **SCREEN 7: LOCATION - ProfileSetupActivity**

### **1. SCREEN PURPOSE**
- **Why it exists:** SIGNUP-ONLY - collects user's work location for matching with jobs/services nearby
- **What happens:** User clicks "Detect Location" button, simulates GPS triangulation, displays detected location
- **User action:** Click "Detect Location" → See simulated detection → Click "Continue"

### **2. JAVA LOGIC EXPLANATION**

**Location Detection Simulation:**
```java
public class ProfileSetupActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private MaterialButton btnContinue;
    private MaterialButton btnDetectLocation;
    private TextView tvDetectedLocation;
    private int selectedRadius = 10;  // Stored but not used in current UI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnContinue = findViewById(R.id.btnContinue);
        btnDetectLocation = findViewById(R.id.btnDetectLocation);
        tvDetectedLocation = findViewById(R.id.tvDetectedLocation);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(this, IdVerificationActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        btnDetectLocation.setOnClickListener(v -> simulateLocationDetection());
    }
}
```

**Simulated Async Location Detection:**
```java
private void simulateLocationDetection() {
    // Show feedback to user
    Toast.makeText(this, "Requesting location permission...", Toast.LENGTH_SHORT).show();

    // Disable button and show initial state
    btnDetectLocation.setEnabled(false);
    btnDetectLocation.setText("Fetching Precise Location...");
    tvDetectedLocation.setText("Detecting...");
    tvDetectedLocation.setTextColor(0xFF64748B);  // Gray color

    // PHASE 1: Simulate initial detection (1 second)
    new Handler().postDelayed(() -> {
        tvDetectedLocation.setText("Triangulating GPS...");
        
        // PHASE 2: Simulate triangulation (1.5 seconds after phase 1)
        new Handler().postDelayed(() -> {
            // Final location result
            String detected = "BML Munjal University, Kaphera";
            tvDetectedLocation.setText(detected);
            tvDetectedLocation.setTextColor(0xFF0F172A);  // Dark color when done

            // Change button to show success
            btnDetectLocation.setText("Location Confirmed");
            btnDetectLocation.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0xFF16A34A)  // Green
            );
            btnDetectLocation.setTextColor(android.graphics.Color.WHITE);

            Toast.makeText(this, "Location set to: " + detected, Toast.LENGTH_SHORT).show();
        }, 1500);  // 1.5 seconds for triangulation phase
    }, 1000);  // 1 second for initial detection phase
}
```

**Timeline visualization:**
```
0ms:     User clicks "Detect Location"
         Button disabled, shows "Fetching Precise Location..."
         Text shows "Detecting..." (gray)
         
1000ms:  Text updates to "Triangulating GPS..."
         
2500ms:  Text shows final location (dark color)
         Button becomes green, text "Location Confirmed"
         Toast confirms location is set
```

### **3. DESIGN DECISION REASONING**

**Why simulate location instead of using real GPS?**

| Approach | Use | Why Chosen |
|----------|-----|-----------|
| **Simulated** | Demo, testing, no permission issues | CHOSEN ✓ - For viva/demo purposes |
| Real GPS (LocationManager) | Production | Needs permissions, takes time, may fail |
| FusedLocationProvider | Production | Better accuracy, requires Play Services |

**Why multi-phase animation?**
```
Single "Loading..." feels boring and unclear
Multi-phase "Detecting..." → "Triangulating..." → "Done" creates:
    ✓ Visual feedback of progress
    ✓ Sense that something is happening
    ✓ More polished UX
```

**Why change button color to green?**
- Green = success/completion (universal UI language)
- User instantly knows location detection succeeded
- No need to read text, visual cue is enough

**Why disable button during detection?**
- Prevents multiple simultaneous detection attempts
- User understands process is running
- Avoids race conditions in real implementation

### **4. DATA FLOW**

```
ProfileInfoActivity (user has filled basic info)
    ↓
ProfileSetupActivity → User clicks "Detect Location"
    ↓
Simulates 2.5 second detection:
    • 1s: "Detecting..."
    • +1.5s: "Triangulating..."
    • Final: "Location Confirmed"
    ↓
User clicks Continue
    ↓
Intent → IdVerificationActivity
```

**Data storage:** Location is displayed but not saved anywhere in this code. In production, would save to SharedPreferences or database.

### **5. EDGE CASE HANDLING**

| Edge Case | Behavior | Code |
|-----------|----------|------|
| User clicks button multiple times | Second click is ignored (button disabled) | `btnDetectLocation.setEnabled(false)` |
| User navigates away during detection | Handler continues executing | **BUG:** Should cancel Handler in onDestroy() |
| User rotates phone mid-detection | Animation might reset | **BUG:** No savedInstanceState handling |
| Continue clicked before detection done | Proceeds anyway (no validation) | **DESIGN:** Should require detection first |
| Handler executes after activity destroyed | Potential crash | **BUG:** Should use WeakReference or cancel in onDestroy() |

### **6. VIVA QUESTIONS & ANSWERS**

**Q: Why simulate location detection instead of using real GPS?**
A: "For a demo/viva environment, simulation is perfect because: (1) No need for permissions, (2) Reliable and instant, (3) Shows the UX flow clearly. In production, we'd use FusedLocationProviderClient for accuracy or LocationManager for device location. Simulation demonstrates the concept without production dependencies."

**Q: Explain the two-phase Handler approach.**
A: "We use nested Handlers to create a realistic detection timeline. First Handler waits 1 second showing 'Detecting...', then the nested Handler waits another 1.5 seconds showing 'Triangulating...'. This creates the illusion of real GPS triangulation. Without phases, the flow would feel instant and less convincing."

**Q: Why is the button disabled during detection?**
A: "To prevent race conditions. If user clicks multiple times, we'd start multiple detection sequences. By disabling the button, we ensure only one detection runs. In production, this prevents multiple API calls for the same data."

**Q: What's the purpose of changing button color to green?**
A: "Color is a visual cue for status. Green universally means success/complete. Users instantly recognize 'oh, the location detection finished' without reading the text. It's an accessibility feature and good UX design."

**Q: What would happen if user navigates back during detection?**
A: "The Handler callbacks would still fire even after the activity is destroyed. This could cause crashes when trying to update views. Should fix by storing Handler references and calling `removeCallbacks()` in `onDestroy()`."

**Q: How would you improve this screen?**
A: "(1) Use real LocationManager or FusedLocationProvider for production, (2) Cancel Handler in onDestroy(), (3) Request location permissions properly, (4) Save location to SharedPreferences, (5) Show location on map for verification, (6) Allow manual location entry as fallback, (7) Require location confirmation before continuing, (8) Add retry button if detection fails."

---

## **SCREEN 8: ACCOUNT TYPE SELECTION - AccountTypeActivity**

### **1. SCREEN PURPOSE**
- **Why it exists:** User chooses their role - determines entire app experience and future flow
- **What happens:** Two options presented (Seeker vs Provider). User selects one, role is saved, flow branches
- **User action:** Click Seeker card OR Provider card → Role saved → Navigate to next screen

### **2. JAVA LOGIC EXPLANATION**

**Role Selection & Persistent Storage:**
```java
public class AccountTypeActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private MaterialCardView cardSeeker, cardProvider;
    private MaterialButton btnSeekerAction, btnProviderAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_type);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        cardSeeker = findViewById(R.id.cardSeeker);
        cardProvider = findViewById(R.id.cardProvider);
        btnSeekerAction = findViewById(R.id.btnSeekerAction);
        btnProviderAction = findViewById(R.id.btnProviderAction);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        // Both card AND button can trigger seeker selection
        cardSeeker.setOnClickListener(v -> completeRegistration("seeker"));
        btnSeekerAction.setOnClickListener(v -> completeRegistration("seeker"));
        
        // Both card AND button can trigger provider selection
        cardProvider.setOnClickListener(v -> completeRegistration("provider"));
        btnProviderAction.setOnClickListener(v -> completeRegistration("provider"));
    }

    private void completeRegistration(String role) {
        if ("seeker".equals(role)) {
            // Save role to persistent storage
            RoleManager.setRole(this, RoleManager.ROLE_SEEKER);
            
            // Route to seeker home
            Intent intent = new Intent(this, HomeSeekerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            // Provider path
            RoleManager.setRole(this, RoleManager.ROLE_PROVIDER);
            
            // Route to community preferences (provider-specific)
            Intent intent = new Intent(this, CommunityPreferencesActivity.class);
            intent.putExtra("USER_ROLE", role);
            startActivity(intent);
        }
    }
}
```

**RoleManager - Persistent Storage:**
```java
public class RoleManager {
    private static final String PREF_NAME = "AppPrefs";
    private static final String KEY_ROLE = "user_role";
    
    public static final String ROLE_SEEKER = "SEEKER";
    public static final String ROLE_PROVIDER = "PROVIDER";

    public static String getRole(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        // Returns saved role, or defaults to SEEKER if never set
        return prefs.getString(KEY_ROLE, ROLE_SEEKER);
    }

    public static void setRole(Context context, String role) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
            .putString(KEY_ROLE, role)
            .apply();  // Persists to device storage
    }
}
```

**Understanding the flow split:**
```java
// SEEKER PATH
if ("seeker".equals(role)) {
    RoleManager.setRole(this, RoleManager.ROLE_SEEKER);
    Intent intent = new Intent(this, HomeSeekerActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    // No more screens - goes directly to home
}

// PROVIDER PATH
else {
    RoleManager.setRole(this, RoleManager.ROLE_PROVIDER);
    Intent intent = new Intent(this, CommunityPreferencesActivity.class);
    intent.putExtra("USER_ROLE", role);
    startActivity(intent);
    // Has additional screens: CommunityPreferences → ProviderDetails → IDVerify
}
```

### **3. DESIGN DECISION REASONING**

**Why does the flow diverge here?**

```
SEEKER (looking for services):
    → Minimal additional setup needed
    → Just go to home to browse services
    → Profile can be completed gradually
    
PROVIDER (offering services):
    → Significant setup required
    → Must specify skills, availability, schedule, etc.
    → Must verify identity
    → Can't post jobs until setup complete
```

**Why use both card AND button?**
- Card click: Large touch target for mobile
- Button click: Clear call-to-action
- Redundancy: Either way works, user choice
- Visual clarity: Card provides context, button is explicit action

**Why RoleManager instead of just SharedPreferences.putString()?**

| Approach | Pros | Cons | Choice |
|----------|------|------|--------|
| **RoleManager** | Single source of truth, reusable, testable | Small abstraction layer | CHOSEN ✓ |
| Direct SharedPreferences | Simpler, no abstraction | Scattered across code, hard to maintain | Doesn't scale |

**Why separate intent flags for seekers?**
```java
intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
```
- Clears entire activity stack
- Seeker goes directly to home (no back to signup)
- Prevents: AccountType ← OtpVerify ← OtpEnter ← Welcome

**Why put role in intent for providers?**
```java
intent.putExtra("USER_ROLE", role);
```
- CommunityPreferencesActivity needs to know role to decide next screen
- Could also use RoleManager.getRole() but explicit intent extra is clearer

### **4. DATA FLOW**

```
ProfileSetupActivity (location detected)
    → IdVerificationActivity (only for signup)
    → CommunityPreferencesActivity (skill selection)
    ↓
AccountTypeActivity (user chooses role)
    ↓
If SEEKER:
    • RoleManager saves ROLE_SEEKER to SharedPreferences
    • Intent → HomeSeekerActivity
    • Stack cleared (no back to signup)
    ↓
If PROVIDER:
    • RoleManager saves ROLE_PROVIDER to SharedPreferences
    • Intent → CommunityPreferencesActivity (with USER_ROLE extra)
    • Continues provider-specific flow
```

**Data Persistence:**
- Role saved to SharedPreferences with key "user_role"
- Survives app restart
- Retrieved by MainActivity on next launch to route correctly

### **5. EDGE CASE HANDLING**

| Edge Case | Behavior | Code |
|-----------|----------|------|
| User rapidly clicks both cards | Multiple intents might fire | **BUG:** No debouncing, could have race condition |
| User chooses provider, then changes mind | Role already saved | Would need "Edit Account Type" option later |
| SharedPreferences write fails | Role not saved, user routed to seeker | **BUG:** No error handling, silent failure |
| Device storage full | putString() might fail silently | **BUG:** No exception handling |
| User navigates back to this screen | Returns to Community Preferences | Goal: make this unreachable once role set |

### **6. VIVA QUESTIONS & ANSWERS**

**Q: Why does the provider flow have more screens than seeker?**
A: "Providers need more setup: they must specify which skills/services they offer, their availability schedule, pricing, and verify their identity. This prevents low-quality or fraudulent providers. Seekers are consumers looking for services - they just need basic profile info and can browse immediately. The risk profiles are different, so setup complexity differs."

**Q: Why use RoleManager instead of calling SharedPreferences directly?**
A: "RoleManager is an abstraction layer - it centralizes role management. Benefits: (1) If we change storage from SharedPreferences to a database, we change it in one place. (2) We can add role validation logic here. (3) Reusable across the entire app - MainActivity, settings, etc. (4) Easier to test - can mock RoleManager. Direct SharedPreferences calls would scatter the storage logic everywhere."

**Q: Explain the two different navigation patterns - seeker vs provider.**
A: "Seekers go directly to HomeSeekerActivity with FLAG_ACTIVITY_CLEAR_TASK - this clears the entire signup stack so they can't go back. Providers go to CommunityPreferencesActivity to continue setup with the USER_ROLE extra. This is because seekers are 'done' with signup, but providers have more mandatory steps."

**Q: What does `intent.addFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK)` accomplish?**
A: "FLAG_ACTIVITY_NEW_TASK creates a new task if it doesn't exist. FLAG_ACTIVITY_CLEAR_TASK clears all other activities in the task stack. Together, they ensure that the seeker home is the only activity in the stack. If user presses back, they exit the app rather than going back to signup. This is appropriate for seekers because their signup is complete."

**Q: Why is role saved BEFORE navigation?**
A: "If we navigate first, then save role, and the save fails, the user is in the home screen but role wasn't persisted. Next app launch, they'd be routed back to onboarding. By saving first, we ensure role is persisted before proceeding."

**Q: How would you improve this screen?**
A: "(1) Show descriptions of what seeker/provider means, (2) Validate that role was actually saved (check SharedPreferences), (3) Debounce rapid clicks to prevent duplicate intents, (4) Show confirmation dialog before sealing role choice, (5) Add 'Edit Account Type' option in settings if user changes mind later, (6) Log which role was chosen for analytics."

