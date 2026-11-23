package com.cezila.truckcockpit

import androidx.annotation.StringRes
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cezila.truckcockpit.core.ui.component.CockpitTabBar
import com.cezila.truckcockpit.feature.dashboard.DashboardRoute
import com.cezila.truckcockpit.feature.trip.TripRoute
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
data object DashboardDestination

@Serializable
data object TripDestination

private enum class Tab(val route: Any, val routeClass: KClass<*>, @StringRes val label: Int) {
    Dashboard(DashboardDestination, DashboardDestination::class, R.string.tab_dashboard),
    Trip(TripDestination, TripDestination::class, R.string.tab_trip),
}

/**
 * The two screens with a tab bar below. The tab bar stays in driving mode: switching screens is a
 * single large tap, and each screen has its own driving layout.
 */
@Composable
fun TruckCockpitApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val selected = Tab.entries.indexOfFirst { tab -> backStackEntry?.destination?.hasRoute(tab.routeClass) == true }
        .coerceAtLeast(0)
    val bottomInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)

    Column(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = DashboardDestination,
            // The tab bar takes the bottom inset; screens only pad for the rest.
            modifier = Modifier
                .weight(1f)
                .consumeWindowInsets(bottomInsets),
            // No decorative animations: the screen switches at once.
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        ) {
            composable<DashboardDestination> { DashboardRoute() }
            composable<TripDestination> { TripRoute() }
        }
        CockpitTabBar(
            tabs = Tab.entries.map { stringResource(it.label) },
            selectedIndex = selected,
            onSelect = { index -> navController.navigateToTab(Tab.entries[index]) },
            modifier = Modifier.windowInsetsPadding(bottomInsets),
        )
    }
}

/** Standard tab navigation: one copy of each tab on the back stack, each keeping its state. */
private fun NavHostController.navigateToTab(tab: Tab) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
