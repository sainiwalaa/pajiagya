// Third-Person Player Controller & State Machine
// Handles 3D movement, third-person camera follow, animations, combat, powers, vehicle driving

class PlayerController {
  constructor(scene, camera, map) {
    this.scene = scene;
    this.camera = camera;
    this.map = map;

    // Character 3D Model
    this.character = ModelFactory.createSoldier(true);
    this.scene.add(this.character.root);

    // Position & Transform
    this.position = new THREE.Vector3(0, 0, 15);
    this.character.root.position.copy(this.position);
    this.rotationY = 0;
    this.velocity = new THREE.Vector3();
    this.isGrounded = true;

    // Third-person camera settings
    this.cameraDistance = 4.2;
    this.cameraHeight = 2.1;
    this.cameraOffsetSide = 0.55; // Over-the-shoulder right offset
    this.cameraPitch = 0.15; // Vertical look angle
    this.cameraYaw = 0; // Horizontal look angle
    this.isAiming = false;

    // Combat Stats & State
    this.health = 100;
    this.maxHealth = 100;
    this.armor = 100;
    this.maxArmor = 100;
    this.isDead = false;
    this.isCrouched = false;
    this.isSprinting = false;

    // Weapons & Ammo Inventory
    this.inventory = {
      primary: 'rifle',
      secondary: 'pistol',
      ammo556: 120,
      ammo762: 90,
      ammoSniper: 15,
      ammoShotgun: 24,
      ammo9mm: 60,
      medkits: 3,
      energyDrinks: 2,
      shieldEnergy: 2
    };

    this.currentSlot = 'primary';
    this.equippedWeaponId = 'rifle';
    this.equippedWeapon = WEAPON_REGISTRY.rifle;
    this.currentAmmo = this.equippedWeapon.magSize;
    this.isReloading = false;
    this.reloadTimer = 0;
    this.lastShotTime = 0;

    // Special Powers
    this.powers = {
      speedBoost: { active: false, timer: 0, cooldown: 0, duration: 6, maxCooldown: 18 },
      energyShield: { active: false, timer: 0, cooldown: 0, duration: 8, maxCooldown: 25 },
      enemyScanner: { active: false, timer: 0, cooldown: 0, duration: 5, maxCooldown: 20 }
    };

    // Vehicle Driving Mode
    this.inVehicle = null; // Reference to vehicle if driving
    this.vehicleSpeed = 0;
    this.vehicleSteerAngle = 0;

    // Animation Timers
    this.animTime = 0;
    this.recoilPitch = 0;
  }

  update(delta, input) {
    if (this.isDead) {
      this.updateDeathAnimation(delta);
      return;
    }

    this.updatePowers(delta);

    if (this.inVehicle) {
      this.updateDriving(delta, input);
    } else {
      this.updateOnFootMovement(delta, input);
      this.updateOnFootAnimations(delta, input);
    }

    this.updateCombat(delta, input);
    this.updateThirdPersonCamera(delta);
  }

