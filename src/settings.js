/* ===== 设置管理 ===== */
const APP_VERSION = '__APP_VERSION__';
document.getElementById('appVersion').textContent = 'v' + APP_VERSION;

const DEFAULTS = {
  tempUnit: 'celsius',
  notifyIntervalOn: true,
  notifyInterval: 60,
  notifyMorningTime: '08:00',
  notifyMorningOn: true,
  notifyEveningTime: '23:00',
  notifyEveningOn: true,
  autoRefresh: true,
};
const FREQ_OPTS = [
  { label:'30 分钟', val:30 },
  { label:'1 小时',  val:60 },
  { label:'2 小时',  val:120 },
];
function freqLabel(v) { const o = FREQ_OPTS.find(f => f.val === v); return o ? o.label : '1 小时'; }
let settings = { ...DEFAULTS };
let refreshTimer = null;

function loadSettings() {
  try {
    const saved = JSON.parse(localStorage.getItem('weather_settings'));
    if (saved) Object.assign(settings, saved);
  } catch (_) {}
}
function saveSettings() {
  localStorage.setItem('weather_settings', JSON.stringify(settings));
  if (typeof WeatherApp !== 'undefined') {
    WeatherApp.saveNotificationSettings(JSON.stringify({
      notifyIntervalOn: settings.notifyIntervalOn,
      notifyInterval: settings.notifyInterval,
      notifyMorningOn: settings.notifyMorningOn,
      notifyMorningTime: settings.notifyMorningTime,
      notifyEveningOn: settings.notifyEveningOn,
      notifyEveningTime: settings.notifyEveningTime,
    }));
  }
}
function applySettingsUI() {
  updateFreqUI();
  updateAlertBtn('notifyMorning', settings.notifyMorningOn, settings.notifyMorningTime);
  updateAlertBtn('notifyEvening', settings.notifyEveningOn, settings.notifyEveningTime);
  document.getElementById('notifyMorningOn').checked = settings.notifyMorningOn;
  document.getElementById('notifyEveningOn').checked = settings.notifyEveningOn;
  document.getElementById('autoRefresh').checked = settings.autoRefresh;
  const seg = document.getElementById('tempUnit');
  seg.querySelectorAll('button').forEach(b => {
    b.classList.toggle('active', b.dataset.val === settings.tempUnit);
  });
  if (settings.autoRefresh) startAutoRefresh();
  else stopAutoRefresh();
}
function updateFreqUI() {
  document.getElementById('notifyIntervalOn').checked = settings.notifyIntervalOn;
  const btn = document.getElementById('notifyIntervalBtn');
  btn.textContent = settings.notifyIntervalOn ? freqLabel(settings.notifyInterval) : '关闭';
  btn.classList.toggle('off', !settings.notifyIntervalOn);
}
function updateAlertBtn(prefix, on, time) {
  const btn = document.getElementById(prefix + 'Btn');
  btn.textContent = on ? time : '关闭';
  btn.classList.toggle('off', !on);
}

function toggleSettings() {
  const weather = document.getElementById('weatherView');
  const panel = document.getElementById('settingsPanel');
  const btn = document.getElementById('settingsBtn');
  if (panel.classList.contains('show')) {
    panel.classList.remove('show');
    weather.style.display = '';
    btn.style.display = '';
    saveSettings();
    if (typeof WeatherApp !== 'undefined') WeatherApp.setSettingsOpen(false);
  } else {
    applySettingsUI();
    weather.style.display = 'none';
    panel.classList.add('show');
    btn.style.display = 'none';
    if (typeof WeatherApp !== 'undefined') WeatherApp.setSettingsOpen(true);
  }
}

// Temp unit segmented control
document.getElementById('tempUnit').addEventListener('click', e => {
  if (e.target.tagName !== 'BUTTON') return;
  settings.tempUnit = e.target.dataset.val;
  document.getElementById('tempUnit').querySelectorAll('button').forEach(b => {
    b.classList.toggle('active', b === e.target);
  });
  saveSettings();
  updateTempDisplay();
});

// Alert & frequency on/off toggles
['notifyIntervalOn','notifyMorningOn','notifyEveningOn'].forEach(id => {
  document.getElementById(id).addEventListener('change', function() {
    settings[id] = this.checked;
    if (id === 'notifyIntervalOn') {
      updateFreqUI();
      if (typeof WeatherApp !== 'undefined') {
        this.checked ? WeatherApp.startMonitor() : WeatherApp.stopMonitor();
      }
    } else { const prefix = id.replace('On',''); updateAlertBtn(prefix, this.checked, settings[prefix + 'Time']); }
    saveSettings();
  });
});

// Auto-refresh toggle
document.getElementById('autoRefresh').addEventListener('change', function() {
  settings.autoRefresh = this.checked;
  saveSettings();
  this.checked ? startAutoRefresh() : stopAutoRefresh();
});

let lastCelsius = null;
function setLastCelsius(c) { lastCelsius = c; updateTempDisplay(); }
function updateTempDisplay() {
  const tempEl = document.getElementById('temp');
  if (lastCelsius == null) return;
  const val = settings.tempUnit === 'fahrenheit' ? Math.round(lastCelsius * 9 / 5 + 32) : Math.round(lastCelsius);
  const unit = settings.tempUnit === 'fahrenheit' ? '°F' : '°C';
  tempEl.innerHTML = val + '<span class="temp-unit">' + unit + '</span>';
}

function startAutoRefresh() {
  stopAutoRefresh();
  refreshTimer = setInterval(() => fetchWeatherFresh(false), 5 * 60 * 1000);
}
function stopAutoRefresh() { if (refreshTimer) { clearInterval(refreshTimer); refreshTimer = null; } }
