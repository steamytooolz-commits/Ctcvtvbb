package com.example.engine

import com.example.data.model.*
import java.util.UUID
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlin.random.Random

enum class SelfAction(
    val title: String,
    val subtitle: String,
    val minAge: Int,
    val cost: Long
) {
    WORKOUT("Athletic Training", "Boosts Health, cuts Stress, trains Athletics", 8, 25L),
    STUDY("Deep Study", "Raises Intellect & Engineering/Finance XP", 6, 0L),
    MEDITATE("Mindful Reset", "Lowers Stress sharply, restores Happiness", 8, 0L),
    THERAPY("Executive Therapy", "Deep Stress relief & emotional clarity", 16, 450L),
    CREATE_ART("Studio Practice", "Trains Arts skill & lifts Happiness", 6, 40L),
    NETWORK("Industry Mixer", "Trains Rhetoric, Charisma & Fame", 18, 180L),
    DOCTOR("Comprehensive Checkup", "Restores Health & screens chronic risks", 0, 320L),
    VOLUNTEER("Community Outreach", "Raises Ethics, Happiness & lowers Heat", 12, 0L),
    STREET_HUSTLE("Back-Alley Hustle", "Trains Streetcraft, risky quick cash (+Heat)", 14, 0L)
}

enum class NpcInteraction(val title: String, val cost: Long) {
    DEEP_TALK("Heart-to-Heart", 0L),
    QUALITY_TIME("Spend Day Together", 80L),
    GIFT("Thoughtful Gift", 500L),
    PROPOSE("Propose Marriage", 2_500L),
    HAVE_CHILD("Start / Expand Family", 1_500L),
    MENTOR("Mentor & Invest", 2_000L),
    CUT_TIES("Distance Yourself", 0L)
}

enum class CrimeAction(val title: String, val riskDesc: String, val minAge: Int, val isLethal: Boolean = false) {
    PICKPOCKET("Pickpocket Metro", "Petty theft • $250–$1.4k, 1y risk", 14),
    BURGLARY("Luxury Home Burglary", "Break & enter • $4k–$16k, 2y risk", 16),
    GRAND_THEFT_AUTO("Exotic Auto Boost", "Chop-shop ring • $18k–$55k, 3y risk", 18),
    CYBER_PHISH("Corporate Ransomware", "High-tech extortion • $14k–$42k, 4y risk", 18),
    EXTORTION("Protection Racket", "Shakedown executives • $25k–$75k, 5y risk", 18),
    INSIDER_TRADE("Insider Block Trade", "Market manipulation • $40k–$125k, 5y risk", 21),
    VAULT_HEIST("Syndicate Vault Heist", "Armed bullion heist • $140k–$460k, 8y Supermax", 21),
    CONTRACT_MURDER("Contract Hit (18+)", "Lethal hit for hire • $95k–$280k, Life/Death Row!", 18, isLethal = true),
    ASSASSINATE_RULER("Assassinate Ruler (18+)", "Strike sitting President/Monarch • Reshapes regime!", 18, isLethal = true)
}

object LifeSimulator {

    fun createNewLife(
        customName: String = "",
        gender: Gender = Gender.entries.random(),
        country: CountryProfile = LifeCatalog.countries.random(),
        preset: OriginPreset = OriginPreset.RANDOM,
        rng: Random = Random
    ): LifeState {
        val surname = if (customName.trim().contains(" ")) {
            customName.trim().substringAfterLast(" ")
        } else {
            LifeCatalog.lastNames[rng.nextInt(LifeCatalog.lastNames.size)]
        }
        val finalName = customName.trim().ifBlank { LifeCatalog.randomFullName(gender, surname, rng) }
        val birthYear = 2026 - rng.nextInt(18, 28)

        val (stats, skills, traits, economicPhase, cash) = when (preset) {
            OriginPreset.RANDOM -> Quintuple(
                Stats(
                    health = rng.nextInt(65, 96),
                    happiness = rng.nextInt(60, 92),
                    intellect = rng.nextInt(45, 88),
                    charisma = rng.nextInt(40, 88),
                    discipline = rng.nextInt(38, 82),
                    stress = rng.nextInt(4, 15),
                    ethics = rng.nextInt(45, 85),
                    fame = 0
                ),
                SkillType.entries.associateWith { SkillProgress(it, rng.nextInt(0, 40)) },
                listOf("Blank Slate"),
                EconomicPhase.EXPANSION,
                preset.startingCash
            )
            OriginPreset.ORPHAN_GRIT -> Quintuple(
                Stats(health = 84, happiness = 52, intellect = 68, charisma = 54, discipline = 84, stress = 22, ethics = 62, fame = 0),
                SkillType.entries.associateWith {
                    val xp = if (it == SkillType.STREETCRAFT || it == SkillType.ATHLETICS) 140 else 20
                    SkillProgress(it, xp)
                },
                listOf("Self-Reliant", "Iron Grit"),
                EconomicPhase.EXPANSION,
                preset.startingCash
            )
            OriginPreset.TRUST_FUND -> Quintuple(
                Stats(health = 88, happiness = 82, intellect = 66, charisma = 78, discipline = 40, stress = 8, ethics = 48, fame = 18),
                SkillType.entries.associateWith {
                    val xp = if (it == SkillType.FINANCE || it == SkillType.RHETORIC) 150 else 30
                    SkillProgress(it, xp)
                },
                listOf("Old Money", "Connected"),
                EconomicPhase.BOOM,
                preset.startingCash
            )
            OriginPreset.RECESSION_PRODIGY -> Quintuple(
                Stats(health = 78, happiness = 64, intellect = 90, charisma = 58, discipline = 76, stress = 16, ethics = 70, fame = 0),
                SkillType.entries.associateWith {
                    val xp = if (it == SkillType.ENGINEERING || it == SkillType.FINANCE) 180 else 25
                    SkillProgress(it, xp)
                },
                listOf("Sharp Mind", "Contrarian"),
                EconomicPhase.RECESSION,
                preset.startingCash
            )
            OriginPreset.SYNDICATE_BLOOD -> Quintuple(
                Stats(health = 82, happiness = 60, intellect = 64, charisma = 76, discipline = 64, stress = 20, ethics = 28, fame = 8),
                SkillType.entries.associateWith {
                    val xp = if (it == SkillType.STREETCRAFT || it == SkillType.RHETORIC) 210 else 25
                    SkillProgress(it, xp)
                },
                listOf("Street-Smart", "Marked Bloodline"),
                EconomicPhase.STAGFLATION,
                preset.startingCash
            )
            OriginPreset.SCHOLAR_DYNASTY -> Quintuple(
                Stats(health = 80, happiness = 74, intellect = 86, charisma = 64, discipline = 78, stress = 12, ethics = 82, fame = 4),
                SkillType.entries.associateWith {
                    val xp = if (it == SkillType.ARTS || it == SkillType.ENGINEERING) 160 else 40
                    SkillProgress(it, xp)
                },
                listOf("Polymath Roots", "Bookworm"),
                EconomicPhase.EXPANSION,
                preset.startingCash
            )
            OriginPreset.ROYAL_BLOOD -> Quintuple(
                Stats(health = 88, happiness = 78, intellect = 72, charisma = 84, discipline = 64, stress = 14, ethics = 52, fame = 42),
                SkillType.entries.associateWith {
                    val xp = if (it == SkillType.RHETORIC || it == SkillType.ATHLETICS) 160 else 35
                    SkillProgress(it, xp)
                },
                listOf("Royal Bloodline", "Sovereign Heir"),
                EconomicPhase.EXPANSION,
                preset.startingCash
            )
        }

        val effectiveCountry = if (preset == OriginPreset.ROYAL_BLOOD && country.regimeType == RegimeType.REPUBLIC) {
            country.copy(regimeType = RegimeType.CONSTITUTIONAL_MONARCHY)
        } else country

        val relations = buildInitialFamily(surname, preset, rng)
        val heat = if (preset == OriginPreset.SYNDICATE_BLOOD) 18 else 0
        val initialRoyal = if (preset == OriginPreset.ROYAL_BLOOD) {
            RoyalState(rank = RoyalRank.CROWN_HEIR, houseName = surname, approval = 72)
        } else RoyalState()
        val initialGov = GovernanceCrimeEngine.initialGovernmentFor(effectiveCountry, rng)

        val introEntry = ChronicleEntry(
            id = UUID.randomUUID().toString(),
            age = 0,
            year = birthYear,
            title = "A New Chronicle Begins",
            narrative = "$finalName was born in ${effectiveCountry.name} under the '${preset.title}' origin. " +
                if (relations.isNotEmpty()) "Welcomed by ${relations.joinToString(", ") { "${it.name} (${it.role.label})" }}." else "Facing the world alone from day one.",
            category = ChronicleCategory.MILESTONE,
            tags = listOf(effectiveCountry.name, preset.badge)
        )

        return LifeState(
            id = UUID.randomUUID().toString(),
            dynastyId = UUID.randomUUID().toString(),
            generation = 1,
            fullName = finalName,
            gender = gender,
            country = effectiveCountry,
            originPreset = preset,
            age = 0,
            birthYear = birthYear,
            currentYear = birthYear,
            cash = cash,
            stats = stats,
            skills = skills,
            traits = traits,
            marketAssets = LifeCatalog.initialMarketAssets(),
            relations = relations,
            chronicle = listOf(introEntry),
            economicPhase = economicPhase,
            heatLevel = heat,
            royal = initialRoyal,
            government = initialGov
        )
    }

