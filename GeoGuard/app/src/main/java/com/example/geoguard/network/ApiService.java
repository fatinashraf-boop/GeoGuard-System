package com.example.geoguard.network;

import com.example.geoguard.models.IncidentReport;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.PUT;


public interface ApiService {
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("api/auth/me")
    Call<MeResponse> getCurrentUser();

    @GET("api/reports")
    Call<ReportsResponse> getReports();

    @GET("api/reports/{id}")
    Call<ReportResponse> getReport(@Path("id") int reportId);

    @POST("api/reports")
    Call<ReportResponse> createReport(@Body CreateReportRequest request);

    // Add these calls inside the ApiService interface:

    @GET("api/reports")
    Call<ReportsResponse> getAllReports();

    @PUT("api/reports/{id}/status")
    Call<ReportResponse> updateReportStatus(@Path("id") int reportId, @Body UpdateStatusRequest request);

    // Add inside ApiService.java interface:

    @POST("api/auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    class RegisterRequest {
        public String username;
        public String email;
        public String password;

        public RegisterRequest(String username, String email, String password) {
            this.username = username;
            this.email = email;
            this.password = password;
        }
    }

    class RegisterResponse {
        public String message;
        public int user_id;
    }

    class UpdateStatusRequest {
        public String status;
        public UpdateStatusRequest(String status) {
            this.status = status;
        }
    }

    class LoginRequest {
        public String email;
        public String password;
        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    class LoginResponse {
        public String message;
        public String token;
        public User user;
        public String getToken() { return token; }
        public User getUser() { return user; }
    }

    class MeResponse {
        public String message;
        public User user;
        public User getUser() { return user; }
    }

    class User {
        public int user_id;
        public String username;
        public String email;
        public String role;
        public String getEmail() { return email; }
        public String getRole() { return role; }
    }

    class ReportsResponse {
        public List<IncidentReport> reports;
    }

    class ReportResponse {
        public String message;
        public IncidentReport report;
    }

    class CreateReportRequest {
        public String title;
        public String description;
        public String incident_type;
        public Double latitude;
        public Double longitude;
        public String address;
        public String image_path;

        public CreateReportRequest(String title, String description, String type,
                                   Double latitude, Double longitude,
                                   String address, String imagePath) {
            this.title = title;
            this.description = description;
            this.incident_type = type;
            this.latitude = latitude;
            this.longitude = longitude;
            this.address = address;
            this.image_path = imagePath;
        }
    }
}
