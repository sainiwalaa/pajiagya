package com.example.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.*
import com.example.ui.theme.*
import kotlin.math.*

@Composable
fun BattleHud(
    player: PlayerState,
    aliveCount: Int,
    safeZone: SafeZoneState,
    killFeed: List<KillFeedItem>,
    currentHint: String,
    isScoped: Boolean,
    nearVehicle: Boolean,
    nearRecallTower: Boolean,
    nearAirdrop: Boolean,
    onMoveJoystick: (Float, Float) -> Unit,
    onFire: () -> Unit,
    onReload: () -> Unit,
    onSwitchWeapon: (WeaponType) -> Unit,
    onThrowGrenade: (Boolean) -> Unit,
    onToggleVehicle: () -> Unit,
    onTriggerRecall: () -> Unit,
    onLootAirdrop: () -> Unit,
    onUseFirstAid: () -> Unit,
    onUseEnergyDrink: () -> Unit,
    onToggleScope: () -> Unit,
    onQuickChat: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showQuickChatMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        // TOP BAR
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            // Compass Bar
            CompassBar(
                playerAngle = player.angle,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Match Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Alive & Kill Badges
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MetricBadge(
                        label = "ALIVE",
                        value = aliveCount.toString(),
                        bgColor = BgmiSurfaceVariant,
                        textColor = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    MetricBadge(
                        label = "KILLED",
                        value = player.kills.toString(),
                        bgColor = Color(0xFFC62828),
                        textColor = Color.White
                    )
                }

                // Playzone timer pill
                val mins = safeZone.timeUntilShrinkSec / 60
                val secs = safeZone.timeUntilShrinkSec % 60
                val timeStr = String.format("%02d:%02d", mins, secs)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (safeZone.isShrinking) Color(0xFFD32F2F) else BgmiSurfaceVariant.copy(alpha = 0.9f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (safeZone.isShrinking) Color.White else BgmiBlueZone)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Zone Timer",
                            tint = if (safeZone.isShrinking) Color.White else BgmiBlueZone,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (safeZone.isShrinking) "SHRINKING" else "SAFE: $timeStr",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // TOP-LEFT KILL FEED
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 90.dp, start = 12.dp)
        ) {
            for (feed in killFeed) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xAA0F141A),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = feed.killer,
                            color = if (feed.killer == "You") BgmiGold else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Eliminated",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = feed.victim,
                            color = Color(0xFFB0BEC5),
                            fontSize = 11.sp
                        )
                        if (feed.isHeadshot) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "🎯 [HEADSHOT]",
                                color = Color(0xFFFFD700),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // CONTEXTUAL GAMEPLAY HINT TICKER (TOP CENTER)
        AnimatedVisibility(
            visible = currentHint.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xDD1B232C),
                border = androidx.compose.foundation.BorderStroke(1.dp, BgmiGold.copy(alpha = 0.5f))
            ) {
                Text(
                    text = currentHint,
                    color = BgmiGoldLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // BOTTOM LEFT: VIRTUAL JOYSTICK
        VirtualJoystick(
            onMove = onMoveJoystick,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 32.dp)
        )

        // BOTTOM CENTER: HEALTH, BOOST & WEAPON STATUS
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Weapon Selection Strip
            WeaponSelectorBar(
                selectedWeapon = player.selectedWeapon,
                secondaryWeapon = player.secondaryWeapon,
                currentAmmo = player.currentAmmo,
                isReloading = player.isReloading,
                reloadProgress = player.reloadProgress,
                onSwitchWeapon = onSwitchWeapon
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Health & Boost Bar
            HealthAndArmorPanel(
                player = player,
                onUseFirstAid = onUseFirstAid,
                onUseEnergyDrink = onUseEnergyDrink
            )
        }

        // BOTTOM RIGHT: COMBAT ACTION CLUSTER
        CombatActionsCluster(
            player = player,
            isScoped = isScoped,
            nearVehicle = nearVehicle,
            nearRecallTower = nearRecallTower,
            nearAirdrop = nearAirdrop,
            onFire = onFire,
            onReload = onReload,
            onToggleScope = onToggleScope,
            onThrowGrenade = onThrowGrenade,
            onToggleVehicle = onToggleVehicle,
            onTriggerRecall = onTriggerRecall,
            onLootAirdrop = onLootAirdrop,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 24.dp)
        )

        // QUICK VOICE CHAT BUTTON (RIGHT SIDE)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
        ) {
            FilledTonalIconButton(
                onClick = { showQuickChatMenu = !showQuickChatMenu },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color(0xAA1E2833),
                    contentColor = BgmiGold
                ),
                modifier = Modifier
                    .size(44.dp)
                    .testTag("quick_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.RecordVoiceOver,
                    contentDescription = "Quick Voice Chat"
                )
            }

            AnimatedVisibility(visible = showQuickChatMenu) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BgmiSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BgmiSurfaceBorder),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        val voiceLines = listOf(
                            "Enemies Ahead!",
                            "I got supplies!",
                            "Fall back to safe zone!",
                            "Get in the car!",
                            "Help me!"
                        )
                        for (line in voiceLines) {
                            Text(
                                text = line,
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .clickable {
                                        onQuickChat(line)
                                        showQuickChatMenu = false
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompassBar(playerAngle: Float, modifier: Modifier = Modifier) {
    val degrees = ((playerAngle * 180f / PI.toFloat() + 360f) % 360f).toInt()
    val cardinal = when (degrees) {
        in 338..360, in 0..22 -> "N"
        in 23..67 -> "NE"
        in 68..112 -> "E"
        in 113..157 -> "SE"
        in 158..202 -> "S"
        in 203..247 -> "SW"
        in 248..292 -> "W"
        else -> "NW"
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xBB0F141A))
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Navigation,
                contentDescription = "Heading",
                tint = BgmiGold,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$degrees° $cardinal",
                color = BgmiGoldLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun MetricBadge(label: String, value: String, bgColor: Color, textColor: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = Color(0xFFB0BEC5), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = value, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun VirtualJoystick(
    onMove: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var thumbOffset by remember { mutableStateOf(Offset.Zero) }
    val maxRadius = 55f

    Box(
        modifier = modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(Color(0x551E2833))
            .border(2.dp, Color(0x88FFB800), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {},
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = thumbOffset + dragAmount
                        val dist = sqrt(newOffset.x * newOffset.x + newOffset.y * newOffset.y)
                        thumbOffset = if (dist > maxRadius) {
                            Offset(
                                (newOffset.x / dist) * maxRadius,
                                (newOffset.y / dist) * maxRadius
                            )
                        } else {
                            newOffset
                        }
                        // Normalize -1..1
                        val nx = (thumbOffset.x / maxRadius).coerceIn(-1f, 1f)
                        val ny = (thumbOffset.y / maxRadius).coerceIn(-1f, 1f)
                        onMove(nx, ny)
                    },
                    onDragEnd = {
                        thumbOffset = Offset.Zero
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        thumbOffset = Offset.Zero
                        onMove(0f, 0f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Inner thumb knob
        Box(
            modifier = Modifier
                .offset { IntOffset(thumbOffset.x.roundToInt(), thumbOffset.y.roundToInt()) }
                .size(46.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(BgmiGold, Color(0xFFC68400))
                    )
                )
                .border(1.5.dp, Color.White, CircleShape)
        )
    }
}

@Composable
private fun WeaponSelectorBar(
    selectedWeapon: WeaponType,
    secondaryWeapon: WeaponType,
    currentAmmo: Int,
    isReloading: Boolean,
    reloadProgress: Float,
    onSwitchWeapon: (WeaponType) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Slot 1: Primary Weapon (e.g. M416)
        WeaponSlot(
            weapon = WeaponType.M416,
            isSelected = selectedWeapon == WeaponType.M416,
            ammo = if (selectedWeapon == WeaponType.M416) currentAmmo else 30,
            isReloading = isReloading && selectedWeapon == WeaponType.M416,
            reloadProgress = reloadProgress,
            onClick = { onSwitchWeapon(WeaponType.M416) }
        )

        // Slot 2: Secondary Weapon (e.g. AKM or AWM)
        val sec = if (selectedWeapon == WeaponType.AWM) WeaponType.AWM else secondaryWeapon
        WeaponSlot(
            weapon = sec,
            isSelected = selectedWeapon == sec,
            ammo = if (selectedWeapon == sec) currentAmmo else sec.magSize,
            isReloading = isReloading && selectedWeapon == sec,
            reloadProgress = reloadProgress,
            onClick = { onSwitchWeapon(sec) }
        )

        // Slot 3: Melee Pan
        WeaponSlot(
            weapon = WeaponType.PAN,
            isSelected = selectedWeapon == WeaponType.PAN,
            ammo = 1,
            isReloading = false,
            reloadProgress = 0f,
            onClick = { onSwitchWeapon(WeaponType.PAN) }
        )
    }
}

@Composable
private fun WeaponSlot(
    weapon: WeaponType,
    isSelected: Boolean,
    ammo: Int,
    isReloading: Boolean,
    reloadProgress: Float,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Color(0xDD2A3B4C) else Color(0x9917202A),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) weapon.skinColor else Color(0x44FFFFFF)
        ),
        modifier = Modifier
            .clickable { onClick() }
            .testTag("weapon_slot_${weapon.name}")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = weapon.name,
                color = if (isSelected) weapon.skinColor else Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            if (isReloading) {
                Text(
                    text = "RELOADING ${(reloadProgress * 100).toInt()}%",
                    color = Color(0xFFFFD54F),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = if (weapon == WeaponType.PAN) "MELEE" else "$ammo / ${weapon.magSize}",
                    color = if (ammo <= 5 && weapon != WeaponType.PAN) Color(0xFFFF5252) else Color(0xFFB0BEC5),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun HealthAndArmorPanel(
    player: PlayerState,
    onUseFirstAid: () -> Unit,
    onUseEnergyDrink: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xCC131A22),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Level 3 Helmet & Vest indicators
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Lv3 Armor",
                        tint = BgmiGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Lv.${player.vestLevel}",
                        color = BgmiGoldLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Bars (Health + Boost)
            Column(modifier = Modifier.width(180.dp)) {
                // Boost bar (Orange)
                val boostPercent = (player.boost / 100f).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { boostPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color(0xFFFF9800),
                    trackColor = Color(0x44FF9800)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // HP Bar (White-Yellow, Red when low)
                val hpPercent = (player.health / player.maxHealth).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { hpPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (hpPercent > 0.35f) Color.White else Color(0xFFFF3D00),
                    trackColor = Color(0x44D32F2F)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "HP ${player.health.toInt()}/100",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "BOOST ${player.boost.toInt()}%",
                        color = Color(0xFFFFB74D),
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Quick Meds: First Aid Kit button
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0x882E7D32),
                modifier = Modifier
                    .clickable { onUseFirstAid() }
                    .padding(end = 4.dp)
                    .testTag("use_medkit_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = "First Aid",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "x${player.firstAidKits}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Quick Energy Drink button
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0x880288D1),
                modifier = Modifier
                    .clickable { onUseEnergyDrink() }
                    .testTag("use_drink_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalDrink,
                        contentDescription = "Energy Drink",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "x${player.energyDrinks}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CombatActionsCluster(
    player: PlayerState,
    isScoped: Boolean,
    nearVehicle: Boolean,
    nearRecallTower: Boolean,
    nearAirdrop: Boolean,
    onFire: () -> Unit,
    onReload: () -> Unit,
    onToggleScope: () -> Unit,
    onThrowGrenade: (Boolean) -> Unit,
    onToggleVehicle: () -> Unit,
    onTriggerRecall: () -> Unit,
    onLootAirdrop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Special Action: Recall Station / Airdrop Loot / Drive Vehicle
        if (nearRecallTower) {
            FilledTonalButton(
                onClick = onTriggerRecall,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFF00E676),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("action_recall_beacon")
            ) {
                Icon(Icons.Default.CellTower, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("ACTIVATE RECALL", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }

        if (nearAirdrop) {
            FilledTonalButton(
                onClick = onLootAirdrop,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFFFF1744),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("action_loot_airdrop")
            ) {
                Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("LOOT AIRDROP", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }

        if (nearVehicle || player.inVehicle != null) {
            FilledTonalButton(
                onClick = onToggleVehicle,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (player.inVehicle != null) Color(0xFFFF5722) else Color(0xFFFF9800),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("action_vehicle_toggle")
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (player.inVehicle != null) "EXIT VEHICLE" else "DRIVE VEHICLE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        // Secondary Combat Row: Scope, Reload, Grenade
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Throw Frag Grenade
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xBB424242))
                    .border(1.5.dp, Color(0xFFFF5252), CircleShape)
                    .clickable { onThrowGrenade(false) }
                    .testTag("action_throw_frag"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Whatshot,
                    contentDescription = "Frag Grenade",
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "${player.fragGrenades}",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 2.dp, end = 4.dp)
                )
            }

            // Scope Button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (isScoped) BgmiGold else Color(0xBB424242))
                    .border(1.5.dp, BgmiGold, CircleShape)
                    .clickable { onToggleScope() }
                    .testTag("action_scope_toggle"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FilterCenterFocus,
                    contentDescription = "Scope",
                    tint = if (isScoped) Color.Black else BgmiGold,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Reload Button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xBB424242))
                    .border(1.5.dp, Color.White, CircleShape)
                    .clickable { onReload() }
                    .testTag("action_reload"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reload",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // MAIN FIRE BUTTON
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFFF3D00), Color(0xFFB71C1C))
                    )
                )
                .border(3.dp, Color(0xFFFFD54F), CircleShape)
                .clickable { onFire() }
                .testTag("action_fire_button"),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Adjust,
                    contentDescription = "FIRE",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
                Text(
                    text = "FIRE",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
