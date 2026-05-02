package weather.now;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
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
        PeriodicWorkRequest hourly = new PeriodicWorkRequest.Builder(HourlyWeatherWorker.class,
                30, TimeUnit.MINUTES)
            .build();
        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
            "hourly_check", ExistingPeriodicWorkPolicy.KEEP, hourly);

        DailyAlertWorker.scheduleNext(ctx, "today");
        DailyAlertWorker.scheduleNext(ctx, "tomorrow");
    }
}
