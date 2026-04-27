package com.example.nearneed;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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

import okhttp3.OkHttpClient;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

import androidx.recyclerview.widget.LinearLayoutManager;
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

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private MapLibreMap mMap;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private MapView mapView;
    private String currentRole;
    private PostViewModel postViewModel;

    // UI elements - Common
    private View infoCard;
    private Marker selectedMarker;
    private ApplicationViewModel applicationViewModel;

    // UI elements - Provider specific
    private RecyclerView jobsList;
    private List<Job> filteredJobs = new ArrayList<>();
    private Map<Job, Marker> jobToMarkerMap = new HashMap<>();
    private Map<Marker, MarkerData> markerDataMap = new HashMap<>();

    // Search logic
    private EditText searchEditText;
    private RecyclerView rvSearchPredictions;
    private SearchPredictionsAdapter searchAdapter;
    private List<Address> searchResults = new ArrayList<>();
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    // Real-time state
    private List<Post> latestPosts = new ArrayList<>();
    private List<UserProfile> latestProviders = new ArrayList<>();

    private static class MarkerData {
        int iconResId;
        int color;
        String title;
        String snippet;
        String budget;
        String type;
        String postId;
        String creatorId;
        MarkerData(int icon, int col, String title, String snippet, String budget, String type, String postId, String creatorId) { 
            this.iconResId = icon; 
            this.color = col;
            this.title = title;
            this.snippet = snippet;
            this.budget = budget;
            this.type = type;
            this.postId = postId;
            this.creatorId = creatorId;
        }
    }

    // Job data class
    public static class Job {
        public String title;
        public String description;
        public String distance;
        public String budget;
        public String category; 
        public int iconResId;
        public int colorResId;
        public LatLng location;
        public String type; 
        public String postId;
        public String creatorId;

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
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted && mMap != null && mMap.getStyle() != null) {
                enableLocationComponent(mMap.getStyle());
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Context context = getContext();
        if (context != null) {
            MapLibre.getInstance(context.getApplicationContext());
            currentRole = RoleManager.getRole(context);
        } else {
            currentRole = RoleManager.ROLE_SEEKER;
        }

        int layoutId = RoleManager.ROLE_PROVIDER.equals(currentRole) ? R.layout.layout_maps_provider : R.layout.layout_maps_seeker;
        View view = inflater.inflate(layoutId, container, false);

        if (getActivity() != null) {
            postViewModel = new ViewModelProvider(requireActivity()).get(PostViewModel.class);
            applicationViewModel = new ViewModelProvider(requireActivity(), 
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(ApplicationViewModel.class);
        }

        int mapId = RoleManager.ROLE_PROVIDER.equals(currentRole) ? R.id.provider_map : R.id.map;
        mapView = view.findViewById(mapId);
        
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        }

        if (RoleManager.ROLE_PROVIDER.equals(currentRole)) {
            initProviderUI(view);
        } else {
            initSeekerUI(view);
        }

        if (getActivity() != null) {
            SeekerNavbarController.bind(getActivity(), view, SeekerNavbarController.TAB_MAP);
        }

        return view;
    }

    private void initProviderUI(View view) {
        infoCard = view.findViewById(R.id.provider_info_card);
        View closeBtn = view.findViewById(R.id.ic_close_card);
        if (closeBtn != null) closeBtn.setOnClickListener(v -> {
            if (infoCard != null) infoCard.setVisibility(View.GONE);
        });

        View bottomSheet = view.findViewById(R.id.provider_bottom_sheet);
        if (bottomSheet != null) bottomSheet.setVisibility(View.GONE);

        // Search Implementation
        searchEditText = view.findViewById(R.id.provider_search_edit_text);
        rvSearchPredictions = view.findViewById(R.id.rv_search_predictions);
        
        if (rvSearchPredictions != null && getContext() != null) {
            rvSearchPredictions.setLayoutManager(new LinearLayoutManager(getContext()));
            searchAdapter = new SearchPredictionsAdapter(searchResults, address -> {
                moveCameraToAddress(address);
                if (rvSearchPredictions != null) rvSearchPredictions.setVisibility(View.GONE);
                if (searchEditText != null) searchEditText.setText(address.getAddressLine(0));
            });
            rvSearchPredictions.setAdapter(searchAdapter);
        }

        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    scheduleSearch(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
            searchEditText.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch(v.getText().toString());
                    return true;
                }
                return false;
            });
        }

        MaterialButton btnViewJob = view.findViewById(R.id.btn_accept_job);
        if (btnViewJob != null) {
            btnViewJob.setOnClickListener(v -> {
                MarkerData data = (selectedMarker != null) ? markerDataMap.get(selectedMarker) : null;
                if (data != null && getContext() != null) {
                    Intent intent;
                    if ("COMMUNITY".equalsIgnoreCase(data.type)) {
                        intent = new Intent(getContext(), CommunityPostDetailActivity.class);
                    } else {
                        intent = new Intent(getContext(), RequestDetailActivity.class);
                    }
                    intent.putExtra("post_id", data.postId);
                    intent.putExtra("title", data.title);
                    intent.putExtra("description", data.snippet);
                    intent.putExtra("creator_id", data.creatorId);
                    startActivity(intent);
                }
            });
        }
        
        View recenterBtn = view.findViewById(R.id.ic_recenter_location);
        if (recenterBtn != null) {
            recenterBtn.setOnClickListener(v -> {
                if (mMap != null && mMap.getStyle() != null) {
                    enableLocationComponent(mMap.getStyle());
                }
            });
        }
    }

    private void scheduleSearch(String query) {
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
        if (query.length() < 3) {
            if (rvSearchPredictions != null) rvSearchPredictions.setVisibility(View.GONE);
            return;
        }
        searchRunnable = () -> performSearch(query);
        searchHandler.postDelayed(searchRunnable, 600);
    }

    private void performSearch(String query) {
        if (query == null || query.isEmpty() || getContext() == null) return;
        
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        new Thread(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocationName(query, 5);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (addresses != null && !addresses.isEmpty()) {
                            searchResults.clear();
                            searchResults.addAll(addresses);
                            if (searchAdapter != null) searchAdapter.notifyDataSetChanged();
                            if (rvSearchPredictions != null) rvSearchPredictions.setVisibility(View.VISIBLE);
                        } else {
                            if (rvSearchPredictions != null) rvSearchPredictions.setVisibility(View.GONE);
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void moveCameraToAddress(Address address) {
        if (mMap != null) {
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f));
        }
    }

    private void initSeekerUI(View view) {
        infoCard = view.findViewById(R.id.seeker_info_card);
        MaterialButton btnBook = view.findViewById(R.id.btn_book_now);
        if (btnBook != null) {
            btnBook.setOnClickListener(v -> {
                Context context = getContext();
                if (context != null) {
                    Toast.makeText(context, "Booking system coming soon", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onMapReady(@NonNull MapLibreMap mapLibreMap) {
        mMap = mapLibreMap;
        String styleUrl = "https://api.maptiler.com/maps/streets/style.json?key=64XK5FEM8kBYRPoTcYBu";
        mMap.setStyle(new Style.Builder().fromUri(styleUrl), style -> {
            if (mMap == null || !isAdded()) return;
            
            LatLng defaultPos = new LatLng(28.4595, 77.0266);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultPos, 13f));

            if (RoleManager.ROLE_PROVIDER.equals(currentRole)) {
                observeRealTimeDataForProvider();
            } else {
                observeRealTimeDataForSeeker();
            }
            
            enableLocationComponent(style);
            
            mMap.setOnMarkerClickListener(this::onMarkerClicked);
            mMap.addOnMapClickListener(latLng -> {
                if (infoCard != null) infoCard.setVisibility(View.GONE);
                return true;
            });
        });
    }

    private boolean onMarkerClicked(@NonNull Marker marker) {
        if (!isAdded()) return false;
        
        if (selectedMarker != null && markerDataMap.containsKey(selectedMarker)) {
            MarkerData oldData = markerDataMap.get(selectedMarker);
            if (oldData != null) {
                selectedMarker.setIcon(getMarkerBitmapDescriptor(selectedMarker.getTitle(), oldData.iconResId, oldData.color, false));
            }
        }

        if (markerDataMap.containsKey(marker)) {
            MarkerData newData = markerDataMap.get(marker);
            if (newData != null) {
                marker.setIcon(getMarkerBitmapDescriptor(marker.getTitle(), newData.iconResId, newData.color, true));
                selectedMarker = marker;
                showInfoCard(marker, newData);
                if (mMap != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                }
            }
        }
        return true;
    }

    private void showInfoCard(Marker marker, MarkerData data) {
        if (infoCard == null || data == null || !isAdded()) return;
        infoCard.setVisibility(View.VISIBLE);
        
        TextView titleTv = infoCard.findViewById(RoleManager.ROLE_SEEKER.equals(currentRole) ? R.id.provider_name : R.id.job_title);
        TextView descTv = infoCard.findViewById(RoleManager.ROLE_SEEKER.equals(currentRole) ? R.id.provider_desc : R.id.job_description);
        
        if (titleTv != null) titleTv.setText(data.title != null ? data.title : "Untitled");
        if (descTv != null) {
            String type = data.type != null ? data.type : "";
            String budgetStr = (data.budget != null && !data.budget.isEmpty() && !"N/A".equals(data.budget)) ? " • ₹" + data.budget : "";
            descTv.setText(type + budgetStr);
        }

        if (RoleManager.ROLE_PROVIDER.equals(currentRole)) {
            TextView budgetTv = infoCard.findViewById(R.id.job_budget);
            if (budgetTv != null) budgetTv.setText("₹" + (data.budget != null ? data.budget : "0"));
        }
    }

    private void observeRealTimeDataForProvider() {
        if (postViewModel == null || !isAdded()) return;
        postViewModel.getNearbyPosts().observe(getViewLifecycleOwner(), posts -> {
            if (posts != null && isAdded()) {
                latestPosts = posts;
                refreshProviderMap();
            }
        });
        postViewModel.observeAllActivePosts();
    }

    private void refreshProviderMap() {
        if (mMap == null || !isAdded()) return;
        mMap.removeAnnotations();
        jobToMarkerMap.clear();
        markerDataMap.clear();
        filteredJobs.clear();

        Context context = getContext();
        if (context == null) return;

        int orange = ContextCompat.getColor(context, R.color.brand_warning);
        int green = ContextCompat.getColor(context, R.color.brand_success);

        for (Post p : latestPosts) {
            if (p == null) continue;
            LatLng pos = new LatLng(p.latitude != null ? p.latitude : 28.4595, p.longitude != null ? p.longitude : 77.0266);
            int color = "COMMUNITY".equalsIgnoreCase(p.type) ? green : orange;
            int icon = "COMMUNITY".equalsIgnoreCase(p.type) ? R.drawable.ic_gardening : R.drawable.ic_plumber;
            
            String title = p.title != null ? p.title : "Request";
            Marker marker = mMap.addMarker(new MarkerOptions()
                .position(pos)
                .title(title)
                .icon(getMarkerBitmapDescriptor(title, icon, color, false)));
            
            Job j = new Job(title, p.description, p.distance, p.budget, p.urgency, icon, color, pos, p.type);
            j.postId = p.postId;
            j.creatorId = p.createdBy;
            
            jobToMarkerMap.put(j, marker);
            markerDataMap.put(marker, new MarkerData(icon, color, title, p.description, p.budget, p.type, p.postId, p.createdBy));
            filteredJobs.add(j);
        }
    }

    private void observeRealTimeDataForSeeker() {
        if (postViewModel == null || !isAdded()) return;
        UserProfileRepository.observeAllProviders(latestProviders, this::refreshSeekerMap);
        postViewModel.getNearbyPosts().observe(getViewLifecycleOwner(), posts -> {
            if (posts != null && isAdded()) {
                latestPosts = posts;
                refreshSeekerMap();
            }
        });
        Context context = getContext();
        if (context != null) {
            postViewModel.observeNearbyPosts(context, 28.4595, 77.0266, 10.0);
        }
    }

    private void refreshSeekerMap() {
        if (mMap == null || !isAdded()) return;
        mMap.removeAnnotations();
        markerDataMap.clear();

        Context context = getContext();
        if (context == null) return;

        int blue = ContextCompat.getColor(context, R.color.palette_primary);
        int orange = ContextCompat.getColor(context, R.color.brand_warning);
        int green = ContextCompat.getColor(context, R.color.brand_success);

        for (UserProfile p : latestProviders) {
            if (p != null && p.latitude != null && p.longitude != null) {
                LatLng pos = new LatLng(p.latitude, p.longitude);
                String name = p.name != null ? p.name : "Provider";
                Marker marker = mMap.addMarker(new MarkerOptions().position(pos).title(name).icon(getMarkerBitmapDescriptor(name, R.drawable.ic_toolbox_seeker, blue, false)));
                markerDataMap.put(marker, new MarkerData(R.drawable.ic_toolbox_seeker, blue, name, p.bio, "N/A", "PROVIDER", null, p.userId));
            }
        }

        for (Post p : latestPosts) {
            if (p == null) continue;
            LatLng pos = new LatLng(p.latitude != null ? p.latitude : 28.4595, p.longitude != null ? p.longitude : 77.0266);
            int color = "COMMUNITY".equalsIgnoreCase(p.type) ? green : orange;
            int icon = "COMMUNITY".equalsIgnoreCase(p.type) ? R.drawable.ic_gardening : R.drawable.ic_plumber;
            String title = p.title != null ? p.title : "Request";
            Marker marker = mMap.addMarker(new MarkerOptions().position(pos).title(title).icon(getMarkerBitmapDescriptor(title, icon, color, false)));
            markerDataMap.put(marker, new MarkerData(icon, color, title, p.description, p.budget, p.type, p.postId, p.createdBy));
        }
    }

    private Icon getMarkerBitmapDescriptor(String title, int iconResId, int bgColor, boolean isSelected) {
        Context context = getContext();
        if (context == null) return null;

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
        
        Drawable drawable = ContextCompat.getDrawable(context, iconResId);
        if (drawable != null) {
            drawable.setTint(Color.WHITE);
            int p = 25;
            drawable.setBounds((int)(centerX - iconSize/2f + p), (int)(centerY - iconSize/2f + p), (int)(centerX + iconSize/2f - p), (int)(centerY + iconSize/2f - p));
            drawable.draw(canvas);
        }
        
        String safeTitle = title != null ? title : "Pin";
        String label = safeTitle.length() > 12 ? safeTitle.substring(0, 10) + ".." : safeTitle;
        paint.setTextSize(24f); 
        paint.setFakeBoldText(true);
        float tw = paint.measureText(label);
        float bh = 36f; 
        float bw = tw + 32f;
        float bl = centerX - (bw / 2f); 
        float bt = iconSize + 4;
        
        paint.setColor(isSelected ? ContextCompat.getColor(context, R.color.palette_primary) : Color.WHITE);
        canvas.drawRoundRect(bl, bt, bl + bw, bt + bh, 10f, 10f, paint);
        
        paint.setColor(isSelected ? Color.WHITE : Color.parseColor("#0F172A"));
        canvas.drawText(label, bl + 16f, bt + bh - 10f, paint);
        
        return IconFactory.getInstance(context).fromBitmap(bitmap);
    }

    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(@NonNull Style style) {
        if (!isAdded()) return;
        Context context = getContext();
        if (context != null && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationComponent lc = mMap.getLocationComponent();
            lc.activateLocationComponent(LocationComponentActivationOptions.builder(context, style).useDefaultLocationEngine(true).build());
            lc.setLocationComponentEnabled(true);
            lc.setCameraMode(CameraMode.TRACKING);
            lc.setRenderMode(RenderMode.COMPASS);
        } else if (context != null) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @Override public void onStart() { super.onStart(); if (mapView != null) mapView.onStart(); }
    @Override public void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
    @Override public void onPause() { super.onPause(); if (mapView != null) mapView.onPause(); }
    @Override public void onStop() { super.onStop(); if (mapView != null) mapView.onStop(); }
    @Override public void onLowMemory() { super.onLowMemory(); if (mapView != null) mapView.onLowMemory(); }
    @Override public void onDestroyView() { 
        if (mapView != null) {
            mapView.onDestroy(); 
        }
        super.onDestroyView(); 
    }
    @Override public void onSaveInstanceState(@NonNull Bundle outState) { super.onSaveInstanceState(outState); if (mapView != null) mapView.onSaveInstanceState(outState); }

    // Helper Adapter for Search Predictions
    private static class SearchPredictionsAdapter extends RecyclerView.Adapter<SearchPredictionsAdapter.ViewHolder> {
        private final List<Address> results;
        private final OnAddressClickListener listener;

        interface OnAddressClickListener { void onAddressClick(Address address); }

        SearchPredictionsAdapter(List<Address> results, OnAddressClickListener listener) {
            this.results = results;
            this.listener = listener;
        }

        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(view);
        }

        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Address address = results.get(position);
            holder.text1.setText(address.getFeatureName());
            holder.text2.setText(address.getAddressLine(0));
            holder.itemView.setOnClickListener(v -> listener.onAddressClick(address));
        }

        @Override public int getItemCount() { return results.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            ViewHolder(View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}
