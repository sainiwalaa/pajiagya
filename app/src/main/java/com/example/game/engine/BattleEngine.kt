package com.example.game.engine

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.game.audio.TacticalSoundEffects
import com.example.game.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.*

class BattleEngine(context: Context) : ViewModel() {

    val audio = TacticalSoundEffects(context)

    // Player state
    private val _player = MutableStateFlow(PlayerState())
    val player: StateFlow<PlayerState> = _player.asStateFlow()

    // World Entities
    private val _bots = MutableStateFlow<List<EnemyBot>>(emptyList())
    val bots: StateFlow<List<EnemyBot>> = _bots.asStateFlow()

    private val _buildings = MutableStateFlow<List<Building>>(emptyList())
    val buildings: StateFlow<List<Building>> = _buildings.asStateFlow()

    private val _vehicles = MutableStateFlow<List<VehicleEntity>>(emptyList())
    val vehicles: StateFlow<List<VehicleEntity>> = _vehicles.asStateFlow()

    private val _loot = MutableStateFlow<List<LootEntity>>(emptyList())
    val loot: StateFlow<List<LootEntity>> = _loot.asStateFlow()

    private val _projectiles = MutableStateFlow<List<Projectile>>(emptyList())
    val projectiles: StateFlow<List<Projectile>> = _projectiles.asStateFlow()

    private val _explosions = MutableStateFlow<List<ExplosionEffect>>(emptyList())
    val explosions: StateFlow<List<ExplosionEffect>> = _explosions.asStateFlow()

    private val _airdrop = MutableStateFlow<AirdropCrate?>(null)
    val airdrop: StateFlow<AirdropCrate?> = _airdrop.asStateFlow()

    private val _recallTower = MutableStateFlow(
        RecallTower("recall_1", "Recall Beacon Station", 1450f, 950f)
    )
    val recallTower: StateFlow<RecallTower> = _recallTower.asStateFlow()

    private val _safeZone = MutableStateFlow(SafeZoneState())
    val safeZone: StateFlow<SafeZoneState> = _safeZone.asStateFlow()

    // Match Metrics
    private val _aliveCount = MutableStateFlow(42)
    val aliveCount: StateFlow<Int> = _aliveCount.asStateFlow()

    private val _killFeed = MutableStateFlow<List<KillFeedItem>>(emptyList())
    val killFeed: StateFlow<List<KillFeedItem>> = _killFeed.asStateFlow()

    private val _currentHint = MutableStateFlow("Tap virtual joystick to move. Search buildings for supplies!")
    val currentHint: StateFlow<String> = _currentHint.asStateFlow()

    private val _activeWaypoint = MutableStateFlow<Pair<Float, Float>?>(null)
    val activeWaypoint: StateFlow<Pair<Float, Float>?> = _activeWaypoint.asStateFlow()

    private val _isVictory = MutableStateFlow(false)
    val isVictory: StateFlow<Boolean> = _isVictory.asStateFlow()

    private val _isGameOver = MutableStateFlow(false)
    val isGameOver: StateFlow<Boolean> = _isGameOver.asStateFlow()

    private val _battlePoints = MutableStateFlow(450)
    val battlePoints: StateFlow<Int> = _battlePoints.asStateFlow()

    private val _isScoped = MutableStateFlow(false)
    val isScoped: StateFlow<Boolean> = _isScoped.asStateFlow()

    // Missions
    private val _missions = MutableStateFlow<List<BattleMission>>(
        listOf(
            BattleMission("m1", "Combat Veteran", "Eliminate 3 enemy combatants in match", 0, 3, 300),
            BattleMission("m2", "High-Speed Raid", "Drive a Buggy or Tanker across the zone", 0, 1, 250),
            BattleMission("m3", "Tactical Breach", "Enter any compound or military bunker", 0, 1, 200),
            BattleMission("m4", "Supply Intercept", "Reach and loot an Airdrop crate", 0, 1, 450),
            BattleMission("m5", "Recall Sentinel", "Activate the Green Recall Beacon Tower", 0, 1, 350),
            BattleMission("m6", "Heavy Ordnance", "Throw and detonate a Frag Grenade", 0, 1, 250),
            BattleMission("m7", "Chicken Dinner", "Survive as the last standing player", 0, 1, 1000)
        )
    )
    val missions: StateFlow<List<BattleMission>> = _missions.asStateFlow()

