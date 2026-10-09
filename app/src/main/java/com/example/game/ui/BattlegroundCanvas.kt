package com.example.game.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import com.example.game.model.*
import kotlin.math.*

@Composable
fun BattlegroundCanvas(
    player: PlayerState,
    bots: List<EnemyBot>,
    buildings: List<Building>,
    vehicles: List<VehicleEntity>,
    loot: List<LootEntity>,
    projectiles: List<Projectile>,
    explosions: List<ExplosionEffect>,
    airdrop: AirdropCrate?,
    recallTower: RecallTower,
    safeZone: SafeZoneState,
    activeWaypoint: Pair<Float, Float>?,
    isScoped: Boolean,
    onAimTouch: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulsing animation for recall beacon and safe zone
    val infiniteTransition = rememberInfiniteTransition(label = "battle_anim")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        // Calculate world coordinates from screen offset
                        // Camera center is screen center
                        val screenCenterX = size.width / 2f
                        val screenCenterY = size.height / 2f
                        val zoom = if (isScoped) 1.35f else 1.0f
                        val worldX = player.x + (offset.x - screenCenterX) / zoom
                        val worldY = player.y + (offset.y - screenCenterY) / zoom
                        onAimTouch(worldX, worldY)
                    },
                    onDrag = { change, _ ->
                        val screenCenterX = size.width / 2f
                        val screenCenterY = size.height / 2f
                        val zoom = if (isScoped) 1.35f else 1.0f
                        val worldX = player.x + (change.position.x - screenCenterX) / zoom
                        val worldY = player.y + (change.position.y - screenCenterY) / zoom
                        onAimTouch(worldX, worldY)
                    }
                )
            }
    ) {
        val screenWidth = size.width
        val screenHeight = size.height
        val zoom = if (isScoped) 1.35f else 1.0f

        // Center camera on player
        val cameraX = player.x - (screenWidth / 2f) / zoom
        val cameraY = player.y - (screenHeight / 2f) / zoom

        fun toScreen(worldX: Float, worldY: Float): Offset {
            return Offset(
                (worldX - cameraX) * zoom,
                (worldY - cameraY) * zoom
            )
        }

        fun toScreenDist(dist: Float): Float = dist * zoom

        // 1. Draw Battlefield Terrain
        drawRect(Color(0xFF1B2A1E)) // Dark tactical foliage / grass

        // Draw tactical roads
        val roadY = 700f
        val roadScreenStart = toScreen(0f, roadY)
        val roadScreenEnd = toScreen(2400f, roadY)
        drawLine(
            color = Color(0xFF37474F),
            start = roadScreenStart,
            end = roadScreenEnd,
            strokeWidth = toScreenDist(80f)
        )
        // Road dashed center line
        drawLine(
            color = Color(0xFFFFD54F),
            start = roadScreenStart,
            end = roadScreenEnd,
            strokeWidth = toScreenDist(4f),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f * zoom, 30f * zoom), 0f)
        )

        // Draw vertical road
        val roadX = 1100f
        val vRoadStart = toScreen(roadX, 0f)
        val vRoadEnd = toScreen(roadX, 2400f)
        drawLine(
            color = Color(0xFF37474F),
            start = vRoadStart,
            end = vRoadEnd,
            strokeWidth = toScreenDist(80f)
        )
        drawLine(
            color = Color(0xFFFFD54F),
            start = vRoadStart,
            end = vRoadEnd,
            strokeWidth = toScreenDist(4f),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f * zoom, 30f * zoom), 0f)
        )

        // Draw tactical rocks / cover in field
        val rocks = listOf(
            Pair(300f, 400f), Pair(750f, 250f), Pair(980f, 650f),
            Pair(1400f, 850f), Pair(1600f, 400f), Pair(600f, 1200f),
            Pair(1050f, 1450f), Pair(1450f, 1300f), Pair(400f, 850f)
        )
        for ((rx, ry) in rocks) {
            val rCenter = toScreen(rx, ry)
            drawCircle(
                color = Color(0xFF455A64),
                radius = toScreenDist(22f),
                center = rCenter
            )
            drawCircle(
                color = Color(0xFF263238),
                radius = toScreenDist(18f),
                center = Offset(rCenter.x + 3f * zoom, rCenter.y + 3f * zoom)
            )
        }

        // Draw decorative trees
        val trees = listOf(
            Pair(200f, 600f), Pair(400f, 750f), Pair(900f, 850f),
            Pair(1300f, 1050f), Pair(650f, 450f), Pair(1550f, 650f),
            Pair(850f, 1300f), Pair(1250f, 1500f)
        )
        for ((tx, ty) in trees) {
            val tCenter = toScreen(tx, ty)
            drawCircle(
                color = Color(0xFF1B5E20),
                radius = toScreenDist(32f),
                center = tCenter
            )
            drawCircle(
                color = Color(0xFF2E7D32),
                radius = toScreenDist(24f),
                center = tCenter
            )
        }

        // 2. Draw Safe Zone & Play Zone Rings
        val safeCenter = toScreen(safeZone.centerX, safeZone.centerY)
        val safeRadius = toScreenDist(safeZone.currentRadius)

        // White Safe Zone Circle
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = safeRadius,
            center = safeCenter,
            style = Stroke(
                width = 3.5f * zoom,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f * zoom, 12f * zoom), 0f)
            )
        )

        // Target Inner Circle (where next safe zone shrinks)
        val targetRadius = toScreenDist(safeZone.targetRadius)
        drawCircle(
            color = Color(0xFFFFD54F).copy(alpha = 0.6f),
            radius = targetRadius,
            center = safeCenter,
            style = Stroke(width = 2f * zoom)
        )

        // Pulsing Blue Zone Hazard Ring
        drawCircle(
            color = Color(0xFF00E5FF).copy(alpha = 0.25f + pulseAnim * 0.15f),
            radius = safeRadius + (pulseAnim * 25f * zoom),
            center = safeCenter,
            style = Stroke(width = 6f * zoom)
        )

        // 3. Draw Buildings (Compounds)
        for (b in buildings) {
            val bTopLeft = toScreen(b.x, b.y)
            val bWidth = toScreenDist(b.width)
            val bHeight = toScreenDist(b.height)

            val isPlayerInsideThisBuilding = player.isInsideBuilding && b.contains(player.x, player.y)

            // Building Floor
            drawRect(
                color = b.floorColor,
                topLeft = bTopLeft,
                size = Size(bWidth, bHeight)
            )

            // Floor grid tiles inside
            val tileSize = toScreenDist(30f)
            var curX = bTopLeft.x
            while (curX < bTopLeft.x + bWidth) {
                drawLine(
                    color = Color(0xFF2C3E50).copy(alpha = 0.4f),
                    start = Offset(curX, bTopLeft.y),
                    end = Offset(curX, bTopLeft.y + bHeight),
                    strokeWidth = 1f
                )
                curX += tileSize
            }

            // Outside Roof vs Inside Transparent Roof
            val roofAlpha = if (isPlayerInsideThisBuilding) 0.18f else 0.95f
            drawRect(
                color = b.roofColor.copy(alpha = roofAlpha),
                topLeft = bTopLeft,
                size = Size(bWidth, bHeight)
            )

            // Building Outer Walls
            drawRect(
                color = Color(0xFF78909C),
                topLeft = bTopLeft,
                size = Size(bWidth, bHeight),
                style = Stroke(width = toScreenDist(8f))
            )

            // Doorway gap
            val doorWidth = toScreenDist(40f)
            val doorX = toScreen(b.entranceX - 20f, b.entranceY).x
            drawRect(
                color = Color(0xFF1B2A1E), // matching terrain floor gap
                topLeft = Offset(doorX, bTopLeft.y + bHeight - toScreenDist(8f)),
                size = Size(doorWidth, toScreenDist(10f))
            )
        }

        // 4. Draw Recall Tower (Station)
        val recallPos = toScreen(recallTower.x, recallTower.y)
        // Pulsing green beacon beam
        val beaconRadius = toScreenDist(35f + pulseAnim * 35f)
        drawCircle(
            color = Color(0xFF00E676).copy(alpha = (1f - pulseAnim) * 0.6f),
            radius = beaconRadius,
            center = recallPos
        )
        // Concrete station base
        drawCircle(
            color = Color(0xFF263238),
            radius = toScreenDist(26f),
            center = recallPos
        )
        // Green transmitter light
        drawCircle(
            color = if (recallTower.isActivated) Color(0xFF00E676) else Color(0xFF76FF03),
            radius = toScreenDist(14f),
            center = recallPos
        )
        // Tower Antenna line
        drawLine(
            color = Color(0xFFFFD54F),
            start = Offset(recallPos.x, recallPos.y - toScreenDist(22f)),
            end = Offset(recallPos.x, recallPos.y + toScreenDist(22f)),
            strokeWidth = 3f * zoom
        )

        // 5. Draw Vehicles
        for (v in vehicles) {
            if (v.isOccupied) continue
            val vCenter = toScreen(v.x, v.y)
            val vLength = toScreenDist(42f)
            val vWidth = toScreenDist(24f)

            rotate(degrees = v.angle * 180f / PI.toFloat(), pivot = vCenter) {
                // Vehicle Body
                drawRoundRect(
                    color = v.type.color,
                    topLeft = Offset(vCenter.x - vLength / 2f, vCenter.y - vWidth / 2f),
                    size = Size(vLength, vWidth),
                    cornerRadius = CornerRadius(6f * zoom, 6f * zoom)
                )
                // Windshield / Cage
                drawRect(
                    color = Color(0xFF102027),
                    topLeft = Offset(vCenter.x - vLength * 0.1f, vCenter.y - vWidth * 0.35f),
                    size = Size(vLength * 0.4f, vWidth * 0.7f)
                )
                // Tires
                val tireLen = toScreenDist(10f)
                val tireWid = toScreenDist(5f)
                val wheelOffsets = listOf(
                    Offset(-vLength * 0.35f, -vWidth * 0.55f),
                    Offset(vLength * 0.35f, -vWidth * 0.55f),
                    Offset(-vLength * 0.35f, vWidth * 0.55f - tireWid),
                    Offset(vLength * 0.35f, vWidth * 0.55f - tireWid)
                )
                for (wo in wheelOffsets) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(vCenter.x + wo.x, vCenter.y + wo.y),
                        size = Size(tireLen, tireWid)
                    )
                }
            }
        }

        // 6. Draw Loot Items
        for (item in loot) {
            if (item.isCollected) continue
            val itemCenter = toScreen(item.x, item.y)

            // Subtle glowing circle underneath
            drawCircle(
                color = item.type.color.copy(alpha = 0.3f),
                radius = toScreenDist(16f),
                center = itemCenter
            )
            // Item box
            drawCircle(
                color = item.type.color,
                radius = toScreenDist(9f),
                center = itemCenter
            )
            // White highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = toScreenDist(4f),
                center = Offset(itemCenter.x - 2f * zoom, itemCenter.y - 2f * zoom)
            )
        }

        // 7. Draw Airdrop Crate
        airdrop?.let { drop ->
            val dropCenter = toScreen(drop.x, drop.y)
            val crateSize = toScreenDist(32f)

            if (!drop.hasLanded) {
                // Parachute canopy above
                val parachuteY = dropCenter.y - toScreenDist(drop.altitude + 35f)
                drawArc(
                    color = Color(0xFF388E3C),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(dropCenter.x - toScreenDist(30f), parachuteY - toScreenDist(20f)),
                    size = Size(toScreenDist(60f), toScreenDist(40f))
                )
                // Parachute lines
                drawLine(
                    color = Color.White.copy(alpha = 0.7f),
                    start = Offset(dropCenter.x - toScreenDist(25f), parachuteY),
                    end = Offset(dropCenter.x, dropCenter.y - toScreenDist(drop.altitude)),
                    strokeWidth = 1.5f
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.7f),
                    start = Offset(dropCenter.x + toScreenDist(25f), parachuteY),
                    end = Offset(dropCenter.x, dropCenter.y - toScreenDist(drop.altitude)),
                    strokeWidth = 1.5f
                )
            } else {
                // Red smoke effect rising
                val smokeOffset = (pulseAnim * 40f) * zoom
                drawCircle(
                    color = Color(0xFFFF1744).copy(alpha = (1f - pulseAnim) * 0.45f),
                    radius = toScreenDist(22f + pulseAnim * 20f),
                    center = Offset(dropCenter.x, dropCenter.y - smokeOffset)
                )
            }

            // The iconic red-and-blue airdrop crate
            val crateTopLeft = Offset(
                dropCenter.x - crateSize / 2f,
                dropCenter.y - crateSize / 2f - toScreenDist(drop.altitude)
            )
            // Blue tarp top
            drawRect(
                color = Color(0xFF1976D2),
                topLeft = crateTopLeft,
                size = Size(crateSize, crateSize * 0.35f)
            )
            // Red container bottom
            drawRect(
                color = Color(0xFFD32F2F),
                topLeft = Offset(crateTopLeft.x, crateTopLeft.y + crateSize * 0.35f),
                size = Size(crateSize, crateSize * 0.65f)
            )
            // Straps
            drawRect(
                color = Color.Black.copy(alpha = 0.6f),
                topLeft = crateTopLeft,
                size = Size(crateSize, crateSize),
                style = Stroke(width = 2f * zoom)
            )
        }

        // 8. Draw Enemy Bots
        for (bot in bots) {
            if (bot.isDead) {
                // Loot Crate on death
                val deadPos = toScreen(bot.x, bot.y)
                drawRect(
                    color = Color(0xFF8D6E63),
                    topLeft = Offset(deadPos.x - toScreenDist(10f), deadPos.y - toScreenDist(10f)),
                    size = Size(toScreenDist(20f), toScreenDist(20f))
                )
                // Green cross on crate
                drawLine(
                    color = Color(0xFF76FF03),
                    start = Offset(deadPos.x - toScreenDist(6f), deadPos.y),
                    end = Offset(deadPos.x + toScreenDist(6f), deadPos.y),
                    strokeWidth = 2f * zoom
                )
                drawLine(
                    color = Color(0xFF76FF03),
                    start = Offset(deadPos.x, deadPos.y - toScreenDist(6f)),
                    end = Offset(deadPos.x, deadPos.y + toScreenDist(6f)),
                    strokeWidth = 2f * zoom
                )
                continue
            }

            val botPos = toScreen(bot.x, bot.y)

            // Health bar above bot head
            val hpBarWidth = toScreenDist(32f)
            val hpBarHeight = toScreenDist(4f)
            val hpPercent = (bot.health / bot.maxHealth).coerceIn(0f, 1f)
            val hpBarTopLeft = Offset(botPos.x - hpBarWidth / 2f, botPos.y - toScreenDist(26f))

            drawRect(
                color = Color.Black.copy(alpha = 0.8f),
                topLeft = hpBarTopLeft,
                size = Size(hpBarWidth, hpBarHeight)
            )
            drawRect(
                color = if (hpPercent > 0.4f) Color(0xFFFF5252) else Color(0xFFFF1744),
                topLeft = hpBarTopLeft,
                size = Size(hpBarWidth * hpPercent, hpBarHeight)
            )

            // Rotate bot facing direction
            rotate(degrees = bot.angle * 180f / PI.toFloat(), pivot = botPos) {
                // Gun barrel
                drawLine(
                    color = Color(0xFF263238),
                    start = botPos,
                    end = Offset(botPos.x + toScreenDist(22f), botPos.y),
                    strokeWidth = 3.5f * zoom
                )
                // Bot Body
                drawCircle(
                    color = bot.color,
                    radius = toScreenDist(14f),
                    center = botPos
                )
                // Helmet
                drawCircle(
                    color = Color(0xFF37474F),
                    radius = toScreenDist(9f),
                    center = botPos
                )
            }
        }

        // 9. Draw Projectiles (Bullets)
        for (proj in projectiles) {
            val pStart = toScreen(proj.x, proj.y)
            val pEnd = Offset(
                pStart.x + proj.vx * 1.5f * zoom,
                pStart.y + proj.vy * 1.5f * zoom
            )
            val bulletColor = if (proj.isPlayerOwned) Color(0xFFFFD54F) else Color(0xFFFF5252)
            drawLine(
                color = bulletColor,
                start = pStart,
                end = pEnd,
                strokeWidth = 3f * zoom,
                cap = StrokeCap.Round
            )
        }

        // 10. Draw Explosions
        for (exp in explosions) {
            val expPos = toScreen(exp.x, exp.y)
            val expRadius = toScreenDist(exp.currentRadius)

            if (!exp.isSmoke) {
                // Fire ring
                drawCircle(
                    color = Color(0xFFFF6D00).copy(alpha = exp.alpha * 0.8f),
                    radius = expRadius,
                    center = expPos
                )
                drawCircle(
                    color = Color(0xFFFFD600).copy(alpha = exp.alpha),
                    radius = expRadius * 0.6f,
                    center = expPos
                )
                drawCircle(
                    color = Color.White.copy(alpha = exp.alpha),
                    radius = expRadius * 0.3f,
                    center = expPos
                )
            } else {
                // Smoke cloud
                drawCircle(
                    color = Color(0xFFECEFF1).copy(alpha = exp.alpha * 0.7f),
                    radius = expRadius,
                    center = expPos
                )
            }
        }

        // 11. Draw Waypoint Ping (if set by user on tactical map)
        activeWaypoint?.let { (wx, wy) ->
            val wpPos = toScreen(wx, wy)
            drawCircle(
                color = Color(0xFFFFD700).copy(alpha = (1f - pulseAnim) * 0.8f),
                radius = toScreenDist(30f + pulseAnim * 20f),
                center = wpPos,
                style = Stroke(width = 3f * zoom)
            )
            drawCircle(
                color = Color(0xFFFFD700),
                radius = toScreenDist(10f),
                center = wpPos
            )
        }

        // 12. Draw Player Character
        val playerPos = toScreen(player.x, player.y)

        // Aim laser guide
        val laserEnd = Offset(
            playerPos.x + cos(player.angle) * toScreenDist(player.selectedWeapon.range),
            playerPos.y + sin(player.angle) * toScreenDist(player.selectedWeapon.range)
        )
        drawLine(
            color = player.selectedWeapon.skinColor.copy(alpha = 0.35f),
            start = playerPos,
            end = laserEnd,
            strokeWidth = 1.5f * zoom,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f * zoom, 12f * zoom), 0f)
        )

        rotate(degrees = player.angle * 180f / PI.toFloat(), pivot = playerPos) {
            // If driving vehicle, render vehicle frame around player
            if (player.inVehicle != null) {
                val vLen = toScreenDist(52f)
                val vWid = toScreenDist(30f)
                drawRoundRect(
                    color = player.inVehicle.color,
                    topLeft = Offset(playerPos.x - vLen / 2f, playerPos.y - vWid / 2f),
                    size = Size(vLen, vWid),
                    cornerRadius = CornerRadius(8f * zoom, 8f * zoom)
                )
                // Boost exhaust flame
                if (pulseAnim > 0.4f) {
                    drawCircle(
                        color = Color(0xFFFF9100),
                        radius = toScreenDist(7f),
                        center = Offset(playerPos.x - vLen / 2f, playerPos.y)
                    )
                }
            }

            // Gun Barrel with stylish skin color!
            val gunLength = toScreenDist(26f)
            drawLine(
                color = player.selectedWeapon.skinColor,
                start = Offset(playerPos.x + toScreenDist(6f), playerPos.y + toScreenDist(6f)),
                end = Offset(playerPos.x + gunLength, playerPos.y + toScreenDist(6f)),
                strokeWidth = 4f * zoom,
                cap = StrokeCap.Round
            )

            // Player Shoulders & Body
            drawCircle(
                color = Color(0xFF2962FF), // Military blue uniform
                radius = toScreenDist(16f),
                center = playerPos
            )

            // Military Vest
            drawCircle(
                color = Color(0xFF37474F),
                radius = toScreenDist(12f),
                center = playerPos
            )

            // Iconic Level 3 Helmet with Golden Visor
            drawCircle(
                color = Color(0xFF1E232A),
                radius = toScreenDist(9f),
                center = playerPos
            )
            // Golden Visor line
            drawLine(
                color = Color(0xFFFFD700),
                start = Offset(playerPos.x + toScreenDist(3f), playerPos.y - toScreenDist(4f)),
                end = Offset(playerPos.x + toScreenDist(9f), playerPos.y - toScreenDist(1f)),
                strokeWidth = 3f * zoom,
                cap = StrokeCap.Round
            )
        }
    }
}
