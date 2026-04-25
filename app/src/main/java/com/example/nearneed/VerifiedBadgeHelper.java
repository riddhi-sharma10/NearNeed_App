package com.example.nearneed;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

public final class VerifiedBadgeHelper {

    private static final int BADGE_DP = 16;
    private static final int PADDING_DP = 4;

    private VerifiedBadgeHelper() {}

    /**
     * Attaches or removes the verified badge as a compound drawable (end side) on a TextView.
     * Safe to call multiple times — always resets to the correct state.
     */
    public static void apply(Context ctx, TextView tv, boolean isVerified) {
        if (tv == null) return;
        if (!isVerified) {
            tv.setCompoundDrawablesRelative(null, null, null, null);
            return;
        }
        Drawable badge = ContextCompat.getDrawable(ctx, R.drawable.ic_verified_small);
        if (badge == null) return;
        float density = ctx.getResources().getDisplayMetrics().density;
        int sizePx    = Math.round(BADGE_DP * density);
        int padPx     = Math.round(PADDING_DP * density);
        badge.setBounds(0, 0, sizePx, sizePx);
        tv.setCompoundDrawablePadding(padPx);
        tv.setCompoundDrawablesRelative(null, null, badge, null);
    }
}
