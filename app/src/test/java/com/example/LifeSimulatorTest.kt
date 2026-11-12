package com.example

import com.example.data.model.*
import com.example.engine.CascadeCommunicationEngine
import com.example.engine.GovernanceCrimeEngine
import com.example.engine.LifeCatalog
import com.example.engine.LifeSimulator
import com.example.engine.PrisonAction
import com.example.engine.SelfAction
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class LifeSimulatorTest {

    @Test
    fun createNewLife_andAdvanceYears_progressesAgeAndChronicle() {
        val rng = Random(42)
        var life = LifeSimulator.createNewLife(
            customName = "Elena Vance",
            gender = Gender.FEMALE,
            country = LifeCatalog.countries.first(),
            preset = OriginPreset.TRUST_FUND,
            rng = rng
        )
        assertEquals("Elena Vance", life.fullName)
        assertEquals(0, life.age)
        assertTrue(life.cash > 0L)

        repeat(20) {
            if (life.pendingEvent != null) {
                life = LifeSimulator.resolveEventChoice(life, 0, rng)
            }
            life = LifeSimulator.advanceYear(life, rng)
        }
        assertEquals(20, life.age)
        assertTrue(life.chronicle.size >= 2)
    }

    @Test
    fun businessAndMarkets_updateNetWorthAccurately() {
        val rng = Random(7)
        val life = LifeSimulator.createNewLife(
            customName = "Marcus Sterling",
            preset = OriginPreset.TRUST_FUND,
            rng = rng
        ).copy(age = 24)

        val (afterBuy, buyMsg) = LifeSimulator.buyAsset(life, "VTX", 10_000L)
        assertTrue(buyMsg.contains("Bought"))
        assertTrue(afterBuy.portfolioValue > 9_500L)

        val (afterBiz, bizMsg) = LifeSimulator.foundBusiness(afterBuy, "Sterling Cloud", BusinessSector.SAAS)
        assertNotNull(afterBiz.business)
        assertTrue(bizMsg.contains("Founded"))

        val (afterAction, _) = LifeSimulator.performSelfAction(afterBiz, SelfAction.WORKOUT, rng)
        assertEquals(1, afterAction.actionsUsedThisYear)

        val (afterExit, _) = LifeSimulator.exitBusiness(afterAction)
        assertNull(afterExit.business)
        assertTrue(afterExit.cash > afterAction.cash)
    }

    @Test
    fun continueAsHeir_incrementsGenerationAndTransfersEstate() {
        val parent = LifeSimulator.createNewLife(
            customName = "Soren Lindqvist",
            preset = OriginPreset.TRUST_FUND
        ).copy(age = 82, isAlive = false, causeOfDeath = "Natural Causes")

        val heir = LifeSimulator.continueAsHeir(parent)
        assertEquals(2, heir.generation)
        assertEquals(parent.dynastyId, heir.dynastyId)
        assertTrue(heir.cash > 0L)
        assertTrue(heir.isAlive)
    }

    @Test
    fun cascadingConsequences_sabotageAndDilemmaSpawnDelayedEvents() {
        val rng = Random(101)
        val base = LifeSimulator.createNewLife(
            customName = "Julian Vale",
            preset = OriginPreset.TRUST_FUND,
            rng = rng
        ).let {
            it.copy(
                age = 25,
                educationLevel = EducationLevel.HIGH_SCHOOL,
                stats = it.stats.copy(intellect = 75)
            )
        }

        val (hired, _) = LifeSimulator.applyForJob(base, "tech_1")
        assertNotNull(hired.currentJob)

        // Force successful office sabotage via deterministic seed or inspect delayed arc firing
        val planted = hired.copy(
            pendingArcs = listOf(
                DelayedArc(
                    id = "arc-1",
                    arcKey = "rival_vendetta",
                    triggerAge = 27,
                    stage = 1,
                    originChoiceLabel = "Sabotaged office rival at Age 25"
                )
            )
        )

        val year26 = LifeSimulator.advanceYear(planted.copy(pendingEvent = null), rng)
        assertEquals(1, year26.pendingArcs.size) // Not due until 27

        val year27 = LifeSimulator.advanceYear(year26.copy(pendingEvent = null), rng)
        assertNotNull(year27.pendingEvent)
        assertEquals(ChronicleCategory.CASCADE, year27.pendingEvent?.category)
        assertEquals("The Rival's Counterstrike", year27.pendingEvent?.title)
        assertTrue(year27.pendingArcs.isEmpty())
    }

    @Test
    fun npcInboxReplyAndDirectDialogue_updateRelationsAndPlantCascade() {
        val friend = NpcRelation(
            id = "npc-friend",
            name = "Aria Chen",
            role = RelationRole.FRIEND,
            age = 24,
            closeness = 60,
            trust = 65,
            archetype = NpcArchetype.AMBITIOUS,
            occupation = "Founder",
            netWorth = 25_000L
        )
        val base = LifeSimulator.createNewLife(
            customName = "Devon Brooks",
            preset = OriginPreset.TRUST_FUND
        ).copy(
            age = 24,
            cash = 30_000L,
            relations = listOf(friend)
        )
        val msg = NpcMessage(
            id = "msg-1",
            npcId = friend.id,
            senderName = friend.name,
            senderRole = friend.role.label,
            ageSent = 24,
            subject = "Urgent favor",
            body = "\"Can you spot me $4,000?\"",
            replyOptions = listOf(
                MessageReply(
                    label = "Wire $4,000",
                    toneTag = "Supportive",
                    closenessDelta = 18,
                    trustDelta = 20,
                    cashDelta = -4_000L,
                    spawnsArcKey = "loan_payoff_surge",
                    cascadeDelayYears = 2,
                    responseSummary = "Aria appreciated your trust."
                )
            )
        )
        val withInbox = base.copy(inbox = listOf(msg))
        assertEquals(1, withInbox.unreadInboxCount)

        val (replied, _) = LifeSimulator.replyToNpcMessage(withInbox, "msg-1", 0)
        assertEquals(0, replied.unreadInboxCount)
        assertEquals(26_000L, replied.cash)
        assertEquals(1, replied.pendingArcs.size)
        assertEquals("loan_payoff_surge", replied.pendingArcs.first().arcKey)
        assertEquals(78, replied.relations.first().closeness)

        val replies = CascadeCommunicationEngine.buildDirectConversationReplies(replied.relations.first(), replied)
        assertTrue(replies.isNotEmpty())
        val (afterDialogue, _) = LifeSimulator.applyDirectDialogue(replied, friend.id, replies.first())
        assertEquals(1, afterDialogue.actionsUsedThisYear)
        assertTrue(afterDialogue.relations.first().trust > replied.relations.first().trust)
    }

    @Test
    fun lifestyleCollectiblesClubsAndPromotion_workEndToEnd() {
        val teen = LifeSimulator.createNewLife(
            customName = "Nadia Park",
            preset = OriginPreset.TRUST_FUND
        ).copy(age = 16, cash = 250_000L)

        val (withClub, _) = LifeSimulator.toggleSchoolClub(teen, SchoolClub.ROBOTICS)
        assertTrue(SchoolClub.ROBOTICS in withClub.schoolClubs)

        val adult = withClub.copy(
            age = 25,
            educationLevel = EducationLevel.UNIVERSITY,
            stats = withClub.stats.copy(intellect = 75),
            skills = withClub.skills + (SkillType.ENGINEERING to SkillProgress(SkillType.ENGINEERING, 120))
        )
        val (withLifestyle, _) = LifeSimulator.setLifestyle(adult, LifestyleTier.UPSCALE)
        assertEquals(LifestyleTier.UPSCALE, withLifestyle.lifestyle)

        val (withLuxury, _) = LifeSimulator.buyLuxuryItem(withLifestyle, LuxuryItem.VINTAGE_CHRONO)
        assertTrue(LuxuryItem.VINTAGE_CHRONO in withLuxury.collectibles)
        assertTrue(withLuxury.collectiblesValue == LuxuryItem.VINTAGE_CHRONO.price)

        val (withJob, _) = LifeSimulator.applyForJob(withLuxury, "tech_1")
        assertNotNull(withJob.currentJob)
        val readyForPromo = withJob.copy(
            currentJob = withJob.currentJob!!.copy(performance = 85),
            actionsUsedThisYear = 0
        )
        val (promoted, promoMsg) = LifeSimulator.seekPromotion(readyForPromo)
        assertTrue(promoMsg.contains("Promoted"))
        assertEquals(2, promoted.currentJob?.tier)
    }

    @Test
    fun enactedLaws_realisticallyAlterTaxesTuitionCryptoAndAmnesty() {
        val base = LifeSimulator.createNewLife(
            customName = "Cassian Vane",
            country = LifeCatalog.countries.first(),
            preset = OriginPreset.TRUST_FUND
        ).copy(
            age = 20,
            cash = 60_000L,
            lifestyle = LifestyleTier.FRUGAL,
            educationLevel = EducationLevel.IN_UNIVERSITY,
            degreeMajor = DegreeMajor.COMPUTER_SCIENCE,
            heatLevel = 45,
            prison = PrisonState(isInPrison = true, yearsRemaining = 4, totalSentence = 4, crimeName = "Wire Fraud"),
            government = GovernmentState(isPlayerPresident = true, presidentName = "Cassian Vane")
        )

        // 1. FREE_TUITION drops university annual tuition from $14,000 to $0
        val (withFreeTuition, _) = LifeSimulator.enactLawAsRuler(base, LawType.FREE_TUITION)
        assertTrue(withFreeTuition.hasLaw(LawType.FREE_TUITION))
        val afterNoTuition = LifeSimulator.advanceYear(withFreeTuition.copy(prison = PrisonState()), Random(11))
        val afterWithTuition = LifeSimulator.advanceYear(base.copy(prison = PrisonState()), Random(11))
        assertEquals(0L, afterNoTuition.debt)
        assertTrue(afterNoTuition.cash > afterWithTuition.cash)

        // 2. CRYPTO_CRACKDOWN immediately blocks crypto purchases
        val (withCryptoBan, _) = LifeSimulator.enactLawAsRuler(withFreeTuition, LawType.CRYPTO_CRACKDOWN)
        val (_, banMsg) = LifeSimulator.buyAsset(withCryptoBan, "BTCX", 1_000L)
        assertTrue(banMsg.contains("Prohibition", ignoreCase = true))

        // 3. WEALTH_SURTAX increases effective tax rate when net worth >= $1M
        val rich = withFreeTuition.copy(cash = 1_500_000L)
        val baseTax = rich.effectiveTaxRate()
        val (withSurtax, _) = LifeSimulator.enactLawAsRuler(rich, LawType.WEALTH_SURTAX)
        assertTrue(withSurtax.effectiveTaxRate() > baseTax)

        // 4. CRIMINAL_AMNESTY commutes non-lethal prison sentence & clears heat to 0
        val (afterAmnesty, _) = LifeSimulator.enactLawAsRuler(base, LawType.CRIMINAL_AMNESTY)
        assertFalse(afterAmnesty.prison.isInPrison)
        assertEquals(0, afterAmnesty.heatLevel)
    }

    @Test
    fun royaltyPresidencyDeathRowAndPrisonYard_workEndToEnd() {
        val royal = LifeSimulator.createNewLife(
            customName = "Alistair Crown",
            preset = OriginPreset.ROYAL_BLOOD
        )
        assertEquals(RoyalRank.CROWN_HEIR, royal.royal.rank)

        // Royal Decree & Action as Monarch
        val monarch = royal.copy(age = 26, royal = royal.royal.copy(rank = RoyalRank.MONARCH))
        val (afterDecree, _) = LifeSimulator.enactLawAsRuler(monarch, LawType.CAPITAL_PUNISHMENT)
        assertTrue(afterDecree.hasLaw(LawType.CAPITAL_PUNISHMENT))

        // Lethal crime failure under CAPITAL_PUNISHMENT puts convict on Death Row
        val (convicted, _) = GovernanceCrimeEngine.sendToPrison(afterDecree, "Contract Murder", 24, isLethal = true)
        assertTrue(convicted.prison.isInPrison)
        assertTrue(convicted.prison.isDeathRow)
        assertEquals(PrisonSecurity.DEATH_ROW, convicted.prison.securityLevel)
        assertEquals(3, convicted.prison.yearsUntilExecution)

        // Study Law Library and Bribe Warden inside Prison
        val (afterStudy, _) = LifeSimulator.performPrisonAction(convicted, PrisonAction.LAW_LIBRARY, Random(3))
        assertEquals(25, afterStudy.prison.lawStudyProgress)

        val (afterBribe, _) = LifeSimulator.performPrisonAction(afterStudy, PrisonAction.BRIBE_WARDEN, Random(3))
        assertFalse(afterBribe.prison.isDeathRow)

        // Death Row execution terminates life after countdown hits 0
        val onDeathRow1y = convicted.copy(prison = convicted.prison.copy(yearsUntilExecution = 1), pendingEvent = null)
        val executed = LifeSimulator.advanceYear(onDeathRow1y, Random(5))
        assertFalse(executed.isAlive)
        assertEquals("Executed on Death Row", executed.causeOfDeath)
    }
}