    private var gameLoopJob: Job? = null
    private var nextProjectileId = 1L
    private var nextExplosionId = 1L
    private var distanceDriven = 0f

    init {
        initializeBattleground()
        startLoop()
    }

    fun restartMatch() {
        gameLoopJob?.cancel()
        _isVictory.value = false
        _isGameOver.value = false
        _aliveCount.value = 42
        _projectiles.value = emptyList()
        _explosions.value = emptyList()
        _player.value = PlayerState(x = 550f, y = 650f)
        _safeZone.value = SafeZoneState()
        _recallTower.value = RecallTower("recall_1", "Recall Beacon Station", 1450f, 950f)
        _airdrop.value = null
        distanceDriven = 0f
        initializeBattleground()
        startLoop()
        showHint("Match Started! Landed in Erangel Battleground. Gear up!")
    }

    private fun initializeBattleground() {
        // Buildings & Compounds
        val buildingList = listOf(
            Building("b1", "Military Barracks A", 450f, 350f, 260f, 200f),
            Building("b2", "Arsenal Warehouse", 850f, 400f, 340f, 240f),
            Building("b3", "Pochinki Outpost 1", 1250f, 450f, 240f, 220f),
            Building("b4", "Central Command Bunker", 700f, 900f, 320f, 280f),
            Building("b5", "Sosnovka Watch Base", 350f, 1100f, 280f, 220f),
            Building("b6", "Sniper Tower Compound", 1200f, 1200f, 220f, 220f),
            Building("b7", "Research Lab Facility", 800f, 1500f, 340f, 260f)
        )
        _buildings.value = buildingList

        // Vehicles
        val vehicleList = listOf(
            VehicleEntity("v1", VehicleType.BUGGY, 580f, 750f, angle = 0.5f),
            VehicleEntity("v2", VehicleType.TANKER, 1020f, 1150f, angle = 1.2f),
            VehicleEntity("v3", VehicleType.BUGGY, 1350f, 600f, angle = -0.8f)
        )
        _vehicles.value = vehicleList

        // Loot scattered around map & inside buildings
        val lootList = mutableListOf<LootEntity>()
        var lootId = 1
        for (b in buildingList) {
            lootList.add(LootEntity("l_${lootId++}", LootType.FIRST_AID, b.x + 60f, b.y + 60f))
            lootList.add(LootEntity("l_${lootId++}", LootType.AMMO_556, b.x + 120f, b.y + 80f))
            lootList.add(LootEntity("l_${lootId++}", LootType.VEST_LV3, b.x + 180f, b.y + 70f))
            lootList.add(LootEntity("l_${lootId++}", LootType.ENERGY_DRINK, b.x + 80f, b.y + 140f))
        }
        // Open field loot
        lootList.add(LootEntity("l_${lootId++}", LootType.MED_KIT, 600f, 600f))
        lootList.add(LootEntity("l_${lootId++}", LootType.HELMET_LV3, 750f, 700f))
        lootList.add(LootEntity("l_${lootId++}", LootType.FRAG_GRENADE, 620f, 850f))
        lootList.add(LootEntity("l_${lootId++}", LootType.AMMO_762, 950f, 800f))
        lootList.add(LootEntity("l_${lootId++}", LootType.AIRDROP_FLARE, 1400f, 1000f))
        _loot.value = lootList

        // Enemy bots
        val botNames = listOf(
            "Bot_Shadow", "Bot_Falcon", "Viper_Nine", "Sniper_Rex",
            "Ghost_Recon", "Delta_Commando", "Rogue_Tiger", "Hawk_Eye",
            "Cobra_Strike", "Steel_Wolf", "Phantom_Ace", "Blitz_Krieg"
        )
        val botList = botNames.mapIndexed { idx, name ->
            val angle = (idx * 0.5f)
            val dist = 350f + (idx * 80f)
            val bx = (1100f + cos(angle) * dist).coerceIn(200f, 2100f)
            val by = (1100f + sin(angle) * dist).coerceIn(200f, 2100f)
            EnemyBot(
                id = idx + 1,
                name = name,
                x = bx,
                y = by,
                angle = Math.random().toFloat() * 6.28f,
                health = 100f,
                weapon = if (idx % 2 == 0) WeaponType.AKM else WeaponType.M416,
                patrolTargetX = bx + (-100..100).random().toFloat(),
                patrolTargetY = by + (-100..100).random().toFloat()
            )
        }
        _bots.value = botList
    }

