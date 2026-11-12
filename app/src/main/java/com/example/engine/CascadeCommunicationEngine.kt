package com.example.engine

import com.example.data.model.*
import java.util.UUID
import kotlin.random.Random

object CascadeCommunicationEngine {

    fun buildCascadeEvent(arc: DelayedArc, state: LifeState): LifeEvent {
        return when (arc.arcKey) {
            "whistleblower_retaliation" -> LifeEvent(
                id = "cascade_whistleblower_${arc.stage}",
                title = "The Whistleblower's Echo",
                description = "Two years after you leaked the redacted ledger, a shadowy PR firm launches a smear campaign claiming you falsified data—while a federal prosecutor secretly offers you an advisory seat.",
                category = ChronicleCategory.CASCADE,
                cascadeSourceNote = "Triggered by: ${arc.originChoiceLabel}",
                choices = listOf(
                    EventChoice(
                        label = "Publish the cryptographic hash proofs live",
                        hintTag = "Engineering Lv.3 • Total Vindication",
                        requiredSkill = SkillType.ENGINEERING,
                        requiredLevel = 3,
                        successNarrative = "Your verification thread went mega-viral! The PR firm collapsed overnight and your integrity became legendary.",
                        failNarrative = "Technical jargon confused the public; the scandal lingered in headlines.",
                        statDelta = StatDelta(fame = 18, ethics = 12, happiness = 14, stress = -12),
                        failStatDelta = StatDelta(stress = 14, happiness = -8),
                        cashDelta = 45_000L,
                        traitGained = "Unbreakable Witness",
                        statusApplied = StatusEffect("viral_glow", "Vindicated Hero", "+Fame & trust across circles", 3, true)
                    ),
                    EventChoice(
                        label = "Accept a quiet non-disclosure settlement",
                        hintTag = "++Cash, -Ethics",
                        successNarrative = "Attorneys wired a massive hush settlement. Your reputation cooled, your bank surged.",
                        statDelta = StatDelta(ethics = -12, fame = -5, stress = -6),
                        cashDelta = 85_000L
                    )
                )
            )

            "blackmail_blowback" -> LifeEvent(
                id = "cascade_blackmail_${arc.stage}",
                title = "New Management's Audit",
                description = "An activist investor took over your old division and noticed the suspicious 'compliance bonus' you extracted years ago.",
                category = ChronicleCategory.CASCADE,
                cascadeSourceNote = "Triggered by: ${arc.originChoiceLabel}",
                choices = listOf(
                    EventChoice(
                        label = "Outmaneuver them in the board hearing",
                        hintTag = "Rhetoric Lv.5 • Keep Bonus",
                        requiredSkill = SkillType.RHETORIC,
                        requiredLevel = 5,
                        successNarrative = "You framed the bonus as a documented risk-mitigation retainer and walked away clean!",
                        failNarrative = "They clawed back half the payout and leaked an internal memo.",
                        statDelta = StatDelta(charisma = 6, stress = 8),
                        failStatDelta = StatDelta(fame = -6, stress = 18),
                        failCashDelta = -35_000L,
                        heatDelta = 10
                    ),
                    EventChoice(
                        label = "Resign cleanly and return a token fee",
                        hintTag = "-$15k, Wipe Heat",
                        successNarrative = "You settled quietly before regulators opened a docket.",
                        statDelta = StatDelta(stress = -10, ethics = 4),
                        cashDelta = -15_000L,
                        heatDelta = -15
                    )
                )
            )

            "opensource_buyout" -> LifeEvent(
                id = "cascade_opensource_${arc.stage}",
                title = "Your Teen Code Built an Empire",
                description = "Remember the open-source utility you published years ago? Three enterprise startups built billion-dollar pipelines on top of it—and one wants you on their founding board.",
                category = ChronicleCategory.CASCADE,
                cascadeSourceNote = "Triggered by: ${arc.originChoiceLabel}",
                choices = listOf(
                    EventChoice(
                        label = "Negotiate a Founding Advisor equity package",
                        hintTag = "Finance Lv.2 • Massive Payday",
                        requiredSkill = SkillType.FINANCE,
                        requiredLevel = 2,
                        successNarrative = "You structured preferred shares that vested immediately in a secondary sale!",
                        failNarrative = "You took standard warrants that diluted down, still netting a tidy stipend.",
                        statDelta = StatDelta(fame = 12, happiness = 14, intellect = 4),
                        cashDelta = 95_000L,
                        failCashDelta = 18_000L,
                        skillGainType = SkillType.FINANCE,
                        skillGainXp = 60
                    ),
                    EventChoice(
                        label = "Ask them to fund a free community lab instead",
                        hintTag = "++Ethics, +Charisma, +Fame",
                        successNarrative = "Tech press celebrated your generosity, sparking lifelong mentor invitations.",
                        statDelta = StatDelta(ethics = 16, fame = 10, charisma = 8, happiness = 12),
                        traitGained = "Community Architect"
                    )
                )
            )

            "loan_payoff_surge" -> LifeEvent(
                id = "cascade_loan_payoff_${arc.stage}",
                title = "An Unexpected Wire Transfer",
                description = "${arc.targetNpcName ?: "An old friend"} you helped back when money was tight just closed a breakout deal—and hasn't forgotten who backed them.",
                category = ChronicleCategory.CASCADE,
                cascadeSourceNote = "Triggered by: ${arc.originChoiceLabel}",
                choices = listOf(
                    EventChoice(
                        label = "Accept the 4x grateful repayment & toast together",
                        hintTag = "++Cash, +Happiness, +Relations",
                        successNarrative = "They insisted on paying back fourfold plus introducing you to their inner circle!",
                        statDelta = StatDelta(happiness = 15, charisma = 5, stress = -10),
                        cashDelta = 18_000L,
                        npcClosenessDelta = 15,
                        npcTrustDelta = 15
                    ),
                    EventChoice(
                        label = "Take only the principal—keep the favor in reserve",
                        hintTag = "+Ethics, Deep Loyalty",
                        successNarrative = "They swore lifelong loyalty. You gained an unshakeable ally.",
                        statDelta = StatDelta(ethics = 12, happiness = 10),
                        cashDelta = 4_000L,
                        npcClosenessDelta = 25,
                        npcTrustDelta = 25,
                        traitGained = "Trusted Kingmaker"
                    )
                )
            )

            "rival_vendetta" -> LifeEvent(
                id = "cascade_rival_${arc.stage}",
                title = "The Rival's Counterstrike",
                description = "A competitor you burned or outmaneuvered has teamed up with a disgruntled vendor to Poach your key contacts and tarnish your name.",
                category = ChronicleCategory.CASCADE,
                cascadeSourceNote = "Triggered by: ${arc.originChoiceLabel}",
                choices = listOf(
                    EventChoice(
                        label = "Bring receipts to a public summit",
                        hintTag = "Rhetoric Lv.4 • Crush Vendetta",
                        requiredSkill = SkillType.RHETORIC,
                        requiredLevel = 4,
                        successNarrative = "You dismantled their claims calmly with documented dates. Your rival lost credibility instantly.",
                        failNarrative = "The debate turned messy, splitting your circle's loyalties.",
                        statDelta = StatDelta(fame = 10, charisma = 8, stress = -6),
                        failStatDelta = StatDelta(stress = 16, happiness = -8),
                        skillGainType = SkillType.RHETORIC,
                        skillGainXp = 60
                    ),
                    EventChoice(
                        label = "Hire street fixers to dig up their dirt",
                        hintTag = "Streetcraft Lv.3 • -Ethics, +Heat",
                        requiredSkill = SkillType.STREETCRAFT,
                        requiredLevel = 3,
                        successNarrative = "One quiet envelope slipped under their door ended the feud permanently.",
                        failNarrative = "The fixer botched it and left traces pointing straight back to you!",
                        statDelta = StatDelta(ethics = -12, stress = -8),
                        failStatDelta = StatDelta(stress = 18, ethics = -10),
                        cashDelta = -8_000L,
                        heatDelta = 14
                    )
                )
            )

            "syndicate_collection" -> LifeEvent(
                id = "cascade_syndicate_${arc.stage}",
                title = "Unfinished Syndicate Business",
                description = "The underground broker from years ago resurfaces—either expecting a cut of your legitimate wealth or offering one last clean score.",
                category = ChronicleCategory.CASCADE,
                cascadeSourceNote = "Triggered by: ${arc.originChoiceLabel}",
                choices = listOf(
                    EventChoice(
                        label = "Pay a clean $20,000 buyout to burn the ledger",
                        hintTag = "-$20k, --Heat, Freedom",
                        successNarrative = "Your name was scrubbed from the syndicate books for good.",
                        statDelta = StatDelta(stress = -16, happiness = 6),
                        cashDelta = -20_000L,
                        heatDelta = -25
                    ),
                    EventChoice(
                        label = "Double-cross them to federal taskforce",
                        hintTag = "++Ethics, Risky Heat Drop",
                        successNarrative = "Taskforce agents swooped in during the handoff; you walked with witness protection immunity.",
                        statDelta = StatDelta(ethics = 14, stress = 10),
                        heatDelta = -40
                    )
                )
            )

            else -> LifeEvent(
                id = "cascade_serendipity_${arc.stage}",
                title = "Ripples of Past Decisions",
                description = "A seed you planted years ago (${arc.originChoiceLabel}) finally blooms into an unexpected opportunity.",
                category = ChronicleCategory.CASCADE,
                cascadeSourceNote = "Triggered by: ${arc.originChoiceLabel}",
                choices = listOf(
                    EventChoice(
                        label = "Lean in and seize the momentum",
                        hintTag = "+Cash, +Skill XP, +Joy",
                        successNarrative = "Timing met preparation—you turned a small past gesture into real momentum.",
                        statDelta = StatDelta(happiness = 10, charisma = 5),
                        cashDelta = 12_500L,
                        skillGainType = SkillType.FINANCE,
                        skillGainXp = 45
                    ),
                    EventChoice(
                        label = "Share the credit with someone close",
                        hintTag = "+Relations, +Ethics",
                        successNarrative = "Your generosity deepened loyalty across your inner circle.",
                        statDelta = StatDelta(ethics = 10, happiness = 8),
                        npcClosenessDelta = 12,
                        npcTrustDelta = 12
                    )
                )
            )
        }
    }

