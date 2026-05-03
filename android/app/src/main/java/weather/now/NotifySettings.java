package weather.now;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONObject;

/** Parsed notification settings — read once per worker cycle to avoid repeated JSON parsing. */
class NotifySettings {
    final boolean intervalOn, morningOn, eveningOn;
    final int intervalMin;
    final String morningTime, eveningTime;

    private NotifySettings(boolean intervalOn, int intervalMin,
                           boolean morningOn, String morningTime,
                           boolean eveningOn, String eveningTime) {
        this.intervalOn = intervalOn;
        this.intervalMin = intervalMin;
        this.morningOn = morningOn;
        this.morningTime = morningTime;
        this.eveningOn = eveningOn;
        this.eveningTime = eveningTime;
    }

    static NotifySettings load(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences("weather", Context.MODE_PRIVATE);
        boolean intervalOn = true;
        int intervalMin = 60;
        boolean morningOn = true;
        String morningTime = "08:00";
        boolean eveningOn = true;
        String eveningTime = "23:00";
        try {
            String json = prefs.getString("notifySettings", null);
            if (json != null) {
                JSONObject s = new JSONObject(json);
                intervalOn = s.optBoolean("notifyIntervalOn", true);
                intervalMin = s.optInt("notifyInterval", 60);
                morningOn = s.optBoolean("notifyMorningOn", true);
                morningTime = s.optString("notifyMorningTime", "08:00");
                eveningOn = s.optBoolean("notifyEveningOn", true);
                eveningTime = s.optString("notifyEveningTime", "23:00");
            }
        } catch (Exception ignored) {}
        return new NotifySettings(intervalOn, intervalMin, morningOn, morningTime, eveningOn, eveningTime);
    }
}