  updateOnFootMovement(delta, input) {
    // Look rotation from input (mouse or touch drag)
    const sens = (window.saveSystem ? window.saveSystem.data.settings.sensitivity : 1.0) * 0.0028;
    this.cameraYaw -= input.lookDeltaX * sens;
    this.cameraPitch = Math.max(-0.6, Math.min(0.7, this.cameraPitch - input.lookDeltaY * sens));
    input.lookDeltaX = 0;
    input.lookDeltaY = 0;

    // Character faces camera forward aim direction (+Z front model -> cameraYaw + PI)
    this.rotationY = this.cameraYaw + Math.PI;
    this.character.root.rotation.y = this.rotationY;

    // Crouch stance toggle
    if (input.crouch) {
      this.isCrouched = !this.isCrouched;
      input.crouch = false;
    }

    // Sprint state (button toggle or pushing joystick full forward)
    this.isSprinting = !this.isCrouched && (!!input.sprint || !!this.sprintActive || input.moveForward > 0.88);

    // Movement speed calculations
    let baseSpeed = 5.2;
    if (this.isCrouched) baseSpeed = 2.6;
    else if (this.isAiming) baseSpeed = 3.2;
    else if (this.isSprinting) baseSpeed = 9.2;

    // Speed boost power
    if (this.powers.speedBoost.active) {
      baseSpeed *= 1.5;
    }

    // Direction vectors relative to camera orientation
    const forward = new THREE.Vector3(-Math.sin(this.cameraYaw), 0, -Math.cos(this.cameraYaw));
    const right = new THREE.Vector3(Math.cos(this.cameraYaw), 0, -Math.sin(this.cameraYaw));

    const moveDir = new THREE.Vector3();
    if (input.moveForward) moveDir.add(forward.clone().multiplyScalar(input.moveForward));
    if (input.moveRight) moveDir.add(right.clone().multiplyScalar(input.moveRight));

    if (moveDir.lengthSq() > 0.01) {
      moveDir.normalize();
      const moveStep = moveDir.multiplyScalar(baseSpeed * delta);
      const nextPos = this.position.clone().add(moveStep);

      // Check map collision with sliding along wall
      if (!this.map.checkCollision(nextPos, 0.45)) {
        this.position.copy(nextPos);
      } else {
        let moved = false;
        const testX = this.position.clone();
        testX.x += moveStep.x;
        if (!this.map.checkCollision(testX, 0.45)) {
          this.position.x = testX.x;
          moved = true;
        }
        const testZ = this.position.clone();
        testZ.z += moveStep.z;
        if (!this.map.checkCollision(testZ, 0.45)) {
          this.position.z = testZ.z;
          moved = true;
        }
        // Anti-stick recovery: if character touches an edge, nudge forward to prevent freezing
        if (!moved && this.map.checkCollision(this.position, 0.45)) {
          this.position.add(moveStep.clone().multiplyScalar(0.4));
        }
      }
    }

    // Jump physics
    if (input.jump && this.isGrounded && !this.isCrouched) {
      this.velocity.y = 6.4;
      this.isGrounded = false;
      input.jump = false;
    }

    if (!this.isGrounded) {
      this.velocity.y -= 18 * delta; // Gravity
      this.position.y += this.velocity.y * delta;
      if (this.position.y <= 0) {
        this.position.y = 0;
        this.velocity.y = 0;
        this.isGrounded = true;
      }
    }

    this.character.root.position.copy(this.position);
  }

  updateOnFootAnimations(delta, input) {
    const isMoving = Math.abs(input.moveForward) > 0.08 || Math.abs(input.moveRight) > 0.08;
    const animSpeed = this.isSprinting ? 14 : (this.isCrouched ? 5.5 : 7.8);
    this.animTime += delta * animSpeed;

    const c = this.character;

    if (!this.isGrounded) {
      // In-air jump animation
      c.leftLegPivot.rotation.x = -0.35;
      c.rightLegPivot.rotation.x = -0.2;
      c.leftArmPivot.rotation.x = -0.4;
      c.rightArmPivot.rotation.x = -Math.PI / 3;
      c.hips.position.y = 0.95;
    } else if (isMoving) {
      // Natural running / walking limb swing
      const swing = Math.sin(this.animTime);
      const amp = this.isSprinting ? 0.8 : (this.isCrouched ? 0.4 : 0.6);
      c.leftLegPivot.rotation.x = swing * amp;
      c.rightLegPivot.rotation.x = -swing * amp;
      c.leftArmPivot.rotation.x = -swing * amp * 0.7;

      // Weapon arm follows aiming or runs
      if (this.isAiming) {
        c.rightArmPivot.rotation.x = -Math.PI / 2 - this.cameraPitch;
        c.leftArmPivot.rotation.x = -Math.PI / 2.3;
      } else {
        c.rightArmPivot.rotation.x = -Math.PI / 3 + Math.sin(this.animTime * 0.5) * 0.1;
      }

      // Torso bobbing
      c.hips.position.y = (this.isCrouched ? 0.55 : 0.9) + Math.abs(Math.sin(this.animTime * 2)) * (this.isSprinting ? 0.08 : 0.04);
    } else {
      // Idle breathing animation
      c.leftLegPivot.rotation.x = 0;
      c.rightLegPivot.rotation.x = 0;
      c.leftArmPivot.rotation.x = Math.sin(this.animTime * 0.3) * 0.05;

      if (this.isAiming) {
        c.rightArmPivot.rotation.x = -Math.PI / 2 - this.cameraPitch;
        c.leftArmPivot.rotation.x = -Math.PI / 2.3;
      } else {
        c.rightArmPivot.rotation.x = -Math.PI / 3.2;
      }

      c.hips.position.y = (this.isCrouched ? 0.55 : 0.9) + Math.sin(this.animTime * 0.4) * 0.02;
    }
  }

