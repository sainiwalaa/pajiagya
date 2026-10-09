package com.example.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.game.model.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun MiniMapRadar(
    player: PlayerState,
    bots: List<EnemyBot>,
    safeZone: SafeZoneState,
    recallTower: RecallTower,
    airdrop: AirdropCrate?,
    vehicles: List<VehicleEntity>,
    activeWaypoint: Pair<Float, Float>?,
    onMapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(105.dp)
            .clip(CircleShape)
            .background(Color(0xCC0F141A))
            .border(2.dp, Color(0xFFFFB800), CircleShape)
            .clickable { onMapClick() }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val radarRadius = size.width / 2f
            val radarCenter = Offset(radarRadius, radarRadius)
            val worldRadarRange = 650f // shows 650 units radius around player

            fun toRadar(wx: Float, wy: Float): Offset? {
                val dx = wx - player.x
                val dy = wy - player.y
                val dist = sqrt(dx * dx + dy * dy)
                if (dist > worldRadarRange) return null
                val scale = radarRadius / worldRadarRange
                return Offset(radarCenter.x + dx * scale, radarCenter.y + dy * scale)
            }

            // Radar grid concentric circles
            drawCircle(
                color = Color(0x334CAF50),
                radius = radarRadius * 0.6f,
                center = radarCenter,
                style = Stroke(width = 1f)
            )

            // Safe Zone Circle on radar
            val safeCenterOffset = toRadar(safeZone.centerX, safeZone.centerY)
            if (safeCenterOffset != null) {
                val safeRadarRadius = (safeZone.currentRadius / worldRadarRange) * radarRadius
                drawCircle(
                    color = Color.White.copy(alpha = 0.7f),
                    radius = safeRadarRadius,
                    center = safeCenterOffset,
                    style = Stroke(width = 1.5f)
                )
            }

            // Waypoint on radar
            activeWaypoint?.let { (wx, wy) ->
                toRadar(wx, wy)?.let { wOffset ->
                    drawCircle(
                        color = Color(0xFFFFD700),
                        radius = 4f,
                        center = wOffset
                    )
                }
            }

            // Vehicles on radar
            for (v in vehicles) {
                toRadar(v.x, v.y)?.let { vPos ->
                    drawCircle(
                        color = Color(0xFFFF9800),
                        radius = 3.5f,
                        center = vPos
                    )
                }
            }

            // Recall Tower on radar
            toRadar(recallTower.x, recallTower.y)?.let { rPos ->
                drawCircle(
                    color = Color(0xFF00E676),
                    radius = 4.5f,
                    center = rPos
                )
            }

            // Airdrop on radar
            airdrop?.let { drop ->
                toRadar(drop.x, drop.y)?.let { aPos ->
                    drawCircle(
                        color = Color(0xFFFF1744),
                        radius = 5f,
                        center = aPos
                    )
                }
            }

            // Nearby Bots radar blips (gunfire/steps)
            for (bot in bots) {
                if (!bot.isDead) {
                    toRadar(bot.x, bot.y)?.let { bPos ->
                        drawCircle(
                            color = Color(0xFFFF3D00),
                            radius = 3.5f,
                            center = bPos
                        )
                    }
                }
            }

            // Player Icon (Center Arrow)
            rotate(degrees = player.angle * 180f / PI.toFloat(), pivot = radarCenter) {
                // Arrow
                drawLine(
                    color = Color(0xFF00E5FF),
                    start = Offset(radarCenter.x, radarCenter.y),
                    end = Offset(radarCenter.x + 8f, radarCenter.y),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = Color(0xFF00E5FF),
                    radius = 4.5f,
                    center = radarCenter
                )
            }
        }
    }
}
