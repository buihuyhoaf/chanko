package com.hoabh.chanko.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.screens.home.HomeRoute
import com.hoabh.chanko.presentation.screens.home.HomeSheet
import com.hoabh.chanko.presentation.screens.home.components.HomeSheetHost
import com.hoabh.chanko.presentation.screens.myitems.MyItemsRoute
import com.hoabh.chanko.ui.theme.CandyColors

fun NavGraphBuilder.mainDestination(
    onNavigateToProfile: () -> Unit = {},
    onNavigateToItemDetail: (Int) -> Unit = {}
) {
    composable(route = Screen.Main.route) {
        MainNavigation(
            onNavigateToProfile = onNavigateToProfile,
            onNavigateToItemDetail = onNavigateToItemDetail
        )
    }
}

internal fun NavHostController.selectTab(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** This controller lives only inside the authenticated account subtree. */
@Composable
fun MainNavigation(
    controller: NavHostController = rememberNavController(),
    onAddNewItemClick: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToItemDetail: (Int) -> Unit = {}
) {
    val entry by controller.currentBackStackEntryAsState()
    val currentRoute = entry?.destination?.route
    var isGlobalQuickActionSheetOpen by rememberSaveable { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = Color.Transparent,
            bottomBar = {
                ChankoBottomBar(
                    currentRoute = currentRoute,
                    onTabSelected = { controller.selectTab(it) },
                    // S16 / HOME-08: Tapping + opens GlobalQuickActionSheet without changing selected tab
                    onAddNewItemClick = { isGlobalQuickActionSheetOpen = true }
                )
            }
        ) { _ ->
            NavHost(
                navController = controller,
                startDestination = TopLevelDestination.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(TopLevelDestination.Home.route) {
                    HomeRoute(
                        onAddNewItemClick = onAddNewItemClick,
                        onProfileClick = onNavigateToProfile
                    )
                }
                composable(TopLevelDestination.MyItems.route) {
                    MyItemsRoute(
                        onNavigateToDetail = onNavigateToItemDetail,
                        onNavigateToArchived = {
                            // Sẽ nối vào ArchivedItemsRoute trong lát cắt tiếp theo
                        }
                    )
                }
                listOf(
                    TopLevelDestination.Budget,
                    TopLevelDestination.Insights
                ).forEach { tab ->
                    composable(tab.route) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(R.string.tab_not_implemented, stringResource(tab.labelRes)),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }

        // Global + Modal Bottom Sheet Overlay (S16, S22)
        if (isGlobalQuickActionSheetOpen) {
            HomeSheetHost(
                activeSheet = HomeSheet.Global,
                onDismiss = { isGlobalQuickActionSheetOpen = false },
                onQuickCheckClick = {
                    isGlobalQuickActionSheetOpen = false
                    onAddNewItemClick()
                },
                onSaveItemClick = {
                    isGlobalQuickActionSheetOpen = false
                    onAddNewItemClick()
                },
                onContextPrimaryAction = { _, _ -> isGlobalQuickActionSheetOpen = false }
            )
        }
    }
}

@Composable
fun ChankoBottomBar(
    currentRoute: String?,
    onTabSelected: (TopLevelDestination) -> Unit,
    onAddNewItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp + navBarBottomPadding)
    ) {
        // Surface bar with top rounded corners
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp + navBarBottomPadding)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: Trang chủ
                ChankoNavItem(
                    destination = TopLevelDestination.Home,
                    icon = Icons.Rounded.Home,
                    isSelected = currentRoute == TopLevelDestination.Home.route,
                    onClick = { onTabSelected(TopLevelDestination.Home) },
                    modifier = Modifier.weight(1f)
                )

                // Tab 2: Món đồ
                ChankoNavItem(
                    destination = TopLevelDestination.MyItems,
                    icon = Icons.AutoMirrored.Rounded.List,
                    isSelected = currentRoute == TopLevelDestination.MyItems.route,
                    onClick = { onTabSelected(TopLevelDestination.MyItems) },
                    modifier = Modifier.weight(1f)
                )

                // Central Gap for floating (+) button
                Spacer(modifier = Modifier.width(68.dp))

                // Tab 3: Ngân sách
                ChankoNavItem(
                    destination = TopLevelDestination.Budget,
                    icon = Icons.Rounded.AccountBalanceWallet,
                    isSelected = currentRoute == TopLevelDestination.Budget.route,
                    onClick = { onTabSelected(TopLevelDestination.Budget) },
                    modifier = Modifier.weight(1f)
                )

                // Tab 4: Phân tích
                ChankoNavItem(
                    destination = TopLevelDestination.Insights,
                    icon = Icons.Rounded.Insights,
                    isSelected = currentRoute == TopLevelDestination.Insights.route,
                    onClick = { onTabSelected(TopLevelDestination.Insights) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Center Floating Action Button (+)
        val addCd = stringResource(R.string.home_cd_add_item)
        FloatingActionButton(
            onClick = onAddNewItemClick,
            shape = CircleShape,
            containerColor = CandyColors.brandPurple,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
                .size(56.dp)
                .semantics { contentDescription = addCd }
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun ChankoNavItem(
    destination: TopLevelDestination,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val label = stringResource(destination.labelRes)

    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                role = Role.Tab,
                onClick = onClick
            )
            .semantics {
                this.selected = isSelected
                this.contentDescription = label
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .then(
                        if (isSelected) Modifier
                            .background(CandyColors.primarySoft)
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                        else Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) CandyColors.primaryInk else CandyColors.textSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight(700) else FontWeight(600)
                ),
                color = if (isSelected) CandyColors.primaryInk else CandyColors.textSecondary
            )
        }
    }
}
