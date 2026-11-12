package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.LegacyHallEntity
import com.example.data.model.*
import com.example.data.repository.LifeRepository
import com.example.engine.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class EpochTab(val label: String) {
    CHRONICLE("Chronicle"),
    CAREER("Career & Biz"),
    MARKETS("Markets"),
    NETWORK("Network"),
    LEGACY("Legacy")
}

data class EpochUiState(
    val activeLife: LifeState? = null,
    val savedLives: List<LifeState> = emptyList(),
    val legacyRecords: List<LegacyHallEntity> = emptyList(),
    val currentTab: EpochTab = EpochTab.CHRONICLE,
    val isDarkTheme: Boolean = true,
    val showOriginStudio: Boolean = false,
    val showSavedLivesSheet: Boolean = false,
    val bannerFeedback: String? = null
)

class EpochViewModel(private val repository: LifeRepository) : ViewModel() {
    private val _activeLife = MutableStateFlow<LifeState?>(null)
    private val _currentTab = MutableStateFlow(EpochTab.CHRONICLE)
    private val _isDarkTheme = MutableStateFlow(true)
    private val _showOriginStudio = MutableStateFlow(false)
    private val _showSavedLivesSheet = MutableStateFlow(false)
    private val _bannerFeedback = MutableStateFlow<String?>(null)

