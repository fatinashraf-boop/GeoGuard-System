package com.example.geoguard.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocationHelper {
    public interface Callback {
        void onLocation(double latitude, double longitude, String address);
        void onError(String message);
    }
    private final Context context;
    private final FusedLocationProviderClient client;
    public LocationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.client = LocationServices.getFusedLocationProviderClient(context);
    }
    @SuppressLint("MissingPermission")
    public void getCurrentLocation(Callback callback) {
        boolean fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!fine && !coarse) { callback.onError("Location permission not granted."); return; }
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location == null) { callback.onError("Current location unavailable. Check device location settings."); return; }
                    double lat = location.getLatitude(), lon = location.getLongitude();
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> {
                        String readable = "";
                        try {
                            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                            if (addresses != null && !addresses.isEmpty()) readable = addresses.get(0).getAddressLine(0);
                        } catch (IOException | IllegalArgumentException ignored) { }
                        String finalReadable = readable;
                        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onLocation(lat, lon, finalReadable));
                        executor.shutdown();
                    });
                })
                .addOnFailureListener(e -> callback.onError("Could not obtain location."));
    }
}
