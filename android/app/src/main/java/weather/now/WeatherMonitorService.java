package weather.now;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

public class WeatherMonitorService extends Service {
    private static final String CHANNEL_ID = "weather_monitor";
    private static final int NOTIFY_ID = 3001;
    private static final long RETRY_DELAY = 60_000; // 1 minute retry if no location

    private Handler handler;
    private Runnable checkRunnable;
    private boolean running;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(getMainLooper());
        createChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFY_ID, buildNotification("天气监测已启动"));
        startMonitoring();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopMonitoring();
        stopForeground(STOP_FOREGROUND_REMOVE);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID, "天气监测", NotificationManager.IMPORTANCE_LOW);
            ch.setDescription("后台天气监测服务");
            ch.setShowBadge(false);
            getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }
    }

    private Notification buildNotification(String text) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }
        return builder
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("天气酱")
            .setContentText(text)
            .setContentIntent(pi)
            .setOngoing(true)
            .setPriority(Notification.PRIORITY_LOW)
            .build();
    }

    private void startMonitoring() {
        if (running) return;
        running = true;
        checkRunnable = new Runnable() {
            @Override
            public void run() {
                if (!running) return;
                doCheck();
                // Schedule next check
                long interval = getInterval();
                if (interval > 0) {
                    handler.postDelayed(this, interval);
                } else {
                    stopSelf();
                }
            }
        };
        handler.post(checkRunnable);
    }

    private void stopMonitoring() {
        running = false;
        if (checkRunnable != null) {
            handler.removeCallbacks(checkRunnable);
            checkRunnable = null;
        }
    }

    private long getInterval() {
        NotifySettings ns = NotifySettings.load(this);
        if (!ns.intervalOn) return 0;
        return ns.intervalMin * 60_000L;
    }

    private void doCheck() {
        Context ctx = getApplicationContext();
        SharedPreferences prefs = ctx.getSharedPreferences("weather", Context.MODE_PRIVATE);
        float lat = prefs.getFloat("lat", Float.NaN);
        float lon = prefs.getFloat("lon", Float.NaN);
        if (Float.isNaN(lat) || Float.isNaN(lon)) {
            // Retry after delay
            handler.postDelayed(checkRunnable, RETRY_DELAY);
            return;
        }

        try {
            String apiUrl = "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + lat + "&longitude=" + lon
                + "&hourly=precipitation_probability,weather_code,precipitation"
                + "&current=temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code,precipitation"
                + "&forecast_hours=3&timezone=auto";
            JSONObject json = HourlyWeatherWorker.fetchJson(apiUrl);
            if (json == null) return;

            // Cache weather for WebView
            HourlyWeatherWorker.cacheCurrentWeather(json, ctx, lat, lon);

            // Check for rain/snow in next hours
            JSONObject hourly = json.getJSONObject("hourly");
            JSONArray codes = hourly.getJSONArray("weather_code");
            JSONArray precip = hourly.getJSONArray("precipitation");
            JSONArray precipProb = hourly.getJSONArray("precipitation_probability");

            boolean alert = false;
            StringBuilder desc = new StringBuilder();
            for (int i = 0; i < codes.length() && i < 2; i++) {
                int code = codes.getInt(i);
                double p = precip.getDouble(i);
                int prob = precipProb.getInt(i);
                String label = HourlyWeatherWorker.weatherLabel(code);
                if (label != null && (p > 0 || prob > 30)) {
                    if (desc.length() > 0) desc.append("，");
                    desc.append(label);
                    alert = true;
                }
            }

            if (alert) {
                WeatherNotificationHelper.show(ctx,
                    "未来1小时天气提醒",
                    "预计有" + desc + "，注意带伞",
                    1001);
            }

            // Update notification text
            updateNotification("天气监测中 · " + getIntervalLabel());
        } catch (Exception e) {
            Log.e("WeatherMonitor", "check failed", e);
        }
    }

    private String getIntervalLabel() {
        int min = (int)(getInterval() / 60_000);
        if (min >= 120) return "每2小时";
        if (min >= 60) return "每小时";
        return "每30分钟";
    }

    private void updateNotification(String text) {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(NOTIFY_ID, buildNotification(text));
    }
}
