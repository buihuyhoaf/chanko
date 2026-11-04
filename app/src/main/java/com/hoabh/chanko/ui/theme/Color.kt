package com.hoabh.chanko.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Raw color palette (CandyPalette).
 * Every hex color in the application is declared here EXACTLY ONCE.
 * Do not access CandyPalette directly from screens; use semantic tokens in CandyColors.
 */
private object CandyPalette {
    // Brand & Primary Purples / Lavenders
    val Purple500 = Color(0xFF8965F6)
    val Purple600 = Color(0xFF7447FF)
    val Purple700 = Color(0xFF7451E8)
    val Purple750 = Color(0xFF7B57E8)
    val Purple800 = Color(0xFF6F49DE)
    val Purple900 = Color(0xFF5731C6)
    val Purple400 = Color(0xFF9C7AF8)
    val Purple300 = Color(0xFFA48BFF)
    val Purple450 = Color(0xFF8B5CF6)
    val Violet500 = Color(0xFFA259D7)
    val Violet700 = Color(0xFF7D35B3)
    val Violet800 = Color(0xFF74418E)
    val Violet200 = Color(0xFFD9C2E1)
    val Violet100 = Color(0xFFE5D9EB)
    val Violet50 = Color(0xFFF2E5F7)
    val VioletMuted = Color(0xFF776A7E)
    val VioletMutedDark = Color(0xFF75667D)
    val VioletDeep = Color(0xFF42204E)
    val VioletSurface = Color(0xFFFCF6FB)
    val Lavender100 = Color(0xFFF0E7FF)
    val Lavender200 = Color(0xFFEBE1FF)
    val Lavender50 = Color(0xFFF4EEFF)
    val Lavender25 = Color(0xFFFAF7FF)
    val Lavender150 = Color(0xFFEEE5FF)
    val Lavender300 = Color(0xFFD9C5FF)
    val Lavender40 = Color(0xFFF5F2FF)
    val Lavender120 = Color(0xFFEDE7FF)
    val Lavender130 = Color(0xFFEEE7FF)
    val Lavender280 = Color(0xFFD9C8FF)
    val Lavender60 = Color(0xFFF2EBFF)
    val Lavender110 = Color(0xFFEDE6F8)
    val Lavender250 = Color(0xFFDDD2F2)
    val Lavender30 = Color(0xFFF3EDFD)
    val Lavender80 = Color(0xFFF0E9FF)
    val Lavender90 = Color(0xFFEDE9FE)
    val Lavender70 = Color(0xFFF3E8FF)
    val Lavender220 = Color(0xFFE4DDF2)
    val Lavender210 = Color(0xFFE3DEEC)
    val Lavender160 = Color(0xFFE7E1FF)
    val Lavender290 = Color(0xFFD3D9FF)
    val Lavender20 = Color(0xFFF8F5FC)
    val Lavender85 = Color(0xFFF0ECF8)
    val Lavender230 = Color(0xFFE2DDEC)
    val Purple20 = Color(0xFFF6F0FF)
    val Purple30 = Color(0xFFF7F3FF)
    val Purple120 = Color(0xFFE8DFF8)
    val Purple80 = Color(0xFFEEE8F5)

    // Pinks, Roses & Magentas
    val Pink500 = Color(0xFFFF85B3)
    val Pink100 = Color(0xFFFCEAF4)
    val Pink300 = Color(0xFFF4B3CB)
    val Pink400 = Color(0xFFE68DB2)
    val Pink50 = Color(0xFFFFEBF3)
    val Pink700 = Color(0xFFE11D74)
    val Pink60 = Color(0xFFFDE8F3)
    val Pink20 = Color(0xFFFDF7FA)
    val Plum900 = Color(0xFF512B45)
    val Plum950 = Color(0xFF4D2939)
    val Rose100 = Color(0xFFFFE4E6)
    val Rose800 = Color(0xFFBE123C)

