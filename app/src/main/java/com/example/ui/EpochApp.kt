package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.components.LifeHudHeader
import com.example.ui.components.OriginStudioDialog
import com.example.ui.components.SavedLivesSheet
import com.example.ui.screens.*
import com.example.ui.theme.EpochLifeTheme
import kotlinx.coroutines.delay

@Composable
fun EpochApp(viewModel: EpochViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val life = state.activeLife
    var legacySection by remember { mutableStateOf(LegacySection.CROWN_GOV) }

    // Auto-clear transient feedback banner after 3.2s
    LaunchedEffect(state.bannerFeedback) {
        if (state.bannerFeedback != null) {
            delay(3200L)
            viewModel.dismissFeedback()
        }
    }

    // Back press on secondary tabs returns cleanly to Chronicle tab
    if (state.currentTab != EpochTab.CHRONICLE) {
        BackHandler {
            viewModel.selectTab(EpochTab.CHRONICLE)
        }
    }

    EpochLifeTheme(darkTheme = state.isDarkTheme) {
        if (life == null) {
            Surface(modifier = Modifier.fillMaxSize()) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            return@EpochLifeTheme
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isExpanded = maxWidth >= 680.dp

            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                topBar = {
                    Column {
                        LifeHudHeader(
                            life = life,
                            isDarkTheme = state.isDarkTheme,
                            onToggleTheme = viewModel::toggleTheme,
                            onOpenSavedLives = { viewModel.openSavedLivesSheet(true) },
                            onOpenOriginStudio = { viewModel.openOriginStudio(true) }
                        )
                        AnimatedVisibility(visible = state.bannerFeedback != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = state.bannerFeedback ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(onClick = viewModel::dismissFeedback) {
                                        Text("OK")
                                    }
                                }
                            }
                        }
                    }
                },
                bottomBar = {
                    if (!isExpanded) {
                        NavigationBar(
                            windowInsets = WindowInsets.navigationBars,
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            EpochTab.entries.forEach { tab ->
                                val selected = state.currentTab == tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { viewModel.selectTab(tab) },
                                    icon = {
                                        EpochAppTabIcon(
                                            tab = tab,
                                            selected = selected,
                                            unreadInboxCount = life.unreadInboxCount,
                                            hasStateAlert = life.government.proposedBill != null || life.prison.isInPrison
                                        )
                                    },
                                    label = { Text(tab.label, maxLines = 1) },
                                    modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                                )
                            }
                        }
                    }
                },
                floatingActionButton = {
                    if (life.isAlive) {
                        ExtendedFloatingActionButton(
                            onClick = viewModel::ageUpOneYear,
                            containerColor = if (life.pendingEvent != null) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                            contentColor = if (life.pendingEvent != null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.testTag("age_up_fab")
                        ) {
                            Icon(Icons.Filled.FastForward, contentDescription = "Age up one year")
                            Spacer(Modifier.width(8.dp))
                            Text(if (life.pendingEvent != null) "Resolve Event" else "Age +1 Year")
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isExpanded) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            Spacer(Modifier.height(12.dp))
                            EpochTab.entries.forEach { tab ->
                                val selected = state.currentTab == tab
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { viewModel.selectTab(tab) },
                                    icon = {
                                        EpochAppTabIcon(
                                            tab = tab,
                                            selected = selected,
                                            unreadInboxCount = life.unreadInboxCount,
                                            hasStateAlert = life.government.proposedBill != null || life.prison.isInPrison
                                        )
                                    },
                                    label = { Text(tab.label) }
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        val contentModifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 780.dp)

                        when (state.currentTab) {
                            EpochTab.CHRONICLE -> ChronicleTab(
                                life = life,
                                onResolveDilemma = viewModel::resolveDilemma,
                                onSelfAction = viewModel::performSelfAction,
                                onAppealPrison = viewModel::appealSentence,
                                onContinueAsHeir = viewModel::continueAsHeir,
                                onOpenOriginStudio = { viewModel.openOriginStudio(true) },
                                onOpenNetworkInbox = { viewModel.selectTab(EpochTab.NETWORK) },
                                onOpenCrownGov = {
                                    legacySection = LegacySection.CROWN_GOV
                                    viewModel.selectTab(EpochTab.LEGACY)
                                },
                                onOpenPrisonYard = {
                                    legacySection = LegacySection.CRIME_PRISON
                                    viewModel.selectTab(EpochTab.LEGACY)
                                },
                                modifier = contentModifier
                            )
                            EpochTab.CAREER -> CareerBizTab(
                                life = life,
                                onEnrollUniversity = viewModel::enrollUniversity,
                                onEnrollGradSchool = viewModel::enrollGradSchool,
                                onApplyJob = viewModel::applyForJob,
                                onWorkHarder = viewModel::workHarder,
                                onNegotiateRaise = viewModel::negotiateRaise,
                                onSeekPromotion = viewModel::seekPromotion,
                                onOfficePolitics = viewModel::officePolitics,
                                onQuitJob = viewModel::quitJob,
                                onToggleSideHustle = viewModel::toggleSideHustle,
                                onToggleSchoolClub = viewModel::toggleSchoolClub,
                                onFoundBusiness = viewModel::foundBusiness,
                                onInvestBusiness = viewModel::investInBusiness,
                                onRaiseVenture = viewModel::raiseVentureRound,
                                onExitBusiness = viewModel::exitBusiness,
                                modifier = contentModifier
                            )
                            EpochTab.MARKETS -> MarketsAssetsTab(
                                life = life,
                                onBuyAsset = viewModel::buyAsset,
                                onSellAsset = viewModel::sellAsset,
                                onBuyRealEstate = viewModel::buyRealEstate,
                                onToggleRent = viewModel::toggleRentProperty,
                                onSellProperty = viewModel::sellProperty,
                                onPayDebt = viewModel::payDebt,
                                onSetLifestyle = viewModel::setLifestyle,
                                onBuyLuxuryItem = viewModel::buyLuxuryItem,
                                onPlayHighStakes = viewModel::playHighStakesGame,
                                modifier = contentModifier
                            )
                            EpochTab.NETWORK -> NetworkTab(
                                life = life,
                                onInteract = viewModel::interactWithNpc,
                                onMeetPerson = viewModel::meetNewPerson,
                                onReplyToMessage = viewModel::replyToNpcMessage,
                                onDirectDialogue = viewModel::applyDirectDialogue,
                                modifier = contentModifier
                            )
                            EpochTab.LEGACY -> LegacyProfileTab(
                                life = life,
                                legacyRecords = state.legacyRecords,
                                activeSection = legacySection,
                                onSelectSection = { legacySection = it },
                                onCommitCrime = viewModel::commitCrime,
                                onKillTargetNpc = viewModel::killTargetNpc,
                                onPrisonAction = viewModel::performPrisonAction,
                                onJoinPrisonGang = viewModel::joinPrisonGang,
                                onAppealPrison = viewModel::appealSentence,
                                onRunForPresident = viewModel::runForPresident,
                                onEnactLaw = viewModel::enactLaw,
                                onRespondToBill = viewModel::respondToPendingBill,
                                onSeekRoyalMarriage = viewModel::seekRoyalMarriage,
                                onRoyalAction = viewModel::performRoyalAction,
                                modifier = contentModifier
                            )
                        }
                    }
                }
            }

            if (state.showOriginStudio) {
                OriginStudioDialog(
                    onDismiss = { viewModel.openOriginStudio(false) },
                    onCreate = viewModel::startNewLife
                )
            }

            if (state.showSavedLivesSheet) {
                SavedLivesSheet(
                    savedLives = state.savedLives,
                    activeLifeId = life.id,
                    onDismiss = { viewModel.openSavedLivesSheet(false) },
                    onSelect = viewModel::loadSavedLife,
                    onDelete = viewModel::deleteSavedLife
                )
            }
        }
    }
}

