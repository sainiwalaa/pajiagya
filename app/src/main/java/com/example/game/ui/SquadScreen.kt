package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.game.model.SquadMember
import com.example.game.model.SquadTeam
import com.example.ui.theme.*

@Composable
fun SquadScreen(
    squadTeam: SquadTeam,
    onUpdateSquad: (SquadTeam) -> Unit,
    modifier: Modifier = Modifier
) {
    var teamName by remember { mutableStateOf(squadTeam.teamName) }
    var clanTag by remember { mutableStateOf(squadTeam.clanTag) }
    var showAddDialog by remember { mutableStateOf(false) }
    var inviteCodeCopied by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgmiDarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // HEADER / SQUAD IDENTITY CARD
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BgmiSurface,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, BgmiGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(BgmiGold)
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Clan Emblem",
                                    tint = Color.Black,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "[$clanTag] $teamName",
                                    color = BgmiGold,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "Competitive 4-Player Battle Royale Squad",
                                    color = BgmiTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BgmiOlive.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BgmiOlive)
                        ) {
                            Text(
                                text = "OFFICIAL CLAN",
                                color = BgmiOlive,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Team Stats Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BgmiSurfaceVariant, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        SquadStatItem("MATCHES", "148")
                        SquadStatItem("CHICKEN DINNERS", "52")
                        SquadStatItem("AVG K/D", "4.6")
                        SquadStatItem("WIN RATE", "35.1%")
                    }
                }
            }
        }

        // EDIT CLAN SETTINGS
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = BgmiSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Customize Clan & Squad Settings",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = clanTag,
                            onValueChange = {
                                clanTag = it.take(6).uppercase()
                                onUpdateSquad(squadTeam.copy(clanTag = clanTag))
                            },
                            label = { Text("Clan Tag") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("clan_tag_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BgmiGold,
                                focusedLabelColor = BgmiGold
                            )
                        )

                        OutlinedTextField(
                            value = teamName,
                            onValueChange = {
                                teamName = it.take(24)
                                onUpdateSquad(squadTeam.copy(teamName = teamName))
                            },
                            label = { Text("Squad Name") },
                            modifier = Modifier
                                .weight(2f)
                                .testTag("squad_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BgmiGold,
                                focusedLabelColor = BgmiGold
                            )
                        )
                    }
                }
            }
        }

        // SQUAD MEMBERS (4 SLOTS)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SQUAD ROSTER (4 SLOTS)",
                    color = BgmiGoldLight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${squadTeam.members.size}/4 Active",
                    color = BgmiTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        itemsIndexed(squadTeam.members) { index, member ->
            SquadMemberCard(
                slotIndex = index + 1,
                member = member,
                onRoleChange = { newRole ->
                    val updated = squadTeam.members.mapIndexed { idx, m ->
                        if (idx == index) m.copy(role = newRole) else m
                    }
                    onUpdateSquad(squadTeam.copy(members = updated))
                },
                onToggleReady = {
                    val updated = squadTeam.members.mapIndexed { idx, m ->
                        if (idx == index) m.copy(isReady = !m.isReady) else m
                    }
                    onUpdateSquad(squadTeam.copy(members = updated))
                }
            )
        }

        // GITHUB & MULTIPLAYER PREPARATION PANEL
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = BgmiSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, BgmiSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "GitHub Ready",
                            tint = BgmiGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GitHub Release & Multiplayer Ready",
                            color = BgmiGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "This squad architecture is structured for open-source GitHub publishing with room for live WebSocket / Firebase online lobby matchmaking in next update.",
                        color = BgmiTextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { inviteCodeCopied = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BgmiGold,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("generate_invite_code_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (inviteCodeCopied) "CODE: BGMI-SQ-9482" else "Invite Squad Code", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val newMember = SquadMember(
                                    id = (squadTeam.members.size + 1).toString(),
                                    name = "Teammate_${(10..99).random()}",
                                    role = "Assaulter",
                                    rank = "Ace",
                                    avatarId = (1..4).random(),
                                    isReady = true,
                                    kills = 3,
                                    isLeader = false
                                )
                                if (squadTeam.members.size < 4) {
                                    onUpdateSquad(squadTeam.copy(members = squadTeam.members + newMember))
                                }
                            },
                            enabled = squadTeam.members.size < 4,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_teammate_button")
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Teammate", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SquadStatItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = BgmiGold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text(text = title, color = BgmiTextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SquadMemberCard(
    slotIndex: Int,
    member: SquadMember,
    onRoleChange: (String) -> Unit,
    onToggleReady: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = BgmiSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (member.isLeader) BgmiGold.copy(alpha = 0.8f) else BgmiSurfaceBorder
        ),
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
                // Slot badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (member.isLeader) BgmiGold else Color(0xFF2C3E50)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#$slotIndex",
                        color = if (member.isLeader) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = member.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (member.isLeader) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = BgmiGold.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "LEADER",
                                    color = BgmiGold,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = member.rank,
                            color = BgmiTextSecondary,
                            fontSize = 11.sp
                        )
                        Text(text = " • ", color = BgmiTextMuted, fontSize = 11.sp)
                        // Clickable role toggle
                        Text(
                            text = "Role: ${member.role}",
                            color = BgmiBlueZone,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                val nextRole = when (member.role) {
                                    "IGL" -> "Assaulter"
                                    "Assaulter" -> "Sniper"
                                    "Sniper" -> "Support"
                                    else -> "IGL"
                                }
                                onRoleChange(nextRole)
                            }
                        )
                    }
                }
            }

            // Ready Toggle
            FilterChip(
                selected = member.isReady,
                onClick = onToggleReady,
                label = {
                    Text(
                        text = if (member.isReady) "READY" else "WAITING",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BgmiOlive,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}
