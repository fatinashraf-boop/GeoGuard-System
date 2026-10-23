package com.example.geoguard.network;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final SharedPreferences preferences;
    public AuthInterceptor(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences("GeoGuardPrefs", Context.MODE_PRIVATE);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String token = preferences.getString("jwt_token", null);
        Request.Builder builder = original.newBuilder();
        if (token != null && !token.trim().isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }
        return chain.proceed(builder.build());
    }
}
