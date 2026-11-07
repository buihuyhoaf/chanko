package com.hoabh.chanko.presentation.features.additem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.additem.AddItemFormState
import com.hoabh.chanko.presentation.designsystem.icon.*
import com.hoabh.chanko.ui.theme.*

/** Feature styles transcribed from DESIGN-REVIEW.html; all colors reuse approved app tokens. */
@Composable
internal fun PrimaryAction(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().heightIn(min = CandySize.primaryButton),
        shape = CandyShape.primaryButton, contentPadding = PaddingValues(CandySpace.lg, CandySpace.md),
        colors = ButtonDefaults.buttonColors(containerColor = CandyColors.primaryInk,
            contentColor = MaterialTheme.colorScheme.surface, disabledContainerColor = CandyColors.addItemDisabledSurface,
            disabledContentColor = CandyColors.textSecondary)) { Text(label, style = MaterialTheme.typography.labelLarge) }
}

@Composable
internal fun SecondaryAction(label: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    OutlinedButton(onClick, enabled = enabled, modifier = modifier.fillMaxWidth().heightIn(min = CandySize.touchTarget),
        shape = CandyShape.input, border = BorderStroke(1.dp, CandyColors.outline),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface,
            contentColor = CandyColors.textPrimary), contentPadding = PaddingValues(CandySpace.lg, CandySpace.md)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
internal fun LinkAction(label: String, modifier: Modifier = Modifier, enabled: Boolean = true,
    icon: ChankoIconId? = null, onClick: () -> Unit) {
    TextButton(onClick, enabled = enabled, modifier = modifier.fillMaxWidth().heightIn(min = CandySize.touchTarget),
        colors = ButtonDefaults.textButtonColors(contentColor = CandyColors.primaryInk)) {
        if (icon != null) {
            ChankoIcon(id = icon, contentDescription = null)
            Spacer(Modifier.width(CandySpace.xs))
        }
        Text(label, style = MaterialTheme.typography.labelLarge, textDecoration = TextDecoration.Underline)
    }
}

@Composable
internal fun ReviewCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxWidth(), shape = CandyShape.card, color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp) {
        Column(Modifier.padding(CandySpace.lg), content = content)
    }
}

@Composable
internal fun HelpText(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier, style = MaterialTheme.typography.bodyMedium, color = CandyColors.textSecondary)
}

@Composable
internal fun ProductSummary(form: AddItemFormState, modifier: Modifier = Modifier, onPhotoError: () -> Unit = {}) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CandySpace.md)) {
        Surface(Modifier.size(64.dp), shape = CandyShape.thumbnail, color = CandyColors.primarySoft) {
            Box(contentAlignment = Alignment.Center) {
                ChankoIcon(id = ChankoIconId.ITM_03, contentDescription = null)
                if (form.photoUri.isNotBlank()) AsyncImage(model = form.photoUri, contentDescription = null,
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize(), onError = { onPhotoError() })
            }
        }
        Column(Modifier.weight(1f)) {
            Text(form.title.ifBlank { stringResource(R.string.add_item_unnamed) },
                style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = CandyColors.textPrimary)
            Text(form.price?.let(::formatVnd) ?: stringResource(R.string.add_item_no_price),
                style = FinancialTypography.productPrice, color = CandyColors.textPrimary)
            HelpText(form.category.ifBlank { stringResource(R.string.add_item_no_category) })
        }
    }
}

@Composable
internal fun MascotPlaceholder() {
    val size = if (LocalDensity.current.fontScale >= 1.5f) 72.dp else 96.dp
    Row(Modifier.fillMaxWidth().clearAndSetSemantics {}, verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CandySpace.md)) {
        Image(painterResource(R.drawable.add_item_mascot_pending), contentDescription = null, modifier = Modifier.size(size))
        HelpText(stringResource(R.string.add_item_mascot_caption), Modifier.weight(1f))
    }
}

/**
 * Pastel Candy Product Stage & Live Summary
 * Đồng bộ với phong cách ProductStageHero & Header trong ItemDetailScreen:
 * Tỉ lệ 4:3, góc bo 24dp, nền calendarSurface, tiêu đề 22sp Bold & giá 20sp Bold.
 */
@Composable
internal fun AddItemProductHero(
    form: AddItemFormState,
    enabled: Boolean,
    onPickPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    onPhotoError: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CandySpace.sm)
    ) {
        // 1. Pastel Candy Product Stage (tỉ lệ 4:3, góc bo 24dp, nền calendarSurface)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(RoundedCornerShape(24.dp))
                .clickable(enabled = enabled, onClick = onPickPhoto),
            shape = RoundedCornerShape(24.dp),
            color = CandyColors.calendarSurface
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (form.photoUri.isNotBlank()) {
                    AsyncImage(
                        model = form.photoUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        onError = { onPhotoError() }
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.ic_product_placeholder),
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                // Overlay chọn/thay ảnh ở góc dưới
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(CandySpace.md),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.92f),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = CandySpace.md, vertical = CandySpace.xs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CandySpace.xs)
                    ) {
                        ChankoIcon(
                            id = ChankoIconId.ACT_16,
                            contentDescription = null,
                            size = 18.dp
                        )
                        Text(
                            text = stringResource(
                                if (form.photoUri.isBlank()) R.string.add_item_photo
                                else R.string.add_item_replace_photo
                            ),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = CandyColors.textPrimary
                        )
                    }
                }
            }
        }

        // Nút xóa ảnh nếu đã có ảnh
        if (form.photoUri.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onRemovePhoto,
                    enabled = enabled,
                    colors = ButtonDefaults.textButtonColors(contentColor = CandyColors.errorText)
                ) {
                    Text(
                        text = stringResource(R.string.add_item_remove_photo),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        // 2. Tiêu đề & Mức giá live preview (giống ItemDetailScreen: 22sp Bold và 20sp Bold)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = CandySpace.xs)
        ) {
            Text(
                text = form.title.ifBlank { stringResource(R.string.add_item_unnamed) },
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = CandyColors.textPrimary
            )
            Spacer(modifier = Modifier.height(CandySpace.xs))
            Text(
                text = form.price?.let(::formatVnd) ?: stringResource(R.string.add_item_no_price),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = CandyColors.textPrimary
            )
            if (form.category.isNotBlank()) {
                Spacer(modifier = Modifier.height(CandySpace.xs))
                HelpText(form.category)
            }
        }
    }
}
