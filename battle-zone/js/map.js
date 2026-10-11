// Open-World 3D Battleground Map Generator & Collision Registry
// Generates town compounds, military base, warehouse area, roads, forests, cover obstacles, and loot spawns

class BattleMap {
  constructor(scene) {
    this.scene = scene;
    this.colliders = []; // Bounding boxes for collision detection
    this.lootSpawns = []; // Pickup entities in the world
    this.landmarks = [
      { name: 'Pochinki Outpost', pos: new THREE.Vector3(-45, 0, -40), radius: 35 },
      { name: 'Military Compound', pos: new THREE.Vector3(50, 0, -50), radius: 45 },
      { name: 'Arsenal Warehouse', pos: new THREE.Vector3(40, 0, 45), radius: 40 },
      { name: 'Forest Bunker', pos: new THREE.Vector3(-55, 0, 50), radius: 35 },
      { name: 'Central Crossroads', pos: new THREE.Vector3(0, 0, 0), radius: 30 }
    ];
    this.vehicles = [];
    this.supplyCrates = [];
    this.deathCrates = [];
  }

  build() {
    this.createTerrain();
    this.createRoads();
    this.createTown();
    this.createMilitaryCompound();
    this.createWarehouseDistrict();
    this.createForestAndRocks();
    this.createVehicles();
    this.createSupplyCrates();

    this.scene.updateMatrixWorld(true);
  }

  createTerrain() {
    // Large ground plane (400 x 400 world units)
    const groundGeo = new THREE.PlaneGeometry(420, 420, 32, 32);
    // Dark military grass / terrain texture
    const groundMat = new THREE.MeshLambertMaterial({ color: 0x1d3124 });
    const ground = new THREE.Mesh(groundGeo, groundMat);
    ground.rotation.x = -Math.PI / 2;
    ground.receiveShadow = true;
    this.scene.add(ground);

    // Boundary perimeter hills / walls
    const borderMat = new THREE.MeshLambertMaterial({ color: 0x142017 });
    const wallGeo = new THREE.BoxGeometry(430, 16, 8);

    // North
    const wN = new THREE.Mesh(wallGeo, borderMat);
    wN.position.set(0, 8, -210);
    this.scene.add(wN);
    wN.updateMatrixWorld(true);
    this.colliders.push(new THREE.Box3().setFromObject(wN));

    // South
    const wS = new THREE.Mesh(wallGeo, borderMat);
    wS.position.set(0, 8, 210);
    this.scene.add(wS);
    wS.updateMatrixWorld(true);
    this.colliders.push(new THREE.Box3().setFromObject(wS));

    // East & West
    const wallGeoV = new THREE.BoxGeometry(8, 16, 430);
    const wE = new THREE.Mesh(wallGeoV, borderMat);
    wE.position.set(210, 8, 0);
    this.scene.add(wE);
    wE.updateMatrixWorld(true);
    this.colliders.push(new THREE.Box3().setFromObject(wE));

    const wW = new THREE.Mesh(wallGeoV, borderMat);
    wW.position.set(-210, 8, 0);
    this.scene.add(wW);
    wW.updateMatrixWorld(true);
    this.colliders.push(new THREE.Box3().setFromObject(wW));
  }

  createRoads() {
    const roadMat = new THREE.MeshLambertMaterial({ color: 0x2c3e50 });

    // East-West Main Highway
    const roadEW = new THREE.Mesh(new THREE.PlaneGeometry(400, 10), roadMat);
    roadEW.rotation.x = -Math.PI / 2;
    roadEW.position.set(0, 0.02, 0);
    this.scene.add(roadEW);

    // North-South Arterial Road
    const roadNS = new THREE.Mesh(new THREE.PlaneGeometry(10, 400), roadMat);
    roadNS.rotation.x = -Math.PI / 2;
    roadNS.position.set(0, 0.02, 0);
    this.scene.add(roadNS);

    // Road connecting Town to Crossroads
    const townRoad = new THREE.Mesh(new THREE.PlaneGeometry(120, 8), roadMat);
    townRoad.rotation.x = -Math.PI / 2;
    townRoad.rotation.z = Math.PI / 4;
    townRoad.position.set(-30, 0.02, -25);
    this.scene.add(townRoad);
  }

