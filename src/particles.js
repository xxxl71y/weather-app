/* ===== 雨/雪粒子 ===== */
const canvas = document.getElementById('rainCanvas');
const ctx = canvas.getContext('2d');
let particles = [];
let animId;

function resizeCanvas() {
  canvas.width = window.innerWidth;
  canvas.height = window.innerHeight;
}
window.addEventListener('resize', resizeCanvas);
resizeCanvas();

function startParticles(theme) {
  cancelAnimationFrame(animId);
  particles = [];

  if (theme === 'rainy' || theme === 'stormy') {
    for (let i = 0; i < (theme === 'stormy' ? 180 : 100); i++) {
      particles.push({
        x: Math.random() * canvas.width,
        y: Math.random() * canvas.height,
        length: 8 + Math.random() * 16,
        speed: 8 + Math.random() * 14,
        opacity: 0.15 + Math.random() * 0.25,
      });
    }
  } else if (theme === 'snowy') {
    for (let i = 0; i < 80; i++) {
      particles.push({
        x: Math.random() * canvas.width,
        y: Math.random() * canvas.height,
        r: 1 + Math.random() * 3,
        speed: 0.4 + Math.random() * 1.2,
        opacity: 0.4 + Math.random() * 0.5,
        wind: -0.3 + Math.random() * 0.6,
      });
    }
  }

  function draw() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    for (const p of particles) {
      ctx.beginPath();
      if (p.length !== undefined) {
        // 雨滴
        ctx.strokeStyle = `rgba(180,210,240,${p.opacity})`;
        ctx.lineWidth = 1;
        ctx.moveTo(p.x, p.y);
        ctx.lineTo(p.x, p.y + p.length);
        ctx.stroke();
        p.y += p.speed;
        if (p.y > canvas.height) {
          p.y = -p.length;
          p.x = Math.random() * canvas.width;
        }
      } else {
        // 雪花
        ctx.fillStyle = `rgba(255,255,255,${p.opacity})`;
        ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
        ctx.fill();
        p.y += p.speed;
        p.x += p.wind;
        if (p.y > canvas.height) {
          p.y = -p.r * 2;
          p.x = Math.random() * canvas.width;
        }
        if (p.x < 0) p.x = canvas.width;
        if (p.x > canvas.width) p.x = 0;
      }
    }

    if (particles.length) animId = requestAnimationFrame(draw);
  }
  if (particles.length) animId = requestAnimationFrame(draw);
}

function setTheme(theme) {
  document.body.className = theme;
  startParticles(theme);
}
