package com.example.geoguard.models;

public class IncidentReport {
    public int report_id;
    public int user_id;
    public String title;
    public String description;
    public String incident_type;
    public String status;
    public Double latitude;
    public Double longitude;
    public String address;
    public String image_path;
    public String created_at;
    public String updated_at;

    @Override
    public String toString() {
        return title + "\n" + incident_type + " • " + status
                + (created_at == null ? "" : "\n" + created_at);
    }
}