    fun continueAsHeir(deceasedState: LifeState, heirId: String? = null, rng: Random = Random): LifeState {
        val chosenChild = deceasedState.relations.firstOrNull { it.id == heirId && it.isAlive }
            ?: deceasedState.relations.firstOrNull { it.role == RelationRole.CHILD && it.isAlive }

        val surname = deceasedState.fullName.substringAfterLast(" ", "Sterling")
        val heirGender = Gender.entries[rng.nextInt(Gender.entries.size)]
        val heirName = chosenChild?.name ?: LifeCatalog.randomFullName(heirGender, surname, rng)
        val heirAge = (chosenChild?.age ?: 18).coerceIn(14, 48)
        val estateTaxRate = 0.15
        val inheritedNet = (deceasedState.netWorth.coerceAtLeast(0L) * (1.0 - estateTaxRate)).roundToLong()
        val heirloomTrait = "Gen ${deceasedState.generation + 1} Heir"

        val startYear = deceasedState.currentYear
        val birthYear = startYear - heirAge

        val inheritedEducation = when {
            heirAge >= 22 -> EducationLevel.UNIVERSITY
            heirAge >= 18 -> EducationLevel.HIGH_SCHOOL
            else -> EducationLevel.PRIMARY
        }

        val survivingFamily = deceasedState.relations
            .filter { it.id != chosenChild?.id && it.isAlive && it.age < 92 }
            .map {
                val newRole = when (it.role) {
                    RelationRole.CHILD -> RelationRole.SIBLING
                    RelationRole.SPOUSE -> RelationRole.MOTHER
                    else -> it.role
                }
                it.copy(role = newRole)
            }

        val intro = ChronicleEntry(
            id = UUID.randomUUID().toString(),
            age = heirAge,
            year = startYear,
            title = "Torch of Generation ${deceasedState.generation + 1}",
            narrative = "$heirName inherited the mantle of ${deceasedState.fullName}, receiving ${formatCurrency(inheritedNet)} after estate duties.",
            category = ChronicleCategory.LEGACY,
            tags = listOf("Dynasty Gen ${deceasedState.generation + 1}", "+${formatCurrency(inheritedNet)}")
        )

        val inheritedRoyal = when (deceasedState.royal.rank) {
            RoyalRank.MONARCH -> RoyalState(rank = RoyalRank.MONARCH, houseName = deceasedState.royal.houseName.ifBlank { surname }, approval = 68)
            RoyalRank.CROWN_HEIR, RoyalRank.DUKE -> RoyalState(rank = RoyalRank.CROWN_HEIR, houseName = deceasedState.royal.houseName.ifBlank { surname }, approval = 66)
            RoyalRank.BARON -> RoyalState(rank = RoyalRank.BARON, houseName = deceasedState.royal.houseName.ifBlank { surname }, approval = 64)
            RoyalRank.NONE -> RoyalState()
        }

        return LifeState(
            id = UUID.randomUUID().toString(),
            dynastyId = deceasedState.dynastyId,
            generation = deceasedState.generation + 1,
            fullName = heirName,
            gender = heirGender,
            country = deceasedState.country,
            originPreset = deceasedState.originPreset,
            age = heirAge,
            birthYear = birthYear,
            currentYear = startYear,
            cash = inheritedNet + 5_000L,
            stats = Stats(
                health = rng.nextInt(74, 95),
                happiness = 66,
                intellect = ((deceasedState.stats.intellect * 0.4) + rng.nextInt(38, 56)).toInt().coerceIn(45, 95),
                charisma = ((deceasedState.stats.charisma * 0.35) + rng.nextInt(35, 58)).toInt().coerceIn(40, 95),
                discipline = 62,
                stress = 14,
                ethics = 60,
                fame = (deceasedState.stats.fame / 3).coerceAtLeast(5)
            ),
            skills = SkillType.entries.associateWith { type ->
                val legacySeed = ((deceasedState.skills[type]?.xp ?: 0) * 0.25).toInt()
                SkillProgress(type, legacySeed)
            },
            traits = listOf(heirloomTrait) + deceasedState.traits.take(2),
            educationLevel = inheritedEducation,
            marketAssets = LifeCatalog.initialMarketAssets(),
            relations = survivingFamily,
            chronicle = listOf(intro),
            economicPhase = deceasedState.economicPhase,
            royal = inheritedRoyal,
            government = deceasedState.government.copy(isPlayerPresident = false, termYearsServed = 0, proposedBill = null)
        )
    }

