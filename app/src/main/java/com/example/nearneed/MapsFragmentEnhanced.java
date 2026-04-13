package com.example.nearneed;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced MapsFragment with interactive bottom sheet panel
 * Features:
 * - Draggable bottom sheet with job list
 * - Interactive filter chips
 * - Synced map markers and list items
 * - Clean, modern Snapchat-style UI
 */
public class MapsFragmentEnhanced extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String currentRole;
    private boolean isGigsMode = true;

    // UI Elements
    private TextView btnGigs, btnCommunity;
    private Chip chipUrgency, chipBudget, chipDistance;
    private EditText searchEditText;
    private ImageView recenterButton;

    // Bottom Sheet
    private BottomSheetBehavior<?> bottomSheetBehavior;
    private MaterialCardView bottomSheet;
    private RecyclerView jobsList;
    private JobListAdapter jobAdapter;
    private List<JobListAdapter.JobItem> allJobs;
    private List<JobListAdapter.JobItem> filteredJobs;

    // Job Detail Card
    private MaterialCardView jobDetailCard;
    private View closeCardButton;

    // Map State
    private Marker selectedMarker;
    private static final float SELECTED_MARKER_SCALE = 1.2f;

    // Filter State
    private boolean filterUrgency = false;
    private boolean filterBudget = false;
    private boolean filterDistance = false;

    // Sample data structure
    private static class MarkerData {
        int iconResId;
        int color;
        MarkerData(int icon, int col) { iconResId = icon; color = col; }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle bundle) {
        currentRole = RoleManager.getRole(requireContext());

        // Use enhanced layout for providers
        int layoutId = RoleManager.ROLE_SEEKER.equals(currentRole) ?
                R.layout.layout_maps_seeker : R.layout.layout_maps_provider_enhanced;

        View view = inflater.inflate(layoutId, container, false);

        initUI(view);
        setupBottomSheet(view);
        initializeJobList();

        // Get map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(RoleManager.ROLE_SEEKER.equals(currentRole) ? R.id.map : R.id.provider_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Bind navbar
        SeekerNavbarController.bind(requireActivity(), view, SeekerNavbarController.TAB_MAP);

        return view;
    }

    private void initUI(View view) {
        if (RoleManager.ROLE_SEEKER.equals(currentRole)) {
            // Seeker mode - keep original
            initSeekerUI(view);
        } else {
            // Provider mode - enhanced
            initProviderUI(view);
        }
    }

    private void initSeekerUI(View view) {
        MaterialCardView infoCard = view.findViewById(R.id.seeker_info_card);

        View bookNow = view.findViewById(R.id.btn_book_now);
        if (bookNow != null) {
            bookNow.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Booking provider...", Toast.LENGTH_SHORT).show());
        }

        EditText searchEdit = view.findViewById(R.id.search_edit_text);
        if (searchEdit != null) {
            searchEdit.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                    Toast.makeText(getContext(), "Searching: " + v.getText(), Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });
        }

        if (infoCard != null) {
            View closeBtn = view.findViewById(R.id.ic_close_card);
            if (closeBtn != null) {
                closeBtn.setOnClickListener(v -> infoCard.setVisibility(View.GONE));
            }
        }
    }

    private void initProviderUI(View view) {
        btnGigs = view.findViewById(R.id.btn_provider_gigs);
        btnCommunity = view.findViewById(R.id.btn_provider_community);
        jobDetailCard = view.findViewById(R.id.provider_job_detail_card);
        closeCardButton = view.findViewById(R.id.ic_close_card);

        // Filter chips
        chipUrgency = view.findViewById(R.id.chip_urgency);
        chipBudget = view.findViewById(R.id.chip_budget);
        chipDistance = view.findViewById(R.id.chip_distance);

        // Search and location
        searchEditText = view.findViewById(R.id.provider_search_edit_text);
        recenterButton = view.findViewById(R.id.ic_recenter_location);

        // Mode toggle
        if (btnGigs != null) btnGigs.setOnClickListener(v -> toggleMode(true));
        if (btnCommunity != null) btnCommunity.setOnClickListener(v -> toggleMode(false));

        // Close detail card
        if (closeCardButton != null) {
            closeCardButton.setOnClickListener(v -> jobDetailCard.setVisibility(View.GONE));
        }

        // Accept job button
        View acceptButton = view.findViewById(R.id.btn_accept_job);
        if (acceptButton != null) {
            acceptButton.setOnClickListener(v -> handleAcceptJob());
        }

        // Setup filter chips
        setupFilterChips();

        // Setup search
        if (searchEditText != null) {
            searchEditText.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                    performSearch(v.getText().toString());
                    return true;
                }
                return false;
            });
        }

        // Setup recenter button
        if (recenterButton != null) {
            recenterButton.setOnClickListener(v -> recenterMap());
        }
    }

    private void setupFilterChips() {
        if (chipUrgency != null) {
            chipUrgency.setOnClickListener(v -> {
                filterUrgency = !filterUrgency;
                updateChipAppearance(chipUrgency, filterUrgency);
                applyFilters();
            });
        }

        if (chipBudget != null) {
            chipBudget.setOnClickListener(v -> {
                filterBudget = !filterBudget;
                updateChipAppearance(chipBudget, filterBudget);
                applyFilters();
            });
        }

        if (chipDistance != null) {
            chipDistance.setOnClickListener(v -> {
                filterDistance = !filterDistance;
                updateChipAppearance(chipDistance, filterDistance);
                applyFilters();
            });
        }
    }

    private void updateChipAppearance(Chip chip, boolean isSelected) {
        if (isSelected) {
            chip.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.sapphire_primary)));
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        } else {
            chip.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_body_dark));
        }
    }

    private void applyFilters() {
        filteredJobs.clear();

        for (JobListAdapter.JobItem job : allJobs) {
            boolean matches = true;

            if (filterUrgency && !job.category.equals("High Urgency")) {
                matches = false;
            }

            if (filterBudget && !job.budget.contains("500")) {
                matches = false;
            }

            if (filterDistance && !job.distance.contains("0.5")) {
                matches = false;
            }

            if (matches) {
                filteredJobs.add(job);
            }
        }

        jobAdapter.updateList(filteredJobs);
        updateMapMarkers();
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            applyFilters();
            return;
        }

        filteredJobs.clear();
        String queryLower = query.toLowerCase();

        for (JobListAdapter.JobItem job : allJobs) {
            if (job.title.toLowerCase().contains(queryLower) ||
                    job.description.toLowerCase().contains(queryLower)) {
                filteredJobs.add(job);
            }
        }

        jobAdapter.updateList(filteredJobs);
        updateMapMarkers();
    }

    private void recenterMap() {
        if (mMap != null) {
            LatLng mumbai = new LatLng(19.0760, 72.8777);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mumbai, 14f));
            Toast.makeText(requireContext(), "Recenterering map...", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBottomSheet(View view) {
        bottomSheet = view.findViewById(R.id.provider_bottom_sheet);
        jobsList = view.findViewById(R.id.provider_jobs_list);

        if (bottomSheet != null) {
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
            bottomSheetBehavior.setPeekHeight(72);
            bottomSheetBehavior.setHideable(false);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            // Listen for state changes
            bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    // Handle state change if needed
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    // Handle slide if needed
                }
            });
        }
    }

    private void initializeJobList() {
        allJobs = new ArrayList<>();
        filteredJobs = new ArrayList<>();

        // Add sample jobs (yellow = gigs, green = community)
        int yellowColor = ContextCompat.getColor(requireContext(), R.color.sapphire_tertiary);
        int greenColor = ContextCompat.getColor(requireContext(), R.color.brand_success);

        allJobs.add(new JobListAdapter.JobItem(
                "Plumbing Repair",
                "Pipe repair in kitchen",
                "0.5km away",
                "₹500 - 800",
                "High Urgency",
                R.drawable.ic_plumber,
                R.color.sapphire_primary,
                null
        ));

        allJobs.add(new JobListAdapter.JobItem(
                "TV Mounting",
                "Wall mount installation",
                "1.2km away",
                "₹1200 - 1500",
                "Normal",
                R.drawable.ic_toolbox_seeker,
                R.color.sapphire_primary,
                null
        ));

        allJobs.add(new JobListAdapter.JobItem(
                "Electrical Work",
                "Wiring repair needed",
                "0.8km away",
                "₹800 - 1200",
                "High Urgency",
                R.drawable.ic_plug_blue,
                R.color.sapphire_primary,
                null
        ));

        filteredJobs.addAll(allJobs);

        // Setup adapter
        jobAdapter = new JobListAdapter(filteredJobs, (job, position) -> {
            // Click on job in list
            selectJob(job, position);
            showJobDetailCard(job);

            // Highlight marker if it exists
            if (job.marker != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(job.marker.getPosition()));
            }
        });

        if (jobsList != null) {
            jobsList.setAdapter(jobAdapter);
        }
    }

    private void selectJob(JobListAdapter.JobItem job, int position) {
        if (selectedMarker != null && selectedMarker.getTag() instanceof MarkerData) {
            MarkerData oldData = (MarkerData) selectedMarker.getTag();
            selectedMarker.setIcon(getMarkerBitmapDescriptor(selectedMarker.getTitle(), oldData.iconResId, oldData.color, false));
        }

        if (job.marker != null) {
            job.marker.setIcon(getMarkerBitmapDescriptor(job.title, job.iconResId, job.colorResId, true));
            selectedMarker = job.marker;
        }
    }

    private void showJobDetailCard(JobListAdapter.JobItem job) {
        if (jobDetailCard != null) {
            jobDetailCard.setVisibility(View.VISIBLE);

            TextView jobTitle = jobDetailCard.findViewById(R.id.job_title);
            TextView jobDescription = jobDetailCard.findViewById(R.id.job_description);
            TextView jobDistance = jobDetailCard.findViewById(R.id.job_distance);
            TextView jobBudget = jobDetailCard.findViewById(R.id.job_budget);
            TextView jobUrgency = jobDetailCard.findViewById(R.id.job_urgency);

            if (jobTitle != null) jobTitle.setText(job.title);
            if (jobDescription != null) jobDescription.setText(job.description);
            if (jobDistance != null) jobDistance.setText(job.distance);
            if (jobBudget != null) jobBudget.setText(job.budget);
            if (jobUrgency != null) jobUrgency.setText(job.category);
        }
    }

    private void toggleMode(boolean gigs) {
        if (isGigsMode == gigs) return;
        isGigsMode = gigs;

        if (btnGigs != null) {
            btnGigs.setBackground(gigs ? ContextCompat.getDrawable(requireContext(), R.drawable.bg_segmented_thumb) : null);
            btnGigs.setTextColor(gigs ? ContextCompat.getColor(requireContext(), R.color.sapphire_primary) : ContextCompat.getColor(requireContext(), R.color.text_body_light));
            btnGigs.setTypeface(null, gigs ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        }

        if (btnCommunity != null) {
            btnCommunity.setBackground(!gigs ? ContextCompat.getDrawable(requireContext(), R.drawable.bg_segmented_thumb) : null);
            btnCommunity.setTextColor(!gigs ? ContextCompat.getColor(requireContext(), R.color.sapphire_primary) : ContextCompat.getColor(requireContext(), R.color.text_body_light));
            btnCommunity.setTypeface(null, !gigs ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        }

        updateMarkers();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        LatLng mumbai = new LatLng(19.0760, 72.8777);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mumbai, 14f));

        updateMarkers();

        mMap.setOnMarkerClickListener(marker -> {
            // Find corresponding job
            for (JobListAdapter.JobItem job : filteredJobs) {
                if (job.marker == marker) {
                    showJobDetailCard(job);
                    selectJob(job, 0);
                    break;
                }
            }
            return true;
        });

        mMap.setOnMapClickListener(latLng -> {
            if (jobDetailCard != null) {
                jobDetailCard.setVisibility(View.GONE);
            }
        });
    }

    private void updateMarkers() {
        if (mMap == null) return;
        mMap.clear();

        int blue = ContextCompat.getColor(requireContext(), R.color.sapphire_primary);
        int yellow = ContextCompat.getColor(requireContext(), R.color.sapphire_tertiary);
        int green = ContextCompat.getColor(requireContext(), R.color.brand_success);

        if (RoleManager.ROLE_SEEKER.equals(currentRole)) {
            if (isGigsMode) {
                addMarkerToJob(new LatLng(19.0820, 72.8850), "Marcus Watts", "Electrician", blue, R.drawable.ic_plug_blue, 0);
                addMarkerToJob(new LatLng(19.0700, 72.8700), "Sarah Chen", "Cleaning", blue, R.drawable.ic_cleaning, 1);
            } else {
                addMarkerToJob(new LatLng(19.0750, 72.8800), "Park Cleanup", "Community", green, R.drawable.ic_gardening, 0);
            }
        } else {
            // Provider mode - add markers for filtered gigs
            updateMapMarkers();
        }
    }

    private void updateMapMarkers() {
        if (mMap == null) return;
        mMap.clear();

        int blue = ContextCompat.getColor(requireContext(), R.color.sapphire_primary);
        int yellow = ContextCompat.getColor(requireContext(), R.color.sapphire_tertiary);

        LatLng[] positions = {
                new LatLng(19.0850, 72.8750),
                new LatLng(19.0650, 72.8650),
                new LatLng(19.0780, 72.8820)
        };

        int i = 0;
        for (JobListAdapter.JobItem job : filteredJobs) {
            if (i < positions.length) {
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(positions[i])
                        .title(job.title)
                        .snippet(job.description)
                        .icon(getMarkerBitmapDescriptor(job.title, job.iconResId, yellow, false)));
                if (marker != null) {
                    job.marker = marker;
                    marker.setTag(new MarkerData(job.iconResId, yellow));
                }
                i++;
            }
        }
    }

    private void addMarkerToJob(LatLng pos, String title, String snippet, int color, int iconResId, int jobIndex) {
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(pos)
                .title(title)
                .snippet(snippet)
                .icon(getMarkerBitmapDescriptor(title, iconResId, color, false)));

        if (marker != null) {
            marker.setTag(new MarkerData(iconResId, color));

            if (jobIndex < allJobs.size()) {
                allJobs.get(jobIndex).marker = marker;
            }
        }
    }

    private BitmapDescriptor getMarkerBitmapDescriptor(String title, int iconResId, int bgColor, boolean isSelected) {
        int iconSize = 100;
        int width = 240;
        int height = iconSize + 60;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        float centerX = width / 2f;
        float centerY = iconSize / 2f;

        // Shadow
        paint.setColor(Color.parseColor("#40000000"));
        canvas.drawCircle(centerX, centerY + 3, iconSize / 2f - 2, paint);

        // Background circle
        paint.setColor(bgColor);
        canvas.drawCircle(centerX, centerY, iconSize / 2f - 2, paint);

        // Icon
        Drawable drawable = ContextCompat.getDrawable(requireContext(), iconResId);
        if (drawable != null) {
            drawable.setTint(Color.WHITE);
            int dPadding = 25;
            drawable.setBounds((int)(centerX - iconSize/2f + dPadding),
                    (int)(centerY - iconSize/2f + dPadding),
                    (int)(centerX + iconSize/2f - dPadding),
                    (int)(centerY + iconSize/2f - dPadding));
            drawable.draw(canvas);
        }

        // Text label
        String labelText = title.length() > 12 ? title.substring(0, 10) + ".." : title;
        paint.setTextSize(24f);
        paint.setFakeBoldText(true);
        float textWidth = paint.measureText(labelText);
        float bubblePaddingH = 16f;
        float bubblePaddingV = 8f;
        float bubbleWidth = textWidth + (bubblePaddingH * 2);
        float bubbleHeight = 36f;

        float bubbleLeft = centerX - (bubbleWidth / 2f);
        float bubbleTop = iconSize + 4;

        paint.setColor(isSelected ? ContextCompat.getColor(requireContext(), R.color.sapphire_primary) : Color.WHITE);
        canvas.drawRoundRect(bubbleLeft, bubbleTop, bubbleLeft + bubbleWidth, bubbleTop + bubbleHeight, 10f, 10f, paint);

        paint.setColor(isSelected ? Color.WHITE : Color.parseColor("#0F172A"));
        canvas.drawText(labelText, bubbleLeft + bubblePaddingH, bubbleTop + bubbleHeight - 10f, paint);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void handleAcceptJob() {
        Toast.makeText(requireContext(), "Job accepted! Navigating to details...", Toast.LENGTH_SHORT).show();
        // TODO: Navigate to job acceptance flow or details screen
    }
}
