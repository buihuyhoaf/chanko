package com.hoabh.chanko.presentation.screens.profile.personality.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.screens.profile.CatPersonality
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandyElevation
import com.hoabh.chanko.ui.theme.CandyShape
import com.hoabh.chanko.ui.theme.CandySize
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.Nunito

/**
 * TopBar của màn hình chọn tính cách
 */
@Composable
fun PersonalityTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = CandySpace.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(CandySize.touchTarget)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.profile_action_back),
                tint = CandyColors.textPrimary
            )
        }

        Spacer(modifier = Modifier.width(CandySpace.xs))

        Text(
            text = stringResource(R.string.profile_personality_title),
            fontFamily = Nunito,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = CandyColors.textPrimary,
            modifier = Modifier.semantics { heading() }
        )
    }
}

/**
 * Thẻ Radio chọn từng tính cách (Friendly / Sassy / Savage)
 */
@Composable
fun PersonalityOptionCard(
    personality: CatPersonality,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (titleRes, descRes) = when (personality) {
        CatPersonality.Friendly -> Pair(R.string.profile_personality_friendly, R.string.profile_personality_friendly_desc)
        CatPersonality.Sassy -> Pair(R.string.profile_personality_sassy, R.string.profile_personality_sassy_desc)
        CatPersonality.Savage -> Pair(R.string.profile_personality_savage, R.string.profile_personality_savage_desc)
    }

    val borderColor = if (isSelected) CandyColors.brandPurple else Color.Transparent
    val backgroundColor = if (isSelected) CandyColors.backgroundLavender.copy(alpha = 0.5f) else Color.White

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(CandyShape.card)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = borderColor,
                shape = CandyShape.card
            )
            .selectable(
                selected = isSelected,
                onClick = onSelect,
                role = Role.RadioButton
            ),
        shape = CandyShape.card,
        color = backgroundColor,
        shadowElevation = if (isSelected) CandyElevation.low else CandyElevation.low
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CandySpace.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = CandyColors.brandPurple,
                    unselectedColor = CandyColors.outline
                )
            )

            Spacer(modifier = Modifier.width(CandySpace.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(titleRes),
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = CandyColors.textPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(descRes),
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = CandyColors.textSecondary
                )
            }
        }
    }
}

/**
 * Thẻ xem trước (Preview) câu thoại mẫu offline của tính cách đang chọn
 */
@Composable
fun PersonalityPreviewCard(
    personality: CatPersonality,
    modifier: Modifier = Modifier
) {
    val sampleQuoteRes = when (personality) {
        CatPersonality.Friendly -> R.string.profile_personality_preview_friendly
        CatPersonality.Sassy -> R.string.profile_personality_preview_sassy
        CatPersonality.Savage -> R.string.profile_personality_preview_savage
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CandyShape.card,
        color = Color.White,
        shadowElevation = CandyElevation.low
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CandySpace.lg)
        ) {
            Box(
                modifier = Modifier
                    .clip(CandyShape.chip)
                    .background(CandyColors.primarySoft)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.profile_personality_preview_badge),
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = CandyColors.primaryInk
                )
            }

            Spacer(modifier = Modifier.height(CandySpace.md))

            Text(
                text = stringResource(sampleQuoteRes),
                fontFamily = Nunito,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = CandyColors.textPrimary
            )
        }
    }
}

/**
 * Dialog cảnh báo thay đổi chưa lưu khi bấm Back
 */
@Composable
fun UnsavedChangesDialog(
    onKeepEditing: () -> Unit,
    onDiscard: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onKeepEditing,
        title = {
            Text(
                text = stringResource(R.string.profile_unsaved_changes_title),
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = stringResource(R.string.profile_unsaved_changes_message),
                fontFamily = Nunito,
                fontWeight = FontWeight.Normal
            )
        },
        confirmButton = {
            TextButton(onClick = onDiscard) {
                Text(
                    text = stringResource(R.string.profile_action_discard),
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    color = CandyColors.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onKeepEditing) {
                Text(
                    text = stringResource(R.string.profile_action_keep_editing),
                    fontFamily = Nunito,
                    fontWeight = FontWeight.SemiBold,
                    color = CandyColors.textSecondary
                )
            }
        },
        shape = CandyShape.dialog
    )
}
