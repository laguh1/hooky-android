package com.crochet.manager.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.crochet.manager.ui.calculator.PriceCalculatorScreen
import com.crochet.manager.ui.camera.CameraScreen
import com.crochet.manager.ui.dashboard.DashboardScreen
import com.crochet.manager.ui.pieces.detail.PieceDetailScreen
import com.crochet.manager.ui.pieces.form.PieceFormScreen
import com.crochet.manager.ui.pieces.list.PieceListScreen
import com.crochet.manager.ui.search.SearchScreen
import com.crochet.manager.ui.stitches.detail.StitchDetailScreen
import com.crochet.manager.ui.stitches.form.StitchFormScreen
import com.crochet.manager.ui.stitches.list.StitchListScreen
import com.crochet.manager.ui.yarns.detail.YarnDetailScreen
import com.crochet.manager.ui.yarns.form.YarnFormScreen
import com.crochet.manager.ui.yarns.list.YarnListScreen
import com.crochet.manager.ui.theme.Slate

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object PieceList : Screen("pieces")
    object PieceDetail : Screen("pieces/{id}") {
        fun createRoute(id: Int) = "pieces/$id"
    }
    object PieceCreate : Screen("pieces/create")
    object PieceEdit : Screen("pieces/{id}/edit") {
        fun createRoute(id: Int) = "pieces/$id/edit"
    }
    object YarnList : Screen("yarns")
    object YarnDetail : Screen("yarns/{id}") {
        fun createRoute(id: Int) = "yarns/$id"
    }
    object YarnCreate : Screen("yarns/create")
    object YarnEdit : Screen("yarns/{id}/edit") {
        fun createRoute(id: Int) = "yarns/$id/edit"
    }
    object StitchList : Screen("stitches")
    object StitchDetail : Screen("stitches/{id}") {
        fun createRoute(id: Int) = "stitches/$id"
    }
    object StitchCreate : Screen("stitches/create")
    object StitchEdit : Screen("stitches/{id}/edit") {
        fun createRoute(id: Int) = "stitches/$id/edit"
    }
    object Camera : Screen("camera")
    object Search : Screen("search")
    object PriceCalculator : Screen("calculator")
}

private data class TopLevelDestination(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

private val topLevelDestinations = listOf(
    TopLevelDestination(Screen.Dashboard, Icons.Filled.Dashboard, "Dashboard"),
    TopLevelDestination(Screen.PieceList, Icons.Filled.GridView, "Pieces"),
    TopLevelDestination(Screen.YarnList, Icons.Filled.Palette, "Yarns"),
    TopLevelDestination(Screen.StitchList, Icons.Filled.Straighten, "Stitches"),
)

private val topLevelRoutes = setOf(
    Screen.Dashboard.route,
    Screen.PieceList.route,
    Screen.YarnList.route,
    Screen.StitchList.route
)

@Composable
fun CrochetNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val showBottomBar = currentRoute in topLevelRoutes && currentRoute != Screen.Camera.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                CrochetBottomBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToPieces = { navController.navigate(Screen.PieceList.route) },
                    onNavigateToYarns = { navController.navigate(Screen.YarnList.route) },
                    onNavigateToStitches = { navController.navigate(Screen.StitchList.route) },
                    onNavigateToPieceDetail = { id -> navController.navigate(Screen.PieceDetail.createRoute(id)) },
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                    onNavigateToPriceCalculator = { navController.navigate(Screen.PriceCalculator.route) }
                )
            }

            // Pieces
            composable(Screen.PieceList.route) {
                PieceListScreen(
                    onNavigateToDetail = { id -> navController.navigate(Screen.PieceDetail.createRoute(id)) },
                    onNavigateToCreate = { navController.navigate(Screen.PieceCreate.route) },
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) }
                )
            }
            composable(
                route = Screen.PieceDetail.route,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: return@composable
                PieceDetailScreen(
                    pieceId = id,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { pieceId ->
                        navController.navigate(Screen.PieceEdit.createRoute(pieceId))
                    }
                )
            }
            composable(Screen.PieceCreate.route) {
                PieceFormScreen(
                    pieceId = null,
                    onNavigateBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                    onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                    navController = navController
                )
            }
            composable(
                route = Screen.PieceEdit.route,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: return@composable
                PieceFormScreen(
                    pieceId = id,
                    onNavigateBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                    onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                    navController = navController
                )
            }

            // Yarns
            composable(Screen.YarnList.route) {
                YarnListScreen(
                    onNavigateToDetail = { id -> navController.navigate(Screen.YarnDetail.createRoute(id)) },
                    onNavigateToCreate = { navController.navigate(Screen.YarnCreate.route) },
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) }
                )
            }
            composable(
                route = Screen.YarnDetail.route,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: return@composable
                YarnDetailScreen(
                    yarnId = id,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { yarnId ->
                        navController.navigate(Screen.YarnEdit.createRoute(yarnId))
                    }
                )
            }
            composable(Screen.YarnCreate.route) {
                YarnFormScreen(
                    yarnId = null,
                    onNavigateBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                    onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                    navController = navController
                )
            }
            composable(
                route = Screen.YarnEdit.route,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: return@composable
                YarnFormScreen(
                    yarnId = id,
                    onNavigateBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                    onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                    navController = navController
                )
            }

            // Stitches
            composable(Screen.StitchList.route) {
                StitchListScreen(
                    onNavigateToDetail = { id -> navController.navigate(Screen.StitchDetail.createRoute(id)) },
                    onNavigateToCreate = { navController.navigate(Screen.StitchCreate.route) },
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) }
                )
            }
            composable(
                route = Screen.StitchDetail.route,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: return@composable
                StitchDetailScreen(
                    stitchId = id,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { stitchId ->
                        navController.navigate(Screen.StitchEdit.createRoute(stitchId))
                    }
                )
            }
            composable(Screen.StitchCreate.route) {
                StitchFormScreen(
                    stitchId = null,
                    onNavigateBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                    onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                    navController = navController
                )
            }
            composable(
                route = Screen.StitchEdit.route,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: return@composable
                StitchFormScreen(
                    stitchId = id,
                    onNavigateBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                    onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                    navController = navController
                )
            }

            // Search — global cross-entity search
            composable(Screen.Search.route) {
                SearchScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPieceDetail = { id -> navController.navigate(Screen.PieceDetail.createRoute(id)) },
                    onNavigateToYarnDetail = { id -> navController.navigate(Screen.YarnDetail.createRoute(id)) },
                    onNavigateToStitchDetail = { id -> navController.navigate(Screen.StitchDetail.createRoute(id)) }
                )
            }

            // Price Calculator
            composable(Screen.PriceCalculator.route) {
                PriceCalculatorScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Camera screen — full-screen, no bottom bar
            composable(Screen.Camera.route) {
                CameraScreen(
                    onPhotoTaken = { path ->
                        // Pass result back to the calling form screen via savedStateHandle
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("photo_path", path)
                        navController.popBackStack()
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun CrochetBottomBar(
    navController: NavHostController,
    currentRoute: String?
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        topLevelDestinations.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any {
                it.route == destination.screen.route
            } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp, 32.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Slate)
                            )
                        }
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label,
                            tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                label = {
                    Text(
                        text = destination.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) Slate else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
