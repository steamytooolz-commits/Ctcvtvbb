package com.example.engine

import com.example.data.model.*
import kotlin.random.Random

object EventEngine {
    fun pickEventForState(state: LifeState, rng: Random = Random): LifeEvent? {
        if (!state.isAlive) return null
        if (state.prison.isInPrison) {
            return prisonEvents[rng.nextInt(prisonEvents.size)]
        }

        val candidates = mutableListOf<LifeEvent>()
        when (state.age) {
            in 5..12 -> candidates += childhoodEvents
            in 13..17 -> candidates += teenEvents
            in 18..25 -> candidates += youngAdultEvents
            in 26..58 -> {
                candidates += careerMidlifeEvents
                if (state.business != null) candidates += founderEvents
                if (state.economicPhase == EconomicPhase.CRASH || state.economicPhase == EconomicPhase.RECESSION) {
                    candidates += downturnEvents
                }
                if (state.heatLevel >= 25 || state.currentJob?.track == CareerTrack.SYNDICATE) {
                    candidates += underworldEvents
                }
            }
            else -> if (state.age >= 59) {
                candidates += elderEvents
                if (state.netWorth >= 500_000L) candidates += wealthLegacyEvents
            }
        }

        if (state.age >= 20 && state.relations.any { it.isAlive }) {
            candidates += relationshipEvents
        }

        if (candidates.isEmpty()) return null
        // Avoid repeating the most recent chronicle title if possible
        val recentTitles = state.chronicle.take(6).map { it.title }.toSet()
        val fresh = candidates.filterNot { it.title in recentTitles }
        val pool = fresh.ifEmpty { candidates }
        return pool[rng.nextInt(pool.size)]
    }

    private val childhoodEvents = listOf(
        LifeEvent(
            id = "child_chess",
            title = "The Regional Logic Tournament",
            description = "Your school teacher notices you finishing math puzzles twice as fast as older kids and invites you to enter the regional logic championship.",
            category = ChronicleCategory.MILESTONE,
            choices = listOf(
                EventChoice(
                    label = "Train every evening and compete",
                    hintTag = "+Intellect, +Discipline, +Stress",
                    successNarrative = "You placed 1st in your bracket! Local newspapers snapped your photo beside the trophy.",
                    statDelta = StatDelta(intellect = 8, discipline = 7, stress = 6, happiness = 6),
                    skillGainType = SkillType.ENGINEERING,
                    skillGainXp = 45,
                    traitGained = "Early Prodigy"
                ),
                EventChoice(
                    label = "Join for fun, share snacks with rivals",
                    hintTag = "+Charisma, +Happiness",
                    successNarrative = "You finished mid-table, but walked away with three lifelong friends and zero burnout.",
                    statDelta = StatDelta(charisma = 7, happiness = 8, stress = -4),
                    skillGainType = SkillType.RHETORIC,
                    skillGainXp = 35
                ),
                EventChoice(
                    label = "Skip it and explore the creek instead",
                    hintTag = "+Health, -Stress",
                    successNarrative = "A sun-drenched afternoon skipping stones taught you more about peace than any trophy.",
                    statDelta = StatDelta(health = 6, happiness = 5, stress = -8),
                    skillGainType = SkillType.ATHLETICS,
                    skillGainXp = 30
                )
            )
        ),
        LifeEvent(
            id = "child_stash",
            title = "The Attic Lockbox",
            description = "While helping clean the attic, you discover a dusty tin box containing vintage collectible coins and a neighbor's lost watch.",
            category = ChronicleCategory.MILESTONE,
            choices = listOf(
                EventChoice(
                    label = "Return the watch to the neighbor",
                    hintTag = "+Ethics, +Karma Cash Reward",
                    successNarrative = "Mrs. Kensington teared up—the watch was her late husband's. She insisted on giving you a reward.",
                    statDelta = StatDelta(ethics = 12, happiness = 6),
                    cashDelta = 400L,
                    traitGained = "Honorable Heart"
                ),
                EventChoice(
                    label = "Haggle at the weekend flea market",
                    hintTag = "+Finance XP, +Cash, -Ethics",
                    successNarrative = "You negotiated a sharp price with an antique dealer and pocketed a tidy childhood fortune.",
                    statDelta = StatDelta(ethics = -8, charisma = 4),
                    cashDelta = 1_100L,
                    skillGainType = SkillType.FINANCE,
                    skillGainXp = 40,
                    heatDelta = 4
                )
            )
        )
    )

