package com.hoabh.chanko.presentation.screens.budget

import com.hoabh.chanko.data.budget.FakeBudgetRepository
import com.hoabh.chanko.domain.budget.model.CardNameValidationResult
import com.hoabh.chanko.domain.budget.model.CreditCard
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreditCardManagementTest {

    private lateinit var repository: FakeBudgetRepository

    @Before
    fun setUp() {
        repository = FakeBudgetRepository()
    }

    @Test
    fun `validateName trims whitespace and counts graphemes using BreakIterator`() {
        val emptyResult = CreditCard.validateName("   ")
        assertTrue(emptyResult is CardNameValidationResult.Empty)

        val validResult = CreditCard.validateName("  Thẻ Techcombank  ")
        assertTrue(validResult is CardNameValidationResult.Valid)
        assertEquals("Thẻ Techcombank", (validResult as CardNameValidationResult.Valid).normalizedName)

        // Emoji family (surrogate pairs + ZWJ) has multiple UTF-16 code units but 1 grapheme cluster
        val emojiCard = "Thẻ 💳 " + "A".repeat(34) // "Thẻ " (4) + "💳" (1 grapheme, 2 UTF-16 chars) + " " (1) + 34 = 40 graphemes (41 UTF-16 chars)
        assertEquals(40, CreditCard.graphemeCount(emojiCard))
        assertTrue(CreditCard.validateName(emojiCard) is CardNameValidationResult.Valid)

        val longName = "A".repeat(41)
        val tooLongResult = CreditCard.validateName(longName)
        assertTrue(tooLongResult is CardNameValidationResult.TooLong)
    }

    @Test
    fun `addCreditCard validates uniqueness case-insensitively`() = runTest {
        val initialCards = repository.getCreditCards().first()
        val existingName = initialCards[0].name // "Thẻ Techcombank"

        val duplicateResult = repository.addCreditCard(existingName.lowercase())
        assertTrue(duplicateResult.isFailure)
        assertEquals("Tên thẻ đã tồn tại", duplicateResult.exceptionOrNull()?.message)

        val uniqueResult = repository.addCreditCard("Thẻ Timo Mastercard")
        assertTrue(uniqueResult.isSuccess)
    }

    @Test
    fun `renameCreditCard allows keeping same name but rejects other existing active names and archived cards`() = runTest {
        val cards = repository.getCreditCards().first()
        val card1 = cards[0] // "Thẻ Techcombank"
        val card2 = cards[1] // "Thẻ VPBank Cake"

        // Đổi tên card1 thành chính nó -> thành công
        val sameNameResult = repository.renameCreditCard(card1.id, "Thẻ Techcombank")
        assertTrue(sameNameResult.isSuccess)

        // Đổi tên card1 thành tên của card2 -> lỗi trùng lặp
        val dupResult = repository.renameCreditCard(card1.id, card2.name)
        assertTrue(dupResult.isFailure)
        assertEquals("Tên thẻ đã tồn tại", dupResult.exceptionOrNull()?.message)

        // Khi thẻ đã bị xóa/archive trước đó -> trả về lỗi "Thẻ không còn khả dụng"
        repository.archiveCreditCard(card1.id)
        val renameArchivedResult = repository.renameCreditCard(card1.id, "Thẻ Mới")
        assertTrue(renameArchivedResult.isFailure)
        assertEquals("Thẻ không còn khả dụng", renameArchivedResult.exceptionOrNull()?.message)
    }

    @Test
    fun `archiveCreditCard removes card from active list without touching expenses`() = runTest {
        val initialCards = repository.getCreditCards().first()
        val card = initialCards[0]

        val archiveResult = repository.archiveCreditCard(card.id)
        assertTrue(archiveResult.isSuccess)

        val updatedCards = repository.getCreditCards().first()
        assertFalse(updatedCards.any { it.id == card.id })

        // Chi tiêu của chu kỳ vẫn giữ nguyên snapshot
        val snapshot = repository.getBudgetSnapshot(repository.currentCycleId).first()
        assertEquals(3_000_000L, snapshot.spentMinor)
    }
}
