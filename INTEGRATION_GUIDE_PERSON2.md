# Person 2: Payment & Status Update - Integration Guide

## 🔌 How to Connect Payment Flow to Your Bookings

---

## Step 1: Add Button to Ongoing Booking Card

**File:** In your ongoing booking card layout or SeekerOngoingFragment

```xml
<!-- Add this button to each booking card in ongoing section -->
<com.google.android.material.button.MaterialButton
    android:id="@+id/btnCompleteService"
    android:layout_width="match_parent"
    android:layout_height="48dp"
    android:text="Complete Service"
    android:textColor="@color/white"
    android:textSize="14sp"
    android:layout_marginTop="12dp"
    app:backgroundTint="@color/sapphire_primary"
    app:cornerRadius="12dp"
    app:icon="@drawable/ic_check_circle_green"
    app:iconGravity="textStart"
    app:iconPadding="8dp" />
```

---

## Step 2: Implement Click Listener in Fragment

**File:** `SeekerOngoingFragment.java` (or your adapter)

```java
public class SeekerOngoingFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seeker_ongoing, container, false);

        // ... existing code ...

        // Setup booking card click listeners
        setupBookingCardListeners(view);

        return view;
    }

    private void setupBookingCardListeners(View view) {
        // Find all "Complete Service" buttons in cards
        MaterialButton btnCompleteService = view.findViewById(R.id.btnCompleteService);

        if (btnCompleteService != null) {
            btnCompleteService.setOnClickListener(v -> {
                // Show completion dialog
                showCompleteBookingDialog(
                    "booking_123",                    // bookingId
                    "Deep Kitchen Sanitization",      // serviceName
                    "Sarah Jenkins",                  // providerName
                    1500.0                            // amount
                );
            });
        }
    }

    private void showCompleteBookingDialog(String bookingId, String serviceName, String providerName, double amount) {
        // Create and show the dialog
        CompleteBookingDialogFragment dialog = CompleteBookingDialogFragment.newInstance(
            bookingId,
            serviceName,
            providerName,
            amount
        );

        // Set callback for payment flow
        dialog.setOnPaymentClickListener((bookingIdParam, rating, notes) -> {
            // Launch payment flow
            launchPaymentFlow(bookingIdParam, serviceName, providerName, amount, rating, notes);
        });

        // Show dialog
        dialog.show(getChildFragmentManager(), "complete_booking_dialog");
    }

    private void launchPaymentFlow(String bookingId, String serviceName, String providerName, double amount, int rating, String notes) {
        Intent intent = new Intent(requireActivity(), PaymentFlowActivity.class);
        
        // Pass all necessary data
        intent.putExtra("booking_id", bookingId);
        intent.putExtra("service_name", serviceName);
        intent.putExtra("provider_name", providerName);
        intent.putExtra("service_amount", amount);
        intent.putExtra("user_rating", rating);
        intent.putExtra("completion_notes", notes);
        
        startActivity(intent);
    }
}
```

---

## Step 3: Connect Adapter Click Events (If Using RecyclerView)

**File:** Your booking adapter (e.g., `BookingAdapter.java`)

```java
public class BookingAdapter extends RecyclerView.Adapter<BookingViewHolder> {

    private OnBookingActionListener listener;

    public interface OnBookingActionListener {
        void onCompleteBooking(String bookingId, String serviceName, String providerName, double amount);
    }

    public BookingViewHolder(View itemView) {
        // ... existing code ...
        
        btnCompleteService = itemView.findViewById(R.id.btnCompleteService);
        btnCompleteService.setOnClickListener(v -> {
            if (listener != null) {
                Booking booking = bookings.get(getAdapterPosition());
                listener.onCompleteBooking(
                    booking.getId(),
                    booking.getTitle(),
                    booking.getProviderName(),
                    booking.getAmount()
                );
            }
        });
    }

    public void setOnBookingActionListener(OnBookingActionListener listener) {
        this.listener = listener;
    }
}
```

**In Fragment:**
```java
adapter.setOnBookingActionListener((bookingId, serviceName, providerName, amount) -> {
    showCompleteBookingDialog(bookingId, serviceName, providerName, amount);
});
```

---

## Step 4: Update BookingsActivity

**File:** `BookingsActivity.java`

```java
public class BookingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookings);

        // ... existing code ...

        // The navbar is already set up
        SeekerNavbarController.bind(this, findViewById(android.R.id.content), SeekerNavbarController.TAB_BOOKINGS);
    }
}
```

---

## Step 5: Data Model Requirements

**Ensure your Booking model has these fields:**

```java
public class Booking {
    private String id;                    // booking ID
    private String title;                 // service name
    private String providerName;          // provider name
    private double amount;                // service amount
    private String status;                // "upcoming", "ongoing", "past"
    
    // ... getters and setters ...
}
```

---

## Step 6: Update Activities in Manifest

**File:** `AndroidManifest.xml` (Already done ✅)

```xml
<activity
    android:name=".PaymentFlowActivity"
    android:exported="false" />

<activity
    android:name=".PaymentSuccessActivity"
    android:exported="false" />
```