  createTown() {
    // Pochinki-style outpost: 4 Explorable buildings with furniture/loot
    const houseConfigs = [
      { x: -55, z: -55, rot: 0, type: 'house' },
      { x: -35, z: -55, rot: Math.PI / 2, type: 'house' },
      { x: -55, z: -30, rot: -Math.PI / 2, type: 'house' },
      { x: -30, z: -30, rot: Math.PI, type: 'bunker' }
    ];

    houseConfigs.forEach(cfg => {
      const bldg = ModelFactory.createBuilding(cfg.type, 13, 7, 15);
      bldg.root.position.set(cfg.x, 0, cfg.z);
      bldg.root.rotation.y = cfg.rot;
      this.scene.add(bldg.root);
      bldg.root.updateMatrixWorld(true);

      // Register vertical walls as colliders (exclude floor: y <= 0.35, exclude roof: y >= 4.5)
      bldg.root.children.forEach(child => {
        if (child.geometry && child.geometry.type === 'BoxGeometry') {
          if (child.position.y > 0.4 && child.position.y < 4.5) {
            // Exclude open doorways / lintels
            if (child.position.z !== 0 || Math.abs(child.position.x) > 2.0) {
              const box = new THREE.Box3().setFromObject(child);
              this.colliders.push(box);
            }
          }
        }
      });

      // Spawn loot inside building
      this.addLoot(cfg.x, 0.5, cfg.z, 'medkit');
      this.addLoot(cfg.x + 2, 0.5, cfg.z + 2, 'ammo_556');
    });

    // Town sandbag barriers
    const sb1 = ModelFactory.createSandbagWall();
    sb1.position.set(-45, 0, -42);
    this.scene.add(sb1);
    sb1.updateMatrixWorld(true);
    this.colliders.push(new THREE.Box3().setFromObject(sb1));
  }

  createMilitaryCompound() {
    // High-tier loot compound: Watchtowers, perimeter security walls, barracks
    const centerX = 65;
    const centerZ = -55;

    // Central Barracks Building
    const barracks = ModelFactory.createBuilding('barracks', 18, 8, 22);
    barracks.root.position.set(centerX, 0, centerZ);
    this.scene.add(barracks.root);
    barracks.root.updateMatrixWorld(true);
    barracks.root.children.forEach(child => {
      if (child.geometry && child.geometry.type === 'BoxGeometry') {
        if (child.position.y > 0.4 && child.position.y < 5.5) {
          if (child.position.z !== 0 || Math.abs(child.position.x) > 2.0) {
            const box = new THREE.Box3().setFromObject(child);
            this.colliders.push(box);
          }
        }
      }
    });

    // Two Watchtowers
    const wt1 = ModelFactory.createWatchtower();
    wt1.root.position.set(centerX - 18, 0, centerZ - 18);
    this.scene.add(wt1.root);
    wt1.root.updateMatrixWorld(true);
    this.colliders.push(new THREE.Box3().setFromObject(wt1.root));

    const wt2 = ModelFactory.createWatchtower();
    wt2.root.position.set(centerX + 18, 0, centerZ + 18);
    this.scene.add(wt2.root);
    wt2.root.updateMatrixWorld(true);
    this.colliders.push(new THREE.Box3().setFromObject(wt2.root));

    // Compound sandbag fortifications
    const sb2 = ModelFactory.createSandbagWall();
    sb2.position.set(centerX, 0, centerZ + 15);
    this.scene.add(sb2);
    sb2.updateMatrixWorld(true);
    this.colliders.push(new THREE.Box3().setFromObject(sb2));

    // High tier loot inside barracks
    this.addLoot(centerX, 0.5, centerZ, 'sniper');
    this.addLoot(centerX + 3, 0.5, centerZ, 'armor_lv3');
    this.addLoot(centerX - 3, 0.5, centerZ, 'ammo_sniper');
  }