    fun advanceYear(state: LifeState, rng: Random = Random): LifeState {
        if (!state.isAlive) return state
        if (state.pendingEvent != null) return state // Must resolve dilemma first

        val nextAge = state.age + 1
        val nextYear = state.currentYear + 1
        var cash = state.cash
        var debt = state.debt
        var stats = state.stats
        var skills = state.skills.toMutableMap()
        var education = state.educationLevel
        var collegeYears = state.collegeYearsCompleted
        var job = state.currentJob
        var business = state.business
        var prison = state.prison
        var heat = (state.heatLevel - 4).coerceAtLeast(0)
        val logs = mutableListOf<ChronicleEntry>()
        val yearTags = mutableListOf<String>()

        // 1. Macro-Economic Shift (18% chance per year)
        val nextEcon = if (rng.nextDouble() < 0.18) {
            val phases = EconomicPhase.entries
            val shifted = phases[rng.nextInt(phases.size)]
            if (shifted != state.economicPhase) {
                logs += ChronicleEntry(
                    id = UUID.randomUUID().toString(),
                    age = nextAge,
                    year = nextYear,
                    title = "Macro Shift: ${shifted.label}",
                    narrative = "Global markets entered a phase of ${shifted.label.lowercase()}, shifting asset volatility and hiring.",
                    category = ChronicleCategory.FINANCE,
                    tags = listOf(shifted.label)
                )
            }
            shifted
        } else {
            state.economicPhase
        }

        // 2. Market Asset Price Update & Dividends (affected by enacted Laws!)
        val lawMarketDrift = state.government.activeLaws.sumOf { it.type.marketDriftDelta }
        val cryptoBanDrag = if (state.hasLaw(LawType.CRYPTO_CRACKDOWN)) -0.18 else 0.0
        var dividendIncome = 0L
        val updatedAssets = state.marketAssets.map { asset ->
            val drift = (if (asset.isCrypto) nextEcon.marketDrift * 1.9 + cryptoBanDrag else nextEcon.marketDrift) + lawMarketDrift
            val vol = if (asset.isCrypto) nextEcon.volatility * 2.4 else nextEcon.volatility
            val shock = (rng.nextDouble() * 2.0 - 1.0) * vol
            val pctChange = (drift + shock).coerceIn(-0.48, 0.72)
            val newPrice = (asset.currentPrice * (1.0 + pctChange)).coerceAtLeast(0.25)
            if (asset.sharesOwned > 0 && asset.dividendYield > 0) {
                dividendIncome += (asset.sharesOwned * newPrice * asset.dividendYield).roundToLong()
            }
            val newHistory = (asset.history + newPrice).takeLast(12)
            asset.copy(
                previousPrice = asset.currentPrice,
                currentPrice = newPrice,
                history = newHistory
            )
        }
        if (dividendIncome > 0) {
            cash += dividendIncome
            yearTags += "+${formatCurrency(dividendIncome)} Divs"
        }

        // 3. Education Automatic Progression (FREE_TUITION Law makes tuition $0!)
        val freeTuition = state.hasLaw(LawType.FREE_TUITION)
        if (nextAge == 6 && education == EducationLevel.NONE) {
            education = EducationLevel.PRIMARY
            logs += milestone(nextAge, nextYear, "First Day of Primary School", "You stepped into the classroom with an oversized backpack.")
        } else if (nextAge == 14 && education == EducationLevel.PRIMARY) {
            education = EducationLevel.HIGH_SCHOOL
            logs += milestone(nextAge, nextYear, "Entered High School", "New cliques, tougher exams, and first real independence.")
        } else if (nextAge == 18 && education == EducationLevel.HIGH_SCHOOL) {
            logs += milestone(nextAge, nextYear, "High School Graduation", "Diploma in hand! You can apply to University or launch straight into the workforce.")
        } else if (education == EducationLevel.IN_UNIVERSITY) {
            collegeYears += 1
            val tuition = if (freeTuition) 0L else (state.degreeMajor?.tuitionPerYear ?: 14_000L)
            if (tuition > 0L) {
                if (cash >= tuition) cash -= tuition else debt += tuition
            }
            state.degreeMajor?.primarySkill?.let { sk ->
                skills[sk] = addSkillXp(skills[sk], 55)
            }
            if (collegeYears >= 4) {
                education = EducationLevel.UNIVERSITY
                logs += milestone(nextAge, nextYear, "University Graduated!", "Earned your Bachelor's in ${state.degreeMajor?.label ?: "Liberal Studies"}!")
            }
        } else if (education == EducationLevel.GRAD_SCHOOL) {
            collegeYears += 1
            val tuition = if (freeTuition) 0L else ((state.degreeMajor?.tuitionPerYear ?: 18_000L) + 4_000L)
            if (tuition > 0L) {
                if (cash >= tuition) cash -= tuition else debt += tuition
            }
            if (collegeYears >= 6) {
                education = EducationLevel.DOCTORATE
                logs += milestone(nextAge, nextYear, "Doctorate Conferred!", "Completed your advanced degree in ${state.degreeMajor?.label ?: "Research"}.")
            }
        }

        // 4. Prison & Death Row Execution Check
        if (prison.isInPrison) {
            if (prison.isDeathRow) {
                val nextExec = (prison.yearsUntilExecution - 1).coerceAtLeast(0)
                if (nextExec == 0) {
                    val executedState = state.copy(
                        age = nextAge,
                        currentYear = nextYear,
                        cash = cash,
                        debt = debt,
                        stats = stats.copy(health = 0),
                        skills = skills,
                        prison = prison.copy(yearsUntilExecution = 0),
                        isAlive = false,
                        causeOfDeath = "Executed on Death Row"
                    )
                    val title = LifeCatalog.evaluateLegacyTitle(executedState)
                    val deathLog = ChronicleEntry(
                        id = UUID.randomUUID().toString(),
                        age = nextAge,
                        year = nextYear,
                        title = "Sentence Carried Out: Executed",
                        narrative = "Final clemency petitions failed. ${state.fullName} was executed at age $nextAge under ${prison.crimeName}.",
                        category = ChronicleCategory.CRIME,
                        tags = listOf("Executed", title)
                    )
                    return executedState.copy(
                        legacyTitle = title,
                        chronicle = (listOf(deathLog) + logs + state.chronicle).take(120)
                    )
                } else {
                    prison = prison.copy(yearsUntilExecution = nextExec)
                    stats = stats.copy(happiness = stats.happiness - 8, stress = stats.stress + 8)
                }
            } else if (!prison.isLifeSentence) {
                val remaining = (prison.yearsRemaining - 1).coerceAtLeast(0)
                if (remaining == 0) {
                    prison = prison.copy(isInPrison = false, yearsRemaining = 0)
                    logs += ChronicleEntry(
                        id = UUID.randomUUID().toString(),
                        age = nextAge,
                        year = nextYear,
                        title = "Released From Custody",
                        narrative = "You walked out the front gates after serving your sentence. Time to rebuild—or settle scores.",
                        category = ChronicleCategory.CRIME,
                        tags = listOf("Free")
                    )
                } else {
                    prison = prison.copy(yearsRemaining = remaining)
                    stats = stats.copy(happiness = stats.happiness - 6, stress = stats.stress + 4, health = stats.health - 2)
                }
            } else {
                stats = stats.copy(happiness = stats.happiness - 6, stress = stats.stress + 5, health = stats.health - 2)
            }
        }

        // 5. Career, Side Hustles, Business, Real Estate & Living Expenses (modulated by Active Laws!)
        val effectiveTax = state.effectiveTaxRate()
        val salaryLawMult = state.government.activeLaws.fold(1.0) { acc, l -> acc * l.type.salaryMultiplier }
        if (!prison.isInPrison) {
            if (job != null) {
                val gross = (job.salary * nextEcon.jobBonus * salaryLawMult).roundToLong()
                val netPay = (gross * (1.0 - effectiveTax)).roundToLong()
                cash += netPay
                yearTags += "+${formatCurrency(netPay)} Pay"
                val perfDelta = if (stats.stress > 75) -4 else 3
                val newPerf = (job.performance + perfDelta).coerceIn(10, 100)
                job = job.copy(
                    yearsInRole = job.yearsInRole + 1,
                    performance = newPerf,
                    salary = if (newPerf >= 80 && rng.nextDouble() < 0.35) (job.salary * 1.06).roundToLong() else job.salary
                )
                stats = stats.copy(stress = stats.stress + (job.stressLoad / 4))
                if (job.track == CareerTrack.SYNDICATE) {
                    heat += 6
                    stats = stats.copy(ethics = stats.ethics - 3)
                }
            }

            // Side hustles
            if (state.sideHustles.isNotEmpty()) {
                var sideTotal = 0L
                state.sideHustles.forEach { hustleId ->
                    val def = LifeCatalog.sideHustles.firstOrNull { it.id == hustleId } ?: return@forEach
                    val skLevel = skills[def.skillBoost]?.level ?: 1
                    val earned = (def.annualBaseIncome * (0.7 + skLevel * 0.22) * nextEcon.jobBonus).roundToLong()
                    sideTotal += earned
                    skills[def.skillBoost] = addSkillXp(skills[def.skillBoost], 22)
                    stats = stats.copy(stress = stats.stress + def.stressCost / 2)
                }
                if (sideTotal > 0) {
                    cash += sideTotal
                    yearTags += "+${formatCurrency(sideTotal)} Side"
                }
            }

            // Business simulation
            if (business != null) {
                val demandFactor = (0.75 + (business.productQuality + business.marketingLevel) / 140.0) * nextEcon.jobBonus
                val revenue = (business.annualRevenue * (0.92 + demandFactor * 0.28)).roundToLong().coerceAtLeast(15_000L)
                val profitMargin = (business.sector.baseMargin * (business.morale / 80.0)).coerceIn(-0.15, 0.55)
                val profit = (revenue * profitMargin).roundToLong()
                val ownerShare = ((profit * business.equityPercent) / 100.0).roundToLong()
                cash += ownerShare
                val newValuation = (revenue * (2.5 + business.productQuality / 35.0)).roundToLong().coerceAtLeast(25_000L)
                business = business.copy(
                    annualRevenue = revenue,
                    annualProfit = profit,
                    valuation = newValuation,
                    ageYears = business.ageYears + 1
                )
                yearTags += "${if (ownerShare >= 0) "+" else ""}${formatCurrency(ownerShare)} Biz"
            }
        }

        // Real Estate
        val updatedProperties = state.realEstate.map { prop ->
            val appreciation = (nextEcon.marketDrift * 0.65) + ((rng.nextDouble() - 0.45) * 0.05)
            val newVal = (prop.currentValue * (1.0 + appreciation)).roundToLong().coerceAtLeast(40_000L)
            var newMortgage = prop.mortgageRemaining
            if (prop.isRentedOut) {
                cash += prop.annualRent
            }
            cash -= prop.type.maintenance
            if (newMortgage > 0) {
                val payment = (prop.purchasePrice * 0.055).roundToLong().coerceAtMost(newMortgage)
                cash -= payment
                newMortgage -= payment
            }
            prop.copy(
                currentValue = newVal,
                mortgageRemaining = newMortgage,
                condition = (prop.condition - 2).coerceIn(20, 100)
            )
        }

        // School Clubs passive XP (ages 12-23)
        if (nextAge in 12..23 && state.schoolClubs.isNotEmpty() && !prison.isInPrison) {
            state.schoolClubs.forEach { club ->
                skills[club.skill] = addSkillXp(skills[club.skill], 32)
            }
            stats = stats.copy(happiness = stats.happiness + 3, charisma = stats.charisma + 2)
        }

        // Lifestyle & Living expenses & debt interest
        if (nextAge >= 18 && !prison.isInPrison) {
            val baseLiving = (state.country.costOfLiving * (1.0 + nextEcon.inflationRate)).roundToLong()
            val lifestyleCost = state.lifestyle.annualCost
            cash -= (baseLiving + lifestyleCost)
            stats = stats.copy(
                happiness = stats.happiness + state.lifestyle.joyDelta,
                stress = stats.stress + state.lifestyle.stressDelta,
                fame = stats.fame + state.lifestyle.fameGain
            )
            if (state.lifestyle == LifestyleTier.FRUGAL) {
                stats = stats.copy(discipline = stats.discipline + 3)
            }
        }
        // Status Effects decay & active perks
        val activeStatuses = state.statusEffects.mapNotNull { eff ->
            if (eff.yearsLeft <= 1) null else eff.copy(yearsLeft = eff.yearsLeft - 1)
        }
        if (debt > 0) {
            debt = (debt * 1.06).roundToLong()
        }
        if (cash < -15_000L) {
            // Convert negative liquidity into formal debt with credit hit
            val deficit = abs(cash)
            debt += deficit
            cash = 0L
            stats = stats.copy(stress = stats.stress + 10, happiness = stats.happiness - 6)
        }

        // 6. Arrest check if heat is high
        if (heat >= 65 && !prison.isInPrison && rng.nextDouble() < (heat - 50) / 100.0) {
            val sentence = rng.nextInt(2, 7)
            prison = PrisonState(
                isInPrison = true,
                yearsRemaining = sentence,
                totalSentence = sentence,
                crimeName = "Racketeering & Financial Fraud",
                notoriety = prison.notoriety + 20
            )
            job = null
            heat = 10
            logs += ChronicleEntry(
                id = UUID.randomUUID().toString(),
                age = nextAge,
                year = nextYear,
                title = "Federal Indictment & Arrest!",
                narrative = "Authorities raided your residence due to rising Heat. Sentenced to $sentence years.",
                category = ChronicleCategory.CRIME,
                tags = listOf("${sentence}y Sentence")
            )
        }

        // 7. Age NPCs
        val updatedRelations = state.relations.map { npc ->
            if (!npc.isAlive) npc
            else {
                val aged = npc.age + 1
                val deathOdds = when {
                    aged < 65 -> 0.002
                    aged < 80 -> 0.03
                    aged < 92 -> 0.12
                    else -> 0.30
                }
                if (rng.nextDouble() < deathOdds) {
                    logs += ChronicleEntry(
                        id = UUID.randomUUID().toString(),
                        age = nextAge,
                        year = nextYear,
                        title = "Farewell, ${npc.name}",
                        narrative = "Your ${npc.role.label.lowercase()}, ${npc.name}, passed away peacefully at age $aged.",
                        category = ChronicleCategory.LOVE,
                        tags = listOf("Loss", "-Happiness")
                    )
                    stats = stats.copy(happiness = stats.happiness - 16, stress = stats.stress + 10)
                    npc.copy(age = aged, isAlive = false)
                } else {
                    npc.copy(age = aged, yearsKnown = npc.yearsKnown + 1)
                }
            }
        }

        // 8. Natural Aging Vitals & Mortality
        val agingHealthDrain = when {
            nextAge < 40 -> 0
            nextAge < 65 -> 1
            nextAge < 80 -> 3
            else -> 6
        }
        val stressPenalty = if (stats.stress >= 80) 4 else 0
        val naturalHealth = stats.health - agingHealthDrain - stressPenalty + if (state.country.healthcareScore >= 88 && nextAge > 50) 1 else 0
        stats = stats.copy(
            health = naturalHealth,
            stress = (stats.stress - 4).coerceAtLeast(0)
        ).clamp()

        // Mortality check
        val deathCause = checkMortality(nextAge, stats, rng)
        if (deathCause != null) {
            val deadState = state.copy(
                age = nextAge,
                currentYear = nextYear,
                cash = cash,
                debt = debt,
                stats = stats.copy(health = 0),
                skills = skills,
                educationLevel = education,
                collegeYearsCompleted = collegeYears,
                currentJob = job,
                business = business,
                marketAssets = updatedAssets,
                realEstate = updatedProperties,
                relations = updatedRelations,
                prison = prison,
                economicPhase = nextEcon,
                heatLevel = heat,
                actionsUsedThisYear = 0,
                isAlive = false,
                causeOfDeath = deathCause
            )
            val legacyTitle = LifeCatalog.evaluateLegacyTitle(deadState)
            val deathEntry = ChronicleEntry(
                id = UUID.randomUUID().toString(),
                age = nextAge,
                year = nextYear,
                title = "End of a Chronicle",
                narrative = "${state.fullName} passed away at age $nextAge ($deathCause). Remembered as: $legacyTitle.",
                category = ChronicleCategory.LEGACY,
                tags = listOf(legacyTitle, formatCurrency(deadState.netWorth))
            )
            return deadState.copy(
                legacyTitle = legacyTitle,
                chronicle = (listOf(deathEntry) + logs + state.chronicle).take(120)
            )
        }

        // 9. Governance, Royalty, Cascading Arcs, Wildcard Curveballs & Proactive NPC Communication
        var tempState = state.copy(
            age = nextAge,
            currentYear = nextYear,
            cash = cash,
            debt = debt,
            stats = stats,
            skills = skills,
            educationLevel = education,
            collegeYearsCompleted = collegeYears,
            currentJob = job,
            business = business,
            marketAssets = updatedAssets,
            realEstate = updatedProperties,
            relations = updatedRelations,
            prison = prison,
            economicPhase = nextEcon,
            heatLevel = heat,
            actionsUsedThisYear = 0,
            statusEffects = activeStatuses,
            lastCascadeSummary = null
        )

        val (afterGovState, govLogs, govTags) = GovernanceCrimeEngine.annualGovernanceTick(tempState, nextAge, nextYear, rng)
        tempState = afterGovState
        logs += govLogs
        yearTags += govTags
        if (!tempState.isAlive) {
            val legacyTitle = LifeCatalog.evaluateLegacyTitle(tempState)
            return tempState.copy(
                legacyTitle = legacyTitle,
                chronicle = (logs + state.chronicle).take(120)
            )
        }

        // Annual Wildcard Event (~24% chance)
        val (afterWildcard, wildcardEntry) = CascadeCommunicationEngine.rollAnnualWildcard(tempState, rng)
        tempState = afterWildcard
        if (wildcardEntry != null) {
            logs += wildcardEntry
        }

        // Proactive NPC Inbox Message (~45% chance when age >= 13 and inbox unresolved < 4)
        var nextInbox = tempState.inbox
        if (nextAge >= 13 && tempState.relations.any { it.isAlive } && nextInbox.count { !it.isRead } < 4 && rng.nextDouble() < 0.45) {
            CascadeCommunicationEngine.generateAnnualNpcMessage(tempState, rng)?.let { msg ->
                nextInbox = (listOf(msg) + nextInbox).take(25)
            }
        }

        // Check due Cascading Story Arcs FIRST so past choices detonate meaningfully!
        val dueArc = tempState.pendingArcs.firstOrNull { it.triggerAge <= nextAge }
        val remainingArcs = if (dueArc != null) {
            tempState.pendingArcs.filterNot { it.id == dueArc.id }
        } else tempState.pendingArcs

        val triggeredEvent = when {
            dueArc != null -> CascadeCommunicationEngine.buildCascadeEvent(dueArc, tempState)
            nextAge >= 5 && rng.nextDouble() < 0.72 -> EventEngine.pickEventForState(tempState, rng)
            else -> null
        }

        if (logs.isEmpty() && triggeredEvent == null) {
            logs += ChronicleEntry(
                id = UUID.randomUUID().toString(),
                age = nextAge,
                year = nextYear,
                title = "Age $nextAge in ${state.country.name}",
                narrative = buildQuietYearSummary(tempState),
                category = ChronicleCategory.MILESTONE,
                tags = yearTags.take(3)
            )
        }

        return tempState.copy(
            pendingEvent = triggeredEvent,
            pendingArcs = remainingArcs,
            inbox = nextInbox,
            chronicle = (logs + state.chronicle).take(120)
        )
    }

