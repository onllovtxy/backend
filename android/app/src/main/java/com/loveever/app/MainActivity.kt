package com.loveever.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.loveever.app.ui.screens.*
import com.loveever.app.ui.theme.LoveEverTheme
import com.loveever.app.viewmodel.LoveViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "首页", Icons.Default.Favorite)
    object Anniversaries : Screen("anniversaries", "清单", Icons.Default.DateRange)
    object Memories : Screen("memories", "时光墙", Icons.Default.Star)
    object Profile : Screen("profile", "我的", Icons.Default.Person)
}

class MainActivity : ComponentActivity() {
    private val viewModel: LoveViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoveEverTheme {
                val navController = rememberNavController()
                val items = listOf(
                    Screen.Home,
                    Screen.Anniversaries,
                    Screen.Memories,
                    Screen.Profile
                )

                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentRoute = navBackStackEntry?.destination?.route

                            items.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = screen.title) },
                                    label = { Text(screen.title) },
                                    selected = currentRoute == screen.route,
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFFF43F5E),
                                        selectedTextColor = Color(0xFFF43F5E),
                                        indicatorColor = Color(0xFFFFF1F2)
                                    ),
                                    onClick = {
                                        if (currentRoute != screen.route) {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.startDestinationId) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Home.route) { HomeScreen(viewModel) }
                        composable(Screen.Anniversaries.route) { AnniversariesScreen(viewModel) }
                        composable(Screen.Memories.route) { MemoriesScreen(viewModel) }
                        composable(Screen.Profile.route) { ProfileScreen(viewModel) }
                    }
                }
            }
        }
    }
}
