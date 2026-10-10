// 5 Distinct Playable Missions & Level Progression System

const MISSION_DEFINITIONS = [
  {
    level: 1,
    id: 'mission_1',
    title: 'Mission 1: First Contact',
    briefing: 'Hostile scouting units have established an outpost in the western town. Neutralize 4 enemy scouts and collect tactical ammunition.',
    targetLocation: new THREE.Vector3(-45, 0, -45),
    targetName: 'Town Outpost',
    objectives: [
      { id: 'kills', text: 'Eliminate Hostile Scouts', current: 0, target: 4 },
      { id: 'loot', text: 'Collect Field Supplies', current: 0, target: 2 }
    ],
    rewards: { xp: 400, coins: 250 },
    enemySpawns: [
      { name: 'Scout_Alpha', loc: 'town', pos: new THREE.Vector3(-45, 0, -40) },
      { name: 'Scout_Bravo', loc: 'town', pos: new THREE.Vector3(-35, 0, -50) },
      { name: 'Scout_Charlie', loc: 'town', pos: new THREE.Vector3(-55, 0, -32) },
      { name: 'Scout_Delta', loc: 'town', pos: new THREE.Vector3(-28, 0, -35) }
    ]
  },
  {
    level: 2,
    id: 'mission_2',
    title: 'Mission 2: Supply Raid',
    briefing: 'A military airdrop has touched down near the crossroads. Secure the perimeter, eliminate 5 defenders, and loot the central airdrop container.',
    targetLocation: new THREE.Vector3(-5, 0, -5),
    targetName: 'Central Airdrop',
    objectives: [
      { id: 'kills', text: 'Eliminate Supply Guards', current: 0, target: 5 },
      { id: 'crate', text: 'Open Central Airdrop Crate', current: 0, target: 1 }
    ],
    rewards: { xp: 600, coins: 400 },
    enemySpawns: [
      { name: 'Guard_Viper', loc: 'town', pos: new THREE.Vector3(-8, 0, -12) },
      { name: 'Guard_Titan', loc: 'warehouse', pos: new THREE.Vector3(5, 0, -15) },
      { name: 'Guard_Shadow', loc: 'military', pos: new THREE.Vector3(-15, 0, 8) },
      { name: 'Guard_Ghost', loc: 'forest', pos: new THREE.Vector3(12, 0, 6) },
      { name: 'Guard_Razor', loc: 'town', pos: new THREE.Vector3(0, 0, -22) }
    ]
  },
  {
    level: 3,
    id: 'mission_3',
    title: 'Mission 3: Warehouse Infiltration',
    briefing: 'The eastern industrial warehouse contains heavy armor and weapons. Breach the compound, eliminate all 6 warehouse guards, and recover the shotgun.',
    targetLocation: new THREE.Vector3(55, 0, 50),
    targetName: 'Arsenal Warehouse',
    objectives: [
      { id: 'kills', text: 'Clear Warehouse Garrison', current: 0, target: 6 },
      { id: 'loot', text: 'Secure Warehouse Munitions', current: 0, target: 2 }
    ],
    rewards: { xp: 850, coins: 550 },
    enemySpawns: [
      { name: 'Heavy_Iron', loc: 'warehouse', pos: new THREE.Vector3(52, 0, 45) },
      { name: 'Heavy_Steel', loc: 'warehouse', pos: new THREE.Vector3(48, 0, 55) },
      { name: 'Breacher_Wolf', loc: 'warehouse', pos: new THREE.Vector3(62, 0, 48) },
      { name: 'Breacher_Cobra', loc: 'warehouse', pos: new THREE.Vector3(58, 0, 38) },
      { name: 'Patrol_Rogue', loc: 'warehouse', pos: new THREE.Vector3(38, 0, 50) },
      { name: 'Patrol_Blitz', loc: 'warehouse', pos: new THREE.Vector3(68, 0, 52) }
    ]
  },
  {
    level: 4,
    id: 'mission_4',
    title: 'Mission 4: Compound Lockdown',
    briefing: 'The high-security military base in the north-east is heavily fortified with snipers. Eliminate the elite garrison and secure the compound.',
    targetLocation: new THREE.Vector3(65, 0, -55),
    targetName: 'Military Base',
    objectives: [
      { id: 'kills', text: 'Eliminate Base Garrison', current: 0, target: 7 },
      { id: 'crate', text: 'Loot Base Supply Crate', current: 0, target: 1 }
    ],
    rewards: { xp: 1200, coins: 800 },
    enemySpawns: [
      { name: 'Sniper_Apex', loc: 'military', pos: new THREE.Vector3(48, 0, -70) },
      { name: 'Sniper_Hawk', loc: 'military', pos: new THREE.Vector3(82, 0, -38) },
      { name: 'Commando_Rex', loc: 'military', pos: new THREE.Vector3(65, 0, -50) },
      { name: 'Commando_Fox', loc: 'military', pos: new THREE.Vector3(72, 0, -60) },
      { name: 'Enforcer_1', loc: 'military', pos: new THREE.Vector3(58, 0, -42) },
      { name: 'Enforcer_2', loc: 'military', pos: new THREE.Vector3(60, 0, -65) },
      { name: 'Elite_Commander', loc: 'military', pos: new THREE.Vector3(75, 0, -52) }
    ]
  },
  {
    level: 5,
    id: 'mission_5',
    title: 'Mission 5: Zone Extraction',
    briefing: 'FINAL EXTRACTION: Take the military buggy vehicle, eliminate 6 hostile patrol commanders, and reach the Extraction Landing Pad at the southern outpost.',
    targetLocation: new THREE.Vector3(-55, 0, 50),
    targetName: 'Extraction Zone',
    objectives: [
      { id: 'vehicle', text: 'Drive Military Buggy (Enter Vehicle)', current: 0, target: 1 },
      { id: 'kills', text: 'Eliminate Extraction Ambush', current: 0, target: 6 },
      { id: 'extract', text: 'Reach Extraction LZ', current: 0, target: 1 }
    ],
    rewards: { xp: 2000, coins: 1500 },
    enemySpawns: [
      { name: 'Ambush_Leader', loc: 'forest', pos: new THREE.Vector3(-45, 0, 42) },
      { name: 'Ambush_Gunner', loc: 'forest', pos: new THREE.Vector3(-60, 0, 48) },
      { name: 'Heavy_Assault_1', loc: 'forest', pos: new THREE.Vector3(-50, 0, 58) },
      { name: 'Heavy_Assault_2', loc: 'town', pos: new THREE.Vector3(-30, 0, 25) },
      { name: 'Interceptor_1', loc: 'warehouse', pos: new THREE.Vector3(10, 0, 20) },
      { name: 'Interceptor_2', loc: 'military', pos: new THREE.Vector3(-15, 0, -10) }
    ]
  }
];

