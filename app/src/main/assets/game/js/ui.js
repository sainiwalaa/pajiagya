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

    this.debugVisible = false;
    this.debugOverlay = document.getElementById('debug-overlay');
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

    // 2. Ammo & Weapon (Shows Magazine / Reserve Ammo)
    if (this.ammoCur) this.ammoCur.textContent = player.currentAmmo;
    if (this.ammoTotal) {
      let reserve = 0;
      if (player.equippedWeaponId === 'shotgun') reserve = player.inventory.ammoShotgun || 0;
      else if (player.equippedWeaponId === 'sniper') reserve = player.inventory.ammoSniper || 0;
      else if (player.equippedWeaponId === 'pistol') reserve = player.inventory.ammo9mm || 0;
      else reserve = player.inventory.ammo556 || 0;
      this.ammoTotal.textContent = player.isReloading ? 'RELOADING' : `/ ${reserve}`;
    }
    if (this.weaponName) this.weaponName.textContent = player.equippedWeapon.name;

    // Mobile driving button context adaptation
    const jumpBtn = document.getElementById('btn-jump');
    if (jumpBtn) {
      jumpBtn.textContent = player.inVehicle ? 'BRAKE' : 'JUMP';
    }
    const fireBtn = document.getElementById('btn-fire');
    if (fireBtn) {
      fireBtn.style.opacity = player.inVehicle ? '0.35' : '1.0';
      fireBtn.style.pointerEvents = player.inVehicle ? 'none' : 'auto';
    }

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

    // 8. Debug Overlay
    if (this.debugVisible) {
      this.updateDebugOverlay(player, map, enemies);
    }
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

    let nearAction = null;

    // 1. Check near Death Crates (enemy loot drops)
    if (map && map.deathCrates) {
      for (let i = 0; i < map.deathCrates.length; i++) {
        const dc = map.deathCrates[i];
        if (!dc.looted && player.position.distanceTo(dc.position) < 3.2) {
          nearAction = `LOOT ${dc.enemyName ? dc.enemyName.toUpperCase() : 'DEATH CRATE'} [E]`;
          break;
        }
      }
    }

    // 2. Check near Field Loot
    if (!nearAction && map && map.lootSpawns) {
      for (let i = 0; i < map.lootSpawns.length; i++) {
        const loot = map.lootSpawns[i];
        if (!loot.collected && player.position.distanceTo(loot.position) < 2.8) {
          const typeName = loot.type.replace('_', ' ').toUpperCase();
          nearAction = `PICK UP ${typeName} [E]`;
          break;
        }
      }
    }

    // 3. Check near vehicle
    if (!nearAction && map && map.vehicles) {
      for (let i = 0; i < map.vehicles.length; i++) {
        const v = map.vehicles[i];
        if (!v.isOccupied && player.position.distanceTo(v.position) < 3.5) {
          nearAction = 'ENTER VEHICLE [E]';
          break;
        }
      }
    }

    // 4. Check near supply crate
    if (!nearAction && map && map.supplyCrates) {
      for (let i = 0; i < map.supplyCrates.length; i++) {
        const c = map.supplyCrates[i];
        if (!c.isOpened && player.position.distanceTo(c.position) < 2.8) {
          nearAction = c.isAirdrop ? 'OPEN AIRDROP [E]' : 'LOOT CRATE [E]';
          break;
        }
      }
    }

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
    if (isVis) {
      if (window.gameInstance && window.gameInstance.player) {
        this.populateInventory(window.gameInstance.player);
      } else {
        this.populateDefaultInventory();
      }
    }
  }

  populateDefaultInventory() {
    const grid = document.getElementById('inventory-grid');
    if (!grid) return;
    const items = [
      { name: 'Primary: M4 Tactical Rifle', count: '30/30', icon: '🔫' },
      { name: 'Secondary: P9 Sidearm', count: '15/15', icon: '🔫' },
      { name: 'Medkit (+75 HP)', count: 'x3', icon: '💊' },
      { name: 'Energy Drink (+50 Armor)', count: 'x2', icon: '⚡' },
      { name: '5.56mm Ammo', count: '120 rds', icon: '📦' },
      { name: 'Body Armor', count: '100%', icon: '🛡️' }
    ];
    let html = '';
    items.forEach(it => {
      html += `<div class="inv-slot">
        <div class="inv-icon">${it.icon}</div>
        <div class="inv-name">${it.name}</div>
        <div class="inv-count">${it.count}</div>
      </div>`;
    });
    grid.innerHTML = html;
  }

  populateInventory(player) {
    const grid = document.getElementById('inventory-grid');
    if (!grid) return;

    const inv = player.inventory;
    const primW = WEAPON_REGISTRY[inv.primary] || WEAPON_REGISTRY.rifle;
    const secW = WEAPON_REGISTRY[inv.secondary] || WEAPON_REGISTRY.pistol;

    const items = [
      {
        name: `Primary: ${primW.name} ${player.currentSlot === 'primary' ? '★ (EQUIPPED)' : ''}`,
        count: `${player.currentSlot === 'primary' ? player.currentAmmo : primW.magSize}/${primW.magSize}`,
        icon: '🔫',
        action: 'equip_primary'
      },
      {
        name: `Secondary: ${secW.name} ${player.currentSlot === 'secondary' ? '★ (EQUIPPED)' : ''}`,
        count: `${player.currentSlot === 'secondary' ? player.currentAmmo : secW.magSize}/${secW.magSize}`,
        icon: '🔫',
        action: 'equip_secondary'
      },
      { name: 'Medkit (+75 HP)', count: `x${inv.medkits}`, icon: '💊', action: 'heal' },
      { name: 'Energy Drink (+50 Armor)', count: `x${inv.energyDrinks}`, icon: '⚡', action: 'drink' },
      { name: '5.56mm Rifle Ammo', count: `${inv.ammo556 || 0} rds`, icon: '📦' },
      { name: '12-Gauge Shotgun Shells', count: `${inv.ammoShotgun || 0} rds`, icon: '📦' },
      { name: 'Sniper Caliber Ammo', count: `${inv.ammoSniper || 0} rds`, icon: '📦' },
      { name: 'Body Armor', count: `${player.armor}%`, icon: '🛡️' }
    ];

    let html = '';
    items.forEach(it => {
      html += `<div class="inv-slot" ${it.action ? `onclick="window.uiManager.useItem('${it.action}')"` : ''} style="${it.action ? 'cursor:pointer;' : ''}">
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
      if (p.health >= p.maxHealth) {
        this.showNotification('Health is already at 100%');
      } else if (p.inventory.medkits <= 0) {
        this.showNotification('No Medkits remaining');
      } else {
        p.heal();
        this.showNotification('Applied Medkit! Restored Health.');
      }
    } else if (action === 'drink') {
      if (p.inventory.energyDrinks <= 0) {
        this.showNotification('No Energy Drinks remaining');
      } else {
        p.useEnergyDrink();
        this.showNotification('Consumed Energy Drink! Restored Armor.');
      }
    } else if (action === 'equip_primary') {
      p.switchWeapon('primary');
      this.showNotification(`Equipped Primary: ${p.equippedWeapon.name}`);
    } else if (action === 'equip_secondary') {
      p.switchWeapon('secondary');
      this.showNotification(`Equipped Secondary: ${p.equippedWeapon.name}`);
    }
    this.populateInventory(p);
  }

  togglePauseMenu() {
    const modal = document.getElementById('modal-pause');
    if (modal) {
      modal.classList.toggle('active');
      if (!modal.classList.contains('active') && window.gameInstance && window.gameInstance.clock) {
        window.gameInstance.clock.getDelta();
      }
    }
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

  toggleDebugOverlay() {
    this.debugVisible = !this.debugVisible;
    if (!this.debugOverlay) {
      this.debugOverlay = document.getElementById('debug-overlay');
    }
    if (this.debugOverlay) {
      this.debugOverlay.style.display = this.debugVisible ? 'block' : 'none';
      if (this.debugVisible && window.gameInstance) {
        this.updateDebugOverlay(
          window.gameInstance.player,
          window.gameInstance.map,
          window.gameInstance.enemyManager ? window.gameInstance.enemyManager.bots : []
        );
      }
    }
    this.showNotification(`Debug Overlay: ${this.debugVisible ? 'ENABLED' : 'DISABLED'}`);
  }

  updateDebugOverlay(player, map, enemies) {
    if (!this.debugOverlay || !player) return;

    // Determine on-foot control substate
    let onFootState = 'Idle';
    if (!player.isGrounded) onFootState = 'In-Air / Jumping';
    else if (player.isSprinting) onFootState = 'Sprinting';
    else if (player.isCrouched) onFootState = 'Crouched';
    else if (player.isAiming) onFootState = 'Aiming Down Sights';
    else if (window.gameInstance && window.gameInstance.input && (window.gameInstance.input.moveForward || window.gameInstance.input.moveRight)) {
      onFootState = 'Walking / Running';
    }

    const enemyList = (enemies || []).map(b => `${b.name}: ${Math.max(0, Math.round(b.health))}/${b.maxHealth}HP ${b.isDead ? '[DEAD]' : ''}`).join('<br>');
    const activeBots = (enemies || []).filter(b => !b.isDead).length;
    const totalBots = (enemies || []).length;

    const deathCrates = (map && map.deathCrates) ? map.deathCrates : [];
    const unlootedDc = deathCrates.filter(dc => !dc.looted).length;
    const fieldLoot = (map && map.lootSpawns) ? map.lootSpawns : [];
    const activeFl = fieldLoot.filter(l => !l.collected).length;
    const supplyCrates = (map && map.supplyCrates) ? map.supplyCrates : [];
    const unlootedSc = supplyCrates.filter(c => !c.isOpened).length;

    const inv = player.inventory;
    const totalAmmo = (inv.ammo556 || 0) + (inv.ammoShotgun || 0) + (inv.ammoSniper || 0) + (inv.ammo9mm || 0);

    this.debugOverlay.innerHTML = `
      <div style="font-weight:bold;color:#ffb800;border-bottom:1px solid #455a64;padding-bottom:4px;margin-bottom:6px;display:flex;justify-content:space-between;">
        <span>🐛 BATTLE ZONE DEBUG [F3]</span>
        <span style="color:#81c784;cursor:pointer;" onclick="window.uiManager.toggleDebugOverlay()">[CLOSE ✕]</span>
      </div>
      <div><b>Input Values:</b> Fwd: ${window.gameInstance && window.gameInstance.input ? window.gameInstance.input.moveForward.toFixed(2) : '0.00'}, Right: ${window.gameInstance && window.gameInstance.input ? window.gameInstance.input.moveRight.toFixed(2) : '0.00'}, Sprint: ${player.isSprinting}</div>
      <div><b>Player Coords:</b> X: ${player.position.x.toFixed(2)}, Y: ${player.position.y.toFixed(2)}, Z: ${player.position.z.toFixed(2)}</div>
      <div><b>Control State:</b> <span style="color:#4fc3f7;">${player.inVehicle ? 'DRIVING VEHICLE' : 'ON-FOOT (' + onFootState + ')'}</span></div>
      <div><b>Vehicle State:</b> ${player.inVehicle ? `<span style="color:#ffb800;">IN VEHICLE (Speed: ${player.vehicleSpeed.toFixed(1)} m/s, Steer: ${(player.inVehicle.rotation.y * 180 / Math.PI).toFixed(1)}°)</span>` : 'None (On-Foot)'}</div>
      <div style="margin-top:4px;"><b>Enemies (${activeBots}/${totalBots} Active):</b><br><span style="font-size:11px;color:#cfd8dc;">${enemyList || 'None'}</span></div>
      <div style="margin-top:4px;"><b>Loot in Scene:</b> Death Crates: ${deathCrates.length} (${unlootedDc} unlooted) | Field: ${fieldLoot.length} (${activeFl} active) | Crates: ${supplyCrates.length} (${unlootedSc} closed)</div>
      <div style="margin-top:4px;"><b>Inventory:</b> Prim: ${inv.primary} (${player.currentAmmo}) | Sec: ${inv.secondary} | Medkits: ${inv.medkits}/5 | Drinks: ${inv.energyDrinks}/5 | Ammo Total: ${totalAmmo} rds</div>
      <div style="margin-top:4px;color:${window.lastRuntimeError ? '#ff5252' : '#81c784'};"><b>Runtime Errors:</b> ${window.lastRuntimeError || 'None (Clean 60 FPS)'}</div>
    `;
  }
}

window.uiManager = new UIManager();
