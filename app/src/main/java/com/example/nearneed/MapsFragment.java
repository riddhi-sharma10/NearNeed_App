package com.example.nearneed;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.core.widget.ImageViewCompat;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String currentRole;
    private boolean isGigsMode = true;

    // UI elements - Common
    private TextView btnGigs, btnCommunity;
    private View infoCard;
    private Marker selectedMarker;

    // UI elements - Provider specific
    private MaterialCardView jobDetailCard;
    private Chip chipUrgency, chipBudget, chipDistance;
    private EditText providerSearchEdit;
    private ImageView recenterButton;
    private RecyclerView jobsList;
    private JobListAdapter jobAdapter;
    private List<Job> allJobs;
    private List<Job> filteredJobs;
    private BottomSheetBehavior<?> bottomSheetBehavior;
    private View bottomSheetPanel;
    private TextView bottomSheetCount;

    // Filter state
    private boolean filterUrgency = false;
    private boolean filterBudget = false;
    private boolean filterDistance = false;

    // Map of jobs to markers for syncing
    private Map<Job, Marker> jobToMarkerMap;

    // Track currently displayed job in detail card
    private Job currentDetailJob;

    private static class MarkerData {
        int iconResId;
        int color;
        MarkerData(int icon, int col) { iconResId = icon; color = col; }
    }

    // Job data class
    public static class Job {
        public String title;
        public String description;
        public String distance;
        public String budget;
        public String category; // "High Urgency", "Normal", etc.
        public int iconResId;
        public int colorResId;
        public LatLng location;
        public String type; // "GIG" or "COMMUNITY"
        public int slots;        // total slots for COMMUNITY jobs (0 = not applicable)
        public int slotsFilled;  // how many have applied

        public Job(String title, String description, String distance, String budget,
                   String category, int iconResId, int colorResId, LatLng location, String type) {
            this.title = title;
            this.description = description;
            this.distance = distance;
            this.budget = budget;
            this.category = category;
            this.iconResId = iconResId;
            this.colorResId = colorResId;
            this.location = location;
            this.type = type;
            this.slots = 0;
            this.slotsFilled = 0;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle bundle) {
        currentRole = RoleManager.getRole(requireContext());

        int layoutId = RoleManager.ROLE_SEEKER.equals(currentRole) ?
                R.layout.layout_maps_seeker : R.layout.layout_maps_provider;

        View view = inflater.inflate(layoutId, container, false);

        initUI(view);

        if (RoleManager.ROLE_PROVIDER.equals(currentRole)) {
            setupProviderMode(view);
        }

        // Obtain the SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(RoleManager.ROLE_SEEKER.equals(currentRole) ? R.id.map : R.id.provider_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        SeekerNavbarController.bind(requireActivity(), view, SeekerNavbarController.TAB_MAP);

        return view;
    }

    private void initUI(View view) {
        if (RoleManager.ROLE_SEEKER.equals(currentRole)) {
            infoCard = view.findViewById(R.id.seeker_info_card);

            View bookNow = view.findViewById(R.id.btn_book_now);
            if (bookNow != null) {
                bookNow.setOnClickListener(v -> {
                    if (selectedMarker != null) {
                        String title = selectedMarker.getTitle();
                        // For now, treat all seeker interactions as community volunteer posts
                        // Check if this is a community pin by checking marker position against community jobs
                        boolean isCommunity = allJobs.stream()
                            .anyMatch(job -> "COMMUNITY".equals(job.type) && job.title.equals(title));
                        if (isCommunity) {
                            showVolunteerSheet(title);
                        } else {
                            Toast.makeText(getContext(), "Seeker Booking...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            EditText searchEdit = view.findViewById(R.id.search_edit_text);
            if (searchEdit != null) {
                searchEdit.setOnEditorActionListener((v, actionId, event) -> {
                    if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                        Toast.makeText(getContext(), "Searching for: " + v.getText(), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    return false;
                });
            }
        } else {
            btnGigs = view.findViewById(R.id.btn_provider_gigs);
            btnCommunity = view.findViewById(R.id.btn_provider_community);
            infoCard = view.findViewById(R.id.provider_info_card);
        }

        if (btnGigs != null) btnGigs.setOnClickListener(v -> toggleMode(true));
        if (btnCommunity != null) btnCommunity.setOnClickListener(v -> toggleMode(false));

        View closeBtn = view.findViewById(R.id.ic_close_card);
        if (closeBtn != null) closeBtn.setOnClickListener(v -> {
            if (infoCard != null) infoCard.setVisibility(View.GONE);
        });
    }

    private void setupProviderMode(View view) {
        jobToMarkerMap = new HashMap<>();
        allJobs = new ArrayList<>();
        filteredJobs = new ArrayList<>();

        // Initialize jobs data
        initializeSampleJobs();

        // Setup bottom sheet
        bottomSheetPanel = view.findViewById(R.id.provider_bottom_sheet);
        bottomSheetCount = view.findViewById(R.id.bottom_sheet_count);
        jobsList = view.findViewById(R.id.provider_jobs_list);

        if (bottomSheetPanel != null) {
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetPanel);
            bottomSheetBehavior.setPeekHeight(72);
            bottomSheetBehavior.setHideable(false);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        // Setup job list adapter
        jobAdapter = new JobListAdapter(filteredJobs, new JobListAdapter.OnJobClickListener() {
            @Override
            public void onJobClick(Job job, int position) {
                selectJob(job);
                showJobDetailCard(job);
            }

            @Override
            public void onViewPostClick(Job job) {
                showProviderJobDetail(job);
            }
        });

        if (jobsList != null) {
            jobsList.setAdapter(jobAdapter);
        }

        // Setup search
        providerSearchEdit = view.findViewById(R.id.provider_search_edit_text);
        if (providerSearchEdit != null) {
            providerSearchEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    performSearch(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // Setup recenter button
        recenterButton = view.findViewById(R.id.ic_recenter_location);
        if (recenterButton != null) {
            recenterButton.setOnClickListener(v -> recenterMap());
        }

        // Setup job detail card
        jobDetailCard = view.findViewById(R.id.provider_info_card);
        View closeCard = view.findViewById(R.id.ic_close_card);
        if (closeCard != null) {
            closeCard.setOnClickListener(v -> {
                if (jobDetailCard != null) jobDetailCard.setVisibility(View.GONE);
            });
        }

        View viewJobBtn = view.findViewById(R.id.btn_accept_job);
        if (viewJobBtn != null) {
            viewJobBtn.setOnClickListener(v -> {
                if (currentDetailJob != null && "COMMUNITY".equals(currentDetailJob.type)) {
                    showVolunteerSheet(currentDetailJob.title);
                } else if (currentDetailJob != null) {
                    showGigApplySheet(currentDetailJob);
                }
            });
        }
    }

    private void initializeSampleJobs() {
        allJobs.add(new Job(
            "Plumbing Repair",
            "Pipe repair in kitchen area",
            "0.5km away",
            "₹500 - 800",
            "High Urgency",
            R.drawable.ic_plumber,
            R.color.sapphire_primary,
            new LatLng(19.0850, 72.8750),
            "GIG"
        ));

        allJobs.add(new Job(
            "TV Mounting",
            "Wall mount installation needed",
            "1.2km away",
            "₹1200 - 1500",
            "Normal",
            R.drawable.ic_toolbox_seeker,
            R.color.sapphire_primary,
            new LatLng(19.0650, 72.8650),
            "GIG"
        ));

        allJobs.add(new Job(
            "Electrical Work",
            "Wiring repair needed",
            "0.8km away",
            "₹800 - 1200",
            "High Urgency",
            R.drawable.ic_plug_blue,
            R.color.sapphire_primary,
            new LatLng(19.0750, 72.8800),
            "GIG"
        ));

        // Add community volunteer posts
        Job communityJob1 = new Job(
            "Grocery Assistance",
            "Help an elderly neighbor with weekly grocery run",
            "0.4km away",
            "",
            "Normal",
            R.drawable.ic_gardening,
            R.color.brand_success,
            new LatLng(19.0790, 72.8720),
            "COMMUNITY"
        );
        communityJob1.slots = 5;
        communityJob1.slotsFilled = 1;
        allJobs.add(communityJob1);

        Job communityJob2 = new Job(
            "Park Cleanup",
            "Community park cleaning drive this Saturday",
            "0.9km away",
            "",
            "Normal",
            R.drawable.ic_gardening,
            R.color.brand_success,
            new LatLng(19.0720, 72.8810),
            "COMMUNITY"
        );
        communityJob2.slots = 8;
        communityJob2.slotsFilled = 0;
        allJobs.add(communityJob2);

        filteredJobs.addAll(allJobs);
        updateBottomSheetCount();
    }

    private void updateChipAppearance(Chip chip, boolean isSelected) {
        if (isSelected) {
            chip.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.sapphire_primary)));
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        } else {
            chip.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_body_dark));
        }
    }

    private void applyFilters() {
        filteredJobs.clear();

        for (Job job : allJobs) {
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

        if (jobAdapter != null) {
            jobAdapter.updateList(filteredJobs);
        }
        updateMapMarkers();
        updateBottomSheetCount();
    }

    private void performSearch(String query) {
        if (filteredJobs == null) return;

        filteredJobs.clear();

        if (query.isEmpty()) {
            applyFilters();
            return;
        }

        String queryLower = query.toLowerCase();

        for (Job job : allJobs) {
            if (job.title.toLowerCase().contains(queryLower) ||
                job.description.toLowerCase().contains(queryLower)) {
                filteredJobs.add(job);
            }
        }

        if (jobAdapter != null) {
            jobAdapter.updateList(filteredJobs);
        }
        updateMapMarkers();
        updateBottomSheetCount();
    }

    private void recenterMap() {
        if (mMap != null) {
            LatLng mumbai = new LatLng(19.0760, 72.8777);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mumbai, 14f));
        }
    }

    private void updateMapMarkers() {
        if (mMap == null) return;
        mMap.clear();
        jobToMarkerMap.clear();

        int yellow = ContextCompat.getColor(requireContext(), R.color.sapphire_tertiary);
        int green = ContextCompat.getColor(requireContext(), R.color.brand_success);

        for (Job job : filteredJobs) {
            // Use green for community, yellow for gigs
            int markerColor = "COMMUNITY".equals(job.type) ? green : yellow;

            Marker marker = mMap.addMarker(new MarkerOptions()
                .position(job.location)
                .title(job.title)
                .snippet(job.description)
                .icon(getMarkerBitmapDescriptor(job.title, job.iconResId, markerColor, false)));

            if (marker != null) {
                jobToMarkerMap.put(job, marker);
                marker.setTag(new MarkerData(job.iconResId, markerColor));
            }
        }
    }

    private void selectJob(Job job) {
        int yellow = ContextCompat.getColor(requireContext(), R.color.sapphire_tertiary);
        int green = ContextCompat.getColor(requireContext(), R.color.brand_success);

        // Clear previous selection
        for (Map.Entry<Job, Marker> entry : jobToMarkerMap.entrySet()) {
            Marker marker = entry.getValue();
            Job j = entry.getKey();
            if (marker.getTag() instanceof MarkerData) {
                MarkerData data = (MarkerData) marker.getTag();
                int markerColor = "COMMUNITY".equals(j.type) ? green : yellow;
                marker.setIcon(getMarkerBitmapDescriptor(marker.getTitle(), data.iconResId, markerColor, false));
            }
        }

        // Highlight new selection
        if (jobToMarkerMap.containsKey(job)) {
            Marker marker = jobToMarkerMap.get(job);
            if (marker != null) {
                int markerColor = "COMMUNITY".equals(job.type) ? green : yellow;
                marker.setIcon(getMarkerBitmapDescriptor(job.title, job.iconResId, markerColor, true));
                selectedMarker = marker;
                mMap.animateCamera(CameraUpdateFactory.newLatLng(job.location));
            }
        }
    }

    private void showJobDetailCard(Job job) {
        if (jobDetailCard == null) return;

        jobDetailCard.setVisibility(View.VISIBLE);
        currentDetailJob = job;

        TextView title = jobDetailCard.findViewById(R.id.job_title);
        TextView description = jobDetailCard.findViewById(R.id.job_description);
        TextView distance = jobDetailCard.findViewById(R.id.job_distance);
        TextView budget = jobDetailCard.findViewById(R.id.job_budget);
        TextView tagGig = jobDetailCard.findViewById(R.id.tag_gig);
        ImageView icon = jobDetailCard.findViewById(R.id.ic_job_type);
        MaterialButton actionBtn = jobDetailCard.findViewById(R.id.btn_accept_job);

        if (title != null) title.setText(job.title);
        if (description != null) description.setText(job.description);
        if (distance != null) distance.setText(job.distance);
        if (budget != null) budget.setText(job.budget);
        if (tagGig != null) tagGig.setText(job.type);

        if (icon != null) {
            icon.setImageResource(job.iconResId);
            int color = ContextCompat.getColor(requireContext(), job.colorResId);
            ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(color));
        }

        // Update action button based on job type
        if (actionBtn != null) {
            if ("COMMUNITY".equals(job.type)) {
                actionBtn.setText("Help");
                actionBtn.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.brand_success)));
            } else {
                actionBtn.setText("View Job");
                actionBtn.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.sapphire_primary)));
            }
        }

        // Display slots for COMMUNITY jobs
        View rowSlots = jobDetailCard.findViewById(R.id.row_slots);
        TextView tvSlots = jobDetailCard.findViewById(R.id.tvSlotsAvailable);
        if ("COMMUNITY".equals(job.type) && job.slots > 0) {
            if (rowSlots != null) rowSlots.setVisibility(View.VISIBLE);
            if (tvSlots != null) tvSlots.setText(job.slotsFilled + " / " + job.slots + " slots filled");
        } else {
            if (rowSlots != null) rowSlots.setVisibility(View.GONE);
        }
    }

    private void updateBottomSheetCount() {
        if (bottomSheetCount != null) {
            bottomSheetCount.setText(filteredJobs.size() + " available");
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

        if (RoleManager.ROLE_PROVIDER.equals(currentRole)) {
            updateMapMarkers();
            setupProviderMapListeners();
        } else {
            updateMarkers();
            setupSeekerMapListeners();
        }
    }

    private void setupProviderMapListeners() {
        mMap.setOnMarkerClickListener(marker -> {
            // Find job by marker
            for (Job job : filteredJobs) {
                if (jobToMarkerMap.containsKey(job) && jobToMarkerMap.get(job) == marker) {
                    selectJob(job);
                    showJobDetailCard(job);
                    return true;
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

    private void setupSeekerMapListeners() {
        mMap.setOnMarkerClickListener(marker -> {
            if (selectedMarker != null && selectedMarker.getTag() instanceof MarkerData) {
                MarkerData oldData = (MarkerData) selectedMarker.getTag();
                selectedMarker.setIcon(getMarkerBitmapDescriptor(selectedMarker.getTitle(), oldData.iconResId, oldData.color, false));
            }

            if (marker.getTag() instanceof MarkerData) {
                MarkerData newData = (MarkerData) marker.getTag();
                marker.setIcon(getMarkerBitmapDescriptor(marker.getTitle(), newData.iconResId, newData.color, true));
            }

            selectedMarker = marker;
            showInfoCard(marker);
            return true;
        });

        mMap.setOnMapClickListener(latLng -> {
            if (infoCard != null) infoCard.setVisibility(View.GONE);
        });
    }

    private void updateMarkers() {
        if (mMap == null) return;
        mMap.clear();

        int blue = ContextCompat.getColor(requireContext(), R.color.sapphire_primary);
        int green = ContextCompat.getColor(requireContext(), R.color.brand_success);
        int yellow = ContextCompat.getColor(requireContext(), R.color.sapphire_tertiary);

        if (isGigsMode) {
            addSampleMarker(new LatLng(19.0820, 72.8850), "Marcus Watts", "Electrician", blue, R.drawable.ic_plug_blue);
            addSampleMarker(new LatLng(19.0700, 72.8700), "Sarah Chen", "Cleaning", blue, R.drawable.ic_cleaning);
        } else {
            addSampleMarker(new LatLng(19.0750, 72.8800), "Park Cleanup", "Community", green, R.drawable.ic_gardening);
        }
    }

    private void addSampleMarker(LatLng pos, String title, String snippet, int color, int iconResId) {
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(pos)
                .title(title)
                .snippet(snippet)
                .icon(getMarkerBitmapDescriptor(title, iconResId, color, false)));
        if (marker != null) marker.setTag(new MarkerData(iconResId, color));
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

        paint.setColor(Color.parseColor("#40000000"));
        canvas.drawCircle(centerX, centerY + 3, iconSize / 2f - 2, paint);

        paint.setColor(bgColor);
        canvas.drawCircle(centerX, centerY, iconSize / 2f - 2, paint);

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

    private void showInfoCard(Marker marker) {
        if (infoCard == null) return;
        infoCard.setVisibility(View.VISIBLE);
        if (RoleManager.ROLE_SEEKER.equals(currentRole)) {
            TextView name = infoCard.findViewById(R.id.provider_name);
            TextView desc = infoCard.findViewById(R.id.provider_desc);
            if (name != null) name.setText(marker.getTitle());
            if (desc != null) desc.setText(marker.getSnippet() + " • 0.8 miles away");
        } else {
            TextView title = infoCard.findViewById(R.id.job_title);
            if (title != null) title.setText(marker.getTitle());
        }
    }

    /**
     * Shows the volunteer message bottom sheet.
     */
    private void showVolunteerSheet(String postTitle) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.layout_community_respond_sheet, null);
        dialog.setContentView(sheetView);

        MaterialButton applyBtn = sheetView.findViewById(R.id.btn_apply_volunteer);
        if (applyBtn != null) {
            applyBtn.setOnClickListener(v -> {
                // Increment slot count if available
                if (currentDetailJob != null && currentDetailJob.slots > 0) {
                    currentDetailJob.slotsFilled = Math.min(currentDetailJob.slotsFilled + 1, currentDetailJob.slots);
                    showJobDetailCard(currentDetailJob);  // Refresh card with new count
                }
                dialog.dismiss();
                showSuccessDialog();
            });
        }

        dialog.show();
    }

    /**
     * Shows the gig application bottom sheet for GIG type jobs.
     */
    private void showGigApplySheet(Job job) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.layout_gig_apply_sheet, null);
        dialog.setContentView(sheetView);

        EditText messageField = sheetView.findViewById(R.id.et_gig_message);
        View cardCash = sheetView.findViewById(R.id.card_cash);
        View cardUpi = sheetView.findViewById(R.id.card_upi);
        MaterialButton submitBtn = sheetView.findViewById(R.id.btn_submit_application);

        // Track selected payment method (default to Cash)
        final String[] selectedPayment = {"CASH"};

        // Handle Cash card selection
        if (cardCash != null) {
            cardCash.setOnClickListener(v -> {
                selectedPayment[0] = "CASH";
                updatePaymentCardUI(cardCash, cardUpi, true);
            });
        }

        // Handle UPI card selection
        if (cardUpi != null) {
            cardUpi.setOnClickListener(v -> {
                selectedPayment[0] = "UPI";
                updatePaymentCardUI(cardCash, cardUpi, false);
            });
        }

        // Handle submit
        if (submitBtn != null) {
            submitBtn.setOnClickListener(v -> {
                String message = messageField != null ? messageField.getText().toString().trim() : "";

                if (message.isEmpty()) {
                    if (messageField != null) {
                        messageField.setError("Please write a short message about yourself");
                    }
                    return;
                }

                dialog.dismiss();
                showGigApplicationSuccessDialog();
            });
        }

        dialog.show();
    }

    /**
     * Updates the visual state of payment method cards.
     */
    private void updatePaymentCardUI(View cardCash, View cardUpi, boolean isCashSelected) {
        if (cardCash != null) {
            MaterialCardView cashCard = (MaterialCardView) cardCash;
            if (isCashSelected) {
                cashCard.setStrokeWidth(2);
                cashCard.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.sapphire_primary));
                cashCard.setCardElevation(2);
            } else {
                cashCard.setStrokeWidth(1);
                cashCard.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.text_muted));
                cashCard.setCardElevation(0);
            }
        }

        if (cardUpi != null) {
            MaterialCardView upiCard = (MaterialCardView) cardUpi;
            if (!isCashSelected) {
                upiCard.setStrokeWidth(2);
                upiCard.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.sapphire_primary));
                upiCard.setCardElevation(2);
            } else {
                upiCard.setStrokeWidth(1);
                upiCard.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.text_muted));
                upiCard.setCardElevation(0);
            }
        }
    }

    /**
     * Shows success dialog for gig application.
     */
    private void showGigApplicationSuccessDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Application Sent!")
                .setMessage("The seeker will review your request. You'll be notified when they respond.")
                .setIcon(android.R.drawable.ic_dialog_info);

        Dialog dialog = builder.show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }, 3000);
    }

    /**
     * Shows success dialog and auto-closes after 3 seconds.
     */
    private void showSuccessDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("You're in!")
                .setMessage("Your response has been sent. Redirecting to home...")
                .setIcon(android.R.drawable.ic_dialog_info);

        Dialog dialog = builder.show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }, 3000);
    }

    /**
     * Navigates to ProviderJobDetailActivity with job details.
     */
    private void showProviderJobDetail(Job job) {
        Intent intent = new Intent(requireContext(), ProviderJobDetailActivity.class);
        intent.putExtra("title", job.title);
        intent.putExtra("description", job.description);
        intent.putExtra("category", job.category);
        intent.putExtra("budget", job.budget);
        intent.putExtra("distance", job.distance);
        intent.putExtra("duration", "2-3 hours");
        intent.putExtra("type", job.type);
        startActivity(intent);
    }
}