    fun generateAnnualNpcMessage(state: LifeState, rng: Random = Random): NpcMessage? {
        val living = state.relations.filter { it.isAlive }
        if (living.isEmpty() || state.age < 13) return null
        val npc = living[rng.nextInt(living.size)]

        return when (npc.role) {
            RelationRole.PARTNER, RelationRole.SPOUSE -> {
                if (state.heatLevel >= 40) {
                    NpcMessage(
                        id = UUID.randomUUID().toString(),
                        npcId = npc.id,
                        senderName = npc.name,
                        senderRole = npc.role.label,
                        ageSent = state.age,
                        subject = "Are you hiding something from me?",
                        body = "\"Late nights, burner calls, nervous glances... Tell me straight—what are you tangled up in?\"",
                        replyOptions = listOf(
                            MessageReply(
                                label = "Come clean and promise to scale back",
                                toneTag = "Honest • +Trust, -Heat",
                                closenessDelta = 6,
                                trustDelta = 14,
                                statDelta = StatDelta(ethics = 6, stress = -6),
                                heatDelta = -8,
                                responseSummary = "${npc.name} appreciated your candor, even if they're worried."
                            ),
                            MessageReply(
                                label = "Smoothly spin it as confidential work",
                                toneTag = "Rhetoric Lv.3 • Bluff",
                                requiredSkill = SkillType.RHETORIC,
                                requiredLevel = 3,
                                closenessDelta = 2,
                                trustDelta = -4,
                                statDelta = StatDelta(stress = 4),
                                responseSummary = "${npc.name} bought the story for now, though doubt lingers."
                            ),
                            MessageReply(
                                label = "Buy a lavish getaway to distract them",
                                toneTag = "-$3,500 • +Closeness",
                                closenessDelta = 12,
                                trustDelta = -6,
                                cashDelta = -3_500L,
                                statDelta = StatDelta(happiness = 6),
                                responseSummary = "Champagne smoothed the tension over—for this year."
                            )
                        )
                    )
                } else {
                    NpcMessage(
                        id = UUID.randomUUID().toString(),
                        npcId = npc.id,
                        senderName = npc.name,
                        senderRole = npc.role.label,
                        ageSent = state.age,
                        subject = "Thinking about our next chapter",
                        body = "\"We've both been running hard lately. Want to pitch in together on a dream weekend or a joint investment?\"",
                        replyOptions = listOf(
                            MessageReply(
                                label = "Plan a spontaneous trip together ($1,200)",
                                toneTag = "+Closeness, ++Joy",
                                closenessDelta = 14,
                                trustDelta = 10,
                                cashDelta = -1_200L,
                                statDelta = StatDelta(happiness = 12, stress = -10),
                                responseSummary = "The getaway recharged both of you deeply."
                            ),
                            MessageReply(
                                label = "Co-invest $5,000 in a joint venture",
                                toneTag = "Finance • Spawns Cascade",
                                closenessDelta = 8,
                                trustDelta = 10,
                                cashDelta = -5_000L,
                                spawnsArcKey = "loan_payoff_surge",
                                cascadeDelayYears = 2,
                                responseSummary = "You pooled capital together—watch how it pays off in 2 years."
                            )
                        )
                    )
                }
            }

            RelationRole.FRIEND, RelationRole.MENTOR, RelationRole.SIBLING -> {
                NpcMessage(
                    id = UUID.randomUUID().toString(),
                    npcId = npc.id,
                    senderName = npc.name,
                    senderRole = npc.role.label,
                    ageSent = state.age,
                    subject = "Urgent favor — can we talk?",
                    body = "\"Hey ${state.fullName.substringBefore(" ")}, I have a shot at launching a small venture, but I'm $4,000 short before Friday. Can you spot me?\"",
                    replyOptions = listOf(
                        MessageReply(
                            label = "Wire the $4,000 no strings attached",
                            toneTag = "-$4,000 • Cascade Payoff Later",
                            closenessDelta = 18,
                            trustDelta = 20,
                            cashDelta = -4_000L,
                            statDelta = StatDelta(ethics = 8, happiness = 5),
                            spawnsArcKey = "loan_payoff_surge",
                            cascadeDelayYears = 2,
                            responseSummary = "${npc.name} was blown away by your trust. Something big may come back around."
                        ),
                        MessageReply(
                            label = "Offer sharp strategic advice instead",
                            toneTag = "Rhetoric • Free, +Respect",
                            closenessDelta = 6,
                            trustDelta = 8,
                            statDelta = StatDelta(charisma = 3),
                            responseSummary = "You helped them pitch a local angel instead of risking your cash."
                        ),
                        MessageReply(
                            label = "Politely decline — focus on your own runway",
                            toneTag = "Guarded • -Closeness",
                            closenessDelta = -10,
                            trustDelta = -8,
                            responseSummary = "${npc.name} understood, though things felt cooler afterward."
                        )
                    )
                )
            }

            RelationRole.CHILD -> {
                NpcMessage(
                    id = UUID.randomUUID().toString(),
                    npcId = npc.id,
                    senderName = npc.name,
                    senderRole = npc.role.label,
                    ageSent = state.age,
                    subject = "A question about my path",
                    body = "\"Hey, everyone at school is chasing safe corporate tracks, but I really want to build something weird and creative. What would you do?\"",
                    replyOptions = listOf(
                        MessageReply(
                            label = "Back their bold dream & fund gear ($1,500)",
                            toneTag = "Supportive • ++Bond",
                            closenessDelta = 16,
                            trustDelta = 16,
                            cashDelta = -1_500L,
                            statDelta = StatDelta(happiness = 10, ethics = 6),
                            responseSummary = "${npc.name}'s eyes lit up—you gave them wings."
                        ),
                        MessageReply(
                            label = "Urge discipline and a pragmatic backup",
                            toneTag = "Pragmatic • +Discipline",
                            closenessDelta = 4,
                            trustDelta = 6,
                            statDelta = StatDelta(discipline = 4),
                            responseSummary = "${npc.name} rolled their eyes slightly, then nodded."
                        )
                    )
                )
            }

            else -> {
                NpcMessage(
                    id = UUID.randomUUID().toString(),
                    npcId = npc.id,
                    senderName = npc.name,
                    senderRole = npc.role.label,
                    ageSent = state.age,
                    subject = "Checking in on you",
                    body = "\"How are you holding up this year? Don't let work swallow your whole life.\"",
                    replyOptions = listOf(
                        MessageReply(
                            label = "Catch up warmly over coffee",
                            toneTag = "+Closeness, -Stress",
                            closenessDelta = 10,
                            trustDelta = 8,
                            statDelta = StatDelta(happiness = 6, stress = -5),
                            responseSummary = "A warm conversation lifted your mood."
                        ),
                        MessageReply(
                            label = "Send a quick thank-you note",
                            toneTag = "Brief • +Trust",
                            closenessDelta = 3,
                            trustDelta = 4,
                            responseSummary = "Short and sweet—staying on good terms."
                        )
                    )
                )
            }
        }
    }

