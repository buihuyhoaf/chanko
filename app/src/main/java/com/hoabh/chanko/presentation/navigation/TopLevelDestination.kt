package com.hoabh.chanko.presentation.navigation

/** Stable route contracts; no credentials, account identifiers or business payloads in routes. */
enum class TopLevelDestination(val route: String, val label: String) {
    Home("home", "Home"),
    MyItems("my_items", "My Items"),
    Budget("budget", "Budget"),
    Insights("insights", "Insights");
}
