package com.example.engine

import com.example.data.model.*
import kotlin.random.Random

object LifeCatalog {
    val countries = listOf(
        CountryProfile("United States", taxRate = 0.24, costOfLiving = 18_500L, healthcareScore = 76, regimeType = RegimeType.REPUBLIC),
        CountryProfile("United Kingdom", taxRate = 0.26, costOfLiving = 19_500L, healthcareScore = 85, regimeType = RegimeType.CONSTITUTIONAL_MONARCHY),
        CountryProfile("Aurelia Crownlands", taxRate = 0.14, costOfLiving = 25_000L, healthcareScore = 92, regimeType = RegimeType.ABSOLUTE_CROWN),
        CountryProfile("Switzerland", taxRate = 0.19, costOfLiving = 28_000L, healthcareScore = 94, regimeType = RegimeType.REPUBLIC),
        CountryProfile("Japan", taxRate = 0.23, costOfLiving = 16_000L, healthcareScore = 92, regimeType = RegimeType.CONSTITUTIONAL_MONARCHY),
        CountryProfile("Singapore", taxRate = 0.14, costOfLiving = 22_000L, healthcareScore = 93, regimeType = RegimeType.REPUBLIC),
        CountryProfile("Germany", taxRate = 0.29, costOfLiving = 17_500L, healthcareScore = 89, regimeType = RegimeType.REPUBLIC),
        CountryProfile("Canada", taxRate = 0.25, costOfLiving = 17_800L, healthcareScore = 87, regimeType = RegimeType.CONSTITUTIONAL_MONARCHY),
        CountryProfile("Norway", taxRate = 0.31, costOfLiving = 24_000L, healthcareScore = 95, regimeType = RegimeType.CONSTITUTIONAL_MONARCHY),
        CountryProfile("Brazil", taxRate = 0.20, costOfLiving = 8_500L, healthcareScore = 66, regimeType = RegimeType.REPUBLIC),
        CountryProfile("Australia", taxRate = 0.25, costOfLiving = 19_000L, healthcareScore = 90, regimeType = RegimeType.CONSTITUTIONAL_MONARCHY)
    )

    val rulerPool = listOf(
        "Elena Rostova", "Arthur Pendelton", "Kenji Takahashi", "Viktor Vane",
        "Camille Moreau", "Henrik Solberg", "Isabella Castellano", "Marcus Thorne"
    )

    val monarchPool = listOf(
        "King Alistair IV", "Queen Victoria IX", "Emperor Haruto", "King Harald VI",
        "Queen Beatrix III", "Sovereign Aurelius II"
    )

    private val femaleFirstNames = listOf(
        "Elena", "Aria", "Maya", "Sora", "Clara", "Nadia", "Freya", "Zoe", "Camille", "Iris",
        "Naomi", "Vera", "Yuna", "Talia", "Astrid", "Mila", "Linnea", "Rhea"
    )
    private val maleFirstNames = listOf(
        "Julian", "Marcus", "Soren", "Kenji", "Rafael", "Arthur", "Dante", "Theo", "Liam", "Cyrus",
        "Matteo", "Lucas", "Victor", "Adrian", "Roman", "Devon", "Jonas", "Ezra"
    )
    private val nbFirstNames = listOf(
        "Rowan", "Sage", "Quinn", "Avery", "Eden", "Kai", "Morgan", "River", "Ellis", "Shiloh"
    )
    val lastNames = listOf(
        "Vance", "Mercer", "Kurosawa", "Lindqvist", "Sterling", "Moreau", "Alvarez", "Thorne",
        "Halloway", "Chen", "Kovacs", "Castellano", "Bauer", "Okafor", "Sinclair", "Novak"
    )

    fun randomFirstName(gender: Gender, rng: Random = Random): String = when (gender) {
        Gender.FEMALE -> femaleFirstNames[rng.nextInt(femaleFirstNames.size)]
        Gender.MALE -> maleFirstNames[rng.nextInt(maleFirstNames.size)]
        Gender.NON_BINARY -> nbFirstNames[rng.nextInt(nbFirstNames.size)]
    }

    fun randomFullName(gender: Gender, surname: String? = null, rng: Random = Random): String {
        val last = surname ?: lastNames[rng.nextInt(lastNames.size)]
        return "${randomFirstName(gender, rng)} $last"
    }

