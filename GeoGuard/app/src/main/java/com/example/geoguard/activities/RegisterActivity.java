package com.example.geoguard.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.geoguard.R;
import com.example.geoguard.network.ApiService;
import com.example.geoguard.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private EditText etUsername, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private ProgressBar progress;
    private TextView tvMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etRegUsername);
        etEmail = findViewById(R.id.etRegEmail);
        etPassword = findViewById(R.id.etRegPassword);
        etConfirmPassword = findViewById(R.id.etRegConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progress = findViewById(R.id.progressReg);
        tvMessage = findViewById(R.id.tvRegMessage);

        btnRegister.setOnClickListener(v -> registerUser());
        findViewById(R.id.tvBackToLogin).setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            tvMessage.setText("All fields are required.");
            return;
        }

        if (password.length() < 8) {
            tvMessage.setText("Password must be at least 8 characters.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            tvMessage.setText("Passwords do not match.");
            return;
        }

        setLoading(true);
        ApiService.RegisterRequest request = new ApiService.RegisterRequest(username, email, password);

        RetrofitClient.getApiService(this).register(request).enqueue(new Callback<ApiService.RegisterResponse>() {
            @Override
            public void onResponse(Call<ApiService.RegisterResponse> call, Response<ApiService.RegisterResponse> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Registration successful! Please log in.", Toast.LENGTH_LONG).show();
                    finish(); // Navigate back to Login screen
                } else if (response.code() == 409) {
                    tvMessage.setText("Username or email already exists.");
                } else {
                    tvMessage.setText("Registration failed (HTTP " + response.code() + ").");
                }
            }

            @Override
            public void onFailure(Call<ApiService.RegisterResponse> call, Throwable t) {
                setLoading(false);
                tvMessage.setText("Cannot connect to server.");
            }
        });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!loading);
    }
}