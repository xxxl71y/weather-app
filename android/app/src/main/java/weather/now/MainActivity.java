package weather.now;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private static final String URL = "https://xxxl71y.github.io/weather-app/";
    private static final int LOCATION_REQUEST = 100;
    private WebView webView;
    private boolean locationGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setGeolocationEnabled(true);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

        requestLocationPermission();
    }

    private void requestLocationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            locationGranted = true;
            webView.loadUrl(URL);
            return;
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationGranted = true;
            webView.loadUrl(URL);
            return;
        }

        // 首次进入：先解释，再弹系统授权框
        new AlertDialog.Builder(this)
            .setTitle("位置权限")
            .setMessage("Weather 需要使用您的位置信息来获取当地的天气数据。")
            .setPositiveButton("允许", (dialog, which) -> {
                requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST
                );
            })
            .setNegativeButton("跳过", (dialog, which) -> {
                locationGranted = false;
                webView.loadUrl(URL);
            })
            .setCancelable(false)
            .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST) {
            locationGranted = grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            webView.loadUrl(URL);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
