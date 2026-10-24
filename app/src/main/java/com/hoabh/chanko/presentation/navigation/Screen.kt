package com.hoabh.chanko.presentation.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Setup : Screen("setup")
    data object Main : Screen("main")
    data object ItemDetail : Screen("item_detail/{itemId}") {
        fun createRoute(itemId: Int): String = "item_detail/$itemId"
    }
    data object TransactionDetail : Screen("transaction_detail/{purchaseId}") {
        fun createRoute(purchaseId: String): String = "transaction_detail/$purchaseId"
    }
    data object Profile : Screen("profile")
    data object Personality : Screen("personality")
}
