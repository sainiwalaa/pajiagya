package com.example.game.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.model.SquadTeam
import com.example.game.model.WeaponType
import com.example.ui.theme.*

@Composable
fun LobbyScreen(
    battlePoints: Int,
    equippedWeapon: WeaponType,
    squadTeam: SquadTeam,
    onStartMatch: () -> Unit,
    onOpenArmory: () -> Unit,
    onOpenMissions: () -> Unit,
    onOpenSquad: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf("Classic - Erangel") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgmiDarkBg)
    ) {
        // TOP PROFILE BAR
        Surface(
            color = BgmiSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Player Card
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .border(2.dp, BgmiGold, CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_bgmi_logo_1791560625230),
                            contentDescription = "Player Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Saini_Warrior",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = BgmiGold
                            ) {
                                Text(
                                    text = "LV.54",
                                    color = Color.Black,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Ace Dominator",
                                color = BgmiGoldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(text = " • ", color = BgmiTextMuted, fontSize = 11.sp)
                            Text(
                                text = "[${squadTeam.clanTag}]",
                                color = BgmiBlueZone,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Currency Pill (BP Coins)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = BgmiSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BgmiSurfaceBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = "BP",
                            tint = BgmiGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$battlePoints BP",
                            color = BgmiGoldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // HERO BANNER & SQUAD DISPLAY
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Battleground Wallpaper Art
            Image(
                painter = painterResource(id = R.drawable.img_bgmi_hero_1791560645976),
                contentDescription = "Battleground Splash",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient Overlays for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0x66000000),
                                BgmiDarkBg
                            )
                        )
                    )
            )

            // Squad Members Floating Badges
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xCC0F141A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BgmiGold.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "SQUAD: ${squadTeam.teamName} (${squadTeam.members.size}/4)",
                        color = BgmiGoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                for (m in squadTeam.members) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xAA1E2631)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (m.isReady) BgmiOlive else Color(0xFFFF9800))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${m.name} (${m.role})",
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Equipped Weapon Floating Pill (Bottom Left)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xDD171F28),
                border = androidx.compose.foundation.BorderStroke(1.dp, equippedWeapon.skinColor),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 16.dp)
                    .clickable { onOpenArmory() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = equippedWeapon.skinColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = equippedWeapon.displayName,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = equippedWeapon.skinName,
                            color = equippedWeapon.skinColor,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }

        // MODE SELECTOR & START MATCH PANEL
        Surface(
            color = BgmiSurface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .navigationBarsPadding()
            ) {
                // Game Mode Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val modes = listOf(
                        "Classic - Erangel",
                        "Heavy Payload Tank",
                        "War Mode (Fast)"
                    )
                    for (mode in modes) {
                        FilterChip(
                            selected = selectedMode == mode,
                            onClick = { selectedMode = mode },
                            label = {
                                Text(
                                    text = mode,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BgmiGold,
                                selectedLabelColor = Color.Black,
                                containerColor = BgmiSurfaceVariant,
                                labelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // BIG "START BATTLE" BUTTON
                Button(
                    onClick = onStartMatch,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BgmiGold,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("start_match_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start",
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "START BATTLE ROYALE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}
