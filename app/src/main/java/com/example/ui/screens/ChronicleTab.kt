package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.R
import com.example.data.model.ChronicleCategory
import com.example.data.model.ChronicleEntry
import com.example.data.model.LifeState
import com.example.engine.SelfAction
import com.example.engine.formatCurrency
import com.example.ui.components.DilemmaCard
import com.example.ui.components.EpitaphSuccessionCard
import com.example.ui.theme.*

@Composable
fun ChronicleTab(
    life: LifeState,
    onResolveDilemma: (Int) -> Unit,
    onSelfAction: (SelfAction) -> Unit,
    onAppealPrison: () -> Unit,
    onContinueAsHeir: (String?) -> Unit,
    onOpenOriginStudio: () -> Unit,
    onOpenNetworkInbox: () -> Unit,
    onOpenCrownGov: () -> Unit,
    onOpenPrisonYard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chapterTitle = when (life.age) {
        in 0..5 -> "Early Childhood"
        in 6..12 -> "Formative Years"
        in 13..17 -> "Adolescence"
        in 18..29 -> "Foundations & Ambition"
        in 30..54 -> "Prime Decade"
        in 55..74 -> "Harvest & Influence"
        else -> "Twilight Chronicle"
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(144.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_chronicle_banner),
                        contentDescription = "Chronicle chapter artwork",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.25f),
                                        Color(0xFF0E121A).copy(alpha = 0.88f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "YEAR ${life.currentYear} • ${chapterTitle.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = BurnishedGold
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = if (life.isAlive) "What defines this year?" else "End of an Era",
                            style = MaterialTheme.typography.headlineSmall,
                            color = WarmIvory
                        )
                    }
                }
            }
        }

        // Unread NPC Messages Banner
        if (life.unreadInboxCount > 0 && life.isAlive) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = DeepTealAccent.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, DeepTealAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onOpenNetworkInbox() }
                        .testTag("unread_inbox_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.MarkEmailUnread, contentDescription = null, tint = DeepTealAccent)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    "${life.unreadInboxCount} Unread Message${if (life.unreadInboxCount > 1) "s" else ""}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = DeepTealAccent
                                )
                                Text(
                                    "Someone in your circle reached out—tap to reply",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text("Open", style = MaterialTheme.typography.labelLarge, color = DeepTealAccent)
                    }
                }
            }
        }

        // Active Status Effects & Brewing Cascades
        if (life.statusEffects.isNotEmpty() || life.pendingArcs.isNotEmpty() || life.lastCascadeSummary != null) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (life.lastCascadeSummary != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = VioletSkill.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, VioletSkill.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Timeline, contentDescription = null, tint = VioletSkill, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    life.lastCascadeSummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VioletSkill
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (life.pendingArcs.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, VioletSkill.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    "${life.pendingArcs.size} Brewing Cascade${if (life.pendingArcs.size > 1) "s" else ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = VioletSkill,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                        life.statusEffects.forEach { eff ->
                            val c = if (eff.isPositive) EmeraldGain else CrimsonLoss
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = c.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, c.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    "${eff.name} (${eff.yearsLeft}y)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = c,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Pending Bill / Royal Decree Alert
        life.government.proposedBill?.let { bill ->
            if (life.isAlive) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = BurnishedGold.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, BurnishedGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onOpenCrownGov() }
                            .testTag("pending_bill_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Filled.AccountBalance, contentDescription = "Proposed law", tint = BurnishedGold)
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        "${bill.proposerTitle} Proposed: ${bill.lawType.title}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = BurnishedGold
                                    )
                                    Text(
                                        bill.lawType.summary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text("Review", style = MaterialTheme.typography.labelLarge, color = BurnishedGold)
                        }
                    }
                }
            }
        }

        if (life.prison.isInPrison && life.isAlive) {
            item {
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = CrimsonLoss.copy(alpha = 0.16f)),
                    border = BorderStroke(1.dp, CrimsonLoss)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    when {
                                        life.prison.isDeathRow -> "DEATH ROW • Execution in ${life.prison.yearsUntilExecution}y"
                                        life.prison.isLifeSentence -> "LIFE SENTENCE (${life.prison.securityLevel.label})"
                                        else -> "Incarcerated (${life.prison.yearsRemaining}y • ${life.prison.securityLevel.label})"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    color = CrimsonLoss
                                )
                                Text(
                                    "Charge: ${life.prison.crimeName} • Respect ${life.prison.respect}%",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onAppealPrison,
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonLoss),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                            ) {
                                Text("Appeal ($25k)")
                            }
                            FilledTonalButton(
                                onClick = onOpenPrisonYard,
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("open_cellblock_button")
                            ) {
                                Text("Cellblock Yard")
                            }
                        }
                    }
                }
            }
        }

        if (!life.isAlive) {
            item {
                EpitaphSuccessionCard(
                    life = life,
                    onContinueAsHeir = onContinueAsHeir,
                    onNewLife = onOpenOriginStudio
                )
            }
        } else if (life.pendingEvent != null) {
            item {
                DilemmaCard(
                    event = life.pendingEvent,
                    skills = life.skills,
                    onChoose = onResolveDilemma
                )
            }
        }

        if (life.isAlive) {
            item {
                Column {
                    Text(
                        text = "Quick Actions (${4 - life.actionsUsedThisYear} left this year)",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SelfAction.entries.filter { life.age >= it.minAge }.forEach { action ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier
                                    .width(152.dp)
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSelfAction(action) }
                                    .testTag("self_action_${action.name.lowercase()}")
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = action.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = if (action.cost > 0) formatCurrency(action.cost) else "Free",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (action.cost > 0) MaterialTheme.colorScheme.primary else EmeraldGain
                                    )
                                    Spacer(Modifier.height(3.dp))
                                    Text(
                                        text = action.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Life Chronicle",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(life.chronicle, key = { it.id }) { entry ->
            ChronicleEntryCard(entry)
        }
    }
}

@Composable
private fun ChronicleEntryCard(entry: ChronicleEntry) {
    val accent = when (entry.category) {
        ChronicleCategory.MILESTONE -> BurnishedGold
        ChronicleCategory.CAREER -> CobaltInfo
        ChronicleCategory.LOVE -> DeepTealAccent
        ChronicleCategory.FINANCE -> EmeraldGain
        ChronicleCategory.CRIME -> CrimsonLoss
        ChronicleCategory.HEALTH -> AmberWarn
        ChronicleCategory.LEGACY -> VioletSkill
        ChronicleCategory.CASCADE -> VioletSkill
        ChronicleCategory.STATE -> BurnishedGold
    }

    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(accent)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = entry.category.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = accent
                    )
                }
                Text(
                    text = "Age ${entry.age} • ${entry.year}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = entry.narrative,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (entry.tags.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    entry.tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