    // Blues & Cyans
    val Blue400 = Color(0xFF8DCDF8)
    val Blue600 = Color(0xFF6888C9)
    val Blue100 = Color(0xFFE0EBFF)
    val Blue50 = Color(0xFFE8F4FF)
    val Blue200 = Color(0xFFB8DFFF)
    val Blue500 = Color(0xFF438BCB)
    val Blue800 = Color(0xFF1B5A8A)
    val Blue900 = Color(0xFF2E4E8A)
    val Teal700 = Color(0xFF08979C)
    val Teal50 = Color(0xFFE6FFFB)

    // Greens & Mints
    val Mint400 = Color(0xFF78DCC3)
    val Mint700 = Color(0xFF269C78)
    val Mint50 = Color(0xFFDFF9EE)
    val Mint200 = Color(0xFFA6EBD1)
    val Mint800 = Color(0xFF16856B)
    val Green100 = Color(0xFFD9FAE6)
    val Green150 = Color(0xFFCFF6DF)
    val Green900 = Color(0xFF00634F)
    val Green800 = Color(0xFF075C48)
    val Green50 = Color(0xFFE6FFF0)
    val Emerald50 = Color(0xFFECFDF5)
    val Emerald100 = Color(0xFFD1FAE5)
    val Emerald800 = Color(0xFF065F46)
    val Green120 = Color(0xFFDCFCE7)
    val Green40 = Color(0xFFF6FFED)
    val Green600 = Color(0xFF389E0D)
    val Green850 = Color(0xFF146E56)

    // Yellows & Ambers
    val Yellow400 = Color(0xFFFFD75E)
    val Amber600 = Color(0xFFE9A23B)
    val Amber50 = Color(0xFFFFF0DB)
    val Amber700 = Color(0xFFD97706)
    val Amber100 = Color(0xFFFFE8CC)
    val Amber800 = Color(0xFFB45309)
    val Amber150 = Color(0xFFFFE7B8)
    val Amber900 = Color(0xFF8B5814)
    val Amber40 = Color(0xFFFFFBEB)
    val Amber120 = Color(0xFFFEF3C7)
    val Amber850 = Color(0xFF92400E)
    val Amber200 = Color(0xFFFFDBA7)
    val Amber750 = Color(0xFFB67C28)
    val Amber500 = Color(0xFFF59E0B)
    val Amber60 = Color(0xFFFFF3CD)
    val Amber70 = Color(0xFFFFF7E6)
    val Amber720 = Color(0xFFD48806)

    // Oranges, Corals & Browns
    val Orange400 = Color(0xFFFFAA61)
    val Orange800 = Color(0xFF8A4B10)
    val Orange700 = Color(0xFFD46B08)
    val Coral600 = Color(0xFFD63F55)
    val Brown800 = Color(0xFF60362F)
    val Peach100 = Color(0xFFFFECE5)
    val Peach150 = Color(0xFFF2E6DE)
    val Peach50 = Color(0xFFFFF7F5)

    // Reds
    val Red400 = Color(0xFFE56B73)
    val Red600 = Color(0xFFD94B57)
    val Red800 = Color(0xFFAA2635)
    val Red500 = Color(0xFFFF4D4F)
    val Red100 = Color(0xFFFEE2E2)

    // Neutrals, Inks & Surfaces
    val WarmWhite = Color(0xFFFAF8F5)
    val WarmWhiteAlt = Color(0xFFFAF7F5)
    val CoolWhite = Color(0xFFFDFBFF)
    val WarmCream = Color(0xFFFFFBF8)
    val Neutral150 = Color(0xFFEEEAF2)
    val Neutral500 = Color(0xFF767676)
    val Neutral550 = Color(0xFF747775)
    val Neutral600 = Color(0xFF595959)
    val Neutral650 = Color(0xFF6B6570)
    val Neutral900 = Color(0xFF1F1F1F)
    val Ink950 = Color(0xFF15131D)
    val Ink900 = Color(0xFF151329)
    val Ink850 = Color(0xFF24183E)
    val Ink820 = Color(0xFF30233E)
    val Ink800 = Color(0xFF241B35)
    val Slate700 = Color(0xFF585066)
    val Slate650 = Color(0xFF62596D)
    val Slate620 = Color(0xFF665684)
    val Slate600 = Color(0xFF585C82)
    val Slate550 = Color(0xFF6C657E)
    val Slate500 = Color(0xFF7E7694)
    val Gray100 = Color(0xFFF3F4F6)
    val Gray150 = Color(0xFFEEEEEE)
    val Gray200 = Color(0xFFE5E7EB)
    val Gray600 = Color(0xFF666666)
    val Gray700 = Color(0xFF374151)
    val Lavender240 = Color(0xFFD9D1E4)
    val White = Color(0xFFFFFFFF)
    val BlackShadow14 = Color(0x14000000)

