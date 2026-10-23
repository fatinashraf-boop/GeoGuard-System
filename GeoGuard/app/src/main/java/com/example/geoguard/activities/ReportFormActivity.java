package com.example.geoguard.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.geoguard.R;
import com.example.geoguard.location.LocationHelper;
import com.example.geoguard.network.ApiService;
import com.example.geoguard.network.RetrofitClient;
import com.example.geoguard.utils.ImageProcessor;
import java.io.File;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportFormActivity extends AppCompatActivity {
    private EditText etTitle, etDescription, etType;
    private TextView tvLocation, tvEvidence, tvMessage;
    private ProgressBar progress;
    private Double latitude, longitude;
    private String address = "", evidencePath = "";

    private final ActivityResultLauncher<String[]> locationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean granted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION))
                        || Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                if (granted) fetchLocation();
                else tvMessage.setText("Location permission was not granted.");
            });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String rawPath = result.getData().getStringExtra("evidence_path");
                    if (rawPath != null) {
                        double lat = (latitude != null) ? latitude : 0.0;
                        double lon = (longitude != null) ? longitude : 0.0;

                        // Maintain the captured location text on screen
                        if (latitude != null && longitude != null) {
                            tvLocation.setText(String.format(java.util.Locale.US, "Lat: %.6f\nLon: %.6f\n%s", latitude, longitude, address));
                        }

                        // Apply Module 3 Canvas Watermarking using ImageProcessor
                        new ImageProcessor().watermarkAndSave(this, rawPath, lat, lon, new ImageProcessor.Callback() {
                            @Override
                            public void onComplete(File watermarkedFile) {
                                evidencePath = watermarkedFile.getAbsolutePath();
                                tvEvidence.setText("Watermarked evidence: " + watermarkedFile.getName());
                                tvMessage.setText("Image watermarked and saved.");
                            }

                            @Override
                            public void onError(String message) {
                                tvMessage.setText("Watermarking failed: " + message);
                            }
                        });
                    }
                }
            });

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_report_form);
        etTitle = findViewById(R.id.etReportTitle);
        etDescription = findViewById(R.id.etReportDescription);
        etType = findViewById(R.id.etReportType);
        tvLocation = findViewById(R.id.tvReportLocation);
        tvEvidence = findViewById(R.id.tvReportEvidence);
        tvMessage = findViewById(R.id.tvReportMessage);
        progress = findViewById(R.id.progressSubmit);

        // Restore saved variables if Activity was recreated after camera launch
        if (state != null) {
            if (state.containsKey("latitude")) latitude = state.getDouble("latitude");
            if (state.containsKey("longitude")) longitude = state.getDouble("longitude");
            address = state.getString("address", "");
            evidencePath = state.getString("evidencePath", "");

            if (latitude != null && longitude != null) {
                tvLocation.setText(String.format(java.util.Locale.US, "Lat: %.6f\nLon: %.6f\n%s", latitude, longitude, address));
            }
            if (!evidencePath.isEmpty()) {
                tvEvidence.setText("Evidence: " + new File(evidencePath).getName());
            }
        }

        findViewById(R.id.btnGetLocation).setOnClickListener(v -> requestLocation());
        findViewById(R.id.btnCaptureEvidence).setOnClickListener(v -> {
            if (latitude == null || longitude == null) {
                Toast.makeText(this, "Please get location coordinates before taking photo.", Toast.LENGTH_SHORT).show();
                return;
            }
            cameraLauncher.launch(new Intent(this, CameraActivity.class));
        });
        findViewById(R.id.btnSubmitReport).setOnClickListener(v -> submitReport());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (latitude != null) outState.putDouble("latitude", latitude);
        if (longitude != null) outState.putDouble("longitude", longitude);
        outState.putString("address", address);
        outState.putString("evidencePath", evidencePath);
    }

    private void requestLocation() {
        boolean fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (fine || coarse) fetchLocation();
        else locationPermission.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
    }

    private void fetchLocation() {
        tvMessage.setText("Getting location...");
        new LocationHelper(this).getCurrentLocation(new LocationHelper.Callback() {
            @Override public void onLocation(double lat, double lon, String addressText) {
                latitude = lat;
                longitude = lon;
                address = addressText == null ? "" : addressText;
                tvLocation.setText(String.format(java.util.Locale.US, "Lat: %.6f\nLon: %.6f\n%s", lat, lon, address));
                tvMessage.setText("Location captured.");
            }
            @Override public void onError(String message) { tvMessage.setText(message); }
        });
    }

    private void submitReport() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String type = etType.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || type.isEmpty()) {
            tvMessage.setText("Enter title, description and incident type.");
            return;
        }
        if (evidencePath.isEmpty()) {
            tvMessage.setText("Capture evidence before submitting.");
            return;
        }

        progress.setVisibility(View.VISIBLE);
        findViewById(R.id.btnSubmitReport).setEnabled(false);

        ApiService.CreateReportRequest request = new ApiService.CreateReportRequest(
                title, description, type, latitude, longitude, address, evidencePath);

        RetrofitClient.getApiService(this).createReport(request).enqueue(new Callback<ApiService.ReportResponse>() {
            @Override public void onResponse(Call<ApiService.ReportResponse> call, Response<ApiService.ReportResponse> response) {
                progress.setVisibility(View.GONE);
                findViewById(R.id.btnSubmitReport).setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ReportFormActivity.this, "Report submitted successfully!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    tvMessage.setText("Submission failed (HTTP " + response.code() + ").");
                }
            }

            @Override public void onFailure(Call<ApiService.ReportResponse> call, Throwable t) {
                progress.setVisibility(View.GONE);
                findViewById(R.id.btnSubmitReport).setEnabled(true);
                tvMessage.setText("Network error while submitting. Local watermarked evidence is preserved.");
            }
        });
    }
}