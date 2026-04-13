# MapsFragment Enhancement - Quick Setup Guide

## 🚀 Setup in 5 Minutes

### Step 1: Copy Files to Project

Copy these files to your Android Studio project:

**Layout Files:**
```
app/src/main/res/layout/
├── layout_maps_provider_enhanced.xml      (NEW)
└── item_job_list.xml                      (NEW)
```

**Java Files:**
```
app/src/main/java/com/example/nearneed/
├── JobListAdapter.java                    (NEW)
└── MapsFragment.java                      (UPDATED)
```

**Drawable:**
```
app/src/main/res/drawable/
└── bg_bottom_sheet_handle.xml             (NEW)
```

### Step 2: Sync Gradle

In Android Studio:
```
File → Sync Now
```

Or use terminal:
```bash
./gradlew sync
```

### Step 3: Verify Dependencies

Ensure these are in `build.gradle` (they should be):

```gradle
// Material Design
implementation 'com.google.android.material:material:1.9.0'

// RecyclerView (for job list)
implementation 'androidx.recyclerview:recyclerview:1.3.0'

// Google Maps
implementation 'com.google.android.gms:play-services-maps:18.1.0'

// Constraint Layout
implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
```

### Step 4: Build & Run

```bash
# Build
./gradlew build

# Run on device/emulator
./gradlew installDebug
```

Or use Android Studio Run button (▶️).

## ✅ Verification Checklist

After running:

- [ ] App launches without crashes
- [ ] Provider mode shows enhanced map layout
- [ ] Bottom sheet is visible at bottom
- [ ] Can drag bottom sheet up/down
- [ ] Filter chips are clickable
- [ ] Search bar is functional
- [ ] Job list shows 3 sample jobs
- [ ] Recenter button exists (right side of search)
- [ ] Seeker mode still works normally (unchanged)

## 🔧 If You Get Errors

### "Cannot resolve symbol 'JobListAdapter'"
**Solution:** Make sure `JobListAdapter.java` is in the correct package:
```
com.example.nearneed.JobListAdapter
```

### "Cannot find layout 'layout_maps_provider_enhanced'"
**Solution:** Ensure layout file is at:
```
app/src/main/res/layout/layout_maps_provider_enhanced.xml
```

### "MapsFragment not found"
**Solution:** The updated MapsFragment should be at:
```
app/src/main/java/com/example/nearneed/MapsFragment.java
```

### Bottom sheet doesn't appear
**Solution:** Check that:
1. Layout includes `provider_bottom_sheet` with id
2. BottomSheetBehavior is properly attached in Java
3. RecyclerView has id `provider_jobs_list`

### Chips not clickable
**Solution:** Make sure chips have:
```xml
android:clickable="true"
android:focusable="true"
```

## 📱 Testing the Features

### Test Bottom Sheet
1. Open app in Provider mode
2. Swipe bottom sheet up
3. Should smoothly reveal job list
4. Swipe down to collapse

### Test Filters
1. Open app in Provider mode
2. Click "Urgency" chip
3. Chip background should turn blue
4. Job list should filter to high-urgency jobs
5. Click again to deactivate

### Test Search
1. Type "plumbing" in search bar
2. Press search button on keyboard
3. List should filter to matching jobs
4. Map should update with relevant markers

### Test Job Selection
1. Tap a job in the bottom sheet list
2. Map marker should highlight (blue background)
3. Detail card should appear at bottom
4. Card should show job info
5. Can close by tapping X button

### Test Recenter
1. Pan map to different location
2. Tap recenter button (compass icon, right of search)
3. Map should animate back to Mumbai (19.0760, 72.8777)

## 🎨 Customization

### Change Sample Jobs
Edit in `MapsFragment.java`, `setupBottomSheetAndJobs()`:

```java
allJobs.add(new JobListAdapter.JobItem(
    "Your Job Title",
    "Your job description",
    "Your distance",
    "Your budget",
    "Your category",
    R.drawable.your_icon,      // Change icon
    R.color.your_color,        // Change color
    null
));
```

### Change Filter Behavior
In `applyFilters()` method:

```java
if (filterUrgency && !job.category.equals("Your Condition")) {
    matches = false;
}
```

### Change Default Location
In `onMapReady()`:

```java
LatLng yourLocation = new LatLng(19.0760, 72.8777);  // Change coordinates
mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(yourLocation, 14f));
```

## 🔌 Connecting to Backend

When ready to use real data:

```java
// In setupBottomSheetAndJobs() method:
// Replace allJobs population with API call:

jobApi.getNearbyJobs(latitude, longitude)
    .subscribe(
        jobs -> {
            allJobs.clear();
            allJobs.addAll(jobs);
            filteredJobs.clear();
            filteredJobs.addAll(allJobs);
            jobAdapter.notifyDataSetChanged();
            updateMapMarkers();
        },
        error -> {
            Toast.makeText(getContext(), "Error loading jobs", Toast.LENGTH_SHORT).show();
        }
    );
```

## 📚 Key Classes Reference

### JobListAdapter
```java
// Usage
JobListAdapter adapter = new JobListAdapter(jobList, (job, position) -> {
    // Handle job click
});
recyclerView.setAdapter(adapter);

// Update list
adapter.updateList(newJobList);
```

### JobItem
```java
JobListAdapter.JobItem job = new JobListAdapter.JobItem(
    title,
    description,
    distance,
    budget,
    category,
    iconResId,
    colorResId,
    marker
);
```

### BottomSheetBehavior
```java
// Get reference
BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);

// Control
behavior.setState(BottomSheetBehavior.STATE_EXPANDED);    // Full screen
behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);   // Peek view
behavior.setState(BottomSheetBehavior.STATE_HIDDEN);      // Hidden
```

## 🐛 Debugging Tips

### Check if Provider Mode Activated
```java
String role = RoleManager.getRole(requireContext());
Log.d("MapsFragment", "Current role: " + role);
// Should print: "PROVIDER" or "SEEKER"
```

### Verify Job List Population
```java
Log.d("MapsFragment", "Total jobs: " + allJobs.size());
Log.d("MapsFragment", "Filtered jobs: " + filteredJobs.size());
```

### Check Map Marker Count
```java
Log.d("MapsFragment", "Markers on map: " + mMap.getShapeMarkers().size());
```

### Monitor Filter State
```java
Log.d("Filters", "Urgency: " + filterUrgency + 
                ", Budget: " + filterBudget + 
                ", Distance: " + filterDistance);
```

## 📞 Support

If you encounter issues:

1. **Check Build Output** - Look for detailed error messages
2. **Verify File Paths** - Ensure all files are in correct directories
3. **Check AndroidManifest.xml** - Permissions should include:
   ```xml
   <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
   <uses-permission android:name="android.permission.INTERNET" />
   ```
4. **Test on Different Devices** - Different screen sizes may reveal layout issues
5. **Check Logcat** - Android Studio's Logcat tab shows detailed logs

## 🎓 Learning Resources

- [Material Design Bottom Sheet](https://material.io/components/bottom-sheets)
- [Android RecyclerView](https://developer.android.com/guide/topics/ui/layout/recyclerview)
- [Google Maps Android API](https://developers.google.com/maps/documentation/android-sdk/overview)
- [Material Chips](https://material.io/components/chips)

---

**You're all set!** 🎉

The enhanced Maps screen is now ready to use. Start with the sample data and gradually integrate real backend services.

