package com.hoabh.chanko.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.hoabh.chanko.domain.auth.SessionState
import com.hoabh.chanko.presentation.features.archived.ArchivedItemsRoute
import com.hoabh.chanko.presentation.features.budget.BudgetRoute
import com.hoabh.chanko.presentation.features.budget.BudgetViewModel
import com.hoabh.chanko.presentation.features.itemdetail.ItemDetailRoute
import com.hoabh.chanko.presentation.features.login.LoginRoute
import com.hoabh.chanko.presentation.features.login.loginDestination
import com.hoabh.chanko.presentation.features.transaction.TransactionDetailRoute
import com.hoabh.chanko.presentation.features.profile.ProfileRoute
import com.hoabh.chanko.presentation.features.profile.personality.PersonalityRoute
import com.hoabh.chanko.presentation.features.setup.setupDestination
import com.hoabh.chanko.presentation.features.splash.splashDestination
import androidx.navigation.navigation
import androidx.compose.runtime.remember
import com.hoabh.chanko.domain.additem.AddItemStep
import com.hoabh.chanko.presentation.features.additem.*
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.hoabh.chanko.presentation.features.chat.ChatDetailScreen
import com.hoabh.chanko.presentation.features.chat.ChatListScreen
import com.hoabh.chanko.presentation.features.chat.NewChatScreen
import com.hoabh.chanko.presentation.features.chat.ChatListViewModel
import com.hoabh.chanko.presentation.features.chat.ChatViewModel
import com.hoabh.chanko.presentation.features.chat.NewChatViewModel

