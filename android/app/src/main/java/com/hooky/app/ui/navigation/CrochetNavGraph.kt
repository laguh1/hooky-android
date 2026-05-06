package com.hooky.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hooky.app.R
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hooky.app.ui.calculator.PriceCalculatorScreen
import com.hooky.app.ui.camera.CameraScreen
import com.hooky.app.ui.dashboard.DashboardScreen
import com.hooky.app.ui.needles.detail.NeedleDetailScreen
import com.hooky.app.ui.needles.form.NeedleFormScreen
import com.hooky.app.ui.needles.list.NeedleListScreen
import com.hooky.app.ui.pieces.detail.PieceDetailScreen
import com.hooky.app.ui.pieces.form.PieceFormScreen
import com.hooky.app.ui.pieces.list.PieceListScreen
import com.hooky.app.ui.search.SearchScreen
import com.hooky.app.ui.stitches.detail.StitchDetailScreen
import com.hooky.app.ui.stitches.form.StitchFormScreen
import com.hooky.app.ui.stitches.list.StitchListScreen
import com.hooky.app.ui.yarns.detail.YarnDetailScreen
import com.hooky.app.ui.yarns.form.YarnFormScreen
import com.hooky.app.ui.yarns.list.YarnListScreen
import com.hooky.app.ui.settings.SettingsScreen
import com.hooky.app.ui.theme.Slate

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
    object NeedleList : Screen("needles")
    object NeedleDetail : Screen("needles/{id}") {
        fun createRoute(id: Int) = "needles/$id"
    }
    object NeedleCreate : Screen("needles/create")
    object NeedleEdit : Screen("needles/{id}/edit") {
        fun createRoute(id: Int) = "needles/$id/edit"
    }
    object Camera : Screen("camera")
    object Search : Screen("search")
    object PriceCalculator : Screen("calculator")
    object Settings : Screen("settings")
}

private data class TopLevelDestination(
    val screen: Screen,
    val icon: ImageVector? = null,
    @DrawableRes val iconRes: Int? = null,
    @StringRes val labelRes: Int
)

private val topLevelDestinations = listOf(
    TopLevelDestination(Screen.Dashboard, icon = Icons.Filled.Dashboard, labelRes = R.string.nav_dashboard),
    TopLevelDestination(Screen.PieceList, iconRes = R.drawable.ic_pieces, labelRes = R.string.nav_pieces),
    TopLevelDestination(Screen.YarnList, iconRes = R.drawable.ic_yarns, labelRes = R.string.nav_yarns),
    TopLevelDestination(Screen.StitchList, iconRes = R.drawable.ic_stitches, labelRes = R.string.nav_stitches),
    TopLevelDestination(Screen.NeedleList, iconRes = R.drawable.ic_needles, labelRes = R.string.nav_needles),
)

private val topLevelRoutes = setOf(
    Screen.Dashboard.route,
    Screen.PieceList.route,
    Screen.YarnList.route,
    Screen.StitchList.route,
    Screen.NeedleList.route
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
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            // Pieces
            composable(Screen.PieceList.route) {
                PieceListScreen(
                    onNavigateToDetail = { id -> navController.navigate(Screen.PieceDetail.createRoute(id)) },
                    onNavigateToCreate = { navController.navigate(Screen.PieceCreate.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToPriceCalculator = { navController.navigate(Screen.PriceCalculator.route) }
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
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
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
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
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

            // Needles
            composable(Screen.NeedleList.route) {
                NeedleListScreen(
                    onNavigateToDetail = { id -> navController.navigate(Screen.NeedleDetail.createRoute(id)) },
                    onNavigateToCreate = { navController.navigate(Screen.NeedleCreate.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }
            composable(
                route = Screen.NeedleDetail.route,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: return@composable
                NeedleDetailScreen(
                    needleId = id,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { needleId ->
                        navController.navigate(Screen.NeedleEdit.createRoute(needleId))
                    }
                )
            }
            composable(Screen.NeedleCreate.route) {
                NeedleFormScreen(
                    needleId = null,
                    onNavigateBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                    onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                    navController = navController
                )
            }
            composable(
                route = Screen.NeedleEdit.route,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: return@composable
                NeedleFormScreen(
                    needleId = id,
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

            composable(Screen.Settings.route) {
                SettingsScreen(
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
                    Box(contentAlignment = Alignment.Center) {
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp, 32.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Slate)
                            )
                        }
                        val iconTint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        if (destination.iconRes != null) {
                            Icon(
                                painter = painterResource(destination.iconRes),
                                contentDescription = stringResource(destination.labelRes),
                                tint = iconTint
                            )
                        } else {
                            Icon(
                                imageVector = destination.icon!!,
                                contentDescription = stringResource(destination.labelRes),
                                tint = iconTint
                            )
                        }
                    }
                },
                label = {
                    Text(
                        text = stringResource(destination.labelRes),
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
