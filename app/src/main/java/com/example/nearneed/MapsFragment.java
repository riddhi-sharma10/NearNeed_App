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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String currentRole;
    private boolean isGigsMode = true;

    // UI elements
    private TextView btnGigs, btnCommunity;
    private View infoCard;
    private Marker selectedMarker;

    private static class MarkerData {
        int iconResId;
        int color;
        MarkerData(int icon, int col) { iconResId = icon; color = col; }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle bundle) {
        currentRole = RoleManager.getRole(requireContext());
        
        int layoutId = RoleManager.ROLE_SEEKER.equals(currentRole) ? 
                R.layout.layout_maps_seeker : R.layout.layout_maps_provider;
                
        View view = inflater.inflate(layoutId, container, false);

        initUI(view);

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
                bookNow.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Seeker Booking...", Toast.LENGTH_SHORT).show());
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
            
            View acceptJob = view.findViewById(R.id.btn_accept_job);
            if (acceptJob != null) {
                acceptJob.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Provider Job Accepted!", Toast.LENGTH_SHORT).show());
            }
        }

        if (btnGigs != null) btnGigs.setOnClickListener(v -> toggleMode(true));
        if (btnCommunity != null) btnCommunity.setOnClickListener(v -> toggleMode(false));
        
        View closeBtn = view.findViewById(R.id.ic_close_card);
        if (closeBtn != null) closeBtn.setOnClickListener(v -> infoCard.setVisibility(View.GONE));
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

        if (RoleManager.ROLE_SEEKER.equals(currentRole)) {
            if (isGigsMode) {
                addSampleMarker(new LatLng(19.0820, 72.8850), "Marcus Watts", "Electrician", blue, R.drawable.ic_plug_blue);
                addSampleMarker(new LatLng(19.0700, 72.8700), "Sarah Chen", "Cleaning", blue, R.drawable.ic_cleaning);
            } else {
                addSampleMarker(new LatLng(19.0750, 72.8800), "Park Cleanup", "Community", green, R.drawable.ic_gardening);
            }
        } else {
            addSampleMarker(new LatLng(19.0850, 72.8750), "Plumbing Repair", "Gig Request", yellow, R.drawable.ic_plumber);
            addSampleMarker(new LatLng(19.0650, 72.8650), "TV Mounting", "Furniture Gig", yellow, R.drawable.ic_toolbox_seeker);
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
}
