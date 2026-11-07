package com.hoabh.chanko.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R

/**
 * Design Tokens for Chanko Chat UI v2.2.
 * Sourced directly from Chanko-Chat-UI-v2.2/tokens/design-tokens.json.
 */
object ChatTokens {
    const val VERSION = "2.2"

    // Colors sourced from CandyColors.Chat
    val primary = CandyColors.Chat.primary
    val primaryTextAndCTA = CandyColors.Chat.primaryTextAndCTA
    val body = CandyColors.Chat.body
    val secondary = CandyColors.Chat.secondary
    val surface = CandyColors.Chat.surface
    val backgroundStart = CandyColors.Chat.backgroundStart
    val backgroundEnd = CandyColors.Chat.backgroundEnd
    val screenBg = CandyColors.Chat.screenBg
    val userBubble = CandyColors.Chat.userBubble
    val chankoBubble = CandyColors.Chat.chankoBubble
    val bubbleBorder = CandyColors.Chat.bubbleBorder
    val cardBorder = CandyColors.Chat.cardBorder
    val divider = CandyColors.Chat.divider
    val alertRed = CandyColors.Chat.alertRed
    val coolingElapsedBg = CandyColors.Chat.coolingElapsedBg
    val coolingElapsedText = CandyColors.Chat.coolingElapsedText
    val coolingActiveBg = CandyColors.Chat.coolingActiveBg
    val coolingActiveBorder = CandyColors.Chat.coolingActiveBorder
    val neutralMuted = CandyColors.Chat.neutralMuted
    val grayIconBg = CandyColors.Chat.grayIconBg
    val cardDeletedBg = CandyColors.Chat.cardDeletedBg
    val cardBorderDeleted = CandyColors.Chat.cardBorderDeleted
    val composerInputBg = CandyColors.Chat.composerInputBg
    val disabledBg = CandyColors.Chat.disabledBg
    val disabledBorder = CandyColors.Chat.disabledBorder
    val refBorder = CandyColors.Chat.refBorder
    val sendDisabled = CandyColors.Chat.sendDisabled
    val indicatorTrack = CandyColors.Chat.indicatorTrack
    val indicatorUnknown = CandyColors.Chat.indicatorUnknown
    val radioUnselected = CandyColors.Chat.radioUnselected
    val listCardBorder = CandyColors.Chat.listCardBorder
    val snapshotCardBg = CandyColors.Chat.snapshotCardBg

    // Dimensions
    val minimumTouchDp = 48.dp
    val iconDp = 24.dp
    val avatarChatDp = 40.dp
    val avatarBubbleDp = 32.dp

    // Upstream Pinned Nunito 3.602 Font Family for Chat
    val chatNunitoFamily = FontFamily(
        Font(R.font.chat_nunito_400, weight = FontWeight(400)),
        Font(R.font.chat_nunito_600, weight = FontWeight(600)),
        Font(R.font.chat_nunito_700, weight = FontWeight(700)),
        Font(R.font.chat_nunito_800, weight = FontWeight(800))
    )

    // Typography
    val heading = TextStyle(
        fontFamily = chatNunitoFamily,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.Bold,
        color = body
    )

    val bodyMedium = TextStyle(
        fontFamily = chatNunitoFamily,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Normal,
        color = body
    )

    val bodySemiBold = TextStyle(
        fontFamily = chatNunitoFamily,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.SemiBold,
        color = body
    )

    val labelSmall = TextStyle(
        fontFamily = chatNunitoFamily,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Medium,
        color = secondary
    )
}
