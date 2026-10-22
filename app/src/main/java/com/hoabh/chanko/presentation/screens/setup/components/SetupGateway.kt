package com.hoabh.chanko.presentation.screens.setup.components

/** Presentation integration seam, not an endpoint or an approved API payload. */
interface SetupGateway {
    suspend fun checkStatus(): SetupStatus
    suspend fun submit(draft: SetupDraft): SetupSubmission
}

enum class SetupStatus { COMPLETE, INCOMPLETE, UNKNOWN, OFFLINE, ERROR, UNAVAILABLE }

enum class SetupSubmission { ACCEPTED, UNKNOWN, OFFLINE, ERROR, UNAVAILABLE }

data class SetupDraft(val amountVnd: String, val anchorDay: Int, val deviceTimeZone: String)

/** Fails closed until a reviewed backend adapter is available. Never returns fake completion. */
object UnconfiguredSetupGateway : SetupGateway {
    override suspend fun checkStatus(): SetupStatus = SetupStatus.UNAVAILABLE
    override suspend fun submit(draft: SetupDraft): SetupSubmission = SetupSubmission.UNAVAILABLE
}
