package com.gokhanarik.okhttpcachetest;

import android.os.Build;
import android.os.StatFs;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final int MIN_DISK_CACHE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MAX_DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50MB

    private static final Interceptor LOGGING_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            return chain.proceed(request);
        }
    };
    Button button;
    ImageView image;
    TextView logs;

    Picasso picasso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.downloadButton);
        image = (ImageView) findViewById(R.id.image);
        logs = (TextView) findViewById(R.id.result);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                picasso.load("http://graph.facebook.com/1464090949/picture?type=large")
                        .into(image);
            }
        });

        setupPicasso();
    }

    private void setupPicasso() {
        File cache;
        String sdState = android.os.Environment.getExternalStorageState();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                && sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
            File sdDir = android.os.Environment.getExternalStorageDirectory();
            cache = new File(sdDir, "/imageCache");
        } else {
            cache = getCacheDir();
        }

        if (!cache.exists()) {
            boolean dirs = cache.mkdirs();
            if (!dirs) {
                Log.e("ImageLoader", "Unable to create cache directories.");
            }
        }

        long size = MIN_DISK_CACHE_SIZE;
        try {
            StatFs statFs = new StatFs(cache.getAbsolutePath());
            long available = statFs.getBlockCount() * statFs.getBlockSize();
            // Target 2% of the total space.
            size = available / 50;
        } catch (IllegalArgumentException ignored) {
            Log.e(MainActivity.class.getName(), ignored.getMessage());
        }
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .cache(new Cache(cache, Math.max(Math.min(size, MAX_DISK_CACHE_SIZE), MIN_DISK_CACHE_SIZE)));

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(LOGGING_INTERCEPTOR);
        }

        OkHttpClient client = builder.build();
        picasso = new Picasso.Builder(this)
                //.indicatorsEnabled(true)
                .loggingEnabled(true)
                .downloader(new OkHttp3Downloader(client))
                .build();

        picasso.setIndicatorsEnabled(true);
    }


}