    private val teenEvents = listOf(
        LifeEvent(
            id = "teen_viral",
            title = "Midnight Garage Prototype",
            description = "You built a quirky web utility to swap study notes with classmates, and overnight it jumped to 12,000 users across neighboring schools.",
            category = ChronicleCategory.CAREER,
            choices = listOf(
                EventChoice(
                    label = "Monetize with ad banners & micro-tiers",
                    hintTag = "Finance/Engineering Check • Cash",
                    requiredSkill = SkillType.ENGINEERING,
                    requiredLevel = 2,
                    successNarrative = "Your payment integration scaled cleanly! Sponsors paid out before finals week.",
                    failNarrative = "The servers crashed under load and angry users mocked the broken checkout.",
                    statDelta = StatDelta(intellect = 6, fame = 10, happiness = 8),
                    failStatDelta = StatDelta(stress = 10, happiness = -5),
                    cashDelta = 4_800L,
                    failCashDelta = -250L,
                    skillGainType = SkillType.FINANCE,
                    skillGainXp = 55
                ),
                EventChoice(
                    label = "Open-source the code for college portfolios",
                    hintTag = "+Intellect, +Ethics, +Engineering XP",
                    successNarrative = "Senior engineers starred your repo, earning you an invitation to a summer tech fellowship.",
                    statDelta = StatDelta(intellect = 9, ethics = 6, fame = 5),
                    skillGainType = SkillType.ENGINEERING,
                    skillGainXp = 75,
                    traitGained = "Open Source Builder",
                    cascadeArcKey = "opensource_buyout",
                    cascadeDelayYears = 4,
                    cascadeNote = "Open-source repo may attract enterprise buyers years later"
                ),
                EventChoice(
                    label = "Shut it down before the principal notices",
                    hintTag = "-Stress, Safe",
                    successNarrative = "You pulled the plug quietly and focused on sleep and grades.",
                    statDelta = StatDelta(stress = -8, discipline = 4)
                )
            )
        ),
        LifeEvent(
            id = "teen_debate",
            title = "The Principal's Budget Cut",
            description = "The school board plans to cancel the arts & robotics budget to fund stadium renovations. Students are rallying outside.",
            category = ChronicleCategory.MILESTONE,
            choices = listOf(
                EventChoice(
                    label = "Deliver an impassioned speech to the board",
                    hintTag = "Rhetoric Lv.2 • +Fame, +Charisma",
                    requiredSkill = SkillType.RHETORIC,
                    requiredLevel = 2,
                    successNarrative = "Your speech went locally viral! The board compromised and preserved both programs.",
                    failNarrative = "Nerves got the best of you mid-sentence, though your courage earned quiet respect.",
                    statDelta = StatDelta(charisma = 10, fame = 12, happiness = 8),
                    failStatDelta = StatDelta(charisma = 3, stress = 8),
                    skillGainType = SkillType.RHETORIC,
                    skillGainXp = 65
                ),
                EventChoice(
                    label = "Organize an underground art auction",
                    hintTag = "+Arts XP, +Cash",
                    successNarrative = "Students sold posters and zines, raising enough to buy new studio gear independently.",
                    statDelta = StatDelta(happiness = 7, discipline = 5),
                    cashDelta = 1_200L,
                    skillGainType = SkillType.ARTS,
                    skillGainXp = 60
                )
            )
        )
    )

