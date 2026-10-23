package com.example.geoguard.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.geoguard.R;
import com.example.geoguard.adapters.ReportAdapter;
import com.example.geoguard.models.IncidentReport;
import com.example.geoguard.network.ApiService;
import com.example.geoguard.network.RetrofitClient;
import com.example.geoguard.storage.LocalReportCache;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportListActivity extends AppCompatActivity {
    private final List<IncidentReport> reports = new ArrayList<>();
    private ReportAdapter adapter;
    private RecyclerView recyclerReports;
    private ProgressBar progress;
    private TextView tvEmpty, tvTotal, tvSubmitted, tvResolved;
    private LocalReportCache reportCache;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_report_list);

        // Bind Views
        recyclerReports = findViewById(R.id.recyclerReports);
        progress = findViewById(R.id.progressReports);
        tvEmpty = findViewById(R.id.tvEmptyReports);
        tvTotal = findViewById(R.id.tvInspectorTotal);
        tvSubmitted = findViewById(R.id.tvInspectorSubmitted);
        tvResolved = findViewById(R.id.tvInspectorResolved);

        reportCache = new LocalReportCache(this);

        // Configure RecyclerView
        recyclerReports.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReportAdapter(reports, report -> {
            Intent intent = new Intent(ReportListActivity.this, ReportDetailActivity.class);
            intent.putExtra("report_id", report.report_id);
            startActivity(intent);
        });
        recyclerReports.setAdapter(adapter);

        // Button Listeners
        findViewById(R.id.btnCreateReport).setOnClickListener(v ->
                startActivity(new Intent(this, ReportFormActivity.class)));

        // Module 4: Cache Directory Cleaning Trigger
        View btnClearCache = findViewById(R.id.btnClearCache);
        if (btnClearCache != null) {
            btnClearCache.setOnClickListener(v -> {
                reportCache.clearTemporaryCache(this);
                Toast.makeText(this, "Temporary cache directories cleaned.", Toast.LENGTH_SHORT).show();
            });
        }

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            getSharedPreferences("GeoGuardPrefs", MODE_PRIVATE).edit()
                    .remove("jwt_token").remove("user_email").remove("user_role").apply();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        loadReports();

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReports();
    }

    private void loadReports() {
        if (progress != null) progress.setVisibility(View.VISIBLE);
        if (tvEmpty != null) tvEmpty.setText("Loading reports...");

        RetrofitClient.getApiService(this).getReports().enqueue(new Callback<ApiService.ReportsResponse>() {
            @Override
            public void onResponse(Call<ApiService.ReportsResponse> call, Response<ApiService.ReportsResponse> response) {
                if (progress != null) progress.setVisibility(View.GONE);

                if (!response.isSuccessful() || response.body() == null) {
                    loadFromLocalCache("Could not load online reports (HTTP " + response.code() + ").");
                    return;
                }

                reports.clear();
                if (response.body().reports != null) {
                    reports.addAll(response.body().reports);

                    // Module 4: Update local report cache
                    if (reportCache != null) {
                        reportCache.save(reports, (cachedList, saved) -> {});
                    }
                }

                adapter.notifyDataSetChanged();
                updateCardCounters(reports);

                if (tvEmpty != null) {
                    tvEmpty.setText(reports.isEmpty() ? "No reports yet. Tap + New Report to add one." : "");
                }
            }

            @Override
            public void onFailure(Call<ApiService.ReportsResponse> call, Throwable t) {
                if (progress != null) progress.setVisibility(View.GONE);
                loadFromLocalCache("Network offline. Displaying cached reports.");
            }
        });
    }

    private void updateCardCounters(List<IncidentReport> reportList) {
        int total = reportList.size();
        int submitted = 0;
        int resolved = 0;

        for (IncidentReport r : reportList) {
            if ("resolved".equalsIgnoreCase(r.status)) {
                resolved++;
            } else {
                submitted++;
            }
        }

        if (tvTotal != null) tvTotal.setText(String.valueOf(total));
        if (tvSubmitted != null) tvSubmitted.setText(String.valueOf(submitted));
        if (tvResolved != null) tvResolved.setText(String.valueOf(resolved));
    }

    private void loadFromLocalCache(String fallbackMessage) {
        if (reportCache == null) return;
        reportCache.load((cachedReports, saved) -> {
            reports.clear();
            if (cachedReports != null && !cachedReports.isEmpty()) {
                reports.addAll(cachedReports);
                adapter.notifyDataSetChanged();
                updateCardCounters(reports);
                if (tvEmpty != null) tvEmpty.setText(fallbackMessage);
            } else {
                if (tvEmpty != null) tvEmpty.setText(fallbackMessage + " No offline cache available.");
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (reportCache != null) reportCache.shutdown();
        super.onDestroy();
    }
}