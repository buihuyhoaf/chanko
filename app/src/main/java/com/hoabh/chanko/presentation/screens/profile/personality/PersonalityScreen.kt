package com.hoabh.chanko.presentation.screens.profile.personality

import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.screens.profile.CatPersonality
import com.hoabh.chanko.presentation.screens.profile.personality.components.PersonalityOptionCard
import com.hoabh.chanko.presentation.screens.profile.personality.components.PersonalityPreviewCard
import com.hoabh.chanko.presentation.screens.profile.personality.components.PersonalityTopBar
import com.hoabh.chanko.presentation.screens.profile.personality.components.UnsavedChangesDialog
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandyShape
import com.hoabh.chanko.ui.theme.CandySize
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.Nunito

/**
 * Màn hình chọn tính cách linh thú (PersonalityScreen):
 * - 3 Lựa chọn: Friendly, Sassy, Savage
 * - Thẻ Preview xem trước offline phản hồi mẫu
 * - Nút "Lưu thay đổi" (Save) ở cuối: chỉ enable khi có thay đổi (dirty) và online
 * - Bấm Back khi dirty hiển thị UnsavedChangesDialog
 */
@Composable
fun PersonalityScreen(
    state: PersonalityUiState,
    onEvent: (PersonalityUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    // Intercept hardware/system back button
    BackHandler(enabled = true) {
        onEvent(PersonalityUiEvent.OnBackClicked)
    }

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(CandyColors.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            PersonalityTopBar(
                onBack = { onEvent(PersonalityUiEvent.OnBackClicked) }
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
                verticalArrangement = Arrangement.spacedBy(CandySpace.md)
            ) {
                // 3 Thẻ chọn Radio
                CatPersonality.entries.forEach { personality ->
                    PersonalityOptionCard(
                        personality = personality,
                        isSelected = state.draftPersonality == personality,
                        onSelect = { onEvent(PersonalityUiEvent.OnSelectPersonality(personality)) }
                    )
                }

                Spacer(modifier = Modifier.height(CandySpace.sm))

                // Thẻ Preview câu thoại mẫu
                PersonalityPreviewCard(personality = state.draftPersonality)

                if (!state.isOnline) {
                    Text(
                        text = stringResource(R.string.profile_network_required),
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = CandyColors.error,
                        modifier = Modifier.padding(horizontal = CandySpace.xs)
                    )
                }

                Spacer(modifier = Modifier.weight(1f, fill = false))
                Spacer(modifier = Modifier.height(CandySpace.lg))

                // Nút Lưu thay đổi (Save)
                Button(
                    onClick = { onEvent(PersonalityUiEvent.OnSaveClicked) },
                    enabled = state.canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(CandySize.primaryButton),
                    shape = CandyShape.primaryButton,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CandyColors.brandPurple,
                        contentColor = Color.White,
                        disabledContainerColor = CandyColors.progressTrack,
                        disabledContentColor = CandyColors.mutedInk
                    )
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.height(24.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.profile_action_save),
                            fontFamily = Nunito,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(CandySpace.section))
            }

            // Dialog khi Back dirty
            if (state.showUnsavedChangesDialog) {
                UnsavedChangesDialog(
                    onKeepEditing = { onEvent(PersonalityUiEvent.OnKeepEditing) },
                    onDiscard = { onEvent(PersonalityUiEvent.OnDiscardChanges) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PersonalityScreenPreview() {
    PersonalityScreen(
        state = PersonalityUiState(
            committedPersonality = CatPersonality.Sassy,
            draftPersonality = CatPersonality.Friendly,
            isOnline = true
        ),
        onEvent = {}
    )
}
