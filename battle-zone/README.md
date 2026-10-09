# BATTLE ZONE: SURVIVAL

A complete, playable, third-person 3D Battle Royale survival game built with **Three.js**, **WebGL**, **HTML5**, and **JavaScript**.

---

## 🎮 Playable Features

1. **Realistic Third-Person 3D Gameplay**:
   - 3D soldier character with procedural body parts, tactical Level 3 helmet with golden visor, chest rig armor vest, and combat boots.
   - Smooth over-the-shoulder third-person camera with pitch, yaw, and adjustable sensitivity.
   - Dynamic animations: idle breathing, walking, running, sprinting, crouching, jumping, aiming, shooting, reloading, taking damage, and death ragdoll.

2. **Open-World 3D Battle Map**:
   - Explorable town compounds with enterable buildings and rooms.
   - Fortified military compound with watchtowers and security walls.
   - Industrial warehouse district with stacked supply crates.
   - Roads, crossroad intersections, pine/oak forests, and tactical cover boulders.
   - Collision detection with buildings, obstacles, vehicles, and boundaries.

3. **Location-Based AI Combat Bots**:
   - Town scouts, warehouse guards, military elite garrison, and forest ambush patrols.
   - State machine: `PATROL`, `INVESTIGATE`, `CHASE`, `ATTACK`, and `DEAD`.
   - Line-of-sight checks against walls, gunfire awareness, aim accuracy, and health bars.
   - Enemy elimination rewards and 3D loot crate drops on death.

4. **Arsenal & Functional Weapons**:
   - **M4 Tactical Rifle**: 34 DMG, 500 RPM, 30-round mag, 160m range.
   - **AWM-50 Heavy Sniper**: 110 DMG (one-shot headshot), high-power telescopic scope, 350m range.
   - **S12 Tactical Shotgun**: 18x8 pellet damage, lethal close quarters spread.
   - **P9 Tactical Sidearm**: Lightweight rapid semi-automatic pistol.
   - Hitscan raycasting, bullet tracers, muzzle flash bursts, recoil, and hit markers.

5. **Drivable 3D Vehicle (Military Buggy)**:
   - Full 3D tubular roll cage chassis with 4 animated wheels and steering.
   - Walk up to buggy -> context button **ENTER VEHICLE [E]** appears.
   - Acceleration, reverse, braking, and steering physics.
   - Exit safely back to on-foot mode at any time.

6. **Special Tactical Powers**:
   - **Speed Boost [⚡]**: +55% movement sprint velocity.
   - **Energy Shield [🛡️]**: 50% incoming damage reduction bubble.
   - **Enemy Scanner [📡]**: Reveals all nearby bot locations on the radar minimap.

7. **5 Playable Missions & Level Progression**:
   - **Level 1: First Contact** — Neutralize 4 town scouts and gather ammunition.
   - **Level 2: Supply Raid** — Eliminate 5 defenders and loot the central airdrop crate.
   - **Level 3: Warehouse Infiltration** — Clear 6 warehouse guards and retrieve munitions.
   - **Level 4: Compound Lockdown** — Eliminate the 7-man military base garrison.
   - **Level 5: Zone Extraction** — Drive the military buggy to the extraction LZ under ambush.

8. **Save System**:
   - Progress (unlocked levels 1-5, completed missions, XP, coins, total kills, and sensitivity settings) is persisted in browser `localStorage`.

---

## 🕹️ Controls

### PC Controls
- **W / A / S / D**: Move forward, left, backward, right
- **Mouse Drag / Move**: Look & aim camera
- **Left Mouse Button**: Fire weapon
- **Right Mouse Button**: Aim down sights (ADS)
- **Space**: Jump
- **C**: Crouch / Stand toggle
- **Shift**: Sprint
- **R**: Reload weapon
- **E / F**: Context action (Enter/Exit vehicle, Open crate, Pick up)
- **1 / 2**: Switch primary / secondary weapon
- **Tab**: Open tactical backpack inventory
- **Escape**: Pause menu

### Mobile Touch Controls
- **Left Virtual Joystick**: 360-degree analog movement
- **Right Screen Drag Zone**: Smooth camera look & aim
- **Action Cluster**: Dedicated touch buttons for FIRE, AIM, JUMP, CROUCH, SPRINT, RELOAD
- **Context Action Button**: Dynamic button for vehicle entry/exit and crate interaction
- **Power Icons**: One-tap activation for Speed Boost, Shield, and Scanner

---

## 🚀 How to Publish to GitHub Pages

1. Create a new GitHub repository (e.g. `battle-zone-survival`).
2. Copy the contents of the `battle-zone/` folder (or root `app/src/main/assets/game/`) to the repository:
   ```bash
   git init
   git add .
   git commit -m "Initial commit: Battle Zone 3D Battle Royale"
   git branch -M main
   git remote add origin https://github.com/YOUR_USERNAME/battle-zone-survival.git
   git push -u origin main
   ```
3. In your GitHub repository:
   - Navigate to **Settings** -> **Pages**.
   - Under **Build and deployment** -> **Source**, select `Deploy from a branch`.
   - Under **Branch**, select `main` / `root` and click **Save**.
4. Your game will be live at `https://YOUR_USERNAME.github.io/battle-zone-survival/`!

---

## 📁 Project Structure

```
├── index.html           # Main HTML entry point & UI overlay
├── css/
│   └── style.css        # Responsive styling & tactical HUD layout
└── js/
    ├── lib/
    │   └── three.min.js # Bundled offline Three.js r128 engine
    ├── audio.js         # Procedural Web Audio API sound synthesizer
    ├── saveSystem.js    # LocalStorage progression & settings manager
    ├── weapons.js       # Weapon definitions & combat ballistics
    ├── models.js        # Procedural 3D meshes (Soldier, Weapons, Buggy, Buildings)
    ├── map.js           # 3D open world battleground generator & colliders
    ├── player.js        # Third-person controller, camera follow, driving
    ├── enemyAI.js       # Dynamic location-based AI bots
    ├── missions.js      # 5 playable levels, objectives & rewards
    ├── input.js         # Unified mobile touch & desktop controls
    ├── ui.js            # HUD, minimap radar, compass, inventory modals
    └── game.js          # Main coordinator & 60 FPS WebGL render loop
```
