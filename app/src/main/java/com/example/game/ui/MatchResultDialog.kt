package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.model.PlayerState
import com.example.ui.theme.*

@Composable
fun MatchResultDialog(
    isVictory: Boolean,
    player: PlayerState,
    aliveCount: Int,
    onPlayAgain: () -> Unit,
    onExitLobby: () -> Unit
) {
    Dialog(onDismissRequest = { /* Modal dialog */ }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(2.dp, if (isVictory) BgmiGold else Color(0xFFFF5252), RoundedCornerShape(20.dp))
                .testTag("match_result_dialog"),
            color = BgmiSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ICON BADGE
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(if (isVictory) BgmiGold else Color(0x33FF5252))
                        .border(2.dp, if (isVictory) Color.White else Color(0xFFFF5252), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isVictory) Icons.Default.EmojiEvents else Icons.Default.SentimentDissatisfied,
                        contentDescription = null,
                        tint = if (isVictory) Color.Black else Color(0xFFFF5252),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // TITLE BANNER
                Text(
                    text = if (isVictory) "#1 / 50" else "#$aliveCount / 50",
                    color = if (isVictory) BgmiGold else Color(0xFFB0BEC5),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = if (isVictory) "WINNER WINNER CHICKEN DINNER!" else "BETTER LUCK NEXT TIME!",
                    color = if (isVictory) BgmiGoldLight else Color(0xFFFF5252),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // STATS GRID
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BgmiSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ResultRow("ELIMINATIONS", "${player.kills} Kills")
                        ResultRow("DAMAGE DEALT", "${player.kills * 180 + 95}")
                        ResultRow("RATING", if (isVictory) "+38 (Ace)" else "+12 (Crown)")
                        ResultRow("BATTLE POINTS EARNED", "+${player.kills * 75 + (if (isVictory) 1000 else 150)} BP")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ACTION BUTTONS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onExitLobby,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("exit_to_lobby_button")
                    ) {
                        Text("LOBBY", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onPlayAgain,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BgmiGold,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("play_again_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PLAY AGAIN", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = BgmiTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Text(text = value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
