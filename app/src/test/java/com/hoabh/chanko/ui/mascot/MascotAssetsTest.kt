package com.hoabh.chanko.ui.mascot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MascotAssetsTest {
    // Resolver fixtures only: these are not registered or rendered assets/approval claims.
    private fun fixture(id: Int) = MascotStaticAsset(id, "test", "fixture", "fixture",
        "fixture", "fixture", 1, 1)

    @Test fun requestedStateTakesPrecedenceOverNeutral() {
        val thinking = fixture(1)
        val assets = mapOf(MascotPresentationState.Thinking to thinking,
            MascotPresentationState.Neutral to fixture(2))
        assertEquals(thinking, resolveMascotAsset(MascotPresentationState.Thinking, assets))
    }

    @Test fun missingStateUsesOnlyNeutralWithoutInventingAnotherEmotion() {
        val neutral = fixture(2)
        val assets = mapOf(MascotPresentationState.Neutral to neutral,
            MascotPresentationState.Welcome to fixture(1))
        assertEquals(neutral, resolveMascotAsset(MascotPresentationState.Concerned, assets))
        assertNull(resolveMascotAsset(MascotPresentationState.Concerned,
            mapOf(MascotPresentationState.Welcome to fixture(1))))
    }

    @Test fun unapprovedCatalogLeavesEveryStateEmpty() {
        MascotPresentationState.entries.forEach { assertNull(MascotAssets.resolve(it)) }
    }
}
