package com.example.easyssh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import com.example.easyssh.ui.screens.*
import com.example.easyssh.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EasysshTheme {
                EasySshApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EasySshApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Removed "Terminal (Lokalny)" from the list
    val allDrawerScreens = listOf(
        "Home (Dashboard)" to Screen.Dashboard.route,
        "Serwery" to Screen.Servers.route,
        "Klucze" to Screen.Keys.route,
        "Snippety" to Screen.Snippets.route,
        "Diagnostyka" to Screen.Diagnostics.route,
        "Tunel" to Screen.Tunnel.route,
        "Akademia" to Screen.Academy.route
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = BgDeep
            ) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "EasySSH Menu",
                    color = AccentGreen,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace
                )
                HorizontalDivider(color = AccentBlue.copy(alpha = 0.2f))
                Spacer(Modifier.height(8.dp))

                allDrawerScreens.forEach { (label, route) ->
                    val isSelected = currentRoute == route ||
                            (route.startsWith("terminal") && currentRoute?.startsWith("terminal") == true)

                    NavigationDrawerItem(
                        label = { Text(text = label, fontFamily = FontFamily.Monospace) },
                        selected = isSelected,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent,
                            selectedContainerColor = AccentBlue.copy(alpha = 0.1f),
                            selectedTextColor = AccentGreen,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {

        Scaffold(
            containerColor = BgDeep,
            bottomBar = {
                if (currentRoute in bottomNavRoutes) {
                    EasySshBottomNav(
                        currentRoute = currentRoute,
                        onItemClick  = { screen ->
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController    = navController,
                startDestination = Screen.Dashboard.route,
                modifier         = Modifier.padding(innerPadding),
            ) {
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        onNavigateToServer      = { serverId ->
                            navController.navigate(Screen.Terminal.createRoute(serverId))
                        },
                        onNavigateToDiagnostics = { navController.navigate(Screen.Diagnostics.route) },
                        onNavigateToTunnel      = { navController.navigate(Screen.Tunnel.route) },
                        onNavigateToAcademy     = { navController.navigate(Screen.Academy.route) },
                    )
                }

                composable(Screen.Servers.route) {
                    ServersScreen(onNavigateToTerminal = { id -> navController.navigate(Screen.Terminal.createRoute(id)) })
                }

                composable(Screen.Keys.route) { KeysScreen() }
                composable(Screen.Snippets.route) { SnippetsScreen() }

                composable(
                    route     = Screen.Terminal.route,
                    arguments = listOf(navArgument("serverId") { type = NavType.StringType }),
                ) { backStackEntry ->
                    val serverId = backStackEntry.arguments?.getString("serverId") ?: "unknown"
                    TerminalScreen(serverId = serverId)
                }

                composable(Screen.Diagnostics.route) { DiagnosticsScreen() }
                composable(Screen.Tunnel.route) { TunnelScreen() }
                composable(Screen.Academy.route) { AcademyScreen() }
            }
        }
    }
}


@Composable
fun EasySshBottomNav(
    currentRoute: String?,
    onItemClick: (Screen) -> Unit,
) {
    NavigationBar(
        containerColor = Surface2,
        tonalElevation = 0.dp,
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.screen.route
            NavigationBarItem(
                selected = selected,
                onClick  = { onItemClick(item.screen) },
                icon = {
                    Icon(
                        imageVector        = item.icon,
                        contentDescription = item.label,
                        tint = if (selected) AccentGreen else TextTertiary,
                    )
                },
                label = {
                    Text(
                        text       = item.label,
                        fontFamily = FontFamily.Monospace,
                        fontSize   = 9.sp,
                        color      = if (selected) AccentGreen else TextTertiary,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color(0x2200FF88),
                ),
            )
        }
    }
}