    private val youngAdultEvents = listOf(
        LifeEvent(
            id = "ya_dorm_pitch",
            title = "Two Roads After Midnight",
            description = "A sharp-eyed founder offers you 15% equity to drop everything and build their logistics app—right before your biggest university exam.",
            category = ChronicleCategory.CAREER,
            choices = listOf(
                EventChoice(
                    label = "Pull double duty: build weekends, ace exams",
                    hintTag = "+Engineering, +Discipline, +High Stress",
                    successNarrative = "Exhausting months paid off: you kept your GPA intact and earned a tidy early buyout.",
                    statDelta = StatDelta(intellect = 6, discipline = 10, stress = 16, health = -5),
                    cashDelta = 14_500L,
                    skillGainType = SkillType.ENGINEERING,
                    skillGainXp = 85
                ),
                EventChoice(
                    label = "Negotiate a paid contractor fee instead of equity",
                    hintTag = "Finance Lv.2 • Guaranteed Cash",
                    requiredSkill = SkillType.FINANCE,
                    requiredLevel = 2,
                    successNarrative = "Smart move—their startup folded a year later, but your invoice cleared in full!",
                    failNarrative = "They balked at your rate and hired someone cheaper, leaving you empty-handed.",
                    statDelta = StatDelta(intellect = 4, happiness = 6),
                    failStatDelta = StatDelta(happiness = -4),
                    cashDelta = 8_500L,
                    skillGainType = SkillType.FINANCE,
                    skillGainXp = 55
                ),
                EventChoice(
                    label = "Decline and invest in lifelong friendships",
                    hintTag = "+Happiness, -Stress, +Charisma",
                    successNarrative = "You spent the semester on road trips and late-night diner talks you'll never forget.",
                    statDelta = StatDelta(happiness = 12, charisma = 7, stress = -10)
                )
            )
        ),
        LifeEvent(
            id = "ya_crypto_tip",
            title = "The Insider Alpha Group",
            description = "A well-connected acquaintance invites you into a private chat promising early allocations on a pre-launch token.",
            category = ChronicleCategory.FINANCE,
            choices = listOf(
                EventChoice(
                    label = "Audit the smart contract first",
                    hintTag = "Engineering Lv.3 • Avoid Rugpull",
                    requiredSkill = SkillType.ENGINEERING,
                    requiredLevel = 3,
                    successNarrative = "You spotted a hidden mint backdoor, shorted the hype, and warned your circle!",
                    failNarrative = "You couldn't decipher the obfuscated bytecode and walked away confused.",
                    statDelta = StatDelta(intellect = 7, ethics = 6, happiness = 7),
                    cashDelta = 16_000L,
                    skillGainType = SkillType.FINANCE,
                    skillGainXp = 60
                ),
                EventChoice(
                    label = "Throw $5,000 in without blinking",
                    hintTag = "High Risk Gamble",
                    requiredSkill = SkillType.STREETCRAFT,
                    requiredLevel = 2,
                    successNarrative = "You exited 30 seconds before the whales dumped—doubling your stake cleanly!",
                    failNarrative = "The liquidity pool vanished overnight. Expensive lesson in hype.",
                    statDelta = StatDelta(happiness = 10, stress = 8),
                    failStatDelta = StatDelta(happiness = -12, stress = 14),
                    cashDelta = 9_500L,
                    failCashDelta = -5_000L
                ),
                EventChoice(
                    label = "Mute the chat and buy broad index funds",
                    hintTag = "+Discipline, +Ethics",
                    successNarrative = "Boring compounds faster than panic. Your future self nods approvingly.",
                    statDelta = StatDelta(discipline = 6, stress = -4, ethics = 4),
                    skillGainType = SkillType.FINANCE,
                    skillGainXp = 35
                )
            )
        )
    )

