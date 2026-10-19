package com.hoabh.chanko.presentation.navigation

import androidx.annotation.StringRes
import com.hoabh.chanko.R

/** Stable route contracts; no credentials, account identifiers or business payloads in routes. */
enum class TopLevelDestination(
    val route: String,
    val label: String,
    @get:StringRes val labelRes: Int
) {
    Home("home", "Home", R.string.tab_home),
    MyItems("my_items", "My Items", R.string.tab_my_items),
    Budget("budget", "Budget", R.string.tab_budget),
    Insights("insights", "Insights", R.string.tab_insights);
}
