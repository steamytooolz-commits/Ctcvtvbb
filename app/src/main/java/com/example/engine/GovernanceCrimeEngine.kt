package com.example.engine

import com.example.data.model.*
import java.util.UUID
import kotlin.math.roundToLong
import kotlin.random.Random

enum class RoyalAction(val title: String, val desc: String, val cost: Long) {
    ROYAL_GALA("Host State Gala", "+10 Approval, +6 Fame, -8 Scandal", 15_000L),
    PHILANTHROPY("Crown Relief Grant", "+14 Approval, +10 Stability, +8 Ethics", 45_000L),
    EXECUTE_DISSIDENT("Execute Dissident (18+)", "Monarch only • +14 Stability, +18 Scandal, coup risk", 0L),
    ABDICATE("Abdicate Throne", "Step down from the Crown to protect dynasty", 0L)
}

enum class PrisonAction(val title: String, val desc: String, val cost: Long) {
    YARD_WORKOUT("Yard Iron & Sparring", "+Health, +Respect, +Athletics XP", 0L),
    LAW_LIBRARY("Study Appellate Law", "+25% Appeal Prep & Rhetoric XP", 0L),
    SMUGGLE("Run Contraband Ring", "+$2k–$7k inside, +Respect, risk +1y Solitary", 0L),
    EXTORT_INMATE("Extort Cellblock", "+$1.5k–$5k, +Respect, +Notoriety", 0L),
    SHANK_RIVAL("Shank Rival Inmate (18+)", "Lethal yard hit • ++Respect & Notoriety, Death Row risk", 0L),
    RIOT("Spark Cellblock Riot", "Chaos! May drop Security tier or add +3y", 0L),
    BRIBE_WARDEN("Bribe Warden", "Slashes 3y sentence or drops Death Row -> Max", 35_000L),
    PAROLE("Petition Parole Board", "Chance of early release if Respect/Rhetoric high", 0L),
    ESCAPE("Tunnel Breakout", "Escape to Fugitive freedom (85 Heat) or +4y Supermax", 0L)
}

object GovernanceCrimeEngine {

    fun initialGovernmentFor(country: CountryProfile, rng: Random = Random): GovernmentState {
        val pres = LifeCatalog.rulerPool[rng.nextInt(LifeCatalog.rulerPool.size)]
        val mon = LifeCatalog.monarchPool[rng.nextInt(LifeCatalog.monarchPool.size)]
        val title = when (country.regimeType) {
            RegimeType.REPUBLIC -> "President"
            RegimeType.CONSTITUTIONAL_MONARCHY -> "Prime Minister"
            RegimeType.ABSOLUTE_CROWN -> "Sovereign"
        }
        return GovernmentState(
            regimeType = country.regimeType,
            rulerTitle = title,
            presidentName = pres,
            monarchName = mon,
            presidentApproval = rng.nextInt(44, 68),
            nextElectionAge = 22,
            nationalStability = rng.nextInt(64, 86),
            activeLaws = emptyList()
        )
    }

