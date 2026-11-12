package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.*
import com.example.engine.LifeCatalog
import com.example.engine.formatCurrency
import com.example.ui.theme.*

@Composable
fun LifeHudHeader(
    life: LifeState,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onOpenSavedLives: () -> Unit,
    onOpenOriginStudio: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${life.age}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = life.fullName,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "Gen ${life.generation}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        val roleLabel = when {
                            life.government.isPlayerPresident -> "${life.government.rulerTitle} • "
                            life.royal.rank != RoyalRank.NONE -> "${life.royal.rank.label} • "
                            else -> ""
                        }
                        Text(
                            text = "${roleLabel}${life.country.name} • ${life.currentJob?.title ?: life.educationLevel.label} • ${life.economicPhase.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (roleLabel.isNotEmpty()) BurnishedGold else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("toggle_theme_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                            contentDescription = "Switch visual theme"
                        )
                    }
                    IconButton(
                        onClick = onOpenSavedLives,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("saved_lives_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FolderShared,
                            contentDescription = "Saved chronicles"
                        )
                    }
                    IconButton(
                        onClick = onOpenOriginStudio,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("new_life_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddCircle,
                            contentDescription = "New chronicle",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetricPill(
                    label = "Cash",
                    value = formatCurrency(life.cash),
                    valueColor = if (life.cash >= 0) EmeraldGain else CrimsonLoss,
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    label = "Net Worth",
                    value = formatCurrency(life.netWorth),
                    valueColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    label = "Actions",
                    value = "${4 - life.actionsUsedThisYear}/4",
                    valueColor = CobaltInfo,
                    modifier = Modifier.weight(0.75f)
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MiniVitalBar("Health", life.stats.health, EmeraldGain, Modifier.weight(1f))
                MiniVitalBar("Joy", life.stats.happiness, BurnishedGold, Modifier.weight(1f))
                MiniVitalBar("Stress", life.stats.stress, if (life.stats.stress >= 75) CrimsonLoss else AmberWarn, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MetricPill(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MiniVitalBar(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "$value%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { (value / 100f).coerceIn(0f, 1f) },
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
fun SparklineChart(
    points: List<Double>,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    val strokeColor = if (isPositive) EmeraldGain else CrimsonLoss
    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas
        val minVal = points.minOrNull() ?: 0.0
        val maxVal = points.maxOrNull() ?: 1.0
        val range = (maxVal - minVal).takeIf { it > 0.0001 } ?: 1.0
        val stepX = size.width / (points.size - 1)
        val path = Path()
        points.forEachIndexed { idx, v ->
            val x = idx * stepX
            val normalizedY = ((v - minVal) / range).toFloat()
            val y = size.height - (normalizedY * size.height * 0.85f) - size.height * 0.075f
            if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = strokeColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        val lastX = (points.size - 1) * stepX
        val lastNorm = ((points.last() - minVal) / range).toFloat()
        val lastY = size.height - (lastNorm * size.height * 0.85f) - size.height * 0.075f
        drawCircle(color = strokeColor, radius = 4.dp.toPx(), center = Offset(lastX, lastY))
    }
}

@Composable
fun DilemmaCard(
    event: LifeEvent,
    skills: Map<SkillType, SkillProgress>,
    onChoose: (Int) -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_dilemma_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = "Active life dilemma",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (event.cascadeSourceNote != null) "CASCADE • ${event.category.label.uppercase()}" else "CROSSROADS • ${event.category.label.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (event.cascadeSourceNote != null) VioletSkill else MaterialTheme.colorScheme.primary
                )
            }
            if (event.cascadeSourceNote != null) {
                Spacer(Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = VioletSkill.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, VioletSkill.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = event.cascadeSourceNote,
                        style = MaterialTheme.typography.labelSmall,
                        color = VioletSkill,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = event.title,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))

            event.choices.forEachIndexed { index, choice ->
                val meetsSkill = choice.requiredSkill == null ||
                    (skills[choice.requiredSkill]?.level ?: 1) >= choice.requiredLevel
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(
                        1.dp,
                        if (choice.requiredSkill != null && meetsSkill) DeepTealAccent else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onChoose(index) }
                        .testTag("dilemma_choice_$index")
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                        Text(
                            text = choice.label,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(Modifier.height(3.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (choice.requiredSkill != null) {
                                Text(
                                    text = "${choice.requiredSkill.label} Lv.${choice.requiredLevel} ${if (meetsSkill) "✓" else "(Low Odds)"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (meetsSkill) DeepTealAccent else AmberWarn
                                )
                            }
                            Text(
                                text = choice.hintTag,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OriginStudioDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Gender, CountryProfile, OriginPreset) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf(Gender.FEMALE) }
    var selectedCountry by remember { mutableStateOf(LifeCatalog.countries.first()) }
    var selectedPreset by remember { mutableStateOf(OriginPreset.RANDOM) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Text(
                text = "Origin Studio",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Design your starting circumstances or let fate roll the dice.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Character Name (Optional)") },
                placeholder = { Text("Leave blank for procedural name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("origin_name_input")
            )

            Spacer(Modifier.height(12.dp))
            Text("Identity", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Gender.entries.forEach { g ->
                    FilterChip(
                        selected = selectedGender == g,
                        onClick = { selectedGender = g },
                        label = { Text(g.label) }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Text("Birthplace", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LifeCatalog.countries.forEach { c ->
                    FilterChip(
                        selected = selectedCountry == c,
                        onClick = { selectedCountry = c },
                        label = { Text("${c.name} (${(c.taxRate * 100).toInt()}% tax)") }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Origin Archetype", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(OriginPreset.entries) { preset ->
                    val isSelected = selectedPreset == preset
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPreset = preset }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(preset.title, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    preset.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = preset.badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onCreate(name, selectedGender, selectedCountry, selectedPreset) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("start_chronicle_button")
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Begin This Chronicle")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedLivesSheet(
    savedLives: List<LifeState>,
    activeLifeId: String,
    onDismiss: () -> Unit,
    onSelect: (LifeState) -> Unit,
    onDelete: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Text("Saved Chronicles", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Switch between parallel lives or dynasties stored locally.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            if (savedLives.isEmpty()) {
                Text("No saved chronicles yet.", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 360.dp)
                ) {
                    items(savedLives, key = { it.id }) { life ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (life.id == activeLifeId) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                1.dp,
                                if (life.id == activeLifeId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(life) }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "${life.fullName} (Gen ${life.generation})",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        "Age ${life.age} • ${life.country.name} • ${if (life.isAlive) "Alive" else "Deceased"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "Net Worth: ${formatCurrency(life.netWorth)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = { onDelete(life.id) },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        Icons.Outlined.DeleteOutline,
                                        contentDescription = "Delete saved chronicle",
                                        tint = CrimsonLoss
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EpitaphSuccessionCard(
    life: LifeState,
    onContinueAsHeir: (String?) -> Unit,
    onNewLife: () -> Unit
) {
    val livingChildren = life.relations.filter { it.role == RelationRole.CHILD && it.isAlive }
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("epitaph_card")
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_dynasty_crest),
                contentDescription = "Dynasty crest",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "IN MEMORIAM • GEN ${life.generation}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = life.fullName,
                style = MaterialTheme.typography.displaySmall
            )
            Text(
                text = "${life.legacyTitle ?: "Remembered Citizen"} • Age ${life.age}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Cause: ${life.causeOfDeath ?: "Natural Causes"} • Final Estate: ${formatCurrency(life.netWorth)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            if (livingChildren.isNotEmpty()) {
                Text(
                    text = "Select an Heir to Continue Dynasty (15% Estate Tax)",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(Modifier.height(8.dp))
                livingChildren.forEach { child ->
                    Button(
                        onClick = { onContinueAsHeir(child.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .height(48.dp)
                    ) {
                        Text("Continue as ${child.name} (Age ${child.age})")
                    }
                }
            } else {
                Button(
                    onClick = { onContinueAsHeir(null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("continue_dynasty_button")
                ) {
                    Icon(Icons.Filled.AccountTree, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Continue Dynasty via Adopted Heir")
                }
            }

            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onNewLife,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("epitaph_new_life_button")
            ) {
                Text("Start Fresh Origin")
            }
        }
    }
}
