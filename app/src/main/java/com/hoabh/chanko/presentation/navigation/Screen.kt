package com.hoabh.chanko.presentation.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Setup : Screen("setup")
    data object Dashboard : Screen("dashboard")
}