    fun annualGovernanceTick(
        state: LifeState,
        nextAge: Int,
        nextYear: Int,
        rng: Random = Random
    ): Triple<LifeState, List<ChronicleEntry>, List<String>> {
        var gov = state.government
        var royal = state.royal
        var cash = state.cash
        var stats = state.stats
        var assets = state.marketAssets
        var prison = state.prison
        var heat = state.heatLevel
        val logs = mutableListOf<ChronicleEntry>()
        val tags = mutableListOf<String>()

        // 1. Resolve any NPC-proposed bill that waited a year (if player is not the sole decider)
        val pendingBill = gov.proposedBill
        if (pendingBill != null && !gov.isPlayerPresident && royal.rank != RoyalRank.MONARCH) {
            val passes = rng.nextDouble() < 0.72
            if (passes) {
                val (afterLawState, lawMsg) = applyEnactedLawEffects(
                    state.copy(cash = cash, stats = stats, marketAssets = assets, prison = prison, heatLevel = heat, government = gov, royal = royal),
                    pendingBill.lawType,
                    enactedYear = nextYear,
                    enactedBy = "${pendingBill.proposerTitle} ${pendingBill.proposerName}"
                )
                gov = afterLawState.government.copy(proposedBill = null)
                cash = afterLawState.cash
                stats = afterLawState.stats
                assets = afterLawState.marketAssets
                prison = afterLawState.prison
                heat = afterLawState.heatLevel
                logs += ChronicleEntry(
                    id = UUID.randomUUID().toString(),
                    age = nextAge,
                    year = nextYear,
                    title = "Law Enacted: ${pendingBill.lawType.title}",
                    narrative = "${pendingBill.proposerTitle} ${pendingBill.proposerName} passed ${pendingBill.lawType.title}. $lawMsg",
                    category = ChronicleCategory.STATE,
                    tags = listOf("Active Law")
                )
            } else {
                gov = gov.copy(proposedBill = null)
                logs += ChronicleEntry(
                    id = UUID.randomUUID().toString(),
                    age = nextAge,
                    year = nextYear,
                    title = "Bill Defeated: ${pendingBill.lawType.title}",
                    narrative = "Opposition lawmakers blocked ${pendingBill.lawType.title} before final signature.",
                    category = ChronicleCategory.STATE
                )
            }
        }

        // 2. Royal Stipends, Succession & Regicide/Coup Risk
        if (royal.rank != RoyalRank.NONE && !royal.isExiled) {
            val mult = if (state.hasLaw(LawType.ROYAL_CROWN_ACT)) 2L else 1L
            val stipend = royal.rank.annualStipend * mult
            if (stipend > 0L) {
                cash += stipend
                tags += "+${formatCurrency(stipend)} Crown"
            }

            // Crown Heir ascends to Monarch
            if (royal.rank == RoyalRank.CROWN_HEIR && nextAge >= 21 && rng.nextDouble() < 0.14) {
                royal = royal.copy(rank = RoyalRank.MONARCH, approval = (royal.approval + 10).coerceAtMost(95))
                gov = gov.copy(monarchName = "Monarch ${state.fullName}")
                logs += ChronicleEntry(
                    id = UUID.randomUUID().toString(),
                    age = nextAge,
                    year = nextYear,
                    title = "Coronation: Ascended the Throne!",
                    narrative = "Long live ${state.fullName}! You were crowned Sovereign of House ${royal.houseName.ifBlank { "Realm" }}.",
                    category = ChronicleCategory.STATE,
                    tags = listOf("Monarch", "+${formatCurrency(RoyalRank.MONARCH.annualStipend)}/yr")
                )
            }

            // Revolution / Execution risk for corrupt or despised Monarch
            if (royal.rank == RoyalRank.MONARCH && (royal.scandalHeat >= 85 || royal.approval <= 12) && rng.nextDouble() < 0.45) {
                if (rng.nextDouble() < 0.40) {
                    val deadState = state.copy(
                        age = nextAge,
                        currentYear = nextYear,
                        cash = cash,
                        stats = stats.copy(health = 0),
                        royal = royal,
                        government = gov,
                        isAlive = false,
                        causeOfDeath = "Executed in Royal Revolution"
                    )
                    logs += ChronicleEntry(
                        id = UUID.randomUUID().toString(),
                        age = nextAge,
                        year = nextYear,
                        title = "Regicide! The Crown Falls",
                        narrative = "An armed uprising stormed the palace. Following a swift tribunal, the throne fell and you were executed.",
                        category = ChronicleCategory.STATE,
                        tags = listOf("Executed")
                    )
                    return Triple(deadState, logs, tags)
                } else {
                    royal = royal.copy(rank = RoyalRank.BARON, isExiled = true, scandalHeat = 20)
                    gov = gov.copy(regimeType = RegimeType.REPUBLIC, rulerTitle = "President")
                    logs += ChronicleEntry(
                        id = UUID.randomUUID().toString(),
                        age = nextAge,
                        year = nextYear,
                        title = "Forced Abdication & Exile",
                        narrative = "Parliament abolished the Crown's authority amid soaring scandal. You fled into gilded exile.",
                        category = ChronicleCategory.STATE,
                        tags = listOf("Exiled")
                    )
                }
            }
        }

        // 3. Presidential Salary & Term Cycle
        if (gov.isPlayerPresident && !prison.isInPrison) {
            cash += 380_000L
            tags += "+$380k Gov Pay"
            val newTerm = gov.termYearsServed + 1
            gov = gov.copy(
                termYearsServed = newTerm,
                presidentApproval = (gov.presidentApproval + (stats.ethics - 50) / 20 - (heat / 25)).coerceIn(5, 98)
            )
            if (newTerm >= 4 && !state.hasLaw(LawType.MARTIAL_LAW)) {
                val reelected = newTerm < 8 && gov.presidentApproval >= 46 && rng.nextDouble() < 0.72
                if (!reelected) {
                    val nextPres = LifeCatalog.rulerPool[rng.nextInt(LifeCatalog.rulerPool.size)]
                    gov = gov.copy(isPlayerPresident = false, presidentName = nextPres, termYearsServed = 0)
                    logs += ChronicleEntry(
                        id = UUID.randomUUID().toString(),
                        age = nextAge,
                        year = nextYear,
                        title = "Presidential Term Concluded",
                        narrative = "After $newTerm years in office, you handed the executive seal to $nextPres.",
                        category = ChronicleCategory.STATE
                    )
                }
            }
        } else if (nextAge >= 18 && gov.proposedBill == null && rng.nextDouble() < 0.30) {
            // NPC President or Monarch proposes a law!
            val availableLaws = LawType.entries.filter { lt -> gov.activeLaws.none { it.type == lt } }
            if (availableLaws.isNotEmpty()) {
                val chosenLaw = availableLaws[rng.nextInt(availableLaws.size)]
                val useMonarch = gov.regimeType != RegimeType.REPUBLIC &&
                    royal.rank != RoyalRank.MONARCH &&
                    (gov.regimeType == RegimeType.ABSOLUTE_CROWN || rng.nextDouble() < 0.45)
                val proposerTitle = if (useMonarch) "Sovereign" else gov.rulerTitle
                val proposerName = if (useMonarch) gov.monarchName else gov.presidentName
                gov = gov.copy(
                    proposedBill = ProposedBill(
                        id = UUID.randomUUID().toString(),
                        lawType = chosenLaw,
                        proposerTitle = proposerTitle,
                        proposerName = proposerName,
                        proposedAge = nextAge
                    )
                )
                logs += ChronicleEntry(
                    id = UUID.randomUUID().toString(),
                    age = nextAge,
                    year = nextYear,
                    title = "Bill Proposed: ${chosenLaw.title}",
                    narrative = "$proposerTitle $proposerName introduced the ${chosenLaw.title}: ${chosenLaw.summary}",
                    category = ChronicleCategory.STATE,
                    tags = listOf("Pending Vote")
                )
            }
        }

        // 4. Universal Healthcare Law perk
        if (state.hasLaw(LawType.UNIVERSAL_HEALTHCARE)) {
            stats = stats.copy(health = (stats.health + 3).coerceAtMost(100))
        }

        val updated = state.copy(
            cash = cash,
            stats = stats.clamp(),
            marketAssets = assets,
            prison = prison,
            heatLevel = heat,
            government = gov,
            royal = royal
        )
        return Triple(updated, logs, tags)
    }