  updateDriving(delta, input) {
    const v = this.inVehicle;
    if (!v) return;

    // Driving physics constants
    const maxSpeed = 24.0;
    const accel = 18.0;
    const footBrake = 26.0;

    // Spacebar is handbrake
    const isHandbrake = !!input.jump;

    if (isHandbrake) {
      this.vehicleSpeed *= Math.pow(0.12, delta);
      window.audio.playVehicleEngine(false);
    } else if (input.moveForward > 0.1) {
      // Accelerate forward
      if (this.vehicleSpeed < 0) {
        // Was in reverse, brake first
        this.vehicleSpeed = Math.min(0, this.vehicleSpeed + footBrake * delta);
      } else {
        this.vehicleSpeed = Math.min(maxSpeed, this.vehicleSpeed + accel * delta * input.moveForward);
      }
      window.audio.playVehicleEngine(true);
    } else if (input.moveForward < -0.1) {
      // S key: footbrake if moving forward, else reverse
      if (this.vehicleSpeed > 1.0) {
        this.vehicleSpeed = Math.max(0, this.vehicleSpeed - footBrake * delta * Math.abs(input.moveForward));
      } else {
        this.vehicleSpeed = Math.max(-10.0, this.vehicleSpeed - accel * 0.7 * delta);
      }
      window.audio.playVehicleEngine(false);
    } else {
      this.vehicleSpeed *= Math.pow(0.72, delta); // Coasting friction
      window.audio.playVehicleEngine(false);
    }

    // Steering
    if (Math.abs(this.vehicleSpeed) > 0.3) {
      const steerSpeed = 2.4;
      const reverseMultiplier = this.vehicleSpeed < 0 ? -1 : 1;
      if (input.moveRight > 0.1) v.rotation.y -= steerSpeed * delta * reverseMultiplier;
      if (input.moveRight < -0.1) v.rotation.y += steerSpeed * delta * reverseMultiplier;
    }

    // Turn front wheels and spin all wheels
    const steerVisual = (input.moveRight || 0) * 0.35;
    if (v.model && v.model.wheels) {
      v.model.wheels.forEach(w => {
        if (w.isFront) w.pivot.rotation.y = -steerVisual;
        w.mesh.rotation.x += this.vehicleSpeed * delta * 2.5; // Spin wheels
      });
    }

    // Move vehicle forward along heading
    const forward = new THREE.Vector3(Math.sin(v.rotation.y), 0, Math.cos(v.rotation.y));
    const step = forward.multiplyScalar(this.vehicleSpeed * delta);
    const nextPos = v.position.clone().add(step);

    if (!this.map.checkCollision(nextPos, 1.4)) {
      v.position.copy(nextPos);
    } else {
      this.vehicleSpeed = -this.vehicleSpeed * 0.35; // Bounce back on impact
      window.audio.playExplosion();
    }

    // Roadkill against enemy bots
    if (Math.abs(this.vehicleSpeed) > 5.0 && window.gameInstance && window.gameInstance.enemyManager) {
      window.gameInstance.enemyManager.bots.forEach(b => {
        if (!b.isDead && v.position.distanceTo(b.position) < 2.6) {
          b.takeDamage(150, false);
          window.audio.playExplosion();
          if (window.uiManager) window.uiManager.showNotification('💥 Vehicle Roadkill Elimination!');
        }
      });
    }

    // Compute driver seat world position inside buggy
    const seatOffset = new THREE.Vector3(-0.4, 0.92, -0.2);
    seatOffset.applyAxisAngle(new THREE.Vector3(0, 1, 0), v.rotation.y);
    const driverWorldPos = v.position.clone().add(seatOffset);

    // Keep player character VISIBLE in driver seat, posed driving
    this.position.copy(v.position);
    this.character.root.position.copy(driverWorldPos);
    this.character.root.rotation.y = v.rotation.y;
    this.character.root.visible = true;

    // Seated driving posture
    const c = this.character;
    c.hips.position.y = 0.52;
    c.leftLegPivot.rotation.x = -Math.PI / 2.3;
    c.rightLegPivot.rotation.x = -Math.PI / 2.3;
    c.leftArmPivot.rotation.x = -Math.PI / 2.6;
    c.rightArmPivot.rotation.x = -Math.PI / 2.6;

    // Camera tracks vehicle orientation smoothly
    this.cameraYaw = v.rotation.y + Math.PI;
  }

  enterVehicle(v) {
    this.inVehicle = v;
    v.isOccupied = true;
    this.vehicleSpeed = 0;
    this.character.root.visible = true; // Visibly seated in driver seat
    window.audio.playPickup();
    if (window.uiManager) {
      window.uiManager.showNotification('🏎️ DRIVING VEHICLE [WASD / Joystick to Drive • Space / Brake to Stop • E to Exit]');
    }
  }

