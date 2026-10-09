package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.VehicleType
import com.example.game.model.WeaponType
import com.example.ui.theme.*

@Composable
fun ArmoryScreen(
    currentWeapon: WeaponType,
    onEquipWeapon: (WeaponType) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedArmoryWeapon by remember { mutableStateOf(currentWeapon) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgmiDarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP WEAPON INSPECTION DISPLAY
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BgmiSurface,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, selectedArmoryWeapon.skinColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = selectedArmoryWeapon.displayName,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Tactical Skin: ${selectedArmoryWeapon.skinName}",
                                color = selectedArmoryWeapon.skinColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x33FFFFFF)
                        ) {
                            Text(
                                text = selectedArmoryWeapon.ammoType,
                                color = BgmiGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Stylish Weapon Visual Showcase
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(BgmiSurfaceVariant, RoundedCornerShape(12.dp))
                            .border(1.dp, BgmiSurfaceBorder, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (selectedArmoryWeapon == WeaponType.PAN) Icons.Default.Kitchen else Icons.Default.SportsEsports,
                                contentDescription = "Weapon Model",
                                tint = selectedArmoryWeapon.skinColor,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "★★★★★ UPGRADED MAX FINISH EFFECT",
                                color = BgmiGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Gun Stat Bars
                    GunStatBar("BASE DAMAGE", selectedArmoryWeapon.baseDamage / 110f, "${selectedArmoryWeapon.baseDamage.toInt()}")
                    GunStatBar("FIRE RATE", 1f - (selectedArmoryWeapon.fireRateMs / 1200f), "${1000 / selectedArmoryWeapon.fireRateMs.coerceAtLeast(1)}/s")
                    GunStatBar("EFFECTIVE RANGE", selectedArmoryWeapon.range / 700f, "${selectedArmoryWeapon.range.toInt()}m")
                    GunStatBar("MAGAZINE CAPACITY", selectedArmoryWeapon.magSize / 40f, "${selectedArmoryWeapon.magSize} rds")

                    Spacer(modifier = Modifier.height(14.dp))

                    // Equip Button
                    Button(
                        onClick = { onEquipWeapon(selectedArmoryWeapon) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BgmiGold,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("equip_weapon_button")
                    ) {
                        Text(
                            text = if (currentWeapon == selectedArmoryWeapon) "CURRENTLY EQUIPPED" else "EQUIP THIS LOADOUT",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // WEAPONS ARSENAL LIST
        item {
            Text(
                text = "WEAPON INVENTORY",
                color = BgmiGoldLight,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (w in WeaponType.values()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedArmoryWeapon == w) BgmiSurfaceVariant else BgmiSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (selectedArmoryWeapon == w) w.skinColor else BgmiSurfaceBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedArmoryWeapon = w }
                            .testTag("armory_weapon_${w.name}")
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(w.skinColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SportsEsports,
                                        contentDescription = null,
                                        tint = w.skinColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = w.displayName,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${w.skinName} • ${w.ammoType}",
                                        color = BgmiTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            if (currentWeapon == w) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = BgmiOlive
                                ) {
                                    Text(
                                        text = "EQUIPPED",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // VEHICLES GARAGE
        item {
            Text(
                text = "BATTLEGROUND VEHICLES",
                color = BgmiGoldLight,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (v in VehicleType.values()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BgmiSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BgmiSurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(v.color.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsCar,
                                        contentDescription = null,
                                        tint = v.color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = v.displayName,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Max HP: ${v.maxHealth.toInt()} • Speed: ${v.maxSpeed * 18} km/h",
                                        color = BgmiTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = BgmiSurfaceVariant
                            ) {
                                Text(
                                    text = "DRIVEABLE",
                                    color = BgmiGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GunStatBar(label: String, progress: Float, valueStr: String) {
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = BgmiTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(text = valueStr, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = BgmiGold,
            trackColor = Color(0x33FFFFFF)
        )
    }
}