    // Home v2.0 Palette (DESIGN-TOKENS.json & reference/home-v2-approved.png)
    val CandyOrange400 = Color(0xFFFFB665)
    val CandyOrange600 = Color(0xFFF59145)
    val CandyOrange200 = Color(0xFFFFD49A)
    val CandyOrange100 = Color(0xFFFDE3BE)
    val CandyOrange800 = Color(0xFFC96A22)
    val Coral700 = Color(0xFFB83248)
    val Lavender350 = Color(0xFFBDA8EB)
    val Gold300 = Color(0xFFF4CF71)
    val Gold500 = Color(0xFFD9A940)
    val Ink810 = Color(0xFF30213F)
    val Slate610 = Color(0xFF665A75)
    val Purple850 = Color(0xFF6740B3)
    val Amber950 = Color(0xFF855200)
    val Orange450 = Color(0xFFFFA940)
    val Mint850 = Color(0xFF246B55)
    val Mint500 = Color(0xFF4CD195)
    val Teal800 = Color(0xFF24627A)
    val Slate400 = Color(0xFF9E97AD)

    // Home v2.2 - 12 Candy Gradient Themes & Neutral Placeholder (Chanko-Home-v2.2-Implementation)
    // 01 Tangerine (Signature CandyOrange: #FFD49A -> #FFB665 -> #F59145)
    val TangerineLight = Color(0xFFFFD49A)
    val TangerineMain = Color(0xFFFFB665)
    val TangerineDark = Color(0xFFF59145)

    // 02 Lavender
    val LavenderThemeLight = Color(0xFFE2CEFF)
    val LavenderThemeMain = Color(0xFFAC86F5)
    val LavenderThemeDark = Color(0xFF7950C9)

    // 03 Mint
    val MintThemeLight = Color(0xFFC8F7E3)
    val MintThemeMain = Color(0xFF70D9B0)
    val MintThemeDark = Color(0xFF299D79)

    // 04 Strawberry
    val StrawberryLight = Color(0xFFFFD1DB)
    val StrawberryMain = Color(0xFFF58FA9)
    val StrawberryDark = Color(0xFFD9557E)

    // 05 Sky Blue
    val SkyBlueLight = Color(0xFFD0E9FF)
    val SkyBlueMain = Color(0xFF7CBDF3)
    val SkyBlueDark = Color(0xFF408BCE)

    // 06 Lemon
    val LemonLight = Color(0xFFFFF1A9)
    val LemonMain = Color(0xFFF7D55E)
    val LemonDark = Color(0xFFD9A52E)

    // 07 Peach
    val PeachThemeLight = Color(0xFFFFE0C8)
    val PeachThemeMain = Color(0xFFFFAE8C)
    val PeachThemeDark = Color(0xFFE77B65)

    // 08 Grape
    val GrapeLight = Color(0xFFDCCBFA)
    val GrapeMain = Color(0xFFA184DE)
    val GrapeDark = Color(0xFF7051AE)

    // 09 Aqua
    val AquaLight = Color(0xFFC5F4F4)
    val AquaMain = Color(0xFF73D5DB)
    val AquaDark = Color(0xFF319FAF)

    // 10 Rose
    val RoseThemeLight = Color(0xFFFCE0F1)
    val RoseThemeMain = Color(0xFFEBA6D0)
    val RoseThemeDark = Color(0xFFC36BA5)

    // 11 Pistachio
    val PistachioLight = Color(0xFFE5F1BC)
    val PistachioMain = Color(0xFFB5D57A)
    val PistachioDark = Color(0xFF779F48)

