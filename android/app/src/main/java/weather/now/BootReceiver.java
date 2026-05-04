package weather.now;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        scheduleAll(ctx);
    }

    static void scheduleAll(Context ctx) {
        scheduleHourlyWorker(ctx);
        NotifySettings ns = NotifySettings.load(ctx);
        DailyAlertWorker.scheduleNext(ctx, ns, "today");
        DailyAlertWorker.scheduleNext(ctx, ns, "tomorrow");
    }

    static void scheduleHourlyWorker(Context ctx) {
        NotifySettings ns = NotifySettings.load(ctx);
        int intervalMin = ns.intervalMin > 0 ? ns.intervalMin : 60;
        PeriodicWorkRequest hourly = new PeriodicWorkRequest.Builder(HourlyWeatherWorker.class,
                intervalMin, TimeUnit.MINUTES)
            .build();
        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
            "hourly_check", ExistingPeriodicWorkPolicy.REPLACE, hourly);
    }

    static void cancelHourlyWorker(Context ctx) {
        WorkManager.getInstance(ctx).cancelUniqueWork("hourly_check");
    }
}
