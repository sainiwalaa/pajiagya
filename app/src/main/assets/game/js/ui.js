// User Interface & HUD Coordinator
// Manages screens, minimap radar, compass, context buttons, inventory, and mission tracking

class UIManager {
  constructor() {
    this.canvasMinimap = document.getElementById('minimap-canvas');
    this.ctxMinimap = this.canvasMinimap ? this.canvasMinimap.getContext('2d') : null;
    this.compassEl = document.getElementById('compass-text');

    this.healthBar = document.getElementById('bar-health');
    this.armorBar = document.getElementById('bar-armor');
    this.ammoCur = document.getElementById('ammo-cur');
    this.ammoTotal = document.getElementById('ammo-total');
    this.weaponName = document.getElementById('weapon-name');

    this.contextActionBtn = document.getElementById('btn-interact');
    this.contextActionLabel = document.getElementById('interact-label');

    this.notifBox = document.getElementById('pickup-notification');
    this.notifTimer = null;
  }

  showNotification(text) {
    if (!this.notifBox) return;
    this.notifBox.textContent = text;
    this.notifBox.classList.add('visible');
    clearTimeout(this.notifTimer);
    this.notifTimer = setTimeout(() => {
      this.notifBox.classList.remove('visible');
    }, 2400);
  }

  updateHUD(player, enemies, mission, map) {
    if (!player) return;

    // 1. Health & Armor Bars
    if (this.healthBar) {
      const hpPct = Math.max(0, Math.min(100, (player.health / player.maxHealth) * 100));
      this.healthBar.style.width = hpPct + '%';
      if (hpPct < 30) this.healthBar.classList.add('low');
      else this.healthBar.classList.remove('low');
    }

    if (this.armorBar) {
      const armPct = Math.max(0, Math.min(100, (player.armor / player.maxArmor) * 100));
      this.armorBar.style.width = armPct + '%';
    }

    // 2. Ammo & Weapon
    if (this.ammoCur) this.ammoCur.textContent = player.currentAmmo;
    if (this.ammoTotal) {
      const res = player.inventory.ammo556;
      this.ammoTotal.textContent = player.isReloading ? 'RELOADING' : `/ ${player.equippedWeapon.magSize}`;
    }
    if (this.weaponName) this.weaponName.textContent = player.equippedWeapon.name;

    // 3. Compass
    if (this.compassEl) {
      const deg = Math.round(((player.cameraYaw * 180 / Math.PI) % 360 + 360) % 360);
      let cardinal = 'N';
      if (deg >= 23 && deg < 68) cardinal = 'NE';
      else if (deg >= 68 && deg < 113) cardinal = 'E';
      else if (deg >= 113 && deg < 158) cardinal = 'SE';
      else if (deg >= 158 && deg < 203) cardinal = 'S';
      else if (deg >= 203 && deg < 248) cardinal = 'SW';
      else if (deg >= 248 && deg < 293) cardinal = 'W';
      else if (deg >= 293 && deg < 338) cardinal = 'NW';
      this.compassEl.textContent = `${deg}° ${cardinal}`;
    }

    // 4. Mission Objective Tracker
    this.updateObjectiveUI(mission);

    // 5. Context-Sensitive Action Button
    this.updateContextButton(player, map);

    // 6. Minimap Radar
    this.drawMinimap(player, enemies, mission, map);

    // 7. Power Cooldown Badges
    this.updatePowerButtons(player);
  }

  updateObjectiveUI(mission) {
    const listEl = document.getElementById('objective-list');
    if (!listEl || !mission) return;

    let html = '';
    mission.objectives.forEach(obj => {
      const done = obj.current >= obj.target;
      html += `<div class="obj-item ${done ? 'done' : ''}">
        <span class="obj-check">${done ? '✓' : '○'}</span>
        <span class="obj-desc">${obj.text} (${obj.current}/${obj.target})</span>
      </div>`;
    });
    listEl.innerHTML = html;
  }

  updateContextButton(player, map) {
    if (!this.contextActionBtn || !this.contextActionLabel) return;

    if (player.inVehicle) {
      this.contextActionBtn.classList.add('visible');
      this.contextActionLabel.textContent = 'EXIT VEHICLE [E]';
      return;
    }

    // Check near vehicle
    let nearAction = null;
    map.vehicles.forEach(v => {
      if (player.position.distanceTo(v.position) < 3.2) {
        nearAction = 'ENTER VEHICLE [E]';
      }
    });

    // Check near supply crate
    map.supplyCrates.forEach(c => {
      if (!c.isOpened && player.position.distanceTo(c.position) < 2.5) {
        nearAction = c.isAirdrop ? 'OPEN AIRDROP [E]' : 'LOOT CRATE [E]';
      }
    });

    if (nearAction) {
      this.contextActionBtn.classList.add('visible');
      this.contextActionLabel.textContent = nearAction;
    } else {
      this.contextActionBtn.classList.remove('visible');
    }
  }

  updatePowerButtons(player) {
    const p1 = document.getElementById('btn-power-speed');
    const p2 = document.getElementById('btn-power-shield');
    const p3 = document.getElementById('btn-power-scan');

    if (p1) {
      const s = player.powers.speedBoost;
      p1.classList.toggle('active', s.active);
      p1.classList.toggle('cooldown', s.cooldown > 0);
    }
    if (p2) {
      const s = player.powers.energyShield;
      p2.classList.toggle('active', s.active);
      p2.classList.toggle('cooldown', s.cooldown > 0);
    }
    if (p3) {
      const s = player.powers.enemyScanner;
      p3.classList.toggle('active', s.active);
      p3.classList.toggle('cooldown', s.cooldown > 0);
    }
  }

