package com.princelumpy.breakvault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.princelumpy.breakvault.ui.theme.BreakVaultTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BreakVaultActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BreakVaultTheme {
                AppRoot()
            }
        }
    }
}

/**
 * Outer NavHost containing Main destination and all overlay screens.
 */
@Composable
fun AppRoot() {
    val outerNavController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        NavHost(
            navController = outerNavController,
            startDestination = Screen.Main.route,
            modifier = Modifier.fillMaxSize()
        ) {
            // Main destination - contains bottom nav experience
            composable(Screen.Main.route) {
                MainAppScreen(outerNavController = outerNavController)
            }

            // All overlay screens
            overlayNavGraph(navController = outerNavController)
        }
    }
}

/**
 * Main screen containing drawer, bottom nav, and inner NavHost with bottom nav screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(outerNavController: NavHostController) {
    val innerNavController = rememberNavController()
    val innerNavActions = remember(innerNavController) {
        BreakVaultNavigationActions(innerNavController)
    }
    val outerNavActions = remember(outerNavController) {
        BreakVaultNavigationActions(outerNavController)
    }

    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val configuration = LocalConfiguration.current
    val drawerWidth = (configuration.screenWidthDp * 0.8f).dp

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(drawerWidth)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    IconButton(onClick = { scope.launch { drawerState.close() } }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Close Drawer"
                        )
                    }
                }
                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.drawer_item_practice_tags)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        outerNavActions.navigateTo(Screen.TagList)
                    }
                )

                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.drawer_item_battle_tags)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        outerNavActions.navigateToBattleTagList()
                    }
                )

                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.drawer_item_archived_goals)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        outerNavActions.navigateToArchivedGoals()
                    }
                )

                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.screen_label_settings)) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        outerNavActions.navigateTo(Screen.Settings)
                    }
                )
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        val itemLabel = screen.labelResId?.let { stringResource(id = it) } ?: ""
                        val isSelected = currentDestination?.hierarchy?.any {
                            it.route == screen.route
                        } == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    screen.icon ?: Icons.Filled.Build,
                                    contentDescription = itemLabel
                                )
                            },
                            label = { Text(itemLabel) },
                            selected = isSelected,
                            onClick = { innerNavActions.navigateTo(screen) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            BottomNavGraph(
                modifier = Modifier.padding(innerPadding),
                navController = innerNavController,
                outerNavController = outerNavController,
                onOpenDrawer = { scope.launch { drawerState.open() } }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    BreakVaultTheme {
        AppRoot()
    }
}
