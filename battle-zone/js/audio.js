// Procedural Tactical Audio Synthesizer (Web Audio API)
// High-fidelity sound effects without any external audio asset dependency

class AudioManager {
  constructor() {
    this.ctx = null;
    this.masterGain = null;
    this.muted = false;
    this.volume = 0.8;
  }

  init() {
    if (this.ctx) return;
    try {
      const AudioCtx = window.AudioContext || window.webkitAudioContext;
      this.ctx = new AudioCtx();
      this.masterGain = this.ctx.createGain();
      this.masterGain.gain.setValueAtTime(this.volume, this.ctx.currentTime);
      this.masterGain.connect(this.ctx.destination);
    } catch (e) {
      console.warn("AudioContext not supported", e);
    }
  }

  resume() {
    if (this.ctx && this.ctx.state === 'suspended') {
      this.ctx.resume();
    }
  }

  playGunshot(weaponType = 'rifle') {
    if (!this.ctx || this.muted) return;
    this.resume();

    const now = this.ctx.currentTime;
    const dur = weaponType === 'sniper' ? 0.35 : weaponType === 'shotgun' ? 0.28 : 0.12;

    // Noise burst for explosive bang
    const bufferSize = this.ctx.sampleRate * dur;
    const buffer = this.ctx.createBuffer(1, bufferSize, this.ctx.sampleRate);
    const data = buffer.getChannelData(0);
    for (let i = 0; i < bufferSize; i++) {
      data[i] = (Math.random() * 2 - 1) * Math.exp(-i / (bufferSize * 0.25));
    }

    const noise = this.ctx.createBufferSource();
    noise.buffer = buffer;

    // Filter
    const filter = this.ctx.createBiquadFilter();
    filter.type = 'lowpass';
    filter.frequency.setValueAtTime(weaponType === 'sniper' ? 1200 : 2200, now);
    filter.frequency.exponentialRampToValueAtTime(150, now + dur);

    // Punch tone
    const osc = this.ctx.createOscillator();
    osc.type = 'sawtooth';
    osc.frequency.setValueAtTime(weaponType === 'sniper' ? 180 : 320, now);
    osc.frequency.exponentialRampToValueAtTime(40, now + dur);

    const gain = this.ctx.createGain();
    gain.gain.setValueAtTime(weaponType === 'sniper' ? 1.0 : 0.7, now);
    gain.gain.exponentialRampToValueAtTime(0.01, now + dur);

    noise.connect(filter);
    filter.connect(gain);
    osc.connect(gain);
    gain.connect(this.masterGain);

    noise.start(now);
    osc.start(now);
    noise.stop(now + dur);
    osc.stop(now + dur);

    // Call Android native vibration if running inside WebView
    if (window.AndroidBridge && window.AndroidBridge.vibrate) {
      window.AndroidBridge.vibrate(30);
    }
  }

  playHitMarker(isHeadshot = false) {
    if (!this.ctx || this.muted) return;
    this.resume();

    const now = this.ctx.currentTime;
    const osc = this.ctx.createOscillator();
    osc.type = 'triangle';
    osc.frequency.setValueAtTime(isHeadshot ? 1400 : 880, now);

    const gain = this.ctx.createGain();
    gain.gain.setValueAtTime(0.3, now);
    gain.gain.exponentialRampToValueAtTime(0.001, now + 0.05);

    osc.connect(gain);
    gain.connect(this.masterGain);
    osc.start(now);
    osc.stop(now + 0.05);

    if (window.AndroidBridge && window.AndroidBridge.vibrate) {
      window.AndroidBridge.vibrate(isHeadshot ? 45 : 20);
    }
  }