    fun resolveEventChoice(state: LifeState, choiceIndex: Int, rng: Random = Random): LifeState {
        val event = state.pendingEvent ?: return state
        val choice = event.choices.getOrNull(choiceIndex) ?: return state

        val hasSkill = choice.requiredSkill == null || state.skillLevel(choice.requiredSkill) >= choice.requiredLevel
        val success = if (hasSkill) {
            true
        } else {
            rng.nextDouble() < 0.25
        }

        val delta = if (success) choice.statDelta else choice.failStatDelta
        val cashGain = if (success) choice.cashDelta else choice.failCashDelta
        val narrative = if (success) choice.successNarrative else choice.failNarrative.ifBlank { choice.successNarrative }

        val updatedStats = applyStatDelta(state.stats, delta)
        val updatedSkills = state.skills.toMutableMap()
        if (success && choice.skillGainType != null && choice.skillGainXp > 0) {
            updatedSkills[choice.skillGainType] = addSkillXp(updatedSkills[choice.skillGainType], choice.skillGainXp)
        }
        val updatedTraits = if (success && choice.traitGained != null && choice.traitGained !in state.traits) {
            state.traits + choice.traitGained
        } else state.traits

        val updatedRelations = if (choice.npcClosenessDelta != 0 || choice.npcTrustDelta != 0) {
            state.relations.map { npc ->
                if (npc.isAlive) npc.copy(
                    closeness = (npc.closeness + choice.npcClosenessDelta).coerceIn(0, 100),
                    trust = (npc.trust + choice.npcTrustDelta).coerceIn(0, 100)
                ) else npc
            }
        } else state.relations

        val updatedArcs = if (choice.cascadeArcKey != null) {
            state.pendingArcs + DelayedArc(
                id = UUID.randomUUID().toString(),
                arcKey = choice.cascadeArcKey,
                triggerAge = state.age + choice.cascadeDelayYears,
                stage = 1,
                originChoiceLabel = "${choice.label} (Age ${state.age})"
            )
        } else state.pendingArcs

        val updatedStatuses = if (success && choice.statusApplied != null) {
            state.statusEffects + choice.statusApplied
        } else state.statusEffects

        val rippleText = buildList {
            if (event.cascadeSourceNote != null) add(event.cascadeSourceNote)
            if (choice.cascadeNote != null) add("Planted future ripple: ${choice.cascadeNote}")
            if (choice.npcClosenessDelta != 0) add("Circle Closeness ${if (choice.npcClosenessDelta > 0) "+" else ""}${choice.npcClosenessDelta}")
        }.joinToString(" • ").ifBlank { null }

        val tags = mutableListOf<String>()
        if (cashGain != 0L) tags += "${if (cashGain > 0) "+" else ""}${formatCurrency(cashGain)}"
        if (delta.intellect != 0) tags += "${if (delta.intellect > 0) "+" else ""}${delta.intellect} Intellect"
        if (delta.happiness != 0) tags += "${if (delta.happiness > 0) "+" else ""}${delta.happiness} Joy"
        if (delta.stress != 0) tags += "${if (delta.stress > 0) "+" else ""}${delta.stress} Stress"
        if (choice.traitGained != null && success) tags += "Trait: ${choice.traitGained}"
        if (choice.cascadeArcKey != null) tags += "Ripple Planted"

        val entry = ChronicleEntry(
            id = UUID.randomUUID().toString(),
            age = state.age,
            year = state.currentYear,
            title = event.title,
            narrative = narrative,
            category = event.category,
            tags = tags.take(4)
        )

        return state.copy(
            cash = (state.cash + cashGain).coerceAtLeast(-50_000L),
            stats = updatedStats,
            skills = updatedSkills,
            traits = updatedTraits,
            relations = updatedRelations,
            pendingArcs = updatedArcs,
            statusEffects = updatedStatuses,
            lastCascadeSummary = rippleText,
            heatLevel = (state.heatLevel + choice.heatDelta).coerceIn(0, 100),
            pendingEvent = null,
            chronicle = (listOf(entry) + state.chronicle).take(120)
        )
    }

    fun performSelfAction(state: LifeState, action: SelfAction, rng: Random = Random): Pair<LifeState, String> {
        if (!state.isAlive) return state to "This chronicle has ended."
        if (state.age < action.minAge) return state to "Must be at least age ${action.minAge} for ${action.title}."
        if (state.actionsUsedThisYear >= 4) return state to "You've reached this year's energy limit (4 actions/year). Age +1 year to continue!"
        val effectiveCost = if (action == SelfAction.DOCTOR && state.hasLaw(LawType.UNIVERSAL_HEALTHCARE)) 0L else action.cost
        if (state.cash < effectiveCost) return state to "Need ${formatCurrency(effectiveCost)} for ${action.title}."

        var cash = state.cash - effectiveCost
        var stats = state.stats
        val skills = state.skills.toMutableMap()
        var heat = state.heatLevel
        val msg: String

        when (action) {
            SelfAction.WORKOUT -> {
                stats = stats.copy(health = stats.health + 6, stress = stats.stress - 8, happiness = stats.happiness + 4)
                skills[SkillType.ATHLETICS] = addSkillXp(skills[SkillType.ATHLETICS], 45)
                msg = "Intense training session completed (+6 Health, -8 Stress, +Athletics XP)."
            }
            SelfAction.STUDY -> {
                stats = stats.copy(intellect = stats.intellect + 5, discipline = stats.discipline + 4, stress = stats.stress + 3)
                skills[SkillType.ENGINEERING] = addSkillXp(skills[SkillType.ENGINEERING], 35)
                skills[SkillType.FINANCE] = addSkillXp(skills[SkillType.FINANCE], 30)
                msg = "Deep focus reading paid off (+5 Intellect, +Engineering & Finance XP)."
            }
            SelfAction.MEDITATE -> {
                stats = stats.copy(stress = stats.stress - 14, happiness = stats.happiness + 6, discipline = stats.discipline + 2)
                msg = "Breathwork and stillness cleared your mind (-14 Stress, +6 Happiness)."
            }
            SelfAction.THERAPY -> {
                stats = stats.copy(stress = stats.stress - 24, happiness = stats.happiness + 12)
                msg = "Executive therapy unpacked months of pressure (-24 Stress, +12 Happiness)."
            }
            SelfAction.CREATE_ART -> {
                stats = stats.copy(happiness = stats.happiness + 8, stress = stats.stress - 6)
                skills[SkillType.ARTS] = addSkillXp(skills[SkillType.ARTS], 50)
                msg = "Spent hours creating original work (+8 Happiness, +Arts XP)."
            }
            SelfAction.NETWORK -> {
                stats = stats.copy(charisma = stats.charisma + 6, fame = stats.fame + 4, stress = stats.stress + 4)
                skills[SkillType.RHETORIC] = addSkillXp(skills[SkillType.RHETORIC], 48)
                msg = "Made sharp contacts at an exclusive mixer (+6 Charisma, +Rhetoric XP)."
            }
            SelfAction.DOCTOR -> {
                stats = stats.copy(health = stats.health + 15, stress = stats.stress - 5)
                msg = "Full preventative panel & treatment boosted vitality (+15 Health)."
            }
            SelfAction.VOLUNTEER -> {
                stats = stats.copy(ethics = stats.ethics + 9, happiness = stats.happiness + 7, stress = stats.stress - 5)
                heat = (heat - 8).coerceAtLeast(0)
                msg = "Community work restored goodwill (+9 Ethics, +7 Happiness, -8 Heat)."
            }
            SelfAction.STREET_HUSTLE -> {
                val lv = state.skillLevel(SkillType.STREETCRAFT)
                val earned = (350L + lv * 280L + rng.nextInt(150, 900)).toLong()
                cash += earned
                heat = (heat + 9).coerceAtMost(100)
                stats = stats.copy(ethics = stats.ethics - 5, stress = stats.stress + 7)
                skills[SkillType.STREETCRAFT] = addSkillXp(skills[SkillType.STREETCRAFT], 55)
                msg = "Pulled off a quick street hustle for +${formatCurrency(earned)} (+Streetcraft XP, +9 Heat)."
            }
        }

        val entry = ChronicleEntry(
            id = UUID.randomUUID().toString(),
            age = state.age,
            year = state.currentYear,
            title = action.title,
            narrative = msg,
            category = ChronicleCategory.HEALTH
        )

        return state.copy(
            cash = cash,
            stats = stats.clamp(),
            skills = skills,
            heatLevel = heat,
            actionsUsedThisYear = state.actionsUsedThisYear + 1,
            chronicle = (listOf(entry) + state.chronicle).take(120)
        ) to msg
    }

    fun enrollUniversity(state: LifeState, major: DegreeMajor): Pair<LifeState, String> {
        if (state.age < 17) return state to "Wait until age 17+ to enroll in University."
        if (state.educationLevel.rank >= EducationLevel.IN_UNIVERSITY.rank) {
            return state to "You are already enrolled in or have completed University."
        }
        val scholarship = if (state.stats.intellect >= 78) " (Merit Scholarship reduced costs!)" else ""
        val entry = ChronicleEntry(
            id = UUID.randomUUID().toString(),
            age = state.age,
            year = state.currentYear,
            title = "Enrolled in ${major.label}",
            narrative = "Accepted into the ${major.label} program$scholarship.",
            category = ChronicleCategory.MILESTONE,
            tags = listOf(major.label)
        )
        return state.copy(
            educationLevel = EducationLevel.IN_UNIVERSITY,
            degreeMajor = major,
            collegeYearsCompleted = 0,
            chronicle = (listOf(entry) + state.chronicle).take(120)
        ) to "Enrolled in ${major.label}!"
    }

    fun enrollGradSchool(state: LifeState): Pair<LifeState, String> {
        if (state.educationLevel != EducationLevel.UNIVERSITY) {
            return state to "Complete a Bachelor's degree first."
        }
        if (state.stats.intellect < 60) {
            return state to "Need Intellect 60+ for Graduate admission."
        }
        val entry = ChronicleEntry(
            id = UUID.randomUUID().toString(),
            age = state.age,
            year = state.currentYear,
            title = "Entered Graduate School",
            narrative = "Started advanced doctoral/professional studies in ${state.degreeMajor?.label ?: "Research"}.",
            category = ChronicleCategory.MILESTONE
        )
        return state.copy(
            educationLevel = EducationLevel.GRAD_SCHOOL,
            chronicle = (listOf(entry) + state.chronicle).take(120)
        ) to "Admitted to Graduate School!"
    }

    fun applyForJob(state: LifeState, listingId: String): Pair<LifeState, String> {
        if (state.age < 16) return state to "Must be 16+ to hold a formal career."
        if (state.prison.isInPrison) return state to "Cannot apply for jobs while incarcerated."
        val listing = LifeCatalog.careerListings.firstOrNull { it.id == listingId } ?: return state to "Role unavailable."

        if (state.educationLevel.rank < listing.minEducation.rank) {
            return state to "Requires ${listing.minEducation.label}."
        }
        if (listing.preferredMajor != null && state.degreeMajor != listing.preferredMajor && state.educationLevel.rank < EducationLevel.DOCTORATE.rank) {
            return state to "Requires a degree in ${listing.preferredMajor.label}."
        }
        if (listing.requiredSkill != null && state.skillLevel(listing.requiredSkill) < listing.requiredSkillLevel) {
            return state to "Requires ${listing.requiredSkill.label} Lv.${listing.requiredSkillLevel}."
        }
        if (state.stats.intellect < listing.minIntellect) {
            return state to "Requires Intellect ${listing.minIntellect}+."
        }

        val offerSalary = (listing.baseSalary * (0.95 + state.stats.charisma / 500.0)).roundToLong()
        val newJob = JobInfo(
            id = listing.id,
            title = listing.title,
            track = listing.track,
            tier = listing.tier,
            salary = offerSalary,
            stressLoad = listing.stressLoad
        )
        val entry = ChronicleEntry(
            id = UUID.randomUUID().toString(),
            age = state.age,
            year = state.currentYear,
            title = "Hired: ${listing.title}",
            narrative = "Signed an offer as ${listing.title} at ${formatCurrency(offerSalary)}/yr.",
            category = ChronicleCategory.CAREER,
            tags = listOf(listing.track.label, "${formatCurrency(offerSalary)}/yr")
        )
        return state.copy(
            currentJob = newJob,
            chronicle = (listOf(entry) + state.chronicle).take(120)
        ) to "Hired as ${listing.title}!"
    }