  exitVehicle() {
    if (!this.inVehicle) return;
    const v = this.inVehicle;
    v.isOccupied = false;

    // Test safe exit position (left side, right side, or rear)
    const offsets = [
      new THREE.Vector3(-2.2, 0, 0),
      new THREE.Vector3(2.2, 0, 0),
      new THREE.Vector3(0, 0, -2.4)
    ];
    let exitPos = null;
    for (const off of offsets) {
      off.applyAxisAngle(new THREE.Vector3(0, 1, 0), v.rotation.y);
      const test = v.position.clone().add(off);
      if (!this.map.checkCollision(test, 0.45)) {
        exitPos = test;
        break;
      }
    }
    if (!exitPos) {
      exitPos = v.position.clone().add(new THREE.Vector3(-2.2, 0, 0));
    }

    this.position.copy(exitPos);
    this.character.root.position.copy(this.position);
    this.character.root.visible = true;

    // Reset standing limb rotations
    const c = this.character;
    c.hips.position.y = 0.9;
    c.leftLegPivot.rotation.x = 0;
    c.rightLegPivot.rotation.x = 0;
    c.leftArmPivot.rotation.x = 0;
    c.rightArmPivot.rotation.x = -Math.PI / 3.2;

    this.inVehicle = null;
    this.vehicleSpeed = 0;
    window.audio.playPickup();
    if (window.uiManager) {
      window.uiManager.showNotification('Exited Vehicle');
    }
  }

  swapWeapons() {
    this.switchWeapon(this.currentSlot === 'primary' ? 'secondary' : 'primary');
  }

  updateCombat(delta, input) {
    // Reload timer
    if (this.isReloading) {
      this.reloadTimer -= delta;
      if (this.reloadTimer <= 0) {
        this.isReloading = false;
        this.currentAmmo = this.equippedWeapon.magSize;
        window.audio.playReload();
      }
    }

    // Recoil recovery
    this.recoilPitch = Math.max(0, this.recoilPitch - delta * 0.3);

    // Aim toggle
    this.isAiming = !!input.aim;

    // Weapon firing
    const now = (typeof performance !== 'undefined' && performance.now) ? (performance.now() / 1000) : (Date.now() / 1000);
    if (input.fire && !this.isReloading && !this.inVehicle) {
      if (now - this.lastShotTime >= this.equippedWeapon.fireRate) {
        if (this.currentAmmo > 0) {
          this.shoot();
          this.lastShotTime = now;
        } else {
          this.reload();
        }
      }
    }

    // Manual reload
    if (input.reload && !this.isReloading && this.currentAmmo < this.equippedWeapon.magSize) {
      this.reload();
      input.reload = false;
    }
  }

  shoot() {
    this.currentAmmo--;
    this.recoilPitch = Math.min(0.08, this.recoilPitch + this.equippedWeapon.recoil);
    this.cameraPitch += this.equippedWeapon.recoil * 0.5;

    window.audio.playGunshot(this.equippedWeapon.id);

    // Raycast forward from camera center
    const raycaster = new THREE.Raycaster();
    const screenCenter = new THREE.Vector2(0, 0);

    // Apply weapon spread
    const spreadVal = this.isAiming ? this.equippedWeapon.spread * 0.35 : this.equippedWeapon.spread;
    const spreadX = (Math.random() - 0.5) * spreadVal;
    const spreadY = (Math.random() - 0.5) * spreadVal;

    raycaster.setFromCamera(new THREE.Vector2(spreadX, spreadY), this.camera);
    raycaster.far = this.equippedWeapon.range;

    // Notify Game engine of fired projectile
    if (window.gameInstance) {
      window.gameInstance.handlePlayerShot(raycaster, this.equippedWeapon);
    }
  }

  reload() {
    if (this.isReloading) return;
    this.isReloading = true;
    this.reloadTimer = this.equippedWeapon.reloadDuration;
    window.audio.playReload();
    if (window.uiManager) {
      window.uiManager.showNotification('🔄 RELOADING...');
    }
    if (window.audio && typeof window.audio.speakAnnouncement === 'function') {
      window.audio.speakAnnouncement("Reloading!", 'military');
    }
  }