    fun buildDirectConversationReplies(npc: NpcRelation, state: LifeState): List<MessageReply> {
        val list = mutableListOf<MessageReply>()
        list += MessageReply(
            label = "Confide your honest ambitions & fears",
            toneTag = "Deep Bond • +Trust",
            closenessDelta = 10,
            trustDelta = 12,
            statDelta = StatDelta(happiness = 6, stress = -6),
            responseSummary = "${npc.name} (${npc.archetype.label}) listened intently and opened up in return."
        )
        if (npc.trust >= 60 && state.age >= 18) {
            list += MessageReply(
                label = "Ask for an insider career or market intro",
                toneTag = "Trust 60+ • +Cash / +Intellect",
                closenessDelta = -2,
                trustDelta = 2,
                cashDelta = 2_500L,
                statDelta = StatDelta(intellect = 3, charisma = 3),
                responseSummary = "${npc.name} connected you with a lucrative freelance contact (+${formatCurrency(2_500L)})."
            )
        }
        if (npc.archetype == NpcArchetype.CALCULATING || npc.archetype == NpcArchetype.AMBITIOUS || state.skillLevel(SkillType.STREETCRAFT) >= 3) {
            list += MessageReply(
                label = "Propose a gray-market side deal",
                toneTag = "Streetcraft Lv.2 • High Risk",
                requiredSkill = SkillType.STREETCRAFT,
                requiredLevel = 2,
                closenessDelta = 6,
                trustDelta = -4,
                cashDelta = 9_000L,
                heatDelta = 8,
                spawnsArcKey = "rival_vendetta",
                cascadeDelayYears = 3,
                responseSummary = "You split a sharp backchannel deal (+${formatCurrency(9_000L)}), though rumors may surface later."
            )
        }
        list += MessageReply(
            label = "Challenge them on an old tension",
            toneTag = "Rhetoric Lv.3 • Resolve Conflict",
            requiredSkill = SkillType.RHETORIC,
            requiredLevel = 3,
            closenessDelta = 12,
            trustDelta = 14,
            statDelta = StatDelta(charisma = 4, stress = -5),
            responseSummary = "Clearing the air turned lingering friction into genuine respect."
        )
        return list
    }

