async function fetchWeatherFresh(showLoader) {
  const errorEl = document.getElementById('error');
  const loader  = document.getElementById('loader');
  errorEl.style.display = 'none';
  if (showLoader) {
    document.getElementById('location').textContent = '获取位置中...';
    document.getElementById('locSub').textContent = '';
    document.getElementById('icon').textContent = '';
    document.getElementById('temp').textContent = '';
    document.getElementById('desc').textContent = '';
    document.getElementById('details').innerHTML = '';
    loader.style.display = 'block';
  }

  let lat, lon, locName = '', locDetail = '', usingGPS = false;

  // 1. Try cached location first (instant, no GPS wait)
  try {
    if (typeof WeatherApp !== 'undefined') {
      const raw = WeatherApp.getLocation();
      if (raw) {
        const loc = JSON.parse(raw);
        if (loc.lat != null && loc.lon != null && (Date.now() - loc.ts < 3600000)) {
          lat = loc.lat; lon = loc.lon; usingGPS = true;
        }
      }
    }
  } catch (_) {}

  // 2. GPS if no cached location or stale
  if (lat == null || lon == null) {
    try {
      const pos = await new Promise((resolve, reject) => {
        if (!navigator.geolocation) return reject(new Error());
        navigator.geolocation.getCurrentPosition(resolve, reject, { timeout: 5000, enableHighAccuracy: true });
      });
      lat = pos.coords.latitude;
      lon = pos.coords.longitude;
      if (typeof WeatherApp !== 'undefined') WeatherApp.saveLocation(lat, lon);
      usingGPS = true;
    } catch (e) {
      try {
        const ipRes = await fetch('https://ipapi.co/json/');
        if (ipRes.ok) {
          const ipData = await ipRes.json();
          lat = ipData.latitude;
          lon = ipData.longitude;
          if (typeof WeatherApp !== 'undefined') WeatherApp.saveLocation(lat, lon);
          locName = ipData.city || ipData.region || '';
        }
      } catch (_) {}
    }
  }

  if (lat == null || lon == null) {
    if (showLoader) { errorEl.style.display = 'block'; errorEl.textContent = '无法获取位置，请检查网络后重试'; }
    loader.style.display = 'none';
    return;
  }

  try {
    const geoUrl = 'https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=' + lat + '&longitude=' + lon + '&localityLanguage=zh-Hans';
    const geoRes = await fetch(geoUrl);
    if (geoRes.ok) {
      const geo = await geoRes.json();
      const parts = [geo.countryName, geo.principalSubdivision, geo.city, geo.locality].filter(Boolean);
      const unique = [...new Set(parts)];
      locName = unique[unique.length - 1];
      locDetail = unique.join(' ');
    }
  } catch (_) {}

  try {
    const weatherUrl = 'https://api.open-meteo.com/v1/forecast?latitude=' + lat + '&longitude=' + lon + '&current=temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code,precipitation&timezone=auto';
    const weatherRes = await fetch(weatherUrl);
    if (!weatherRes.ok) throw new Error('HTTP ' + weatherRes.status);
    const data = await weatherRes.json();
    const c = data.current;

    const d = {
      lat, lon,
      locName: locName || lat.toFixed(2) + ', ' + lon.toFixed(2),
      locDetail, usingGPS,
      temperature: c.temperature_2m,
      humidity: c.relative_humidity_2m,
      windSpeed: c.wind_speed_10m,
      weatherCode: c.weather_code,
      precipitation: c.precipitation ?? 0,
    };
    displayWeatherData(d);
    saveCachedWeather(d);
  } catch (err) {
    if (showLoader) {
      errorEl.style.display = 'block';
      errorEl.textContent = '天气获取失败: ' + err.message;
    } else {
      // Silent refresh failed — show subtle hint if cached data is visible
      const ageEl = document.getElementById('cacheAge');
      if (ageEl.textContent) ageEl.textContent += ' · 刷新失败';
    }
  }
  loader.style.display = 'none';
}

function loadWeather() {
  const cached = loadCachedWeather();
  if (cached) displayWeatherData(cached);
  fetchWeatherFresh(!cached);
}
/* ===== Bootstrap ===== */
loadSettings();
saveSettings();

/* ===== 主流程 ===== */
loadWeather();
if (settings.autoRefresh) startAutoRefresh();
if (typeof WeatherApp !== 'undefined') {
  settings.notifyIntervalOn ? WeatherApp.startMonitor() : WeatherApp.stopMonitor();
}

// PWA: 注册 Service Worker
if ('serviceWorker' in navigator) {
  navigator.serviceWorker.register('sw.js');
}
