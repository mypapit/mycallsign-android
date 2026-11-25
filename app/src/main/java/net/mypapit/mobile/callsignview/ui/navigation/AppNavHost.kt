package net.mypapit.mobile.callsignview.ui.navigation

import android.net.Uri
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import net.mypapit.mobile.callsignview.AppContainer
import net.mypapit.mobile.callsignview.ui.about.AboutScreen
import net.mypapit.mobile.callsignview.ui.aprs.AprsMapScreen
import net.mypapit.mobile.callsignview.ui.aprs.AprsMapViewModel
import net.mypapit.mobile.callsignview.ui.callsigns.CallsignListScreen
import net.mypapit.mobile.callsignview.ui.callsigns.CallsignListViewModel
import net.mypapit.mobile.callsignview.ui.components.MasterDetailPane
import net.mypapit.mobile.callsignview.ui.components.classifyWindowWidth
import net.mypapit.mobile.callsignview.ui.detail.CallsignDetailPane
import net.mypapit.mobile.callsignview.ui.detail.CallsignDetailScreen
import net.mypapit.mobile.callsignview.ui.favorites.FavoritesScreen
import net.mypapit.mobile.callsignview.ui.onboarding.OnboardingScreen
import net.mypapit.mobile.callsignview.ui.settings.SettingsScreen
import net.mypapit.mobile.callsignview.ui.stats.ExpiredCallsignsScreen
import net.mypapit.mobile.callsignview.ui.stats.StatsScreen
import net.mypapit.mobile.callsignview.ui.theme.AppMotion
import net.mypapit.mobile.callsignview.ui.theme.OrangeAccent
import net.mypapit.mobile.callsignview.ui.theme.OrangeContainer

private object Routes {
    const val CALLSIGNS = "callsigns"
    const val FAVORITES = "favorites"
    const val APRS_MAP = "aprs_map"
    const val STATS = "stats"
    const val EXPIRED_5Y = "expired_5y"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val DETAIL = "detail/{callsign}"

    fun detail(callsign: String): String = "detail/${Uri.encode(callsign)}"
}

private data class TopLevelTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun MYCallsignApp(container: AppContainer) {
    val prefsState by container.preferences.preferencesFlow.collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()

    // Preload callsign directory on first launch in background without blocking onboarding or asking permissions
    LaunchedEffect(Unit) {
        container.callsignRepository.preloadOnFirstLaunchIfEmpty()
    }