    fun rollAnnualWildcard(state: LifeState, rng: Random = Random): Pair<LifeState, ChronicleEntry?> {
        if (state.age < 15 || state.prison.isInPrison) return state to null
        val roll = rng.nextDouble()
        if (roll > 0.24) return state to null

        return when (rng.nextInt(5)) {
            0 -> { // Market Short Squeeze or Dividend Surprise
                val invested = state.portfolioValue
                if (invested >= 5_000L) {
                    val windfall = (invested * 0.14).toLong().coerceAtMost(90_000L)
                    val entry = ChronicleEntry(
                        id = UUID.randomUUID().toString(),
                        age = state.age,
                        year = state.currentYear,
                        title = "Wildcard: Short Squeeze Windfall!",
                        narrative = "One of your holdings spiked on sudden institutional covering, paying out ${formatCurrency(windfall)}!",
                        category = ChronicleCategory.FINANCE,
                        tags = listOf("Wildcard", "+${formatCurrency(windfall)}")
                    )
                    state.copy(
                        cash = state.cash + windfall,
                        statusEffects = state.statusEffects + StatusEffect("mkt_oracle", "Market Momentum", "Sharp timing glow", 2, true)
                    ) to entry
                } else state to null
            }
            1 -> { // Headhunter Poach
                val job = state.currentJob
                if (job != null && job.performance >= 72) {
                    val bump = (job.salary * 1.18).toLong()
                    val entry = ChronicleEntry(
                        id = UUID.randomUUID().toString(),
                        age = state.age,
                        year = state.currentYear,
                        title = "Wildcard: Headhunter Poach Offer",
                        narrative = "A rival firm tried to poach you—forcing your employer to bump your salary to ${formatCurrency(bump)}/yr!",
                        category = ChronicleCategory.CAREER,
                        tags = listOf("Wildcard", "${formatCurrency(bump)}/yr")
                    )
                    state.copy(currentJob = job.copy(salary = bump)) to entry
                } else state to null
            }
            2 -> { // Tax Audit
                if (state.netWorth >= 200_000L || state.heatLevel >= 25) {
                    val penalty = (state.netWorth * 0.03).toLong().coerceIn(2_500L, 45_000L)
                    val entry = ChronicleEntry(
                        id = UUID.randomUUID().toString(),
                        age = state.age,
                        year = state.currentYear,
                        title = "Wildcard: Surprise Revenue Audit",
                        narrative = "Tax inspectors flagged discrepancies from past filings and assessed ${formatCurrency(penalty)} in back adjustments.",
                        category = ChronicleCategory.FINANCE,
                        tags = listOf("Wildcard", "-${formatCurrency(penalty)}")
                    )
                    state.copy(
                        cash = state.cash - penalty,
                        stats = state.stats.copy(stress = state.stats.stress + 10).clamp()
                    ) to entry
                } else state to null
            }
            3 -> { // Sudden Viral Spotlight
                val entry = ChronicleEntry(
                    id = UUID.randomUUID().toString(),
                    age = state.age,
                    year = state.currentYear,
                    title = "Wildcard: Viral Moment",
                    narrative = "A candid clip of you defending a stranger went viral across social feeds, surging Fame and Charisma!",
                    category = ChronicleCategory.MILESTONE,
                    tags = listOf("Wildcard", "+12 Fame")
                )
                state.copy(
                    stats = state.stats.copy(fame = state.stats.fame + 12, charisma = state.stats.charisma + 5, happiness = state.stats.happiness + 8).clamp(),
                    statusEffects = state.statusEffects + StatusEffect("viral", "Viral Spotlight", "+Charisma perception", 2, true)
                ) to entry
            }
            else -> { // Mystery Estate / Health Curveball
                if (rng.nextBoolean()) {
                    val gift = rng.nextLong(3_000L, 18_000L)
                    val entry = ChronicleEntry(
                        id = UUID.randomUUID().toString(),
                        age = state.age,
                        year = state.currentYear,
                        title = "Wildcard: Distant Relative Bequest",
                        narrative = "An estranged great-aunt left you ${formatCurrency(gift)} and an old brass key in her will.",
                        category = ChronicleCategory.LEGACY,
                        tags = listOf("Wildcard", "+${formatCurrency(gift)}")
                    )
                    state.copy(cash = state.cash + gift) to entry
                } else {
                    val entry = ChronicleEntry(
                        id = UUID.randomUUID().toString(),
                        age = state.age,
                        year = state.currentYear,
                        title = "Wildcard: Sudden Burnout Wave",
                        narrative = "A brutal seasonal bug combined with overwork knocked your energy flat.",
                        category = ChronicleCategory.HEALTH,
                        tags = listOf("Wildcard", "-9 Health", "+10 Stress")
                    )
                    state.copy(
                        stats = state.stats.copy(health = state.stats.health - 9, stress = state.stats.stress + 10).clamp(),
                        statusEffects = state.statusEffects + StatusEffect("burnout", "Burnout Fog", "Need rest or therapy", 2, false)
                    ) to entry
                }
            }
        }
    }
}
