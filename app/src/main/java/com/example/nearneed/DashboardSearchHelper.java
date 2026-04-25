package com.example.nearneed;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

public final class DashboardSearchHelper {

    private DashboardSearchHelper() {
    }

    public static void bindSeekerSearch(View root, 
                                        RecyclerView.Adapter<?> gigsAdapter,
                                        RecyclerView.Adapter<?> communityAdapter,
                                        Context context) {
        if (root == null) {
            return;
        }

        EditText searchEdit = root.findViewById(R.id.searchEditText);
        if (searchEdit == null) {
            return;
        }

        TextView emptyState = root.findViewById(R.id.tvSearchEmptyState);

        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s == null ? "" : s.toString().trim();
                
                // If adapters support filtering, call it here
                // For now, we'll just handle the visibility of empty state
                if (gigsAdapter instanceof CommunityVolunteeringAdapter) {
                    ((CommunityVolunteeringAdapter) gigsAdapter).filter(query);
                }
                if (communityAdapter instanceof CommunityVolunteeringAdapter) {
                    ((CommunityVolunteeringAdapter) communityAdapter).filter(query);
                }

                if (emptyState != null && !query.isEmpty()) {
                    boolean hasAny = (gigsAdapter != null && gigsAdapter.getItemCount() > 0)
                            || (communityAdapter != null && communityAdapter.getItemCount() > 0);
                    emptyState.setVisibility(hasAny ? View.GONE : View.VISIBLE);
                } else if (emptyState != null) {
                    emptyState.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public static void bindProviderSearch(EditText searchEdit,
                                          NearbyRequestsAdapter nearbyRequestsAdapter,
                                          CommunityVolunteeringAdapter communityVolunteeringAdapter,
                                          TextView emptyState) {
        if (searchEdit == null) {
            return;
        }

        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s == null ? "" : s.toString();
                if (nearbyRequestsAdapter != null) {
                    nearbyRequestsAdapter.filter(query);
                }
                if (communityVolunteeringAdapter != null) {
                    communityVolunteeringAdapter.filter(query);
                }
                if (emptyState != null) {
                    boolean hasAny = (nearbyRequestsAdapter != null && nearbyRequestsAdapter.getItemCount() > 0)
                            || (communityVolunteeringAdapter != null && communityVolunteeringAdapter.getItemCount() > 0);
                    emptyState.setVisibility(hasAny ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public static void bindMapSearchShortcut(EditText searchEdit, Context context) {
        if (searchEdit == null || context == null) {
            return;
        }

        searchEdit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                openMapSearch(context, searchEdit.getText().toString());
                return true;
            }
            return false;
        });
    }

    public static void openMapSearch(Context context, String query) {
        Intent intent = new Intent(context, MapsActivity.class);
        intent.putExtra("FOCUS_SEARCH", true);
        if (query != null && !query.trim().isEmpty()) {
            intent.putExtra("SEARCH_QUERY", query.trim());
        }
        context.startActivity(intent);
    }

    private static boolean contains(String query, String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && candidate.toLowerCase(Locale.ROOT).contains(query)) {
                return true;
            }
        }
        return false;
    }
}