    // 12 Blueberry
    val BlueberryLight = Color(0xFFD3DAFF)
    val BlueberryMain = Color(0xFF929FEB)
    val BlueberryDark = Color(0xFF596BBE)

    // Neutral Placeholder
    val NeutralPlaceholderBg = Color(0xFFF7F5FA)
    val NeutralPlaceholderBorder = Color(0xFFDDD6E8)
    val NeutralPlaceholderInk = Color(0xFF8A7D9D)
}

// UI-02 approved signal colors. Do not use as small text on white.
object CandyColors {
    // --- Core Signal & Brand Tokens (Backward Compatible Root Tokens) ---
    val primary = CandyPalette.Purple500
    val secondary = CandyPalette.Pink500
    val background = CandyPalette.WarmWhite
    val yellow = CandyPalette.Yellow400
    val mint = CandyPalette.Mint400
    val blue = CandyPalette.Blue400
    val orange = CandyPalette.Orange400
    val coolingOff = blue
    val readyForReview = yellow
    val purchased = CandyPalette.Blue600
    val skipped = mint
    val postponed = orange
    val budgetWarning = CandyPalette.Amber600
    val overBudget = CandyPalette.Red400
    val error = CandyPalette.Red600
    val success = CandyPalette.Mint700
    // Reference foregrounds, verified in phase-2-1-completion.md; PV-01 remains open.
    val ink = Color.Black
    val mutedInk = CandyPalette.Neutral600
    val outline = CandyPalette.Neutral500
    val errorText = CandyPalette.Red800
    // Splash brand typography colors
    val logoBrown = CandyPalette.Brown800
    val logoCoral = CandyPalette.Coral600

    // Home v5 Tokens (from UI-HANDOFF.md and tokens.json)
    val brandPurple = CandyPalette.Purple500
    val brandPink = CandyPalette.Pink500
    val backgroundLavender = CandyPalette.Lavender100
    val backgroundPink = CandyPalette.Pink100
    val calendarSurface = CandyPalette.Lavender50
    val calendarInnerSurface = CandyPalette.Lavender25
    // Add & Consider DESIGN-REVIEW.html disabled control surface.
    val addItemDisabledSurface = CandyPalette.Neutral150
    val primarySoft = CandyPalette.Lavender200
    val primaryInk = CandyPalette.Purple900
    val textPrimary = CandyPalette.Ink900
    val textSecondary = CandyPalette.Slate600
    val divider = CandyPalette.Lavender220
    val progressTrack = CandyPalette.Lavender210
    val readySurface = CandyPalette.Green100
    val readySurfaceEnd = CandyPalette.Green150
    val readyAction = CandyPalette.Green900
    val onReadyAction = CandyPalette.White
    val readyInk = CandyPalette.Green800
    val readyChip = CandyPalette.Green50
    val coolingBlue = CandyPalette.Blue100
    val coolingLavender = CandyPalette.Lavender160
    val coolingChip = CandyPalette.Lavender290
    val speechSurface = CandyPalette.Peach100

    // Home v2 Image-Aligned Semantic Tokens (approved-home-user-reference.png & 11-ASSET-DELIVERY.md)
    val homeScreenBackgroundTop = CandyPalette.Purple20
    val homeScreenBackgroundBottom = CandyPalette.Pink20
    val homeCardSurface = CandyPalette.CoolWhite
    val homeCardBorder = CandyPalette.Lavender110
    val dialogueBubbleSurface = CandyPalette.WarmCream
    val dialogueBubbleBorder = CandyPalette.Peach150
    val calendarDayInk = CandyPalette.Slate700

    // Quick Actions
    val quickCheckGradientStart = CandyPalette.Purple400
    val quickCheckGradientEnd = CandyPalette.Purple750
    val saveItemSurface = CandyPalette.Purple30
    val saveItemBorder = CandyPalette.Purple120
    val quickActionAskBg = CandyPalette.Purple700
    val quickActionAskBorder = CandyPalette.Purple300
    val quickActionAskText = CandyPalette.White
    val quickActionSaveBg = CandyPalette.Pink300
    val quickActionSaveBorder = CandyPalette.Pink400
    val quickActionSaveText = CandyPalette.Plum900