    private val careerMidlifeEvents = listOf(
        LifeEvent(
            id = "mid_whistleblower",
            title = "The Redacted Ledger",
            description = "Late at the office, you uncover an encrypted spreadsheet showing senior leadership concealing massive safety liabilities.",
            category = ChronicleCategory.CAREER,
            choices = listOf(
                EventChoice(
                    label = "Leak verified docs to regulators & press",
                    hintTag = "+Ethics, +Fame, Job Risk",
                    successNarrative = "Regulators intervened. You became an industry symbol of integrity and landed consulting offers.",
                    statDelta = StatDelta(ethics = 18, fame = 16, stress = 14),
                    cashDelta = 22_000L,
                    traitGained = "Iron Conscience",
                    cascadeArcKey = "whistleblower_retaliation",
                    cascadeDelayYears = 2,
                    cascadeNote = "Corporate PR retaliation may strike within 2 years"
                ),
                EventChoice(
                    label = "Leverage the file for a quiet promotion",
                    hintTag = "Rhetoric Lv.4 • -Ethics, ++Cash",
                    requiredSkill = SkillType.RHETORIC,
                    requiredLevel = 4,
                    successNarrative = "The board panicked and promoted you with a massive retention bonus to 'own compliance'.",
                    failNarrative = "They called your bluff, froze your access, and slapped you with legal threats.",
                    statDelta = StatDelta(ethics = -18, stress = 10),
                    failStatDelta = StatDelta(ethics = -12, stress = 18, happiness = -10),
                    cashDelta = 65_000L,
                    failCashDelta = -12_000L,
                    heatDelta = 12,
                    cascadeArcKey = "blackmail_blowback",
                    cascadeDelayYears = 3,
                    cascadeNote = "Future board audits could uncover your hush bonus"
                ),
                EventChoice(
                    label = "Delete the cache and transfer teams",
                    hintTag = "Safe, -Stress",
                    successNarrative = "You quietly moved to a clean division and kept your peace of mind.",
                    statDelta = StatDelta(stress = -6, ethics = -4)
                )
            )
        ),
        LifeEvent(
            id = "mid_burnout",
            title = "The 3 AM Crossroads",
            description = "Your calendar has been solid red for six months. Your doctor warns your cortisol is spiking, just as a coveted keynote invitation arrives.",
            category = ChronicleCategory.HEALTH,
            choices = listOf(
                EventChoice(
                    label = "Deliver the keynote, then book a silent retreat",
                    hintTag = "Athletics Lv.3 • Balance",
                    requiredSkill = SkillType.ATHLETICS,
                    requiredLevel = 3,
                    successNarrative = "Your stamina carried you through a standing-ovation talk, and the retreat reset your vitals.",
                    failNarrative = "You powered through the keynote, but caught a nasty flu right after.",
                    statDelta = StatDelta(fame = 10, health = 8, stress = -14, happiness = 10),
                    failStatDelta = StatDelta(fame = 6, health = -10, stress = 4),
                    cashDelta = -4_500L
                ),
                EventChoice(
                    label = "Cancel the keynote and prioritize recovery",
                    hintTag = "++Health, --Stress",
                    successNarrative = "Four weeks of sleep, trail runs, and home cooking restored your spark.",
                    statDelta = StatDelta(health = 16, stress = -22, happiness = 9),
                    skillGainType = SkillType.ATHLETICS,
                    skillGainXp = 45
                ),
                EventChoice(
                    label = "Double down on espresso and take two clients",
                    hintTag = "++Cash, --Health, ++Stress",
                    successNarrative = "Your bank account swelled, though your eye hasn't stopped twitching.",
                    statDelta = StatDelta(health = -14, stress = 18, discipline = 5),
                    cashDelta = 28_000L
                )
            )
        )
    )

    private val founderEvents = listOf(
        LifeEvent(
            id = "biz_acquirer",
            title = "Term Sheet at Dawn",
            description = "A rival conglomerate wants to license your core IP—or lure away your lead engineers if you refuse.",
            category = ChronicleCategory.FINANCE,
            choices = listOf(
                EventChoice(
                    label = "Counter with an aggressive cross-licensing deal",
                    hintTag = "Rhetoric Lv.4 • Huge Payout",
                    requiredSkill = SkillType.RHETORIC,
                    requiredLevel = 4,
                    successNarrative = "They blinked first! You locked in recurring royalties without surrendering control.",
                    failNarrative = "Talks collapsed and you had to raise employee retention bonuses.",
                    statDelta = StatDelta(charisma = 8, fame = 8, happiness = 9),
                    failStatDelta = StatDelta(stress = 12, happiness = -5),
                    cashDelta = 110_000L,
                    failCashDelta = -25_000L,
                    skillGainType = SkillType.FINANCE,
                    skillGainXp = 70
                ),
                EventChoice(
                    label = "Share equity bonuses with your team to lock loyalty",
                    hintTag = "+Ethics, +Team Morale",
                    successNarrative = "Your engineers rejected the rival's poaching attempts and shipped a breakthrough release.",
                    statDelta = StatDelta(ethics = 10, happiness = 8, stress = -5),
                    cashDelta = -18_000L
                )
            )
        )
    )

