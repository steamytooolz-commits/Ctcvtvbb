package com.example.data.model

import com.squareup.moshi.JsonClass
import kotlin.math.roundToLong

enum class Gender(val label: String) {
    FEMALE("Female"),
    MALE("Male"),
    NON_BINARY("Non-Binary")
}

enum class OriginPreset(
    val title: String,
    val subtitle: String,
    val startingCash: Long,
    val badge: String
) {
    RANDOM("Twist of Fate", "Randomized birthplace, family wealth, and innate traits.", 250L, "Classic"),
    ORPHAN_GRIT("Zero-to-Hero", "Raised in a state home with iron discipline and street instincts.", 50L, "Hardcore"),
    TRUST_FUND("Gilded Heir", "Born into generational wealth, elite connections, and high expectations.", 120_000L, "Privileged"),
    RECESSION_PRODIGY("Recession Prodigy", "High intellect born during an economic downturn. Outsmart the crash.", 1_500L, "Tactical"),
    SYNDICATE_BLOOD("Syndicate Blood", "A family shadowed by underground ties, high charisma, and heat.", 8_000L, "Underworld"),
    SCHOLAR_DYNASTY("Scholar Dynasty", "Academic parents who prize intellect, arts, and curiosity above gold.", 6_000L, "Academic"),
    ROYAL_BLOOD("Royal Bloodline", "Born to a ruling House with noble stipends, decrees, and palace intrigue.", 280_000L, "Royalty")
}

enum class RegimeType(val label: String) {
    REPUBLIC("Presidential Republic"),
    CONSTITUTIONAL_MONARCHY("Constitutional Monarchy"),
    ABSOLUTE_CROWN("Sovereign Crown")
}

enum class RoyalRank(val label: String, val annualStipend: Long, val prestige: Int) {
    NONE("Commoner", 0L, 0),
    BARON("Baron / Baroness", 38_000L, 14),
    DUKE("Duke / Duchess", 160_000L, 30),
    CROWN_HEIR("Crown Prince / Princess", 440_000L, 55),
    MONARCH("King / Queen", 1_500_000L, 88)
}

enum class LawType(
    val title: String,
    val summary: String,
    val taxDelta: Double = 0.0,
    val salaryMultiplier: Double = 1.0,
    val marketDriftDelta: Double = 0.0,
    val isCryptoBan: Boolean = false,
    val isFreeTuition: Boolean = false,
    val isStrictSentencing: Boolean = false,
    val isMartialLaw: Boolean = false,
    val isDeathPenalty: Boolean = false,
    val isUniversalHealth: Boolean = false,
    val isRoyalSovereignty: Boolean = false,
    val isAmnesty: Boolean = false
) {
    WEALTH_SURTAX(
        title = "Wealth Surtax Act",
        summary = "+12% tax rate for $1M+ net worth; funds public stability (+6%).",
        taxDelta = 0.12
    ),
    CORPORATE_TAX_CUT(
        title = "Enterprise Deregulation Act",
        summary = "-7% tax rate & +6% market boom drift; lowers public stability.",
        taxDelta = -0.07,
        marketDriftDelta = 0.06
    ),
    FREE_TUITION(
        title = "Universal Tuition Charter",
        summary = "All University & Graduate School tuition drops to $0!",
        taxDelta = 0.02,
        isFreeTuition = true
    ),
    CRYPTO_CRACKDOWN(
        title = "Digital Asset Prohibition",
        summary = "Bans Crypto trading (BTCX/ETHR/NVAX) & plunges coin prices.",
        isCryptoBan = true
    ),
    OPEN_TRADE_ACCORD(
        title = "Global Free Trade Accord",
        summary = "+14% Career Salaries & stronger Business profit margins.",
        salaryMultiplier = 1.14,
        marketDriftDelta = 0.03
    ),
    MANDATORY_MINIMUMS(
        title = "Iron Fist Sentencing Act",
        summary = "Prison terms +50% longer, Appeals harder, Crime Heat surges faster.",
        isStrictSentencing = true
    ),
    CAPITAL_PUNISHMENT(
        title = "Capital Punishment Mandate",
        summary = "Murder, assassination, and treason carry Death Row execution!",
        isDeathPenalty = true
    ),
    MARTIAL_LAW(
        title = "Emergency Martial Law",
        summary = "Suspends elections, cuts crime success -22%, enforces Death Row.",
        isMartialLaw = true,
        isDeathPenalty = true,
        isStrictSentencing = true
    ),
    UNIVERSAL_HEALTHCARE(
        title = "National Healthcare Charter",
        summary = "Doctor checkups are FREE and citizens gain +3 Health per year.",
        taxDelta = 0.03,
        isUniversalHealth = true
    ),
    ROYAL_CROWN_ACT(
        title = "Crown Prerogative Decree",
        summary = "Doubles Royal Stipends and empowers Monarch to rule by decree.",
        isRoyalSovereignty = true
    ),
    CRIMINAL_AMNESTY(
        title = "Clean Slate Amnesty Act",
        summary = "Wipes Heat to 0 & commutes non-lethal prison sentences immediately!",
        isAmnesty = true
    )
}

