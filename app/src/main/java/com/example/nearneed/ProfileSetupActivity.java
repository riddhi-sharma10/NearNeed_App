package com.example.nearneed;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ProfileSetupActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private MaterialButton btnContinue;
    private MaterialButton btnDetectLocation;
    private TextView tvDetectedLocation;
    private EditText etLocationSearch;
    private RecyclerView rvSearchPredictions;

    private int selectedRadius = 10;
    
    private static final int LOCATION_PERMISSION_REQ = 101;
    private FusedLocationProviderClient fusedLocationClient;
    private OkHttpClient httpClient;
    private SearchPredictionAdapter searchPredictionAdapter;
    private Handler searchHandler;
    private Runnable searchRunnable;
    private final ExecutorService geocodeExecutor = Executors.newFixedThreadPool(2);

    private double selectedLat = Double.NaN;
    private double selectedLng = Double.NaN;
    private String selectedAddress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        httpClient = new OkHttpClient();
        searchHandler = new Handler(Looper.getMainLooper());

        initViews();
        setupListeners();
        setupSearchPredictions();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnContinue = findViewById(R.id.btnContinue);
        btnDetectLocation = findViewById(R.id.btnDetectLocation);
        tvDetectedLocation = findViewById(R.id.tvDetectedLocation);
        etLocationSearch = findViewById(R.id.etLocationSearch);
        rvSearchPredictions = findViewById(R.id.rv_search_predictions);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnContinue.setOnClickListener(v -> {
            if (selectedAddress != null && !selectedAddress.isEmpty()) {
                UserPrefs.saveLocation(this, selectedAddress);
                saveLocationToFirestore(selectedAddress);
            }
            Intent intent = new Intent(this, IdVerificationActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        btnDetectLocation.setOnClickListener(v -> detectRealLocation());

        if (etLocationSearch != null) {
            etLocationSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s == null ? "" : s.toString().trim();
                    if (query.isEmpty()) {
                        if (rvSearchPredictions != null) {
                            rvSearchPredictions.setVisibility(View.GONE);
                        }
                        selectedAddress = null;
                        selectedLat = Double.NaN;
                        selectedLng = Double.NaN;
                        return;
                    }

                    if (searchHandler != null && searchRunnable != null) {
                        searchHandler.removeCallbacks(searchRunnable);
                    }

                    searchRunnable = () -> performGeocoding(query);
                    if (searchHandler != null) {
                        searchHandler.postDelayed(searchRunnable, 450);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }

    private void setupSearchPredictions() {
        if (rvSearchPredictions == null) {
            return;
        }

        rvSearchPredictions.setLayoutManager(new LinearLayoutManager(this));
        searchPredictionAdapter = new SearchPredictionAdapter((lat, lng, name) -> {
            selectedLat = lat;
            selectedLng = lng;
            selectedAddress = name;

            tvDetectedLocation.setText(name);
            tvDetectedLocation.setTextColor(0xFF0F172A);

            if (etLocationSearch != null) {
                etLocationSearch.setText(name);
                etLocationSearch.setSelection(etLocationSearch.getText().length());
            }

            rvSearchPredictions.setVisibility(View.GONE);
            hideKeyboard();

            btnDetectLocation.setEnabled(true);
            btnDetectLocation.setText("Use current location");
            btnDetectLocation.setBackgroundTintList(null);
            btnDetectLocation.setTextColor(0xFF1E3A8A);
            Toast.makeText(this, "Selected: " + name, Toast.LENGTH_SHORT).show();
        });
        rvSearchPredictions.setAdapter(searchPredictionAdapter);

        String saved = UserPrefs.getLocation(this);
        if (saved != null && !saved.isEmpty()) {
            tvDetectedLocation.setText(saved);
            tvDetectedLocation.setTextColor(0xFF0F172A);
            if (etLocationSearch != null) {
                etLocationSearch.setText(saved);
            }
        }
    }

    private void performGeocoding(String query) {
        if (query.trim().isEmpty() || searchPredictionAdapter == null || rvSearchPredictions == null) {
            if (rvSearchPredictions != null) {
                rvSearchPredictions.setVisibility(View.GONE);
            }
            return;
        }

        geocodeExecutor.submit(() -> {
            List<SearchPredictionAdapter.GeocodingResult> aggregated = new ArrayList<>();
            CountDownLatch latch = new CountDownLatch(2);

            String encodedQuery = query;
            try {
                encodedQuery = URLEncoder.encode(query, "UTF-8");
            } catch (Exception ignored) {
            }

            String photonUrl = "https://photon.komoot.io/api/?limit=5&lang=en&q=" + encodedQuery;
            Request req1 = new Request.Builder().url(photonUrl).build();
            httpClient.newCall(req1).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    latch.countDown();
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            JSONObject obj = new JSONObject(response.body().string());
                            JSONArray features = obj.optJSONArray("features");
                            if (features != null) {
                                for (int i = 0; i < features.length(); i++) {
                                    JSONObject f = features.getJSONObject(i);
                                    JSONObject p = f.optJSONObject("properties");
                                    if (p == null) {
                                        continue;
                                    }
                                    String name = p.optString("name", p.optString("street", "Unknown location"));
                                    String sec = p.optString("city", "");
                                    if (!p.optString("state", "").isEmpty()) {
                                        sec += (sec.isEmpty() ? "" : ", ") + p.optString("state");
                                    }
                                    if (!p.optString("country", "").isEmpty()) {
                                        sec += (sec.isEmpty() ? "" : ", ") + p.optString("country");
                                    }
                                    JSONArray coords = f.getJSONObject("geometry").getJSONArray("coordinates");
                                    aggregated.add(new SearchPredictionAdapter.GeocodingResult(name, sec, coords.getDouble(1), coords.getDouble(0)));
                                }
                            }
                        }
                    } catch (Exception ignored) {
                    } finally {
                        latch.countDown();
                    }
                }
            });

            String nomUrl = "https://nominatim.openstreetmap.org/search?format=json&limit=5&q=" + encodedQuery;
            Request req2 = new Request.Builder()
                    .url(nomUrl)
                    .addHeader("Accept-Language", "en")
                    .addHeader("User-Agent", "NearNeed-AndroidApp")
                    .build();
            httpClient.newCall(req2).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    latch.countDown();
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            JSONArray arr = new JSONArray(response.body().string());
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject o = arr.getJSONObject(i);
                                String[] parts = o.optString("display_name", "Unknown").split(",", 2);
                                String name = parts[0].trim();
                                String sec = parts.length > 1 ? parts[1].trim() : "";
                                aggregated.add(new SearchPredictionAdapter.GeocodingResult(name, sec, o.getDouble("lat"), o.getDouble("lon")));
                            }
                        }
                    } catch (Exception ignored) {
                    } finally {
                        latch.countDown();
                    }
                }
            });

            try {
                latch.await(4, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
            }

            List<SearchPredictionAdapter.GeocodingResult> finalList = new ArrayList<>();
            for (SearchPredictionAdapter.GeocodingResult res : aggregated) {
                boolean dup = false;
                for (SearchPredictionAdapter.GeocodingResult existing : finalList) {
                    float[] results = new float[1];
                    android.location.Location.distanceBetween(res.lat, res.lng, existing.lat, existing.lng, results);
                    if (results[0] < 500) {
                        dup = true;
                        break;
                    }
                }
                if (!dup && finalList.size() < 6) {
                    finalList.add(res);
                }
            }

            runOnUiThread(() -> {
                if (searchPredictionAdapter == null || rvSearchPredictions == null) {
                    return;
                }
                if (!finalList.isEmpty()) {
                    rvSearchPredictions.setVisibility(View.VISIBLE);
                    searchPredictionAdapter.setPredictions(finalList);
                } else {
                    rvSearchPredictions.setVisibility(View.GONE);
                }
            });
        });
    }

    private void detectRealLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQ);
            return;
        }

        btnDetectLocation.setEnabled(false);
        btnDetectLocation.setText("Fetching Precise Location...");
        tvDetectedLocation.setText("Detecting...");
        tvDetectedLocation.setTextColor(0xFF64748B);

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(location -> {
            if (location == null) {
                fusedLocationClient.getLastLocation().addOnSuccessListener(lastLocation -> {
                    if (lastLocation == null) {
                        showLocationError("Unable to get current location");
                        return;
                    }
                    applyResolvedCurrentLocation(lastLocation.getLatitude(), lastLocation.getLongitude());
                }).addOnFailureListener(e -> showLocationError("Location error: " + e.getMessage()));
                return;
            }
            applyResolvedCurrentLocation(location.getLatitude(), location.getLongitude());
        }).addOnFailureListener(e -> showLocationError("Location error: " + e.getMessage()));
    }

    private void applyResolvedCurrentLocation(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String detected = addresses.get(0).getAddressLine(0);
                selectedLat = lat;
                selectedLng = lng;
                selectedAddress = detected;

                tvDetectedLocation.setText(detected);
                tvDetectedLocation.setTextColor(0xFF0F172A);

                if (etLocationSearch != null) {
                    etLocationSearch.setText(detected);
                    etLocationSearch.setSelection(etLocationSearch.getText().length());
                }
                if (rvSearchPredictions != null) {
                    rvSearchPredictions.setVisibility(View.GONE);
                }

                btnDetectLocation.setText("Location Confirmed");
                btnDetectLocation.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF16A34A));
                btnDetectLocation.setTextColor(android.graphics.Color.WHITE);
                Toast.makeText(this, "Location set to: " + detected, Toast.LENGTH_SHORT).show();
            } else {
                showLocationError("Address not found");
            }
        } catch (IOException e) {
            showLocationError("Error getting address");
        }
    }

    private void showLocationError(String errorMsg) {
        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
        btnDetectLocation.setEnabled(true);
        btnDetectLocation.setText("Use current location");
        tvDetectedLocation.setText("Unknown Location");
    }

    private void hideKeyboard() {
        View currentFocus = getCurrentFocus();
        if (currentFocus == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    private void saveLocationToFirestore(String address) {
        com.google.firebase.auth.FirebaseUser user =
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("location", address);
        if (!Double.isNaN(selectedLat)) data.put("lat", selectedLat);
        if (!Double.isNaN(selectedLng)) data.put("lng", selectedLng);

        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("Users").document(user.getUid())
                .set(data, com.google.firebase.firestore.SetOptions.merge());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        geocodeExecutor.shutdownNow();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                detectRealLocation();
            } else {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
                btnDetectLocation.setEnabled(true);
                btnDetectLocation.setText("Use current location");
            }
        }
    }
}
