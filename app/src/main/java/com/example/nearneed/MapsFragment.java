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

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import org.maplibre.android.location.LocationComponent;
import org.maplibre.android.location.LocationComponentActivationOptions;
import org.maplibre.android.location.modes.CameraMode;
import org.maplibre.android.location.modes.RenderMode;

import androidx.recyclerview.widget.RecyclerView;

import org.maplibre.android.MapLibre;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.Style;
import org.maplibre.android.camera.CameraUpdateFactory;

import org.maplibre.android.maps.OnMapReadyCallback;
import org.maplibre.android.annotations.Icon;
import org.maplibre.android.annotations.IconFactory;



import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import java.io.IOException;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.annotations.Marker;
import org.maplibre.android.annotations.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;

import androidx.lifecycle.ViewModelProvider;
import java.util.HashMap;
import java.util.Map;
import com.google.firebase.auth.FirebaseAuth;


public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private MapLibreMap mMap;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private MapView mapView;
    private String currentRole;
    private boolean isGigsMode = true;

    // UI elements - Common
    
    private View infoCard;
    private Marker selectedMarker;

    // UI elements - Provider specific
    private MaterialCardView jobDetailCard;
    private Chip chipUrgency, chipBudget, chipDistance;
    private EditText providerSearchEdit;
    private ImageView recenterButton;
    private RecyclerView jobsList;
    private JobListAdapter jobAdapter;
    private List<Job> allJobs = new ArrayList<>();
    private List<Job> filteredJobs = new ArrayList<>();
    private BottomSheetBehavior<?> bottomSheetBehavior;
    private View bottomSheetPanel;
    private TextView bottomSheetCount;
    private PostViewModel postViewModel;


    // Filter state
    private boolean filterUrgency = false;
    private boolean filterBudget = false;
    private boolean filterDistance = false;

    // Map of jobs to markers for syncing
    private Map<Job, Marker> jobToMarkerMap;
    private Map<Marker, MarkerData> markerDataMap;

    // Track currently displayed job in detail card
    private Job currentDetailJob;

    private RecyclerView rvPredictions;
    private SearchPredictionAdapter searchPredictionAdapter;
    private OkHttpClient httpClient;
    private final ExecutorService geocodeExecutor = Executors.newFixedThreadPool(2);
    private Handler searchHandler;
    private Runnable searchRunnable;


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
        public boolean hasApplied;     // true if provider has already applied
        public String applicationStatus; // "pending", "accepted", "rejected"

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
            this.hasApplied = false;
            this.applicationStatus = null;
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted && mMap != null && mMap.getStyle() != null) {
                enableLocationComponent(mMap.getStyle());
            } else if (!isGranted) {
                Toast.makeText(requireContext(), "Location permission denied. Showing default location.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationComponent locationComponent = mMap.getLocationComponent();
            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(requireContext(), loadedMapStyle)
                            .useDefaultLocationEngine(true)
                            .build();

            locationComponent.activateLocationComponent(locationComponentActivationOptions);
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle bundle) {
        // Initialize MapLibre before inflating layout
        MapLibre.getInstance(requireContext());
        
        jobToMarkerMap = new HashMap<>();
        markerDataMap = new HashMap<>();
        currentRole = RoleManager.getRole(requireContext());

        int layoutId = RoleManager.ROLE_SEEKER.equals(currentRole) ?
                R.layout.layout_maps_seeker : R.layout.layout_maps_provider;

        View view = inflater.inflate(layoutId, container, false);

        
        httpClient = new OkHttpClient();
        searchHandler = new Handler(Looper.getMainLooper());
        
        rvPredictions = view.findViewById(R.id.rv_search_predictions);
        if (rvPredictions != null) {
            rvPredictions.setLayoutManager(new LinearLayoutManager(requireContext()));
            searchPredictionAdapter = new SearchPredictionAdapter((lat, lng, name) -> {
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null && view.getWindowToken() != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                rvPredictions.setVisibility(View.GONE);
                if (mMap != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 15f));
                }
            });
            rvPredictions.setAdapter(searchPredictionAdapter);
        }

        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);

        initUI(view);

        if (RoleManager.ROLE_PROVIDER.equals(currentRole)) {
            setupProviderMode(view);
        } else {
            allJobs = new ArrayList<>();
            filteredJobs = new ArrayList<>();
        }

        setupObservers();
        applyInitialSearchState(view);

        // Obtain the MapView
        mapView = view.findViewById(RoleManager.ROLE_SEEKER.equals(currentRole) ? R.id.map : R.id.provider_map);
        if (mapView != null) {
            mapView.onCreate(bundle);
            mapView.getMapAsync(this);
        }

        SeekerNavbarController.bind(requireActivity(), view, SeekerNavbarController.TAB_MAP);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mapView != null) mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mapView != null) mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapView != null) mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
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
            setupSearchIntegration(searchEdit);
            
            ImageView icCompass = view.findViewById(R.id.ic_compass);
            if (icCompass != null) {
                icCompass.setOnClickListener(v -> recenterMapToUser());
            }

        } else {

            infoCard = view.findViewById(R.id.provider_info_card);
        }

        View closeBtn = view.findViewById(R.id.ic_close_card);
        if (closeBtn != null) closeBtn.setOnClickListener(v -> {
            if (infoCard != null) infoCard.setVisibility(View.GONE);
        });
    }
    private void setupProviderMode(View view) {
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
        setupSearchIntegration(providerSearchEdit);
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
            recenterButton.setOnClickListener(v -> recenterMapToUser());
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
                if (currentDetailJob != null) {
                    // Check if provider has already applied
                    if (currentDetailJob.hasApplied) {
                        // Show application status dialog
                        showApplicationStatusDialog(currentDetailJob);
                    } else {
                        // Allow application
                        if ("COMMUNITY".equals(currentDetailJob.type)) {
                            showVolunteerSheet(currentDetailJob.title);
                        } else {
                            showGigApplySheet(currentDetailJob);
                        }
                    }
                }
            });
        }
    }

    private void setupObservers() {
        postViewModel.getNearbyPosts().observe(getViewLifecycleOwner(), posts -> {
            allJobs.clear();
            for (Post post : posts) {
                Job job = mapPostToJob(post);
                allJobs.add(job);
            }
            applyFilters();
        });

        // Trigger observation - default to Gurgaon
        double lat = 28.4595;
        double lng = 77.0266;
        postViewModel.observeNearbyPosts(requireContext(), lat, lng, 50.0);
    }

    private Job mapPostToJob(Post p) {
        Job job = new Job(
            p.title,
            p.description,
            p.distance != null ? p.distance : "Nearby",
            p.budget != null ? p.budget : "",
            p.urgency != null ? p.urgency : "Normal",
            p.type != null && p.type.equals("COMMUNITY") ? R.drawable.ic_gardening : R.drawable.ic_toolbox_seeker,
            p.type != null && p.type.equals("COMMUNITY") ? R.color.brand_success : R.color.sapphire_primary,
            new LatLng(p.lat != null ? p.lat : 28.4595, p.lng != null ? p.lng : 77.0266),
            p.type
        );
        job.slots = p.slots != null ? p.slots : 0;
        job.slotsFilled = p.slotsFilled != null ? p.slotsFilled : 0;
        job.hasApplied = p.hasApplied != null ? p.hasApplied : false;
        job.applicationStatus = p.applicationStatus;
        return job;
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

            // Seeker mode: Filter by GIG vs COMMUNITY toggle
            if (RoleManager.ROLE_SEEKER.equals(currentRole)) {
                String expectedType = isGigsMode ? "GIG" : "COMMUNITY";
                if (!expectedType.equals(job.type)) {
                    matches = false;
                }
            }

            if (matches && filterUrgency && !job.category.equals("High Urgency")) {
                matches = false;
            }

            if (matches && filterBudget && !job.budget.contains("500")) {
                matches = false;
            }

            if (matches && filterDistance && !job.distance.contains("0.5")) {
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

    
    private void recenterMapToUser() {
        if (mMap != null && mMap.getLocationComponent() != null && mMap.getLocationComponent().isLocationComponentActivated()) {
            mMap.getLocationComponent().setCameraMode(CameraMode.TRACKING);
            mMap.getLocationComponent().setRenderMode(RenderMode.COMPASS);
        }
    }

        private void setupSearchIntegration(EditText editText) {
        if (editText == null) return;
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchHandler != null && searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                
                searchRunnable = () -> {
                    GeocodingHelper.performSearch(s.toString(), results -> {
                        if (!results.isEmpty()) {
                            rvPredictions.setVisibility(View.VISIBLE);
                            searchPredictionAdapter.setPredictions(results);
                        } else {
                            rvPredictions.setVisibility(View.GONE);
                        }
                    });
                };
                if (searchHandler != null) {
                    searchHandler.postDelayed(searchRunnable, 500);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
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

    private void applyInitialSearchState(View view) {
        if (view == null || getActivity() == null) {
            return;
        }

        android.content.Intent intent = requireActivity().getIntent();
        String query = intent.getStringExtra("SEARCH_QUERY");
        boolean focusSearch = intent.getBooleanExtra("FOCUS_SEARCH", false);

        EditText searchEdit = RoleManager.ROLE_SEEKER.equals(currentRole)
                ? view.findViewById(R.id.search_edit_text)
                : view.findViewById(R.id.provider_search_edit_text);

        if (searchEdit == null) {
            return;
        }

        if (query != null && !query.trim().isEmpty()) {
            searchEdit.setText(query);
            searchEdit.setSelection(searchEdit.getText().length());
            focusSearch = true;
        }

        if (focusSearch) {
            view.post(() -> {
                searchEdit.requestFocus();
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(searchEdit, InputMethodManager.SHOW_IMPLICIT);
                }
            });
        }
    }

    private void recenterMap() {
        if (mMap != null) {
            LatLng gurgaon = new LatLng(28.4595, 77.0266);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gurgaon, 12f));
        }
    }


    private void updateMapMarkers() {
        if (mMap == null) return;
        mMap.removeAnnotations();
        jobToMarkerMap.clear();
        markerDataMap.clear();

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
                if (markerDataMap != null) markerDataMap.put(marker, new MarkerData(job.iconResId, markerColor));
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
            if (markerDataMap.containsKey(marker)) {
                MarkerData data = markerDataMap.get(marker);
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

        // Update action button based on job type and application status
        if (actionBtn != null) {
            if (job.hasApplied) {
                // Provider has already applied - show status
                actionBtn.setEnabled(false);
                actionBtn.setAlpha(0.6f);

                if ("pending".equals(job.applicationStatus)) {
                    actionBtn.setText("Application Pending");
                } else if ("accepted".equals(job.applicationStatus)) {
                    actionBtn.setText("✓ Selected");
                } else if ("rejected".equals(job.applicationStatus)) {
                    actionBtn.setText("Application Rejected");
                } else {
                    actionBtn.setText("Already Applied");
                }
                actionBtn.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.text_muted)));
            } else {
                // Provider hasn't applied yet - show apply option
                actionBtn.setEnabled(true);
                actionBtn.setAlpha(1.0f);

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

        

        applyFilters();
    }

    @Override
    public void onMapReady(@NonNull MapLibreMap mapLibreMap) {
        mMap = mapLibreMap;
        mMap.getUiSettings().setCompassEnabled(false);

        String styleUrl = "https://api.maptiler.com/maps/streets-v2/style.json?key=" + BuildConfig.MAPTILER_API_KEY;
        mMap.setStyle(new Style.Builder().fromUri(styleUrl), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                // Default fallback to Gurgaon
                LatLng gurgaon = new LatLng(28.4595, 77.0266);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gurgaon, 12f));

                if (RoleManager.ROLE_PROVIDER.equals(currentRole)) {
                    updateMapMarkers();
                    setupProviderMapListeners();
                } else {
                    updateMapMarkers();
                    setupSeekerMapListeners();
                }
                
                // Now check for permissions and launch tracking if allowed
                enableLocationComponent(style);
            }
        });
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

        mMap.addOnMapClickListener(latLng -> {
            if (jobDetailCard != null) {
                jobDetailCard.setVisibility(View.GONE);
            }
            return true;
        });
    }

    private void setupSeekerMapListeners() {
        mMap.setOnMarkerClickListener(marker -> {
            if (selectedMarker != null && markerDataMap.containsKey(selectedMarker)) {
                MarkerData oldData = markerDataMap.get(selectedMarker);
                selectedMarker.setIcon(getMarkerBitmapDescriptor(selectedMarker.getTitle(), oldData.iconResId, oldData.color, false));
            }

            if (markerDataMap.containsKey(marker)) {
                MarkerData newData = markerDataMap.get(marker);
                marker.setIcon(getMarkerBitmapDescriptor(marker.getTitle(), newData.iconResId, newData.color, true));
            }

            selectedMarker = marker;
            showInfoCard(marker);
            return true;
        });

        mMap.addOnMapClickListener(latLng -> {
            if (infoCard != null) infoCard.setVisibility(View.GONE);
            return true;
        });
    }

    // Legacy methods removed

    private Icon getMarkerBitmapDescriptor(String title, int iconResId, int bgColor, boolean isSelected) {
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

        return IconFactory.getInstance(requireContext()).fromBitmap(bitmap);
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
                // Mark job as applied with pending status
                if (currentDetailJob != null) {
                    currentDetailJob.hasApplied = true;
                    currentDetailJob.applicationStatus = "pending";

                    // Increment slot count if available
                    if (currentDetailJob.slots > 0) {
                        currentDetailJob.slotsFilled = Math.min(currentDetailJob.slotsFilled + 1, currentDetailJob.slots);
                    }
                }
                dialog.dismiss();
                showSuccessDialog();

                // Update the detail card to show application status
                if (currentDetailJob != null) {
                    showJobDetailCard(currentDetailJob);
                }
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
        Slider budgetSlider = sheetView.findViewById(R.id.slider_budget);
        TextView budgetValue = sheetView.findViewById(R.id.tv_budget_value);
        MaterialButton submitBtn = sheetView.findViewById(R.id.btn_submit_application);

        // Track selected payment method (default to Cash)
        final String[] selectedPayment = {"CASH"};

        // Handle budget slider
        if (budgetSlider != null && budgetValue != null) {
            budgetSlider.addOnChangeListener((slider, value, fromUser) -> {
                budgetValue.setText("₹" + String.format("%.0f", value));
            });
        }

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

                // Mark job as applied with pending status
                job.hasApplied = true;
                job.applicationStatus = "pending";

                dialog.dismiss();
                showGigApplicationSuccessDialog();

                // Update the detail card to show application status
                if (currentDetailJob != null) {
                    showJobDetailCard(currentDetailJob);
                }
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
     * Shows a dialog displaying the provider's current application status.
     */
    private void showApplicationStatusDialog(Job job) {
        String title = "Application Status";
        String message;

        if ("pending".equals(job.applicationStatus)) {
            message = "Your application is pending review. The seeker will notify you soon.";
        } else if ("accepted".equals(job.applicationStatus)) {
            message = "Congratulations! You have been selected for this job.";
        } else if ("rejected".equals(job.applicationStatus)) {
            message = "Unfortunately, your application was not selected for this opportunity.";
        } else {
            message = "You have already applied for this job. Check back for updates.";
        }

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
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
