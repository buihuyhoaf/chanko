package com.hoabh.chanko.presentation.features.additem

import com.hoabh.chanko.domain.additem.*
import org.junit.Assert.*
import org.junit.Test

class AddItemFormStateTest {
    private fun valid() = AddItemFormState( title = "Tai nghe", rawPrice = "2.490.000",
        category = "Điện tử", reason = "Làm việc", confirmedPrice = 2490000, saveIdempotencyKey = "stable")
    @Test fun exactPriceRejectsAmbiguousRangesAndOverflow() {
        listOf("", "0", "-10", "100-200", "100k", "1,000", "1.23", "1.000.00", "9223372036854775808").forEach {
            assertNull(it, parseExactVnd(it))
        }
        assertEquals(2490000L, parseExactVnd("2.490.000"))
        assertEquals(1000L, parseExactVnd("1000"))
        assertEquals(Long.MAX_VALUE, parseExactVnd(Long.MAX_VALUE.toString()))
    }
    @Test fun requiredFieldsAndExactConfirmationGateContinue() {
        val d = valid()
        assertTrue(d.canContinue)
        assertFalse(d.copy(reason = "  ").canContinue)
        assertFalse(d.copy(category = "").canContinue)
        assertFalse(d.copy(confirmedPrice = 20).canContinue)
        assertFalse(d.copy(title = " ").canContinue)
    }
    @Test fun editInvalidatesPriceAnswersAndAssessment() {
        val d = valid().copy(answers = mapOf("q1" to "a"), coolingChoice = CoolingChoice.APPLIED,
            assessment = AssessmentSnapshot(Verdict.BUY, listOf("reason"), emptyList(), 0, 0, "test"))
        val edited = d.editProduct { it.copy(url = "https://example.com/new") }
        assertNull(edited.confirmedPrice)
        assertNull(edited.assessment)
        assertTrue(edited.answers.isEmpty())
        assertEquals(CoolingChoice.UNSELECTED, edited.coolingChoice)
        assertEquals(1L, edited.productRevision)
        assertEquals("stable", edited.saveIdempotencyKey)
    }
    @Test fun urlValidationAcceptsOnlyWebUrlsWithHosts() {
        assertTrue(isProductUrl("https://example.com/p?id=2"))
        listOf("", "example.com", "file:///tmp/p", "javascript:alert(1)", "https://", "https://user:pass@example.com").forEach {
            assertFalse(it, isProductUrl(it))
        }
    }
}