    private val downturnEvents = listOf(
        LifeEvent(
            id = "econ_crash_opp",
            title = "Blood in the Streets",
            description = "Markets are sliding into panic. Distressed assets and panicked sellers are liquidating quality holdings at steep discounts.",
            category = ChronicleCategory.FINANCE,
            choices = listOf(
                EventChoice(
                    label = "Deploy cash reserves into distressed bargains",
                    hintTag = "Finance Lv.3 • Contrarian Bet",
                    requiredSkill = SkillType.FINANCE,
                    requiredLevel = 3,
                    successNarrative = "You snapped up undervalued assets near the bottom—positioning yourself for the rebound.",
                    failNarrative = "You caught a falling knife too early and watched prices dip further.",
                    statDelta = StatDelta(intellect = 6, discipline = 7),
                    failStatDelta = StatDelta(stress = 12),
                    cashDelta = 38_000L,
                    failCashDelta = -15_000L,
                    skillGainType = SkillType.FINANCE,
                    skillGainXp = 65
                ),
                EventChoice(
                    label = "Hoard liquidity and trim personal expenses",
                    hintTag = "-Stress, +Discipline",
                    successNarrative = "Cash is oxygen in a storm. You slept peacefully while others panicked.",
                    statDelta = StatDelta(discipline = 8, stress = -10)
                )
            )
        )
    )

    private val underworldEvents = listOf(
        LifeEvent(
            id = "uw_vault",
            title = "The Blueprints in the Courier Bag",
            description = "An underworld broker slips you schematics for an unguarded luxury vault—and asks for a clean 50/50 split.",
            category = ChronicleCategory.CRIME,
            choices = listOf(
                EventChoice(
                    label = "Execute a ghost-clean digital bypass",
                    hintTag = "Streetcraft Lv.4 • Huge Haul",
                    requiredSkill = SkillType.STREETCRAFT,
                    requiredLevel = 4,
                    successNarrative = "No alarms, no witnesses. You vanished into the night with untraceable bearer bonds.",
                    failNarrative = "Silent sensors tripped! You escaped by a hair, burning half your gear and spiking Heat.",
                    statDelta = StatDelta(ethics = -14, stress = 12, happiness = 8),
                    failStatDelta = StatDelta(health = -10, stress = 22, ethics = -14),
                    cashDelta = 145_000L,
                    failCashDelta = -8_000L,
                    skillGainType = SkillType.STREETCRAFT,
                    skillGainXp = 90,
                    heatDelta = 18,
                    cascadeArcKey = "syndicate_collection",
                    cascadeDelayYears = 3,
                    cascadeNote = "Syndicate broker may come collecting in 3 years"
                ),
                EventChoice(
                    label = "Tip off authorities anonymously to wipe Heat",
                    hintTag = "+Ethics, --Heat",
                    successNarrative = "Detectives busted the broker's crew, and your name quietly dropped off their watchlists.",
                    statDelta = StatDelta(ethics = 14, stress = -12),
                    heatDelta = -25,
                    cascadeArcKey = "rival_vendetta",
                    cascadeDelayYears = 3,
                    cascadeNote = "Someone from that busted crew may suspect a rat"
                )
            )
        )
    )

    private val relationshipEvents = listOf(
        LifeEvent(
            id = "rel_feud",
            title = "An Unspoken Apology",
            description = "Someone close to you calls late at night after months of quiet distance, asking if you're willing to meet for coffee.",
            category = ChronicleCategory.LOVE,
            choices = listOf(
                EventChoice(
                    label = "Listen openly without keeping score",
                    hintTag = "+Happiness, +Charisma, +Ethics",
                    successNarrative = "An hour of honest conversation melted years of pride. You left feeling lighter.",
                    statDelta = StatDelta(happiness = 12, charisma = 6, ethics = 7, stress = -10),
                    skillGainType = SkillType.RHETORIC,
                    skillGainXp = 35
                ),
                EventChoice(
                    label = "Send a thoughtful gift, keep polite distance",
                    hintTag = "Balanced",
                    successNarrative = "Boundaries stayed intact while warmth remained.",
                    statDelta = StatDelta(stress = -3, happiness = 3),
                    cashDelta = -350L
                )
            )
        )
    )