    data class CareerListing(
        val id: String,
        val title: String,
        val track: CareerTrack,
        val tier: Int,
        val baseSalary: Long,
        val stressLoad: Int,
        val minEducation: EducationLevel,
        val preferredMajor: DegreeMajor? = null,
        val requiredSkill: SkillType? = null,
        val requiredSkillLevel: Int = 1,
        val minIntellect: Int = 0
    )

    val careerListings = listOf(
        // Tech
        CareerListing("tech_1", "Junior QA Tester", CareerTrack.TECH, 1, 46_000L, 18, EducationLevel.HIGH_SCHOOL, null, SkillType.ENGINEERING, 1, 40),
        CareerListing("tech_2", "Software Engineer", CareerTrack.TECH, 2, 92_000L, 26, EducationLevel.UNIVERSITY, DegreeMajor.COMPUTER_SCIENCE, SkillType.ENGINEERING, 2, 55),
        CareerListing("tech_3", "Senior Systems Architect", CareerTrack.TECH, 3, 148_000L, 32, EducationLevel.UNIVERSITY, DegreeMajor.COMPUTER_SCIENCE, SkillType.ENGINEERING, 4, 68),
        CareerListing("tech_4", "Principal AI Scientist", CareerTrack.TECH, 4, 235_000L, 38, EducationLevel.DOCTORATE, DegreeMajor.COMPUTER_SCIENCE, SkillType.ENGINEERING, 6, 80),
        CareerListing("tech_5", "Chief Technology Officer", CareerTrack.TECH, 5, 410_000L, 48, EducationLevel.UNIVERSITY, null, SkillType.ENGINEERING, 8, 82),

        // Corporate
        CareerListing("corp_1", "Operations Coordinator", CareerTrack.CORPORATE, 1, 42_000L, 20, EducationLevel.HIGH_SCHOOL, null, SkillType.RHETORIC, 1, 35),
        CareerListing("corp_2", "Financial Analyst", CareerTrack.CORPORATE, 2, 84_000L, 28, EducationLevel.UNIVERSITY, DegreeMajor.FINANCE, SkillType.FINANCE, 2, 55),
        CareerListing("corp_3", "Investment Banker", CareerTrack.CORPORATE, 3, 165_000L, 46, EducationLevel.UNIVERSITY, DegreeMajor.FINANCE, SkillType.FINANCE, 4, 65),
        CareerListing("corp_4", "VP of Strategy", CareerTrack.CORPORATE, 4, 265_000L, 44, EducationLevel.DOCTORATE, null, SkillType.RHETORIC, 6, 72),
        CareerListing("corp_5", "Fortune 500 CEO", CareerTrack.CORPORATE, 5, 580_000L, 55, EducationLevel.UNIVERSITY, null, SkillType.RHETORIC, 8, 78),

        // Medical
        CareerListing("med_1", "EMT Paramedic", CareerTrack.MEDICAL, 1, 48_000L, 32, EducationLevel.HIGH_SCHOOL, null, SkillType.ATHLETICS, 1, 45),
        CareerListing("med_2", "Clinical Resident", CareerTrack.MEDICAL, 2, 74_000L, 42, EducationLevel.DOCTORATE, DegreeMajor.MEDICINE, SkillType.ATHLETICS, 2, 70),
        CareerListing("med_3", "Attending Physician", CareerTrack.MEDICAL, 3, 210_000L, 38, EducationLevel.DOCTORATE, DegreeMajor.MEDICINE, SkillType.ATHLETICS, 4, 76),
        CareerListing("med_4", "Neurosurgeon", CareerTrack.MEDICAL, 4, 390_000L, 50, EducationLevel.DOCTORATE, DegreeMajor.MEDICINE, SkillType.ENGINEERING, 6, 85),

        // Legal
        CareerListing("law_1", "Paralegal Clerk", CareerTrack.LEGAL, 1, 45_000L, 22, EducationLevel.HIGH_SCHOOL, null, SkillType.RHETORIC, 1, 45),
        CareerListing("law_2", "Defense Attorney", CareerTrack.LEGAL, 2, 118_000L, 35, EducationLevel.DOCTORATE, DegreeMajor.LAW, SkillType.RHETORIC, 3, 68),
        CareerListing("law_3", "Corporate Partner", CareerTrack.LEGAL, 3, 275_000L, 44, EducationLevel.DOCTORATE, DegreeMajor.LAW, SkillType.RHETORIC, 6, 76),
        CareerListing("law_4", "Appellate Justice", CareerTrack.LEGAL, 4, 310_000L, 30, EducationLevel.DOCTORATE, DegreeMajor.LAW, SkillType.RHETORIC, 8, 84),

        // Creative
        CareerListing("art_1", "Indie Illustrator", CareerTrack.CREATIVE, 1, 34_000L, 16, EducationLevel.NONE, null, SkillType.ARTS, 1, 30),
        CareerListing("art_2", "Art Director", CareerTrack.CREATIVE, 2, 82_000L, 25, EducationLevel.UNIVERSITY, DegreeMajor.FINE_ARTS, SkillType.ARTS, 3, 50),
        CareerListing("art_3", "Lead Game Director", CareerTrack.CREATIVE, 3, 160_000L, 36, EducationLevel.NONE, null, SkillType.ARTS, 5, 60),
        CareerListing("art_4", "Acclaimed Auteur", CareerTrack.CREATIVE, 4, 320_000L, 32, EducationLevel.NONE, null, SkillType.ARTS, 8, 65),

        // Politics
        CareerListing("pol_1", "Policy Aide", CareerTrack.POLITICS, 1, 44_000L, 24, EducationLevel.UNIVERSITY, DegreeMajor.POLITICAL_SCIENCE, SkillType.RHETORIC, 2, 55),
        CareerListing("pol_2", "City Council Member", CareerTrack.POLITICS, 2, 95_000L, 32, EducationLevel.UNIVERSITY, null, SkillType.RHETORIC, 4, 60),
        CareerListing("pol_3", "National Senator", CareerTrack.POLITICS, 3, 185_000L, 42, EducationLevel.UNIVERSITY, null, SkillType.RHETORIC, 6, 68),
        CareerListing("pol_4", "Head of State", CareerTrack.POLITICS, 4, 360_000L, 58, EducationLevel.UNIVERSITY, null, SkillType.RHETORIC, 9, 75),

        // Syndicate
        CareerListing("syn_1", "Street Lookout", CareerTrack.SYNDICATE, 1, 38_000L, 26, EducationLevel.NONE, null, SkillType.STREETCRAFT, 1, 20),
        CareerListing("syn_2", "Cipher Broker", CareerTrack.SYNDICATE, 2, 96_000L, 36, EducationLevel.NONE, null, SkillType.STREETCRAFT, 3, 45),
        CareerListing("syn_3", "Syndicate Enforcer", CareerTrack.SYNDICATE, 3, 175_000L, 45, EducationLevel.NONE, null, SkillType.STREETCRAFT, 5, 50),
        CareerListing("syn_4", "Underworld Kingpin", CareerTrack.SYNDICATE, 4, 480_000L, 54, EducationLevel.NONE, null, SkillType.STREETCRAFT, 8, 65)
    )