    fun applyEnactedLawEffects(
        state: LifeState,
        lawType: LawType,
        enactedYear: Int,
        enactedBy: String
    ): Pair<LifeState, String> {
        // Remove conflicting laws
        val conflicts = when (lawType) {
            LawType.WEALTH_SURTAX -> setOf(LawType.CORPORATE_TAX_CUT)
            LawType.CORPORATE_TAX_CUT -> setOf(LawType.WEALTH_SURTAX)
            LawType.CRYPTO_CRACKDOWN -> setOf(LawType.OPEN_TRADE_ACCORD)
            LawType.CRIMINAL_AMNESTY -> setOf(LawType.MANDATORY_MINIMUMS, LawType.MARTIAL_LAW)
            LawType.MARTIAL_LAW -> setOf(LawType.CRIMINAL_AMNESTY)
            else -> emptySet()
        }
        val filtered = state.government.activeLaws.filterNot { it.type == lawType || it.type in conflicts }
        val newList = (listOf(EnactedLaw(lawType, enactedYear, enactedBy)) + filtered).take(5)

        var cash = state.cash
        var heat = state.heatLevel
        var prison = state.prison
        var assets = state.marketAssets
        var stability = state.government.nationalStability

        val effectNote = when {
            lawType.isAmnesty -> {
                heat = 0
                if (prison.isInPrison && !prison.isDeathRow && !prison.isLifeSentence) {
                    prison = PrisonState(notoriety = prison.notoriety)
                    "All non-lethal prison terms were commuted and your Heat was wiped!"
                } else {
                    "Your criminal Heat dropped to 0%."
                }
            }
            lawType.isCryptoBan -> {
                assets = assets.map {
                    if (it.isCrypto) it.copy(previousPrice = it.currentPrice, currentPrice = (it.currentPrice * 0.68).coerceAtLeast(1.0))
                    else it
                }
                "Crypto markets plunged 32% and exchanges froze retail buys."
            }
            lawType == LawType.CORPORATE_TAX_CUT -> {
                assets = assets.map {
                    if (!it.isCrypto) it.copy(previousPrice = it.currentPrice, currentPrice = it.currentPrice * 1.08)
                    else it
                }
                stability = (stability - 4).coerceIn(10, 100)
                "Equities rallied +8% immediately on tax relief."
            }
            lawType == LawType.WEALTH_SURTAX -> {
                stability = (stability + 6).coerceIn(10, 100)
                "Public stability rose (+6%), while $1M+ earners face a +12% surtax."
            }
            else -> lawType.summary
        }

        val updatedGov = state.government.copy(
            activeLaws = newList,
            nationalStability = stability,
            proposedBill = null
        )
        return state.copy(
            cash = cash,
            heatLevel = heat,
            prison = prison,
            marketAssets = assets,
            government = updatedGov
        ) to effectNote
    }

    fun enactLawAsRuler(state: LifeState, lawType: LawType): Pair<LifeState, String> {
        val isPres = state.government.isPlayerPresident
        val isMonarch = state.royal.rank == RoyalRank.MONARCH
        if (!isPres && !isMonarch) {
            return state to "Only the sitting President or Sovereign Monarch can enact laws directly."
        }
        val title = if (isMonarch) "Monarch" else "President"
        val (updated, note) = applyEnactedLawEffects(state, lawType, state.currentYear, "$title ${state.fullName}")
        val decrees = if (isMonarch) updated.royal.decreesPassed + 1 else updated.royal.decreesPassed
        val entry = ChronicleEntry(
            id = UUID.randomUUID().toString(),
            age = state.age,
            year = state.currentYear,
            title = "Decreed: ${lawType.title}",
            narrative = "You signed ${lawType.title} into national law. $note",
            category = ChronicleCategory.STATE,
            tags = listOf(title, "Law Passed")
        )
        return updated.copy(
            royal = updated.royal.copy(decreesPassed = decrees),
            chronicle = (listOf(entry) + updated.chronicle).take(120)
        ) to "Enacted ${lawType.title}!"
    }