  drawMinimap(player, enemies, mission, map) {
    if (!this.ctxMinimap || !this.canvasMinimap) return;
    const ctx = this.ctxMinimap;
    const w = this.canvasMinimap.width;
    const h = this.canvasMinimap.height;
    const cx = w / 2;
    const cy = h / 2;
    const range = 75; // world radius visible on minimap
    const scale = (w / 2) / range;

    // Clear
    ctx.clearRect(0, 0, w, h);

    // Radar grid ring
    ctx.strokeStyle = 'rgba(76, 175, 80, 0.35)';
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.arc(cx, cy, (w / 2) * 0.6, 0, Math.PI * 2);
    ctx.stroke();

    const toRadar = (worldX, worldZ) => {
      const dx = worldX - player.position.x;
      const dz = worldZ - player.position.z;
      return {
        x: cx + dx * scale,
        y: cy + dz * scale
      };
    };

    // Draw Mission Target Waypoint
    if (mission && mission.targetLocation) {
      const tPos = toRadar(mission.targetLocation.x, mission.targetLocation.z);
      ctx.fillStyle = '#ffd700';
      ctx.beginPath();
      ctx.arc(tPos.x, tPos.y, 5, 0, Math.PI * 2);
      ctx.fill();
    }

    // Draw Vehicles
    map.vehicles.forEach(v => {
      const pos = toRadar(v.position.x, v.position.z);
      ctx.fillStyle = '#ff9800';
      ctx.fillRect(pos.x - 3, pos.y - 3, 6, 6);
    });

    // Draw Supply Crates
    map.supplyCrates.forEach(c => {
      const pos = toRadar(c.position.x, c.position.z);
      ctx.fillStyle = c.isAirdrop ? '#ff1744' : '#8d6e63';
      ctx.fillRect(pos.x - 3, pos.y - 3, 6, 6);
    });

    // Draw Enemy radar blips (visible if Scanner power active or within 30m)
    enemies.forEach(b => {
      if (!b.isDead) {
        const d = player.position.distanceTo(b.position);
        if (player.powers.enemyScanner.active || d < 32) {
          const bPos = toRadar(b.position.x, b.position.z);
          ctx.fillStyle = '#f44336';
          ctx.beginPath();
          ctx.arc(bPos.x, bPos.y, 4, 0, Math.PI * 2);
          ctx.fill();
        }
      }
    });

    // Draw Player Arrow at Center
    ctx.save();
    ctx.translate(cx, cy);
    ctx.rotate(player.cameraYaw);
    ctx.fillStyle = '#00e5ff';
    ctx.beginPath();
    ctx.moveTo(0, -7);
    ctx.lineTo(5, 6);
    ctx.lineTo(0, 3);
    ctx.lineTo(-5, 6);
    ctx.closePath();
    ctx.fill();
    ctx.restore();
  }

  showScreen(screenId) {
    const screens = document.querySelectorAll('.screen');
    screens.forEach(s => s.classList.remove('active'));
    const target = document.getElementById(screenId);
    if (target) target.classList.add('active');
  }

  toggleInventory() {
    const modal = document.getElementById('modal-inventory');
    if (!modal) return;
    const isVis = modal.classList.toggle('active');
    if (isVis && window.gameInstance && window.gameInstance.player) {
      this.populateInventory(window.gameInstance.player);
    }
  }

  populateInventory(player) {
    const grid = document.getElementById('inventory-grid');
    if (!grid) return;

    const inv = player.inventory;
    const items = [
      { name: 'Primary: ' + player.equippedWeapon.name, count: `${player.currentAmmo}/${player.equippedWeapon.magSize}`, icon: '🔫' },
      { name: 'Medkit (+75 HP)', count: `x${inv.medkits}`, icon: '💊', action: 'heal' },
      { name: 'Energy Drink (+50 Armor)', count: `x${inv.energyDrinks}`, icon: '⚡', action: 'drink' },
      { name: '5.56mm Ammo', count: `${inv.ammo556} rds`, icon: '📦' },
      { name: 'Body Armor', count: `${player.armor}%`, icon: '🛡️' }
    ];

    let html = '';
    items.forEach(it => {
      html += `<div class="inv-slot" ${it.action ? `onclick="window.uiManager.useItem('${it.action}')"` : ''}>
        <div class="inv-icon">${it.icon}</div>
        <div class="inv-name">${it.name}</div>
        <div class="inv-count">${it.count}</div>
      </div>`;
    });
    grid.innerHTML = html;
  }

  useItem(action) {
    if (!window.gameInstance || !window.gameInstance.player) return;
    const p = window.gameInstance.player;
    if (action === 'heal') {
      p.heal();
      this.showNotification('Applied Medkit! Restored Health.');
    } else if (action === 'drink') {
      p.useEnergyDrink();
      this.showNotification('Consumed Energy Drink! Restored Armor.');
    }
    this.populateInventory(p);
  }

  togglePauseMenu() {
    const modal = document.getElementById('modal-pause');
    if (modal) modal.classList.toggle('active');
  }

  showMissionComplete(mission, xp, coins) {
    const screen = document.getElementById('modal-victory');
    if (!screen) return;
    document.getElementById('victory-title').textContent = `${mission.title} COMPLETED!`;
    document.getElementById('victory-xp').textContent = `+${xp} XP`;
    document.getElementById('victory-coins').textContent = `+${coins} COINS`;
    screen.classList.add('active');
  }

  showGameOver(attackerName) {
    const screen = document.getElementById('modal-gameover');
    if (!screen) return;
    document.getElementById('gameover-reason').textContent = `Eliminated by ${attackerName}`;
    screen.classList.add('active');
  }
}

window.uiManager = new UIManager();