@JsonClass(generateAdapter = true)
data class EnactedLaw(
    val type: LawType,
    val enactedYear: Int,
    val enactedBy: String
)

@JsonClass(generateAdapter = true)
data class ProposedBill(
    val id: String,
    val lawType: LawType,
    val proposerTitle: String,
    val proposerName: String,
    val proposedAge: Int
)

@JsonClass(generateAdapter = true)
data class RoyalState(
    val rank: RoyalRank = RoyalRank.NONE,
    val houseName: String = "",
    val approval: Int = 65,
    val scandalHeat: Int = 0,
    val decreesPassed: Int = 0,
    val isExiled: Boolean = false
)

@JsonClass(generateAdapter = true)
data class GovernmentState(
    val regimeType: RegimeType = RegimeType.REPUBLIC,
    val rulerTitle: String = "President",
    val presidentName: String = "Elena Rostova",
    val monarchName: String = "King Alistair IV",
    val presidentApproval: Int = 54,
    val nextElectionAge: Int = 22,
    val isPlayerPresident: Boolean = false,
    val termYearsServed: Int = 0,
    val nationalStability: Int = 74,
    val activeLaws: List<EnactedLaw> = emptyList(),
    val proposedBill: ProposedBill? = null
)

enum class PrisonSecurity(val label: String) {
    MINIMUM("Minimum Security"),
    MAXIMUM("Maximum Security"),
    SUPERMAX("Supermax Lockdown"),
    DEATH_ROW("Death Row")
}

enum class PrisonGang(val label: String, val perkDesc: String) {
    NONE("Unaffiliated", "Fly under the radar"),
    IRON_SYNDICATE("Iron Syndicate", "+Smuggling & Extortion odds"),
    LAWHOUSE_CIRCLE("Lawhouse Circle", "+Appellate & Parole odds"),
    APEX_ENFORCERS("Apex Enforcers", "+Respect, Shanks & Riot power")
}

enum class EconomicPhase(
    val label: String,
    val marketDrift: Double,
    val volatility: Double,
    val jobBonus: Double,
    val inflationRate: Double
) {
    BOOM("Economic Boom", 0.14, 0.12, 1.15, 0.035),
    EXPANSION("Steady Expansion", 0.08, 0.08, 1.05, 0.024),
    STAGFLATION("Stagflation", 0.01, 0.14, 0.95, 0.065),
    RECESSION("Recession", -0.09, 0.18, 0.85, 0.015),
    CRASH("Market Crash", -0.22, 0.28, 0.72, 0.005)
}

enum class SkillType(val label: String, val iconHint: String) {
    ATHLETICS("Athletics", "Fitness & Endurance"),
    RHETORIC("Rhetoric", "Persuasion & Leadership"),
    ENGINEERING("Engineering", "Systems & Tech"),
    FINANCE("Finance", "Markets & Valuation"),
    ARTS("Arts", "Craft & Expression"),
    STREETCRAFT("Streetcraft", "Stealth & Underworld")
}

