package com.example.nearneed;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import androidx.cardview.widget.CardView;

public class PostOptionsActivity extends AppCompatActivity {

    private MaterialCardView cardGigPost, cardCommunityPost;
    private MaterialButton btnContinueGig, btnContinueCommunity;
    private CardView iconGigCard, iconCommunityCard;
    private TextView badgeGig, badgeCommunity, titleGig, titleCommunity;
    
    private String selectedType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Premium transparent status bar
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));
        window.getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        setContentView(R.layout.activity_post_options);

        initViews();
        setupListeners();
    }

    private void initViews() {
        cardGigPost = findViewById(R.id.cardGigPost);
        cardCommunityPost = findViewById(R.id.cardCommunityPost);
        btnContinueGig = findViewById(R.id.btnContinueGig);
        btnContinueCommunity = findViewById(R.id.btnContinueCommunity);
        iconGigCard = findViewById(R.id.iconGigCard);
        iconCommunityCard = findViewById(R.id.iconCommunityCard);
        badgeGig = findViewById(R.id.badgeGig);
        badgeCommunity = findViewById(R.id.badgeCommunity);
        titleGig = findViewById(R.id.titleGig);
        titleCommunity = findViewById(R.id.titleCommunity);
        
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        SeekerNavbarController.bind(this, findViewById(android.R.id.content), SeekerNavbarController.TAB_HOME);
    }

    private void setupListeners() {
        cardGigPost.setOnClickListener(v -> selectType("gig"));
        btnContinueGig.setOnClickListener(v -> {
            selectType("gig");
            navigateToCreatePost("gig");
        });

        cardCommunityPost.setOnClickListener(v -> selectType("community"));
        btnContinueCommunity.setOnClickListener(v -> {
            selectType("community");
            navigateToCreatePost("community");
        });
    }

    private void selectType(String type) {
        selectedType = type;
        
        // Neutralize Gig
        cardGigPost.setStrokeColor(ContextCompat.getColor(this, R.color.palette_neutral));
        cardGigPost.setStrokeWidth((int) (1 * getResources().getDisplayMetrics().density));
        cardGigPost.setCardBackgroundColor(ContextCompat.getColor(this, R.color.palette_neutral));
        iconGigCard.setCardBackgroundColor(Color.parseColor("#64748B"));
        badgeGig.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1F5F9")));
        badgeGig.setTextColor(Color.parseColor("#64748B"));
        titleGig.setTextColor(Color.BLACK);
        btnContinueGig.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1F5F9")));
        btnContinueGig.setTextColor(Color.parseColor("#64748B"));

        // Neutralize Community
        cardCommunityPost.setStrokeColor(ContextCompat.getColor(this, R.color.palette_neutral));
        cardCommunityPost.setStrokeWidth((int) (1 * getResources().getDisplayMetrics().density));
        cardCommunityPost.setCardBackgroundColor(ContextCompat.getColor(this, R.color.palette_neutral));
        iconCommunityCard.setCardBackgroundColor(Color.parseColor("#64748B"));
        badgeCommunity.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1F5F9")));
        badgeCommunity.setTextColor(Color.parseColor("#64748B"));
        titleCommunity.setTextColor(Color.BLACK);
        btnContinueCommunity.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1F5F9")));
        btnContinueCommunity.setTextColor(Color.parseColor("#64748B"));

        // Highlight Selected
        if (type.equals("gig")) {
            cardGigPost.setStrokeColor(ContextCompat.getColor(this, R.color.palette_primary));
            cardGigPost.setStrokeWidth((int) (2 * getResources().getDisplayMetrics().density));
            cardGigPost.setCardBackgroundColor(ContextCompat.getColor(this, R.color.palette_primary_light));
            iconGigCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.palette_primary));
            badgeGig.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.palette_primary)));
            badgeGig.setTextColor(Color.WHITE);
            titleGig.setTextColor(ContextCompat.getColor(this, R.color.palette_primary));
            btnContinueGig.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.palette_primary)));
            btnContinueGig.setTextColor(Color.WHITE);
        } else if (type.equals("community")) {
            cardCommunityPost.setStrokeColor(ContextCompat.getColor(this, R.color.palette_secondary));
            cardCommunityPost.setStrokeWidth((int) (2 * getResources().getDisplayMetrics().density));
            cardCommunityPost.setCardBackgroundColor(ContextCompat.getColor(this, R.color.palette_secondary_light));
            iconCommunityCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.palette_secondary));
            badgeCommunity.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.palette_secondary)));
            badgeCommunity.setTextColor(Color.WHITE);
            titleCommunity.setTextColor(ContextCompat.getColor(this, R.color.palette_secondary));
            btnContinueCommunity.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.palette_secondary)));
            btnContinueCommunity.setTextColor(Color.WHITE);
        }
    }

    private void navigateToCreatePost(String type) {
        Intent intent = new Intent(this, CreatePostActivity.class);
        intent.putExtra("post_type", type);
        startActivity(intent);
    }
}