    fun workHarder(state: LifeState): Pair<LifeState, String> {
        val job = state.currentJob ?: return state to "You don't have an active career."
        if (state.actionsUsedThisYear >= 4) return state to "No energy left this year. Age +1 year!"

        val updatedJob = job.copy(performance = (job.performance + 14).coerceAtMost(100))
        val updatedStats = state.stats.copy(stress = state.stats.stress + 8, discipline = state.stats.discipline + 3).clamp()
        return state.copy(
            currentJob = updatedJob,
            stats = updatedStats,
            actionsUsedThisYear = state.actionsUsedThisYear + 1
        ) to "Delivered standout quarterly output (Performance: ${updatedJob.performance}%)."
    }

    fun negotiateRaise(state: LifeState, rng: Random = Random): Pair<LifeState, String> {
        val job = state.currentJob ?: return state to "No active job."
        if (state.actionsUsedThisYear >= 4) return state to "No energy left this year."
        val odds = 0.25 + (job.performance / 150.0) + (state.skillLevel(SkillType.RHETORIC) * 0.06)
        return if (rng.nextDouble() < odds) {
            val newSal = (job.salary * 1.14).roundToLong()
            val updated = job.copy(salary = newSal)
            val entry = ChronicleEntry(
                id = UUID.randomUUID().toString(),
                age = state.age,
                year = state.currentYear,
                title = "Salary Raise Secured",
                narrative = "Negotiated a 14% raise to ${formatCurrency(newSal)}/yr.",
                category = ChronicleCategory.CAREER
            )
            state.copy(
                currentJob = updated,
                actionsUsedThisYear = state.actionsUsedThisYear + 1,
                chronicle = (listOf(entry) + state.chronicle).take(120)
            ) to "Raise approved! New salary: ${formatCurrency(newSal)}/yr."
        } else {
            state.copy(
                currentJob = job.copy(performance = (job.performance - 6).coerceAtLeast(10)),
                stats = state.stats.copy(stress = state.stats.stress + 6).clamp(),
                actionsUsedThisYear = state.actionsUsedThisYear + 1
            ) to "Management declined the raise citing budget freeze."
        }
    }

    fun quitJob(state: LifeState): Pair<LifeState, String> {
        val job = state.currentJob ?: return state to "Not employed."
        val entry = ChronicleEntry(
            id = UUID.randomUUID().toString(),
            age = state.age,
            year = state.currentYear,
            title = "Resigned from ${job.title}",
            narrative = "Handed in notice and stepped away from ${job.track.label}.",
            category = ChronicleCategory.CAREER
        )
        return state.copy(
            currentJob = null,
            stats = state.stats.copy(stress = (state.stats.stress - 10).coerceAtLeast(0)),
            chronicle = (listOf(entry) + state.chronicle).take(120)
        ) to "Left ${job.title}."
    }

    fun toggleSideHustle(state: LifeState, hustleId: String): Pair<LifeState, String> {
        if (state.age < 15) return state to "Must be 15+ to run a side hustle."
        return if (hustleId in state.sideHustles) {
            state.copy(sideHustles = state.sideHustles - hustleId) to "Paused side hustle."
        } else {
            if (state.sideHustles.size >= 2) return state to "You can juggle at most 2 side hustles simultaneously."
            state.copy(sideHustles = state.sideHustles + hustleId) to "Launched new side hustle!"
        }
    }

    fun foundBusiness(state: LifeState, name: String, sector: BusinessSector): Pair<LifeState, String> {
        if (state.age < 18) return state to "Must be 18+ to incorporate a company."
        if (state.business != null) return state to "You already operate ${state.business.name}."
        if (state.cash < sector.startupCost) return state to "Need ${formatCurrency(sector.startupCost)} startup capital."

        val cleanName = name.trim().ifBlank { "${state.fullName.substringAfterLast(" ")} ${sector.label.split(" ").first()}" }
        val biz = BusinessVenture(
            id = UUID.randomUUID().toString(),
            name = cleanName,
            sector = sector,
            equityPercent = 100,
            valuation = sector.startupCost * 2,
            annualRevenue = (sector.startupCost * 1.1).roundToLong(),
            annualProfit = (sector.startupCost * 0.2).roundToLong()
        )
        val entry = ChronicleEntry(
            id = UUID.randomUUID().toString(),
            age = state.age,
            year = state.currentYear,
            title = "Founded $cleanName",
            narrative = "Incorporated $cleanName in ${sector.label} with ${formatCurrency(sector.startupCost)} seed capital.",
            category = ChronicleCategory.CAREER,
            tags = listOf("Founder", sector.label)
        )
        return state.copy(
            cash = state.cash - sector.startupCost,
            business = biz,
            chronicle = (listOf(entry) + state.chronicle).take(120)
        ) to "Founded $cleanName!"
    }

    fun investInBusiness(state: LifeState, focus: String): Pair<LifeState, String> {
        val biz = state.business ?: return state to "No active company."
        val cost = 15_000L
        if (state.cash < cost) return state to "Need ${formatCurrency(cost)} to fund $focus."
        val updated = when (focus) {
            "R&D" -> biz.copy(productQuality = (biz.productQuality + 12).coerceAtMost(100))
            "Marketing" -> biz.copy(marketingLevel = (biz.marketingLevel + 12).coerceAtMost(100))
            else -> biz.copy(morale = (biz.morale + 14).coerceAtMost(100))
        }
        return state.copy(
            cash = state.cash - cost,
            business = updated
        ) to "Invested ${formatCurrency(cost)} into $focus."
    }

    fun raiseVentureRound(state: LifeState): Pair<LifeState, String> {
        val biz = state.business ?: return state to "No active company."
        if (biz.equityPercent <= 30) return state to "Equity too diluted for another VC round."
        val injection = (biz.valuation * 0.35).roundToLong()
        val updated = biz.copy(
            equityPercent = (biz.equityPercent - 15).coerceAtLeast(15),
            valuation = (biz.valuation * 1.45).roundToLong(),
            annualRevenue = (biz.annualRevenue * 1.25).roundToLong()
        )
        return state.copy(
            cash = state.cash + injection,
            business = updated
        ) to "Raised ${formatCurrency(injection)} Series round for 15% equity!"
    }

    fun exitBusiness(state: LifeState): Pair<LifeState, String> {
        val biz = state.business ?: return state to "No active company."
        val payout = ((biz.valuation * biz.equityPercent) / 100.0).roundToLong()
        val entry = ChronicleEntry(
            id = UUID.randomUUID().toString(),
            age = state.age,
            year = state.currentYear,
            title = "Acquired: ${biz.name}!",
            narrative = "Sold your ${biz.equityPercent}% stake in ${biz.name} for a landmark exit of ${formatCurrency(payout)}.",
            category = ChronicleCategory.FINANCE,
            tags = listOf("Exit", "+${formatCurrency(payout)}")
        )
        return state.copy(
            cash = state.cash + payout,
            business = null,
            stats = state.stats.copy(fame = state.stats.fame + 15, happiness = state.stats.happiness + 12).clamp(),
            chronicle = (listOf(entry) + state.chronicle).take(120)
        ) to "Exited ${biz.name} for ${formatCurrency(payout)}!"
    }

    fun buyAsset(state: LifeState, ticker: String, dollarAmount: Long): Pair<LifeState, String> {
        if (dollarAmount <= 0L) return state to "Enter a positive investment amount."
        if (state.cash < dollarAmount) return state to "Insufficient cash (${formatCurrency(state.cash)})."
        val target = state.marketAssets.firstOrNull { it.ticker == ticker } ?: return state to "Asset not found."
        if (target.isCrypto && state.hasLaw(LawType.CRYPTO_CRACKDOWN)) {
            return state to "Blocked by Digital Asset Prohibition law! Repeal/veto ban to trade crypto."
        }
        val sharesBought = dollarAmount / target.currentPrice
        val updatedList = state.marketAssets.map {
            if (it.ticker == ticker) {
                val totalShares = it.sharesOwned + sharesBought
                val newCostBasis = ((it.sharesOwned * it.avgCostBasis) + dollarAmount) / totalShares
                it.copy(sharesOwned = totalShares, avgCostBasis = newCostBasis)
            } else it
        }
        return state.copy(
            cash = state.cash - dollarAmount,
            marketAssets = updatedList
        ) to "Bought ${"%.2f".format(sharesBought)} $ticker for ${formatCurrency(dollarAmount)}."
    }

    fun sellAsset(state: LifeState, ticker: String, fraction: Double = 1.0): Pair<LifeState, String> {
        val target = state.marketAssets.firstOrNull { it.ticker == ticker } ?: return state to "Asset not found."
        if (target.sharesOwned <= 0.0) return state to "No $ticker shares held."
        val sharesToSell = target.sharesOwned * fraction.coerceIn(0.1, 1.0)
        val proceeds = (sharesToSell * target.currentPrice).roundToLong()
        val remaining = (target.sharesOwned - sharesToSell).let { if (it < 0.0001) 0.0 else it }
        val updatedList = state.marketAssets.map {
            if (it.ticker == ticker) {
                it.copy(sharesOwned = remaining, avgCostBasis = if (remaining == 0.0) 0.0 else it.avgCostBasis)
            } else it
        }
        return state.copy(
            cash = state.cash + proceeds,
            marketAssets = updatedList
        ) to "Sold $ticker position for ${formatCurrency(proceeds)}."
    }

    fun buyRealEstate(state: LifeState, type: PropertyType, useMortgage: Boolean): Pair<LifeState, String> {
        if (state.age < 18) return state to "Must be 18+ to sign property deeds."
        val downPayment = if (useMortgage) (type.basePrice * 0.20).roundToLong() else type.basePrice
        if (state.cash < downPayment) return state to "Need ${formatCurrency(downPayment)} upfront."
        val mortgage = if (useMortgage) type.basePrice - downPayment else 0L
        val prop = RealEstateAsset(
            id = UUID.randomUUID().toString(),
            name = type.label,
            type = type,
            purchasePrice = type.basePrice,
            currentValue = type.basePrice,
            annualRent = type.baseRent,
            mortgageRemaining = mortgage,
            isRentedOut = false
        )
        val entry = ChronicleEntry(
            id = UUID.randomUUID().toString(),
            age = state.age,
            year = state.currentYear,
            title = "Purchased ${type.label}",
            narrative = "Acquired ${type.label} for ${formatCurrency(type.basePrice)} (${if (useMortgage) "20% down" else "all cash"}).",
            category = ChronicleCategory.FINANCE
        )
        return state.copy(
            cash = state.cash - downPayment,
            realEstate = state.realEstate + prop,
            chronicle = (listOf(entry) + state.chronicle).take(120)
        ) to "Acquired ${type.label}!"
    }

    fun toggleRentProperty(state: LifeState, propId: String): Pair<LifeState, String> {
        val updated = state.realEstate.map {
            if (it.id == propId) it.copy(isRentedOut = !it.isRentedOut) else it
        }
        return state.copy(realEstate = updated) to "Updated property lease status."
    }