enum class EducationLevel(val label: String, val rank: Int) {
    NONE("No Formal Schooling", 0),
    PRIMARY("Primary School", 1),
    HIGH_SCHOOL("High School Diploma", 2),
    IN_UNIVERSITY("University Student", 3),
    UNIVERSITY("Bachelor's Degree", 4),
    GRAD_SCHOOL("Graduate Student", 5),
    DOCTORATE("Advanced Degree", 6)
}

enum class DegreeMajor(val label: String, val tuitionPerYear: Long, val primarySkill: SkillType) {
    COMPUTER_SCIENCE("Computer Science", 14_000L, SkillType.ENGINEERING),
    FINANCE("Quantitative Finance", 16_000L, SkillType.FINANCE),
    MEDICINE("Biomedical Sciences", 24_000L, SkillType.ATHLETICS),
    LAW("Jurisprudence & Law", 20_000L, SkillType.RHETORIC),
    FINE_ARTS("Fine Arts & Design", 11_000L, SkillType.ARTS),
    POLITICAL_SCIENCE("Political Economy", 13_000L, SkillType.RHETORIC)
}

enum class CareerTrack(val label: String) {
    CORPORATE("Corporate"),
    TECH("Technology"),
    MEDICAL("Medical"),
    LEGAL("Legal"),
    CREATIVE("Creative"),
    POLITICS("Politics"),
    SYNDICATE("Syndicate")
}

enum class BusinessSector(val label: String, val startupCost: Long, val baseMargin: Double) {
    SAAS("Cloud SaaS", 35_000L, 0.34),
    BIOTECH("Biotech Lab", 120_000L, 0.42),
    MEDIA("Indie Media Studio", 22_000L, 0.25),
    HOSPITALITY("Boutique Hospitality", 65_000L, 0.18),
    LOGISTICS("Autonomous Logistics", 90_000L, 0.24)
}

enum class RelationRole(val label: String) {
    MOTHER("Mother"),
    FATHER("Father"),
    SIBLING("Sibling"),
    FRIEND("Friend"),
    PARTNER("Partner"),
    SPOUSE("Spouse"),
    CHILD("Child"),
    MENTOR("Mentor"),
    RIVAL("Rival")
}

enum class NpcArchetype(val label: String) {
    WARM("Warmhearted"),
    AMBITIOUS("Ambitious"),
    ECCENTRIC("Eccentric"),
    STOIC("Stoic"),
    CALCULATING("Calculating")
}

enum class ChronicleCategory(val label: String) {
    MILESTONE("Milestone"),
    CAREER("Career"),
    LOVE("Relations"),
    FINANCE("Markets"),
    CRIME("Underworld"),
    HEALTH("Vitals"),
    LEGACY("Legacy"),
    CASCADE("Cascade"),
    STATE("Crown & Gov")
}

enum class PropertyType(val label: String, val basePrice: Long, val baseRent: Long, val maintenance: Long) {
    STUDIO_APT("Downtown Studio", 145_000L, 14_400L, 2_400L),
    SUBURBAN_HOME("Maplecrest Townhome", 340_000L, 28_800L, 5_200L),
    LUXURY_CONDO("Skyline Penthouse", 920_000L, 66_000L, 14_000L),
    COMMERCIAL_LOFT("Riverfront Commercial Loft", 610_000L, 54_000L, 8_800L),
    ESTATE_MANOR("Crestwood Estate Manor", 2_450_000L, 150_000L, 38_000L)
}

enum class LifestyleTier(
    val label: String,
    val annualCost: Long,
    val joyDelta: Int,
    val stressDelta: Int,
    val fameGain: Int
) {
    FRUGAL("Frugal Minimalist", 0L, -2, 2, 0),
    MODEST("Modest Comfort", 4_500L, 2, -2, 0),
    UPSCALE("Upscale Urban", 22_000L, 6, -5, 2),
    LUXURY("Lavish Elite", 78_000L, 11, -9, 6)
}