    data class SideHustleDef(
        val id: String,
        val title: String,
        val annualBaseIncome: Long,
        val stressCost: Int,
        val skillBoost: SkillType
    )

    val sideHustles = listOf(
        SideHustleDef("indie_app", "Indie Mobile App", 18_000L, 8, SkillType.ENGINEERING),
        SideHustleDef("podcast", "Culture & Markets Podcast", 14_000L, 6, SkillType.RHETORIC),
        SideHustleDef("quant_bot", "Micro-Arbitrage Script", 24_000L, 9, SkillType.FINANCE),
        SideHustleDef("commission_art", "Bespoke Digital Commissions", 12_000L, 5, SkillType.ARTS),
        SideHustleDef("gray_resale", "Rare Collectible Flipping", 20_000L, 10, SkillType.STREETCRAFT)
    )

    fun initialMarketAssets(): List<MarketAsset> = listOf(
        MarketAsset(
            ticker = "VTX",
            name = "Vertex Global Index",
            sector = "Broad Market ETF",
            isCrypto = false,
            currentPrice = 142.50,
            previousPrice = 138.00,
            history = listOf(112.0, 118.4, 125.2, 121.0, 130.5, 138.0, 142.5),
            dividendYield = 0.022
        ),
        MarketAsset(
            ticker = "NRV",
            name = "NeuralWave Systems",
            sector = "Semiconductors & AI",
            isCrypto = false,
            currentPrice = 264.00,
            previousPrice = 248.00,
            history = listOf(130.0, 155.0, 190.0, 175.0, 218.0, 248.0, 264.0),
            dividendYield = 0.005
        ),
        MarketAsset(
            ticker = "SOLV",
            name = "SolvBio Therapeutics",
            sector = "Biotech",
            isCrypto = false,
            currentPrice = 89.20,
            previousPrice = 91.50,
            history = listOf(72.0, 79.0, 88.0, 95.0, 84.0, 91.5, 89.2),
            dividendYield = 0.012
        ),
        MarketAsset(
            ticker = "AUR",
            name = "Aurelia Holdings",
            sector = "Luxury & Real Assets",
            isCrypto = false,
            currentPrice = 184.00,
            previousPrice = 180.00,
            history = listOf(150.0, 156.0, 162.0, 170.0, 174.0, 180.0, 184.0),
            dividendYield = 0.031
        ),
        MarketAsset(
            ticker = "AET",
            name = "Aether Grid Energy",
            sector = "Clean Utilities",
            isCrypto = false,
            currentPrice = 64.40,
            previousPrice = 63.10,
            history = listOf(58.0, 60.0, 59.5, 61.2, 62.8, 63.1, 64.4),
            dividendYield = 0.041
        ),
        MarketAsset(
            ticker = "BTCX",
            name = "BitCore Ledger",
            sector = "Layer-1 Crypto",
            isCrypto = true,
            currentPrice = 48_500.0,
            previousPrice = 44_200.0,
            history = listOf(19_000.0, 28_000.0, 39_500.0, 31_000.0, 41_200.0, 44_200.0, 48_500.0),
            dividendYield = 0.0
        ),
        MarketAsset(
            ticker = "ETHR",
            name = "Etheria Protocol",
            sector = "Smart Contracts",
            isCrypto = true,
            currentPrice = 2_840.0,
            previousPrice = 2_690.0,
            history = listOf(1_250.0, 1_800.0, 2_400.0, 1_950.0, 2_510.0, 2_690.0, 2_840.0),
            dividendYield = 0.0
        ),
        MarketAsset(
            ticker = "NVAX",
            name = "NovaMeme Token",
            sector = "Speculative Altcoin",
            isCrypto = true,
            currentPrice = 14.80,
            previousPrice = 18.20,
            history = listOf(3.2, 9.5, 24.0, 11.4, 21.8, 18.2, 14.8),
            dividendYield = 0.0
        )
    )

