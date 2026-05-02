package weather.now;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class WeatherNotificationHelper {
    static final String CHANNEL_ID = "weather_alert";
    private static final String CHANNEL_NAME = "天气预警";

    public static void createChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("雨雪天气预警通知");
            ch.setShowBadge(true);
            ctx.getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }
    }

    public static void show(Context ctx, String title, String text, int id) {
        Intent intent = new Intent(ctx, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(ctx, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.notify(id, new android.app.Notification.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setPriority(android.app.Notification.PRIORITY_HIGH)
                .build());
        } else {
            nm.notify(id, new android.app.Notification.Builder(ctx)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setPriority(android.app.Notification.PRIORITY_HIGH)
                .build());
        }
    }
}
