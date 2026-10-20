package com.hoabh.chanko.presentation.screens.home.model

sealed interface MascotAdvice {
    data object Safe : MascotAdvice
    data class Warning(val percent: Int) : MascotAdvice
    data object OverBudget : MascotAdvice
}
