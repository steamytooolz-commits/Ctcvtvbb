package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.*
import com.example.engine.RoyalAction
import com.example.engine.formatCurrency
import com.example.ui.theme.*

@Composable
fun CrownGovSection(
    life: LifeState,
    onRunForPresident: (Long) -> Unit,
    onEnactLaw: (LawType) -> Unit,
    onRespondToBill: (Boolean, Long) -> Unit,
    onSeekRoyalMarriage: () -> Unit,
    onRoyalAction: (RoyalAction) -> Unit
) {
    val gov = life.government
    val royal = life.royal
    val isRuler = gov.isPlayerPresident || royal.rank == RoyalRank.MONARCH

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // 1. Royalty & Peerage Card
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, BurnishedGold.copy(alpha = 0.7f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.MilitaryTech, contentDescription = "Royalty", tint = BurnishedGold)
                        Text("CROWN & ARISTOCRACY", style = MaterialTheme.typography.labelSmall, color = BurnishedGold)
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            gov.regimeType.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))
                Text(
                    if (royal.rank != RoyalRank.NONE) "${royal.rank.label} • House ${royal.houseName.ifBlank { "Crown" }}"
                    else "Commoner • Sovereign: ${if (gov.regimeType == RegimeType.REPUBLIC) "None (Republic)" else gov.monarchName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                if (royal.rank != RoyalRank.NONE) {
                    val mult = if (life.hasLaw(LawType.ROYAL_CROWN_ACT)) 2L else 1L
                    Text(
                        "Crown Stipend: ${formatCurrency(royal.rank.annualStipend * mult)}/yr • Decrees: ${royal.decreesPassed}${if (royal.isExiled) " • EXILED" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldGain
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Crown Approval", style = MaterialTheme.typography.labelSmall)
                                Text("${royal.approval}%", style = MaterialTheme.typography.labelSmall)
                            }
                            LinearProgressIndicator(
                                progress = { royal.approval / 100f },
                                color = EmeraldGain,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Scandal / Revolt", style = MaterialTheme.typography.labelSmall)
                                Text("${royal.scandalHeat}%", style = MaterialTheme.typography.labelSmall, color = if (royal.scandalHeat >= 65) CrimsonLoss else AmberWarn)
                            }
                            LinearProgressIndicator(
                                progress = { royal.scandalHeat / 100f },
                                color = CrimsonLoss,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    RoyalAction.entries.forEach { act ->
                        val isMonarchAct = act == RoyalAction.EXECUTE_DISSIDENT || act == RoyalAction.ABDICATE
                        if (!isMonarchAct || royal.rank == RoyalRank.MONARCH) {
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
                                    onClick = { onRoyalAction(act) },
                                    enabled = life.isAlive && !life.prison.isInPrison && life.cash >= act.cost,
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("royal_action_${act.name.lowercase()}")
                                ) {
                                    Text(if (act.cost > 0) formatCurrency(act.cost) else "Enact")
                                }
                            }
                        }
                    }
                } else {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Build Fame 40+ or Net Worth $750k+ to court royal matchmakers and enter the peerage.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    FilledTonalButton(
                        onClick = onSeekRoyalMarriage,
                        enabled = life.isAlive && life.age >= 18 && !life.prison.isInPrison,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("seek_royal_marriage_button")
                    ) {
                        Text("Court Royal Matchmakers (18+)")
                    }
                }
            }
        }

        // 2. National Government, Presidency & Active Laws
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, CobaltInfo.copy(alpha = 0.65f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.AccountBalance, contentDescription = "Government", tint = CobaltInfo)
                        Text("GOVERNMENT & LAWS", style = MaterialTheme.typography.labelSmall, color = CobaltInfo)
                    }
                    Text(
                        "Effective Tax: ${(life.effectiveTaxRate() * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (life.effectiveTaxRate() > life.country.taxRate) CrimsonLoss else EmeraldGain
                    )
                }

                Spacer(Modifier.height(6.dp))
                Text(
                    if (gov.isPlayerPresident) "You are sitting ${gov.rulerTitle} (Year ${gov.termYearsServed + 1}, Approval ${gov.presidentApproval}%)"
                    else "${gov.rulerTitle}: ${gov.presidentName} • Stability ${gov.nationalStability}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                // Pending Bill Block
                gov.proposedBill?.let { bill ->
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BurnishedGold.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, BurnishedGold)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "PENDING BILL • ${bill.proposerTitle} ${bill.proposerName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = BurnishedGold
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(bill.lawType.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(bill.lawType.summary, style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onRespondToBill(true, if (isRuler) 0L else 20_000L) },
                                    enabled = life.isAlive && (isRuler || life.cash >= 20_000L),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("bill_approve_button")
                                ) {
                                    Text(if (isRuler) "Sign Law" else "Lobby Pass ($20k)")
                                }
                                OutlinedButton(
                                    onClick = { onRespondToBill(false, if (isRuler) 0L else 20_000L) },
                                    enabled = life.isAlive && (isRuler || life.cash >= 20_000L),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("bill_veto_button")
                                ) {
                                    Text(if (isRuler) "Veto" else "Lobby Block ($20k)")
                                }
                            }
                        }
                    }
                }

                // Campaign for President
                if (!gov.isPlayerPresident) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        if (life.hasLaw(LawType.MARTIAL_LAW)) "Elections suspended under Martial Law."
                        else "Run for Head of State (Age 25+, Rhetoric & Fame boost odds)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(
                            onClick = { onRunForPresident(25_000L) },
                            enabled = life.isAlive && life.age >= 25 && !life.prison.isInPrison && !life.hasLaw(LawType.MARTIAL_LAW) && life.cash >= 25_000L,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("run_president_grassroots")
                        ) { Text("Run ($25k)") }
                        Button(
                            onClick = { onRunForPresident(150_000L) },
                            enabled = life.isAlive && life.age >= 25 && !life.prison.isInPrison && !life.hasLaw(LawType.MARTIAL_LAW) && life.cash >= 150_000L,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("run_president_blitz")
                        ) { Text("Media Blitz ($150k)") }
                    }
                }

                // Active Enacted Laws
                Spacer(Modifier.height(12.dp))
                Text("Active National Laws (${gov.activeLaws.size}/5)", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(6.dp))
                if (gov.activeLaws.isEmpty()) {
                    Text(
                        "Standard constitutional code in effect. Laws enacted by the President or Monarch dynamically shift taxes, tuition, markets, and sentencing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        gov.activeLaws.forEach { enacted ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(enacted.type.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                        Text("${enacted.enactedYear}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Text(enacted.type.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Passed by ${enacted.enactedBy}", style = MaterialTheme.typography.labelSmall, color = CobaltInfo)
                                }
                            }
                        }
                    }
                }

                // Ruler Decree Console (when Player is President or Monarch)
                if (isRuler) {
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.Gavel, contentDescription = null, tint = BurnishedGold, modifier = Modifier.size(18.dp))
                        Text("Enact Executive / Royal Law", style = MaterialTheme.typography.titleSmall, color = BurnishedGold)
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LawType.entries.forEach { law ->
                            val alreadyActive = life.hasLaw(law)
                            FilterChip(
                                selected = alreadyActive,
                                onClick = { if (!alreadyActive) onEnactLaw(law) },
                                label = { Text(law.title) },
                                modifier = Modifier.testTag("enact_law_${law.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        }
    }
}