class MissionManager {
  constructor() {
    this.currentLevel = 1;
    this.currentMission = null;
  }

  loadLevel(levelNumber) {
    const found = MISSION_DEFINITIONS.find(m => m.level === levelNumber) || MISSION_DEFINITIONS[0];
    this.currentLevel = found.level;
    this.currentMission = {
      level: found.level,
      id: found.id,
      title: found.title,
      briefing: found.briefing,
      targetLocation: found.targetLocation ? found.targetLocation.clone() : new THREE.Vector3(0, 0, 0),
      targetName: found.targetName,
      objectives: found.objectives.map(o => ({ ...o, current: 0 })),
      rewards: { ...found.rewards },
      enemySpawns: (found.enemySpawns || []).map(e => ({
        name: e.name,
        loc: e.loc,
        pos: e.pos && typeof e.pos.clone === 'function'
          ? e.pos.clone()
          : new THREE.Vector3(e.pos ? e.pos.x : 0, e.pos ? (e.pos.y || 0) : 0, e.pos ? e.pos.z : 0)
      }))
    };
    return this.currentMission;
  }

  updateObjective(type, amount = 1) {
    if (!this.currentMission) return false;

    let updated = false;
    this.currentMission.objectives.forEach(obj => {
      if (obj.id === type && obj.current < obj.target) {
        obj.current = Math.min(obj.target, obj.current + amount);
        updated = true;
      }
    });

    return updated;
  }

  isMissionComplete() {
    if (!this.currentMission) return false;
    return this.currentMission.objectives.every(obj => obj.current >= obj.target);
  }
}

window.MISSION_DEFINITIONS = MISSION_DEFINITIONS;
window.missionManager = new MissionManager();
