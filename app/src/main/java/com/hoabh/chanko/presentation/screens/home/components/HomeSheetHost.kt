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
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.screens.home.ContextKind
import com.hoabh.chanko.presentation.screens.home.HomeSheet
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandyElevation
import com.hoabh.chanko.ui.theme.CandyShape
import com.hoabh.chanko.ui.theme.CandySize
import com.hoabh.chanko.ui.theme.CandySpace

private val DragHandleWidth = 36.dp
private val DragHandleHeight = 4.dp
private val SheetCloseVisualSize = 32.dp
private val SheetTileIconBoxSize = 44.dp
private val SheetTileIconBoxShape = RoundedCornerShape(12.dp)
private val SheetTileShape = RoundedCornerShape(18.dp)
private val ScrimColor = Color.Black.copy(alpha = 0.42f)

/**
 * HOME-08 — Modal Sheet Host (v2 Image-Aligned):
 * - Đảm bảo chính sách tối đa 1 sheet hoạt động tại một thời điểm (`S16`).
 * - Hỗ trợ `GlobalQuickActionSheet` (`S22`) và `ContextualSheet` (`S17`).
 * - Render dưới dạng modal overlay có scrim phía trên nội dung Home (không bao giờ chèn vào LazyColumn).
 */
@Composable
fun HomeSheetHost(
    activeSheet: HomeSheet?,
    onDismiss: () -> Unit,
    onQuickCheckClick: () -> Unit,
    onSaveItemClick: () -> Unit,
    onContextPrimaryAction: (ContextKind, String?) -> Unit,
    modifier: Modifier = Modifier
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

                when (activeSheet) {
                    HomeSheet.Global -> GlobalQuickActionSheetContent(
                        onDismiss = onDismiss,
                        onQuickCheckClick = onQuickCheckClick,
                        onSaveItemClick = onSaveItemClick
                    )
                    is HomeSheet.Contextual -> ContextualSheetContent(
                        sheet = activeSheet,
                        onDismiss = onDismiss,
                        onPrimaryAction = {
                            onContextPrimaryAction(activeSheet.kind, activeSheet.entityId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun GlobalQuickActionSheetContent(
    onDismiss: () -> Unit,
    onQuickCheckClick: () -> Unit,
    onSaveItemClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CandySpace.md)
    ) {
        // Header Row: "Bạn muốn làm gì?" + Close X Button
        SheetHeaderRow(
            title = stringResource(R.string.home_global_sheet_title),
            onDismiss = onDismiss
        )

        // Entry 1: Quick Check
        GlobalSheetActionTile(
            title = stringResource(R.string.home_quick_check_title),
            description = stringResource(R.string.home_global_sheet_quick_check_desc),
            icon = ChankoIconId.ITM_01,
            iconTileBg = CandyColors.primarySoft,
            tileBg = CandyColors.saveItemSurface,
            tileBorder = CandyColors.saveItemBorder,
            onClick = onQuickCheckClick
        )

        // Entry 2: Save an Item
        GlobalSheetActionTile(
            title = stringResource(R.string.home_save_item_title),
            description = stringResource(R.string.home_global_sheet_save_item_desc),
            icon = ChankoIconId.ITM_02,
            iconTileBg = CandyColors.globalSheetSaveIconTileBg,
            tileBg = CandyColors.homeCardSurface,
            tileBorder = CandyColors.homeCardBorder,
            onClick = onSaveItemClick
        )
    }
}

@Composable
private fun GlobalSheetActionTile(
    title: String,
    description: String,
    icon: ChankoIconId,
    iconTileBg: Color,
    tileBg: Color,
    tileBorder: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SheetTileShape)
            .clickable(
                role = Role.Button,
                onClickLabel = title,
                onClick = onClick
            ),
        shape = SheetTileShape,
        color = tileBg,
        border = BorderStroke(1.dp, tileBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CandySpace.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(SheetTileIconBoxSize)
                    .clip(SheetTileIconBoxShape)
                    .background(iconTileBg),
                contentAlignment = Alignment.Center
            ) {
                ChankoIcon(
                    id = icon,
                    contentDescription = null,
                    size = 22.dp
                )
            }

            Spacer(modifier = Modifier.width(CandySpace.md))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = CandyColors.textPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = CandyColors.textSecondary
                )
            }

            Spacer(modifier = Modifier.width(CandySpace.sm))

            ChankoIcon(
                id = ChankoIconId.ACT_18,
                contentDescription = null,
                size = 20.dp
            )
        }
    }
}

@Composable
private fun ContextualSheetContent(
    sheet: HomeSheet.Contextual,
    onDismiss: () -> Unit,
    onPrimaryAction: () -> Unit
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

        val bodyText = if (sheet.isEntityMissing) {
            stringResource(R.string.home_context_sheet_missing_body)
        } else {
            sheet.subtitle
        }

        SheetHeaderRow(
            title = headerTitle,
            onDismiss = onDismiss
        )

        if (!sheet.isEntityMissing && !sheet.highlightValue.isNullOrBlank()) {
            Surface(
                shape = CandyShape.chip,
                color = CandyColors.primarySoft
            ) {
                Text(
                    text = sheet.highlightValue,
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
            text = bodyText,
            style = MaterialTheme.typography.bodyMedium,
            color = CandyColors.textSecondary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CandySpace.sm)
        ) {
            // S17: Primary write/navigation action is disabled when entity is missing
            Button(
                onClick = onPrimaryAction,
                enabled = !sheet.isEntityMissing,
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
                    text = stringResource(R.string.home_context_sheet_primary_cta),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

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