    val uiState: StateFlow<EpochUiState> = combine(
        _activeLife,
        repository.savedLives,
        repository.legacyRecords,
        _currentTab,
        _isDarkTheme,
        _showOriginStudio,
        _showSavedLivesSheet,
        _bannerFeedback
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        EpochUiState(
            activeLife = values[0] as LifeState?,
            savedLives = values[1] as List<LifeState>,
            legacyRecords = values[2] as List<LegacyHallEntity>,
            currentTab = values[3] as EpochTab,
            isDarkTheme = values[4] as Boolean,
            showOriginStudio = values[5] as Boolean,
            showSavedLivesSheet = values[6] as Boolean,
            bannerFeedback = values[7] as String?
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = EpochUiState()
    )

    init {
        viewModelScope.launch {
            val latest = repository.getMostRecentLife()
            if (latest != null) {
                _activeLife.value = latest
            } else {
                val starter = LifeSimulator.createNewLife(preset = OriginPreset.RANDOM)
                _activeLife.value = starter
                repository.saveLife(starter)
            }
        }
    }

    fun selectTab(tab: EpochTab) {
        _currentTab.value = tab
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun openOriginStudio(open: Boolean) {
        _showOriginStudio.value = open
    }

    fun openSavedLivesSheet(open: Boolean) {
        _showSavedLivesSheet.value = open
    }

    fun dismissFeedback() {
        _bannerFeedback.value = null
    }

    fun startNewLife(
        customName: String,
        gender: Gender,
        country: CountryProfile,
        preset: OriginPreset
    ) {
        val created = LifeSimulator.createNewLife(
            customName = customName,
            gender = gender,
            country = country,
            preset = preset
        )
        _activeLife.value = created
        _currentTab.value = EpochTab.CHRONICLE
        _showOriginStudio.value = false
        _bannerFeedback.value = "New chronicle started: ${created.fullName}"
        persist(created)
    }

    fun loadSavedLife(life: LifeState) {
        _activeLife.value = life
        _showSavedLivesSheet.value = false
        _currentTab.value = EpochTab.CHRONICLE
        _bannerFeedback.value = "Loaded ${life.fullName} (Age ${life.age})"
    }

    fun deleteSavedLife(lifeId: String) {
        viewModelScope.launch {
            repository.deleteLife(lifeId)
            if (_activeLife.value?.id == lifeId) {
                val next = repository.getMostRecentLife()
                    ?: LifeSimulator.createNewLife(preset = OriginPreset.RANDOM).also { repository.saveLife(it) }
                _activeLife.value = next
            }
        }
    }

    fun ageUpOneYear() {
        val current = _activeLife.value ?: return
        if (!current.isAlive) return
        if (current.pendingEvent != null) {
            _bannerFeedback.value = "Resolve the active dilemma before advancing to next year."
            return
        }
        val next = LifeSimulator.advanceYear(current)
        _activeLife.value = next
        persist(next)
        if (!next.isAlive) {
            viewModelScope.launch { repository.recordLegacy(next) }
        }
    }

    fun resolveDilemma(choiceIndex: Int) {
        val current = _activeLife.value ?: return
        val next = LifeSimulator.resolveEventChoice(current, choiceIndex)
        _activeLife.value = next
        persist(next)
    }

    fun continueAsHeir(heirId: String? = null) {
        val current = _activeLife.value ?: return
        val heir = LifeSimulator.continueAsHeir(current, heirId)
        _activeLife.value = heir
        _currentTab.value = EpochTab.CHRONICLE
        _bannerFeedback.value = "Generation ${heir.generation} begins as ${heir.fullName}!"
        persist(heir)
    }

    fun performSelfAction(action: SelfAction) = applyPair { LifeSimulator.performSelfAction(it, action) }
    fun enrollUniversity(major: DegreeMajor) = applyPair { LifeSimulator.enrollUniversity(it, major) }
    fun enrollGradSchool() = applyPair { LifeSimulator.enrollGradSchool(it) }
    fun applyForJob(jobId: String) = applyPair { LifeSimulator.applyForJob(it, jobId) }
    fun workHarder() = applyPair { LifeSimulator.workHarder(it) }
    fun negotiateRaise() = applyPair { LifeSimulator.negotiateRaise(it) }
    fun quitJob() = applyPair { LifeSimulator.quitJob(it) }
    fun toggleSideHustle(hustleId: String) = applyPair { LifeSimulator.toggleSideHustle(it, hustleId) }
    fun foundBusiness(name: String, sector: BusinessSector) = applyPair { LifeSimulator.foundBusiness(it, name, sector) }
    fun investInBusiness(focus: String) = applyPair { LifeSimulator.investInBusiness(it, focus) }
    fun raiseVentureRound() = applyPair { LifeSimulator.raiseVentureRound(it) }
    fun exitBusiness() = applyPair { LifeSimulator.exitBusiness(it) }
    fun buyAsset(ticker: String, dollars: Long) = applyPair { LifeSimulator.buyAsset(it, ticker, dollars) }
    fun sellAsset(ticker: String, fraction: Double = 1.0) = applyPair { LifeSimulator.sellAsset(it, ticker, fraction) }
    fun buyRealEstate(type: PropertyType, mortgage: Boolean) = applyPair { LifeSimulator.buyRealEstate(it, type, mortgage) }
    fun toggleRentProperty(id: String) = applyPair { LifeSimulator.toggleRentProperty(it, id) }
    fun sellProperty(id: String) = applyPair { LifeSimulator.sellProperty(it, id) }
    fun payDebt(amount: Long) = applyPair { LifeSimulator.payDebt(it, amount) }
    fun interactWithNpc(npcId: String, interaction: NpcInteraction) = applyPair { LifeSimulator.interactWithNpc(it, npcId, interaction) }
    fun meetNewPerson(romantic: Boolean) = applyPair { LifeSimulator.meetNewPerson(it, romantic) }
    fun commitCrime(crime: CrimeAction) = applyPair { LifeSimulator.commitCrime(it, crime) }
    fun appealSentence() = applyPair { LifeSimulator.appealSentence(it) }
    fun setLifestyle(tier: LifestyleTier) = applyPair { LifeSimulator.setLifestyle(it, tier) }
    fun toggleSchoolClub(club: SchoolClub) = applyPair { LifeSimulator.toggleSchoolClub(it, club) }
    fun buyLuxuryItem(item: LuxuryItem) = applyPair { LifeSimulator.buyLuxuryItem(it, item) }
    fun playHighStakesGame(stake: Long) = applyPair { LifeSimulator.playHighStakesGame(it, stake) }
    fun seekPromotion() = applyPair { LifeSimulator.seekPromotion(it) }
    fun officePolitics(tactic: String) = applyPair { LifeSimulator.officePolitics(it, tactic) }
    fun replyToNpcMessage(messageId: String, replyIndex: Int) = applyPair { LifeSimulator.replyToNpcMessage(it, messageId, replyIndex) }
    fun applyDirectDialogue(npcId: String, reply: MessageReply) = applyPair { LifeSimulator.applyDirectDialogue(it, npcId, reply) }
    fun runForPresident(budget: Long) = applyPair { LifeSimulator.runForPresident(it, budget) }
    fun enactLaw(lawType: LawType) = applyPair { LifeSimulator.enactLawAsRuler(it, lawType) }
    fun respondToPendingBill(support: Boolean, lobbyCost: Long = 0L) = applyPair { LifeSimulator.respondToPendingBill(it, support, lobbyCost) }
    fun seekRoyalMarriage() = applyPair { LifeSimulator.seekRoyalMarriage(it) }
    fun performRoyalAction(action: RoyalAction) = applyPair { LifeSimulator.performRoyalAction(it, action) }
    fun killTargetNpc(npcId: String) = applyPair { LifeSimulator.killTargetNpc(it, npcId) }
    fun performPrisonAction(action: PrisonAction) = applyPair { LifeSimulator.performPrisonAction(it, action) }
    fun joinPrisonGang(gang: PrisonGang) = applyPair { LifeSimulator.joinPrisonGang(it, gang) }

    private fun applyPair(action: (LifeState) -> Pair<LifeState, String>) {
        val current = _activeLife.value ?: return
        val (updated, feedback) = action(current)
        _activeLife.value = updated
        _bannerFeedback.value = feedback
        if (updated != current) persist(updated)
    }

    private fun persist(state: LifeState) {
        viewModelScope.launch { repository.saveLife(state) }
    }

    class Factory(private val repository: LifeRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EpochViewModel(repository) as T
        }
    }
}