---

## 🔄 Complete Data Flow Diagram

```
SeekerOngoingFragment
    ↓ (Get booking data)
setupBookingCardListeners()
    ↓ (Click "Complete Service")
showCompleteBookingDialog()
    ↓ (Create CompleteBookingDialogFragment)
CompleteBookingDialog
    ├─ [Display service info]
    ├─ [Rate: 1-5 stars]
    ├─ [Notes: optional]
    └─ [Proceed to Payment clicked]
         ↓ (Collect rating & notes)
    launchPaymentFlow()
         ↓ (Pass Intent extras)
    PaymentFlowActivity
         ├─ [Display order summary]
         ├─ [Select payment method]
         └─ [Confirm & Pay clicked]
              ↓ (Process payment: 2 sec)
         PaymentSuccessActivity
              ├─ [Show success message]
              ├─ [Display transaction details]
              └─ [Back to Home clicked]
                   ↓ (Clear stack)
         HomeSeekerActivity
```

---

## 🧪 Testing the Flow

### 1. Test Complete Booking Dialog
```java
// In your test activity:
CompleteBookingDialogFragment dialog = CompleteBookingDialogFragment.newInstance(
    "test_123",
    "Test Service",
    "Test Provider",
    500.0
);
dialog.show(getSupportFragmentManager(), "test");
```

### 2. Test Payment Flow
```java
Intent intent = new Intent(this, PaymentFlowActivity.class);
intent.putExtra("booking_id", "test_123");
intent.putExtra("service_name", "Test Service");
intent.putExtra("provider_name", "Test Provider");
intent.putExtra("service_amount", 500.0);
intent.putExtra("user_rating", 5);
intent.putExtra("completion_notes", "Great service!");
startActivity(intent);
```

### 3. Test Success Screen
```java
Intent intent = new Intent(this, PaymentSuccessActivity.class);
intent.putExtra("booking_id", "test_123");
intent.putExtra("service_name", "Test Service");
intent.putExtra("provider_name", "Test Provider");
intent.putExtra("service_amount", 500.0);
startActivity(intent);
```

---

## 💾 Data to Save (Backend Integration)

**When Payment is Successful, Save These:**

```java
Payment payment = new Payment();
payment.setId(UUID.randomUUID().toString());
payment.setBookingId(bookingId);
payment.setAmount(totalAmount);
payment.setServiceAmount(serviceAmount);
payment.setPlatformFee(platformFee);
payment.setPaymentMethod(selectedPaymentMethod);
payment.setStatus("SUCCESS");
payment.setTimestamp(System.currentTimeMillis());
payment.setTransactionId(transactionId);

// Save to database
paymentRepository.save(payment);

// Update booking status
booking.setStatus("completed");
booking.setCompletionNotes(completionNotes);
booking.setUserRating(userRating);
bookingRepository.update(booking);

// Send notification to provider
sendNotificationToProvider(providerName, bookingId, serviceAmount);
```

---

## 🔐 Security Considerations

1. **Amount Validation:** Always validate amounts on backend
2. **Payment Method Storage:** Don't store full card details
3. **Transaction Verification:** Verify payment gateway response
4. **Rate Limiting:** Prevent duplicate payment attempts
5. **User Verification:** Ensure user owns the booking

---

## 🚀 Next Steps After Frontend

1. **Implement Payment Gateway:**
   - Add Razorpay/PayU SDK
   - Replace simulated 2-second delay with actual API call
   - Handle payment errors and retries

2. **Database Integration:**
   - Create Payment entity and DAO
   - Save payment records
   - Update booking status

3. **Provider Notification:**
   - Send FCM notification to provider
   - Create provider confirmation UI
   - Update provider earnings

4. **Receipt Generation:**
   - Generate PDF receipt
   - Email to user
   - Store in cloud storage

---

## 📞 Common Issues & Solutions

### Issue: Dialog not showing
```
Solution: Ensure you're using getSupportFragmentManager() (not getFragmentManager())
          and the parent activity is a FragmentActivity/AppCompatActivity
```

### Issue: Data not passing between screens
```
Solution: Check all intent.putExtra() calls match the expected keys
          in the receiving activity's getIntent().getStringExtra(key)
```

### Issue: Payment method not selecting
```
Solution: Ensure RadioButton selection is properly implemented in adapter
          and notifyItemChanged() is called when selection changes
```

### Issue: Back button causing navigation issues
```
Solution: PaymentSuccessActivity prevents back navigation with onBackPressed()
          Home return clears the stack with FLAG_ACTIVITY_CLEAR_TASK
```

---

## 📝 Checklist Before Going Live

- ✅ Frontend UI is complete and styled
- ⏳ Payment gateway is integrated
- ⏳ Database schema is implemented
- ⏳ Provider notification system is working
- ⏳ Receipt generation is functional
- ⏳ Error handling is in place
- ⏳ Testing is complete
- ⏳ Security review is done

---

**Document Created:** April 14, 2026  
**Purpose:** Guide for integrating payment flow with existing bookings  
**Status:** Ready for backend development  
**Contact:** Person 2 Lead for questions
