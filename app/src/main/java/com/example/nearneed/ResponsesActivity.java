package com.example.nearneed;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

public class ResponsesActivity extends AppCompatActivity {

    private RecyclerView rvResponses;
    private LinearLayout emptyStateLayout;
    private ResponsesAdapter adapter;
    private ApplicationViewModel appViewModel;
    private BookingViewModel bookingViewModel;
    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_responses);

        postId = getIntent().getStringExtra("post_id");
        if (postId == null) {
            Toast.makeText(this, "Error: Post ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        setupViewModels();
    }

    private void initViews() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        
        rvResponses = findViewById(R.id.rvResponses);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        
        TextView tvTitle = findViewById(R.id.tvTitle);
        if (tvTitle != null) tvTitle.setText("Applicants");
    }

    private void setupViewModels() {
        appViewModel = new ViewModelProvider(this).get(ApplicationViewModel.class);
        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);

        appViewModel.getApplicationsByPost(postId).observe(this, apps -> {
            adapter.setApplications(apps);
            emptyStateLayout.setVisibility(apps.isEmpty() ? View.VISIBLE : View.GONE);
            rvResponses.setVisibility(apps.isEmpty() ? View.GONE : View.VISIBLE);
        });

        appViewModel.observeApplicationsByPost(this, postId);
    }

    private void setupRecyclerView() {
        rvResponses.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ResponsesAdapter(new ArrayList<>(), new ResponsesAdapter.OnResponseActionListener() {
            @Override
            public void onAccept(Application application, int position) {
                confirmAcceptance(application);
            }

            @Override
            public void onDecline(Application application, int position) {
                appViewModel.updateApplicationStatus(application.applicationId, "declined");
            }

            @Override
            public void onCall(Application application) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + (application.applicantPhone != null ? application.applicantPhone : "")));
                startActivity(intent);
            }

            @Override
            public void onMessage(Application application) {
                Intent intent = new Intent(ResponsesActivity.this, ChatActivity.class);
                intent.putExtra("CHAT_NAME", application.applicantName);
                intent.putExtra("CHAT_USER_ID", application.applicantId);
                startActivity(intent);
            }
        });
        rvResponses.setAdapter(adapter);
    }

    private void confirmAcceptance(Application app) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Accept Applicant")
                .setMessage("Accept " + app.applicantName + " for this task?")
                .setPositiveButton("Accept", (dialog, which) -> {
                    // 1. Update Application Status
                    appViewModel.updateApplicationStatus(app.applicationId, "accepted");
                    
                    // 2. Create Real Booking
                    bookingViewModel.createBookingFromApplication(app);
                    
                    Toast.makeText(this, "Applicant Accepted!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
