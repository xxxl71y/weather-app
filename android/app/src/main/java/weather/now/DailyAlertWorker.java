package weather.now;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class DailyAlertWorker extends Worker {
    private static final String WORK_NAME_MORNING = "daily_morning";
    private static final String WORK_NAME_EVENING = "daily_evening";

    public DailyAlertWorker(@NonNull Context ctx, @NonNull WorkerParameters params) {
        super(ctx, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        String mode = getInputData().getString("mode");
        if (mode == null) mode = "today";

        NotifySettings ns = NotifySettings.load(ctx);

        boolean enabled = mode.equals("tomorrow") ? ns.eveningOn : ns.morningOn;
        if (!enabled) {
            scheduleNext(ctx, ns, mode);
            return Result.success();
        }

        SharedPreferences prefs = ctx.getSharedPreferences("weather", Context.MODE_PRIVATE);
        float lat = prefs.getFloat("lat", Float.NaN);
        float lon = prefs.getFloat("lon", Float.NaN);
        if (Float.isNaN(lat) || Float.isNaN(lon)) {
            scheduleNext(ctx, ns, mode);
            return Result.success();
        }

        try {
            int days = mode.equals("tomorrow") ? 2 : 1;
            String apiUrl = "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + lat + "&longitude=" + lon
                + "&daily=weather_code,precipitation_sum"
                + "&forecast_days=" + days + "&timezone=auto";
            org.json.JSONObject json = HourlyWeatherWorker.fetchJson(apiUrl);
            if (json == null) {
                scheduleNext(ctx, mode);
                return Result.success();
            }

            org.json.JSONArray daily = json.getJSONObject("daily").getJSONArray("weather_code");
            int idx = mode.equals("tomorrow") ? 1 : 0;
            if (idx >= daily.length()) {
                scheduleNext(ctx, mode);
                return Result.success();
            }

            int code = daily.getInt(idx);
            String label = HourlyWeatherWorker.weatherLabel(code);

            if (label != null) {
                String timeLabel = mode.equals("tomorrow") ? "明天" : "今天";
                WeatherNotificationHelper.show(ctx,
                    timeLabel + "天气提醒",
                    timeLabel + "预计有" + label + "，出门注意🌦️",
                    mode.equals("tomorrow") ? 2002 : 2001);
            }
        } catch (Exception e) {
            Log.e("DailyAlert", "check failed", e);
        }

        scheduleNext(ctx, ns, mode);
        return Result.success();
    }

    static void scheduleNext(Context ctx, NotifySettings ns, String mode) {
        String workName = mode.equals("tomorrow") ? WORK_NAME_EVENING : WORK_NAME_MORNING;
        String time = mode.equals("tomorrow") ? ns.eveningTime : ns.morningTime;

        int hour = 23, min = 0;
        if (mode.equals("today")) { hour = 8; min = 0; }
        try {
            String[] parts = time.split(":");
            hour = Integer.parseInt(parts[0]);
            min = Integer.parseInt(parts[1]);
        } catch (Exception ignored) {}

        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, hour);
        target.set(Calendar.MINUTE, min);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);
        if (!target.after(now)) target.add(Calendar.DAY_OF_MONTH, 1);

        long delay = target.getTimeInMillis() - System.currentTimeMillis();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(DailyAlertWorker.class)
            .setInputData(new androidx.work.Data.Builder().putString("mode", mode).build())
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build();

        WorkManager.getInstance(ctx).enqueueUniqueWork(
            workName, ExistingWorkPolicy.REPLACE, request);
    }
}
