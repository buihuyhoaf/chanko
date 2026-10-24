package com.hoabh.chanko.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.screens.profile.components.AccountIdentityCard
import com.hoabh.chanko.presentation.screens.profile.components.CozyCornerCard
import com.hoabh.chanko.presentation.screens.profile.components.GoogleAccountCard
import com.hoabh.chanko.presentation.screens.profile.components.InformationRow
import com.hoabh.chanko.presentation.screens.profile.components.PersonalityRow
import com.hoabh.chanko.presentation.screens.profile.components.ProfileTopBar
import com.hoabh.chanko.presentation.screens.profile.components.ReadyForReviewRow
import com.hoabh.chanko.presentation.screens.profile.components.SettingsSection
import com.hoabh.chanko.presentation.screens.profile.components.SignOutSection
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace

/**
 * Màn hình stateless Profile & Settings:
 * - Gộp thông tin tài khoản và toàn bộ cấu hình hệ thống
 * - Không có Bottom Navigation
 * - Hỗ trợ tablet/màn hình rộng (max width 600dp canh giữa)
 * - Tự động thích ứng Font Scale 200% và Safe System Insets
 */
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onEvent: (ProfileUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSignOutDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(CandyColors.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            ProfileTopBar(
                onBack = { onEvent(ProfileUiEvent.OnBackClicked) }
            )
        },
        containerColor = CandyColors.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .verticalScroll(scrollState)
                    .padding(horizontal = CandySpace.screen, vertical = CandySpace.sm),
                verticalArrangement = Arrangement.spacedBy(CandySpace.lg)
            ) {
                // 1. Thẻ tài khoản tóm tắt
                AccountIdentityCard(account = state.account)

                // 2. Cozy Corner (Mascot)
                CozyCornerCard(
                    onTap = { onEvent(ProfileUiEvent.OnCozyCornerTapped) }
                )

                // 3. Nhóm Linh thú đồng hành
                SettingsSection(title = stringResource(R.string.profile_section_mascot)) {
                    PersonalityRow(
                        personality = state.committedPersonality,
                        onClick = { onEvent(ProfileUiEvent.OnPersonalityRowClicked) }
                    )
                }

                // 4. Nhóm Thông báo
                SettingsSection(title = stringResource(R.string.profile_section_notifications)) {
                    ReadyForReviewRow(
                        enabled = state.isReadyForReviewNotificationEnabled,
                        onToggle = { onEvent(ProfileUiEvent.OnReadyForReviewToggled(it)) }
                    )
                }

                // 5. Thẻ tài khoản Google chi tiết (Read-only)
                GoogleAccountCard(email = state.account.email)

                // 6. Nhóm Thông tin & Hỗ trợ
                SettingsSection(title = stringResource(R.string.profile_section_information)) {
                    InformationRow(
                        title = stringResource(R.string.profile_row_privacy),
                        icon = Icons.Rounded.Security,
                        onClick = { onEvent(ProfileUiEvent.OnPrivacyPolicyClicked) }
                    )
                    HorizontalDivider(color = CandyColors.divider.copy(alpha = 0.5f))
                    InformationRow(
                        title = stringResource(R.string.profile_row_about),
                        icon = Icons.Rounded.Info,
                        onClick = { onEvent(ProfileUiEvent.OnAboutClicked) },
                        trailingText = stringResource(R.string.profile_app_version, state.appVersion)
                    )
                }

                // 7. Đăng xuất
                SignOutSection(
                    onSignOutClick = { showSignOutDialog = true },
                    showDialog = showSignOutDialog,
                    onConfirmSignOut = {
                        showSignOutDialog = false
                        onEvent(ProfileUiEvent.OnConfirmSignOut)
                    },
                    onDismissDialog = { showSignOutDialog = false }
                )

                Spacer(modifier = Modifier.height(CandySpace.section))
            }

            if (state.isLoading || state.isSignOutInProgress) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CandyColors.background.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CandyColors.brandPurple)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    ProfileScreen(
        state = ProfileUiState(
            account = ProfileAccountUiModel(
                displayName = "Nguyễn Văn Hoa",
                email = "hoa.nguyen@gmail.com"
            ),
            committedPersonality = CatPersonality.Sassy
        ),
        onEvent = {}
    )
}