    fun evaluateLegacyTitle(state: LifeState): String {
        val nw = state.netWorth
        return when {
            state.causeOfDeath?.contains("Executed", ignoreCase = true) == true -> "Executed Outlaw"
            state.royal.rank == RoyalRank.MONARCH -> "Sovereign Monarch of House ${state.royal.houseName.ifBlank { "Realm" }}"
            state.government.isPlayerPresident -> "Architect President of the Nation"
            state.royal.rank.ordinal >= RoyalRank.DUKE.ordinal -> "High Noble of the Crown"
            state.prison.notoriety >= 60 && nw >= 2_000_000L -> "Mythic Shadow Kingpin"
            state.currentJob?.track == CareerTrack.POLITICS && state.currentJob.tier >= 4 -> "Architect of the Republic"
            state.business?.isPublic == true && nw >= 10_000_000L -> "Titan of Industry"
            nw >= 25_000_000L -> "Sovereign Mogul"
            state.stats.fame >= 80 && state.stats.ethics >= 60 -> "Beloved Cultural Icon"
            state.stats.ethics >= 88 && state.relations.count { it.closeness >= 75 } >= 3 -> "Beloved Matriarch / Patriarch"
            state.stats.intellect >= 90 && state.educationLevel == EducationLevel.DOCTORATE -> "Luminary Polymath"
            state.heatLevel >= 50 || state.prison.totalSentence >= 5 -> "Notorious Outlaw"
            nw >= 1_000_000L -> "Self-Made Millionaire"
            state.age >= 90 -> "Resilient Centenarian"
            else -> "Storyteller of Quiet Days"
        }
    }
}
