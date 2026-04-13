# NearNeed2 Viva Guide - Complete Onboarding Flow
## PART 1: SPLASH → WELCOME → SIGNUP/LOGIN → OTP

---

## **SCREEN 1: SPLASH (MainActivity)**

### **1. SCREEN PURPOSE**
- **Why it exists:** Acts as a central dispatcher/router after app launch
- **What happens:** Checks persistent user role and routes to appropriate home screen
- **User action:** No action - automatic routing based on saved preferences

### **2. JAVA LOGIC EXPLANATION**

```java
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dispatchByRole();
    }

    private void dispatchByRole() {
        String role = RoleManager.getRole(this);  // Retrieves from SharedPreferences
        Intent intent;

        if (RoleManager.ROLE_PROVIDER.equals(role)) {
            intent = new Intent(this, HomeProviderActivity.class);
        } else {
            intent = new Intent(this, HomeSeekerActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
```

**Key Logic Points:**
- `RoleManager.getRole()` → Uses SharedPreferences to retrieve saved role
- Intent flags clear the back stack to prevent returning to splash
- Animation provides smooth UX transition
- `finish()` removes this activity from memory after navigation

### **3. DESIGN DECISION REASONING**

**Why Activity instead of Fragment?**
- Single-purpose dispatcher doesn't need fragment lifecycle
- Activities are entry points; this is the app's entry point
- Cleaner separation from home screens

**Why separate dispatcher instead of direct launch?**
- **Flexibility:** If user role changes, MainActivity routes correctly
- **Persistence:** Role survives app restart via SharedPreferences
- **Future scaling:** Easy to add more role types (Admin, Manager, etc.)

**Why this approach over alternatives?**
| Approach | Pros | Cons | Why Not Used |
|----------|------|------|--------------|
| **Dispatcher Activity** | Clear, flexible, reusable | Adds one screen | CHOSEN ✓ |
| Direct to Home | Fewer screens | Hard to handle role changes | No persistence logic |
| SplashScreen API | Material design standard | Requires Android 12+ | Users on older devices |

### **4. DATA FLOW**

```
SharedPreferences (AppPrefs)
    ↓
RoleManager.getRole()
    ↓
MainActivity checks role
    ↓
Routes to HomeProviderActivity OR HomeSeekerActivity
    ↓
Activity stack cleared
```

**No new data collected** - this is purely routing based on existing data.

### **5. EDGE CASE HANDLING**

| Edge Case | What Happens | Code |
|-----------|-------------|------|
| First-time user (no role saved) | Defaults to SEEKER | `getRole()` returns ROLE_SEEKER by default |
| SharedPreferences corrupted | Falls back to SEEKER | Default parameter handles it |
| Role updated elsewhere | Correctly routes to new role | Reads latest value each app launch |

### **6. VIVA QUESTIONS & ANSWERS**

**Q: Why did you create a separate dispatcher instead of going directly to home?**
A: "Because user roles are persistent - once a user is a Provider, they should always land on Provider home even after closing and reopening the app. The dispatcher checks their saved role and routes them correctly. This is scalable: if we add Admin or Manager roles, we just add another condition here."

**Q: What are these Intent flags doing?**
A: "`FLAG_ACTIVITY_NEW_TASK` creates a new task if one doesn't exist, and `FLAG_ACTIVITY_CLEAR_TASK` clears the entire activity stack. Together, they prevent the user from pressing back and returning to the dispatcher. This is important because we don't want users seeing splash screens after they've logged in."

**Q: How does this handle first-time users?**
A: "First-time users haven't had their role set yet, so `getRole()` returns the default value 'SEEKER'. They're routed to the seeker home screen. Once they complete onboarding and select their role, it's saved to SharedPreferences and future launches use that role."

**Q: How would you improve this?**
A: "I could add: (1) Check if user is logged in at all - if not, redirect to Welcome screen instead of home. (2) Show a loading screen while checking role. (3) Add a timeout in case SharedPreferences read fails. (4) Log the routing decision for analytics."

---

## **SCREEN 2: WELCOME ACTIVITY**

### **1. SCREEN PURPOSE**
- **Why it exists:** First screen users see during signup - introduces the app with visual appeal
- **What happens:** Displays auto-rotating carousel of welcome images + two action buttons
- **User action:** Choose between "Login" or "Sign Up"

### **2. JAVA LOGIC EXPLANATION**