    fun sellProperty(state: LifeState, propId: String): Pair<LifeState, String> {
        val prop = state.realEstate.firstOrNull { it.id == propId } ?: return state to "Property not found."
        val netProceeds = (prop.currentValue - prop.mortgageRemaining)
        return state.copy(
            cash = state.cash + netProceeds,
            realEstate = state.realEstate.filterNot { it.id == propId }
        ) to "Sold ${prop.name} for net ${formatCurrency(netProceeds)}."
    }

    fun payDebt(state: LifeState, amount: Long): Pair<LifeState, String> {
        if (state.debt <= 0L) return state to "You are debt-free!"
        val pay = amount.coerceAtMost(state.debt).coerceAtMost(state.cash)
        if (pay <= 0L) return state to "Not enough cash to pay down debt."
        return state.copy(
            cash = state.cash - pay,
            debt = state.debt - pay
        ) to "Paid ${formatCurrency(pay)} toward outstanding debt."
    }

    fun interactWithNpc(state: LifeState, npcId: String, interaction: NpcInteraction, rng: Random = Random): Pair<LifeState, String> {
        val npc = state.relations.firstOrNull { it.id == npcId && it.isAlive } ?: return state to "Contact unavailable."
        if (state.actionsUsedThisYear >= 4) return state to "You've reached this year's action limit."
        if (state.cash < interaction.cost) return state to "Need ${formatCurrency(interaction.cost)}."

        var cash = state.cash - interaction.cost
        var stats = state.stats
        var relations = state.relations
        var msg = ""

        when (interaction) {
            NpcInteraction.DEEP_TALK -> {
                relations = updateNpc(relations, npcId) {
                    it.copy(closeness = (it.closeness + 9).coerceAtMost(100), trust = (it.trust + 7).coerceAtMost(100))
                }
                stats = stats.copy(happiness = stats.happiness + 4, stress = stats.stress - 5)
                msg = "Had a meaningful conversation with ${npc.name}."
            }
            NpcInteraction.QUALITY_TIME -> {
                relations = updateNpc(relations, npcId) {
                    it.copy(closeness = (it.closeness + 12).coerceAtMost(100), trust = (it.trust + 8).coerceAtMost(100))
                }
                stats = stats.copy(happiness = stats.happiness + 7, stress = stats.stress - 6)
                msg = "Spent a memorable day out with ${npc.name}."
            }
            NpcInteraction.GIFT -> {
                relations = updateNpc(relations, npcId) {
                    it.copy(closeness = (it.closeness + 15).coerceAtMost(100), trust = (it.trust + 10).coerceAtMost(100))
                }
                msg = "${npc.name} loved your thoughtful gift!"
            }
            NpcInteraction.PROPOSE -> {
                if (state.age < 18) return state to "Must be 18+ to marry."
                if (npc.role != RelationRole.PARTNER) return state to "Can only propose to a romantic Partner."
                if (npc.closeness < 70) return state to "${npc.name} isn't ready yet (needs 70+ Closeness)."
                relations = updateNpc(relations, npcId) {
                    it.copy(role = RelationRole.SPOUSE, closeness = 95, trust = 95)
                }
                stats = stats.copy(happiness = stats.happiness + 18)
                msg = "${npc.name} said YES! You got married!"
            }
            NpcInteraction.HAVE_CHILD -> {
                if (state.age < 18) return state to "Must be 18+."
                if (npc.role != RelationRole.PARTNER && npc.role != RelationRole.SPOUSE) {
                    return state to "Start a family with your Partner or Spouse."
                }
                val surname = state.fullName.substringAfterLast(" ")
                val childGender = Gender.entries[rng.nextInt(Gender.entries.size)]
                val childName = LifeCatalog.randomFullName(childGender, surname, rng)
                val child = NpcRelation(
                    id = UUID.randomUUID().toString(),
                    name = childName,
                    role = RelationRole.CHILD,
                    age = 0,
                    closeness = 95,
                    trust = 90,
                    archetype = NpcArchetype.entries[rng.nextInt(NpcArchetype.entries.size)],
                    occupation = "Infant",
                    netWorth = 0L
                )
                relations = relations + child
                stats = stats.copy(happiness = stats.happiness + 20)
                msg = "Welcomed $childName into the family!"
            }
            NpcInteraction.MENTOR -> {
                relations = updateNpc(relations, npcId) {
                    it.copy(
                        closeness = (it.closeness + 10).coerceAtMost(100),
                        netWorth = it.netWorth + 5_000L
                    )
                }
                stats = stats.copy(ethics = stats.ethics + 5)
                msg = "Invested mentorship and capital into ${npc.name}'s growth."
            }
            NpcInteraction.CUT_TIES -> {
                relations = relations.filterNot { it.id == npcId }
                stats = stats.copy(stress = stats.stress - 4)
                msg = "Parted ways with ${npc.name}."
            }
        }

        val entry = ChronicleEntry(
            id = UUID.randomUUID().toString(),
            age = state.age,
            year = state.currentYear,
            title = "${interaction.title}: ${npc.name}",
            narrative = msg,
            category = ChronicleCategory.LOVE
        )
        return state.copy(
            cash = cash,
            stats = stats.clamp(),
            relations = relations,
            actionsUsedThisYear = state.actionsUsedThisYear + 1,
            chronicle = (listOf(entry) + state.chronicle).take(120)
        ) to msg
    }

    fun meetNewPerson(state: LifeState, romantic: Boolean, rng: Random = Random): Pair<LifeState, String> {
        if (state.age < 12) return state to "Focus on growing up first!"
        if (romantic && state.age < 16) return state to "Wait until age 16+ for dating."
        if (state.actionsUsedThisYear >= 4) return state to "No energy left this year."

        val gender = Gender.entries[rng.nextInt(Gender.entries.size)]
        val name = LifeCatalog.randomFullName(gender, rng = rng)
        val role = if (romantic) RelationRole.PARTNER else RelationRole.FRIEND
        val npc = NpcRelation(
            id = UUID.randomUUID().toString(),
            name = name,
            role = role,
            age = (state.age + rng.nextInt(-3, 5)).coerceAtLeast(12),
            closeness = rng.nextInt(52, 76),
            trust = rng.nextInt(48, 72),
            archetype = NpcArchetype.entries[rng.nextInt(NpcArchetype.entries.size)],
            occupation = if (state.age < 22) "Student" else listOf("Architect", "Analyst", "Designer", "Physician", "Founder", "Journalist")[rng.nextInt(6)],
            netWorth = rng.nextLong(5_000L, 180_000L)
        )
        val msg = if (romantic) "Sparked a romance with $name (${npc.occupation})." else "Became friends with $name (${npc.occupation})."
        return state.copy(
            relations = state.relations + npc,
            stats = state.stats.copy(happiness = state.stats.happiness + 6).clamp(),
            actionsUsedThisYear = state.actionsUsedThisYear + 1
        ) to msg
    }

    fun commitCrime(state: LifeState, crime: CrimeAction, rng: Random = Random): Pair<LifeState, String> {
        if (state.age < crime.minAge) return state to "Must be ${crime.minAge}+."
        if (state.prison.isInPrison) return state to "Already behind bars!"
        if (state.actionsUsedThisYear >= 4) return state to "No energy left this year."

        val street = state.skillLevel(SkillType.STREETCRAFT)
        val eng = state.skillLevel(SkillType.ENGINEERING)
        val martialPenalty = if (state.hasLaw(LawType.MARTIAL_LAW)) 0.22 else 0.0
        val heatMult = if (state.hasLaw(LawType.MANDATORY_MINIMUMS)) 1.35 else 1.0

        val (rawOdds, payout, sentenceYears, rawHeatAdd) = when (crime) {
            CrimeAction.PICKPOCKET -> Quad(0.70 + street * 0.03, rng.nextLong(250L, 1_400L), 1, 10)
            CrimeAction.BURGLARY -> Quad(0.58 + street * 0.035, rng.nextLong(4_000L, 16_500L), 2, 15)
            CrimeAction.GRAND_THEFT_AUTO -> Quad(0.50 + street * 0.04, rng.nextLong(18_000L, 55_000L), 3, 20)
            CrimeAction.CYBER_PHISH -> Quad(0.48 + (street + eng) * 0.035, rng.nextLong(14_000L, 42_000L), 4, 20)
            CrimeAction.EXTORTION -> Quad(0.46 + street * 0.04, rng.nextLong(25_000L, 75_000L), 5, 24)
            CrimeAction.INSIDER_TRADE -> Quad(0.50 + state.skillLevel(SkillType.FINANCE) * 0.04, rng.nextLong(40_000L, 125_000L), 5, 24)
            CrimeAction.VAULT_HEIST -> Quad(0.34 + street * 0.045, rng.nextLong(140_000L, 460_000L), 8, 36)
            CrimeAction.CONTRACT_MURDER -> Quad(0.36 + street * 0.05, rng.nextLong(95_000L, 280_000L), 24, 42)
            CrimeAction.ASSASSINATE_RULER -> Quad(0.22 + street * 0.055, 0L, 30, 55)
        }

        val heatAdd = (rawHeatAdd * heatMult).roundToLong().toInt()
        val success = rng.nextDouble() < (rawOdds - martialPenalty).coerceIn(0.10, 0.90)
        val skills = state.skills.toMutableMap()
        skills[SkillType.STREETCRAFT] = addSkillXp(skills[SkillType.STREETCRAFT], if (crime.isLethal) 85 else 50)

        return if (success) {
            if (crime == CrimeAction.ASSASSINATE_RULER) {
                val strikeMonarch = state.government.regimeType != RegimeType.REPUBLIC &&
                    state.royal.rank != RoyalRank.MONARCH && rng.nextBoolean()
                val newPres = LifeCatalog.rulerPool[rng.nextInt(LifeCatalog.rulerPool.size)]
                val newMon = LifeCatalog.monarchPool[rng.nextInt(LifeCatalog.monarchPool.size)]
                val nextRoyal = if (strikeMonarch && state.royal.rank == RoyalRank.CROWN_HEIR) {
                    state.royal.copy(rank = RoyalRank.MONARCH)
                } else state.royal
                val targetTitle = if (strikeMonarch) state.government.monarchName else "${state.government.rulerTitle} ${state.government.presidentName}"
                val gov = state.government.copy(
                    presidentName = if (strikeMonarch) state.government.presidentName else newPres,
                    monarchName = if (strikeMonarch) newMon else state.government.monarchName,
                    isPlayerPresident = if (!strikeMonarch) false else state.government.isPlayerPresident,
                    nationalStability = (state.government.nationalStability - 28).coerceIn(10, 100),
                    proposedBill = null
                )
                val entry = ChronicleEntry(
                    id = UUID.randomUUID().toString(),
                    age = state.age,
                    year = state.currentYear,
                    title = "Regicide / Political Assassination!",
                    narrative = "$targetTitle was eliminated in a precision strike. The nation plunged into crisis as ${if (strikeMonarch && nextRoyal.rank == RoyalRank.MONARCH) "you seized the throne!" else "a successor took emergency power."}",
                    category = ChronicleCategory.STATE,
                    tags = listOf("Assassination", "+$heatAdd Heat")
                )
                return state.copy(
                    heatLevel = (state.heatLevel + heatAdd).coerceAtMost(100),
                    prison = state.prison.copy(notoriety = state.prison.notoriety + 45),
                    skills = skills,
                    royal = nextRoyal,
                    government = gov,
                    stats = state.stats.copy(ethics = state.stats.ethics - 25, stress = state.stats.stress + 18, fame = state.stats.fame + 15).clamp(),
                    actionsUsedThisYear = state.actionsUsedThisYear + 1,
                    chronicle = (listOf(entry) + state.chronicle).take(120)
                ) to "Target $targetTitle assassinated! National power shifted!"
            }

            val entry = ChronicleEntry(
                id = UUID.randomUUID().toString(),
                age = state.age,
                year = state.currentYear,
                title = "${crime.title} Succeeded",
                narrative = if (crime.isLethal) "Executed a clean lethal contract for ${formatCurrency(payout)}. Detectives have zero leads."
                else "Cleared ${formatCurrency(payout)} without leaving a clean trail.",
                category = ChronicleCategory.CRIME,
                tags = listOf("+${formatCurrency(payout)}", "+$heatAdd Heat")
            )
            state.copy(
                cash = state.cash + payout,
                heatLevel = (state.heatLevel + heatAdd).coerceAtMost(100),
                prison = state.prison.copy(notoriety = state.prison.notoriety + if (crime.isLethal) 22 else 8),
                skills = skills,
                stats = state.stats.copy(ethics = state.stats.ethics - (if (crime.isLethal) 22 else 10), stress = state.stats.stress + 8).clamp(),
                actionsUsedThisYear = state.actionsUsedThisYear + 1,
                chronicle = (listOf(entry) + state.chronicle).take(120)
            ) to "Pulled off ${crime.title} for +${formatCurrency(payout)}!"
        } else {
            GovernanceCrimeEngine.sendToPrison(
                state = state.copy(skills = skills, actionsUsedThisYear = state.actionsUsedThisYear + 1),
                crimeTitle = crime.title,
                baseYears = sentenceYears,
                isLethal = crime.isLethal
            )
        }
    }