enum class SchoolClub(val label: String, val skill: SkillType) {
    DEBATE("Debate Union", SkillType.RHETORIC),
    ROBOTICS("Robotics & Code Lab", SkillType.ENGINEERING),
    VARSITY("Varsity Athletics", SkillType.ATHLETICS),
    STUDIO_ART("Arts & Film Collective", SkillType.ARTS),
    POKER("After-Hours Markets Club", SkillType.FINANCE)
}

enum class LuxuryItem(
    val label: String,
    val price: Long,
    val perkSummary: String,
    val fameBonus: Int,
    val charismaBonus: Int
) {
    VINTAGE_CHRONO("Swiss Chronograph", 18_000L, "+4 Charisma, +3 Fame", 3, 4),
    CONTEMPORARY_ART("Blue-Chip Canvas", 85_000L, "+6 Fame, Prestige Asset", 6, 3),
    SPORTS_COUPE("Collector Grand Tourer", 145_000L, "+10 Fame, +8 Joy", 10, 5),
    PRIVATE_CHARTER("Fractional Jet Share", 620_000L, "+16 Fame, -10 Stress", 16, 6)
}

@JsonClass(generateAdapter = true)
data class StatusEffect(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val yearsLeft: Int = 2,
    val isPositive: Boolean = true
)

@JsonClass(generateAdapter = true)
data class DelayedArc(
    val id: String = "",
    val arcKey: String = "",
    val triggerAge: Int = 0,
    val stage: Int = 1,
    val originChoiceLabel: String = "",
    val targetNpcId: String? = null,
    val targetNpcName: String? = null
)

@JsonClass(generateAdapter = true)
data class MessageReply(
    val label: String = "",
    val toneTag: String = "",
    val requiredSkill: SkillType? = null,
    val requiredLevel: Int = 0,
    val closenessDelta: Int = 0,
    val trustDelta: Int = 0,
    val cashDelta: Long = 0L,
    val statDelta: StatDelta = StatDelta(),
    val responseSummary: String = "",
    val spawnsArcKey: String? = null,
    val cascadeDelayYears: Int = 2,
    val heatDelta: Int = 0
)

@JsonClass(generateAdapter = true)
data class NpcMessage(
    val id: String = "",
    val npcId: String = "",
    val senderName: String = "",
    val senderRole: String = "",
    val ageSent: Int = 0,
    val subject: String = "",
    val body: String = "",
    val isRead: Boolean = false,
    val requiresReply: Boolean = true,
    val resolvedReplyText: String? = null,
    val replyOptions: List<MessageReply> = emptyList()
)

@JsonClass(generateAdapter = true)
data class Stats(
    val health: Int = 80,
    val happiness: Int = 75,
    val intellect: Int = 60,
    val charisma: Int = 55,
    val discipline: Int = 50,
    val stress: Int = 10,
    val ethics: Int = 65,
    val fame: Int = 0
) {
    fun clamp(): Stats = copy(
        health = health.coerceIn(0, 100),
        happiness = happiness.coerceIn(0, 100),
        intellect = intellect.coerceIn(0, 100),
        charisma = charisma.coerceIn(0, 100),
        discipline = discipline.coerceIn(0, 100),
        stress = stress.coerceIn(0, 100),
        ethics = ethics.coerceIn(0, 100),
        fame = fame.coerceIn(0, 100)
    )
}

@JsonClass(generateAdapter = true)
data class SkillProgress(
    val type: SkillType,
    val xp: Int = 0
) {
    val level: Int
        get() = (1 + (xp / 100)).coerceIn(1, 10)

    val levelProgress: Float
        get() = if (level >= 10) 1f else (xp % 100) / 100f
}

@JsonClass(generateAdapter = true)
data class JobInfo(
    val id: String,
    val title: String,
    val track: CareerTrack,
    val tier: Int,
    val salary: Long,
    val stressLoad: Int,
    val performance: Int = 55,
    val yearsInRole: Int = 0
)