**ViewPager2 Setup (Carousel):**
```java
private void setupSlideshow() {
    vpSlideshow = findViewById(R.id.vpSlideshow);
    int[] images = {
        R.drawable.welcome_bg_1,
        R.drawable.welcome_bg_2,
        R.drawable.welcome_bg_3
    };

    SlideshowAdapter adapter = new SlideshowAdapter(images);
    vpSlideshow.setAdapter(adapter);
    
    // Start auto-transition
    slideshowHandler.postDelayed(slideshowRunnable, 3000);
}
```

**Auto-Transition Logic:**
```java
private Runnable slideshowRunnable = new Runnable() {
    @Override
    public void run() {
        if (vpSlideshow != null) {
            currentSlide = (currentSlide + 1) % 3;  // 0→1→2→0→1→2...
            vpSlideshow.setCurrentItem(currentSlide, true);  // true = animate
            slideshowHandler.postDelayed(this, 3000);  // Reschedule in 3s
        }
    }
};
```

**Breaking it down:**
- `currentSlide % 3` → Cycles through slides: 0,1,2,0,1,2,0...
- `setCurrentItem(position, smoothScroll)` → Moves to slide with animation
- `postDelayed(runnable, 3000)` → Calls runnable after 3 seconds
- The runnable reschedules itself = continuous loop

**Button Navigation:**
```java
btnLogin.setOnClickListener(v -> {
    Intent intent = new Intent(WelcomeActivity.this, OtpEnterActivity.class);
    intent.putExtra("IS_SIGNUP", false);  // Flag: this is login
    startActivity(intent);
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
});

btnSignUp.setOnClickListener(v -> {
    Intent intent = new Intent(WelcomeActivity.this, CreateAccountActivity.class);
    intent.putExtra("IS_SIGNUP", true);  // Flag: this is signup
    startActivity(intent);
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
});
```

**Cleanup:**
```java
@Override
protected void onDestroy() {
    super.onDestroy();
    slideshowHandler.removeCallbacks(slideshowRunnable);  // Stop auto-rotation
}
```

**SlideshowAdapter (Inner Class):**
```java
private class SlideshowAdapter extends RecyclerView.Adapter<SlideshowAdapter.ViewHolder> {
    private int[] images;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_welcome_slideshow, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.imageView.setImageResource(images[position]);
    }

    @Override
    public int getItemCount() {
        return images.length;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivSlideshow);
        }
    }
}
```

### **3. DESIGN DECISION REASONING**

**Why ViewPager2 for carousel?**
| Approach | Use Case | Why Not |
|----------|----------|---------|
| **ViewPager2** | Standard carousel, recycled views | CHOSEN ✓ - Material component, efficient |
| ImageView with manual transitions | Single image | Can't swipe, no recycling |
| Fragment transitions | Complex content per slide | Overkill for just images |

**Why separate Login and Signup flows?**
- Login users already exist → Skip profile collection
- Signup users are new → Collect full profile
- `IS_SIGNUP` flag branching determines downstream screens

**Why 3-second auto-rotation?**
- Fast enough to feel dynamic (not boring)
- Slow enough for users to read/appreciate images
- Industry standard for carousels

**Why remove callbacks in onDestroy()?**
- **Memory leak prevention:** Handler keeps reference to runnable
- Without cleanup, runnable continues executing even after activity destroyed
- Causes memory overhead and potential crashes

### **4. DATA FLOW**

```
User sees carousel (auto-rotating every 3s)
    ↓
User clicks "Login" OR "Sign Up"
    ↓
Intent created with IS_SIGNUP flag
    ↓
If Login (IS_SIGNUP=false):
    → OtpEnterActivity
    
If Signup (IS_SIGNUP=true):
    → CreateAccountActivity
```

No data stored at this stage - just routing decision.

### **5. EDGE CASE HANDLING**

| Edge Case | What Happens | Code |
|-----------|-------------|------|
| Activity destroyed mid-carousel | Runnable stops cleanly | `removeCallbacks()` in `onDestroy()` |
| User navigates while animation playing | Animation continues briefly | Safe - checks `if (vpSlideshow != null)` |
| ViewPager2 is null | Carousel stops | Null check prevents crash |
| Rapid button clicks | Multiple intents launched | No debouncing - could improve this |

### **6. VIVA QUESTIONS & ANSWERS**

