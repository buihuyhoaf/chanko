package com.hoabh.chanko.ui.mascot

import com.hoabh.chanko.domain.chat.model.MascotEmotion
import com.hoabh.chanko.domain.rewards.MascotEvent
import com.hoabh.chanko.domain.rewards.MascotReactionPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MascotEmotionStateTest {

    @Test
    fun rewardEarned_returnsEncouragingAndCelebratory() {
        val reaction = MascotReactionPolicy.resolveReaction(
            MascotEvent.RewardEarned(hatEarned = 15, pateEarned = 15)
        )
        assertEquals(MascotEmotion.ENCOURAGING, reaction.emotion)
        assertTrue(reaction.isCelebratory)
        assertTrue(reaction.messageVi.contains("+15 Hạt"))
        assertTrue(reaction.messageVi.contains("+15 Pate"))
    }

    @Test
    fun levelUp_returnsWelcomeAndCelebratory() {
        val reaction = MascotReactionPolicy.resolveReaction(
            MascotEvent.LevelUp(newLevel = 4)
        )
        assertEquals(MascotEmotion.WELCOME, reaction.emotion)
        assertTrue(reaction.isCelebratory)
        assertTrue(reaction.messageVi.contains("Cấp 4"))
    }

    @Test
    fun dailyCheckIn_returnsEncouragingAndCelebratory() {
        val reaction = MascotReactionPolicy.resolveReaction(
            MascotEvent.DailyCheckInCompleted
        )
        assertEquals(MascotEmotion.ENCOURAGING, reaction.emotion)
        assertTrue(reaction.isCelebratory)
    }

    @Test
    fun earlyUnfreeze_strictlyNeutralAndNonJudgmental() {
        val reaction = MascotReactionPolicy.resolveReaction(
            MascotEvent.EarlyUnfreeze(hatDeducted = 25, pateDeducted = 12)
        )
        // MASCOT-FEEDING = A: Chanko không khóc, không giận dỗi, không trách móc
        assertEquals(MascotEmotion.NEUTRAL, reaction.emotion)
        assertFalse(reaction.isCelebratory)
        assertTrue(reaction.messageVi.contains("25 Hạt"))
        assertTrue(reaction.messageVi.contains("12 Pate"))
        assertFalse(reaction.messageVi.contains("buồn") || reaction.messageVi.contains("khóc"))
    }

    @Test
    fun purchaseRecorded_strictlyNeutralTone() {
        val reaction = MascotReactionPolicy.resolveReaction(
            MascotEvent.PurchaseRecorded(itemName = "Tai nghe Sony")
        )
        assertEquals(MascotEmotion.NEUTRAL, reaction.emotion)
        assertFalse(reaction.isCelebratory)
        assertTrue(reaction.messageVi.contains("Tai nghe Sony"))
    }

    @Test
    fun itemPassed_returnsRelaxedEmotion() {
        val reaction = MascotReactionPolicy.resolveReaction(
            MascotEvent.ItemPassed
        )
        assertEquals(MascotEmotion.RELAXED, reaction.emotion)
        assertFalse(reaction.isCelebratory)
        assertTrue(reaction.messageVi.contains("bảo toàn ngân sách"))
    }
}