    fun appealSentence(state: LifeState, rng: Random = Random): Pair<LifeState, String> {
        if (!state.prison.isInPrison) return state to "You aren't in prison."
        val fee = 25_000L
        if (state.cash < fee) return state to "Elite appellate counsel costs ${formatCurrency(fee)}."
        val strictPenalty = if (state.hasLaw(LawType.MANDATORY_MINIMUMS)) 0.16 else 0.0
        val gangBonus = if (state.prison.gang == PrisonGang.LAWHOUSE_CIRCLE) 0.12 else 0.0
        val prepBonus = state.prison.lawStudyProgress / 350.0
        val odds = (0.35 + state.skillLevel(SkillType.RHETORIC) * 0.04 + prepBonus + gangBonus - strictPenalty).coerceIn(0.10, 0.85)
        return if (rng.nextDouble() < odds) {
            state.copy(
                cash = state.cash - fee,
                prison = PrisonState(notoriety = state.prison.notoriety)
            ) to "Appeal succeeded! Conviction overturned and you walk free!"
        } else {
            state.copy(cash = state.cash - fee) to "Appeal denied by appellate panel."
        }
    }

    fun runForPresident(state: LifeState, budget: Long, rng: Random = Random) =
        GovernanceCrimeEngine.runForPresident(state, budget, rng)

    fun enactLawAsRuler(state: LifeState, lawType: LawType) =
        GovernanceCrimeEngine.enactLawAsRuler(state, lawType)

    fun respondToPendingBill(state: LifeState, support: Boolean, lobbyCost: Long = 0L) =
        GovernanceCrimeEngine.respondToPendingBill(state, support, lobbyCost)

    fun seekRoyalMarriage(state: LifeState, rng: Random = Random) =
        GovernanceCrimeEngine.seekRoyalMarriage(state, rng)

    fun performRoyalAction(state: LifeState, action: RoyalAction, rng: Random = Random) =
        GovernanceCrimeEngine.performRoyalAction(state, action, rng)

    fun killTargetNpc(state: LifeState, npcId: String, rng: Random = Random) =
        GovernanceCrimeEngine.killTargetNpc(state, npcId, rng)

    fun performPrisonAction(state: LifeState, action: PrisonAction, rng: Random = Random) =
        GovernanceCrimeEngine.performPrisonAction(state, action, rng)

    fun joinPrisonGang(state: LifeState, gang: PrisonGang) =
        GovernanceCrimeEngine.joinPrisonGang(state, gang)

    fun setLifestyle(state: LifeState, tier: LifestyleTier): Pair<LifeState, String> {
        if (state.age < 18) return state to "Lifestyle tiers unlock at age 18."
        return state.copy(lifestyle = tier) to "Switched lifestyle to ${tier.label} (${formatCurrency(tier.annualCost)}/yr)."
    }

    fun toggleSchoolClub(state: LifeState, club: SchoolClub): Pair<LifeState, String> {
        if (state.age !in 12..23) return state to "School clubs are for ages 12–23."
        return if (club in state.schoolClubs) {
            state.copy(schoolClubs = state.schoolClubs - club) to "Left ${club.label}."
        } else {
            if (state.schoolClubs.size >= 2) return state to "You can join up to 2 clubs."
            state.copy(schoolClubs = state.schoolClubs + club) to "Joined ${club.label} (+${club.skill.label} XP/yr)!"
        }
    }

    fun buyLuxuryItem(state: LifeState, item: LuxuryItem): Pair<LifeState, String> {
        if (item in state.collectibles) return state to "Already own ${item.label}."
        if (state.cash < item.price) return state to "Need ${formatCurrency(item.price)} for ${item.label}."
        val entry = ChronicleEntry(
            id = UUID.randomUUID().toString(),
            age = state.age,
            year = state.currentYear,
            title = "Acquired ${item.label}",
            narrative = "Added ${item.label} to your private collection (${item.perkSummary}).",
            category = ChronicleCategory.FINANCE
        )
        return state.copy(
            cash = state.cash - item.price,
            collectibles = state.collectibles + item,
            stats = state.stats.copy(
                fame = state.stats.fame + item.fameBonus,
                charisma = state.stats.charisma + item.charismaBonus,
                happiness = state.stats.happiness + 8
            ).clamp(),
            chronicle = (listOf(entry) + state.chronicle).take(120)
        ) to "Purchased ${item.label}!"
    }

    fun playHighStakesGame(state: LifeState, stake: Long, rng: Random = Random): Pair<LifeState, String> {
        if (state.age < 18) return state to "Must be 18+."
        if (state.actionsUsedThisYear >= 4) return state to "No energy left this year."
        if (state.cash < stake) return state to "Need ${formatCurrency(stake)} to buy in."

        val odds = (0.42 + state.skillLevel(SkillType.FINANCE) * 0.015 + state.skillLevel(SkillType.STREETCRAFT) * 0.015).coerceAtMost(0.62)
        val won = rng.nextDouble() < odds
        val delta = if (won) (stake * 1.4).roundToLong() else -stake
        val msg = if (won) "High-stakes table paid out +${formatCurrency(delta)}!" else "Lost ${formatCurrency(stake)} on a brutal river card."
        val entry = ChronicleEntry(
            id = UUID.randomUUID().toString(),
            age = state.age,
            year = state.currentYear,
            title = if (won) "High-Stakes Win" else "Table Loss",
            narrative = msg,
            category = ChronicleCategory.FINANCE
        )
        return state.copy(
            cash = state.cash + delta,
            stats = state.stats.copy(
                happiness = state.stats.happiness + if (won) 10 else -9,
                stress = state.stats.stress + if (won) 4 else 12
            ).clamp(),
            actionsUsedThisYear = state.actionsUsedThisYear + 1,
            chronicle = (listOf(entry) + state.chronicle).take(120)
        ) to msg
    }

    fun seekPromotion(state: LifeState): Pair<LifeState, String> {
        val job = state.currentJob ?: return state to "No active career."
        if (state.actionsUsedThisYear >= 4) return state to "No energy left this year."
        if (job.performance < 75) return state to "Need 75%+ Performance Rating before seeking promotion (currently ${job.performance}%)."

        val nextListing = LifeCatalog.careerListings.firstOrNull {
            it.track == job.track && it.tier == job.tier + 1
        } ?: return state to "You are already at the pinnacle of ${job.track.label}!"

        if (state.educationLevel.rank < nextListing.minEducation.rank) {
            return state to "Next tier (${nextListing.title}) requires ${nextListing.minEducation.label}."
        }
        if (nextListing.requiredSkill != null && state.skillLevel(nextListing.requiredSkill) < nextListing.requiredSkillLevel) {
            return state to "Next tier (${nextListing.title}) requires ${nextListing.requiredSkill.label} Lv.${nextListing.requiredSkillLevel}."
        }

        val promoted = JobInfo(
            id = nextListing.id,
            title = nextListing.title,
            track = nextListing.track,
            tier = nextListing.tier,
            salary = nextListing.baseSalary,
            stressLoad = nextListing.stressLoad,
            performance = 62,
            yearsInRole = 0
        )
        val entry = ChronicleEntry(
            id = UUID.randomUUID().toString(),
            age = state.age,
            year = state.currentYear,
            title = "Promoted: ${nextListing.title}!",
            narrative = "Earned promotion to ${nextListing.title} at ${formatCurrency(nextListing.baseSalary)}/yr.",
            category = ChronicleCategory.CAREER,
            tags = listOf("Tier ${nextListing.tier}", "${formatCurrency(nextListing.baseSalary)}/yr")
        )
        return state.copy(
            currentJob = promoted,
            stats = state.stats.copy(happiness = state.stats.happiness + 12, fame = state.stats.fame + 4).clamp(),
            actionsUsedThisYear = state.actionsUsedThisYear + 1,
            chronicle = (listOf(entry) + state.chronicle).take(120)
        ) to "Promoted to ${nextListing.title}!"
    }

    fun officePolitics(state: LifeState, tactic: String, rng: Random = Random): Pair<LifeState, String> {
        val job = state.currentJob ?: return state to "No active career."
        if (state.actionsUsedThisYear >= 4) return state to "No energy left this year."

        val skills = state.skills.toMutableMap()
        return when (tactic) {
            "MENTOR" -> {
                skills[SkillType.RHETORIC] = addSkillXp(skills[SkillType.RHETORIC], 45)
                state.copy(
                    currentJob = job.copy(performance = (job.performance + 8).coerceAtMost(100)),
                    skills = skills,
                    stats = state.stats.copy(ethics = state.stats.ethics + 6, happiness = state.stats.happiness + 5).clamp(),
                    actionsUsedThisYear = state.actionsUsedThisYear + 1
                ) to "Mentored juniors—earned leadership goodwill (+8% Performance, +Rhetoric XP)."
            }
            "SABOTAGE" -> {
                val caught = rng.nextDouble() < 0.28
                if (caught) {
                    state.copy(
                        currentJob = job.copy(performance = (job.performance - 16).coerceAtLeast(10)),
                        stats = state.stats.copy(ethics = state.stats.ethics - 14, stress = state.stats.stress + 14).clamp(),
                        actionsUsedThisYear = state.actionsUsedThisYear + 1
                    ) to "Sabotage backfired! Management flagged your conduct (-16% Performance)."
                } else {
                    val arc = DelayedArc(
                        id = UUID.randomUUID().toString(),
                        arcKey = "rival_vendetta",
                        triggerAge = state.age + 2,
                        stage = 1,
                        originChoiceLabel = "Sabotaged office rival at Age ${state.age}"
                    )
                    state.copy(
                        currentJob = job.copy(performance = (job.performance + 18).coerceAtMost(100)),
                        stats = state.stats.copy(ethics = state.stats.ethics - 12).clamp(),
                        pendingArcs = state.pendingArcs + arc,
                        lastCascadeSummary = "Planted future rival vendetta (may strike in ~2 years)",
                        actionsUsedThisYear = state.actionsUsedThisYear + 1
                    ) to "Outmaneuvered your rival (+18% Performance), though they suspect you..."
                }
            }
            else -> {
                skills[SkillType.RHETORIC] = addSkillXp(skills[SkillType.RHETORIC], 30)
                state.copy(
                    currentJob = job.copy(performance = (job.performance + 10).coerceAtMost(100)),
                    skills = skills,
                    stats = state.stats.copy(charisma = state.stats.charisma + 4).clamp(),
                    actionsUsedThisYear = state.actionsUsedThisYear + 1
                ) to "Built executive visibility across departments (+10% Performance)."
            }
        }
    }