  createWarehouseDistrict() {
    // Large hangar warehouse with crates
    const whX = 55;
    const whZ = 50;

    const wh = ModelFactory.createWarehouse(24, 9, 30);
    wh.root.position.set(whX, 0, whZ);
    this.scene.add(wh.root);
    wh.root.updateMatrixWorld(true);
    wh.root.children.forEach(child => {
      if (child.geometry && child.geometry.type === 'BoxGeometry') {
        // Exclude floor (y <= 0.4) and high roof/overhead arch (y >= 6.5)
        if (child.position.y > 0.4 && child.position.y < 6.5) {
          const box = new THREE.Box3().setFromObject(child);
          this.colliders.push(box);
        }
      }
    });

    // Stacked crates inside and outside warehouse
    const crateOffsets = [
      [whX - 6, whZ - 6], [whX - 6, whZ + 6], [whX + 6, whZ - 6],
      [whX + 16, whZ - 10], [whX - 16, whZ + 10]
    ];
    crateOffsets.forEach(([cx, cz]) => {
      const cr = ModelFactory.createSupplyCrate(false);
      cr.position.set(cx, 0, cz);
      this.scene.add(cr);
      cr.updateMatrixWorld(true);
      this.colliders.push(new THREE.Box3().setFromObject(cr));
    });

    // Loot inside warehouse
    this.addLoot(whX, 0.5, whZ - 4, 'shotgun');
    this.addLoot(whX, 0.5, whZ + 4, 'ammo_shotgun');
    this.addLoot(whX + 4, 0.5, whZ, 'energy_drink');
  }

  createForestAndRocks() {
    // Forests in South-West and North-East
    const treeCoords = [
      [-90, -90], [-80, -100], [-105, -75], [-70, -85], [-95, -60],
      [-40, 70], [-55, 85], [-75, 75], [-60, 60], [-85, 95],
      [90, 80], [80, 95], [105, 70], [75, 85], [95, 105],
      [15, -80], [25, -95], [35, -85], [-15, 85], [-25, 95],
      [-10, -40], [15, -45], [-35, 15], [35, 10]
    ];

    treeCoords.forEach(([tx, tz]) => {
      const tree = ModelFactory.createTree();
      tree.position.set(tx, 0, tz);
      this.scene.add(tree);
      // Trunk collider
      const trunkBox = new THREE.Box3(
        new THREE.Vector3(tx - 0.6, 0, tz - 0.6),
        new THREE.Vector3(tx + 0.6, 4, tz + 0.6)
      );
      this.colliders.push(trunkBox);
    });

    // Rocks / Boulders for tactical cover
    const rockCoords = [
      [-15, -15], [20, -18], [-25, 30], [18, 25],
      [-70, -20], [80, -25], [-10, 50], [50, -10],
      [-5, -75], [75, 20]
    ];

    rockCoords.forEach(([rx, rz]) => {
      const rock = ModelFactory.createRock();
      rock.position.set(rx, 0, rz);
      this.scene.add(rock);
      rock.updateMatrixWorld(true);
      this.colliders.push(new THREE.Box3().setFromObject(rock));
    });
  }

  createVehicles() {
    // Drivable military buggies in world
    const v1 = ModelFactory.createVehicle();
    v1.root.position.set(12, 0, 15);
    v1.root.rotation.y = 0.5;
    this.scene.add(v1.root);
    v1.root.updateMatrixWorld(true);

    const v2 = ModelFactory.createVehicle();
    v2.root.position.set(-38, 0, -18);
    v2.root.rotation.y = -1.2;
    this.scene.add(v2.root);
    v2.root.updateMatrixWorld(true);

    this.vehicles = [
      { id: 'v1', model: v1, position: v1.root.position, rotation: v1.root.rotation, isOccupied: false, health: 450, maxHealth: 450 },
      { id: 'v2', model: v2, position: v2.root.position, rotation: v2.root.rotation, isOccupied: false, health: 450, maxHealth: 450 }
    ];
  }

