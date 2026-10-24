package com.hoabh.chanko.presentation.screens.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.screens.profile.CatPersonality
import com.hoabh.chanko.presentation.screens.profile.ProfileAccountUiModel
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandyElevation
import com.hoabh.chanko.ui.theme.CandyShape
import com.hoabh.chanko.ui.theme.CandySize
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.Nunito

/**
 * TopBar tinh gọn của màn hình Profile & Settings
 */
@Composable
fun ProfileTopBar(
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
            text = stringResource(R.string.profile_settings_title),
            fontFamily = Nunito,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = CandyColors.textPrimary,
            modifier = Modifier.semantics { heading() }
        )
    }
}

/**
 * Thẻ Identity tóm tắt tài khoản người dùng
 */
@Composable
fun AccountIdentityCard(
    account: ProfileAccountUiModel,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CandyShape.card,
        color = Color.White,
        shadowElevation = CandyElevation.low
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CandySpace.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Placeholder / Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(CandyColors.primarySoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = CandyColors.primaryInk,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(CandySpace.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.displayName.ifBlank { stringResource(R.string.profile_default_display_name) },
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = CandyColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = CandyColors.success,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.profile_account_status_synced),
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = CandyColors.textSecondary
                    )
                }
            }
        }
    }
}

/**
 * Nhóm thiết lập có tiêu đề Section
 */
@Composable
fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontFamily = Nunito,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = CandyColors.textSecondary,
            modifier = Modifier
                .padding(horizontal = CandySpace.sm, vertical = CandySpace.xs)
                .semantics { heading() }
        )

        Spacer(modifier = Modifier.height(CandySpace.xs))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = CandyShape.card,
            color = Color.White,
            shadowElevation = CandyElevation.low
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

/**
 * Dòng cấu hình Tính cách mèo
 */
@Composable
fun PersonalityRow(
    personality: CatPersonality,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val personalityLabel = when (personality) {
        CatPersonality.Friendly -> stringResource(R.string.profile_personality_friendly)
        CatPersonality.Sassy -> stringResource(R.string.profile_personality_sassy)
        CatPersonality.Savage -> stringResource(R.string.profile_personality_savage)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = CandySpace.lg, vertical = CandySpace.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Pets,
            contentDescription = null,
            tint = CandyColors.brandPurple,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(CandySpace.md))

        Text(
            text = stringResource(R.string.profile_personality_row_title),
            fontFamily = Nunito,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = CandyColors.textPrimary,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = personalityLabel,
            fontFamily = Nunito,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = CandyColors.brandPurple
        )

        Spacer(modifier = Modifier.width(CandySpace.xs))

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = CandyColors.textSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Dòng bật/tắt nhắc nhở Ready for Review
 */
@Composable
fun ReadyForReviewRow(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .padding(horizontal = CandySpace.lg, vertical = CandySpace.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Notifications,
            contentDescription = null,
            tint = CandyColors.brandPurple,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(CandySpace.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.profile_ready_for_review_title),
                fontFamily = Nunito,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = CandyColors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.profile_ready_for_review_desc),
                fontFamily = Nunito,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                color = CandyColors.textSecondary
            )
        }

        Spacer(modifier = Modifier.width(CandySpace.sm))

        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = CandyColors.brandPurple
            )
        )
    }
}

/**
 * Card tài khoản Google (Read-only)
 */
@Composable
fun GoogleAccountCard(
    email: String,
    modifier: Modifier = Modifier
) {
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
            Text(
                text = stringResource(R.string.profile_google_account_title),
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = CandyColors.textPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = email.ifBlank { "google-account@gmail.com" },
                fontFamily = Nunito,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = CandyColors.textSecondary
            )

            Spacer(modifier = Modifier.height(CandySpace.xs))

            Text(
                text = stringResource(R.string.profile_google_provider_label),
                fontFamily = Nunito,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = CandyColors.brandPurple
            )
        }
    }
}

/**
 * Dòng thông tin / chính sách
 */
@Composable
fun InformationRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingText: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = CandySpace.lg, vertical = CandySpace.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CandyColors.textSecondary,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(CandySpace.md))

        Text(
            text = title,
            fontFamily = Nunito,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = CandyColors.textPrimary,
            modifier = Modifier.weight(1f)
        )

        if (trailingText != null) {
            Text(
                text = trailingText,
                fontFamily = Nunito,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = CandyColors.textSecondary
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = CandyColors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Nút Đăng xuất và Dialog xác nhận
 */
@Composable
fun SignOutSection(
    onSignOutClick: () -> Unit,
    showDialog: Boolean,
    onConfirmSignOut: () -> Unit,
    onDismissDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(CandyShape.card)
            .clickable(role = Role.Button, onClick = onSignOutClick),
        shape = CandyShape.card,
        color = Color.White,
        shadowElevation = CandyElevation.low
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = CandySpace.lg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.profile_action_sign_out),
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = CandyColors.error
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismissDialog,
            title = {
                Text(
                    text = stringResource(R.string.profile_sign_out_dialog_title),
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.profile_sign_out_dialog_message),
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Normal
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirmSignOut) {
                    Text(
                        text = stringResource(R.string.profile_action_sign_out),
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        color = CandyColors.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDialog) {
                    Text(
                        text = stringResource(R.string.profile_action_cancel),
                        fontFamily = Nunito,
                        fontWeight = FontWeight.SemiBold,
                        color = CandyColors.textSecondary
                    )
                }
            },
            shape = CandyShape.dialog
        )
    }
}
