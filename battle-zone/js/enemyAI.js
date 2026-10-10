// Dynamic Location-Based AI Enemy Bot System
// Controls patrol, detection, line-of-sight, cover seeking, aiming, shooting, and loot drops

class EnemyBot {
  constructor(id, name, locationType, spawnPos, scene, map) {
    this.id = id;
    this.name = name;
    this.locationType = locationType; // 'town', 'warehouse', 'military', 'forest'
    this.spawnPos = (spawnPos && typeof spawnPos.clone === 'function')
      ? spawnPos.clone()
      : new THREE.Vector3(spawnPos ? spawnPos.x : 0, spawnPos ? (spawnPos.y || 0) : 0, spawnPos ? spawnPos.z : 0);
    this.scene = scene;
    this.map = map;

    // 3D Soldier Model
    this.character = ModelFactory.createSoldier(false); // Enemy red camo
    this.scene.add(this.character.root);
    this.position = this.spawnPos.clone();
    this.character.root.position.copy(this.position);

    // AI Stats based on location tier
    if (locationType === 'military') {
      this.health = 130;
      this.maxHealth = 130;
      this.weapon = WEAPON_REGISTRY.rifle;
      this.damage = 22;
      this.accuracy = 0.65;
      this.detectionRange = 45;
      this.attackRange = 35;
    } else if (locationType === 'warehouse') {
      this.health = 100;
      this.maxHealth = 100;
      this.weapon = WEAPON_REGISTRY.shotgun;
      this.damage = 18;
      this.accuracy = 0.55;
      this.detectionRange = 35;
      this.attackRange = 25;
    } else {
      // Town & Forest patrols
      this.health = 85;
      this.maxHealth = 85;
      this.weapon = WEAPON_REGISTRY.rifle;
      this.damage = 14;
      this.accuracy = 0.45;
      this.detectionRange = 32;
      this.attackRange = 25;
    }

    // State Machine: 'PATROL', 'INVESTIGATE', 'CHASE', 'ATTACK', 'COVER', 'DEAD'
    this.state = 'PATROL';
    this.isDead = false;
    this.patrolTarget = this.getRandomPatrolPoint();
    this.patrolTimer = 0;
    this.lastShotTime = 0;
    this.shootInterval = 1.2 + Math.random() * 0.8;
    this.animTime = Math.random() * 10;
  }

  getRandomPatrolPoint() {
    const range = this.locationType === 'warehouse' ? 12 : 22;
    return new THREE.Vector3(
      this.spawnPos.x + (Math.random() - 0.5) * range * 2,
      0,
      this.spawnPos.z + (Math.random() - 0.5) * range * 2
    );
  }

  update(delta, playerPos, playerDead) {
    if (this.isDead) return;

    this.animTime += delta * 6;
    const distToPlayer = this.position.distanceTo(playerPos);

    // AI State Transitions
    if (!playerDead && distToPlayer <= this.detectionRange) {
      if (this.hasLineOfSight(playerPos)) {
        if (distToPlayer <= this.attackRange) {
          this.state = 'ATTACK';
        } else {
          this.state = 'CHASE';
        }
      } else {
        this.state = 'INVESTIGATE';
      }
    } else {
      this.state = 'PATROL';
    }

    // Execute state behavior
    switch (this.state) {
      case 'PATROL':
        this.updatePatrol(delta);
        break;
      case 'INVESTIGATE':
      case 'CHASE':
        this.updateChase(delta, playerPos);
        break;
      case 'ATTACK':
        this.updateAttack(delta, playerPos);
        break;
    }

    this.character.root.position.copy(this.position);
  }

  hasLineOfSight(targetPos) {
    // Ray test against map colliders
    const from = this.position.clone().add(new THREE.Vector3(0, 1.4, 0));
    const to = targetPos.clone().add(new THREE.Vector3(0, 1.4, 0));
    const dist = from.distanceTo(to);
    const ray = new THREE.Ray(from, to.clone().sub(from).normalize());

    for (let i = 0; i < this.map.colliders.length; i++) {
      const box = this.map.colliders[i];
      const intersect = ray.intersectBox(box, new THREE.Vector3());
      if (intersect) {
        const hitDist = from.distanceTo(intersect);
        if (hitDist > 0.8 && hitDist < dist - 0.8) {
          return false; // Obstructed by wall or building
        }
      }
    }
    return true;
  }

  updatePatrol(delta) {
    this.patrolTimer += delta;
    const distToTarget = this.position.distanceTo(this.patrolTarget);

    if (distToTarget < 1.5 || this.patrolTimer > 8) {
      this.patrolTarget = this.getRandomPatrolPoint();
      this.patrolTimer = 0;
    } else {
      const dir = this.patrolTarget.clone().sub(this.position).normalize();
      const move = dir.multiplyScalar(2.2 * delta);
      const nextPos = this.position.clone().add(move);

      if (!this.map.checkCollision(nextPos, 0.45)) {
        this.position.copy(nextPos);
        this.character.root.rotation.y = Math.atan2(dir.x, dir.z);
      } else {
        this.patrolTarget = this.getRandomPatrolPoint();
      }
    }

    // Idle/Walk limb swing
    const swing = Math.sin(this.animTime);
    this.character.leftLegPivot.rotation.x = swing * 0.4;
    this.character.rightLegPivot.rotation.x = -swing * 0.4;
  }