@JsonClass(generateAdapter = true)
data class BusinessVenture(
    val id: String,
    val name: String,
    val sector: BusinessSector,
    val equityPercent: Int = 100,
    val valuation: Long,
    val annualRevenue: Long,
    val annualProfit: Long,
    val productQuality: Int = 50,
    val marketingLevel: Int = 35,
    val morale: Int = 75,
    val ageYears: Int = 0,
    val isPublic: Boolean = false
)

@JsonClass(generateAdapter = true)
data class MarketAsset(
    val ticker: String,
    val name: String,
    val sector: String,
    val isCrypto: Boolean,
    val currentPrice: Double,
    val previousPrice: Double,
    val history: List<Double>,
    val dividendYield: Double,
    val sharesOwned: Double = 0.0,
    val avgCostBasis: Double = 0.0
) {
    val positionValue: Long
        get() = (sharesOwned * currentPrice).roundToLong()

    val dayChangePercent: Double
        get() = if (previousPrice > 0.0) ((currentPrice - previousPrice) / previousPrice) * 100.0 else 0.0
}

@JsonClass(generateAdapter = true)
data class RealEstateAsset(
    val id: String,
    val name: String,
    val type: PropertyType,
    val purchasePrice: Long,
    val currentValue: Long,
    val annualRent: Long,
    val mortgageRemaining: Long,
    val isRentedOut: Boolean = false,
    val condition: Int = 92
)

@JsonClass(generateAdapter = true)
data class NpcRelation(
    val id: String,
    val name: String,
    val role: RelationRole,
    val age: Int,
    val closeness: Int,
    val trust: Int,
    val archetype: NpcArchetype,
    val occupation: String,
    val netWorth: Long,
    val isAlive: Boolean = true,
    val yearsKnown: Int = 1,
    val lastConversationSummary: String = "",
    val isRoyal: Boolean = false
)

@JsonClass(generateAdapter = true)
data class PrisonState(
    val isInPrison: Boolean = false,
    val yearsRemaining: Int = 0,
    val totalSentence: Int = 0,
    val crimeName: String = "",
    val notoriety: Int = 0,
    val securityLevel: PrisonSecurity = PrisonSecurity.MINIMUM,
    val respect: Int = 35,
    val gang: PrisonGang = PrisonGang.NONE,
    val lawStudyProgress: Int = 0,
    val escapeAttempts: Int = 0,
    val isLifeSentence: Boolean = false,
    val isDeathRow: Boolean = false,
    val yearsUntilExecution: Int = 0
)

