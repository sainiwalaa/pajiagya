package com.example.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.model.*
import com.example.ui.theme.BgmiGold
import com.example.ui.theme.BgmiSurface
import com.example.ui.theme.BgmiSurfaceBorder

@Composable
fun FullMapDialog(
    player: PlayerState,
    buildings: List<Building>,
    safeZone: SafeZoneState,
    recallTower: RecallTower,
    airdrop: AirdropCrate?,
    activeWaypoint: Pair<Float, Float>?,
    onSetWaypoint: (Float, Float) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, BgmiGold, RoundedCornerShape(16.dp))
                .testTag("full_map_dialog"),
            color = BgmiSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Map Location",
                            tint = BgmiGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ERANGEL TACTICAL MAP",
                            color = BgmiGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_map_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Map",
                            tint = Color.White
                        )
                    }
                }

                Text(
                    text = "Tap anywhere on the map to set a tactical squad waypoint mark.",
                    color = Color(0xFFB0BEC5),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Map Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, BgmiSurfaceBorder, RoundedCornerShape(8.dp))
                        .background(Color(0xFF141F16))
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures { tapOffset ->
                                    val worldWidth = 2400f
                                    val worldHeight = 2400f
                                    val wx = (tapOffset.x / size.width) * worldWidth
                                    val wy = (tapOffset.y / size.height) * worldHeight
                                    onSetWaypoint(wx, wy)
                                }
                            }
                    ) {
                        val mapW = size.width
                        val mapH = size.height
                        val worldScaleX = mapW / 2400f
                        val worldScaleY = mapH / 2400f

                        fun toScreen(wx: Float, wy: Float): Offset {
                            return Offset(wx * worldScaleX, wy * worldScaleY)
                        }

                        // Grid lines
                        for (i in 1..7) {
                            val lineX = i * (mapW / 8f)
                            drawLine(
                                color = Color(0x22FFFFFF),
                                start = Offset(lineX, 0f),
                                end = Offset(lineX, mapH),
                                strokeWidth = 1f
                            )
                            val lineY = i * (mapH / 8f)
                            drawLine(
                                color = Color(0x22FFFFFF),
                                start = Offset(0f, lineY),
                                end = Offset(mapW, lineY),
                                strokeWidth = 1f
                            )
                        }

                        // Compounds
                        for (b in buildings) {
                            val bOffset = toScreen(b.x, b.y)
                            drawRect(
                                color = Color(0xFF455A64),
                                topLeft = bOffset,
                                size = Size(b.width * worldScaleX, b.height * worldScaleY)
                            )
                        }

                        // Safe Zone
                        val safePos = toScreen(safeZone.centerX, safeZone.centerY)
                        val safeRad = safeZone.currentRadius * worldScaleX
                        drawCircle(
                            color = Color.White.copy(alpha = 0.85f),
                            radius = safeRad,
                            center = safePos,
                            style = Stroke(width = 2f)
                        )
                        // Target Zone
                        val targetRad = safeZone.targetRadius * worldScaleX
                        drawCircle(
                            color = Color(0xFFFFD54F),
                            radius = targetRad,
                            center = safePos,
                            style = Stroke(width = 1.5f)
                        )

                        // Recall Tower
                        val recallPos = toScreen(recallTower.x, recallTower.y)
                        drawCircle(
                            color = Color(0xFF00E676),
                            radius = 6f,
                            center = recallPos
                        )

                        // Airdrop
                        airdrop?.let { drop ->
                            val dropPos = toScreen(drop.x, drop.y)
                            drawCircle(
                                color = Color(0xFFFF1744),
                                radius = 7f,
                                center = dropPos
                            )
                        }

                        // Waypoint
                        activeWaypoint?.let { (wx, wy) ->
                            val wPos = toScreen(wx, wy)
                            drawCircle(
                                color = Color(0xFFFFD700),
                                radius = 9f,
                                center = wPos
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 4f,
                                center = wPos
                            )
                        }

                        // Player Position (Blue Star)
                        val pPos = toScreen(player.x, player.y)
                        drawCircle(
                            color = Color(0xFF00E5FF),
                            radius = 6f,
                            center = pPos
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.5f,
                            center = pPos
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Map Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    LegendItem("You", Color(0xFF00E5FF))
                    LegendItem("Safe Zone", Color.White)
                    LegendItem("Recall Beacon", Color(0xFF00E676))
                    LegendItem("Airdrop", Color(0xFFFF1744))
                    LegendItem("Waypoint", Color(0xFFFFD700))
                }
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, color = Color.White, fontSize = 11.sp)
    }
}