  updateChase(delta, playerPos) {
    const dir = playerPos.clone().sub(this.position).normalize();
    const move = dir.multiplyScalar(3.8 * delta);
    const nextPos = this.position.clone().add(move);

    if (!this.map.checkCollision(nextPos, 0.45)) {
      this.position.copy(nextPos);
    } else {
      // Slide around obstacle
      const testX = this.position.clone();
      testX.x += move.x;
      if (!this.map.checkCollision(testX, 0.45)) {
        this.position.x = testX.x;
      } else {
        const testZ = this.position.clone();
        testZ.z += move.z;
        if (!this.map.checkCollision(testZ, 0.45)) {
          this.position.z = testZ.z;
        }
      }
    }
    this.character.root.rotation.y = Math.atan2(dir.x, dir.z);

    const swing = Math.sin(this.animTime * 1.5);
    this.character.leftLegPivot.rotation.x = swing * 0.6;
    this.character.rightLegPivot.rotation.x = -swing * 0.6;
  }

  updateAttack(delta, playerPos) {
    // Face player
    const dir = playerPos.clone().sub(this.position).normalize();
    this.character.root.rotation.y = Math.atan2(dir.x, dir.z);

    // Aim rifle at player
    this.character.rightArmPivot.rotation.x = -Math.PI / 2;

    const now = performance.now() / 1000;
    if (now - this.lastShotTime >= this.shootInterval) {
      this.lastShotTime = now;
      this.shootAtPlayer(playerPos);
    }
  }

  shootAtPlayer(playerPos) {
    window.audio.playGunshot(this.weapon.id);

    // Hit chance based on bot accuracy
    const hitRoll = Math.random();
    if (hitRoll < this.accuracy) {
      if (window.gameInstance && window.gameInstance.player) {
        window.gameInstance.player.takeDamage(this.damage, this.name);
      }
    }

    // Visual muzzle flash tracer toward player
    if (window.gameInstance) {
      const from = this.position.clone().add(new THREE.Vector3(0, 1.4, 0));
      const to = playerPos.clone().add(new THREE.Vector3(
        (Math.random() - 0.5) * 0.8,
        1.2 + (Math.random() - 0.5) * 0.6,
        (Math.random() - 0.5) * 0.8
      ));
      window.gameInstance.spawnBulletTracer(from, to, 0xff5252);
    }
  }

  takeDamage(amount, isHeadshot = false) {
    if (this.isDead) return;

    const finalDmg = isHeadshot ? amount * 1.8 : amount;
    this.health -= finalDmg;

    // Visual flash hit effect (tint red momentarily)
    this.character.torso.material.color.setHex(0xffffff);
    setTimeout(() => {
      if (!this.isDead) {
        this.character.torso.material.color.setHex(0x78281f);
      }
    }, 80);

    if (this.health <= 0) {
      this.die(isHeadshot);
    }
  }

  die(isHeadshot) {
    this.isDead = true;
    window.audio.playHitMarker(true);

    // Death ragdoll fall
    this.character.root.rotation.x = -Math.PI / 2;
    this.character.root.position.y = 0.2;

    // Spawn collectible 3D loot drop
    if (this.map) {
      const dropTypes = ['ammo_556', 'medkit', 'energy_drink'];
      const chosenType = dropTypes[Math.floor(Math.random() * dropTypes.length)];
      this.map.addLoot(this.position.x, 0.4, this.position.z, chosenType);
    }

    // Notify game engine
    if (window.gameInstance) {
      window.gameInstance.onEnemyEliminated(this, isHeadshot);
    }
  }
}

class EnemyManager {
  constructor(scene, map) {
    this.scene = scene;
    this.map = map;
    this.bots = [];
    this.maxActiveBots = 10;
  }

  spawnMissionEnemies(missionConfig) {
    // Clear old bots
    this.bots.forEach(b => {
      this.scene.remove(b.character.root);
    });
    this.bots = [];

    const spawns = missionConfig.enemySpawns || [
      { name: 'Scout_Alpha', loc: 'town', pos: new THREE.Vector3(-45, 0, -42) },
      { name: 'Scout_Bravo', loc: 'town', pos: new THREE.Vector3(-32, 0, -50) },
      { name: 'Guard_Viper', loc: 'warehouse', pos: new THREE.Vector3(52, 0, 48) },
      { name: 'Guard_Titan', loc: 'warehouse', pos: new THREE.Vector3(45, 0, 55) },
      { name: 'Garrison_Rex', loc: 'military', pos: new THREE.Vector3(65, 0, -50) }
    ];

    spawns.forEach((s, idx) => {
      const bot = new EnemyBot(idx + 1, s.name, s.loc, s.pos, this.scene, this.map);
      this.bots.push(bot);
    });
  }

  update(delta, playerPos, playerDead) {
    this.bots.forEach(b => {
      b.update(delta, playerPos, playerDead);
    });
  }

  getActiveBotCount() {
    return this.bots.filter(b => !b.isDead).length;
  }
}

window.EnemyManager = EnemyManager;
window.EnemyBot = EnemyBot;
