// Custom time picker (shared overlay)
let tpMode = null, tpTarget = null;
const H = 260, IH = 44, CENTER = (H - IH) / 2;

function populateCol(id, items, cls) {
  const col = document.getElementById(id);
  col.innerHTML = '';
  items.forEach(v => { const d = document.createElement('div'); d.className = 'tp-col-item' + (cls||''); d.textContent = v; col.appendChild(d); });
}

(function initTimePicker() {
  populateCol('tpHours', Array.from({length: 24}, (_, i) => String(i).padStart(2, '0')));
  populateCol('tpMins',  Array.from({length: 60}, (_, i) => String(i).padStart(2, '0')));
  document.getElementById('tpHours').addEventListener('click', e => {
    const el = e.target.closest('.tp-col-item');
    if (el) selectTPVal(el);
  });
  document.getElementById('tpMins').addEventListener('click', e => {
    const el = e.target.closest('.tp-col-item');
    if (el) selectTPVal(el);
  });
})();

function openFreqPicker() {
  tpMode = 'freq';
  const items = FREQ_OPTS.map(f => f.label);
  populateCol('tpHours', items, ' freq-item');
  document.getElementById('tpHours').classList.add('freq-single');
  document.getElementById('tpMins').style.display = 'none';
  document.querySelector('.tp-colon').style.display = 'none';
  document.querySelector('.tp-title').textContent = '选择频率';
  const curLabel = freqLabel(settings.notifyInterval);
  document.getElementById('tpHours').querySelectorAll('.tp-col-item').forEach((el, i) => {
    el.classList.toggle('active', el.textContent === curLabel);
    el._val = FREQ_OPTS[i].val;
  });
  document.getElementById('tpOverlay').classList.add('show');
  requestAnimationFrame(() => {
    const active = document.getElementById('tpHours').querySelector('.active');
    if (active) document.getElementById('tpHours').scrollTop = active.offsetTop - CENTER;
  });
}

function openTimePicker(key) {
  tpMode = 'time'; tpTarget = key;
  document.getElementById('tpHours').classList.remove('freq-single');
  document.getElementById('tpMins').style.display = '';
  document.querySelector('.tp-colon').style.display = '';
  document.querySelector('.tp-title').textContent = '选择时间';
  populateCol('tpHours', Array.from({length: 24}, (_, i) => String(i).padStart(2, '0')));
  populateCol('tpMins',  Array.from({length: 60}, (_, i) => String(i).padStart(2, '0')));
  const time = settings[key + 'Time'];
  const [h, m] = time.split(':');
  document.getElementById('tpHours').querySelectorAll('.tp-col-item').forEach(el => el.classList.toggle('active', el.textContent === h));
  document.getElementById('tpMins').querySelectorAll('.tp-col-item').forEach(el => el.classList.toggle('active', el.textContent === m));
  document.getElementById('tpOverlay').classList.add('show');
  requestAnimationFrame(() => {
    const ah = document.getElementById('tpHours').querySelector('.active');
    const am = document.getElementById('tpMins').querySelector('.active');
    if (ah) document.getElementById('tpHours').scrollTop = ah.offsetTop - CENTER;
    if (am) document.getElementById('tpMins').scrollTop = am.offsetTop - CENTER;
  });
}

function selectTPVal(el) {
  if (el.classList.contains('active')) return; // already selected
  const col = el.parentElement;
  col.querySelectorAll('.tp-col-item').forEach(d => d.classList.remove('active'));
  el.classList.add('active');
  el.scrollIntoView({block:'center', behavior:'smooth'});

  if (tpMode === 'freq') {
    settings.notifyInterval = el._val;
    document.getElementById('notifyIntervalBtn').textContent = el.textContent;
  } else {
    const hSel = document.getElementById('tpHours').querySelector('.active');
    const mSel = document.getElementById('tpMins').querySelector('.active');
    if (hSel && mSel) {
      settings[tpTarget + 'Time'] = hSel.textContent + ':' + mSel.textContent;
      document.getElementById(tpTarget + 'Btn').textContent = settings[tpTarget + 'Time'];
    }
  }
  saveSettings();
}

function closeTimePicker() {
  document.getElementById('tpOverlay').classList.remove('show');
  tpMode = null; tpTarget = null;
}
