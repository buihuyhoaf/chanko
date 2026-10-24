package com.hoabh.chanko.ui.theme

import androidx.compose.ui.graphics.Color

// UI-02 approved signal colors. Do not use as small text on white.
object CandyColors {
    val primary = Color(0xFF8965F6)
    val secondary = Color(0xFFFF85B3)
    val background = Color(0xFFFAF8F5)
    val yellow = Color(0xFFFFD75E)
    val mint = Color(0xFF78DCC3)
    val blue = Color(0xFF8DCDF8)
    val orange = Color(0xFFFFAA61)
    val coolingOff = blue
    val readyForReview = yellow
    val purchased = Color(0xFF6888C9)
    val skipped = mint
    val postponed = orange
    val budgetWarning = Color(0xFFE9A23B)
    val overBudget = Color(0xFFE56B73)
    val error = Color(0xFFD94B57)
    val success = Color(0xFF269C78)
    // Reference foregrounds, verified in phase-2-1-completion.md; PV-01 remains open.
    val ink = Color.Black
    val mutedInk = Color(0xFF595959)
    val outline = Color(0xFF767676)
    val errorText = Color(0xFFAA2635)
    // Splash brand typography colors
    val logoBrown = Color(0xFF60362F)
    val logoCoral = Color(0xFFD63F55)

    // Home v5 Tokens (from UI-HANDOFF.md and tokens.json)
    val brandPurple = Color(0xFF8965F6)
    val brandPink = Color(0xFFFF85B3)
    val backgroundLavender = Color(0xFFF0E7FF)
    val backgroundPink = Color(0xFFFCEAF4)
    val calendarSurface = Color(0xFFF4EEFF)
    val calendarInnerSurface = Color(0xFFFAF7FF)
    val primarySoft = Color(0xFFEBE1FF)
    val primaryInk = Color(0xFF5731C6)
    val textPrimary = Color(0xFF151329)
    val textSecondary = Color(0xFF585C82)
    val divider = Color(0xFFE4DDF2)
    val progressTrack = Color(0xFFE3DEEC)
    val readySurface = Color(0xFFD9FAE6)
    val readySurfaceEnd = Color(0xFFCFF6DF)
    val readyAction = Color(0xFF00634F)
    val onReadyAction = Color(0xFFFFFFFF)
    val readyInk = Color(0xFF075C48)
    val readyChip = Color(0xFFE6FFF0)
    val coolingBlue = Color(0xFFE0EBFF)
    val coolingLavender = Color(0xFFE7E1FF)
    val coolingChip = Color(0xFFD3D9FF)
    val speechSurface = Color(0xFFFFECE5)

    // Home v2 Image-Aligned Semantic Tokens (approved-home-user-reference.png & 11-ASSET-DELIVERY.md)
    val homeScreenBackgroundTop = Color(0xFFF6F0FF)
    val homeScreenBackgroundBottom = Color(0xFFFDF7FA)
    val homeCardSurface = Color(0xFFFDFBFF)
    val homeCardBorder = Color(0xFFEDE6F8)
    val dialogueBubbleSurface = Color(0xFFFFFBF8)
    val dialogueBubbleBorder = Color(0xFFF2E6DE)
    val calendarDayInk = Color(0xFF585066)

    // Quick Actions
    val quickCheckGradientStart = Color(0xFF9C7AF8)
    val quickCheckGradientEnd = Color(0xFF7B57E8)
    val saveItemSurface = Color(0xFFF7F3FF)
    val saveItemBorder = Color(0xFFE8DFF8)
    val quickActionAskBg = Color(0xFF7451E8)
    val quickActionAskBorder = Color(0xFFA48BFF)
    val quickActionAskText = Color(0xFFFFFFFF)
    val quickActionSaveBg = Color(0xFFF4B3CB)
    val quickActionSaveBorder = Color(0xFFE68DB2)
    val quickActionSaveText = Color(0xFF512B45)

    // Candy Pop 3D Home Budget Snapshot
    val budgetSnapshotBg = Color(0xFFEEE5FF)
    val budgetSnapshotBorder = Color(0xFFD9C5FF)
    val budgetSnapshotAmount = Color(0xFF6F49DE)
    val budgetSnapshotSecondaryText = Color(0xFF665684)
    val budgetSnapshotSpendingPanel = Color(0xFFFFFFFF)
    val budgetSnapshotBadgeBg = Color(0xFFFFE7B8)
    val budgetSnapshotBadgeText = Color(0xFF8B5814)
    val budgetSnapshotProgressTrack = Color(0xFFEEE8F5)
    val budgetSnapshotProgressFill = Color(0xFF8965F6)

    // Budget Snapshot
    val budgetWarningBadgeBg = Color(0xFFFFF0DB)
    val budgetWarningBadgeInk = Color(0xFFD97706)
    val budgetReachedBadgeBg = Color(0xFFFFE8CC)
    val budgetReachedBadgeInk = Color(0xFFB45309)
    val budgetOverBadgeBg = Color(0xFFFFE4E6)
    val budgetOverBadgeInk = Color(0xFFBE123C)
    val budgetNormalBadgeBg = Color(0xFFE6FFF0)
    val budgetNormalBadgeInk = Color(0xFF075C48)

    // Your Decisions Rows & Pills
    val decisionReadyRowBg = Color(0xFFECFDF5)
    val decisionReadyPillBg = Color(0xFFD1FAE5)
    val decisionReadyPillInk = Color(0xFF065F46)
    val decisionWaitingRowBg = Color(0xFFF5F2FF)
    val decisionWaitingPillBg = Color(0xFFEDE7FF)
    val decisionWaitingPillInk = Color(0xFF5731C6)
    val decisionConsideringRowBg = Color(0xFFFFFBEB)
    val decisionConsideringPillBg = Color(0xFFFEF3C7)
    val decisionConsideringPillInk = Color(0xFF92400E)
    val decisionSavedRowBg = Color(0xFFF3F4F6)
    val decisionSavedPillBg = Color(0xFFE5E7EB)
    val decisionSavedPillInk = Color(0xFF374151)

    // Candy Item Tile Semantic Tokens
    val attentionTileReadyBg = Color(0xFFDFF9EE)
    val attentionTileReadyBorder = Color(0xFFA6EBD1)
    val attentionTileReadyAccent = Color(0xFF16856B)

    val attentionTileWaitingBg = Color(0xFFE8F4FF)
    val attentionTileWaitingBorder = Color(0xFFB8DFFF)
    val attentionTileWaitingAccent = Color(0xFF438BCB)

    val attentionTileConsideringBg = Color(0xFFEEE7FF)
    val attentionTileConsideringBorder = Color(0xFFD9C8FF)
    val attentionTileConsideringAccent = Color(0xFF7451E8)

    val attentionTileSavedBg = Color(0xFFFFF0DB)
    val attentionTileSavedBorder = Color(0xFFFFDBA7)
    val attentionTileSavedAccent = Color(0xFFB67C28)

    // Observation & Global Sheet
    val observationIconTileBg = Color(0xFFF2EBFF)
    val observationBulbYellow = Color(0xFFF59E0B)
    val globalSheetSaveIconTileBg = Color(0xFFFFEBF3)
    val globalSheetSaveIconInk = Color(0xFFE11D74)
}
