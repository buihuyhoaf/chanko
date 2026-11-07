package com.hoabh.chanko.presentation.navigation

import java.net.URLEncoder

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Setup : Screen("setup")
    data object AddFlow : Screen("add_flow")
    data object VerifyItemInfo : Screen("verify_item_info")
    data object ManualItemInput : Screen("manual_item_input")
    data object QuickCheck : Screen("add_quick_check")
    data object DecisionResult : Screen("add_result")
    data object Main : Screen("main")
    data object ItemDetail : Screen("item_detail/{itemId}") {
        fun createRoute(itemId: Int): String = "item_detail/$itemId"
    }
    data object TransactionDetail : Screen("transaction_detail/{purchaseId}") {
        fun createRoute(purchaseId: String): String = "transaction_detail/$purchaseId"
    }
    data object ArchivedItems : Screen("archived_items")
    data object Profile : Screen("profile")
    data object Personality : Screen("personality")
    data object BudgetGallery : Screen("budget_gallery")
    data object BudgetDetail : Screen("budget_detail?cycleId={cycleId}&mode={mode}") {
        fun createRoute(
            cycleId: String? = null,
            mode: String? = null
        ): String {
            val params = mutableListOf<String>()
            if (!cycleId.isNullOrBlank()) params.add("cycleId=$cycleId")
            if (!mode.isNullOrBlank()) params.add("mode=$mode")
            return if (params.isEmpty()) "budget_detail" else "budget_detail?${params.joinToString("&")}"
        }
    }
    data object BudgetDetails : Screen("budget_details?cycleId={cycleId}&mode={mode}&expandHistory={expandHistory}") {
        fun createRoute(
            cycleId: String? = null,
            mode: String? = null,
            expandHistory: Boolean = false
        ): String {
            val params = mutableListOf<String>()
            if (!cycleId.isNullOrBlank()) params.add("cycleId=$cycleId")
            if (!mode.isNullOrBlank()) params.add("mode=$mode")
            if (expandHistory) params.add("expandHistory=true")
            return if (params.isEmpty()) "budget_details" else "budget_details?${params.joinToString("&")}"
        }
    }
    data object WalletSettings : Screen("wallet_settings?expandHistory={expandHistory}") {
        fun createRoute(expandHistory: Boolean = false): String =
            "wallet_settings?expandHistory=$expandHistory"
    }

    // CHAT-UX-REV-01 Navigation Routes
    data object Chat : Screen("chat") // Screen 1: CL (Chat List)
    data object NewChat : Screen("new_chat") // Screen 2: NC (New Chat)
    data object ChatDetail : Screen("chat_detail?threadId={threadId}&contextMode={contextMode}&itemId={itemId}&itemName={itemName}&itemPrice={itemPrice}") { // Screen 3: CD (Chat Detail)
        fun createRoute(
            threadId: String? = null,
            contextMode: String? = null,
            itemId: String? = null,
            itemName: String? = null,
            itemPrice: Long? = null
        ): String {
            val params = mutableListOf<String>()
            if (!threadId.isNullOrBlank()) params.add("threadId=$threadId")
            if (!contextMode.isNullOrBlank()) params.add("contextMode=$contextMode")
            if (!itemId.isNullOrBlank()) params.add("itemId=$itemId")
            if (!itemName.isNullOrBlank()) params.add("itemName=${URLEncoder.encode(itemName, "UTF-8")}")
            if (itemPrice != null) params.add("itemPrice=$itemPrice")
            return if (params.isEmpty()) "chat_detail" else "chat_detail?${params.joinToString("&")}"
        }
    }
    data object ChatThread : Screen("chat_thread/{threadId}") {
        fun createRoute(threadId: String): String = ChatDetail.createRoute(threadId = threadId)
    }
}