**Q: Why use ViewPager2 instead of just ImageView?**
A: "ViewPager2 is designed for carousels - it recycles views efficiently even with hundreds of images. ImageView can't handle swiping or pagination. ViewPager2 is the standard component for this pattern in Android."

**Q: Explain the auto-rotation logic. How does it prevent infinite loops?**
A: "The carousel has exactly 3 slides (0, 1, 2). After slide 2, `(2 + 1) % 3` gives 0, so it wraps back to the start. The modulo operator ensures we never exceed 3 slides. Each iteration reschedules itself after 3 seconds, creating a loop. It stops when `onDestroy()` is called and we remove the callbacks."

**Q: What happens if the user is on slide 2 and presses back while carousel is auto-rotating?**
A: "The Activity is destroyed, triggering `onDestroy()`, which calls `slideshowHandler.removeCallbacks(slideshowRunnable)`. This stops the carousel from executing further. Without this cleanup, the runnable would keep running and cause a memory leak."

**Q: Why do you pass IS_SIGNUP flag as an Intent extra?**
A: "Because the two flows diverge after OTP verification. Signup users need to fill in profile info, but login users go directly to account type selection. The IS_SIGNUP flag tells downstream screens which path the user took. It's passed through the entire flow: Welcome → OtpEnter → OtpVerify → (branches based on flag)."

**Q: How would you improve the carousel?**
A: "Add page indicators (dots showing current slide), let users manually swipe between slides, add a pause-on-touch feature (stop auto-rotation when user interacts), and use Fragment instead of simple images for more complex content per slide."

---

## **SCREEN 3: SIGNUP - CreateAccountActivity**

### **1. SCREEN PURPOSE**
- **Why it exists:** New users need to create account credentials (email + password) and provide a name
- **What happens:** Collects full name, email, and password with real-time validation
- **User action:** Fill form → Click "Submit" to proceed to OTP verification

### **2. JAVA LOGIC EXPLANATION**

**View Initialization:**
```java
private void initViews() {
    btnBack = findViewById(R.id.btnBack);
    btnSubmit = findViewById(R.id.btnSubmit);
    etFullName = findViewById(R.id.etFullName);
    etEmail = findViewById(R.id.etEmail);
    etPassword = findViewById(R.id.etPassword);
    tilFullName = findViewById(R.id.tilFullName);  // TextInputLayout for error display
    tilEmail = findViewById(R.id.tilEmail);
    tilPassword = findViewById(R.id.tilPassword);
    tvLogin = findViewById(R.id.tvLogin);
}
```

**Form Validation Logic:**
```java
btnSubmit.setOnClickListener(v -> {
    // Step 1: Get input values
    String name = etFullName.getText().toString().trim();
    String email = etEmail.getText().toString().trim();
    String password = etPassword.getText().toString().trim();

    boolean isValid = true;

    // Step 2: Clear previous errors
    tilFullName.setError(null);
    tilEmail.setError(null);
    tilPassword.setError(null);

    // Step 3: Validate each field
    
    // Name validation
    if (name.isEmpty()) {
        tilFullName.setError("Please enter your name");
        isValid = false;
    }

    // Email validation
    if (email.isEmpty()) {
        tilEmail.setError("Please enter your email");
        isValid = false;
    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        tilEmail.setError("Please enter a valid email");
        isValid = false;
    }

    // Password validation - REGEX EXPLANATION
    if (password.isEmpty()) {
        tilPassword.setError("Please create a password");
        isValid = false;
    } else if (!password.matches("^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[^a-zA-Z0-9]).+$")) {
        /*
        REGEX BREAKDOWN:
        ^ = Start of string
        (?=.*[0-9]) = Positive lookahead: must contain at least one digit 0-9
        (?=.*[a-zA-Z]) = Positive lookahead: must contain at least one letter
        (?=.*[^a-zA-Z0-9]) = Positive lookahead: must contain at least one special char
        .+ = At least one character of any type
        $ = End of string
        
        Example matches: "Pass1!", "Test@123", "Secure#99"
        Example fails: "password" (no digit), "Pass123" (no special char), "P@1" (too short idea)
        */
        tilPassword.setError("Password must be alphanumeric and include at least one special character");
        isValid = false;
    }

    // Step 4: If any validation failed, stop
    if (!isValid) {
        return;
    }

    // Step 5: All valid - proceed to OTP
    Intent intent = new Intent(CreateAccountActivity.this, OtpEnterActivity.class);
    intent.putExtra("IS_SIGNUP", true);
    startActivity(intent);
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
});

// Back button
btnBack.setOnClickListener(v -> onBackPressed());

// "Already have account? Login" link
tvLogin.setOnClickListener(v -> {
    Intent intent = new Intent(CreateAccountActivity.this, OtpEnterActivity.class);
    intent.putExtra("IS_SIGNUP", false);  // Switch to login flow
    startActivity(intent);
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    finish();  // Close signup activity
});
```

