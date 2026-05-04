package weather.now;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private static final String URL = "file:///android_asset/index.html";
    private static final int PERM_REQUEST = 100;
    private WebView webView;
    private boolean permissionsReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WeatherNotificationHelper.createChannel(this);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setGeolocationEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        webView.addJavascriptInterface(new AppBridge(), "WeatherApp");

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

        requestAllPermissions();

        // Start background weather monitoring
        BootReceiver.scheduleAll(this);

        // Request battery optimization exemption (first launch only)
        SharedPreferences prefs = getSharedPreferences("weather", MODE_PRIVATE);
        if (!prefs.getBoolean("permRequested", false)) {
            prefs.edit().putBoolean("permRequested", true).apply();
            requestBatteryExemption();
            requestAutoStart();
        }
    }

    class AppBridge {
        @JavascriptInterface
        public void saveLocation(double lat, double lon) {
            getSharedPreferences("weather", MODE_PRIVATE)
                .edit()
                .putFloat("lat", (float) lat)
                .putFloat("lon", (float) lon)
                .apply();
        }

        @JavascriptInterface
        public void saveNotificationSettings(String json) {
            getSharedPreferences("weather", MODE_PRIVATE)
                .edit()
                .putString("notifySettings", json)
                .apply();
        }

        @JavascriptInterface
        public String getWeatherCache() {
            return getSharedPreferences("weather", MODE_PRIVATE)
                .getString("weatherCache", "");
        }

        @JavascriptInterface
        public void saveWeatherCache(String json) {
            getSharedPreferences("weather", MODE_PRIVATE)
                .edit()
                .putString("weatherCache", json)
                .apply();
        }

        @JavascriptInterface
        public void startMonitor() {
            Intent svc = new Intent(MainActivity.this, WeatherMonitorService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(svc);
            } else {
                startService(svc);
            }
        }

        @JavascriptInterface
        public void stopMonitor() {
            stopService(new Intent(MainActivity.this, WeatherMonitorService.class));
        }
    }

    private void requestBatteryExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
                try {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                } catch (Exception ignored) {}
            }
        }
    }

    private void requestAutoStart() {
        String[][] roms = {
            {"com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"},
            {"com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"},
            {"com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"},
            {"com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"},
            {"com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity"},
        };
        for (String[] rom : roms) {
            try {
                Intent intent = new Intent();
                intent.setClassName(rom[0], rom[1]);
                startActivity(intent);
                return;
            } catch (Exception ignored) {}
        }
    }

    private void requestAllPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            permissionsReady = true;
            webView.loadUrl(URL);
            return;
        }

        java.util.ArrayList<String> needed = new java.util.ArrayList<>();

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                needed.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (needed.isEmpty()) {
            permissionsReady = true;
            webView.loadUrl(URL);
            return;
        }

        requestPermissions(needed.toArray(new String[0]), PERM_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != PERM_REQUEST) return;
        permissionsReady = true;
        webView.loadUrl(URL);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
