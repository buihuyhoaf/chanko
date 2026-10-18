package com.hoabh.chanko.ui.mascot

import androidx.annotation.DrawableRes

/** UI-05 presentation only. Not the SPEC-07 business emotion or an API enum. */
enum class MascotPresentationState {
    Welcome, Thinking, Sleeping, Curious, Concerned, Relaxed, Encouraging, Neutral
}

/** Static transparent render. Every entry needs design and usage-rights evidence before registration. */
internal data class MascotStaticAsset(
    @param:DrawableRes val drawableRes: Int,
    val version: String,
    val source: String,
    val usageRightsEvidence: String,
    val designApprovalEvidence: String,
    val sha256: String,
    val widthPx: Int,
    val heightPx: Int
)

internal fun resolveMascotAsset(
    state: MascotPresentationState,
    approvedAssets: Map<MascotPresentationState, MascotStaticAsset>
): MascotStaticAsset? = approvedAssets[state] ?: approvedAssets[MascotPresentationState.Neutral]

internal object MascotAssets {
    // Intentionally empty: legacy cat_mascot has no verified alpha, rights or state approval.
    // Do not register a final asset until its evidence and alpha/crop checks are reviewed.
    private val approvedAssets: Map<MascotPresentationState, MascotStaticAsset> = emptyMap()

    fun resolve(state: MascotPresentationState): MascotStaticAsset? =
        resolveMascotAsset(state, approvedAssets)
}