  playReload() {
    if (!this.ctx || this.muted) return;
    this.resume();

    const now = this.ctx.currentTime;
    // Click clack reload sound
    [0, 0.25].forEach((delay, idx) => {
      const osc = this.ctx.createOscillator();
      osc.type = 'sine';
      osc.frequency.setValueAtTime(idx === 0 ? 350 : 620, now + delay);
      const gain = this.ctx.createGain();
      gain.gain.setValueAtTime(0.25, now + delay);
      gain.gain.exponentialRampToValueAtTime(0.01, now + delay + 0.08);

      osc.connect(gain);
      gain.connect(this.masterGain);
      osc.start(now + delay);
      osc.stop(now + delay + 0.08);
    });
  }

  playExplosion() {
    if (!this.ctx || this.muted) return;
    this.resume();

    const now = this.ctx.currentTime;
    const dur = 0.8;
    const bufferSize = this.ctx.sampleRate * dur;
    const buffer = this.ctx.createBuffer(1, bufferSize, this.ctx.sampleRate);
    const data = buffer.getChannelData(0);
    for (let i = 0; i < bufferSize; i++) {
      data[i] = (Math.random() * 2 - 1) * Math.exp(-i / (bufferSize * 0.4));
    }

    const noise = this.ctx.createBufferSource();
    noise.buffer = buffer;

    const filter = this.ctx.createBiquadFilter();
    filter.type = 'lowpass';
    filter.frequency.setValueAtTime(600, now);
    filter.frequency.exponentialRampToValueAtTime(60, now + dur);

    const gain = this.ctx.createGain();
    gain.gain.setValueAtTime(1.0, now);
    gain.gain.exponentialRampToValueAtTime(0.01, now + dur);

    noise.connect(filter);
    filter.connect(gain);
    gain.connect(this.masterGain);
    noise.start(now);
    noise.stop(now + dur);

    if (window.AndroidBridge && window.AndroidBridge.vibrate) {
      window.AndroidBridge.vibrate(150);
    }
  }

  playPickup() {
    if (!this.ctx || this.muted) return;
    this.resume();

    const now = this.ctx.currentTime;
    const osc = this.ctx.createOscillator();
    osc.type = 'sine';
    osc.frequency.setValueAtTime(520, now);
    osc.frequency.exponentialRampToValueAtTime(880, now + 0.1);

    const gain = this.ctx.createGain();
    gain.gain.setValueAtTime(0.2, now);
    gain.gain.exponentialRampToValueAtTime(0.01, now + 0.1);

    osc.connect(gain);
    gain.connect(this.masterGain);
    osc.start(now);
    osc.stop(now + 0.1);
  }

  playVehicleEngine(isAccelerating = false) {
    if (!this.ctx || this.muted) return;
    // Low frequency hum
    const now = this.ctx.currentTime;
    const osc = this.ctx.createOscillator();
    osc.type = 'sawtooth';
    osc.frequency.setValueAtTime(isAccelerating ? 110 : 75, now);
    const gain = this.ctx.createGain();
    gain.gain.setValueAtTime(0.08, now);
    gain.gain.exponentialRampToValueAtTime(0.01, now + 0.09);

    const filter = this.ctx.createBiquadFilter();
    filter.type = 'lowpass';
    filter.frequency.setValueAtTime(350, now);

    osc.connect(filter);
    filter.connect(gain);
    gain.connect(this.masterGain);
    osc.start(now);
    osc.stop(now + 0.09);
  }

  playPowerActivation() {
    if (!this.ctx || this.muted) return;
    this.resume();

    const now = this.ctx.currentTime;
    const osc = this.ctx.createOscillator();
    osc.type = 'sine';
    osc.frequency.setValueAtTime(300, now);
    osc.frequency.exponentialRampToValueAtTime(950, now + 0.25);

    const gain = this.ctx.createGain();
    gain.gain.setValueAtTime(0.4, now);
    gain.gain.exponentialRampToValueAtTime(0.01, now + 0.3);

    osc.connect(gain);
    gain.connect(this.masterGain);
    osc.start(now);
    osc.stop(now + 0.3);
  }
}

window.audio = new AudioManager();