    // Candy Pop 3D Home Budget Snapshot
    val budgetSnapshotBg = CandyPalette.Lavender150
    val budgetSnapshotBorder = CandyPalette.Lavender300
    val budgetSnapshotAmount = CandyPalette.Purple800
    val budgetSnapshotSecondaryText = CandyPalette.Slate620
    val budgetSnapshotSpendingPanel = CandyPalette.White
    val budgetSnapshotBadgeBg = CandyPalette.Amber150
    val budgetSnapshotBadgeText = CandyPalette.Amber900
    val budgetSnapshotProgressTrack = CandyPalette.Purple80
    val budgetSnapshotProgressFill = CandyPalette.Purple500

    // Budget Snapshot
    val budgetWarningBadgeBg = CandyPalette.Amber50
    val budgetWarningBadgeInk = CandyPalette.Amber700
    val budgetReachedBadgeBg = CandyPalette.Amber100
    val budgetReachedBadgeInk = CandyPalette.Amber800
    val budgetOverBadgeBg = CandyPalette.Rose100
    val budgetOverBadgeInk = CandyPalette.Rose800
    val budgetNormalBadgeBg = CandyPalette.Green50
    val budgetNormalBadgeInk = CandyPalette.Green800

    // Your Decisions Rows & Pills
    val decisionReadyRowBg = CandyPalette.Emerald50
    val decisionReadyPillBg = CandyPalette.Emerald100
    val decisionReadyPillInk = CandyPalette.Emerald800
    val decisionWaitingRowBg = CandyPalette.Lavender40
    val decisionWaitingPillBg = CandyPalette.Lavender120
    val decisionWaitingPillInk = CandyPalette.Purple900
    val decisionConsideringRowBg = CandyPalette.Amber40
    val decisionConsideringPillBg = CandyPalette.Amber120
    val decisionConsideringPillInk = CandyPalette.Amber850
    val decisionSavedRowBg = CandyPalette.Gray100
    val decisionSavedPillBg = CandyPalette.Gray200
    val decisionSavedPillInk = CandyPalette.Gray700

    // Candy Item Tile Semantic Tokens
    val attentionTileReadyBg = CandyPalette.Mint50
    val attentionTileReadyBorder = CandyPalette.Mint200
    val attentionTileReadyAccent = CandyPalette.Mint800

    val attentionTileWaitingBg = CandyPalette.Blue50
    val attentionTileWaitingBorder = CandyPalette.Blue200
    val attentionTileWaitingAccent = CandyPalette.Blue500

    val attentionTileConsideringBg = CandyPalette.Lavender130
    val attentionTileConsideringBorder = CandyPalette.Lavender280
    val attentionTileConsideringAccent = CandyPalette.Purple700

    val attentionTileSavedBg = CandyPalette.Amber50
    val attentionTileSavedBorder = CandyPalette.Amber200
    val attentionTileSavedAccent = CandyPalette.Amber750

    // Observation & Global Sheet
    val observationIconTileBg = CandyPalette.Lavender60
    val observationBulbYellow = CandyPalette.Amber500
    val globalSheetSaveIconTileBg = CandyPalette.Pink50
    val globalSheetSaveIconInk = CandyPalette.Pink700

    // =========================================================================
    // Feature Sub-objects (Scoped Semantic Tokens)
    // =========================================================================

    /** Feature tokens for Budget & Wallet screens, components, and dialogs */
    object Wallet {
        val purplePrimary = CandyPalette.Purple600
        val textPrimary = CandyPalette.Ink850
        val textMuted = CandyPalette.Slate500
        val textMutedAlt = CandyPalette.Slate550
        val dialogBg = CandyPalette.WarmWhiteAlt
        val shadowSpot = CandyPalette.BlackShadow14
        val cycleIconBg = CandyPalette.Pink60
        val progressTrack = CandyPalette.Lavender85
        val progressGradientStart = CandyPalette.Purple450
        val cardInnerBg = CandyPalette.Lavender20
        val badgePillBg = CandyPalette.Amber120
        val badgeIncreaseBg = CandyPalette.Green120
        val badgeDecreaseBg = CandyPalette.Red100
        val iconContainerBg = CandyPalette.Lavender90
        val iconContainerBgAlt = CandyPalette.Lavender85
        val unfocusedBorder = CandyPalette.Lavender230
        val presetTagBg = CandyPalette.Lavender70
        val deleteErrorText = CandyPalette.Red500
        val warningText = CandyPalette.Orange700
    }

