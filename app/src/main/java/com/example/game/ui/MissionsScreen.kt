package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.BattleMission
import com.example.ui.theme.*

@Composable
fun MissionsScreen(
    missions: List<BattleMission>,
    battlePoints: Int,
    onClaimReward: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgmiDarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // TOP BP BALANCE & ROYALE PASS BANNER
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BgmiSurface,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, BgmiGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(BgmiGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "BP Coins",
                                tint = Color.Black,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "BATTLE POINTS (BP)",
                                color = BgmiTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "$battlePoints BP",
                                color = BgmiGold,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BgmiSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BgmiSurfaceBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MilitaryTech,
                                contentDescription = "Royale Pass",
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "RP LV.42",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // MISSIONS TITLE
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DAILY BATTLE MISSIONS",
                    color = BgmiGoldLight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                val completedCount = missions.count { it.isCompleted }
                Text(
                    text = "$completedCount / ${missions.size} Completed",
                    color = BgmiOlive,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // MISSION ITEMS
        items(missions) { mission ->
            MissionCard(
                mission = mission,
                onClaim = { onClaimReward(mission.id) }
            )
        }
    }
}

@Composable
private fun MissionCard(
    mission: BattleMission,
    onClaim: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = BgmiSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (mission.isCompleted && !mission.isClaimed) BgmiGold else BgmiSurfaceBorder
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mission.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = mission.description,
                        color = BgmiTextSecondary,
                        fontSize = 12.sp
                    )
                }

                // Reward Tag
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0x33FFB800),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BgmiGold.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "+${mission.rewardBp} BP",
                        color = BgmiGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar and Claim Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    val progressRatio = (mission.current.toFloat() / mission.target).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (mission.isCompleted) BgmiOlive else BgmiGold,
                        trackColor = Color(0x22FFFFFF)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Progress: ${mission.current} / ${mission.target}",
                        color = BgmiTextMuted,
                        fontSize = 11.sp
                    )
                }

                when {
                    mission.isClaimed -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BgmiSurfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = BgmiOlive,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("CLAIMED", color = BgmiOlive, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    mission.isCompleted -> {
                        Button(
                            onClick = onClaim,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BgmiGold,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("claim_mission_${mission.id}")
                        ) {
                            Text("CLAIM BP", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    else -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BgmiSurfaceVariant
                        ) {
                            Text(
                                text = "IN PROGRESS",
                                color = BgmiTextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