    private val elderEvents = listOf(
        LifeEvent(
            id = "elder_memoir",
            title = "The Biographer's Tape Recorder",
            description = "A young journalist wants to chronicle your life's twists, triumphs, and regrets into a hardcover biography.",
            category = ChronicleCategory.LEGACY,
            choices = listOf(
                EventChoice(
                    label = "Tell the unvarnished truth—warts and all",
                    hintTag = "+Fame, +Happiness, +Ethics",
                    successNarrative = "Readers adored your candor. The book hit bestseller lists and cemented your story.",
                    statDelta = StatDelta(fame = 18, happiness = 12, ethics = 8),
                    cashDelta = 42_000L,
                    skillGainType = SkillType.ARTS,
                    skillGainXp = 50,
                    traitGained = "Candid Chronicler"
                ),
                EventChoice(
                    label = "Keep your secrets and write private letters to family",
                    hintTag = "+Happiness, -Stress",
                    successNarrative = "Some chapters belong only to blood and memory. Your heirs will treasure those pages.",
                    statDelta = StatDelta(happiness = 10, stress = -10, ethics = 6)
                )
            )
        )
    )

    private val wealthLegacyEvents = listOf(
        LifeEvent(
            id = "elder_endow",
            title = "The Bronze Plaque",
            description = "A local university offers to name a scholarship and innovation wing after your family surname.",
            category = ChronicleCategory.LEGACY,
            choices = listOf(
                EventChoice(
                    label = "Endow the scholarship fund ($120,000)",
                    hintTag = "++Fame, ++Ethics, Dynasty Honor",
                    successNarrative = "Dozens of first-generation scholars will walk through doors bearing your name.",
                    statDelta = StatDelta(fame = 22, ethics = 20, happiness = 14),
                    cashDelta = -120_000L,
                    traitGained = "Generational Patron"
                ),
                EventChoice(
                    label = "Keep capital compounding for your heirs",
                    hintTag = "+Finance XP",
                    successNarrative = "You politely declined and kept the family trust untouched.",
                    statDelta = StatDelta(discipline = 5),
                    skillGainType = SkillType.FINANCE,
                    skillGainXp = 40
                )
            )
        )
    )

    private val prisonEvents = listOf(
        LifeEvent(
            id = "prison_library",
            title = "The Cellblock Library",
            description = "Inside the facility, you have quiet hours between counts. How do you spend your energy behind the walls?",
            category = ChronicleCategory.CRIME,
            choices = listOf(
                EventChoice(
                    label = "Master constitutional law & financial theory",
                    hintTag = "+Intellect, +Finance XP, +Discipline",
                    successNarrative = "You read every volume in the law cart and drafted sharpest appeal briefs on the block.",
                    statDelta = StatDelta(intellect = 8, discipline = 10, stress = -6),
                    skillGainType = SkillType.FINANCE,
                    skillGainXp = 70
                ),
                EventChoice(
                    label = "Train calisthenics in the yard",
                    hintTag = "+Health, +Athletics XP",
                    successNarrative = "Iron routine kept your body sharp and earned quiet respect in the yard.",
                    statDelta = StatDelta(health = 10, stress = -8),
                    skillGainType = SkillType.ATHLETICS,
                    skillGainXp = 75
                ),
                EventChoice(
                    label = "Run the commissary barter network",
                    hintTag = "Streetcraft Lv.3 • +Cash, +Notoriety",
                    requiredSkill = SkillType.STREETCRAFT,
                    requiredLevel = 3,
                    successNarrative = "You cornered the coffee-pack trade and amassed hidden commissary profits.",
                    failNarrative = "Guards tossed your cell during a shakedown and tacked on solitary time.",
                    statDelta = StatDelta(charisma = 5, ethics = -8),
                    failStatDelta = StatDelta(health = -8, stress = 16, happiness = -10),
                    cashDelta = 8_500L,
                    skillGainType = SkillType.STREETCRAFT,
                    skillGainXp = 55
                )
            )
        )
    )
}
