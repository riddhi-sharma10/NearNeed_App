package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class WelcomeActivity extends AppCompatActivity {

    private androidx.viewpager2.widget.ViewPager2 vpSlideshow;
    private final android.os.Handler slideshowHandler = new android.os.Handler();
    private int currentSlide = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_activity);

        setupSlideshow();

        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        MaterialButton btnSignUp = findViewById(R.id.btnSignUp);

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, OtpEnterActivity.class);
            intent.putExtra("IS_SIGNUP", false);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        btnSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, CreateAccountActivity.class);
            intent.putExtra("IS_SIGNUP", true);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void setupSlideshow() {
        vpSlideshow = findViewById(R.id.vpSlideshow);
        int[] images = {
            R.drawable.welcome_bg_1,
            R.drawable.welcome_bg_2,
            R.drawable.welcome_bg_3
        };

        SlideshowAdapter adapter = new SlideshowAdapter(images);
        vpSlideshow.setAdapter(adapter);

        // Auto transition
        slideshowHandler.postDelayed(slideshowRunnable, 3000);
    }

    private final Runnable slideshowRunnable = new Runnable() {
        @Override
        public void run() {
            if (vpSlideshow != null) {
                currentSlide = (currentSlide + 1) % 3;
                vpSlideshow.setCurrentItem(currentSlide, true);
                slideshowHandler.postDelayed(this, 3000);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        slideshowHandler.removeCallbacks(slideshowRunnable);
    }

    private class SlideshowAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<SlideshowAdapter.ViewHolder> {
        private final int[] images;

        public SlideshowAdapter(int[] images) {
            this.images = images;
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext()).inflate(R.layout.item_welcome_slideshow, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.imageView.setImageResource(images[position]);
        }

        @Override
        public int getItemCount() {
            return images.length;
        }

        class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            android.widget.ImageView imageView;
            public ViewHolder(android.view.View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.ivSlideshow);
            }
        }
    }
}
