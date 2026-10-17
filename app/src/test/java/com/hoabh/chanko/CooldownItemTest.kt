package com.hoabh.chanko

import com.hoabh.chanko.domain.model.CooldownItem
import com.hoabh.chanko.domain.model.ItemStatus
import org.junit.Assert.*
import org.junit.Test

class CooldownItemTest {
    private val item = CooldownItem(name = "Tai nghe", price = 500_000,
        addedTimestamp = 1_000, cooldownDurationMillis = 3_600_000, reason = "Làm việc")

    @Test fun cannotDecideBeforeDeadline() {
        assertEquals(1L, item.remainingMillis(3_600_999))
        assertFalse(item.canDecide(3_600_999))
    }
    @Test fun canDecideAtDeadlineAndAfterRestart() {
        assertTrue(item.canDecide(3_601_000))
        assertTrue(item.copy().canDecide(9_000_000))
        assertEquals(0L, item.remainingMillis(9_000_000))
    }
    @Test fun finishedItemsCannotBeDecidedAgain() {
        assertFalse(item.copy(status = ItemStatus.PASSED).canDecide(9_000_000))
        assertFalse(item.copy(status = ItemStatus.BOUGHT).canDecide(9_000_000))
    }
    @Test fun clockMovingBackDoesNotExceedOriginalDuration() {
        assertEquals(3_600_000L, item.remainingMillis(0))
    }
}
