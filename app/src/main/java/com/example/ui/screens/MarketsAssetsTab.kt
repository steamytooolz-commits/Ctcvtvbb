package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.*
import com.example.engine.formatCurrency
import com.example.ui.components.SparklineChart
import com.example.ui.theme.*

private enum class AssetsSection(val label: String) {
    MARKETS("Equities & Crypto"),
    REAL_ESTATE("Real Estate"),
    LIFESTYLE("Lifestyle & Stakes")
}

@Composable
fun MarketsAssetsTab(
    life: LifeState,
    onBuyAsset: (String, Long) -> Unit,
    onSellAsset: (String, Double) -> Unit,
    onBuyRealEstate: (PropertyType, Boolean) -> Unit,
    onToggleRent: (String) -> Unit,
    onSellProperty: (String) -> Unit,
    onPayDebt: (Long) -> Unit,
    onSetLifestyle: (LifestyleTier) -> Unit,
    onBuyLuxuryItem: (LuxuryItem) -> Unit,
    onPlayHighStakes: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSection by remember { mutableStateOf(AssetsSection.MARKETS) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Macro Economy & Portfolio Summary
        item {
            val econColor = when (life.economicPhase) {
                EconomicPhase.BOOM, EconomicPhase.EXPANSION -> EmeraldGain
                EconomicPhase.STAGFLATION -> AmberWarn
                EconomicPhase.RECESSION, EconomicPhase.CRASH -> CrimsonLoss
            }
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, econColor.copy(alpha = 0.65f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("MACRO CLIMATE", style = MaterialTheme.typography.labelSmall, color = econColor)
                            Text(life.economicPhase.label, style = MaterialTheme.typography.headlineSmall)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Portfolio", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                formatCurrency(life.portfolioValue),
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFamily),
                                color = EmeraldGain
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Inflation ${(life.economicPhase.inflationRate * 100).let { "%.1f".format(it) }}% • Real Estate: ${formatCurrency(life.realEstateEquity)} • Collectibles: ${formatCurrency(life.collectiblesValue)} • Debt: ${formatCurrency(life.debt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (life.debt > 0L) {
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onPayDebt(5_000L) },
                                enabled = life.cash >= 500L,
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("Pay $5k Debt")
                            }
                            OutlinedButton(
                                onClick = { onPayDebt(life.debt) },
                                enabled = life.cash >= life.debt,
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("Pay Off All")
                            }
                        }
                    }
                }
            }
        }

        // Section switcher: Equities/Crypto, Real Estate, Lifestyle & Stakes
        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = activeSection == AssetsSection.MARKETS,
                    onClick = { activeSection = AssetsSection.MARKETS },
                    label = { Text("Equities & Crypto (${life.marketAssets.size})") },
                    modifier = Modifier.testTag("tab_equities_chip")
                )
                FilterChip(
                    selected = activeSection == AssetsSection.REAL_ESTATE,
                    onClick = { activeSection = AssetsSection.REAL_ESTATE },
                    label = { Text("Real Estate (${life.realEstate.size})") },
                    modifier = Modifier.testTag("tab_realestate_chip")
                )
                FilterChip(
                    selected = activeSection == AssetsSection.LIFESTYLE,
                    onClick = { activeSection = AssetsSection.LIFESTYLE },
                    label = { Text("Lifestyle & Stakes") },
                    modifier = Modifier.testTag("tab_lifestyle_chip")
                )
            }
        }

        when (activeSection) {
            AssetsSection.MARKETS -> {
                items(life.marketAssets, key = { it.ticker }) { asset ->
                    AssetCard(
                        asset = asset,
                        cashAvailable = life.cash,
                        onBuy = { amount -> onBuyAsset(asset.ticker, amount) },
                        onSell = { frac -> onSellAsset(asset.ticker, frac) }
                    )
                }
            }

            AssetsSection.REAL_ESTATE -> {
                if (life.realEstate.isNotEmpty()) {
                    item {
                        Text("Owned Properties", style = MaterialTheme.typography.titleMedium)
                    }
                    items(life.realEstate, key = { it.id }) { prop ->
                        Card(
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(prop.name, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        formatCurrency(prop.currentValue),
                                        style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFamily),
                                        color = EmeraldGain
                                    )
                                }
                                Text(
                                    "Mortgage: ${formatCurrency(prop.mortgageRemaining)} • Rent: ${formatCurrency(prop.annualRent)}/yr • ${if (prop.isRentedOut) "Leased" else "Owner Occupied"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilledTonalButton(
                                        onClick = { onToggleRent(prop.id) },
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Text(if (prop.isRentedOut) "Move In" else "Lease Out")
                                    }
                                    OutlinedButton(
                                        onClick = { onSellProperty(prop.id) },
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Text("Sell Property")
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text("Property Market", style = MaterialTheme.typography.titleMedium)
                }
                items(PropertyType.entries) { type ->
                    val down = (type.basePrice * 0.20).toLong()
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(type.label, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    formatCurrency(type.basePrice),
                                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFamily),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                "Est. Rent: ${formatCurrency(type.baseRent)}/yr • Upkeep: ${formatCurrency(type.maintenance)}/yr",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onBuyRealEstate(type, true) },
                                    enabled = life.cash >= down,
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Text("20% Down (${formatCurrency(down)})")
                                }
                                OutlinedButton(
                                    onClick = { onBuyRealEstate(type, false) },
                                    enabled = life.cash >= type.basePrice,
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Text("Buy Cash")
                                }
                            }
                        }
                    }
                }
            }

            AssetsSection.LIFESTYLE -> {
                // Lifestyle Tier Selector
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Weekend, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Living Standard", style = MaterialTheme.typography.titleMedium)
                    }
                }
                items(LifestyleTier.entries) { tier ->
                    val isSelected = life.lifestyle == tier
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tier.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${formatCurrency(tier.annualCost)}/yr • Joy ${signed(tier.joyDelta)} • Stress ${signed(tier.stressDelta)} • Fame +${tier.fameGain}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            if (isSelected) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        "ACTIVE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            } else {
                                FilledTonalButton(
                                    onClick = { onSetLifestyle(tier) },
                                    enabled = life.isAlive,
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("lifestyle_${tier.name.lowercase()}")
                                ) {
                                    Text("Select")
                                }
                            }
                        }
                    }
                }

                // Luxury Collectibles
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(Icons.Filled.Diamond, contentDescription = null, tint = BurnishedGold)
                        Text("Luxury Collectibles (${life.collectibles.size}/${LuxuryItem.entries.size})", style = MaterialTheme.typography.titleMedium)
                    }
                }
                items(LuxuryItem.entries) { item ->
                    val owned = item in life.collectibles
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(
                            1.dp,
                            if (owned) EmeraldGain.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${formatCurrency(item.price)} • ${item.perkSummary}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            if (owned) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = "Owned", tint = EmeraldGain, modifier = Modifier.size(18.dp))
                                    Text("Owned", style = MaterialTheme.typography.labelMedium, color = EmeraldGain)
                                }
                            } else {
                                Button(
                                    onClick = { onBuyLuxuryItem(item) },
                                    enabled = life.isAlive && life.age >= 18 && life.cash >= item.price,
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("luxury_${item.name.lowercase()}")
                                ) {
                                    Text("Acquire")
                                }
                            }
                        }
                    }
                }

                // High-Stakes Tables
                item {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, VioletSkill.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.Casino, contentDescription = "High-stakes table", tint = VioletSkill)
                                Text("HIGH-STAKES TABLES", style = MaterialTheme.typography.labelSmall, color = VioletSkill)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Finance & Streetcraft boost your edge. Uses 1 annual energy.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))
                            val canPlay = life.isAlive && life.age >= 18 && !life.prison.isInPrison && life.actionsUsedThisYear < 4
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(1_000L, 10_000L, 50_000L).forEach { stake ->
                                    FilledTonalButton(
                                        onClick = { onPlayHighStakes(stake) },
                                        enabled = canPlay && life.cash >= stake,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("high_stakes_$stake")
                                    ) {
                                        Text(formatCurrency(stake), maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun signed(v: Int): String = if (v >= 0) "+$v" else "$v"

@Composable
private fun AssetCard(
    asset: MarketAsset,
    cashAvailable: Long,
    onBuy: (Long) -> Unit,
    onSell: (Double) -> Unit
) {
    val pct = asset.dayChangePercent
    val positive = pct >= 0.0
    val changeColor = if (positive) EmeraldGain else CrimsonLoss

    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            asset.ticker,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        if (asset.isCrypto) {
                            Text("CRYPTO", style = MaterialTheme.typography.labelSmall, color = VioletSkill)
                        }
                    }
                    Text(asset.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                SparklineChart(
                    points = asset.history,
                    isPositive = positive,
                    modifier = Modifier
                        .width(78.dp)
                        .height(34.dp)
                )

                Spacer(Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "$${"%.2f".format(asset.currentPrice)}",
                        style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFamily)
                    )
                    Text(
                        "${if (positive) "+" else ""}${"%.1f".format(pct)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = changeColor
                    )
                }
            }

            if (asset.sharesOwned > 0.0) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Holding: ${"%.2f".format(asset.sharesOwned)} shares (${formatCurrency(asset.positionValue)})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = { onBuy(1_000L) },
                    enabled = cashAvailable >= 1_000L,
                    modifier = Modifier.height(48.dp)
                ) { Text("+$1k") }
                FilledTonalButton(
                    onClick = { onBuy(10_000L) },
                    enabled = cashAvailable >= 10_000L,
                    modifier = Modifier.height(48.dp)
                ) { Text("+$10k") }
                if (asset.sharesOwned > 0.0) {
                    OutlinedButton(
                        onClick = { onSell(0.5) },
                        modifier = Modifier.height(48.dp)
                    ) { Text("Sell 50%") }
                    OutlinedButton(
                        onClick = { onSell(1.0) },
                        modifier = Modifier.height(48.dp)
                    ) { Text("Sell All") }
                }
            }
        }
    }
}
