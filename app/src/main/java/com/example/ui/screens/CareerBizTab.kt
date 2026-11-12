package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.*
import com.example.engine.LifeCatalog
import com.example.engine.formatCurrency
import com.example.ui.theme.*

@Composable
fun CareerBizTab(
    life: LifeState,
    onEnrollUniversity: (DegreeMajor) -> Unit,
    onEnrollGradSchool: () -> Unit,
    onApplyJob: (String) -> Unit,
    onWorkHarder: () -> Unit,
    onNegotiateRaise: () -> Unit,
    onSeekPromotion: () -> Unit,
    onOfficePolitics: (String) -> Unit,
    onQuitJob: () -> Unit,
    onToggleSideHustle: (String) -> Unit,
    onToggleSchoolClub: (SchoolClub) -> Unit,
    onFoundBusiness: (String, BusinessSector) -> Unit,
    onInvestBusiness: (String) -> Unit,
    onRaiseVenture: () -> Unit,
    onExitBusiness: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTrack by remember { mutableStateOf(CareerTrack.TECH) }
    var showFounderDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Active Career Card
        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ACTIVE CAREER", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(4.dp))
                    if (life.currentJob != null) {
                        val job = life.currentJob
                        Text(job.title, style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "${job.track.label} (Tier ${job.tier}) • ${formatCurrency(job.salary)}/yr • ${job.yearsInRole} yrs",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EmeraldGain
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Performance Rating", style = MaterialTheme.typography.bodySmall)
                            Text("${job.performance}%", style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { job.performance / 100f },
                            color = CobaltInfo,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onWorkHarder,
                                modifier = Modifier.testTag("work_harder_button")
                            ) { Text("Work Hard") }
                            FilledTonalButton(
                                onClick = onSeekPromotion,
                                modifier = Modifier.testTag("seek_promotion_button")
                            ) { Text("Seek Promotion") }
                            OutlinedButton(onClick = onNegotiateRaise) { Text("Ask Raise") }
                            TextButton(onClick = onQuitJob) { Text("Quit", color = CrimsonLoss) }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Office Politics", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AssistChip(onClick = { onOfficePolitics("MENTOR") }, label = { Text("Mentor Junior (+Ethics)") })
                            AssistChip(onClick = { onOfficePolitics("NETWORK") }, label = { Text("Executive Visibility") })
                            AssistChip(onClick = { onOfficePolitics("SABOTAGE") }, label = { Text("Sabotage Rival (Risk)") })
                        }
                    } else {
                        Text("Currently Unemployed", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Apply below once you're 16+, launch a side hustle, or incorporate a startup.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 2. Startup / Entrepreneurship Console
        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("VENTURE STUDIO", style = MaterialTheme.typography.labelSmall, color = BurnishedGold)
                            Text(
                                life.business?.name ?: "Entrepreneurship",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        if (life.business == null) {
                            Button(
                                onClick = { showFounderDialog = true },
                                enabled = life.age >= 18,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("found_company_button")
                            ) {
                                Text("Found Startup")
                            }
                        }
                    }

                    if (life.business != null) {
                        val biz = life.business
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "${biz.sector.label} • Valuation ${formatCurrency(biz.valuation)} • Your Equity ${biz.equityPercent}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EmeraldGain
                        )
                        Text(
                            "Revenue: ${formatCurrency(biz.annualRevenue)}/yr • Quality ${biz.productQuality}% • Marketing ${biz.marketingLevel}% • Morale ${biz.morale}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(onClick = { onInvestBusiness("R&D") }) { Text("Boost R&D ($15k)") }
                            OutlinedButton(onClick = { onInvestBusiness("Marketing") }) { Text("Marketing ($15k)") }
                            OutlinedButton(onClick = { onInvestBusiness("Morale") }) { Text("Team Perks ($15k)") }
                            FilledTonalButton(onClick = onRaiseVenture) { Text("Raise VC (-15% Eq)") }
                            Button(
                                onClick = onExitBusiness,
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGain, contentColor = ObsidianBg)
                            ) {
                                Text("Sell / Exit")
                            }
                        }
                    } else {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Build a SaaS, Biotech, Media, Hospitality, or Logistics company, raise VC rounds, and sell for millions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 3. Education & University & Clubs
        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("EDUCATION & CLUBS", style = MaterialTheme.typography.labelSmall, color = CobaltInfo)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${life.educationLevel.label}${life.degreeMajor?.let { " — ${it.label}" } ?: ""}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (life.age in 12..23) {
                        Spacer(Modifier.height(10.dp))
                        Text("Extracurricular Clubs (Passive Skill XP/yr, Max 2)", style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SchoolClub.entries.forEach { club ->
                                FilterChip(
                                    selected = club in life.schoolClubs,
                                    onClick = { onToggleSchoolClub(club) },
                                    label = { Text("${club.label} (+${club.skill.label})") }
                                )
                            }
                        }
                    }
                    if (life.age >= 17 && life.educationLevel.rank < EducationLevel.IN_UNIVERSITY.rank) {
                        Spacer(Modifier.height(10.dp))
                        Text("Enroll in University", style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DegreeMajor.entries.forEach { major ->
                                AssistChip(
                                    onClick = { onEnrollUniversity(major) },
                                    label = { Text("${major.label} (${formatCurrency(major.tuitionPerYear)}/yr)") }
                                )
                            }
                        }
                    } else if (life.educationLevel == EducationLevel.UNIVERSITY) {
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = onEnrollGradSchool) {
                            Text("Apply to Graduate / Doctoral School")
                        }
                    }
                }
            }
        }

        // 4. Side Hustles
        item {
            Text("Side Hustles (Max 2)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LifeCatalog.sideHustles.forEach { hustle ->
                    val active = hustle.id in life.sideHustles
                    FilterChip(
                        selected = active,
                        onClick = { onToggleSideHustle(hustle.id) },
                        label = { Text("${hustle.title} (~${formatCurrency(hustle.annualBaseIncome)}/yr)") }
                    )
                }
            }
        }

        // 5. Career Ladder Browser
        item {
            Text("Career Opportunities", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CareerTrack.entries.forEach { track ->
                    FilterChip(
                        selected = selectedTrack == track,
                        onClick = { selectedTrack = track },
                        label = { Text(track.label) }
                    )
                }
            }
        }

        val trackJobs = LifeCatalog.careerListings.filter { it.track == selectedTrack }
        items(trackJobs, key = { it.id }) { listing ->
            val meetsEdu = life.educationLevel.rank >= listing.minEducation.rank
            val meetsSkill = listing.requiredSkill == null || life.skillLevel(listing.requiredSkill) >= listing.requiredSkillLevel
            val meetsIntellect = life.stats.intellect >= listing.minIntellect
            val eligible = life.age >= 16 && meetsEdu && meetsSkill && meetsIntellect

            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, if (eligible) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Tier ${listing.tier} • ${listing.title}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Base: ${formatCurrency(listing.baseSalary)}/yr • Stress Load ${listing.stressLoad}",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldGain
                        )
                        val reqParts = buildList {
                            if (listing.minEducation != EducationLevel.NONE) add(listing.minEducation.label)
                            listing.preferredMajor?.let { add(it.label) }
                            listing.requiredSkill?.let { add("${it.label} Lv.${listing.requiredSkillLevel}") }
                            if (listing.minIntellect > 0) add("IQ ${listing.minIntellect}+")
                        }
                        Text(
                            text = "Req: ${reqParts.joinToString(" • ").ifBlank { "None" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onApplyJob(listing.id) },
                        enabled = life.currentJob?.id != listing.id,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Text(if (life.currentJob?.id == listing.id) "Current" else "Apply")
                    }
                }
            }
        }
    }

    if (showFounderDialog) {
        FoundBusinessDialog(
            cash = life.cash,
            onDismiss = { showFounderDialog = false },
            onConfirm = { name, sector ->
                showFounderDialog = false
                onFoundBusiness(name, sector)
            }
        )
    }
}

@Composable
private fun FoundBusinessDialog(
    cash: Long,
    onDismiss: () -> Unit,
    onConfirm: (String, BusinessSector) -> Unit
) {
    var companyName by remember { mutableStateOf("") }
    var selectedSector by remember { mutableStateOf(BusinessSector.SAAS) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Incorporate Venture") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Company Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Select Sector (Available Cash: ${formatCurrency(cash)})", style = MaterialTheme.typography.labelMedium)
                BusinessSector.entries.forEach { sector ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedSector == sector) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        onClick = { selectedSector = sector }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(sector.label, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                formatCurrency(sector.startupCost),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (cash >= sector.startupCost) EmeraldGain else CrimsonLoss
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(companyName, selectedSector) }) {
                Text("Launch")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
