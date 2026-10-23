package com.example.geoguard.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.geoguard.R;
import com.example.geoguard.models.IncidentReport;
import com.example.geoguard.network.ApiService;
import com.example.geoguard.network.RetrofitClient;
import com.squareup.picasso.Picasso;
import java.io.File;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportDetailActivity extends AppCompatActivity {
    // Matches the base IP configuration used in RetrofitClient
    private static final String SERVER_BASE_URL = "http://192.168.68.51:3000";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_report_detail);

        TextView detail = findViewById(R.id.tvReportDetail);
        ImageView imgEvidence = findViewById(R.id.imgReportEvidence);
        ProgressBar progress = findViewById(R.id.progressDetail);

        int reportId = getIntent().getIntExtra("report_id", -1);
        if (reportId < 1) {
            detail.setText("Invalid report ID.");
            return;
        }

        progress.setVisibility(View.VISIBLE);

        RetrofitClient.getApiService(this).getReport(reportId).enqueue(new Callback<ApiService.ReportResponse>() {
            @Override
            public void onResponse(Call<ApiService.ReportResponse> call, Response<ApiService.ReportResponse> response) {
                progress.setVisibility(View.GONE);

                if (!response.isSuccessful() || response.body() == null || response.body().report == null) {
                    detail.setText("Could not load report (HTTP " + response.code() + ").");
                    return;
                }

                IncidentReport r = response.body().report;

                detail.setText("Report #" + r.report_id + "\n\n" + r.title + "\n\nType: " + r.incident_type
                        + "\nStatus: " + r.status + "\n\n" + r.description
                        + "\n\nAddress: " + (r.address == null ? "Not available" : r.address)
                        + "\nCoordinates: " + r.latitude + ", " + r.longitude
                        + "\nCreated: " + r.created_at);

                // Handle loading the image from server host URL or local filesystem fallback
                if (r.image_path != null && !r.image_path.trim().isEmpty()) {
                    if (r.image_path.startsWith("/uploads/")) {
                        String fullImageUrl = SERVER_BASE_URL + r.image_path;
                        Picasso.get()
                                .load(fullImageUrl)
                                .placeholder(R.drawable.ic_launcher_background)
                                .error(R.drawable.ic_launcher_background)
                                .into(imgEvidence);
                    } else {
                        File localFile = new File(r.image_path);
                        if (localFile.exists()) {
                            Picasso.get().load(localFile).into(imgEvidence);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiService.ReportResponse> call, Throwable t) {
                progress.setVisibility(View.GONE);
                detail.setText("Network error loading report.");
            }
        });
    }
}
