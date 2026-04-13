package com.example.nearneed;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * @deprecated Replaced by {@link HomeFragment}.
 * Layout logic has been migrated; this class is kept only for back-compat.
 */
@Deprecated
public class HomeProviderActivity extends AppCompatActivity {

    private TextView tvDeliveryLocation;
    private static final String PREFS = "LocationPrefs";
    private static final String KEY_LOCATION = "delivery_location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ensure user is in the correct role for this activity
        if (RoleManager.ROLE_SEEKER.equals(RoleManager.getRole(this))) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_home_provider);

        // Location picker setup
        tvDeliveryLocation = findViewById(R.id.tvDeliveryLocation);
        loadSavedLocation();

        View locationSection = findViewById(R.id.locationSection);
        if (locationSection != null) {
            locationSection.setOnClickListener(v -> showLocationPicker());
        }

        // Today's schedule → Calendar
        findViewById(R.id.viewCalendar).setOnClickListener(v -> {
            Intent intent = new Intent(this, CalendarProviderActivity.class);
            startActivity(intent);
        });

        // Post community request
        findViewById(R.id.btnPostCommunityRequest).setOnClickListener(v -> {
            Intent intent = new Intent(this, CommunityPostActivity.class);
            startActivity(intent);
        });

        // Earnings card → My Earnings
        findViewById(R.id.earningsCard).setOnClickListener(v -> {
            Intent intent = new Intent(this, MyEarningsActivity.class);
            startActivity(intent);
        });

        // View All Requests → Map Activity
        findViewById(R.id.viewAllRequestsContainer).setOnClickListener(v -> {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
        });

        // Setup nearby requests RecyclerView
        setupNearbyRequests();

        // Bind the unified navbar – Home tab active
        SeekerNavbarController.bind(this, findViewById(android.R.id.content), SeekerNavbarController.TAB_HOME);
    }

    private void setupNearbyRequests() {
        RecyclerView rvNearbyRequests = findViewById(R.id.rvNearbyRequests);
        NearbyRequestsAdapter adapter = new NearbyRequestsAdapter(new NearbyRequestsAdapter.OnRequestActionListener() {
            @Override
            public void onAccept(int position) {
                // Handle accept
            }

            @Override
            public void onDecline(int position) {
                // Handle decline
            }
        });

        // Sample data
        List<NearbyRequestsAdapter.RequestItem> requests = new ArrayList<>();
        requests.add(new NearbyRequestsAdapter.RequestItem(
            "Leaky Faucet Repair",
            "0.8 km",
            "₹45 est",
            R.drawable.ic_plumber
        ));
        requests.add(new NearbyRequestsAdapter.RequestItem(
            "Light Installation",
            "2.4 km",
            "₹80 est",
            R.drawable.ic_electrician
        ));

        adapter.setRequests(requests);
        rvNearbyRequests.setAdapter(adapter);
    }

    private void loadSavedLocation() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String saved = prefs.getString(KEY_LOCATION, null);
        if (saved != null && tvDeliveryLocation != null) {
            tvDeliveryLocation.setText(saved);
        }
    }

    private void saveLocation(String displayText) {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_LOCATION, displayText);
        editor.apply();

        if (tvDeliveryLocation != null) {
            tvDeliveryLocation.setText(displayText);
        }
    }

    private void showLocationPicker() {
        LocationPickerHelper.show(this, displayText -> saveLocation(displayText));
    }
}