    /** Feature tokens for Chat screens, bubbles, and sheets */
    object Chat {
        val userBubbleBg = CandyPalette.Lavender110
        val userBubbleBorder = CandyPalette.Lavender250
        val userBubbleText = CandyPalette.Ink800
        val assistantBubbleBg = CandyPalette.Lavender25
        val assistantBubbleText = CandyPalette.Ink800
        val itemIconBg = CandyPalette.Lavender30
        val itemCardBg = CandyPalette.Lavender30
        val contextHeaderBg = CandyPalette.Peach100
        val selectedContextBg = CandyPalette.Lavender80
        val unselectedContextBg = CandyPalette.WarmWhite
        val pendingBannerBg = CandyPalette.Amber60
        val headerGradientStart = CandyPalette.Lavender100
        val headerGradientEnd = CandyPalette.Pink100
        val searchFieldBg = CandyPalette.Lavender100
        val tagArchivedBg = CandyPalette.Gray150
        val tagArchivedText = CandyPalette.Gray600
        val tagConsideringBg = CandyPalette.Amber70
        val tagConsideringText = CandyPalette.Amber720
        val tagReadyBg = CandyPalette.Teal50
        val tagReadyText = CandyPalette.Teal700
        val tagPurchasedBg = CandyPalette.Green40
        val tagPurchasedText = CandyPalette.Green600
        val tagFreeformBg = CandyPalette.Lavender100
    }

    /** Feature tokens for UrlInputDialog modal and extraction flow */
    object UrlImport {
        val ink = CandyPalette.VioletDeep
        val purple = CandyPalette.Violet800
        val outline = CandyPalette.Violet200
        val dialogSurface = CandyPalette.Peach50
        val mutedText = CandyPalette.VioletMutedDark
        val fieldContainer = CandyPalette.VioletSurface
        val disabledButtonBg = CandyPalette.Violet50
        val submitGradientStart = CandyPalette.Violet500
        val submitGradientEnd = CandyPalette.Violet700
        val disabledGradient = CandyPalette.Violet100
        val disabledButtonText = CandyPalette.VioletMuted
    }

    /** Feature tokens for Authentication (LoginScreen) and Initial Setup (SetupTheme) */
    object Auth {
        val loginPlum = CandyPalette.Plum950
        val googleButtonBorder = CandyPalette.Neutral550
        val googleButtonText = CandyPalette.Neutral900
        val legalText = CandyPalette.Neutral650
        val ink = CandyPalette.Ink820
        val secondary = CandyPalette.Slate650
        val onPurple = CandyPalette.Ink950
        val stepDotInactive = CandyPalette.Lavender240
    }

    /** Shared status tokens for item states, badges, and countdown indicators */
    object Status {
        val accentBlue = CandyPalette.Blue800
        val coolingOffText = CandyPalette.Blue800
        val postponedText = CandyPalette.Orange800
        val purchasedText = CandyPalette.Blue900
        val skippedText = CandyPalette.Green850
    }

    /** Home v2.0 Semantic Tokens (Budget Credit Card, Item Journey & Observation — DESIGN-TOKENS.json) */
    object HomeV2 {
        val background = CandyPalette.Purple30
        val textPrimary = CandyPalette.Ink810
        val textSecondary = CandyPalette.Slate610
        val candyOrange = CandyPalette.CandyOrange400
        val candyOrangeShadow = CandyPalette.CandyOrange600
        val candyOrangeHighlight = CandyPalette.CandyOrange200
        val candyOrangeTrack = CandyPalette.CandyOrange100
        val candyOrangeProgressFill = CandyPalette.CandyOrange800
        val coral = CandyPalette.Coral700
        val coralSoft = CandyPalette.Rose100
        val lavenderConnector = CandyPalette.Lavender350
        val chipGold = CandyPalette.Gold300
        val chipGoldStroke = CandyPalette.Gold500
        val headerFrostedSurface = CandyPalette.Purple20
        val headerBorderDivider = CandyPalette.Lavender110

