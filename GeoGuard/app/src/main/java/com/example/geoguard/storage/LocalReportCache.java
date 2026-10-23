package com.example.geoguard.storage;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.example.geoguard.models.IncidentReport;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalReportCache {
    public interface Callback { void onComplete(List<IncidentReport> reports, boolean saved); }
    private final File file;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final Gson gson = new Gson();

    public LocalReportCache(Context context) { file = new File(context.getFilesDir(), "reports_cache.json"); }

    public void save(List<IncidentReport> reports, Callback callback) {
        executor.execute(() -> {
            boolean saved = false;
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                gson.toJson(reports, writer); saved = true;
            } catch (Exception ignored) { }
            boolean result = saved;
            main.post(() -> callback.onComplete(reports, result));
        });
    }

    public void load(Callback callback) {
        executor.execute(() -> {
            List<IncidentReport> reports = new ArrayList<>();
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                Type type = new TypeToken<List<IncidentReport>>(){}.getType();
                List<IncidentReport> loaded = gson.fromJson(reader, type);
                if (loaded != null) reports = loaded;
            } catch (Exception ignored) { }
            List<IncidentReport> result = reports;
            main.post(() -> callback.onComplete(result, true));
        });
    }

    public void clearTemporaryCache(Context context) {
        executor.execute(() -> {
            File[] entries = context.getCacheDir().listFiles();
            if (entries != null) for (File entry : entries) deleteTree(entry);
        });
    }
    private void deleteTree(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) for (File child : children) deleteTree(child);
        }
        file.delete();
    }
    public void shutdown() { executor.shutdown(); }
}
