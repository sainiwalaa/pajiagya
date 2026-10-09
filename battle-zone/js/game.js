// Main Game Coordinator & 60 FPS Render Loop
// Integrates 3D graphics, physics, combat, missions, and screen states

class BattleZoneGame {
  constructor() {
    this.container = document.getElementById('canvas-container');
    this.scene = null;
    this.camera = null;
    this.renderer = null;

    this.map = null;
    this.player = null;
    this.enemyManager = null;
    this.input = null;

    this.bulletTracers = [];
    this.clock = new THREE.Clock();
    this.isRunning = false;
    this.currentLevel = 1;

    this.initThree();
  }

  initThree() {
    // 1. Scene with atmospheric battlefield fog
    this.scene = new THREE.Scene();
    this.scene.background = new THREE.Color(0x7da4c2); // Daylight military sky
    this.scene.fog = new THREE.FogExp2(0x7da4c2, 0.007);

    // 2. Camera
    const aspect = window.innerWidth / window.innerHeight;
    this.camera = new THREE.PerspectiveCamera(65, aspect, 0.1, 500);

    // 3. Renderer with high performance settings
    this.renderer = new THREE.WebGLRenderer({ antialias: true, powerPreference: 'high-performance' });
    this.renderer.setSize(window.innerWidth, window.innerHeight);
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2.0));
    this.renderer.shadowMap.enabled = true;
    this.container.appendChild(this.renderer.domElement);

    // 4. Lighting
    const hemiLight = new THREE.HemisphereLight(0xffffff, 0x445544, 0.65);
    hemiLight.position.set(0, 50, 0);
    this.scene.add(hemiLight);

    const dirLight = new THREE.DirectionalLight(0xfffaed, 0.95);
    dirLight.position.set(80, 120, 60);
    dirLight.castShadow = true;
    dirLight.shadow.mapSize.width = 1024;
    dirLight.shadow.mapSize.height = 1024;
    this.scene.add(dirLight);

    // 5. Input
    this.input = new InputManager();

    // 6. Handle window resizing
    window.addEventListener('resize', () => {
      this.camera.aspect = window.innerWidth / window.innerHeight;
      this.camera.updateProjectionMatrix();
      this.renderer.setSize(window.innerWidth, window.innerHeight);
    });
  }

  startMission(levelNumber = 1) {
    const wasRunning = this.isRunning;
    this.currentLevel = levelNumber;
    window.audio.init();

    // Reset scene
    while (this.scene.children.length > 0) {
      this.scene.remove(this.scene.children[0]);
    }

    // Re-add lights
    const hemi = new THREE.HemisphereLight(0xffffff, 0x445544, 0.65);
    hemi.position.set(0, 50, 0);
    this.scene.add(hemi);

    const dir = new THREE.DirectionalLight(0xfffaed, 0.95);
    dir.position.set(80, 120, 60);
    this.scene.add(dir);

    // Build World Map
    this.map = new BattleMap(this.scene);
    this.map.build();

    // Spawn Player
    this.player = new PlayerController(this.scene, this.camera, this.map);

    // Load Mission & Spawn Enemies
    const mission = window.missionManager.loadLevel(levelNumber);
    this.enemyManager = new EnemyManager(this.scene, this.map);
    this.enemyManager.spawnMissionEnemies(mission);

    this.bulletTracers = [];
    this.clock.getDelta(); // reset clock delta to prevent jump
    this.isRunning = true;

    // Show in-game HUD
    window.uiManager.showScreen('screen-game');
    window.uiManager.showNotification(`Loaded ${mission.title}! Ready for combat.`);

    if (!wasRunning) {
      this.loop();
    }
  }

  loop() {
    if (!this.isRunning) return;
    requestAnimationFrame(() => this.loop());

    const delta = Math.min(this.clock.getDelta(), 0.08); // Cap delta to prevent tunneling

    // 1. Update Player
    if (this.player) {
      this.player.update(delta, this.input);

      // Check Weapon Switch input
      if (this.input.switchWeaponSlot) {
        this.player.switchWeapon(this.input.switchWeaponSlot);
        this.input.switchWeaponSlot = null;
      }

      // Check Interact key [E] / [F]
      if (this.input.interact) {
        this.handlePlayerInteract();
        this.input.interact = false;
      }

      // Check Loot Collisions
      this.checkLootPickups();
    }

    // 2. Update AI Enemies
    if (this.enemyManager && this.player) {
      this.enemyManager.update(delta, this.player.position, this.player.isDead);
    }

    // 3. Update Bullet Tracers
    this.updateBulletTracers(delta);

    // 4. Update HUD & Minimap
    if (window.uiManager && this.player && this.enemyManager && this.map) {
      window.uiManager.updateHUD(
        this.player,
        this.enemyManager.bots,
        window.missionManager.currentMission,
        this.map
      );
    }

    // 5. Check Mission Extraction Zone
    if (this.currentLevel === 5 && this.player && !this.player.isDead) {
      const curMission = window.missionManager.currentMission;
      if (curMission && this.player.position.distanceTo(curMission.targetLocation) < 15) {
        window.missionManager.updateObjective('extract', 1);
        this.checkMissionStatus();
      }
    }

    // Render 3D Frame
    this.renderer.render(this.scene, this.camera);
  }

  handlePlayerInteract() {
    if (!this.player || !this.map) return;

    // If currently driving, exit vehicle
    if (this.player.inVehicle) {
      this.player.exitVehicle();
      return;
    }

    // Check enter vehicle
    for (let i = 0; i < this.map.vehicles.length; i++) {
      const v = this.map.vehicles[i];
      if (this.player.position.distanceTo(v.position) < 3.4) {
        this.player.enterVehicle(v);
        window.missionManager.updateObjective('vehicle', 1);
        this.checkMissionStatus();
        return;
      }
    }

    // Check open supply crate
    for (let i = 0; i < this.map.supplyCrates.length; i++) {
      const c = this.map.supplyCrates[i];
      if (!c.isOpened && this.player.position.distanceTo(c.position) < 2.8) {
        c.isOpened = true;
        c.mesh.position.y -= 0.3; // Open visual feedback
        this.player.inventory.ammo556 += 60;
        this.player.inventory.medkits += 1;
        this.player.armor = 100;
        window.audio.playPickup();
        window.uiManager.showNotification(c.isAirdrop ? '+Airdrop Gear: Ammo, Armor & Medkit!' : '+Supply Crate Looted!');
        window.missionManager.updateObjective('crate', 1);
        this.checkMissionStatus();
        return;
      }
    }
  }

  checkLootPickups() {
    this.map.lootSpawns.forEach(loot => {
      if (!loot.collected) {
        loot.mesh.rotation.y += 0.04; // Rotating 3D pickup
        if (this.player.position.distanceTo(loot.position) < 2.0) {
          loot.collected = true;
          this.scene.remove(loot.group);

          // Apply loot reward
          if (loot.type.includes('ammo')) {
            this.player.inventory.ammo556 += 45;
            window.uiManager.showNotification('+45 Rifle Ammo Collected');
          } else if (loot.type === 'medkit') {
            this.player.inventory.medkits += 1;
            window.uiManager.showNotification('+1 Tactical Medkit Collected');
          } else if (loot.type === 'energy_drink') {
            this.player.inventory.energyDrinks += 1;
            window.uiManager.showNotification('+1 Energy Drink Collected');
          } else if (loot.type.includes('armor')) {
            this.player.armor = 100;
            window.uiManager.showNotification('+Body Armor Restored to 100%!');
          } else if (loot.type === 'sniper') {
            this.player.inventory.secondary = 'sniper';
            window.uiManager.showNotification('Equipped AWM-50 Sniper Rifle!');
          } else if (loot.type === 'shotgun') {
            this.player.inventory.secondary = 'shotgun';
            window.uiManager.showNotification('Equipped S12 Shotgun!');
          }

          window.missionManager.updateObjective('loot', 1);
          window.audio.playPickup();
          this.checkMissionStatus();
        }
      }
    });
  }

  handlePlayerShot(raycaster, weapon) {
    // 1. Gather all shootable bot meshes
    const botMeshes = [];
    const botMap = new Map();

    this.enemyManager.bots.forEach(b => {
      if (!b.isDead) {
        b.character.root.traverse(child => {
          if (child.isMesh) {
            botMeshes.push(child);
            botMap.set(child, b);
          }
        });
      }
    });

    // 2. Gather solid obstacle meshes (walls, rocks, buildings) for line-of-sight blocking
    const obstacleMeshes = [];
    if (this.scene) {
      this.scene.traverse(child => {
        if (child.isMesh && child.name && !botMap.has(child) && child !== this.player.character.root) {
          if (child.parent && child.parent.name && (child.parent.name.includes('building') || child.parent.name.includes('vehicle') || child.parent.name.includes('crate'))) {
            obstacleMeshes.push(child);
          }
        }
      });
    }

    const allTargets = [...botMeshes, ...obstacleMeshes];
    const intersects = raycaster.intersectObjects(allTargets, false);
    const startPos = this.player.character.muzzle.getWorldPosition(new THREE.Vector3());

    if (intersects.length > 0) {
      const hit = intersects[0];
      const bot = botMap.get(hit.object);

      if (bot) {
        // Hit enemy bot!
        const isHeadshot = (hit.point.y - bot.position.y) > 1.35;
        bot.takeDamage(weapon.damage, isHeadshot);
        window.audio.playHitMarker(isHeadshot);
        this.spawnBulletTracer(startPos, hit.point, 0xffeb3b);
      } else {
        // Hit a solid wall / obstacle
        window.audio.playHitMarker(false);
        this.spawnBulletTracer(startPos, hit.point, 0xff7043);
      }
    } else {
      // Bullet travels to max range along ray direction
      const endPos = startPos.clone().add(raycaster.ray.direction.clone().multiplyScalar(weapon.range));
      this.spawnBulletTracer(startPos, endPos, 0xffeb3b);
    }
  }

  spawnBulletTracer(from, to, colorHex = 0xffeb3b) {
    const geo = new THREE.BufferGeometry().setFromPoints([from, to]);
    const mat = new THREE.LineBasicMaterial({ color: colorHex, linewidth: 2 });
    const line = new THREE.Line(geo, mat);
    this.scene.add(line);
    this.bulletTracers.push({ mesh: line, life: 0.08 });
  }

  updateBulletTracers(delta) {
    for (let i = this.bulletTracers.length - 1; i >= 0; i--) {
      const tr = this.bulletTracers[i];
      tr.life -= delta;
      if (tr.life <= 0) {
        this.scene.remove(tr.mesh);
        this.bulletTracers.splice(i, 1);
      }
    }
  }

  onEnemyEliminated(bot, isHeadshot) {
    window.uiManager.showNotification(`Eliminated ${bot.name} ${isHeadshot ? '[HEADSHOT!]' : ''} (+100 XP)`);
    window.missionManager.updateObjective('kills', 1);
    this.checkMissionStatus();
  }

  checkMissionStatus() {
    if (window.missionManager.isMissionComplete()) {
      const mission = window.missionManager.currentMission;
      window.saveSystem.addReward(mission.rewards.xp, mission.rewards.coins, 1);
      window.saveSystem.unlockLevel(this.currentLevel + 1);
      window.saveSystem.completeMission(mission.id);

      setTimeout(() => {
        window.audio.playPowerActivation();
        window.uiManager.showMissionComplete(mission, mission.rewards.xp, mission.rewards.coins);
      }, 700);
    }
  }

  onPlayerDied(attackerName) {
    setTimeout(() => {
      window.uiManager.showGameOver(attackerName);
    }, 800);
  }
}

window.addEventListener('DOMContentLoaded', () => {
  window.gameInstance = new BattleZoneGame();
});
