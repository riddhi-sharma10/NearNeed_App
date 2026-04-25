package com.example.nearneed;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;

public final class DashboardSearchHelper {

    private DashboardSearchHelper() {
    }

    public static void bindSeekerSearch(View root, boolean hasPosts, Context context) {
        if (root == null) {
            return;
        }

        EditText searchEdit = root.findViewById(R.id.searchEditText);
        if (searchEdit == null) {
            return;
        }

        if (!hasPosts) {
            bindMapSearchShortcut(searchEdit, context);
            return;
        }

        View gigsScrollContainer = root.findViewById(R.id.gigsScrollContainer);
        View communityCardsContainer = root.findViewById(R.id.communityCardsContainer);
        View gigCard1 = root.findViewById(R.id.gig_card_1);
        View gigCard2 = root.findViewById(R.id.gig_card_2);
        View communityCard1 = root.findViewById(R.id.community_card_1);
        View communityCard2 = root.findViewById(R.id.community_card_2);
        TextView emptyState = root.findViewById(R.id.tvSearchEmptyState);

        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s == null ? "" : s.toString().trim().toLowerCase(Locale.ROOT);
                boolean showGig1 = query.isEmpty() || contains(query, "Plumbing Repair", "Fixing leaky faucets and clogged drains for the community.");
                boolean showGig2 = query.isEmpty() || contains(query, "Electrical Fix", "Wiring inspection and circuit breaker repairs for Sector 15.");
                boolean showCommunity1 = query.isEmpty() || contains(query, "Grocery Assistance", "Sarah J. needs help picking out fresh groceries for her weekly meals.");
                boolean showCommunity2 = query.isEmpty() || contains(query, "Tech Setup Help", "David M. needs assistance setting up his new computer and installing software.");

                if (gigCard1 != null) {
                    gigCard1.setVisibility(showGig1 ? View.VISIBLE : View.GONE);
                }
                if (gigCard2 != null) {
                    gigCard2.setVisibility(showGig2 ? View.VISIBLE : View.GONE);
                }
                if (communityCard1 != null) {
                    communityCard1.setVisibility(showCommunity1 ? View.VISIBLE : View.GONE);
                }
                if (communityCard2 != null) {
                    communityCard2.setVisibility(showCommunity2 ? View.VISIBLE : View.GONE);
                }

                if (gigsScrollContainer != null) {
                    gigsScrollContainer.setVisibility(showGig1 || showGig2 ? View.VISIBLE : View.GONE);
                }
                if (communityCardsContainer != null) {
                    communityCardsContainer.setVisibility(showCommunity1 || showCommunity2 ? View.VISIBLE : View.GONE);
                }

                if (emptyState != null) {
                    emptyState.setVisibility(query.isEmpty() || showGig1 || showGig2 || showCommunity1 || showCommunity2
                            ? View.GONE
                            : View.VISIBLE);
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