    fun respondToPendingBill(state: LifeState, support: Boolean, lobbyCost: Long = 0L): Pair<LifeState, String> {
        val bill = state.government.proposedBill ?: return state to "No pending bill right now."
        val isRuler = state.government.isPlayerPresident || state.royal.rank == RoyalRank.MONARCH

        if (isRuler) {
            return if (support) {
                enactLawAsRuler(state, bill.lawType)
            } else {
                val entry = ChronicleEntry(
                    id = UUID.randomUUID().toString(),
                    age = state.age,
                    year = state.currentYear,
                    title = "Vetoed: ${bill.lawType.title}",
                    narrative = "Exercised executive veto to strike down ${bill.lawType.title}.",
                    category = ChronicleCategory.STATE
                )
                state.copy(
                    government = state.government.copy(proposedBill = null),
                    chronicle = (listOf(entry) + state.chronicle).take(120)
                ) to "Vetoed ${bill.lawType.title}."
            }
        }

        // Citizen lobbying ($20k)
        if (state.cash < lobbyCost) return state to "Need ${formatCurrency(lobbyCost)} to lobby lawmakers."
        return if (support) {
            val (updated, note) = applyEnactedLawEffects(
                state.copy(cash = state.cash - lobbyCost),
                bill.lawType,
                state.currentYear,
                "${bill.proposerTitle} ${bill.proposerName}"
            )
            val entry = ChronicleEntry(
                id = UUID.randomUUID().toString(),
                age = state.age,
                year = state.currentYear,
                title = "Lobbied Law: ${bill.lawType.title}",
                narrative = "Backed ${bill.lawType.title} through key committee votes. $note",
                category = ChronicleCategory.STATE
            )
            updated.copy(chronicle = (listOf(entry) + updated.chronicle).take(120)) to "Lobby succeeded—${bill.lawType.title} passed!"
        } else {
            val entry = ChronicleEntry(
                id = UUID.randomUUID().toString(),
                age = state.age,
                year = state.currentYear,
                title = "Blocked Bill: ${bill.lawType.title}",
                narrative = "Funded an opposition coalition that killed ${bill.lawType.title} in committee.",
                category = ChronicleCategory.STATE
            )
            state.copy(
                cash = state.cash - lobbyCost,
                government = state.government.copy(proposedBill = null),
                chronicle = (listOf(entry) + state.chronicle).take(120)
            ) to "Lobby succeeded—${bill.lawType.title} blocked!"
        }
    }

    fun runForPresident(state: LifeState, campaignBudget: Long, rng: Random = Random): Pair<LifeState, String> {
        if (state.age < 25) return state to "Must be at least age 25 to run for Head of State."
        if (state.prison.isInPrison) return state to "Cannot run for office while incarcerated."
        if (state.government.isPlayerPresident) return state to "You are already the sitting President!"
        if (state.hasLaw(LawType.MARTIAL_LAW)) return state to "Elections are suspended under Emergency Martial Law!"
        if (state.cash < campaignBudget) return state to "Need ${formatCurrency(campaignBudget)} for campaign war chest."
        if (state.actionsUsedThisYear >= 4) return state to "No energy left this year."

        val rhetoric = state.skillLevel(SkillType.RHETORIC)
        val fameScore = state.stats.fame / 200.0
        val budgetBoost = if (campaignBudget >= 150_000L) 0.24 else 0.10
        val winOdds = (0.14 + rhetoric * 0.055 + fameScore + budgetBoost + (state.stats.charisma / 250.0) - (state.prison.notoriety / 220.0)).coerceIn(0.08, 0.88)

        val won = rng.nextDouble() < winOdds
        return if (won) {
            val entry = ChronicleEntry(
                id = UUID.randomUUID().toString(),
                age = state.age,
                year = state.currentYear,
                title = "Elected President!",
                narrative = "${state.fullName} swept the national election with a ${formatCurrency(campaignBudget)} campaign and assumed supreme executive office!",
                category = ChronicleCategory.STATE,
                tags = listOf("President", "$380k/yr")
            )
            state.copy(
                cash = state.cash - campaignBudget,
                actionsUsedThisYear = state.actionsUsedThisYear + 1,
                stats = state.stats.copy(fame = state.stats.fame + 22, happiness = state.stats.happiness + 16).clamp(),
                government = state.government.copy(
                    isPlayerPresident = true,
                    presidentName = state.fullName,
                    presidentApproval = 62,
                    termYearsServed = 0
                ),
                chronicle = (listOf(entry) + state.chronicle).take(120)
            ) to "Victory! You were elected President of ${state.country.name}!"
        } else {
            val entry = ChronicleEntry(
                id = UUID.randomUUID().toString(),
                age = state.age,
                year = state.currentYear,
                title = "Presidential Bid Fell Short",
                narrative = "Spent ${formatCurrency(campaignBudget)} on the campaign trail, falling short at the ballot box.",
                category = ChronicleCategory.STATE
            )
            state.copy(
                cash = state.cash - campaignBudget,
                actionsUsedThisYear = state.actionsUsedThisYear + 1,
                stats = state.stats.copy(fame = state.stats.fame + 6, stress = state.stats.stress + 10).clamp(),
                chronicle = (listOf(entry) + state.chronicle).take(120)
            ) to "Election defeat—grow Rhetoric & Fame before running again."
        }
    }

