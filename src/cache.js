function displayWeatherData(d) {
  document.getElementById('location').textContent = d.locName || `${d.lat.toFixed(2)}, ${d.lon.toFixed(2)}`;
  document.getElementById('locDot').className = 'loc-dot' + (d.usingGPS ? '' : ' ip');
  const sub = (d.usingGPS ? 'GPS 精确定位' : 'IP 粗略定位');
  document.getElementById('locSub').textContent = sub + (d.locDetail ? ' · ' + d.locDetail : '');

  // Cache staleness indicator
  const ageEl = document.getElementById('cacheAge');
  if (d.timestamp && d.timestamp < Date.now() - 3000) {
    const min = Math.round((Date.now() - d.timestamp) / 60000);
    ageEl.textContent = min < 1 ? '刚刚更新' : min < 60 ? min + ' 分钟前更新' : Math.round(min / 60) + ' 小时前更新';
  } else {
    ageEl.textContent = '';
  }

  const wmo = WMO_CODES[d.weatherCode] || { icon: '🌈', desc: '未知', theme: 'cloudy' };
  document.getElementById('icon').textContent = wmo.icon;
  setLastCelsius(d.temperature);
  document.getElementById('desc').textContent = wmo.desc;

  document.getElementById('details').innerHTML =
    '<div class="detail-item"><span class="detail-icon">💧</span><span class="detail-value">' + d.humidity + '%</span><span class="detail-label">湿度</span></div>' +
    '<div class="detail-item"><span class="detail-icon">🌬️</span><span class="detail-value">' + d.windSpeed + ' km/h</span><span class="detail-label">风速</span></div>' +
    '<div class="detail-item"><span class="detail-icon">🌊</span><span class="detail-value">' + (d.precipitation ?? 0) + ' mm</span><span class="detail-label">降水</span></div>';

  setTheme(wmo.theme);
  document.getElementById('error').style.display = 'none';
  document.getElementById('loader').style.display = 'none';
}

function loadCachedWeather() {
  try {
    const raw = localStorage.getItem('weather_cache');
    if (raw) { const d = JSON.parse(raw); if (d.temperature != null) return d; }
  } catch (_) {}
  try {
    if (typeof WeatherApp !== 'undefined') {
      const raw = WeatherApp.getWeatherCache();
      if (raw) {
        const d = JSON.parse(raw);
        if (d.temperature != null) { localStorage.setItem('weather_cache', raw); return d; }
      }
    }
  } catch (_) {}
  return null;
}

function saveCachedWeather(d) {
  d.timestamp = Date.now();
  const json = JSON.stringify(d);
  try { localStorage.setItem('weather_cache', json); } catch (_) {}
  try { if (typeof WeatherApp !== 'undefined') WeatherApp.saveWeatherCache(json); } catch (_) {}
}
