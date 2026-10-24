package com.hoabh.chanko.presentation.screens.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.screens.home.DraftSyncState
import com.hoabh.chanko.presentation.screens.home.DraftUi
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandyElevation
import com.hoabh.chanko.ui.theme.CandyShape
import com.hoabh.chanko.ui.theme.CandySpace

private val DraftTileSize = 40.dp
private val DraftTileShape = RoundedCornerShape(12.dp)
private val DraftIconSize = 22.dp

/**
 * HOME-07 — Local Draft Indicator (`DraftResumeCard`):
 * - Chỉ hiển thị khi `draft != null` (S09, S11, S14).
 * - Phân biệt trạng thái `LocalDraft`, `PendingSync`, `SyncFailed`.
 */
@Composable
fun DraftResumeCard(
    draft: DraftUi,
    onResumeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    canResume: Boolean = true
) {
    val statusText = when (draft.syncState) {
        DraftSyncState.LocalDraft -> draft.updatedLabel.ifBlank {
            stringResource(R.string.home_draft_status_local)
        }
        DraftSyncState.PendingSync -> stringResource(R.string.home_draft_status_pending)
        DraftSyncState.SyncFailed -> stringResource(R.string.home_draft_status_failed)
    }

    val resumeLabel = stringResource(R.string.home_draft_resume_cta)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(CandyShape.card)
            .then(
                if (canResume) {
                    Modifier.clickable(
                        role = Role.Button,
                        onClickLabel = resumeLabel,
                        onClick = { onResumeClick(draft.id) }
                    )
                } else {
                    Modifier
                }
            ),
        shape = CandyShape.card,
        color = CandyColors.homeCardSurface,
        tonalElevation = CandyElevation.low,
        shadowElevation = CandyElevation.low,
        border = BorderStroke(1.dp, CandyColors.homeCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CandySpace.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(DraftTileSize)
                    .clip(DraftTileShape)
                    .background(CandyColors.primarySoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.EditNote,
                    contentDescription = null,
                    tint = CandyColors.brandPurple,
                    modifier = Modifier.size(DraftIconSize)
                )
            }

            Spacer(modifier = Modifier.width(CandySpace.md))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_draft_header),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = CandyColors.primaryInk
                )
                Text(
                    text = draft.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = CandyColors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(CandySpace.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!draft.priceFormatted.isNullOrBlank()) {
                        Text(
                            text = draft.priceFormatted,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = CandyColors.textPrimary
                        )
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = CandyColors.textSecondary
                    )
                }
            }

            if (canResume) {
                Spacer(modifier = Modifier.width(CandySpace.sm))
                Surface(
                    modifier = Modifier.heightIn(min = 32.dp),
                    shape = CandyShape.chip,
                    color = CandyColors.primarySoft
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = CandySpace.md,
                            vertical = CandySpace.xs
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = resumeLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = CandyColors.primaryInk
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = CandyColors.primaryInk,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