    fun seekRoyalMarriage(state: LifeState, rng: Random = Random): Pair<LifeState, String> {
        if (state.age < 18) return state to "Must be 18+ to court nobility."
        if (state.royal.rank.ordinal >= RoyalRank.DUKE.ordinal) return state to "You already hold high royal rank (${state.royal.rank.label})."
        if (state.actionsUsedThisYear >= 4) return state to "No energy left this year."
        if (state.stats.fame < 40 && state.netWorth < 750_000L) {
            return state to "Royal court invitations require Fame 40+ or Net Worth $750k+."
        }
        val odds = (0.35 + state.stats.charisma / 220.0 + state.stats.fame / 250.0).coerceAtMost(0.85)
        return if (rng.nextDouble() < odds) {
            val royalName = "Prince/Lady ${LifeCatalog.randomFirstName(Gender.entries[rng.nextInt(Gender.entries.size)], rng)} of Crownhouse"
            val royalSpouse = NpcRelation(
                id = UUID.randomUUID().toString(),
                name = royalName,
                role = RelationRole.SPOUSE,
                age = (state.age + rng.nextInt(-3, 5)).coerceAtLeast(18),
                closeness = 78,
                trust = 74,
                archetype = NpcArchetype.AMBITIOUS,
                occupation = "Royal Consort",
                netWorth = 2_400_000L,
                isRoyal = true
            )
            val newRank = if (state.royal.rank == RoyalRank.NONE) RoyalRank.BARON else RoyalRank.DUKE
            val entry = ChronicleEntry(
                id = UUID.randomUUID().toString(),
                age = state.age,
                year = state.currentYear,
                title = "Royal Wedding: Elevated to ${newRank.label}!",
                narrative = "Married $royalName in a grand cathedral ceremony, entering the aristocracy!",
                category = ChronicleCategory.STATE,
                tags = listOf(newRank.label, "+${formatCurrency(newRank.annualStipend)}/yr")
            )
            state.copy(
                relations = state.relations + royalSpouse,
                royal = state.royal.copy(rank = newRank, houseName = "Windsor-Vane", approval = 70),
                stats = state.stats.copy(fame = state.stats.fame + 20, happiness = state.stats.happiness + 14).clamp(),
                actionsUsedThisYear = state.actionsUsedThisYear + 1,
                chronicle = (listOf(entry) + state.chronicle).take(120)
            ) to "Married into Royalty! Elevated to ${newRank.label}!"
        } else {
            state.copy(actionsUsedThisYear = state.actionsUsedThisYear + 1) to "The royal gala matchmakers turned you down this season."
        }
    }

    fun performRoyalAction(state: LifeState, action: RoyalAction, rng: Random = Random): Pair<LifeState, String> {
        if (state.royal.rank == RoyalRank.NONE) return state to "Requires Royal title."
        if (state.cash < action.cost) return state to "Need ${formatCurrency(action.cost)}."
        if (state.actionsUsedThisYear >= 4) return state to "No energy left this year."

        return when (action) {
            RoyalAction.ROYAL_GALA -> {
                state.copy(
                    cash = state.cash - action.cost,
                    royal = state.royal.copy(
                        approval = (state.royal.approval + 10).coerceAtMost(100),
                        scandalHeat = (state.royal.scandalHeat - 8).coerceAtLeast(0)
                    ),
                    stats = state.stats.copy(fame = state.stats.fame + 6, charisma = state.stats.charisma + 3).clamp(),
                    actionsUsedThisYear = state.actionsUsedThisYear + 1
                ) to "Hosted glittering State Gala (+10% Approval, -8 Scandal)."
            }
            RoyalAction.PHILANTHROPY -> {
                state.copy(
                    cash = state.cash - action.cost,
                    royal = state.royal.copy(approval = (state.royal.approval + 14).coerceAtMost(100)),
                    government = state.government.copy(nationalStability = (state.government.nationalStability + 10).coerceAtMost(100)),
                    stats = state.stats.copy(ethics = state.stats.ethics + 8).clamp(),
                    actionsUsedThisYear = state.actionsUsedThisYear + 1
                ) to "Disbursed Crown Relief Grant (+14% Royal Approval, +10% Stability)."
            }
            RoyalAction.EXECUTE_DISSIDENT -> {
                if (state.age < 18 || state.royal.rank != RoyalRank.MONARCH) {
                    return state to "Only an adult Monarch (King/Queen) can sign royal execution warrants."
                }
                val entry = ChronicleEntry(
                    id = UUID.randomUUID().toString(),
                    age = state.age,
                    year = state.currentYear,
                    title = "Royal Execution Decree",
                    narrative = "Ordered the public execution of an accused traitor at dawn. Dissent froze, though whispers of tyranny spread.",
                    category = ChronicleCategory.STATE,
                    tags = listOf("+18 Scandal", "+14 Stability")
                )
                state.copy(
                    royal = state.royal.copy(
                        approval = (state.royal.approval - 12).coerceAtLeast(0),
                        scandalHeat = (state.royal.scandalHeat + 18).coerceAtMost(100)
                    ),
                    government = state.government.copy(nationalStability = (state.government.nationalStability + 14).coerceAtMost(100)),
                    stats = state.stats.copy(ethics = state.stats.ethics - 16, stress = state.stats.stress + 8).clamp(),
                    actionsUsedThisYear = state.actionsUsedThisYear + 1,
                    chronicle = (listOf(entry) + state.chronicle).take(120)
                ) to "Signed execution warrant—dissent crushed (+18 Scandal)."
            }
            RoyalAction.ABDICATE -> {
                if (state.royal.rank != RoyalRank.MONARCH) return state to "Only the Monarch can abdicate the throne."
                val entry = ChronicleEntry(
                    id = UUID.randomUUID().toString(),
                    age = state.age,
                    year = state.currentYear,
                    title = "Royal Abdication",
                    narrative = "Stepped down from the throne peacefully, retiring as Duke/Duchess.",
                    category = ChronicleCategory.STATE
                )
                state.copy(
                    royal = state.royal.copy(rank = RoyalRank.DUKE, scandalHeat = 5),
                    actionsUsedThisYear = state.actionsUsedThisYear + 1,
                    chronicle = (listOf(entry) + state.chronicle).take(120)
                ) to "Abdicated the throne peacefully."
            }
        }
    }

