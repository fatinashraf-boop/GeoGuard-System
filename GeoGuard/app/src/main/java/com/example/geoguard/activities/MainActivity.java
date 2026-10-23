package com.example.geoguard.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.geoguard.R;
import com.example.geoguard.network.ApiService;
import com.example.geoguard.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TextView tvMessage;
    private SharedPreferences preferences;
    private Call<ApiService.LoginResponse> loginCall;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        tvMessage = findViewById(R.id.tvMessage);
        preferences = getSharedPreferences("GeoGuardPrefs", Context.MODE_PRIVATE);
        btnLogin.setOnClickListener(v -> loginUser());

        // Verify active token session on startup
        String savedToken = preferences.getString("jwt_token", null);
        if (savedToken != null && !savedToken.trim().isEmpty()) {
            verifySavedToken();
        }

        findViewById(R.id.tvGoToRegister).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RegisterActivity.class))
        );
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        if (email.isEmpty() || password.isEmpty()) {
            tvMessage.setText("Please enter your username/email and password.");
            return;
        }
        setLoading(true);
        ApiService.LoginRequest request = new ApiService.LoginRequest(email, password);
        loginCall = RetrofitClient.getApiService(this).login(request);
        loginCall.enqueue(new Callback<ApiService.LoginResponse>() {
            @Override
            public void onResponse(Call<ApiService.LoginResponse> call, Response<ApiService.LoginResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    setLoading(false);
                    tvMessage.setText("Login failed (HTTP " + response.code() + ").");
                    return;
                }
                ApiService.LoginResponse result = response.body();
                if (result.token == null || result.token.trim().isEmpty()) {
                    setLoading(false);
                    tvMessage.setText("Login response did not contain a token.");
                    return;
                }
                preferences.edit().putString("jwt_token", result.token).apply();
                if (result.user != null) {
                    preferences.edit().putString("user_email", result.user.email)
                            .putString("user_role", result.user.role).apply();
                }
                etPassword.setText("");
                verifySavedToken();
            }

            @Override
            public void onFailure(Call<ApiService.LoginResponse> call, Throwable t) {
                setLoading(false);
                Log.e("GeoGuardLogin", "Login request failed", t);
                tvMessage.setText("Cannot connect to the server. Check backend and BASE_URL.");
            }
        });
    }

    private void verifySavedToken() {
        setLoading(true);
        RetrofitClient.getApiService(this).getCurrentUser().enqueue(new Callback<ApiService.MeResponse>() {
            @Override
            public void onResponse(Call<ApiService.MeResponse> call, Response<ApiService.MeResponse> response) {
                setLoading(false);
                if (!response.isSuccessful() || response.body() == null || response.body().user == null) {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e("GeoGuardAuth", "Could not read error body", e);
                    }

                    Log.e("GeoGuardAuth", "Verification HTTP: " + response.code() + ", response: " + errorBody);
                    tvMessage.setText("Session verification failed (HTTP " + response.code() + "). Please log in again.");
                    return;
                }

                ApiService.User user = response.body().user;
                String role = (user.role == null) ? "" : user.role.trim().toLowerCase();

                // Role-Based Navigation Routing
                if ("admin".equals(role)) {
                    // Navigate to Admin Dashboard
                    startActivity(new Intent(MainActivity.this, AdminDashboardActivity.class));
                    finish();
                } else if ("inspector".equals(role)) {
                    // Navigate to Field Inspector Report List
                    startActivity(new Intent(MainActivity.this, ReportListActivity.class));
                    finish();
                } else {
                    tvMessage.setText("Authenticated, but no home screen is configured for role: " + user.role);
                }
            }


            @Override
            public void onFailure(Call<ApiService.MeResponse> call, Throwable t) {
                setLoading(false);
                Log.e("GeoGuardAuth", "Token verification failed", t);
                tvMessage.setText("Cannot reach server to verify saved session.");
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
    }

    @Override
    protected void onDestroy() {
        if (loginCall != null && !loginCall.isCanceled()) loginCall.cancel();
        super.onDestroy();
    }
}