    val prefs = prefsState
    if (prefs == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = OrangeAccent)
        }
        return
    }

    var replayOnboarding by rememberSaveable { mutableStateOf(false) }

    if (!prefs.onboardingCompleted || replayOnboarding) {
        OnboardingScreen(
            initialAprsEnabled = prefs.aprsEnabled,
            onFinish = { enableAprs ->
                scope.launch {
                    container.preferences.setAprsEnabled(enableAprs)
                    container.preferences.setOnboardingCompleted(true)
                    replayOnboarding = false
                }
            }
        )
        return
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    var callsignsSelected by rememberSaveable { mutableStateOf<String?>(null) }
    var favoritesSelected by rememberSaveable { mutableStateOf<String?>(null) }
    var expiredSelected by rememberSaveable { mutableStateOf<String?>(null) }

    val tabs = buildList {
        add(TopLevelTab(Routes.CALLSIGNS, "Callsigns", Icons.Filled.Radio, Icons.Outlined.Radio))
        add(TopLevelTab(Routes.FAVORITES, "Favorites", Icons.Filled.Star, Icons.Outlined.StarBorder))
        if (prefs.aprsEnabled) {
            add(TopLevelTab(Routes.APRS_MAP, "APRS Map", Icons.Filled.Map, Icons.Outlined.Map))
        }
        add(TopLevelTab(Routes.STATS, "Stats", Icons.Filled.BarChart, Icons.Outlined.BarChart))
    }

    val topLevelRoutes = setOf(Routes.CALLSIGNS, Routes.FAVORITES, Routes.APRS_MAP, Routes.STATS)
    val showTopLevelChrome = currentDestination?.route in topLevelRoutes

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val layoutType = classifyWindowWidth(maxWidth.value.toInt())

        if (layoutType.useNavigationRail) {
            Row(modifier = Modifier.fillMaxSize()) {
                if (showTopLevelChrome) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight(),
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Spacer(modifier = Modifier.statusBarsPadding().padding(top = 12.dp))
                        tabs.forEach { tab ->
                            val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = OrangeAccent,
                                    selectedTextColor = OrangeAccent,
                                    indicatorColor = OrangeContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }

                AppNavHost(
                    navController = navController,
                    container = container,
                    isTwoPane = layoutType.isTwoPane,
                    callsignsSelected = callsignsSelected,
                    onSelectCallsign = { callsignsSelected = it },
                    favoritesSelected = favoritesSelected,
                    onSelectFavorite = { favoritesSelected = it },
                    expiredSelected = expiredSelected,
                    onSelectExpired = { expiredSelected = it },
                    onNavigateTab = ::navigateToTab,
                    onReplayOnboarding = { replayOnboarding = true },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        } else {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    if (showTopLevelChrome) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            tabs.forEach { tab ->
                                val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateToTab(tab.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.label
                                        )
                                    },
                                    label = { Text(tab.label) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = OrangeAccent,
                                        selectedTextColor = OrangeAccent,
                                        indicatorColor = OrangeContainer,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                AppNavHost(
                    navController = navController,
                    container = container,
                    isTwoPane = false,
                    callsignsSelected = callsignsSelected,
                    onSelectCallsign = { callsignsSelected = it },
                    favoritesSelected = favoritesSelected,
                    onSelectFavorite = { favoritesSelected = it },
                    expiredSelected = expiredSelected,
                    onSelectExpired = { expiredSelected = it },
                    onNavigateTab = ::navigateToTab,
                    onReplayOnboarding = { replayOnboarding = true },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    container: AppContainer,
    isTwoPane: Boolean,
    callsignsSelected: String?,
    onSelectCallsign: (String?) -> Unit,
    favoritesSelected: String?,
    onSelectFavorite: (String?) -> Unit,
    expiredSelected: String?,
    onSelectExpired: (String?) -> Unit,
    onNavigateTab: (String) -> Unit,
    onReplayOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = Routes.CALLSIGNS,
        modifier = modifier,
        enterTransition = {
            fadeIn() + slideInHorizontally(
                animationSpec = AppMotion.ExpressiveIntOffsetSpring,
                initialOffsetX = { it / 12 }
            )
        },
        exitTransition = { fadeOut() },
        popEnterTransition = {
            fadeIn() + slideInHorizontally(
                animationSpec = AppMotion.ExpressiveIntOffsetSpring,
                initialOffsetX = { -it / 12 }
            )
        },
        popExitTransition = { fadeOut() + slideOutHorizontally { it / 12 } }
    ) {
        composable(Routes.CALLSIGNS) {
            val vm: CallsignListViewModel = viewModel(
                factory = CallsignListViewModel.factory(
                    repository = container.callsignRepository,
                    preferences = container.preferences
                )
            )
            MasterDetailPane(
                isTwoPane = isTwoPane,
                selectedCallsign = callsignsSelected,
                masterContent = { masterModifier ->
                    CallsignListScreen(
                        viewModel = vm,
                        onCallsignClick = { cs ->
                            if (isTwoPane) {
                                onSelectCallsign(cs)
                            } else {
                                navController.navigate(Routes.detail(cs))
                            }
                        },
                        onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                        onOpenAbout = { navController.navigate(Routes.ABOUT) },
                        selectedCallsign = if (isTwoPane) callsignsSelected else null,
                        modifier = masterModifier
                    )
                },
                detailContent = { cs, detailModifier ->
                    CallsignDetailPane(
                        callsign = cs,
                        repository = container.callsignRepository,
                        showBackButton = false,
                        onBack = {},
                        modifier = detailModifier
                    )
                }
            )
        }

        composable(Routes.FAVORITES) {
            MasterDetailPane(
                isTwoPane = isTwoPane,
                selectedCallsign = favoritesSelected,
                masterContent = { masterModifier ->
                    FavoritesScreen(
                        repository = container.callsignRepository,
                        onCallsignClick = { cs ->
                            if (isTwoPane) {
                                onSelectFavorite(cs)
                            } else {
                                navController.navigate(Routes.detail(cs))
                            }
                        },
                        selectedCallsign = if (isTwoPane) favoritesSelected else null,
                        modifier = masterModifier
                    )
                },
                detailContent = { cs, detailModifier ->
                    CallsignDetailPane(
                        callsign = cs,
                        repository = container.callsignRepository,
                        showBackButton = false,
                        onBack = {},
                        modifier = detailModifier
                    )
                }
            )
        }

        composable(Routes.APRS_MAP) {
            val vm: AprsMapViewModel = viewModel(
                factory = AprsMapViewModel.factory(
                    aprsRepository = container.aprsRepository,
                    preferences = container.preferences
                )
            )
            AprsMapScreen(
                viewModel = vm,
                onViewCallsignDetail = { cs ->
                    if (isTwoPane) {
                        onSelectCallsign(cs)
                        onNavigateTab(Routes.CALLSIGNS)
                    } else {
                        navController.navigate(Routes.detail(cs))
                    }
                },
                onDisableAprsInSettings = {
                    scope.launch {
                        container.preferences.setAprsEnabled(false)
                        onNavigateTab(Routes.CALLSIGNS)
                    }
                }
            )
        }

        composable(Routes.STATS) {
            StatsScreen(
                repository = container.callsignRepository,
                onOpenExpiredOver5Years = { navController.navigate(Routes.EXPIRED_5Y) }
            )
        }

        composable(Routes.EXPIRED_5Y) {
            MasterDetailPane(
                isTwoPane = isTwoPane,
                selectedCallsign = expiredSelected,
                masterContent = { masterModifier ->
                    ExpiredCallsignsScreen(
                        repository = container.callsignRepository,
                        onCallsignClick = { cs ->
                            if (isTwoPane) {
                                onSelectExpired(cs)
                            } else {
                                navController.navigate(Routes.detail(cs))
                            }
                        },
                        onBack = { navController.popBackStack() },
                        selectedCallsign = if (isTwoPane) expiredSelected else null,
                        modifier = masterModifier
                    )
                },
                detailContent = { cs, detailModifier ->
                    CallsignDetailPane(
                        callsign = cs,
                        repository = container.callsignRepository,
                        showBackButton = false,
                        onBack = {},
                        modifier = detailModifier
                    )
                }
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("callsign") { type = NavType.StringType })
        ) { entry ->
            val callsign = Uri.decode(entry.arguments?.getString("callsign").orEmpty())
            CallsignDetailScreen(
                callsign = callsign,
                repository = container.callsignRepository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                repository = container.callsignRepository,
                preferences = container.preferences,
                onOpenAbout = { navController.navigate(Routes.ABOUT) },
                onReplayOnboarding = onReplayOnboarding,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}
