package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.LifeState
import com.example.data.model.PrisonGang
import com.example.data.model.PrisonSecurity
import com.example.engine.CrimeAction
import com.example.engine.PrisonAction
import com.example.engine.formatCurrency
import com.example.ui.theme.*

@Composable
fun UnderworldPrisonSection(
    life: LifeState,
    onCommitCrime: (CrimeAction) -> Unit,
    onKillTargetNpc: (String) -> Unit,
    onPrisonAction: (PrisonAction) -> Unit,
    onJoinPrisonGang: (PrisonGang) -> Unit,
    onAppealPrison: () -> Unit
) {
    val p = life.prison
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // 1. Full Interactive Prison Yard Console
        if (p.isInPrison) {
            val secColor = if (p.isDeathRow || p.securityLevel == PrisonSecurity.SUPERMAX) CrimsonLoss else AmberWarn
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = secColor.copy(alpha = 0.12f)),
                border = BorderStroke(1.5.dp, secColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Lock, contentDescription = "Cellblock", tint = secColor)
                            Text("PENITENTIARY CELLBLOCK", style = MaterialTheme.typography.labelSmall, color = secColor)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = secColor
                        ) {
                            Text(
                                p.securityLevel.label.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = ObsidianBg,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(
                        when {
                            p.isDeathRow -> "DEATH ROW • Execution scheduled in ${p.yearsUntilExecution} yr(s)"
                            p.isLifeSentence -> "LIFE WITHOUT PAROLE • Charge: ${p.crimeName}"
                            else -> "${p.yearsRemaining}y Left (of ${p.totalSentence}y) • ${p.crimeName}"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = secColor
                    )

                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Yard Respect", style = MaterialTheme.typography.labelSmall)
                                Text("${p.respect}%", style = MaterialTheme.typography.labelSmall)
                            }
                            LinearProgressIndicator(
                                progress = { p.respect / 100f },
                                color = BurnishedGold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Appeal Prep", style = MaterialTheme.typography.labelSmall)
                                Text("${p.lawStudyProgress}%", style = MaterialTheme.typography.labelSmall)
                            }
                            LinearProgressIndicator(
                                progress = { p.lawStudyProgress / 100f },
                                color = CobaltInfo,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Text("Prison Faction", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PrisonGang.entries.filter { it != PrisonGang.NONE }.forEach { gang ->
                            FilterChip(
                                selected = p.gang == gang,
                                onClick = { onJoinPrisonGang(gang) },
                                label = { Text("${gang.label} (${gang.perkDesc})") }
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = onAppealPrison,
                        enabled = life.isAlive && life.cash >= 25_000L,
                        colors = ButtonDefaults.buttonColors(containerColor = CobaltInfo),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("prison_appeal_button")
                    ) {
                        Text("Hire Appellate Counsel ($25,000)")
                    }

                    Spacer(Modifier.height(8.dp))
                    PrisonAction.entries.forEach { act ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(act.title, style = MaterialTheme.typography.titleSmall)
                                Text(act.desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            OutlinedButton(
                                onClick = { onPrisonAction(act) },
                                enabled = life.isAlive && life.actionsUsedThisYear < 4 && life.cash >= act.cost,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("prison_action_${act.name.lowercase()}")
                            ) {
                                Text(if (act.cost > 0L) formatCurrency(act.cost) else "Act")
                            }
                        }
                    }
                }
            }
        }

        // 2. Underworld, Heat & 18+ Lethal Operations
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, CrimsonLoss.copy(alpha = 0.65f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.Warning, contentDescription = "Underworld", tint = CrimsonLoss, modifier = Modifier.size(18.dp))
                        Text("UNDERWORLD & 18+ OPS", style = MaterialTheme.typography.labelSmall, color = CrimsonLoss)
                    }
                    Text(
                        "Heat ${life.heatLevel}% • Notoriety ${p.notoriety}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (life.heatLevel >= 55) CrimsonLoss else AmberWarn
                    )
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { life.heatLevel / 100f },
                    color = CrimsonLoss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Spacer(Modifier.height(10.dp))

                CrimeAction.entries.forEach { crime ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                crime.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (crime.isLethal) CrimsonLoss else MaterialTheme.colorScheme.onSurface
                            )
                            Text(crime.riskDesc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedButton(
                            onClick = { onCommitCrime(crime) },
                            enabled = life.isAlive && !p.isInPrison && life.age >= crime.minAge,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("crime_${crime.name.lowercase()}")
                        ) {
                            Text(if (crime.isLethal) "Strike" else "Execute", color = CrimsonLoss)
                        }
                    }
                }

                // Targeted NPC Contract Hits (18+)
                val targets = life.relations.filter { it.isAlive && it.age >= 16 }
                if (life.age >= 18 && !p.isInPrison && targets.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Targeted Contract Hit (18+) — Collect Estate or Silence Rivals",
                        style = MaterialTheme.typography.labelMedium,
                        color = CrimsonLoss
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        targets.forEach { npc ->
                            AssistChip(
                                onClick = { onKillTargetNpc(npc.id) },
                                label = { Text("Hit: ${npc.name} (${npc.role.label})", color = CrimsonLoss) },
                                modifier = Modifier.testTag("hit_npc_${npc.id}")
                            )
                        }
                    }
                }
            }
        }
    }
}
