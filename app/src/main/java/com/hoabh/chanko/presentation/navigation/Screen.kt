package com.hoabh.chanko.presentation.navigation

import java.net.URLEncoder

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
    data object ArchivedItems : Screen("archived_items")
    data object Profile : Screen("profile")
    data object Personality : Screen("personality")

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
