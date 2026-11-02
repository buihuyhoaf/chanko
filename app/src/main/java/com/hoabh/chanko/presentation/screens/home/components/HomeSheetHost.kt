package com.hoabh.chanko.presentation.screens.home.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.presentation.screens.home.ContextKind
import com.hoabh.chanko.presentation.screens.home.DecisionUi
import com.hoabh.chanko.presentation.screens.home.HomeSheet
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandyElevation
import com.hoabh.chanko.ui.theme.CandyShape
import com.hoabh.chanko.ui.theme.CandySize
import com.hoabh.chanko.ui.theme.CandySpace

private val DragHandleWidth = 36.dp
private val DragHandleHeight = 4.dp
private val SheetCloseVisualSize = 32.dp
private val SheetItemRowRadius = 14.dp
private val ScrimColor = Color.Black.copy(alpha = 0.42f)

/**
 * Contextual Modal Bottom Sheet Host (Candy Pop 3D v1.0 — AC-04, AC-05, AC-06, AC-14, AC-15, AC-19):
 * - Đảm bảo tối đa 1 sheet hoạt động tại một thời điểm.
 * - Phục vụ 4 vật thể Little Room ("Đang chờ", "Cần xem lại", "Ngân sách", "Đã bỏ qua")
 *   và giải thích chi tiết "Góc nhìn từ Chanko" (ContextKind.Observation).
 * - Hiển thị empty state theo ngữ cảnh khi nhóm có 0 món (AC-05) và cô lập lỗi/loading (AC-15).
 * - Không tự mở lại sau khi Android hủy tiến trình (AC-19).
 */
@Composable
fun HomeSheetHost(
    activeSheet: HomeSheet.Contextual?,
    onDismiss: () -> Unit,
    onContextPrimaryAction: (ContextKind, String?) -> Unit,
    modifier: Modifier = Modifier,
    onItemClick: (String) -> Unit = {}
) {
    if (activeSheet == null) return

    BackHandler(enabled = true, onBack = onDismiss)

    val navBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val scrimInteractionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Modal Scrim Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScrimColor)
                .clickable(
                    interactionSource = scrimInteractionSource,
                    indication = null,
                    onClick = onDismiss
                )
        )

        // Sheet Surface
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                ),
            shape = CandyShape.sheet,
            color = CandyColors.homeCardSurface,
            tonalElevation = CandyElevation.high,
            shadowElevation = CandyElevation.high
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = CandySpace.lg,
                        top = CandySpace.md,
                        end = CandySpace.lg,
                        bottom = CandySpace.lg + navBarBottomPadding
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Drag Handle
                Box(
                    modifier = Modifier
                        .size(width = DragHandleWidth, height = DragHandleHeight)
                        .clip(CircleShape)
                        .background(CandyColors.divider)
                )

                Spacer(modifier = Modifier.height(CandySpace.md))

                ContextualSheetContent(
                    sheet = activeSheet,
                    onDismiss = onDismiss,
                    onPrimaryAction = {
                        onContextPrimaryAction(activeSheet.kind, activeSheet.entityId)
                    },
                    onItemClick = onItemClick
                )
            }
        }
    }
}

