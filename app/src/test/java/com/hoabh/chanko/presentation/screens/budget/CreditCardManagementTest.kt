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

    @Test
    fun `validateRename enforces C07 rules - empty, 30 chars, Techcombank 11, reserved, no-change, case-only, duplicate`() {
        val original = "Techcombank"
        val otherCards = listOf("Thẻ VPBank Cake", "Thẻ Timo")

        // 1. Techcombank is 11 graphemes
        assertEquals(11, CreditCard.graphemeCount(original))

        // 2. Empty or whitespace -> Empty
        assertTrue(CreditCard.validateRename("   ", original, otherCards) is com.hoabh.chanko.domain.budget.model.RenameValidationResult.Empty)

        // 3. > 30 graphemes -> TooLong
        val over30 = "A".repeat(31)
        assertTrue(CreditCard.validateRename(over30, original, otherCards) is com.hoabh.chanko.domain.budget.model.RenameValidationResult.TooLong)

        // 4. Exact 30 graphemes -> Valid
        val exact30 = "A".repeat(30)
        assertTrue(CreditCard.validateRename(exact30, original, otherCards) is com.hoabh.chanko.domain.budget.model.RenameValidationResult.Valid)

        // 5. Reserved name "Pay Now" (case-insensitive) -> ReservedPayNow
        assertTrue(CreditCard.validateRename("Pay Now", original, otherCards) is com.hoabh.chanko.domain.budget.model.RenameValidationResult.ReservedPayNow)
        assertTrue(CreditCard.validateRename("pay now", original, otherCards) is com.hoabh.chanko.domain.budget.model.RenameValidationResult.ReservedPayNow)

        // 6. Same as original -> NoChange
        assertTrue(CreditCard.validateRename("Techcombank", original, otherCards) is com.hoabh.chanko.domain.budget.model.RenameValidationResult.NoChange)
        assertTrue(CreditCard.validateRename("  Techcombank  ", original, otherCards) is com.hoabh.chanko.domain.budget.model.RenameValidationResult.NoChange)

        // 7. Case-only change of self -> Valid (AC33)
        val caseOnly = CreditCard.validateRename("techcombank", original, otherCards)
        assertTrue(caseOnly is com.hoabh.chanko.domain.budget.model.RenameValidationResult.Valid)
        assertEquals("techcombank", (caseOnly as com.hoabh.chanko.domain.budget.model.RenameValidationResult.Valid).normalizedName)

        // 8. Duplicate of other active card -> Duplicate
        assertTrue(CreditCard.validateRename("Thẻ VPBank Cake", original, otherCards) is com.hoabh.chanko.domain.budget.model.RenameValidationResult.Duplicate)
        assertTrue(CreditCard.validateRename("thẻ vpbank cake", original, otherCards) is com.hoabh.chanko.domain.budget.model.RenameValidationResult.Duplicate)

        // 9. Legacy name > 30
        val legacy35 = "A".repeat(35)
        assertTrue(CreditCard.isLegacyOverLimit(legacy35))
        assertFalse(CreditCard.isLegacyOverLimit(original))
    }

    @Test
    fun `renameCreditCard guards against reserved Pay Now and preserves card color`() = runTest {
        val cards = repository.getCreditCards().first()
        val card1 = cards[0]

        // Reject "Pay Now"
        val payNowResult = repository.renameCreditCard(card1.id, "Pay Now")
        assertTrue(payNowResult.isFailure)
        assertEquals("Tên này dành cho Pay Now", payNowResult.exceptionOrNull()?.message)

        // Rename succeeds and preserves color
        val originalColor = card1.color
        val renameResult = repository.renameCreditCard(card1.id, "Thẻ Chi Tiêu Mới")
        assertTrue(renameResult.isSuccess)

        val updatedCards = repository.getCreditCards().first()
        val updatedCard1 = updatedCards.first { it.id == card1.id }
        assertEquals("Thẻ Chi Tiêu Mới", updatedCard1.name)
        assertEquals(originalColor, updatedCard1.color)
    }
    @Test
    fun `add validation normalizes names and applies dedicated add constraints`() {
        val empty = emptyList<String>()
        assertTrue(CreditCard.validateAdd("  ", empty) is com.hoabh.chanko.domain.budget.model.AddCardValidationResult.Empty)
        assertTrue(CreditCard.validateAdd("x".repeat(30), empty) is com.hoabh.chanko.domain.budget.model.AddCardValidationResult.Valid)
        assertTrue(CreditCard.validateAdd("x".repeat(31), empty) is com.hoabh.chanko.domain.budget.model.AddCardValidationResult.TooLong)
        assertTrue(CreditCard.validateAdd(" PAY NOW ", empty) is com.hoabh.chanko.domain.budget.model.AddCardValidationResult.ReservedPayNow)
        assertTrue(CreditCard.validateAdd(" the\u0309 ", listOf("Thẻ")) is com.hoabh.chanko.domain.budget.model.AddCardValidationResult.Duplicate)
        val valid = CreditCard.validateAdd("  Thẻ mới  ", empty) as com.hoabh.chanko.domain.budget.model.AddCardValidationResult.Valid
        assertEquals("Thẻ mới", valid.normalizedName)
    }

    @Test
    fun `repository rejects new card exceeding UI limit without adding a record`() = runTest {
        val before = repository.getCreditCards().first()
        assertTrue(repository.addCreditCard("x".repeat(31)).isFailure)
        assertEquals(before, repository.getCreditCards().first())
    }
}
