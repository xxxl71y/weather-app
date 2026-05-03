package weather.now;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class HourlyWeatherWorker extends Worker {
    public HourlyWeatherWorker(@NonNull Context ctx, @NonNull WorkerParameters params) {
        super(ctx, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        SharedPreferences prefs = ctx.getSharedPreferences("weather", Context.MODE_PRIVATE);

        NotifySettings ns = NotifySettings.load(ctx);
        if (!ns.intervalOn) return Result.success();

        float lat = prefs.getFloat("lat", Float.NaN);
        float lon = prefs.getFloat("lon", Float.NaN);
        if (Float.isNaN(lat) || Float.isNaN(lon)) return Result.success();

        try {
            String apiUrl = "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + lat + "&longitude=" + lon
                + "&hourly=precipitation_probability,weather_code,precipitation"
                + "&forecast_hours=3&timezone=auto";
            JSONObject json = fetchJson(apiUrl);
            if (json == null) return Result.retry();

            JSONObject hourly = json.getJSONObject("hourly");
            JSONArray codes = hourly.getJSONArray("weather_code");
            JSONArray precip = hourly.getJSONArray("precipitation");
            JSONArray precipProb = hourly.getJSONArray("precipitation_probability");

            int now = (int)(System.currentTimeMillis() / 1000);
            boolean alert = false;
            StringBuilder desc = new StringBuilder();

            for (int i = 0; i < codes.length() && i < 2; i++) {
                int code = codes.getInt(i);
                double p = precip.getDouble(i);
                int prob = precipProb.getInt(i);
                String label = weatherLabel(code);
                if (label != null && (p > 0 || prob > 30)) {
                    if (desc.length() > 0) desc.append("，");
                    desc.append(label);
                    alert = true;
                }
            }

            if (alert) {
                WeatherNotificationHelper.show(ctx,
                    "未来1小时天气提醒",
                    "预计有" + desc + "，注意带伞🌂",
                    1001);
            }
            return Result.success();
        } catch (Exception e) {
            Log.e("HourlyWeather", "check failed", e);
            return Result.retry();
        }
    }

    static String weatherLabel(int code) {
        if (code >= 51 && code <= 57) return "毛毛雨";
        if (code >= 61 && code <= 67) return "雨";
        if (code >= 71 && code <= 77) return "雪";
        if (code >= 80 && code <= 82) return "阵雨";
        if (code >= 85 && code <= 86) return "阵雪";
        if (code >= 95 && code <= 99) return "雷暴";
        return null;
    }

    static JSONObject fetchJson(String urlStr) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        try {
            if (conn.getResponseCode() != 200) return null;
            BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line);
            r.close();
            return new JSONObject(sb.toString());
        } finally {
            conn.disconnect();
        }
    }
}
