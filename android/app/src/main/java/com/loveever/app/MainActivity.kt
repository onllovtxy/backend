package com.loveever.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.loveever.app.ui.screens.*
import com.loveever.app.ui.theme.LoveEverTheme
import com.loveever.app.viewmodel.AuthState
import com.loveever.app.viewmodel.LoveViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Conversations : Screen("conversations", "消息", Icons.AutoMirrored.Filled.Chat)
    object Home : Screen("home", "首页", Icons.Filled.Favorite)
    object Memories : Screen("memories", "时光墙", Icons.Filled.PhotoCamera)
    object Anniversaries : Screen("anniversaries", "纪念日", Icons.Filled.CalendarMonth)
    object Profile : Screen("profile", "我的", Icons.Filled.Person)
}

class MainActivity : ComponentActivity() {
    private val viewModel: LoveViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoveEverTheme {
                val authState by viewModel.auth.collectAsState()
                val error by viewModel.error.collectAsState()
                val info by viewModel.info.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(error) {
                    error?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.clearError()
                    }
                }
                LaunchedEffect(info) {
                    info?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.clearInfo()
                    }
                }

                when (val state = authState) {
                    is AuthState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    is AuthState.LoggedOut -> {
                        AuthScreen(viewModel)
                    }
                    is AuthState.LoggedIn -> {
                        val navController = rememberNavController()
                        val unreadCount by viewModel.unreadCount.collectAsState()
                        val items = listOf(
                            Screen.Conversations,
                            Screen.Home,
                            Screen.Memories,
                            Screen.Anniversaries,
                            Screen.Profile
                        )

                        Scaffold(
                            snackbarHost = { SnackbarHost(snackbarHostState) },
                            bottomBar = {
                                val navBackStackEntry by navController.currentBackStackEntryAsState()
                                val currentRoute = navBackStackEntry?.destination?.route
                                if (currentRoute != "chat") {
                                    NavigationBar(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ) {
                                        items.forEach { screen ->
                                            NavigationBarItem(
                                                icon = {
                                                    Box {
                                                        Icon(
                                                            screen.icon,
                                                            contentDescription = screen.title
                                                        )
                                                        if (screen == Screen.Conversations && unreadCount > 0) {
                                                            Badge {
                                                                Text("$unreadCount")
                                                            }
                                                        }
                                                    }
                                                },
                                                label = { Text(screen.title) },
                                                selected = currentRoute == screen.route,
                                                colors = NavigationBarItemDefaults.colors(
                                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
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
                            }
                        ) { innerPadding ->
                            NavHost(
                                navController = navController,
                                startDestination = Screen.Conversations.route,
                                modifier = Modifier.padding(innerPadding)
                            ) {
                                composable(Screen.Conversations.route) {
                                    ConversationScreen(
                                        viewModel = viewModel,
                                        onOpenChat = { navController.navigate("chat") }
                                    )
                                }
                                composable(Screen.Home.route) { HomeScreen(viewModel) }
                                composable(Screen.Memories.route) { MemoriesScreen(viewModel) }
                                composable(Screen.Anniversaries.route) { AnniversariesScreen(viewModel) }
                                composable(Screen.Profile.route) { ProfileScreen(viewModel) }
                                composable("chat") {
                                    ChatScreen(
                                        viewModel = viewModel,
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