  switchWeapon(slot) {
    if (this.isReloading || this.currentSlot === slot) return;
    this.currentSlot = slot;
    this.equippedWeaponId = slot === 'primary' ? this.inventory.primary : this.inventory.secondary;
    this.equippedWeapon = WEAPON_REGISTRY[this.equippedWeaponId] || WEAPON_REGISTRY.rifle;
    this.currentAmmo = this.equippedWeapon.magSize;

    // Update 3D mesh
    this.character.weaponPivot.remove(this.character.currentWeapon);
    const newMesh = ModelFactory.createWeapon(this.equippedWeaponId);
    this.character.weaponPivot.add(newMesh);
    this.character.currentWeapon = newMesh;

    window.audio.playReload();
  }

  takeDamage(amount, attackerName = 'Enemy') {
    if (this.isDead) return;

    // Energy Shield power mitigation
    if (this.powers.energyShield.active) {
      amount *= 0.5; // 50% damage reduction
    }

    // Armor absorption (takes 60% of damage)
    if (this.armor > 0) {
      const absorbed = Math.min(this.armor, amount * 0.6);
      this.armor -= absorbed;
      amount -= absorbed;
    }

    this.health = Math.max(0, this.health - amount);
    window.audio.playHitMarker(false);

    if (window.uiManager) {
      window.uiManager.showNotification('⚠️ TAKING FIRE FROM ' + attackerName.toUpperCase() + '!');
    }

    if (this.health <= 0) {
      this.die(attackerName);
    }
  }

  heal() {
    if (this.health >= this.maxHealth || this.inventory.medkits <= 0) return;
    this.inventory.medkits--;
    this.health = Math.min(this.maxHealth, this.health + 75);
    window.audio.playPickup();
  }

  useEnergyDrink() {
    if (this.inventory.energyDrinks <= 0) return;
    this.inventory.energyDrinks--;
    this.armor = Math.min(this.maxArmor, this.armor + 50);
    this.health = Math.min(this.maxHealth, this.health + 25);
    window.audio.playPickup();
  }

  activatePower(powerType) {
    const p = this.powers[powerType];
    if (!p || p.active || p.cooldown > 0) return;

    p.active = true;
    p.timer = p.duration;
    p.cooldown = p.maxCooldown;
    window.audio.playPowerActivation();
  }

  updatePowers(delta) {
    for (const key in this.powers) {
      const p = this.powers[key];
      if (p.active) {
        p.timer -= delta;
        if (p.timer <= 0) {
          p.active = false;
        }
      }
      if (p.cooldown > 0) {
        p.cooldown -= delta;
      }
    }
  }

  die(attackerName) {
    this.isDead = true;
    window.audio.playExplosion();
    if (window.gameInstance) {
      window.gameInstance.onPlayerDied(attackerName);
    }
  }

  updateDeathAnimation(delta) {
    // Fall backward
    if (this.character.root.rotation.x > -Math.PI / 2) {
      this.character.root.rotation.x -= 2.5 * delta;
    }
  }

  updateThirdPersonCamera(delta) {
    // Over-the-shoulder third-person camera positioning
    const targetDist = this.isAiming ? 2.2 : (this.inVehicle ? 6.5 : this.cameraDistance);
    const targetHeight = this.inVehicle ? 3.2 : this.cameraHeight;
    const targetSide = this.isAiming ? 0.7 : (this.inVehicle ? 0.0 : this.cameraOffsetSide);

    // Compute camera position relative to player
    const pitch = this.cameraPitch + this.recoilPitch;
    const yaw = this.cameraYaw;

    // Aim forward direction unit vector
    const aimDir = new THREE.Vector3(
      -Math.sin(yaw) * Math.cos(pitch),
      Math.sin(pitch),
      -Math.cos(yaw) * Math.cos(pitch)
    );

    // Camera right vector for shoulder offset
    const rightDir = new THREE.Vector3(
      Math.cos(yaw),
      0,
      -Math.sin(yaw)
    );

    // Head position
    const headPos = this.position.clone().add(new THREE.Vector3(0, targetHeight, 0));

    // Place camera behind player head along -aimDir and offset to right shoulder
    const desiredCamPos = headPos.clone()
      .sub(aimDir.clone().multiplyScalar(targetDist))
      .add(rightDir.clone().multiplyScalar(targetSide));

    this.camera.position.lerp(desiredCamPos, 0.35);

    // Look target far ahead along the aim direction so the crosshair points where bullets hit!
    const lookTarget = headPos.clone().add(aimDir.clone().multiplyScalar(60.0));
    this.camera.lookAt(lookTarget);
  }
}

window.PlayerController = PlayerController;