### **3. DESIGN DECISION REASONING**

**Why TextInputLayout instead of EditText?**
| Component | Feature | Why Chosen |
|-----------|---------|-----------|
| **TextInputLayout** | Shows errors below, Material design, animated hint | CHOSEN ✓ |
| EditText | Basic input only | No error display capability |
| Custom validation view | Full control | Over-engineering for simple form |

**Why validate on submit instead of real-time?**
- Users find real-time validation annoying (errors while typing)
- Submit validation allows user to finish typing before feedback
- Only validation that must be real-time: character count, field length (which isn't done here)

**Password security approach:**
- **Why regex over simple length check?** Ensures users can't use weak passwords like "password123" (no special char)
- **Why this specific pattern?** Industry standard: letters + digits + special char prevents common dictionary attacks
- **Why not hash?** Too late at signup - we're not storing yet, just validating format

**Why separate signup and login flows?**
- **Signup:** CreateAccount → OtpEnter → OtpVerify → **ProfileInfo** (collect data)
- **Login:** OtpEnter → OtpVerify → **AccountType** (quick path for existing users)
- Creates different UX for different user states

### **4. DATA FLOW**

```
Welcome → CreateAccountActivity
    ↓
User fills: Name, Email, Password
    ↓
Submit button validates all fields
    ↓
If valid:
    → Intent to OtpEnterActivity (IS_SIGNUP=true)
    
If invalid:
    → Error messages display
    → User corrects and resubmits
```

**Data NOT stored at this stage** - just collected for validation. Actual account creation happens after OTP verification (backend would handle this).

### **5. EDGE CASE HANDLING**

| Edge Case | Behavior | Code |
|-----------|----------|------|
| Empty name | Error shown, submit blocked | `if (name.isEmpty())` check |
| Invalid email format | Error shown, submit blocked | `EMAIL_ADDRESS.matcher()` |
| Weak password | Error shown, submit blocked | Regex validation |
| Rapid submit clicks | Could create multiple intents | No debouncing - could improve |
| User navigates away mid-form | Data lost (no auto-save) | Acceptable for signup flow |
| Paste invalid email | Caught by matcher | Works because matcher runs on final value |

### **6. VIVA QUESTIONS & ANSWERS**

**Q: Why use Patterns.EMAIL_ADDRESS instead of simple string check?**
A: "Android's Patterns.EMAIL_ADDRESS is a pre-built regex that follows RFC standards for email validation. It handles edge cases like subdomains, plus addressing (name+tag@email.com), and special characters that a simple string.contains('@') wouldn't catch. It's reliable and tested by the framework."

**Q: Explain the password regex in simple terms.**
A: "Lookaheads check that password contains: (1) At least one digit 0-9, (2) At least one letter a-z or A-Z, (3) At least one special character like !@#. The .+ means the password must be at least 1 character. Together, this ensures users can't use weak passwords like 'password123' (missing special char) or 'Pass!' (missing digit)."

**Q: Why validate on the TextInputLayout instead of EditText?**
A: "TextInputLayout is the Material Design wrapper - it has built-in error display that animates error text below the field with proper styling. EditText alone can't show errors nicely. TextInputLayout also handles hint animation when user focuses the field."

**Q: What happens if user clicks "Already have account?" link?**
A: "It creates an intent to OtpEnterActivity with IS_SIGNUP=false, which puts them in the login flow. This skips the signup process and goes straight to OTP verification. Then it calls finish() to close the signup activity."

**Q: How would you improve this screen?**
A: "(1) Add password strength indicator (weak/medium/strong), (2) Show password toggle button to reveal/hide password, (3) Real-time email availability check (backend call), (4) Debounce rapid submit clicks, (5) Save form state if user navigates away, (6) Add password confirmation field for user certainty."