  createSupplyCrates() {
    // Supply Crates in distinct zones
    const crates = [
      { x: -5, z: -5, isAirdrop: true },   // Central Airdrop
      { x: -50, z: -40, isAirdrop: false }, // Town loot
      { x: 65, z: -45, isAirdrop: false },  // Military loot
      { x: 55, z: 45, isAirdrop: false }    // Warehouse loot
    ];

    crates.forEach((c, idx) => {
      const crateMesh = ModelFactory.createSupplyCrate(c.isAirdrop);
      crateMesh.position.set(c.x, 0, c.z);
      this.scene.add(crateMesh);
      crateMesh.updateMatrixWorld(true);

      const box = new THREE.Box3().setFromObject(crateMesh);
      this.colliders.push(box);

      this.supplyCrates.push({
        id: 'crate_' + idx,
        isAirdrop: c.isAirdrop,
        position: new THREE.Vector3(c.x, 0, c.z),
        mesh: crateMesh,
        isOpened: false
      });
    });
  }

  addLoot(x, y, z, type) {
    // Floating rotating 3D pickup icon/box
    const group = new THREE.Group();
    group.position.set(x, y + 0.35, z);

    let matColor = 0xffffff;
    if (type.includes('ammo')) matColor = 0x81c784;
    else if (type.includes('med') || type.includes('drink')) matColor = 0x00e676;
    else if (type === 'sniper') matColor = 0x76ff03;
    else if (type === 'shotgun') matColor = 0xff7043;
    else if (type.includes('armor')) matColor = 0xffd700;

    const geo = new THREE.BoxGeometry(0.5, 0.5, 0.5);
    const mat = new THREE.MeshLambertMaterial({ color: matColor });
    const mesh = new THREE.Mesh(geo, mat);
    group.add(mesh);

    // Glowing halo
    const haloGeo = new THREE.RingGeometry(0.35, 0.45, 16);
    const haloMat = new THREE.MeshBasicMaterial({ color: matColor, side: THREE.DoubleSide });
    const halo = new THREE.Mesh(haloGeo, haloMat);
    halo.rotation.x = Math.PI / 2;
    halo.position.y = -0.3;
    group.add(halo);

    this.scene.add(group);
    this.lootSpawns.push({
      type,
      position: new THREE.Vector3(x, y, z),
      group,
      mesh,
      collected: false
    });
  }

  spawnDeathCrate(bot) {
    const crateMesh = ModelFactory.createDeathCrate();
    crateMesh.position.set(bot.position.x, 0, bot.position.z);
    this.scene.add(crateMesh);

    const weaponId = (bot.weapon && bot.weapon.id) ? bot.weapon.id : 'rifle';
    const weaponName = (bot.weapon && bot.weapon.name) ? bot.weapon.name : 'M4 Tactical Rifle';

    const deathCrate = {
      id: 'death_crate_' + bot.id,
      enemyName: bot.name,
      position: bot.position.clone(),
      mesh: crateMesh,
      looted: false,
      items: {
        weaponId: weaponId,
        weaponName: weaponName,
        ammo: weaponId === 'sniper' ? 10 : (weaponId === 'shotgun' ? 16 : 45),
        medkit: 1,
        energyDrink: 1
      }
    };

    if (!this.deathCrates) this.deathCrates = [];
    this.deathCrates.push(deathCrate);
    return deathCrate;
  }

  checkCollision(position, radius = 0.5) {
    const playerBox = new THREE.Box3(
      new THREE.Vector3(position.x - radius, position.y + 0.1, position.z - radius),
      new THREE.Vector3(position.x + radius, position.y + 1.8, position.z + radius)
    );

    for (let i = 0; i < this.colliders.length; i++) {
      if (playerBox.intersectsBox(this.colliders[i])) {
        return true;
      }
    }
    return false;
  }
}

window.BattleMap = BattleMap;
