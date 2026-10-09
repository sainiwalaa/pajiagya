package com.example.game.model

import androidx.compose.ui.graphics.Color
import kotlin.math.sqrt

enum class WeaponType(
    val displayName: String,
    val ammoType: String,
    val baseDamage: Float,
    val fireRateMs: Long,
    val magSize: Int,
    val reloadTimeMs: Long,
    val range: Float,
    val bulletSpeed: Float,
    val skinName: String,
    val skinColor: Color
) {
    M416(
        displayName = "M416 Glacier",
        ammoType = "5.56mm",
        baseDamage = 41f,
        fireRateMs = 120L,
        magSize = 30,
        reloadTimeMs = 1800L,
        range = 420f,
        bulletSpeed = 16f,
        skinName = "Glacier Ice Gold",
        skinColor = Color(0xFF00E5FF)
    ),
    AKM(
        displayName = "AKM Dragonfire",
        ammoType = "7.62mm",
        baseDamage = 49f,
        fireRateMs = 160L,
        magSize = 30,
        reloadTimeMs = 2100L,
        range = 380f,
        bulletSpeed = 14f,
        skinName = "Hellfire Crimson",
        skinColor = Color(0xFFFF3D00)
    ),
    AWM(
        displayName = "AWM Monster",
        ammoType = ".300 Magnum",
        baseDamage = 105f,
        fireRateMs = 1100L,
        magSize = 5,
        reloadTimeMs = 2500L,
        range = 650f,
        bulletSpeed = 24f,
        skinName = "Godzilla Ghillie",
        skinColor = Color(0xFF76FF03)
    ),
    UZI(
        displayName = "Micro UZI Cyber",
        ammoType = "9mm",
        baseDamage = 26f,
        fireRateMs = 80L,
        magSize = 25,
        reloadTimeMs = 1400L,
        range = 280f,
        bulletSpeed = 15f,
        skinName = "Cyberpunk Pulse",
        skinColor = Color(0xFFE040FB)
    ),
    PAN(
        displayName = "Cast-Iron Pan",
        ammoType = "Melee",
        baseDamage = 100f,
        fireRateMs = 450L,
        magSize = 1,
        reloadTimeMs = 0L,
        range = 60f,
        bulletSpeed = 0f,
        skinName = "Golden Winner",
        skinColor = Color(0xFFFFD700)
    )
}

enum class VehicleType(
    val displayName: String,
    val maxSpeed: Float,
    val maxHealth: Float,
    val color: Color
) {
    BUGGY(
        displayName = "Tactical Buggy",
        maxSpeed = 9.5f,
        maxHealth = 400f,
        color = Color(0xFFFF9800)
    ),
    TANKER(
        displayName = "Heavy Armored UAZ",
        maxSpeed = 6.8f,
        maxHealth = 850f,
        color = Color(0xFF455A64)
    )
}

enum class LootType(val displayName: String, val color: Color) {
    FIRST_AID("First Aid Kit", Color(0xFF4CAF50)),
    MED_KIT("Full Med Kit", Color(0xFF00E676)),
    ENERGY_DRINK("Energy Drink", Color(0xFF00B0FF)),
    PAINKILLER("Painkillers", Color(0xFFFFC107)),
    HELMET_LV3("Level 3 Helmet", Color(0xFFFFD700)),
    VEST_LV3("Level 3 Vest", Color(0xFF90A4AE)),
    AMMO_556("5.56mm Ammo (x60)", Color(0xFF81C784)),
    AMMO_762("7.62mm Ammo (x60)", Color(0xFFFF8A65)),
    FRAG_GRENADE("Frag Grenade", Color(0xFFFF5252)),
    SMOKE_GRENADE("Smoke Grenade", Color(0xFFB0BEC5)),
    AIRDROP_FLARE("Green Flare Gun", Color(0xFF69F0AE))
}

data class PlayerState(
    val x: Float = 500f,
    val y: Float = 500f,
    val angle: Float = 0f, // radians
    val health: Float = 100f,
    val maxHealth: Float = 100f,
    val boost: Float = 30f,
    val helmetLevel: Int = 2,
    val helmetDurability: Float = 80f,
    val vestLevel: Int = 2,
    val vestDurability: Float = 80f,
    val selectedWeapon: WeaponType = WeaponType.M416,
    val secondaryWeapon: WeaponType = WeaponType.AKM,
    val currentAmmo: Int = 30,
    val totalAmmo556: Int = 180,
    val totalAmmo762: Int = 120,
    val totalSniperAmmo: Int = 15,
    val fragGrenades: Int = 3,
    val smokeGrenades: Int = 2,
    val firstAidKits: Int = 4,
    val energyDrinks: Int = 3,
    val kills: Int = 0,
    val score: Int = 0,
    val isKnocked: Boolean = false,
    val isDead: Boolean = false,
    val isReloading: Boolean = false,
    val reloadProgress: Float = 0f,
    val inVehicle: VehicleType? = null,
    val isInsideBuilding: Boolean = false,
    val activeBuildingName: String? = null,
    val hasRecallRevive: Boolean = true
) {
    fun distanceTo(ox: Float, oy: Float): Float {
        val dx = x - ox
        val dy = y - oy
        return sqrt(dx * dx + dy * dy)
    }
}

