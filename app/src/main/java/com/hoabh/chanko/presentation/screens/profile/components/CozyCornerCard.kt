package com.hoabh.chanko.presentation.screens.profile.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandyElevation
import com.hoabh.chanko.ui.theme.CandyShape
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.Nunito
import kotlinx.coroutines.launch

/**
 * PROFILE-04 — Cozy Corner Card:
 * - Minh họa mèo trắng ngồi ôm cốc tím tại bàn trà nhỏ.
 * - Card nền trắng / pastel bo góc 24dp.
 * - Tương tác chạm: whole-scene subtle bounce (1.00 -> 1.025 -> 1.00 trong 350ms).
 * - Không có LLM/AI, không pet grooming. Hỗ trợ Reduce Motion.
 */
@Composable
fun CozyCornerCard(
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false,
    onTap: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val scale = remember { Animatable(1.0f) }
    val interactionSource = remember { MutableInteractionSource() }

    val tapDescription = stringResource(R.string.profile_cozy_corner_tap_description)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(CandyShape.card)
            .scale(scale.value)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = {
                    onTap()
                    if (!reduceMotion && !scale.isRunning) {
                        coroutineScope.launch {
                            scale.animateTo(
                                targetValue = 1.025f,
                                animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
                            )
                            scale.animateTo(
                                targetValue = 1.0f,
                                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                            )
                        }
                    }
                }
            )
            .semantics {
                contentDescription = tapDescription
            },
        shape = CandyShape.card,
        color = Color.White,
        shadowElevation = CandyElevation.low
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CandySpace.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.4f)
                    .clip(CandyShape.smallCard)
                    .background(CandyColors.backgroundLavender),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.chanko_profile_cozy_corner),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(CandySpace.md))

            Text(
                text = stringResource(R.string.profile_cozy_corner_title),
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = CandyColors.textPrimary
            )
        }
    }
}