    private fun startLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            var zoneTimerCounter = 0
            while (isActive) {
                delay(33L) // ~30 FPS loop for ultra smooth responsiveness
                updateGame(0.033f)

                zoneTimerCounter++
                if (zoneTimerCounter >= 30) {
                    zoneTimerCounter = 0
                    updateZoneTimer()
                    simulateBotSkirmishes()
                }
            }
        }
    }

    private fun updateGame(dt: Float) {
        if (_isGameOver.value || _isVictory.value) return

        val p = _player.value
        val safe = _safeZone.value

        // 1. Check building entrance / interior
        var inside = false
        var insideBuildingName: String? = null
        for (b in _buildings.value) {
            if (b.contains(p.x, p.y)) {
                inside = true
                insideBuildingName = b.name
                break
            }
        }
        if (inside != p.isInsideBuilding) {
            _player.update { it.copy(isInsideBuilding = inside, activeBuildingName = insideBuildingName) }
            if (inside) {
                showHint("Entered $insideBuildingName. Buildings offer cover from sniper fire!")
                updateMissionProgress("m3", 1)
            } else {
                showHint("Exited compound. Watch out for open fields!")
            }
        }

        // 2. Boost health regeneration
        if (p.boost > 0f && p.health < p.maxHealth && !p.isDead) {
            val healAmount = dt * 1.5f
            val newHealth = (p.health + healAmount).coerceAtMost(p.maxHealth)
            val newBoost = (p.boost - dt * 0.8f).coerceAtLeast(0f)
            _player.update { it.copy(health = newHealth, boost = newBoost) }
        }

        // 3. Blue Zone damage
        val distToZoneCenter = sqrt((p.x - safe.centerX).pow(2) + (p.y - safe.centerY).pow(2))
        if (distToZoneCenter > safe.currentRadius && !p.isDead) {
            val zoneDmg = dt * (3f + safe.phase * 2f)
            applyDamageToPlayer(zoneDmg, "PlayZone")
            if (Math.random() < 0.05) {
                showHint("⚠️ In Blue Zone! Move towards the White Safe Circle immediately!")
            }
        }

        // 4. Update Projectiles
        val updatedProjectiles = mutableListOf<Projectile>()
        for (proj in _projectiles.value) {
            proj.x += proj.vx
            proj.y += proj.vy
            proj.traveledDistance += sqrt(proj.vx.pow(2) + proj.vy.pow(2))

            var hit = false

            // Check hit against player if enemy owned
            if (!proj.isPlayerOwned) {
                val pDist = sqrt((proj.x - p.x).pow(2) + (proj.y - p.y).pow(2))
                if (pDist < 25f && !p.isDead) {
                    hit = true
                    val isHeadshot = Math.random() < 0.25
                    val multiplier = if (isHeadshot) 1.5f else 1.0f
                    applyDamageToPlayer(proj.damage * multiplier, "Enemy Bot")
                    audio.playHitMarker()
                }
            } else {
                // Check hit against Bots
                val currentBots = _bots.value
                for (bot in currentBots) {
                    if (!bot.isDead) {
                        val bDist = sqrt((proj.x - bot.x).pow(2) + (proj.y - bot.y).pow(2))
                        if (bDist < 26f) {
                            hit = true
                            val isHeadshot = Math.random() < 0.3
                            val finalDmg = proj.damage * (if (isHeadshot) 1.8f else 1.0f)
                            bot.health -= finalDmg
                            audio.playHitMarker()
                            if (bot.health <= 0f) {
                                bot.isDead = true
                                onBotEliminated(bot, isHeadshot)
                            }
                            break
                        }
                    }
                }
            }

            if (!hit && proj.traveledDistance < proj.maxDistance) {
                updatedProjectiles.add(proj)
            }
        }
        _projectiles.value = updatedProjectiles

        // 5. Update Explosions
        val remainingExplosions = _explosions.value.mapNotNull { exp ->
            exp.currentRadius += dt * 140f
            exp.alpha -= dt * 1.5f
            if (exp.alpha > 0f) exp else null
        }
        _explosions.value = remainingExplosions

        // 6. Update Airdrop drop altitude
        val drop = _airdrop.value
        if (drop != null && !drop.hasLanded) {
            drop.altitude -= dt * 60f
            if (drop.altitude <= 0f) {
                drop.altitude = 0f
                drop.hasLanded = true
                showHint("🚁 AIRDROP TOUCHDOWN with Red Smoke! High Tier Supplies Available!")
            }
        }

        // 7. Update Bots AI
        updateBotAI(dt)

        // 8. Vehicle roadkill if player is driving
        if (p.inVehicle != null) {
            val vSpeed = if (p.inVehicle == VehicleType.TANKER) 7f else 9.5f
            distanceDriven += vSpeed * dt
            if (distanceDriven > 100f) {
                updateMissionProgress("m2", 1)
            }

            // Check collision with enemy bots
            for (bot in _bots.value) {
                if (!bot.isDead) {
                    val dist = sqrt((p.x - bot.x).pow(2) + (p.y - bot.y).pow(2))
                    if (dist < 40f) {
                        bot.health = 0f
                        bot.isDead = true
                        onBotEliminated(bot, false, byVehicle = true)
                        audio.playExplosion()
                    }
                }
            }
        }

        // 9. Recall Tower activation progress
        val rTower = _recallTower.value
        if (rTower.isActivating) {
            rTower.activationProgress += dt * 0.4f
            if (rTower.activationProgress >= 1f) {
                rTower.isActivating = false
                rTower.isActivated = true
                _player.update { it.copy(hasRecallRevive = true) }
                updateMissionProgress("m5", 1)
                audio.playRecallBeacon()
                showHint("📡 RECALL TOWER ACTIVATED! Squad Reinforcements Ready!")
            }
        }

        // 10. Check Victory condition
        val aliveBots = _bots.value.count { !it.isDead }
        if (aliveBots == 0 && !p.isDead && !_isVictory.value) {
            _isVictory.value = true
            _battlePoints.update { it + 1000 }
            updateMissionProgress("m7", 1)
            audio.playEnemyEliminated()
            showHint("🏆 WINNER WINNER CHICKEN DINNER! #1 / 50")
        }
    }

    private fun updateBotAI(dt: Float) {
        val p = _player.value
        val now = System.currentTimeMillis()

        for (bot in _bots.value) {
            if (bot.isDead) continue

            val distToPlayer = sqrt((p.x - bot.x).pow(2) + (p.y - bot.y).pow(2))

            // AI states
            if (distToPlayer < 380f && !p.isDead) {
                // Engage player: aim towards player
                val targetAngle = atan2(p.y - bot.y, p.x - bot.x)
                bot.angle = targetAngle

                // Move closer if far, or strafe
                if (distToPlayer > 120f) {
                    bot.x += cos(targetAngle) * (45f * dt)
                    bot.y += sin(targetAngle) * (45f * dt)
                }

                // Shoot at player
                if (now - bot.lastShotTime > 1400L) {
                    bot.lastShotTime = now
                    val spread = (Math.random().toFloat() - 0.5f) * 0.15f
                    val shootAngle = targetAngle + spread
                    val vx = cos(shootAngle) * 12f
                    val vy = sin(shootAngle) * 12f
                    val proj = Projectile(
                        id = nextProjectileId++,
                        x = bot.x + cos(shootAngle) * 18f,
                        y = bot.y + sin(shootAngle) * 18f,
                        vx = vx,
                        vy = vy,
                        damage = bot.weapon.baseDamage * 0.35f, // balanced bot damage
                        isPlayerOwned = false,
                        maxDistance = 450f
                    )
                    _projectiles.update { it + proj }
                }
            } else {
                // Patrol mode towards patrol target
                val distToTarget = sqrt((bot.patrolTargetX - bot.x).pow(2) + (bot.patrolTargetY - bot.y).pow(2))
                if (distToTarget < 30f) {
                    bot.patrolTargetX = (bot.x + (-200..200).random().toFloat()).coerceIn(200f, 2100f)
                    bot.patrolTargetY = (bot.y + (-200..200).random().toFloat()).coerceIn(200f, 2100f)
                } else {
                    val pAngle = atan2(bot.patrolTargetY - bot.y, bot.patrolTargetX - bot.x)
                    bot.angle = pAngle
                    bot.x += cos(pAngle) * (30f * dt)
                    bot.y += sin(pAngle) * (30f * dt)
                }
            }
        }
    }

    private fun updateZoneTimer() {
        val safe = _safeZone.value
        if (safe.isShrinking) {
            safe.currentRadius -= 15f
            if (safe.currentRadius <= safe.targetRadius) {
                safe.isShrinking = false
                safe.phase++
                safe.timeUntilShrinkSec = 40
                safe.targetRadius = (safe.targetRadius * 0.65f).coerceAtLeast(150f)
                showHint("Safe Zone stable. Next circle closing soon!")
            }
        } else {
            safe.timeUntilShrinkSec--
            if (safe.timeUntilShrinkSec <= 0) {
                safe.isShrinking = true
                showHint("⚠️ Playzone is shrinking! Get to the safe circle!")
                audio.playHeavyShot()
            }
        }
        _safeZone.value = safe.copy()
    }

    private fun simulateBotSkirmishes() {
        // Occasionally simulate distant bots taking each other down to match BGMI battle royale feeling
        if (_aliveCount.value > 12 && Math.random() < 0.35) {
            val deadBotsNames = listOf("Player_Ninja", "Vanguard_7", "Sniper_Rex", "Apex_Hawk", "Ghost_Recon", "Predator_X")
            val victim = deadBotsNames.random()
            val killer = listOf("Bot_Shadow", "Viper_Nine", "Falcon_Ghost").random()
            val weapon = listOf("AKM", "M416", "Kar98k", "AWM", "M24").random()

            addKillFeed(killer, victim, weapon, isHeadshot = Math.random() < 0.2)
            _aliveCount.update { (it - 1).coerceAtLeast(2) }
        }
    }

    // Player Actions
    fun movePlayer(dx: Float, dy: Float) {
        val p = _player.value
        if (p.isDead) return

        val speed = when {
            p.inVehicle == VehicleType.BUGGY -> 9.5f
            p.inVehicle == VehicleType.TANKER -> 6.8f
            _isScoped.value -> 2.2f
            else -> 4.5f
        }

        val nx = (p.x + dx * speed).coerceIn(80f, 2320f)
        val ny = (p.y + dy * speed).coerceIn(80f, 2320f)
        val nAngle = if (dx != 0f || dy != 0f) atan2(dy, dx) else p.angle

        _player.update { it.copy(x = nx, y = ny, angle = nAngle) }

        if (p.inVehicle != null && (abs(dx) > 0.1f || abs(dy) > 0.1f)) {
            if (Math.random() < 0.08) {
                audio.playVehicleEngine()
            }
        }

        // Auto pickup close loot if within 35 units
        checkAutoLoot(nx, ny)
    }

    fun aimPlayer(targetX: Float, targetY: Float) {
        val p = _player.value
        val nAngle = atan2(targetY - p.y, targetX - p.x)
        _player.update { it.copy(angle = nAngle) }
    }

    fun fireWeapon() {
        val p = _player.value
        if (p.isDead || p.isReloading) return

        if (p.currentAmmo <= 0) {
            reloadWeapon()
            return
        }

        val weapon = p.selectedWeapon
        val vx = cos(p.angle) * weapon.bulletSpeed
        val vy = sin(p.angle) * weapon.bulletSpeed

        val proj = Projectile(
            id = nextProjectileId++,
            x = p.x + cos(p.angle) * 25f,
            y = p.y + sin(p.angle) * 25f,
            vx = vx,
            vy = vy,
            damage = weapon.baseDamage,
            isPlayerOwned = true,
            maxDistance = weapon.range
        )

        _projectiles.update { it + proj }
        _player.update { it.copy(currentAmmo = it.currentAmmo - 1) }

        if (weapon == WeaponType.AWM) {
            audio.playHeavyShot()
        } else {
            audio.playGunshot()
        }
    }

    fun reloadWeapon() {
        val p = _player.value
        if (p.isReloading || p.currentAmmo == p.selectedWeapon.magSize) return

        viewModelScope.launch {
            _player.update { it.copy(isReloading = true, reloadProgress = 0f) }
            audio.playReload()
            val totalSteps = 10
            val stepDelay = p.selectedWeapon.reloadTimeMs / totalSteps
            for (i in 1..totalSteps) {
                delay(stepDelay)
                _player.update { it.copy(reloadProgress = i / 10f) }
            }
            _player.update {
                it.copy(
                    isReloading = false,
                    reloadProgress = 0f,
                    currentAmmo = it.selectedWeapon.magSize
                )
            }
        }
    }

    fun switchWeapon(weaponType: WeaponType) {
        val p = _player.value
        if (p.selectedWeapon == weaponType) return
        _player.update {
            it.copy(
                selectedWeapon = weaponType,
                currentAmmo = weaponType.magSize,
                isReloading = false
            )
        }
        audio.playReload()
        showHint("Equipped ${weaponType.displayName} (${weaponType.skinName})")
    }

    fun throwGrenade(isSmoke: Boolean = false) {
        val p = _player.value
        if (p.isDead) return

        if (!isSmoke && p.fragGrenades <= 0) {
            showHint("No Frag Grenades left!")
            return
        }
        if (isSmoke && p.smokeGrenades <= 0) {
            showHint("No Smoke Grenades left!")
            return
        }

        val throwDist = 220f
        val targetX = p.x + cos(p.angle) * throwDist
        val targetY = p.y + sin(p.angle) * throwDist

        viewModelScope.launch {
            if (!isSmoke) {
                _player.update { it.copy(fragGrenades = it.fragGrenades - 1) }
            } else {
                _player.update { it.copy(smokeGrenades = it.smokeGrenades - 1) }
            }
            audio.playReload()
            showHint(if (isSmoke) "Smoke Grenade deployed!" else "Frag Grenade thrown! FUSE 2.5s...")

            delay(2200L) // Fuse delay
            // Explosion triggers
            audio.playExplosion()
            val exp = ExplosionEffect(
                id = nextExplosionId++,
                x = targetX,
                y = targetY,
                isSmoke = isSmoke
            )
            _explosions.update { it + exp }

            if (!isSmoke) {
                updateMissionProgress("m6", 1)
                // Damage bots in blast radius
                for (bot in _bots.value) {
                    if (!bot.isDead) {
                        val d = sqrt((targetX - bot.x).pow(2) + (targetY - bot.y).pow(2))
                        if (d < 140f) {
                            val blastDmg = 150f * (1f - (d / 140f))
                            bot.health -= blastDmg
                            if (bot.health <= 0f) {
                                bot.isDead = true
                                onBotEliminated(bot, false, byGrenade = true)
                            }
                        }
                    }
                }
            }
        }
    }

    fun toggleVehicle() {
        val p = _player.value
        if (p.inVehicle != null) {
            // Exit vehicle
            _player.update { it.copy(inVehicle = null) }
            audio.playVehicleEngine()
            showHint("Exited vehicle. On foot mode.")
        } else {
            // Check closest vehicle
            val nearest = _vehicles.value.minByOrNull {
                sqrt((p.x - it.x).pow(2) + (p.y - it.y).pow(2))
            }
            if (nearest != null) {
                val dist = sqrt((p.x - nearest.x).pow(2) + (p.y - nearest.y).pow(2))
                if (dist < 80f) {
                    _player.update { it.copy(inVehicle = nearest.type) }
                    audio.playVehicleEngine()
                    showHint("Driving ${nearest.type.displayName}! Speed boost active.")
                } else {
                    showHint("Too far from vehicle. Move closer!")
                }
            }
        }
    }

    fun triggerRecallTower() {
        val p = _player.value
        val tower = _recallTower.value
        val dist = sqrt((p.x - tower.x).pow(2) + (p.y - tower.y).pow(2))

        if (dist > 120f) {
            showHint("Too far from Recall Tower. Head to the green beacon!")
            return
        }

        if (tower.isActivated) {
            showHint("Recall Tower already used! Reinforcement beacon online.")
            return
        }

        tower.isActivating = true
        _recallTower.value = tower.copy()
        audio.playRecallBeacon()
        showHint("Activating Recall Station! Stand guard...")
    }

    fun summonAirdrop() {
        val p = _player.value
        _airdrop.value = AirdropCrate(
            id = "drop_1",
            x = p.x + 80f,
            y = p.y + 80f,
            altitude = 350f
        )
        audio.playHeavyShot()
        showHint("🟢 Green Flare Fired! Supply Plane dropping Tactical Airdrop!")
    }

    fun lootAirdrop() {
        val p = _player.value
        val drop = _airdrop.value ?: return
        val dist = sqrt((p.x - drop.x).pow(2) + (p.y - drop.y).pow(2))
        if (dist < 80f && drop.hasLanded && !drop.isLooted) {
            drop.isLooted = true
            _player.update {
                it.copy(
                    selectedWeapon = WeaponType.AWM,
                    currentAmmo = 5,
                    totalSniperAmmo = 20,
                    helmetLevel = 3,
                    helmetDurability = 100f,
                    vestLevel = 3,
                    vestDurability = 100f,
                    health = 100f,
                    boost = 100f
                )
            }
            audio.playItemPickup()
            updateMissionProgress("m4", 1)
            showHint("AIRDROP LOOTED! AWM + Lv3 Helmet + Ghillie Suit Equipped!")
        }
    }

    fun useFirstAid() {
        val p = _player.value
        if (p.firstAidKits > 0 && p.health < 75f) {
            _player.update {
                it.copy(
                    firstAidKits = it.firstAidKits - 1,
                    health = 75f
                )
            }
            audio.playItemPickup()
            showHint("Used First Aid Kit. Health restored to 75%!")
        } else if (p.health >= 75f) {
            showHint("Health already above 75%. Drink Energy Drink for full boost!")
        } else {
            showHint("No First Aid Kits available!")
        }
    }

    fun useEnergyDrink() {
        val p = _player.value
        if (p.energyDrinks > 0) {
            val newBoost = (p.boost + 45f).coerceAtMost(100f)
            _player.update {
                it.copy(
                    energyDrinks = it.energyDrinks - 1,
                    boost = newBoost
                )
            }
            audio.playItemPickup()
            showHint("Boost applied! Running speed and HP regeneration increased!")
        } else {
            showHint("No Energy Drinks left!")
        }
    }

    fun toggleScope() {
        _isScoped.update { !it }
        audio.playReload()
    }

    fun setTacticalWaypoint(x: Float, y: Float) {
        _activeWaypoint.value = Pair(x, y)
        showHint("Tactical Waypoint set on Map [${x.toInt()}, ${y.toInt()}]")
        audio.playHitMarker()
    }

    fun sendQuickChat(message: String) {
        showHint("📢 [Squad Voice]: $message")
        audio.playGunshot()
    }

    fun claimMissionReward(missionId: String) {
        val list = _missions.value.map {
            if (it.id == missionId && it.isCompleted && !it.isClaimed) {
                _battlePoints.update { bp -> bp + it.rewardBp }
                it.copy(isClaimed = true)
            } else it
        }
        _missions.value = list
        audio.playEnemyEliminated()
    }

    private fun checkAutoLoot(px: Float, py: Float) {
        val lootList = _loot.value
        var itemPicked = false
        for (item in lootList) {
            if (!item.isCollected) {
                val dist = sqrt((px - item.x).pow(2) + (py - item.y).pow(2))
                if (dist < 38f) {
                    item.isCollected = true
                    itemPicked = true
                    applyLootEffect(item.type)
                }
            }
        }
        if (itemPicked) {
            audio.playItemPickup()
        }
    }

    private fun applyLootEffect(type: LootType) {
        _player.update { p ->
            when (type) {
                LootType.FIRST_AID -> p.copy(firstAidKits = p.firstAidKits + 1)
                LootType.MED_KIT -> p.copy(health = 100f)
                LootType.ENERGY_DRINK -> p.copy(energyDrinks = p.energyDrinks + 1)
                LootType.PAINKILLER -> p.copy(boost = (p.boost + 60f).coerceAtMost(100f))
                LootType.HELMET_LV3 -> p.copy(helmetLevel = 3, helmetDurability = 100f)
                LootType.VEST_LV3 -> p.copy(vestLevel = 3, vestDurability = 100f)
                LootType.AMMO_556 -> p.copy(totalAmmo556 = p.totalAmmo556 + 60)
                LootType.AMMO_762 -> p.copy(totalAmmo762 = p.totalAmmo762 + 60)
                LootType.FRAG_GRENADE -> p.copy(fragGrenades = p.fragGrenades + 1)
                LootType.SMOKE_GRENADE -> p.copy(smokeGrenades = p.smokeGrenades + 1)
                LootType.AIRDROP_FLARE -> {
                    summonAirdrop()
                    p
                }
            }
        }
        showHint("Picked up ${type.displayName}")
    }

    private fun applyDamageToPlayer(rawDamage: Float, source: String) {
        val p = _player.value
        if (p.isDead) return

        // Armor mitigation
        val vestReduction = when (p.vestLevel) {
            3 -> 0.55f
            2 -> 0.40f
            else -> 0.20f
        }
        val reducedDmg = rawDamage * (1f - vestReduction)
        val newHealth = p.health - reducedDmg

        if (newHealth <= 0f) {
            if (p.hasRecallRevive) {
                // Second chance / Recall rescue!
                _player.update {
                    it.copy(
                        health = 60f,
                        hasRecallRevive = false,
                        isKnocked = false
                    )
                }
                audio.playRecallBeacon()
                showHint("⚡ RECALL BEACON RESCUE ACTIVATED! Reinforcements redeployed you!")
            } else {
                _player.update { it.copy(health = 0f, isDead = true) }
                _isGameOver.value = true
                audio.playExplosion()
                showHint("💀 Eliminated by $source. Better Luck Next Time!")
            }
        } else {
            _player.update { it.copy(health = newHealth) }
        }
    }

    private fun onBotEliminated(bot: EnemyBot, isHeadshot: Boolean, byVehicle: Boolean = false, byGrenade: Boolean = false) {
        _player.update {
            it.copy(
                kills = it.kills + 1,
                score = it.score + 150
            )
        }
        _aliveCount.update { (it - 1).coerceAtLeast(1) }
        _battlePoints.update { it + 75 }

        val weaponUsed = when {
            byVehicle -> "Vehicle Roadkill"
            byGrenade -> "Frag Grenade"
            else -> _player.value.selectedWeapon.displayName
        }

        addKillFeed("You", bot.name, weaponUsed, isHeadshot)
        updateMissionProgress("m1", 1)
        audio.playEnemyEliminated()

        showHint("🎯 Eliminated ${bot.name} ${if (isHeadshot) "[HEADSHOT!]" else ""} (+150 PTS)")
    }

    private fun addKillFeed(killer: String, victim: String, weapon: String, isHeadshot: Boolean) {
        val item = KillFeedItem(
            id = System.currentTimeMillis() + (0..999).random(),
            killer = killer,
            victim = victim,
            weapon = weapon,
            isHeadshot = isHeadshot
        )
        val updated = (_killFeed.value + item).takeLast(4)
        _killFeed.value = updated
    }

    private fun updateMissionProgress(missionId: String, amount: Int) {
        val list = _missions.value.map {
            if (it.id == missionId && !it.isCompleted) {
                val newCur = (it.current + amount).coerceAtMost(it.target)
                it.copy(current = newCur)
            } else it
        }
        _missions.value = list
    }

    private fun showHint(hint: String) {
        _currentHint.value = hint
    }
}