    fun killTargetNpc(state: LifeState, npcId: String, rng: Random = Random): Pair<LifeState, String> {
        if (state.age < 18) return state to "Lethal underworld hits unlock at age 18+."
        if (state.prison.isInPrison) return state to "Already behind bars!"
        if (state.actionsUsedThisYear >= 4) return state to "No energy left this year."
        val target = state.relations.firstOrNull { it.id == npcId && it.isAlive } ?: return state to "Target unavailable."

        val street = state.skillLevel(SkillType.STREETCRAFT)
        val martialPenalty = if (state.hasLaw(LawType.MARTIAL_LAW)) 0.18 else 0.0
        val odds = (0.34 + street * 0.055 - martialPenalty).coerceIn(0.15, 0.85)
        val skills = state.skills.toMutableMap()
        skills[SkillType.STREETCRAFT] = addSkillXp(skills[SkillType.STREETCRAFT], 75)

        return if (rng.nextDouble() < odds) {
            val inheritance = if (target.role == RelationRole.SPOUSE || target.role == RelationRole.FATHER || target.role == RelationRole.MOTHER) {
                (target.netWorth * 0.45).roundToLong()
            } else 0L
            val updatedRelations = state.relations.map { if (it.id == npcId) it.copy(isAlive = false) else it }
            val arc = DelayedArc(
                id = UUID.randomUUID().toString(),
                arcKey = "whistleblower_retaliation",
                triggerAge = state.age + 2,
                stage = 1,
                originChoiceLabel = "Contract elimination of ${target.name} at Age ${state.age}"
            )
            val entry = ChronicleEntry(
                id = UUID.randomUUID().toString(),
                age = state.age,
                year = state.currentYear,
                title = "Target Eliminated: ${target.name}",
                narrative = "Arranged a silent lethal hit on ${target.name}. " +
                    if (inheritance > 0L) "You collected ${formatCurrency(inheritance)} from the estate." else "Cold case opened by detectives.",
                category = ChronicleCategory.CRIME,
                tags = listOf("+34 Heat", "+Notoriety")
            )
            state.copy(
                cash = state.cash + inheritance,
                relations = updatedRelations,
                heatLevel = (state.heatLevel + 34).coerceAtMost(100),
                prison = state.prison.copy(notoriety = state.prison.notoriety + 28),
                skills = skills,
                pendingArcs = state.pendingArcs + arc,
                lastCascadeSummary = "Cold-case homicide heat lingering...",
                stats = state.stats.copy(ethics = state.stats.ethics - 25, stress = state.stats.stress + 14).clamp(),
                actionsUsedThisYear = state.actionsUsedThisYear + 1,
                chronicle = (listOf(entry) + state.chronicle).take(120)
            ) to "Hit executed on ${target.name}." + if (inheritance > 0L) " (+${formatCurrency(inheritance)} estate)" else ""
        } else {
            sendToPrison(
                state = state.copy(skills = skills, actionsUsedThisYear = state.actionsUsedThisYear + 1),
                crimeTitle = "Premeditated Murder (${target.name})",
                baseYears = 25,
                isLethal = true
            )
        }
    }

    fun sendToPrison(state: LifeState, crimeTitle: String, baseYears: Int, isLethal: Boolean): Pair<LifeState, String> {
        val strictMult = if (state.hasLaw(LawType.MANDATORY_MINIMUMS)) 1.5 else 1.0
        val finalYears = (baseYears * strictMult).roundToLong().toInt().coerceAtLeast(1)
        val deathPenalty = isLethal && (state.hasLaw(LawType.CAPITAL_PUNISHMENT) || state.hasLaw(LawType.MARTIAL_LAW))
        val lifeTerm = !deathPenalty && isLethal && finalYears >= 25
        val sec = when {
            deathPenalty -> PrisonSecurity.DEATH_ROW
            finalYears >= 12 || isLethal -> PrisonSecurity.SUPERMAX
            finalYears >= 5 -> PrisonSecurity.MAXIMUM
            else -> PrisonSecurity.MINIMUM
        }
        val label = if (deathPenalty) "Death Row (Execution in 3y)" else if (lifeTerm) "Life Imprisonment" else "$finalYears years (${sec.label})"
        val entry = ChronicleEntry(
            id = UUID.randomUUID().toString(),
            age = state.age,
            year = state.currentYear,
            title = "Convicted: $crimeTitle",
            narrative = "Found guilty of $crimeTitle. Remanded under $label.",
            category = ChronicleCategory.CRIME,
            tags = listOf(sec.label)
        )
        return state.copy(
            currentJob = null,
            heatLevel = 10,
            prison = state.prison.copy(
                isInPrison = true,
                yearsRemaining = finalYears,
                totalSentence = finalYears,
                crimeName = crimeTitle,
                notoriety = state.prison.notoriety + finalYears * 4,
                securityLevel = sec,
                isLifeSentence = lifeTerm,
                isDeathRow = deathPenalty,
                yearsUntilExecution = if (deathPenalty) 3 else 0
            ),
            stats = state.stats.copy(ethics = state.stats.ethics - 14, happiness = state.stats.happiness - 22).clamp(),
            chronicle = (listOf(entry) + state.chronicle).take(120)
        ) to "Sentenced to $label!"
    }

    fun joinPrisonGang(state: LifeState, gang: PrisonGang): Pair<LifeState, String> {
        if (!state.prison.isInPrison) return state to "Not incarcerated."
        return state.copy(
            prison = state.prison.copy(gang = gang, respect = (state.prison.respect + 8).coerceAtMost(100))
        ) to "Joined ${gang.label} (${gang.perkDesc})."
    }