data class EnemyBot(
    val id: Int,
    val name: String,
    var x: Float,
    var y: Float,
    var angle: Float = 0f,
    var health: Float = 100f,
    val maxHealth: Float = 100f,
    val weapon: WeaponType = WeaponType.AKM,
    var isDead: Boolean = false,
    var lastShotTime: Long = 0L,
    var patrolTargetX: Float = 0f,
    var patrolTargetY: Float = 0f,
    val color: Color = Color(0xFFFF5252)
)

data class Building(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val color: Color = Color(0xFF2C3440),
    val roofColor: Color = Color(0xFF3F4A59),
    val floorColor: Color = Color(0xFF1E242B),
    val entranceX: Float = x + width / 2f,
    val entranceY: Float = y + height
) {
    fun contains(px: Float, py: Float): Boolean {
        return px in x..(x + width) && py in y..(y + height)
    }
}

data class VehicleEntity(
    val id: String,
    val type: VehicleType,
    var x: Float,
    var y: Float,
    var angle: Float = 0f,
    var health: Float = type.maxHealth,
    var isOccupied: Boolean = false
)

data class LootEntity(
    val id: String,
    val type: LootType,
    val x: Float,
    val y: Float,
    var isCollected: Boolean = false
)

data class Projectile(
    val id: Long,
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val damage: Float,
    val isPlayerOwned: Boolean,
    val maxDistance: Float,
    var traveledDistance: Float = 0f
)

data class GrenadeEntity(
    val id: Long,
    var x: Float,
    var y: Float,
    val targetX: Float,
    val targetY: Float,
    var progress: Float = 0f,
    val isSmoke: Boolean = false
)

data class ExplosionEffect(
    val id: Long,
    val x: Float,
    val y: Float,
    val maxRadius: Float = 120f,
    var currentRadius: Float = 10f,
    var alpha: Float = 1f,
    val isSmoke: Boolean = false
)

data class AirdropCrate(
    val id: String,
    var x: Float,
    var y: Float,
    var altitude: Float = 300f, // drops from sky
    var hasLanded: Boolean = false,
    var isLooted: Boolean = false
)

data class RecallTower(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    var isActivated: Boolean = false,
    var isActivating: Boolean = false,
    var activationProgress: Float = 0f
)

data class SafeZoneState(
    var centerX: Float = 1200f,
    var centerY: Float = 1200f,
    var currentRadius: Float = 1100f,
    var targetRadius: Float = 600f,
    var phase: Int = 1,
    var timeUntilShrinkSec: Int = 45,
    var isShrinking: Boolean = false
)

data class KillFeedItem(
    val id: Long,
    val killer: String,
    val victim: String,
    val weapon: String,
    val isHeadshot: Boolean = false
)

data class BattleMission(
    val id: String,
    val title: String,
    val description: String,
    val current: Int,
    val target: Int,
    val rewardBp: Int,
    val isClaimed: Boolean = false
) {
    val isCompleted: Boolean get() = current >= target
}

data class SquadMember(
    val id: String,
    val name: String,
    val role: String, // "IGL", "Assaulter", "Sniper", "Support"
    val rank: String, // "Crown I", "Ace Dominator", "Conqueror"
    val avatarId: Int,
    val isReady: Boolean = true,
    val kills: Int = 0,
    val isLeader: Boolean = false
)

data class SquadTeam(
    val clanTag: String = "BGMI",
    val teamName: String = "Saini Warriors",
    val members: List<SquadMember> = listOf(
        SquadMember("1", "Saini_Commander", "IGL", "Ace Dominator", 1, true, 8, true),
        SquadMember("2", "Falcon_Ghost", "Assaulter", "Crown I", 2, true, 5, false),
        SquadMember("3", "Sniper_Viper", "Sniper", "Crown II", 3, true, 4, false),
        SquadMember("4", "Delta_Medic", "Support", "Diamond I", 4, true, 2, false)
    )
)
