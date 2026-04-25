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
import android.content.Context;
import android.view.View;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationPickerHelper {

    public interface OnLocationSelectedListener {
        void onSelected(String displayText);
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
                selectLocation("DELIVER TO: HOME - SECTOR 15");
                dialog.dismiss();
            });
        }

        // Work location
        View rowWork = view.findViewById(R.id.rowWork);
        if (rowWork != null) {
            rowWork.setOnClickListener(v -> {
                selectLocation("DELIVER TO: WORK - DLF CYBER CITY");
                dialog.dismiss();
            });
        }

        // Recent 1
        View rowRecent1 = view.findViewById(R.id.rowRecent1);
        if (rowRecent1 != null) {
            rowRecent1.setOnClickListener(v -> {
                selectLocation("DELIVER TO: SECTOR 21, GURUGRAM");
                dialog.dismiss();
            });
        }

        // Recent 2
        View rowRecent2 = view.findViewById(R.id.rowRecent2);
        if (rowRecent2 != null) {
            rowRecent2.setOnClickListener(v -> {
                selectLocation("DELIVER TO: CONNAUGHT PLACE, DELHI");
                dialog.dismiss();
            });
        }

        // Use current location
        View rowCurrentLocation = view.findViewById(R.id.rowCurrentLocation);
        if (rowCurrentLocation != null) {
            rowCurrentLocation.setOnClickListener(v -> {
                ProgressBar progressBar = view.findViewById(R.id.progressCurrentLocation);
                getCurrentLocation(activity, dialog, progressBar);
            });
        }

        dialog.show();
    }

    private static void selectLocation(String displayText) {
        if (currentCallback != null) {
            currentCallback.onSelected(displayText);
        }
    }

    private static void getCurrentLocation(Activity activity, BottomSheetDialog dialog, ProgressBar progressBar) {
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

        // Get current location directly instead of relying on last known location cache
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(location -> {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }

            if (location != null) {
                // Reverse geocode to get address
                Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        String addressText = address.getAddressLine(0);
                        String displayText = "DELIVER TO: " + addressText;
                        selectLocation(displayText);
                        dialog.dismiss();
                    } else {
                        Toast.makeText(activity, "Unable to get address", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(activity, "Error getting address", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(activity, "Unable to get current location", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            Toast.makeText(activity, "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
