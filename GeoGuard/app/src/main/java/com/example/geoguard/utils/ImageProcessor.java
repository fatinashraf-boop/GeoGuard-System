package com.example.geoguard.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageProcessor {
    public interface Callback { void onComplete(File file); void onError(String message); }
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void watermarkAndSave(Context context, String sourcePath, double latitude, double longitude, Callback callback) {
        executor.execute(() -> {
            try {
                Bitmap source = BitmapFactory.decodeFile(sourcePath);
                if (source == null) throw new IllegalStateException("Could not decode evidence image.");
                Bitmap marked = source.copy(Bitmap.Config.ARGB_8888, true);
                Canvas canvas = new Canvas(marked);
                Paint background = new Paint(Paint.ANTI_ALIAS_FLAG);
                background.setColor(0xB8000000);
                int bannerHeight = Math.max(100, marked.getHeight() / 8);
                canvas.drawRect(0, marked.getHeight() - bannerHeight, marked.getWidth(), marked.getHeight(), background);
                Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
                text.setColor(Color.WHITE);
                text.setTextSize(Math.max(24, marked.getWidth() / 35f));
                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                canvas.drawText(time, 20, marked.getHeight() - bannerHeight / 2f, text);
                canvas.drawText(String.format(Locale.US, "%.6f, %.6f", latitude, longitude), 20, marked.getHeight() - 20, text);
                File dir = new File(context.getExternalFilesDir(null), "evidence");
                if (!dir.exists() && !dir.mkdirs()) throw new IllegalStateException("Cannot create evidence folder.");
                File output = new File(dir, "watermarked_" + System.currentTimeMillis() + ".jpg");
                try (FileOutputStream stream = new FileOutputStream(output)) {
                    if (!marked.compress(Bitmap.CompressFormat.JPEG, 92, stream)) throw new IllegalStateException("Could not save watermarked image.");
                }
                source.recycle(); marked.recycle();
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onComplete(output));
            } catch (Exception e) {
                String message = e.getMessage() == null ? "Image processing failed." : e.getMessage();
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError(message));
            }
        });
    }
    public void shutdown() { executor.shutdown(); }
}
