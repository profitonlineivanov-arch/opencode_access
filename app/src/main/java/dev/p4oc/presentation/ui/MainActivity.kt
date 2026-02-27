package dev.p4oc.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.p4oc.domain.model.ServerConfig
import dev.p4oc.presentation.ui.screens.ChatScreen
import dev.p4oc.presentation.ui.screens.FilesScreen
import dev.p4oc.presentation.ui.screens.QrScannerScreen
import dev.p4oc.presentation.ui.screens.SettingsScreen
import dev.p4oc.presentation.ui.theme.P4OCTheme
import dev.p4oc.presentation.viewmodel.SettingsViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settingsState by settingsViewModel.uiState.collectAsState()

            val darkTheme = when (settingsState.theme) {
                dev.p4oc.domain.repository.ThemeMode.DARK -> true
                dev.p4oc.domain.repository.ThemeMode.LIGHT -> false
                dev.p4oc.domain.repository.ThemeMode.SYSTEM -> null
            }

            P4OCTheme(
                darkTheme = darkTheme ?: androidx.compose.foundation.isSystemInDarkTheme()
            ) {
                MainScreen()
            }
        }
    }
}

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Chat : Screen("chat", "Chat", Icons.Default.Chat)
    data object Files : Screen("files", "Files", Icons.Default.Folder)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object QrScanner : Screen("qr_scanner", "QR Scanner", Icons.Default.QrCodeScanner)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val screens = listOf(Screen.Chat, Screen.Files, Screen.Settings)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in screens.map { it.route }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val currentDestination = navBackStackEntry?.destination

                    screens.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Chat.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Chat.route) {
                ChatScreen(
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onNavigateToQrScanner = {
                        navController.navigate(Screen.QrScanner.route)
                    }
                )
            }

            composable(Screen.Files.route) {
                FilesScreen()
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }

            composable(Screen.QrScanner.route) {
                QrScannerScreen(
                    onQrCodeScanned = { url ->
                        navController.previousBackStackEntry?.savedStateHandle?.set("scanned_url", url)
                        navController.popBackStack()
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
