// Weapons & Combat System

const WEAPON_REGISTRY = {
  rifle: {
    id: 'rifle',
    name: 'M4 Tactical Rifle',
    type: 'Assault Rifle',
    damage: 34,
    fireRate: 0.12, // seconds between shots (~500 RPM)
    magSize: 30,
    reloadDuration: 2.0,
    range: 160,
    spread: 0.022,
    recoil: 0.02,
    ammoType: '5.56mm',
    color: 0x4fc3f7,
    unlockedByDefault: true,
    description: 'Versatile all-around assault rifle with moderate recoil and high fire rate.'
  },
  sniper: {
    id: 'sniper',
    name: 'AWM-50 Heavy Sniper',
    type: 'Sniper Rifle',
    damage: 110,
    fireRate: 1.1,
    magSize: 5,
    reloadDuration: 2.8,
    range: 350,
    spread: 0.003,
    recoil: 0.07,
    ammoType: '.300 Mag',
    color: 0x76ff03,
    unlockedByDefault: false,
    price: 600,
    description: 'Devastating high-caliber bolt-action rifle. One-shot headshot.'
  },
  shotgun: {
    id: 'shotgun',
    name: 'S12 Tactical Shotgun',
    type: 'Shotgun',
    damage: 18, // per pellet (8 pellets = 144 max close range)
    pellets: 8,
    fireRate: 0.7,
    magSize: 6,
    reloadDuration: 2.4,
    range: 45,
    spread: 0.075,
    recoil: 0.055,
    ammoType: '12 Gauge',
    color: 0xff7043,
    unlockedByDefault: false,
    price: 450,
    description: 'Lethal close-quarters breaching weapon with wide spread pattern.'
  },
  pistol: {
    id: 'pistol',
    name: 'P9 Tactical Sidearm',
    type: 'Handgun',
    damage: 26,
    fireRate: 0.22,
    magSize: 15,
    reloadDuration: 1.4,
    range: 85,
    spread: 0.035,
    recoil: 0.015,
    ammoType: '9mm',
    color: 0xffd54f,
    unlockedByDefault: true,
    description: 'Lightweight rapid semi-automatic secondary pistol.'
  }
};

window.WEAPON_REGISTRY = WEAPON_REGISTRY;