    fun performPrisonAction(state: LifeState, action: PrisonAction, rng: Random = Random): Pair<LifeState, String> {
        val p = state.prison
        if (!p.isInPrison) return state to "Not incarcerated."
        if (state.actionsUsedThisYear >= 4) return state to "No energy left this year."
        if (state.cash < action.cost) return state to "Need ${formatCurrency(action.cost)}."

        val skills = state.skills.toMutableMap()
        val gang = p.gang
        return when (action) {
            PrisonAction.YARD_WORKOUT -> {
                skills[SkillType.ATHLETICS] = addSkillXp(skills[SkillType.ATHLETICS], 50)
                state.copy(
                    skills = skills,
                    prison = p.copy(respect = (p.respect + 8).coerceAtMost(100)),
                    stats = state.stats.copy(health = state.stats.health + 7, stress = state.stats.stress - 6).clamp(),
                    actionsUsedThisYear = state.actionsUsedThisYear + 1
                ) to "Lifted yard iron (+7 Health, +8 Respect)."
            }
            PrisonAction.LAW_LIBRARY -> {
                skills[SkillType.RHETORIC] = addSkillXp(skills[SkillType.RHETORIC], 45)
                state.copy(
                    skills = skills,
                    prison = p.copy(lawStudyProgress = (p.lawStudyProgress + 25).coerceAtMost(100)),
                    stats = state.stats.copy(intellect = state.stats.intellect + 3).clamp(),
                    actionsUsedThisYear = state.actionsUsedThisYear + 1
                ) to "Studied case precedents (Appeal Prep: ${(p.lawStudyProgress + 25).coerceAtMost(100)}%)."
            }
            PrisonAction.SMUGGLE -> {
                val bonus = if (gang == PrisonGang.IRON_SYNDICATE) 0.16 else 0.0
                if (rng.nextDouble() < 0.62 + bonus) {
                    val haul = rng.nextLong(2_200L, 7_500L)
                    state.copy(
                        cash = state.cash + haul,
                        prison = p.copy(respect = (p.respect + 9).coerceAtMost(100), notoriety = p.notoriety + 5),
                        actionsUsedThisYear = state.actionsUsedThisYear + 1
                    ) to "Smuggled contraband inside cellblock (+${formatCurrency(haul)})."
                } else {
                    state.copy(
                        prison = p.copy(yearsRemaining = p.yearsRemaining + 1, respect = (p.respect - 4).coerceAtLeast(0)),
                        stats = state.stats.copy(stress = state.stats.stress + 10).clamp(),
                        actionsUsedThisYear = state.actionsUsedThisYear + 1
                    ) to "Guards raided your bunk! Added +1 year & solitary."
                }
            }
            PrisonAction.EXTORT_INMATE -> {
                val bonus = if (gang == PrisonGang.IRON_SYNDICATE || gang == PrisonGang.APEX_ENFORCERS) 0.15 else 0.0
                if (rng.nextDouble() < 0.58 + p.respect / 250.0 + bonus) {
                    val cashTake = rng.nextLong(1_500L, 5_000L)
                    state.copy(
                        cash = state.cash + cashTake,
                        prison = p.copy(respect = (p.respect + 10).coerceAtMost(100), notoriety = p.notoriety + 8),
                        actionsUsedThisYear = state.actionsUsedThisYear + 1
                    ) to "Collected cellblock protection (+${formatCurrency(cashTake)}, +10 Respect)."
                } else {
                    state.copy(
                        stats = state.stats.copy(health = state.stats.health - 12, stress = state.stats.stress + 10).clamp(),
                        actionsUsedThisYear = state.actionsUsedThisYear + 1
                    ) to "Yard brawl broke out—took a beating (-12 Health)."
                }
            }
            PrisonAction.SHANK_RIVAL -> {
                val bonus = if (gang == PrisonGang.APEX_ENFORCERS) 0.18 else 0.0
                if (rng.nextDouble() < 0.56 + state.skillLevel(SkillType.ATHLETICS) * 0.04 + bonus) {
                    val entry = ChronicleEntry(
                        id = UUID.randomUUID().toString(),
                        age = state.age,
                        year = state.currentYear,
                        title = "Yard Execution",
                        narrative = "Eliminated a rival shot-caller in the showers; guards couldn't pin the blade on you.",
                        category = ChronicleCategory.CRIME,
                        tags = listOf("+22 Respect", "+18 Notoriety")
                    )
                    state.copy(
                        prison = p.copy(respect = (p.respect + 22).coerceAtMost(100), notoriety = p.notoriety + 18),
                        stats = state.stats.copy(ethics = state.stats.ethics - 18).clamp(),
                        actionsUsedThisYear = state.actionsUsedThisYear + 1,
                        chronicle = (listOf(entry) + state.chronicle).take(120)
                    ) to "Rival shot-caller eliminated—the yard fears you (+22 Respect)."
                } else {
                    val deathPenalty = state.hasLaw(LawType.CAPITAL_PUNISHMENT)
                    state.copy(
                        prison = p.copy(
                            securityLevel = if (deathPenalty) PrisonSecurity.DEATH_ROW else PrisonSecurity.SUPERMAX,
                            yearsRemaining = p.yearsRemaining + 12,
                            isDeathRow = deathPenalty || p.isDeathRow,
                            yearsUntilExecution = if (deathPenalty && !p.isDeathRow) 3 else p.yearsUntilExecution
                        ),
                        actionsUsedThisYear = state.actionsUsedThisYear + 1
                    ) to if (deathPenalty) "Caught on camera! Re-sentenced to Death Row!" else "Caught! +12y added & moved to Supermax."
                }
            }
            PrisonAction.RIOT -> {
                val power = if (gang == PrisonGang.APEX_ENFORCERS) 0.15 else 0.0
                if (rng.nextDouble() < 0.48 + p.respect / 220.0 + power) {
                    state.copy(
                        prison = p.copy(
                            respect = (p.respect + 16).coerceAtMost(100),
                            yearsRemaining = (p.yearsRemaining - 1).coerceAtLeast(1),
                            securityLevel = if (p.securityLevel == PrisonSecurity.SUPERMAX) PrisonSecurity.MAXIMUM else p.securityLevel
                        ),
                        actionsUsedThisYear = state.actionsUsedThisYear + 1
                    ) to "Riot forced Warden concessions (-1y sentence, +16 Respect)!"
                } else {
                    state.copy(
                        prison = p.copy(yearsRemaining = p.yearsRemaining + 3, securityLevel = PrisonSecurity.SUPERMAX),
                        stats = state.stats.copy(health = state.stats.health - 15).clamp(),
                        actionsUsedThisYear = state.actionsUsedThisYear + 1
                    ) to "Riot crushed by tactical unit (-15 Health, +3y Supermax)."
                }
            }
            PrisonAction.BRIBE_WARDEN -> {
                val nextYears = (p.yearsRemaining - 3).coerceAtLeast(1)
                state.copy(
                    cash = state.cash - action.cost,
                    prison = p.copy(
                        yearsRemaining = nextYears,
                        isDeathRow = false,
                        yearsUntilExecution = 0,
                        securityLevel = if (p.securityLevel == PrisonSecurity.DEATH_ROW) PrisonSecurity.MAXIMUM else p.securityLevel
                    ),
                    actionsUsedThisYear = state.actionsUsedThisYear + 1
                ) to "Warden pocketed ${formatCurrency(action.cost)} (Death Row lifted / -3y term)."
            }
            PrisonAction.PAROLE -> {
                if (p.isDeathRow) return state to "Death Row inmates must bribe, appeal, or break out—no parole."
                val lawBonus = if (gang == PrisonGang.LAWHOUSE_CIRCLE) 0.14 else 0.0
                val odds = (0.18 + state.skillLevel(SkillType.RHETORIC) * 0.05 + p.lawStudyProgress / 280.0 + lawBonus - p.notoriety / 300.0).coerceIn(0.05, 0.78)
                if (rng.nextDouble() < odds) {
                    val entry = ChronicleEntry(
                        id = UUID.randomUUID().toString(),
                        age = state.age,
                        year = state.currentYear,
                        title = "Parole Board Granted Release",
                        narrative = "Convinced the Parole Board of rehabilitation and walked free early.",
                        category = ChronicleCategory.CRIME
                    )
                    state.copy(
                        prison = PrisonState(notoriety = p.notoriety),
                        actionsUsedThisYear = state.actionsUsedThisYear + 1,
                        chronicle = (listOf(entry) + state.chronicle).take(120)
                    ) to "Parole granted! You are walking free!"
                } else {
                    state.copy(actionsUsedThisYear = state.actionsUsedThisYear + 1) to "Parole board denied early release."
                }
            }
            PrisonAction.ESCAPE -> {
                val secPenalty = when (p.securityLevel) {
                    PrisonSecurity.MINIMUM -> 0.0
                    PrisonSecurity.MAXIMUM -> 0.12
                    PrisonSecurity.SUPERMAX -> 0.24
                    PrisonSecurity.DEATH_ROW -> 0.30
                }
                val odds = (0.24 + state.skillLevel(SkillType.STREETCRAFT) * 0.05 + state.skillLevel(SkillType.ATHLETICS) * 0.03 - secPenalty).coerceIn(0.08, 0.72)
                if (rng.nextDouble() < odds) {
                    val entry = ChronicleEntry(
                        id = UUID.randomUUID().toString(),
                        age = state.age,
                        year = state.currentYear,
                        title = "Prison Breakout!",
                        narrative = "Escaped custody through a service conduit! Authorities placed an urgent warrant (85% Heat).",
                        category = ChronicleCategory.CRIME,
                        tags = listOf("Fugitive", "85 Heat")
                    )
                    state.copy(
                        prison = PrisonState(notoriety = p.notoriety + 30),
                        heatLevel = 85,
                        actionsUsedThisYear = state.actionsUsedThisYear + 1,
                        chronicle = (listOf(entry) + state.chronicle).take(120)
                    ) to "Escaped! You're free as a high-heat fugitive!"
                } else {
                    state.copy(
                        prison = p.copy(
                            yearsRemaining = p.yearsRemaining + 4,
                            escapeAttempts = p.escapeAttempts + 1,
                            securityLevel = if (p.isDeathRow) PrisonSecurity.DEATH_ROW else PrisonSecurity.SUPERMAX
                        ),
                        actionsUsedThisYear = state.actionsUsedThisYear + 1
                    ) to "Siren alarm! Recaptured in perimeter sweep (+4y Supermax)."
                }
            }
        }
    }

    private fun addSkillXp(current: SkillProgress?, delta: Int): SkillProgress {
        val base = current ?: return SkillProgress(SkillType.ENGINEERING, delta)
        return base.copy(xp = (base.xp + delta).coerceAtMost(950))
    }
}