@JsonClass(generateAdapter = true)
data class ChronicleEntry(
    val id: String,
    val age: Int,
    val year: Int,
    val title: String,
    val narrative: String,
    val category: ChronicleCategory,
    val tags: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class StatDelta(
    val health: Int = 0,
    val happiness: Int = 0,
    val intellect: Int = 0,
    val charisma: Int = 0,
    val discipline: Int = 0,
    val stress: Int = 0,
    val ethics: Int = 0,
    val fame: Int = 0
)

@JsonClass(generateAdapter = true)
data class EventChoice(
    val label: String,
    val hintTag: String,
    val requiredSkill: SkillType? = null,
    val requiredLevel: Int = 0,
    val successNarrative: String,
    val failNarrative: String = "",
    val statDelta: StatDelta = StatDelta(),
    val failStatDelta: StatDelta = StatDelta(),
    val cashDelta: Long = 0L,
    val failCashDelta: Long = 0L,
    val skillGainType: SkillType? = null,
    val skillGainXp: Int = 0,
    val traitGained: String? = null,
    val heatDelta: Int = 0,
    val cascadeArcKey: String? = null,
    val cascadeDelayYears: Int = 2,
    val cascadeNote: String? = null,
    val npcClosenessDelta: Int = 0,
    val npcTrustDelta: Int = 0,
    val statusApplied: StatusEffect? = null
)

@JsonClass(generateAdapter = true)
data class LifeEvent(
    val id: String,
    val title: String,
    val description: String,
    val category: ChronicleCategory,
    val choices: List<EventChoice>,
    val cascadeSourceNote: String? = null
)

@JsonClass(generateAdapter = true)
data class CountryProfile(
    val name: String,
    val taxRate: Double,
    val costOfLiving: Long,
    val healthcareScore: Int,
    val currencySymbol: String = "$",
    val regimeType: RegimeType = RegimeType.REPUBLIC
)

@JsonClass(generateAdapter = true)
data class LifeState(
    val id: String,
    val dynastyId: String,
    val generation: Int = 1,
    val fullName: String,
    val gender: Gender,
    val country: CountryProfile,
    val originPreset: OriginPreset,
    val age: Int = 0,
    val birthYear: Int = 2004,
    val currentYear: Int = 2004,
    val cash: Long = 0L,
    val debt: Long = 0L,
    val stats: Stats = Stats(),
    val skills: Map<SkillType, SkillProgress> = SkillType.entries.associateWith { SkillProgress(it, 0) },
    val traits: List<String> = emptyList(),
    val educationLevel: EducationLevel = EducationLevel.NONE,
    val degreeMajor: DegreeMajor? = null,
    val collegeYearsCompleted: Int = 0,
    val gpa: Double = 3.2,
    val currentJob: JobInfo? = null,
    val sideHustles: List<String> = emptyList(),
    val business: BusinessVenture? = null,
    val marketAssets: List<MarketAsset> = emptyList(),
    val realEstate: List<RealEstateAsset> = emptyList(),
    val relations: List<NpcRelation> = emptyList(),
    val chronicle: List<ChronicleEntry> = emptyList(),
    val pendingEvent: LifeEvent? = null,
    val prison: PrisonState = PrisonState(),
    val economicPhase: EconomicPhase = EconomicPhase.EXPANSION,
    val heatLevel: Int = 0,
    val actionsUsedThisYear: Int = 0,
    val isAlive: Boolean = true,
    val causeOfDeath: String? = null,
    val legacyTitle: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val lifestyle: LifestyleTier = LifestyleTier.MODEST,
    val schoolClubs: List<SchoolClub> = emptyList(),
    val collectibles: List<LuxuryItem> = emptyList(),
    val statusEffects: List<StatusEffect> = emptyList(),
    val storyFlags: Set<String> = emptySet(),
    val pendingArcs: List<DelayedArc> = emptyList(),
    val inbox: List<NpcMessage> = emptyList(),
    val lastCascadeSummary: String? = null,
    val royal: RoyalState = RoyalState(),
    val government: GovernmentState = GovernmentState()
) {
    val portfolioValue: Long
        get() = marketAssets.sumOf { it.positionValue }

    val realEstateEquity: Long
        get() = realEstate.sumOf { (it.currentValue - it.mortgageRemaining).coerceAtLeast(0L) }

    val collectiblesValue: Long
        get() = collectibles.sumOf { it.price }

    val businessEquityValue: Long
        get() = business?.let { ((it.valuation * it.equityPercent) / 100.0).roundToLong() } ?: 0L

    val netWorth: Long
        get() = cash + portfolioValue + realEstateEquity + collectiblesValue + businessEquityValue - debt

    val unreadInboxCount: Int
        get() = inbox.count { !it.isRead || (it.requiresReply && it.resolvedReplyText == null) }

    fun hasLaw(type: LawType): Boolean = government.activeLaws.any { it.type == type }

    fun effectiveTaxRate(): Double {
        var rate = country.taxRate
        government.activeLaws.forEach { law ->
            when (law.type) {
                LawType.WEALTH_SURTAX -> if (netWorth >= 1_000_000L) rate += law.type.taxDelta
                else -> rate += law.type.taxDelta
            }
        }
        return rate.coerceIn(0.02, 0.68)
    }

    fun skillLevel(type: SkillType): Int = skills[type]?.level ?: 1
}
