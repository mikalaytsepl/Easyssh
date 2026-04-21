package com.example.easyssh

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.ui.graphics.vector.ImageVector

// ── Screen routes ────────────────────────────────────────────

sealed class Screen(val route: String) {
    // Bottom-nav destinations
    object Dashboard : Screen("dashboard")
    object Servers   : Screen("servers")
    object Keys      : Screen("keys")
    object Snippets  : Screen("snippets")

    // Detail / tool screens (no bottom nav)
    object Terminal    : Screen("terminal/{serverId}") {
        fun createRoute(serverId: String) = "terminal/$serverId"
    }
    object Diagnostics : Screen("diagnostics")
    object Tunnel      : Screen("tunnel")
    object Academy     : Screen("academy")
}

// ── Bottom navigation items ──────────────────────────────────

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, "Home",     Icons.Filled.Home),
    BottomNavItem(Screen.Servers,   "Serwery",  Icons.Outlined.AccountBox),
    BottomNavItem(Screen.Keys,      "Klucze",   Icons.Outlined.Lock),
    BottomNavItem(Screen.Snippets,  "Snippety", Icons.Outlined.Info),
)

// ── Which routes show the bottom nav ────────────────────────

val bottomNavRoutes = bottomNavItems.map { it.screen.route }.toSet()
