package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.LegacyHallEntity
import com.example.data.model.LawType
import com.example.data.model.LifeState
import com.example.data.model.PrisonGang
import com.example.data.model.SkillType
import com.example.engine.CrimeAction
import com.example.engine.PrisonAction
import com.example.engine.RoyalAction
import com.example.engine.formatCurrency
import com.example.ui.theme.*

enum class LegacySection(val label: String) {
    CROWN_GOV("Crown & Gov"),
    CRIME_PRISON("Crime & Prison"),
    PROFILE_HALL("Profile & Hall")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LegacyProfileTab(
    life: LifeState,
    legacyRecords: List<LegacyHallEntity>,
    activeSection: LegacySection,
    onSelectSection: (LegacySection) -> Unit,
    onCommitCrime: (CrimeAction) -> Unit,
    onKillTargetNpc: (String) -> Unit,
    onPrisonAction: (PrisonAction) -> Unit,
    onJoinPrisonGang: (PrisonGang) -> Unit,
    onAppealPrison: () -> Unit,
    onRunForPresident: (Long) -> Unit,
    onEnactLaw: (LawType) -> Unit,
    onRespondToBill: (Boolean, Long) -> Unit,
    onSeekRoyalMarriage: () -> Unit,
    onRoyalAction: (RoyalAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LegacySection.entries.forEach { sec ->
                    FilterChip(
                        selected = activeSection == sec,
                        onClick = { onSelectSection(sec) },
                        label = {
                            val badge = when {
                                sec == LegacySection.CROWN_GOV && life.government.proposedBill != null -> " (!)"
                                sec == LegacySection.CRIME_PRISON && life.prison.isInPrison -> " (LOCKED)"
                                else -> ""
                            }
                            Text(sec.label + badge)
                        },
                        modifier = Modifier.testTag("legacy_sec_${sec.name.lowercase()}")
                    )
                }
            }
        }

        when (activeSection) {
            LegacySection.CROWN_GOV -> {
                item {
                    CrownGovSection(
                        life = life,
                        onRunForPresident = onRunForPresident,
                        onEnactLaw = onEnactLaw,
                        onRespondToBill = onRespondToBill,
                        onSeekRoyalMarriage = onSeekRoyalMarriage,
                        onRoyalAction = onRoyalAction
                    )
                }
            }

            LegacySection.CRIME_PRISON -> {
                item {
                    UnderworldPrisonSection(
                        life = life,
                        onCommitCrime = onCommitCrime,
                        onKillTargetNpc = onKillTargetNpc,
                        onPrisonAction = onPrisonAction,
                        onJoinPrisonGang = onJoinPrisonGang,
                        onAppealPrison = onAppealPrison
                    )
                }
            }

            LegacySection.PROFILE_HALL -> {
                // 8 Core Attributes
                item {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("CHARACTER ATTRIBUTES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(10.dp))
                            val s = life.stats
                            val rows = listOf(
                                Triple("Health", s.health, EmeraldGain),
                                Triple("Happiness", s.happiness, BurnishedGold),
                                Triple("Intellect", s.intellect, CobaltInfo),
                                Triple("Charisma", s.charisma, DeepTealAccent),
                                Triple("Discipline", s.discipline, VioletSkill),
                                Triple("Ethics / Karma", s.ethics, EmeraldGain),
                                Triple("Stress", s.stress, if (s.stress >= 75) CrimsonLoss else AmberWarn),
                                Triple("Fame", s.fame, BurnishedGold)
                            )
                            rows.chunked(2).forEach { pair ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    pair.forEach { (label, valInt, color) ->
                                        StatMeterCell(label, valInt, color, Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                // 6 Skill Mastery Trees
                item {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("SKILL MASTERY TREES", style = MaterialTheme.typography.labelSmall, color = DeepTealAccent)
                            Spacer(Modifier.height(10.dp))
                            SkillType.entries.forEach { type ->
                                val prog = life.skills[type]
                                val lv = prog?.level ?: 1
                                val pct = prog?.levelProgress ?: 0f
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${type.label} (${type.iconHint})", style = MaterialTheme.typography.bodyMedium)
                                        Text("Lv.$lv", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(Modifier.height(3.dp))
                                    LinearProgressIndicator(
                                        progress = { pct },
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                    )
                                }
                            }
                        }
                    }
                }

                // Permanent Traits
                item {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("LIFE TRAITS & REPUTATION", style = MaterialTheme.typography.labelSmall, color = VioletSkill)
                            Spacer(Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                life.traits.forEach { trait ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            trait,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Dynasty Hall of Fame
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_dynasty_crest),
                            contentDescription = "Dynasty Hall of Fame crest",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                        )
                        Column {
                            Text("Hall of Legacies", style = MaterialTheme.typography.titleLarge)
                            Text("Completed lives & generational dynasties", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (legacyRecords.isEmpty()) {
                    item {
                        Text(
                            "No completed lives yet. Live a full chronicle to enshrine your ancestor here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(legacyRecords, key = { it.id }) { rec ->
                        Card(
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${rec.fullName} (Gen ${rec.generation})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        formatCurrency(rec.finalNetWorth),
                                        style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFamily),
                                        color = EmeraldGain
                                    )
                                }
                                Text(
                                    "${rec.legacyTitle} • Died at ${rec.ageAtDeath} (${rec.causeOfDeath})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "${rec.careerHighlight} • ${rec.notableTraits}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
private fun StatMeterCell(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text("$value", style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { (value / 100f).coerceIn(0f, 1f) },
            color = color,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}
