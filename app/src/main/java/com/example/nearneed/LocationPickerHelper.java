package com.example.nearneed;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import android.Manifest;
import android.widget.TextView;
import org.json.JSONObject;
import java.util.List;
import java.util.Locale;
import java.io.IOException;
import android.view.View;
import android.widget.EditText;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;


public class LocationPickerHelper {

    public interface OnLocationSelectedListener {
        void onSelected(String displayText, double lat, double lng);
    }

    private static final int LOCATION_PERMISSION_REQ = 101;
    private static BottomSheetDialog currentDialog;
    private static OnLocationSelectedListener currentCallback;

    public static void show(Activity activity, OnLocationSelectedListener listener) {
        currentCallback = listener;

        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        currentDialog = dialog;

        View view = activity.getLayoutInflater().inflate(R.layout.dialog_location_picker, null);
        dialog.setContentView(view);

        // Close button
        View closeBtn = view.findViewById(R.id.btnCloseLocationPicker);
        if (closeBtn != null) {
            closeBtn.setOnClickListener(v -> dialog.dismiss());
        }

        // Home location
        View rowHome = view.findViewById(R.id.rowHome);
        if (rowHome != null) {
            rowHome.setOnClickListener(v -> {
                selectLocation("DELIVER TO: HOME - SECTOR 15", 28.4671, 77.0425);
                dialog.dismiss();
            });
        }

        // Work location
        View rowWork = view.findViewById(R.id.rowWork);
        if (rowWork != null) {
            rowWork.setOnClickListener(v -> {
                selectLocation("DELIVER TO: WORK - DLF CYBER CITY", 28.4951, 77.0881);
                dialog.dismiss();
            });
        }

        // Recent 1
        View rowRecent1 = view.findViewById(R.id.rowRecent1);
        if (rowRecent1 != null) {
            rowRecent1.setOnClickListener(v -> {
                selectLocation("DELIVER TO: SECTOR 21, GURUGRAM", 28.5034, 77.0678);
                dialog.dismiss();
            });
        }

        // Recent 2
        View rowRecent2 = view.findViewById(R.id.rowRecent2);
        if (rowRecent2 != null) {
            rowRecent2.setOnClickListener(v -> {
                selectLocation("DELIVER TO: CONNAUGHT PLACE, DELHI", 28.6304, 77.2177);
                dialog.dismiss();
            });
        }

        // Use current location
        View rowCurrentLocation = view.findViewById(R.id.rowCurrentLocation);
        if (rowCurrentLocation != null) {
            rowCurrentLocation.setOnClickListener(v -> {
                ProgressBar progressBar = view.findViewById(R.id.progressCurrentLocation);
                TextView tvSubtitle = view.findViewById(R.id.tvCurrentLocationSubtitle);
                getCurrentLocation(activity, dialog, progressBar, tvSubtitle);
            });
        }


        // Search Bar Logic
        EditText etSearch = view.findViewById(R.id.etLocationSearch);
        RecyclerView rvPredictions = view.findViewById(R.id.rvLocationPredictions);
        
        if (etSearch != null && rvPredictions != null) {
            rvPredictions.setLayoutManager(new LinearLayoutManager(activity));
            SearchPredictionAdapter adapter = new SearchPredictionAdapter((lat, lng, name) -> {
                // When a search result is clicked
                String displayText = "DELIVER TO: " + name;
                selectLocation(displayText, lat, lng);
                dialog.dismiss();
            });
            rvPredictions.setAdapter(adapter);

            Handler searchHandler = new Handler(Looper.getMainLooper());
            Runnable searchRunnable = null;

            etSearch.addTextChangedListener(new TextWatcher() {
                private Runnable currentRunnable;
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (currentRunnable != null) searchHandler.removeCallbacks(currentRunnable);
                    currentRunnable = () -> {
                        GeocodingHelper.performSearch(s.toString(), results -> {
                            if (!results.isEmpty()) {
                                rvPredictions.setVisibility(View.VISIBLE);
                                adapter.setPredictions(results);
                            } else {
                                rvPredictions.setVisibility(View.GONE);
                            }
                        });
                    };
                    searchHandler.postDelayed(currentRunnable, 500);
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        dialog.show();
    }


    private static void selectLocation(String displayText, double lat, double lng) {
        if (currentCallback != null) {
            currentCallback.onSelected(displayText, lat, lng);
        }
    }

    private static void getCurrentLocation(Activity activity, BottomSheetDialog dialog, ProgressBar progressBar, TextView tvSubtitle) {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);

        // Check permission
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQ);
            return;
        }

        // Show progress
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (tvSubtitle != null) {
            tvSubtitle.setText("Fetching...");
        }

        // Get current location directly instead of relying on last known location cache
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(location -> {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }

            if (location != null) {
                // Reverse geocode to get address
                new Thread(() -> {
                    String addressText = null;
                    
                    // Try Geocoder first
                    try {
                        Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            addressText = addresses.get(0).getAddressLine(0);
                        }
                    } catch (Exception ignored) {}

                    // Fallback to Nominatim
                    if (addressText == null) {
                        try {
                            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                            String url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=" + location.getLatitude() + "&lon=" + location.getLongitude() + "&zoom=18";
                            okhttp3.Request req = new okhttp3.Request.Builder()
                                    .url(url)
                                    .header("User-Agent", "NearNeedApp")
                                    .build();
                            okhttp3.Response response = client.newCall(req).execute();
                            if (response.isSuccessful() && response.body() != null) {
                                JSONObject json = new JSONObject(response.body().string());
                                addressText = json.optString("display_name", null);
                            }
                        } catch (Exception ignored) {}
                    }

                    final String finalAddress = addressText;
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        if (finalAddress != null) {
                            String displayText = "DELIVER TO: " + finalAddress;
                            selectLocation(displayText, location.getLatitude(), location.getLongitude());
                            dialog.dismiss();
                        } else {
                            if (tvSubtitle != null) tvSubtitle.setText("Tap to detect");
                            Toast.makeText(activity, "Unable to get address", Toast.LENGTH_SHORT).show();
                        }
                    });
                }).start();
            } else {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (tvSubtitle != null) tvSubtitle.setText("Tap to detect");
                Toast.makeText(activity, "Unable to get current location", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            if (tvSubtitle != null) {
                tvSubtitle.setText("Tap to detect");
            }
            Toast.makeText(activity, "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
