package weather.now;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        scheduleAll(ctx);
    }

    static void scheduleAll(Context ctx) {
        // Start foreground monitoring service
        Intent svc = new Intent(ctx, WeatherMonitorService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ctx.startForegroundService(svc);
        } else {
            ctx.startService(svc);
        }

        NotifySettings ns = NotifySettings.load(ctx);
        DailyAlertWorker.scheduleNext(ctx, ns, "today");
        DailyAlertWorker.scheduleNext(ctx, ns, "tomorrow");
    }
}