    fun replyToNpcMessage(state: LifeState, messageId: String, replyIndex: Int): Pair<LifeState, String> {
        val msg = state.inbox.firstOrNull { it.id == messageId } ?: return state to "Message not found."
        val reply = msg.replyOptions.getOrNull(replyIndex) ?: return state to "Invalid reply."
        if (reply.cashDelta < 0 && state.cash < abs(reply.cashDelta)) {
            return state to "Need ${formatCurrency(abs(reply.cashDelta))} for this reply."
        }
        val meetsSkill = reply.requiredSkill == null || state.skillLevel(reply.requiredSkill) >= reply.requiredLevel
        if (!meetsSkill) {
            return state to "Requires ${reply.requiredSkill?.label} Lv.${reply.requiredLevel} for that response."
        }

        val updatedRelations = updateNpc(state.relations, msg.npcId) { npc ->
            npc.copy(
                closeness = (npc.closeness + reply.closenessDelta).coerceIn(0, 100),
                trust = (npc.trust + reply.trustDelta).coerceIn(0, 100),
                lastConversationSummary = reply.responseSummary
            )
        }
        val updatedInbox = state.inbox.map {
            if (it.id == messageId) it.copy(isRead = true, resolvedReplyText = reply.responseSummary) else it
        }
        val updatedArcs = if (reply.spawnsArcKey != null) {
            state.pendingArcs + DelayedArc(
                id = UUID.randomUUID().toString(),
                arcKey = reply.spawnsArcKey,
                triggerAge = state.age + reply.cascadeDelayYears,
                stage = 1,
                originChoiceLabel = "Replied to ${msg.senderName} at Age ${state.age}",
                targetNpcId = msg.npcId,
                targetNpcName = msg.senderName
            )
        } else state.pendingArcs

        val entry = ChronicleEntry(
            id = UUID.randomUUID().toString(),
            age = state.age,
            year = state.currentYear,
            title = "Message to ${msg.senderName}",
            narrative = reply.responseSummary,
            category = ChronicleCategory.LOVE,
            tags = buildList {
                if (reply.closenessDelta != 0) add("Closeness ${if (reply.closenessDelta > 0) "+" else ""}${reply.closenessDelta}")
                if (reply.spawnsArcKey != null) add("Cascade Planted")
            }
        )

        return state.copy(
            cash = state.cash + reply.cashDelta,
            stats = applyStatDelta(state.stats, reply.statDelta),
            relations = updatedRelations,
            inbox = updatedInbox,
            pendingArcs = updatedArcs,
            heatLevel = (state.heatLevel + reply.heatDelta).coerceIn(0, 100),
            lastCascadeSummary = if (reply.spawnsArcKey != null) "Cascade planted with ${msg.senderName}" else state.lastCascadeSummary,
            chronicle = (listOf(entry) + state.chronicle).take(120)
        ) to reply.responseSummary
    }

    fun applyDirectDialogue(state: LifeState, npcId: String, reply: MessageReply): Pair<LifeState, String> {
        val npc = state.relations.firstOrNull { it.id == npcId && it.isAlive } ?: return state to "Contact unavailable."
        if (state.actionsUsedThisYear >= 4) return state to "No energy left this year."
        if (reply.requiredSkill != null && state.skillLevel(reply.requiredSkill) < reply.requiredLevel) {
            return state to "Requires ${reply.requiredSkill.label} Lv.${reply.requiredLevel}."
        }
        val updatedRelations = updateNpc(state.relations, npcId) {
            it.copy(
                closeness = (it.closeness + reply.closenessDelta).coerceIn(0, 100),
                trust = (it.trust + reply.trustDelta).coerceIn(0, 100),
                lastConversationSummary = reply.responseSummary
            )
        }
        val updatedArcs = if (reply.spawnsArcKey != null) {
            state.pendingArcs + DelayedArc(
                id = UUID.randomUUID().toString(),
                arcKey = reply.spawnsArcKey,
                triggerAge = state.age + reply.cascadeDelayYears,
                stage = 1,
                originChoiceLabel = "Side deal with ${npc.name} at Age ${state.age}",
                targetNpcId = npcId,
                targetNpcName = npc.name
            )
        } else state.pendingArcs

        val entry = ChronicleEntry(
            id = UUID.randomUUID().toString(),
            age = state.age,
            year = state.currentYear,
            title = "Talked with ${npc.name}",
            narrative = reply.responseSummary,
            category = ChronicleCategory.LOVE
        )
        return state.copy(
            cash = state.cash + reply.cashDelta,
            stats = applyStatDelta(state.stats, reply.statDelta),
            relations = updatedRelations,
            pendingArcs = updatedArcs,
            heatLevel = (state.heatLevel + reply.heatDelta).coerceIn(0, 100),
            actionsUsedThisYear = state.actionsUsedThisYear + 1,
            chronicle = (listOf(entry) + state.chronicle).take(120)
        ) to reply.responseSummary
    }

    private fun buildInitialFamily(surname: String, preset: OriginPreset, rng: Random): List<NpcRelation> {
        if (preset == OriginPreset.ORPHAN_GRIT) {
            return listOf(
                NpcRelation(
                    id = UUID.randomUUID().toString(),
                    name = LifeCatalog.randomFullName(Gender.FEMALE, rng = rng),
                    role = RelationRole.MENTOR,
                    age = 38,
                    closeness = 70,
                    trust = 75,
                    archetype = NpcArchetype.WARM,
                    occupation = "Social Caseworker",
                    netWorth = 45_000L
                )
            )
        }
        val baseWealth = when (preset) {
            OriginPreset.TRUST_FUND -> 1_800_000L
            OriginPreset.SCHOLAR_DYNASTY -> 240_000L
            OriginPreset.SYNDICATE_BLOOD -> 420_000L
            else -> rng.nextLong(25_000L, 190_000L)
        }
        val list = mutableListOf(
            NpcRelation(
                id = UUID.randomUUID().toString(),
                name = LifeCatalog.randomFullName(Gender.FEMALE, surname, rng),
                role = RelationRole.MOTHER,
                age = rng.nextInt(24, 38),
                closeness = rng.nextInt(72, 95),
                trust = rng.nextInt(75, 95),
                archetype = NpcArchetype.WARM,
                occupation = if (preset == OriginPreset.SCHOLAR_DYNASTY) "Professor" else "Operations Manager",
                netWorth = baseWealth
            ),
            NpcRelation(
                id = UUID.randomUUID().toString(),
                name = LifeCatalog.randomFullName(Gender.MALE, surname, rng),
                role = RelationRole.FATHER,
                age = rng.nextInt(25, 41),
                closeness = rng.nextInt(65, 92),
                trust = rng.nextInt(70, 92),
                archetype = if (preset == OriginPreset.SYNDICATE_BLOOD) NpcArchetype.CALCULATING else NpcArchetype.STOIC,
                occupation = if (preset == OriginPreset.SYNDICATE_BLOOD) "Import Broker" else "Civil Engineer",
                netWorth = baseWealth
            )
        )
        if (rng.nextBoolean()) {
            val g = if (rng.nextBoolean()) Gender.FEMALE else Gender.MALE
            list += NpcRelation(
                id = UUID.randomUUID().toString(),
                name = LifeCatalog.randomFullName(g, surname, rng),
                role = RelationRole.SIBLING,
                age = rng.nextInt(1, 7),
                closeness = rng.nextInt(60, 88),
                trust = rng.nextInt(60, 85),
                archetype = NpcArchetype.ECCENTRIC,
                occupation = "Child",
                netWorth = 0L
            )
        }
        return list
    }

    private fun updateNpc(
        list: List<NpcRelation>,
        id: String,
        transform: (NpcRelation) -> NpcRelation
    ): List<NpcRelation> = list.map { if (it.id == id) transform(it) else it }

    private fun addSkillXp(current: SkillProgress?, delta: Int): SkillProgress {
        val base = current ?: return SkillProgress(SkillType.ENGINEERING, delta)
        return base.copy(xp = (base.xp + delta).coerceAtMost(950))
    }

    private fun applyStatDelta(s: Stats, d: StatDelta): Stats = s.copy(
        health = s.health + d.health,
        happiness = s.happiness + d.happiness,
        intellect = s.intellect + d.intellect,
        charisma = s.charisma + d.charisma,
        discipline = s.discipline + d.discipline,
        stress = s.stress + d.stress,
        ethics = s.ethics + d.ethics,
        fame = s.fame + d.fame
    ).clamp()

    private fun checkMortality(age: Int, stats: Stats, rng: Random): String? {
        if (stats.health <= 0) return "Critical Health Collapse"
        val baseRisk = when {
            age < 55 -> 0.001
            age < 75 -> 0.012
            age < 88 -> 0.055
            age < 98 -> 0.16
            else -> 0.35
        }
        val healthMod = if (stats.health < 25) 0.08 else 0.0
        val stressMod = if (stats.stress > 88) 0.03 else 0.0
        return if (rng.nextDouble() < (baseRisk + healthMod + stressMod)) {
            when {
                stats.stress > 85 -> "Stress-Induced Cardiac Failure"
                age >= 82 -> "Natural Causes"
                else -> "Sudden Medical Complication"
            }
        } else null
    }

    private fun buildQuietYearSummary(state: LifeState): String = when {
        state.age <= 5 -> "Growing fast, learning words, and exploring every corner of the house."
        state.age in 6..17 -> "Another school year passed—balancing friendships, curiosity, and growing ambitions."
        state.prison.isInPrison -> "Another year behind bars (${state.prison.yearsRemaining} remaining). Counting days."
        state.business != null -> "${state.business.name} generated ${formatCurrency(state.business.annualRevenue)} in revenue during ${state.economicPhase.label.lowercase()}."
        state.currentJob != null -> "Worked steadily as ${state.currentJob.title} while navigating ${state.economicPhase.label.lowercase()}."
        else -> "Spent the year reflecting, exploring opportunities, and managing personal affairs."
    }

    private fun milestone(age: Int, year: Int, title: String, narrative: String) = ChronicleEntry(
        id = UUID.randomUUID().toString(),
        age = age,
        year = year,
        title = title,
        narrative = narrative,
        category = ChronicleCategory.MILESTONE
    )

    private data class Quintuple<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E)
    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}

fun formatCurrency(amount: Long): String {
    val absVal = abs(amount)
    val sign = if (amount < 0) "-" else ""
    return when {
        absVal >= 1_000_000_000L -> "$sign$${"%.2f".format(absVal / 1_000_000_000.0)}B"
        absVal >= 1_000_000L -> "$sign$${"%.2f".format(absVal / 1_000_000.0)}M"
        absVal >= 10_000L -> "$sign$${"%,d".format(absVal)}"
        else -> "$sign$${"%,d".format(absVal)}"
    }
}