@Composable
private fun TabNavIcon(
    tab: EpochTab,
    selected: Boolean,
    unreadInboxCount: Int
) {
    EpochAppTabIcon(tab, selected, unreadInboxCount, false)
}

@Composable
private fun EpochAppTabIcon(
    tab: EpochTab,
    selected: Boolean,
    unreadInboxCount: Int,
    hasStateAlert: Boolean
) {
    when {
        tab == EpochTab.NETWORK && unreadInboxCount > 0 -> {
            BadgedBox(
                badge = {
                    Badge {
                        Text(if (unreadInboxCount > 9) "9+" else unreadInboxCount.toString())
                    }
                }
            ) {
                Icon(tabIcon(tab, selected), contentDescription = "${tab.label} ($unreadInboxCount unread)")
            }
        }
        tab == EpochTab.LEGACY && hasStateAlert -> {
            BadgedBox(badge = { Badge() }) {
                Icon(tabIcon(tab, selected), contentDescription = "${tab.label} alert")
            }
        }
        else -> {
            Icon(tabIcon(tab, selected), contentDescription = tab.label)
        }
    }
}

private fun tabIcon(tab: EpochTab, selected: Boolean) = when (tab) {
    EpochTab.CHRONICLE -> if (selected) Icons.Filled.AutoStories else Icons.Outlined.AutoStories
    EpochTab.CAREER -> if (selected) Icons.Filled.Work else Icons.Outlined.WorkOutline
    EpochTab.MARKETS -> if (selected) Icons.AutoMirrored.Filled.ShowChart else Icons.AutoMirrored.Outlined.ShowChart
    EpochTab.NETWORK -> if (selected) Icons.Filled.People else Icons.Outlined.PeopleOutline
    EpochTab.LEGACY -> if (selected) Icons.Filled.AccountTree else Icons.Outlined.AccountTree
}
