package com.example.geoguard.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.geoguard.R;
import com.example.geoguard.adapters.ReportAdapter;
import com.example.geoguard.models.IncidentReport;
import com.example.geoguard.network.ApiService;
import com.example.geoguard.network.RetrofitClient;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardActivity extends AppCompatActivity {
    private TextView tvTotal, tvUnresolved, tvResolved;
    private RecyclerView recyclerAdmin;
    private ProgressBar progressAdmin;
    private ReportAdapter adapter;
    private final List<IncidentReport> reportList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        tvTotal = findViewById(R.id.tvTotalCount);
        tvUnresolved = findViewById(R.id.tvUnresolvedCount);
        tvResolved = findViewById(R.id.tvResolvedCount);
        recyclerAdmin = findViewById(R.id.recyclerAdminReports);
        progressAdmin = findViewById(R.id.progressAdmin);

        recyclerAdmin.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReportAdapter(reportList, this::showStatusUpdateDialog);
        recyclerAdmin.setAdapter(adapter);

        findViewById(R.id.btnAdminLogout).setOnClickListener(v -> {
            getSharedPreferences("GeoGuardPrefs", MODE_PRIVATE).edit()
                    .remove("jwt_token").remove("user_email").remove("user_role").apply();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        loadDashboardData();
    }

    private void loadDashboardData() {
        progressAdmin.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService(this).getAllReports().enqueue(new Callback<ApiService.ReportsResponse>() {
            @Override
            public void onResponse(Call<ApiService.ReportsResponse> call, Response<ApiService.ReportsResponse> response) {
                progressAdmin.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().reports != null) {
                    reportList.clear();
                    reportList.addAll(response.body().reports);
                    adapter.notifyDataSetChanged();
                    updateCounters(reportList);
                } else {
                    Toast.makeText(AdminDashboardActivity.this, "Failed to fetch reports.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.ReportsResponse> call, Throwable t) {
                progressAdmin.setVisibility(View.GONE);
                Toast.makeText(AdminDashboardActivity.this, "Network error loading dashboard.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCounters(List<IncidentReport> reports) {
        int total = reports.size();
        int resolved = 0;
        int unresolved = 0;

        for (IncidentReport r : reports) {
            if ("resolved".equalsIgnoreCase(r.status)) {
                resolved++;
            } else {
                unresolved++;
            }
        }

        tvTotal.setText(String.valueOf(total));
        tvUnresolved.setText(String.valueOf(unresolved));
        tvResolved.setText(String.valueOf(resolved));
    }

    private void showStatusUpdateDialog(IncidentReport report) {
        String[] options = {"submitted", "in_progress", "resolved", "rejected"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Report #" + report.report_id + " Status");
        builder.setItems(options, (dialog, which) -> {
            String selectedStatus = options[which];
            updateStatusOnServer(report.report_id, selectedStatus);
        });
        builder.show();
    }

    private void updateStatusOnServer(int reportId, String newStatus) {
        progressAdmin.setVisibility(View.VISIBLE);
        ApiService.UpdateStatusRequest req = new ApiService.UpdateStatusRequest(newStatus);
        RetrofitClient.getApiService(this).updateReportStatus(reportId, req).enqueue(new Callback<ApiService.ReportResponse>() {
            @Override
            public void onResponse(Call<ApiService.ReportResponse> call, Response<ApiService.ReportResponse> response) {
                progressAdmin.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(AdminDashboardActivity.this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                    loadDashboardData();
                } else {
                    Toast.makeText(AdminDashboardActivity.this, "Failed to update status.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.ReportResponse> call, Throwable t) {
                progressAdmin.setVisibility(View.GONE);
                Toast.makeText(AdminDashboardActivity.this, "Network error.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}