        // 6 Journey Node Statuses (Ring + Badge + Label Ink)
        val statusSavedInk = CandyPalette.Slate610
        val statusSavedRing = CandyPalette.Purple500
        val statusConsideringInk = CandyPalette.Purple850
        val statusConsideringRing = CandyPalette.Purple400
        val statusWaitingInk = CandyPalette.Amber950
        val statusWaitingRing = CandyPalette.Orange450
        val statusReadyInk = CandyPalette.Mint850
        val statusReadyRing = CandyPalette.Mint500
        val statusPurchasedInk = CandyPalette.Teal800
        val statusPurchasedRing = CandyPalette.Blue500
        val statusSkippedInk = CandyPalette.Slate610
        val statusSkippedRing = CandyPalette.Slate400
    }

    /** Home v2.2 - 12 Candy Gradient Themes & Budget Carousel Semantic Tokens */
    object BudgetThemes {
        data class GradientTheme(
            val id: Int,
            val name: String,
            val light: Color,
            val main: Color,
            val dark: Color
        )

        val Tangerine = GradientTheme(0, "Tangerine", CandyPalette.TangerineLight, CandyPalette.TangerineMain, CandyPalette.TangerineDark)
        val Lavender = GradientTheme(1, "Lavender", CandyPalette.LavenderThemeLight, CandyPalette.LavenderThemeMain, CandyPalette.LavenderThemeDark)
        val Mint = GradientTheme(2, "Mint", CandyPalette.MintThemeLight, CandyPalette.MintThemeMain, CandyPalette.MintThemeDark)
        val Strawberry = GradientTheme(3, "Strawberry", CandyPalette.StrawberryLight, CandyPalette.StrawberryMain, CandyPalette.StrawberryDark)
        val SkyBlue = GradientTheme(4, "Sky Blue", CandyPalette.SkyBlueLight, CandyPalette.SkyBlueMain, CandyPalette.SkyBlueDark)
        val Lemon = GradientTheme(5, "Lemon", CandyPalette.LemonLight, CandyPalette.LemonMain, CandyPalette.LemonDark)
        val Peach = GradientTheme(6, "Peach", CandyPalette.PeachThemeLight, CandyPalette.PeachThemeMain, CandyPalette.PeachThemeDark)
        val Grape = GradientTheme(7, "Grape", CandyPalette.GrapeLight, CandyPalette.GrapeMain, CandyPalette.GrapeDark)
        val Aqua = GradientTheme(8, "Aqua", CandyPalette.AquaLight, CandyPalette.AquaMain, CandyPalette.AquaDark)
        val Rose = GradientTheme(9, "Rose", CandyPalette.RoseThemeLight, CandyPalette.RoseThemeMain, CandyPalette.RoseThemeDark)
        val Pistachio = GradientTheme(10, "Pistachio", CandyPalette.PistachioLight, CandyPalette.PistachioMain, CandyPalette.PistachioDark)
        val Blueberry = GradientTheme(11, "Blueberry", CandyPalette.BlueberryLight, CandyPalette.BlueberryMain, CandyPalette.BlueberryDark)

        val allThemes = listOf(
            Tangerine, Lavender, Mint, Strawberry, SkyBlue, Lemon,
            Peach, Grape, Aqua, Rose, Pistachio, Blueberry
        )

        fun forOrdinal(ordinal: Int): GradientTheme {
            val index = Math.floorMod(ordinal, 12)
            return allThemes[index]
        }

        // Neutral Placeholder Tokens
        val placeholderBg = CandyPalette.NeutralPlaceholderBg
        val placeholderBorder = CandyPalette.NeutralPlaceholderBorder
        val placeholderInk = CandyPalette.NeutralPlaceholderInk

        // OverBudget Semantic Coral
        val coral = CandyPalette.Coral700
    }
}
