package com.hoabh.chanko.presentation.screens.setup.data

enum class SetupPage { CHECKING, STATUS_ERROR, FORM, COMPLETE }

enum class SetupProblem { UNKNOWN, OFFLINE, SAVE_ERROR, UNAVAILABLE, TIME_ZONE }

data class SetupUiState(
    val page: SetupPage = SetupPage.FORM,
    val step: Int = 1,
    val amount: String = "",
    val day: Int? = null,
    val amountError: Boolean = false,
    val dayError: Boolean = false,
    val busy: Boolean = false,
    val problem: SetupProblem? = null,
    val showErrorDialog: Boolean = false,
    // A possibly committed submission must be reconciled before any new submission.
    val needsReconciliation: Boolean = false,
)
