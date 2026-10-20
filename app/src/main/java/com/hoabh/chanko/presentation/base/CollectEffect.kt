package com.hoabh.chanko.presentation.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow

/**
 * Tiện ích thu thập Flow Effect một cách an toàn theo vòng đời của màn hình Compose.
 * Chỉ nhận effect khi màn hình ở trạng thái tối thiểu là [Lifecycle.State.STARTED].
 */
@Composable
fun <T> CollectEffect(
    effectFlow: Flow<T>,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    onEffect: suspend (T) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(effectFlow, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(lifecycleState) {
            effectFlow.collect { effect ->
                onEffect(effect)
            }
        }
    }
}
