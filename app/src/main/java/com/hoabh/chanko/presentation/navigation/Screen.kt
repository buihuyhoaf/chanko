package com.hoabh.chanko.presentation.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object AddItem : Screen("add_item")
    data object ItemDetail : Screen("item_detail/{itemId}") {
        fun createRoute(itemId: Int) = "item_detail/$itemId"
    }
    data object Decision : Screen("decision/{itemId}") {
        fun createRoute(itemId: Int) = "decision/$itemId"
    }
    data object Vault : Screen("vault")
}