/** Các route công khai không yêu cầu phiên đăng nhập */
private val UnauthenticatedRoutes = setOf(
    Screen.Splash.route,
    Screen.Login.route,
    Screen.Setup.route,
    Screen.Main.route
)

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    loginContent: @Composable (() -> Unit) -> Unit = { LoginRoute(it) },
    session: LoginSessionViewModel = hiltViewModel()
) {
    val identity by session.session.collectAsStateWithLifecycle()
    val restored by session.restorationComplete.collectAsStateWithLifecycle()
    val authenticated = identity is SessionState.Available
    val entry by navController.currentBackStackEntryAsState()

    LaunchedEffect(entry, authenticated, restored) {
        if (!restored) return@LaunchedEffect

        val currentRoute = entry?.destination?.route
        if (authenticated && currentRoute == Screen.Login.route) {
            navController.navigate(Screen.Setup.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
                launchSingleTop = true
            }
            return@LaunchedEffect
        }

        // TODO(dummy): Cho phép truy cập Screen.Setup khi chưa có session authenticated chính thức
        if (!authenticated && currentRoute != null && currentRoute !in UnauthenticatedRoutes) {
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.id) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        splashDestination(
            isRestored = { restored },
            onSplashFinished = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        )

        loginDestination(
            loginContent = loginContent,
            onLoginSuccess = {
                // TODO(dummy): Navigate tới màn SetupGate khi logic onLoginSuccess pass (tạm thời cho đến khi kết nối backend/OAuth)
                navController.navigate(Screen.Setup.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )

        setupDestination(
            accountKeyProvider = {
                // TODO(dummy): Hiển thị màn SetupGate độc lập với accountKey giả lập trước khi có session thật từ server
                (identity as? SessionState.Available)?.account?.value ?: "dummy_account"
            },
            onConfirmedComplete = {
                navController.navigate(Screen.Main.route) {
                    popUpTo(Screen.Setup.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )

        mainDestination(
            onAddItemEffect = { navController.handleAddItemEffect(it) },
            onNavigateToProfile = {
                navController.navigate(Screen.Profile.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToChat = {
                navController.navigate(Screen.Chat.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToItemDetail = { itemId ->
                navController.navigate(Screen.ItemDetail.createRoute(itemId)) {
                    launchSingleTop = true
                }
            },
            onNavigateToArchived = {
                navController.navigate(Screen.ArchivedItems.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToChatThread = { threadId ->
                navController.navigate(Screen.ChatThread.createRoute(threadId)) {
                    launchSingleTop = true
                }
            },
            onNavigateToNewChat = {
                navController.navigate(Screen.NewChat.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToBudgetDetails = {
                // Màn hình Ngân sách đã được tích hợp trực tiếp trong MainNavigation để giữ Bottom Navigation
            }
        )

        navigation(startDestination = Screen.ManualItemInput.route, route = Screen.AddFlow.route) {
            listOf(Screen.VerifyItemInfo.route to AddItemStep.VERIFY_INFO,
                Screen.ManualItemInput.route to AddItemStep.MANUAL_INPUT,
                Screen.QuickCheck.route to AddItemStep.QUICK_CHECK,
                Screen.DecisionResult.route to AddItemStep.DECISION_RESULT).forEach { (route, step) ->
                composable(route) { entry ->
                    val parent = remember(entry) { navController.getBackStackEntry(Screen.Main.route) }
                    val viewModel: AddItemViewModel = hiltViewModel(parent)
                    AddItemRoute(step, viewModel) { navController.handleAddItemEffect(it) }
                }
            }
        }

        composable(
            route = Screen.BudgetDetails.route,
            arguments = listOf(
                navArgument("cycleId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("mode") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = "Active"
                },
                navArgument("expandHistory") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val expandHistory = backStackEntry.arguments?.getBoolean("expandHistory") ?: false
            BudgetRoute(
                onBackClick = { navController.popBackStack() },
                onNavigateToItemDetail = { itemId ->
                    navController.navigate(Screen.ItemDetail.createRoute(itemId)) {
                        launchSingleTop = true
                    }
                },
                expandHistory = expandHistory,
                openCyclePicker = expandHistory
            )
        }

        composable(
            route = Screen.WalletSettings.route,
            arguments = listOf(
                navArgument("expandHistory") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val expandHistory = backStackEntry.arguments?.getBoolean("expandHistory") ?: false
            val parentEntry = androidx.compose.runtime.remember(backStackEntry) {
                try {
                    navController.getBackStackEntry(Screen.BudgetDetails.route)
                } catch (e: Exception) {
                    backStackEntry
                }
            }
            val budgetViewModel: BudgetViewModel = hiltViewModel(parentEntry)
            BudgetRoute(
                onBackClick = { navController.popBackStack() },
                expandHistory = expandHistory,
                viewModel = budgetViewModel
            )
        }

        composable(
            route = Screen.ArchivedItems.route
        ) {
            ArchivedItemsRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { itemId ->
                    navController.navigate(Screen.ItemDetail.createRoute(itemId)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Screen.ItemDetail.route,
            arguments = listOf(
                navArgument("itemId") {
                    type = NavType.IntType
                }
            )
        ) {
            ItemDetailRoute(
                onNavigateBack = { navController.popBackStack() },
                onOpenTransactionDetail = { purchaseId ->
                    navController.navigate(Screen.TransactionDetail.createRoute(purchaseId)) {
                        launchSingleTop = true
                    }
                },
                onNavigateToChat = { itemId, itemName, itemPrice ->
                    navController.navigate(
                        Screen.ChatDetail.createRoute(
                            contextMode = "ITEM",
                            itemId = itemId,
                            itemName = itemName,
                            itemPrice = itemPrice
                        )
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Screen.TransactionDetail.route
        ) {
            TransactionDetailRoute(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPersonality = {
                    navController.navigate(Screen.Personality.route) {
                        launchSingleTop = true
                    }
                },
                onSignOutSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Personality.route) {
            PersonalityRoute(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Screen 1: CL — Chat List Screen
        composable(Screen.Chat.route) {
            val listViewModel: ChatListViewModel = hiltViewModel()
            ChatListScreen(
                viewModel = listViewModel,
                onNavigateToDetail = { threadId ->
                    navController.navigate(Screen.ChatDetail.createRoute(threadId = threadId)) {
                        launchSingleTop = true
                    }
                },
                onNavigateToNewChat = {
                    navController.navigate(Screen.NewChat.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToItemDetail = { itemId ->
                    val id = itemId.toIntOrNull()
                    if (id != null) {
                        navController.navigate(Screen.ItemDetail.createRoute(id)) {
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        // Screen 2: NC — New Chat Screen
        composable(Screen.NewChat.route) {
            val newChatViewModel: NewChatViewModel = hiltViewModel()
            NewChatScreen(
                viewModel = newChatViewModel,
                onNavigateToChatDetail = { contextMode, itemId, itemName, itemPrice ->
                    navController.navigate(
                        Screen.ChatDetail.createRoute(
                            contextMode = contextMode,
                            itemId = itemId,
                            itemName = itemName,
                            itemPrice = itemPrice
                        )
                    ) {
                        popUpTo(Screen.NewChat.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToMyItems = {
                    navController.popBackStack()
                },
                onClose = { navController.popBackStack() }
            )
        }

        // Screen 3: CD — Chat Detail Screen
        composable(
            route = Screen.ChatDetail.route,
            arguments = listOf(
                navArgument("threadId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("contextMode") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("itemId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("itemName") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("itemPrice") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val threadId = backStackEntry.arguments?.getString("threadId")
            val contextMode = backStackEntry.arguments?.getString("contextMode")
            val itemId = backStackEntry.arguments?.getString("itemId")
            val rawItemName = backStackEntry.arguments?.getString("itemName")
            val itemName = if (!rawItemName.isNullOrBlank()) {
                try {
                    java.net.URLDecoder.decode(rawItemName, "UTF-8")
                } catch (e: Exception) {
                    rawItemName
                }
            } else null
            val itemPrice = backStackEntry.arguments?.getString("itemPrice")?.toLongOrNull()

            val chatViewModel: ChatViewModel = hiltViewModel()
            ChatDetailScreen(
                threadId = threadId,
                viewModel = chatViewModel,
                contextMode = contextMode,
                itemId = itemId,
                itemName = itemName,
                itemPrice = itemPrice,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToItemDetail = { idStr ->
                    val id = idStr.toIntOrNull()
                    if (id != null) {
                        navController.navigate(Screen.ItemDetail.createRoute(id)) {
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        // Legacy / Backward compatibility route for ChatThread
        composable(
            route = Screen.ChatThread.route,
            arguments = listOf(
                navArgument("threadId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val threadId = backStackEntry.arguments?.getString("threadId") ?: ""
            val chatViewModel: ChatViewModel = hiltViewModel()
            ChatDetailScreen(
                threadId = threadId,
                viewModel = chatViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToItemDetail = { idStr ->
                    val id = idStr.toIntOrNull()
                    if (id != null) {
                        navController.navigate(Screen.ItemDetail.createRoute(id)) {
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}

private fun NavHostController.handleAddItemEffect(effect: AddItemEffect) {
    when (effect) {
        AddItemEffect.Exit -> popBackStack(Screen.Main.route, inclusive = false)
        is AddItemEffect.Navigate -> {
            val target = when (effect.step) {
                AddItemStep.VERIFY_INFO -> Screen.VerifyItemInfo.route
                AddItemStep.MANUAL_INPUT -> Screen.ManualItemInput.route
                AddItemStep.QUICK_CHECK -> Screen.QuickCheck.route
                AddItemStep.DECISION_RESULT -> Screen.DecisionResult.route
                AddItemStep.URL_DIALOG -> return
            }
            if (currentDestination?.route != target) navigate(target) { launchSingleTop = true }
        }
    }
}
