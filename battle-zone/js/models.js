// Procedural 3D Mesh & Character Factory for Three.js
// Builds complete animated 3D soldiers, weapons, drivable buggy vehicles, and buildings

const ModelFactory = {
  materials: {
    // Shared materials for high performance
    playerBody: new THREE.MeshLambertMaterial({ color: 0x283747 }), // Tactical dark navy
    playerArmor: new THREE.MeshLambertMaterial({ color: 0x1c2833 }),
    playerVisor: new THREE.MeshLambertMaterial({ color: 0xffb800 }), // Golden visor
    botBody: new THREE.MeshLambertMaterial({ color: 0x78281f }),    // Red-tinted combat camo
    botArmor: new THREE.MeshLambertMaterial({ color: 0x4a235a }),
    botVisor: new THREE.MeshLambertMaterial({ color: 0xe74c3c }),
    skin: new THREE.MeshLambertMaterial({ color: 0xd4a373 }),
    weaponMetal: new THREE.MeshLambertMaterial({ color: 0x17202a }),
    weaponAccent: new THREE.MeshLambertMaterial({ color: 0x5dade2 }),
    vehicleBody: new THREE.MeshLambertMaterial({ color: 0xd35400 }), // Military orange buggy
    vehicleRollCage: new THREE.MeshLambertMaterial({ color: 0x212f3d }),
    vehicleWheel: new THREE.MeshLambertMaterial({ color: 0x1b2631 }),
    buildingWall: new THREE.MeshLambertMaterial({ color: 0x5d6d7e }),
    buildingRoof: new THREE.MeshLambertMaterial({ color: 0x34495e }),
    buildingFloor: new THREE.MeshLambertMaterial({ color: 0x2c3e50 }),
    woodCrate: new THREE.MeshLambertMaterial({ color: 0x873600 }),
    airdropRed: new THREE.MeshLambertMaterial({ color: 0xc0392b }),
    airdropBlue: new THREE.MeshLambertMaterial({ color: 0x2980b9 }),
    sandbag: new THREE.MeshLambertMaterial({ color: 0xb7950b }),
    treeTrunk: new THREE.MeshLambertMaterial({ color: 0x56382d }),
    treeLeaves: new THREE.MeshLambertMaterial({ color: 0x1e8449 }),
    rock: new THREE.MeshLambertMaterial({ color: 0x566573 })
  },

  createSoldier(isPlayer = true) {
    const root = new THREE.Group();
    const bodyMat = (isPlayer ? this.materials.playerBody : this.materials.botBody).clone();
    const armorMat = (isPlayer ? this.materials.playerArmor : this.materials.botArmor).clone();
    const visorMat = (isPlayer ? this.materials.playerVisor : this.materials.botVisor).clone();

    // Pelvis / Hips (Root pivot)
    const hips = new THREE.Group();
    hips.position.y = 0.9;
    root.add(hips);

    // Torso / Spine
    const torso = new THREE.Mesh(new THREE.BoxGeometry(0.5, 0.55, 0.28), bodyMat);
    torso.position.y = 0.28;
    hips.add(torso);

    // Chest Tactical Armor Vest
    const vest = new THREE.Mesh(new THREE.BoxGeometry(0.54, 0.45, 0.34), armorMat);
    vest.position.set(0, 0.05, 0);
    torso.add(vest);

    // Neck & Head Pivot
    const headPivot = new THREE.Group();
    headPivot.position.y = 0.36;
    torso.add(headPivot);

    const head = new THREE.Mesh(new THREE.BoxGeometry(0.28, 0.3, 0.28), this.materials.skin);
    head.position.y = 0.15;
    headPivot.add(head);

    // Tactical Helmet (Level 3 style)
    const helmet = new THREE.Mesh(new THREE.BoxGeometry(0.32, 0.22, 0.32), armorMat);
    helmet.position.set(0, 0.08, 0.01);
    head.add(helmet);

    // Golden Visor
    const visor = new THREE.Mesh(new THREE.BoxGeometry(0.24, 0.08, 0.06), visorMat);
    visor.position.set(0, 0.02, 0.15);
    head.add(visor);

    // Left Arm (Shoulder pivot)
    const leftArmPivot = new THREE.Group();
    leftArmPivot.position.set(-0.34, 0.22, 0);
    torso.add(leftArmPivot);

    const leftArm = new THREE.Mesh(new THREE.BoxGeometry(0.16, 0.52, 0.16), bodyMat);
    leftArm.position.y = -0.22;
    leftArmPivot.add(leftArm);

    // Right Arm (Shoulder pivot holding gun)
    const rightArmPivot = new THREE.Group();
    rightArmPivot.position.set(0.34, 0.22, 0);
    torso.add(rightArmPivot);

    const rightArm = new THREE.Mesh(new THREE.BoxGeometry(0.16, 0.52, 0.16), bodyMat);
    rightArm.position.y = -0.22;
    rightArmPivot.add(rightArm);

    // Weapon Hand Pivot
    const weaponPivot = new THREE.Group();
    weaponPivot.position.set(0, -0.24, 0.15);
    rightArm.add(weaponPivot);

    // Attach initial weapon
    const initialWeapon = this.createWeapon('rifle');
    weaponPivot.add(initialWeapon);

    // Left Leg (Hip joint)
    const leftLegPivot = new THREE.Group();
    leftLegPivot.position.set(-0.16, 0, 0);
    hips.add(leftLegPivot);

    const leftLeg = new THREE.Mesh(new THREE.BoxGeometry(0.18, 0.85, 0.18), bodyMat);
    leftLeg.position.y = -0.42;
    leftLegPivot.add(leftLeg);

    // Left Boot
    const leftBoot = new THREE.Mesh(new THREE.BoxGeometry(0.2, 0.16, 0.28), armorMat);
    leftBoot.position.set(0, -0.38, 0.04);
    leftLeg.add(leftBoot);

    // Right Leg (Hip joint)
    const rightLegPivot = new THREE.Group();
    rightLegPivot.position.set(0.16, 0, 0);
    hips.add(rightLegPivot);

    const rightLeg = new THREE.Mesh(new THREE.BoxGeometry(0.18, 0.85, 0.18), bodyMat);
    rightLeg.position.y = -0.42;
    rightLegPivot.add(rightLeg);

    // Right Boot
    const rightBoot = new THREE.Mesh(new THREE.BoxGeometry(0.2, 0.16, 0.28), armorMat);
    rightBoot.position.set(0, -0.38, 0.04);
    rightLeg.add(rightBoot);

    // Muzzle Flash Point (for raycasting and shooting particle bursts)
    const muzzle = new THREE.Object3D();
    muzzle.position.set(0, 0, 0.7);
    weaponPivot.add(muzzle);

    return {
      root,
      hips,
      torso,
      headPivot,
      leftArmPivot,
      rightArmPivot,
      leftLegPivot,
      rightLegPivot,
      weaponPivot,
      muzzle,
      currentWeapon: initialWeapon
    };
  },

  createWeapon(weaponId) {
    const gun = new THREE.Group();
    gun.name = 'gun_' + weaponId;

    if (weaponId === 'sniper') {
      // Long AWM Sniper
      const body = new THREE.Mesh(new THREE.BoxGeometry(0.08, 0.12, 0.85), this.materials.weaponMetal);
      body.position.z = 0.25;
      gun.add(body);
      const barrel = new THREE.Mesh(new THREE.CylinderGeometry(0.02, 0.02, 0.55), this.materials.weaponMetal);
      barrel.rotation.x = Math.PI / 2;
      barrel.position.z = 0.85;
      gun.add(barrel);
      // Large Telescopic Scope
      const scope = new THREE.Mesh(new THREE.CylinderGeometry(0.035, 0.035, 0.35), this.materials.weaponAccent);
      scope.rotation.x = Math.PI / 2;
      scope.position.set(0, 0.1, 0.25);
      gun.add(scope);
      // Stock
      const stock = new THREE.Mesh(new THREE.BoxGeometry(0.07, 0.14, 0.3), this.materials.woodCrate);
      stock.position.z = -0.2;
      gun.add(stock);
    } else if (weaponId === 'shotgun') {
      // S12 Shotgun
      const body = new THREE.Mesh(new THREE.BoxGeometry(0.09, 0.14, 0.6), this.materials.weaponMetal);
      body.position.z = 0.2;
      gun.add(body);
      const barrel = new THREE.Mesh(new THREE.CylinderGeometry(0.035, 0.035, 0.4), this.materials.weaponMetal);
      barrel.rotation.x = Math.PI / 2;
      barrel.position.z = 0.6;
      gun.add(barrel);
      const pump = new THREE.Mesh(new THREE.BoxGeometry(0.1, 0.08, 0.2), this.materials.weaponAccent);
      pump.position.set(0, -0.04, 0.45);
      gun.add(pump);
    } else if (weaponId === 'pistol') {
      // P9 Handgun
      const slide = new THREE.Mesh(new THREE.BoxGeometry(0.06, 0.08, 0.25), this.materials.weaponMetal);
      slide.position.z = 0.08;
      gun.add(slide);
      const grip = new THREE.Mesh(new THREE.BoxGeometry(0.055, 0.14, 0.07), this.materials.woodCrate);
      grip.position.set(0, -0.08, -0.02);
      gun.add(grip);
    } else {
      // Default M4 Tactical Rifle
      const body = new THREE.Mesh(new THREE.BoxGeometry(0.08, 0.12, 0.65), this.materials.weaponMetal);
      body.position.z = 0.2;
      gun.add(body);
      const barrel = new THREE.Mesh(new THREE.CylinderGeometry(0.02, 0.02, 0.35), this.materials.weaponMetal);
      barrel.rotation.x = Math.PI / 2;
      barrel.position.z = 0.65;
      gun.add(barrel);
      // Curved Magazine
      const mag = new THREE.Mesh(new THREE.BoxGeometry(0.06, 0.18, 0.12), this.materials.weaponAccent);
      mag.position.set(0, -0.12, 0.22);
      mag.rotation.x = 0.2;
      gun.add(mag);
      // Red Dot Holo Sight
      const sight = new THREE.Mesh(new THREE.BoxGeometry(0.05, 0.06, 0.12), this.materials.weaponAccent);
      sight.position.set(0, 0.09, 0.18);
      gun.add(sight);
      // Stock
      const stock = new THREE.Mesh(new THREE.BoxGeometry(0.06, 0.12, 0.25), this.materials.weaponMetal);
      stock.position.z = -0.2;
      gun.add(stock);
    }

    return gun;
  },

  createVehicle() {
    // 3D Military Buggy with roll cage and 4 spinning wheels
    const group = new THREE.Group();
    group.name = 'vehicle_buggy';

    // Main Chassis
    const chassis = new THREE.Mesh(new THREE.BoxGeometry(2.0, 0.5, 3.8), this.materials.vehicleBody);
    chassis.position.y = 0.75;
    group.add(chassis);

    // Front Slanted Hood
    const hood = new THREE.Mesh(new THREE.BoxGeometry(1.6, 0.4, 1.2), this.materials.vehicleBody);
    hood.position.set(0, 0.85, 1.3);
    hood.rotation.x = -0.15;
    group.add(hood);

    // Roll Cage Frame
    const rollCage = new THREE.Group();
    const barGeo = new THREE.CylinderGeometry(0.05, 0.05, 1.4);
    // 4 Corner vertical posts
    const postOffsets = [
      [-0.85, 1.4, 0.6], [0.85, 1.4, 0.6],
      [-0.85, 1.4, -1.0], [0.85, 1.4, -1.0]
    ];
    postOffsets.forEach(([x, y, z]) => {
      const bar = new THREE.Mesh(barGeo, this.materials.vehicleRollCage);
      bar.position.set(x, y, z);
      rollCage.add(bar);
    });
    // Top roof crossbars
    const roofBarGeo = new THREE.CylinderGeometry(0.05, 0.05, 1.7);
    const roofBar1 = new THREE.Mesh(roofBarGeo, this.materials.vehicleRollCage);
    roofBar1.rotation.z = Math.PI / 2;
    roofBar1.position.set(0, 2.05, 0.6);
    rollCage.add(roofBar1);

    const roofBar2 = new THREE.Mesh(roofBarGeo, this.materials.vehicleRollCage);
    roofBar2.rotation.z = Math.PI / 2;
    roofBar2.position.set(0, 2.05, -1.0);
    rollCage.add(roofBar2);
    group.add(rollCage);

    // Seats & Steering Wheel
    const seatMat = new THREE.MeshLambertMaterial({ color: 0x17202a });
    const seat1 = new THREE.Mesh(new THREE.BoxGeometry(0.65, 0.6, 0.6), seatMat);
    seat1.position.set(-0.4, 1.1, -0.2);
    group.add(seat1);

    const seat2 = new THREE.Mesh(new THREE.BoxGeometry(0.65, 0.6, 0.6), seatMat);
    seat2.position.set(0.4, 1.1, -0.2);
    group.add(seat2);

    const steering = new THREE.Mesh(new THREE.TorusGeometry(0.18, 0.03, 8, 16), seatMat);
    steering.position.set(-0.4, 1.4, 0.4);
    steering.rotation.x = Math.PI / 4;
    group.add(steering);

    // Headlights
    const lightMat = new THREE.MeshBasicMaterial({ color: 0xfff9c4 });
    const hl1 = new THREE.Mesh(new THREE.CylinderGeometry(0.12, 0.12, 0.08), lightMat);
    hl1.rotation.x = Math.PI / 2;
    hl1.position.set(-0.6, 0.85, 1.95);
    group.add(hl1);

    const hl2 = new THREE.Mesh(new THREE.CylinderGeometry(0.12, 0.12, 0.08), lightMat);
    hl2.rotation.x = Math.PI / 2;
    hl2.position.set(0.6, 0.85, 1.95);
    group.add(hl2);

    // 4 Wheels
    const wheelGeo = new THREE.CylinderGeometry(0.48, 0.48, 0.36, 16);
    const wheels = [];
    const wheelCoords = [
      { name: 'frontLeft', x: -1.15, z: 1.2, isFront: true },
      { name: 'frontRight', x: 1.15, z: 1.2, isFront: true },
      { name: 'rearLeft', x: -1.15, z: -1.2, isFront: false },
      { name: 'rearRight', x: 1.15, z: -1.2, isFront: false }
    ];

    wheelCoords.forEach(wc => {
      const wheelPivot = new THREE.Group();
      wheelPivot.position.set(wc.x, 0.48, wc.z);

      const wheelMesh = new THREE.Mesh(wheelGeo, this.materials.vehicleWheel);
      wheelMesh.rotation.z = Math.PI / 2;
      wheelPivot.add(wheelMesh);

      // Hubcap
      const cap = new THREE.Mesh(new THREE.CylinderGeometry(0.2, 0.2, 0.38, 8), this.materials.vehicleRollCage);
      cap.rotation.z = Math.PI / 2;
      wheelPivot.add(cap);

      group.add(wheelPivot);
      wheels.push({ pivot: wheelPivot, mesh: wheelMesh, isFront: wc.isFront });
    });

    return {
      root: group,
      wheels,
      driverSeatPos: new THREE.Vector3(-0.4, 1.2, -0.2)
    };
  },

  createBuilding(type = 'house', width = 12, height = 7, depth = 14) {
    const group = new THREE.Group();
    group.name = 'building_' + type;

    // Floor
    const floor = new THREE.Mesh(new THREE.BoxGeometry(width, 0.3, depth), this.materials.buildingFloor);
    floor.position.y = 0.15;
    group.add(floor);

    // Wall thickness
    const wallThick = 0.4;
    const wallHeight = height - 1.5;

    // Back wall
    const backWall = new THREE.Mesh(new THREE.BoxGeometry(width, wallHeight, wallThick), this.materials.buildingWall);
    backWall.position.set(0, wallHeight / 2, -depth / 2 + wallThick / 2);
    group.add(backWall);

    // Left wall with window opening
    const leftWall1 = new THREE.Mesh(new THREE.BoxGeometry(wallThick, wallHeight, depth * 0.4), this.materials.buildingWall);
    leftWall1.position.set(-width / 2 + wallThick / 2, wallHeight / 2, -depth * 0.25);
    group.add(leftWall1);

    const leftWall2 = new THREE.Mesh(new THREE.BoxGeometry(wallThick, wallHeight, depth * 0.4), this.materials.buildingWall);
    leftWall2.position.set(-width / 2 + wallThick / 2, wallHeight / 2, depth * 0.25);
    group.add(leftWall2);

    // Right wall
    const rightWall = new THREE.Mesh(new THREE.BoxGeometry(wallThick, wallHeight, depth), this.materials.buildingWall);
    rightWall.position.set(width / 2 - wallThick / 2, wallHeight / 2, 0);
    group.add(rightWall);

    // Front wall with open doorway in center
    const doorWidth = 3.2;
    const frontSideWidth = (width - doorWidth) / 2;

    const frontLeft = new THREE.Mesh(new THREE.BoxGeometry(frontSideWidth, wallHeight, wallThick), this.materials.buildingWall);
    frontLeft.position.set(-width / 2 + frontSideWidth / 2, wallHeight / 2, depth / 2 - wallThick / 2);
    group.add(frontLeft);

    const frontRight = new THREE.Mesh(new THREE.BoxGeometry(frontSideWidth, wallHeight, wallThick), this.materials.buildingWall);
    frontRight.position.set(width / 2 - frontSideWidth / 2, wallHeight / 2, depth / 2 - wallThick / 2);
    group.add(frontRight);

    // Lintel above door
    const lintel = new THREE.Mesh(new THREE.BoxGeometry(doorWidth, wallHeight * 0.35, wallThick), this.materials.buildingWall);
    lintel.position.set(0, wallHeight * 0.825, depth / 2 - wallThick / 2);
    group.add(lintel);

    // Roof
    const roof = new THREE.Mesh(new THREE.BoxGeometry(width + 0.8, 0.4, depth + 0.8), this.materials.buildingRoof);
    roof.position.y = wallHeight + 0.2;
    group.add(roof);

    return {
      root: group,
      bounds: new THREE.Box3().setFromObject(group),
      doorPosition: new THREE.Vector3(0, 0, depth / 2)
    };
  },

  createWarehouse(width = 24, height = 9, depth = 32) {
    const group = new THREE.Group();
    group.name = 'building_warehouse';

    // Concrete floor
    const floor = new THREE.Mesh(new THREE.BoxGeometry(width, 0.4, depth), this.materials.buildingFloor);
    floor.position.y = 0.2;
    group.add(floor);

    const wallMat = new THREE.MeshLambertMaterial({ color: 0x47525e }); // Corrugated metal look
    const wallThick = 0.5;

    // Back wall
    const backWall = new THREE.Mesh(new THREE.BoxGeometry(width, height, wallThick), wallMat);
    backWall.position.set(0, height / 2, -depth / 2 + wallThick / 2);
    group.add(backWall);

    // Left wall
    const leftWall = new THREE.Mesh(new THREE.BoxGeometry(wallThick, height, depth), wallMat);
    leftWall.position.set(-width / 2 + wallThick / 2, height / 2, 0);
    group.add(leftWall);

    // Right wall
    const rightWall = new THREE.Mesh(new THREE.BoxGeometry(wallThick, height, depth), wallMat);
    rightWall.position.set(width / 2 - wallThick / 2, height / 2, 0);
    group.add(rightWall);

    // Front hangar opening
    const frontPillarW = 5.0;
    const frontL = new THREE.Mesh(new THREE.BoxGeometry(frontPillarW, height, wallThick), wallMat);
    frontL.position.set(-width / 2 + frontPillarW / 2, height / 2, depth / 2 - wallThick / 2);
    group.add(frontL);

    const frontR = new THREE.Mesh(new THREE.BoxGeometry(frontPillarW, height, wallThick), wallMat);
    frontR.position.set(width / 2 - frontPillarW / 2, height / 2, depth / 2 - wallThick / 2);
    group.add(frontR);

    // Big overhead arch
    const arch = new THREE.Mesh(new THREE.BoxGeometry(width - frontPillarW * 2, 2.5, wallThick), wallMat);
    arch.position.set(0, height - 1.25, depth / 2 - wallThick / 2);
    group.add(arch);

    // Arched Roof
    const roof = new THREE.Mesh(new THREE.BoxGeometry(width + 1.2, 0.5, depth + 1.2), this.materials.buildingRoof);
    roof.position.y = height + 0.25;
    group.add(roof);

    return {
      root: group,
      bounds: new THREE.Box3().setFromObject(group),
      doorPosition: new THREE.Vector3(0, 0, depth / 2)
    };
  },

  createWatchtower() {
    const group = new THREE.Group();
    group.name = 'building_watchtower';

    // 4 Corner structural legs
    const legGeo = new THREE.CylinderGeometry(0.18, 0.22, 11);
    const legMat = new THREE.MeshLambertMaterial({ color: 0x3e4854 });
    const offsets = [[-2.5, -2.5], [2.5, -2.5], [-2.5, 2.5], [2.5, 2.5]];
    offsets.forEach(([x, z]) => {
      const leg = new THREE.Mesh(legGeo, legMat);
      leg.position.set(x, 5.5, z);
      group.add(leg);
    });

    // Observation platform
    const platform = new THREE.Mesh(new THREE.BoxGeometry(6.4, 0.4, 6.4), this.materials.buildingFloor);
    platform.position.y = 11;
    group.add(platform);

    // Guard rails
    const railMat = this.materials.buildingWall;
    const rail1 = new THREE.Mesh(new THREE.BoxGeometry(6.4, 1.2, 0.15), railMat);
    rail1.position.set(0, 11.6, 3.1);
    group.add(rail1);
    const rail2 = new THREE.Mesh(new THREE.BoxGeometry(6.4, 1.2, 0.15), railMat);
    rail2.position.set(0, 11.6, -3.1);
    group.add(rail2);
    const rail3 = new THREE.Mesh(new THREE.BoxGeometry(0.15, 1.2, 6.4), railMat);
    rail3.position.set(3.1, 11.6, 0);
    group.add(rail3);
    const rail4 = new THREE.Mesh(new THREE.BoxGeometry(0.15, 1.2, 6.4), railMat);
    rail4.position.set(-3.1, 11.6, 0);
    group.add(rail4);

    // Roof canopy
    const roof = new THREE.Mesh(new THREE.BoxGeometry(7.2, 0.3, 7.2), this.materials.buildingRoof);
    roof.position.y = 14.5;
    group.add(roof);

    return { root: group };
  },

  createSupplyCrate(isAirdrop = false) {
    const group = new THREE.Group();
    const size = isAirdrop ? 1.6 : 1.1;

    if (isAirdrop) {
      // Iconic Red & Blue Military Airdrop container
      const topBlue = new THREE.Mesh(new THREE.BoxGeometry(size, size * 0.35, size), this.materials.airdropBlue);
      topBlue.position.y = size * 0.825;
      group.add(topBlue);

      const bottomRed = new THREE.Mesh(new THREE.BoxGeometry(size, size * 0.65, size), this.materials.airdropRed);
      bottomRed.position.y = size * 0.325;
      group.add(bottomRed);

      // Black Straps
      const strapMat = new THREE.MeshLambertMaterial({ color: 0x111111 });
      const strap = new THREE.Mesh(new THREE.BoxGeometry(size + 0.04, size + 0.04, 0.15), strapMat);
      strap.position.y = size / 2;
      group.add(strap);
    } else {
      // Wooden Field Supply Crate
      const crate = new THREE.Mesh(new THREE.BoxGeometry(size, size * 0.8, size), this.materials.woodCrate);
      crate.position.y = (size * 0.8) / 2;
      group.add(crate);

      // Gold latch indicator
      const latch = new THREE.Mesh(new THREE.BoxGeometry(0.18, 0.18, 0.06), this.materials.playerVisor);
      latch.position.set(0, (size * 0.8) / 2, size / 2 + 0.02);
      group.add(latch);
    }

    return group;
  },

  createTree() {
    const group = new THREE.Group();
    const trunk = new THREE.Mesh(new THREE.CylinderGeometry(0.35, 0.5, 3.5, 8), this.materials.treeTrunk);
    trunk.position.y = 1.75;
    group.add(trunk);

    // Foliage cones (pine / fir tree)
    const cone1 = new THREE.Mesh(new THREE.ConeGeometry(2.8, 3.2, 8), this.materials.treeLeaves);
    cone1.position.y = 4.2;
    group.add(cone1);

    const cone2 = new THREE.Mesh(new THREE.ConeGeometry(2.2, 2.8, 8), this.materials.treeLeaves);
    cone2.position.y = 5.8;
    group.add(cone2);

    const cone3 = new THREE.Mesh(new THREE.ConeGeometry(1.5, 2.2, 8), this.materials.treeLeaves);
    cone3.position.y = 7.2;
    group.add(cone3);

    return group;
  },

  createRock() {
    const rockGeo = new THREE.DodecahedronGeometry(1.4, 1);
    const rock = new THREE.Mesh(rockGeo, this.materials.rock);
    rock.scale.set(1.4, 0.9, 1.2);
    rock.position.y = 0.8;
    return rock;
  },

  createSandbagWall() {
    const group = new THREE.Group();
    const sackGeo = new THREE.BoxGeometry(1.2, 0.35, 0.6);
    for (let row = 0; row < 3; row++) {
      const count = 3;
      for (let col = 0; col < count; col++) {
        const sack = new THREE.Mesh(sackGeo, this.materials.sandbag);
        const offsetX = (col - (count - 1) / 2) * 1.15;
        const stagger = (row % 2) * 0.25;
        sack.position.set(offsetX + stagger, 0.2 + row * 0.32, 0);
        group.add(sack);
      }
    }
    return group;
  }
};

window.ModelFactory = ModelFactory;