@Composable
private fun ContextualSheetContent(
    sheet: HomeSheet.Contextual,
    onDismiss: () -> Unit,
    onPrimaryAction: () -> Unit,
    onItemClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CandySpace.md)
    ) {
        val headerTitle = if (sheet.isEntityMissing) {
            stringResource(R.string.home_context_sheet_missing_title)
        } else {
            sheet.title
        }

        SheetHeaderRow(
            title = headerTitle,
            onDismiss = onDismiss
        )

        when {
            sheet.isLoading -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = CandySpace.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CandySpace.md)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = CandyColors.brandPurple,
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = stringResource(R.string.home_section_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = CandyColors.textSecondary
                    )
                }
            }

            !sheet.errorMessage.isNullOrBlank() -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(SheetItemRowRadius),
                    color = CandyColors.primarySoft.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, CandyColors.homeCardBorder)
                ) {
                    Text(
                        text = sheet.errorMessage,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = CandyColors.textPrimary,
                        modifier = Modifier.padding(CandySpace.md)
                    )
                }
            }

            sheet.isEntityMissing -> {
                Text(
                    text = stringResource(R.string.home_context_sheet_missing_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = CandyColors.textSecondary
                )
            }

            sheet.kind == ContextKind.Observation -> {
                ObservationExplanationSheetBody(sheet = sheet)
            }

            else -> {
                if (!sheet.highlightValue.isNullOrBlank()) {
                    Surface(
                        shape = CandyShape.chip,
                        color = CandyColors.primarySoft
                    ) {
                        Text(
                            text = if (sheet.kind == ContextKind.Wallet) {
                                sheet.highlightValue
                            } else {
                                "${sheet.highlightValue} món"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = CandyColors.primaryInk,
                            modifier = Modifier.padding(
                                horizontal = CandySpace.md,
                                vertical = CandySpace.xs
                            )
                        )
                    }
                }

                Text(
                    text = sheet.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CandyColors.textSecondary
                )

                // AC-05: Hiển thị empty state theo đúng ngữ cảnh khi nhóm không có dữ liệu
                if (sheet.isEmpty) {
                    val emptyMessage = when (sheet.kind) {
                        ContextKind.Hourglass -> stringResource(R.string.home_context_sheet_empty_waiting)
                        ContextKind.Calendar -> stringResource(R.string.home_context_sheet_empty_review)
                        ContextKind.PauseJar -> stringResource(R.string.home_context_sheet_empty_skipped)
                        else -> sheet.subtitle
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(SheetItemRowRadius),
                        color = CandyColors.primarySoft.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, CandyColors.homeCardBorder)
                    ) {
                        Text(
                            text = emptyMessage,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = CandyColors.textSecondary,
                            modifier = Modifier.padding(CandySpace.md)
                        )
                    }
                } else if (sheet.items.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(CandySpace.xs)
                    ) {
                        sheet.items.take(3).forEach { item ->
                            SheetPreviewItemRow(
                                item = item,
                                onClick = { onItemClick(item.id) }
                            )
                        }
                    }
                }
            }
        }

        val defaultCtaText = when (sheet.kind) {
            ContextKind.Wallet -> stringResource(R.string.home_context_sheet_budget_cta)
            ContextKind.Observation -> stringResource(R.string.home_context_sheet_observation_cta)
            else -> stringResource(R.string.home_context_sheet_primary_cta)
        }
        val primaryCtaText = sheet.primaryActionLabel ?: defaultCtaText

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CandySpace.sm)
        ) {
            Button(
                onClick = onPrimaryAction,
                enabled = !sheet.isEntityMissing && sheet.errorMessage == null && !sheet.isLoading,
                shape = CandyShape.primaryButton,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CandyColors.brandPurple,
                    contentColor = CandyColors.onReadyAction
                ),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = CandySize.touchTarget)
            ) {
                Text(
                    text = primaryCtaText,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            if (sheet.kind != ContextKind.Observation) {
                val closeLabel = stringResource(R.string.home_context_sheet_close_cta)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = CandySize.touchTarget)
                        .clip(CandyShape.primaryButton)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = closeLabel,
                            onClick = onDismiss
                        ),
                    shape = CandyShape.primaryButton,
                    color = CandyColors.primarySoft
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = closeLabel,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = CandyColors.primaryInk
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ObservationExplanationSheetBody(
    sheet: HomeSheet.Contextual
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CandySpace.sm)
    ) {
        Text(
            text = sheet.subtitle,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = CandyColors.textPrimary
        )

        if (!sheet.explanationDetail.isNullOrBlank() && sheet.explanationDetail != sheet.subtitle) {
            Text(
                text = sheet.explanationDetail,
                style = MaterialTheme.typography.bodyMedium,
                color = CandyColors.textSecondary
            )
        }

        if (!sheet.evidenceText.isNullOrBlank()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(SheetItemRowRadius),
                color = CandyColors.primarySoft.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, CandyColors.homeCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(CandySpace.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CandySpace.sm)
                ) {
                    ChankoIcon(
                        id = ChankoIconId.SYS_15,
                        contentDescription = null,
                        size = 20.dp
                    )
                    Text(
                        text = sheet.evidenceText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = CandyColors.primaryInk,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SheetPreviewItemRow(
    item: DecisionUi,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = CandySize.touchTarget)
            .clip(RoundedCornerShape(SheetItemRowRadius))
            .clickable(
                role = Role.Button,
                onClickLabel = item.title,
                onClick = onClick
            ),
        shape = RoundedCornerShape(SheetItemRowRadius),
        color = Color.White,
        border = BorderStroke(1.dp, CandyColors.homeCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CandySpace.md, vertical = CandySpace.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = CandyColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.priceFormatted,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = CandyColors.textSecondary
                )
            }

            Spacer(modifier = Modifier.width(CandySpace.sm))

            ChankoIcon(
                id = ChankoIconId.ACT_18,
                contentDescription = null,
                size = 16.dp
            )
        }
    }
}

@Composable
private fun SheetHeaderRow(
    title: String,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold
            ),
            color = CandyColors.textPrimary,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() }
        )

        val closeCd = stringResource(R.string.home_cd_close_sheet)
        Box(
            modifier = Modifier
                .size(CandySize.touchTarget)
                .clip(CircleShape)
                .clickable(
                    role = Role.Button,
                    onClickLabel = closeCd,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(SheetCloseVisualSize)
                    .clip(CircleShape)
                    .background(CandyColors.decisionSavedRowBg),
                contentAlignment = Alignment.Center
            ) {
                ChankoIcon(
                    id = ChankoIconId.ACT_03,
                    contentDescription = closeCd,
                    size = 18.dp
                )
            }
        }
    }
}
