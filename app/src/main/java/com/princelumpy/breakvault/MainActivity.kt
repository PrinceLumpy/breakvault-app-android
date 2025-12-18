package com.princelumpy.breakvault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.princelumpy.breakvault.ui.battlecombos.addedit.AddEditBattleComboScreen
import com.princelumpy.breakvault.ui.savedcombos.addedit.AddEditComboScreen
import com.princelumpy.breakvault.ui.goals.addedit.AddEditGoalScreen
import com.princelumpy.breakvault.ui.goals.addedit.stage.AddEditGoalStageScreen
import com.princelumpy.breakvault.ui.moves.addedit.AddEditMoveScreen
import com.princelumpy.breakvault.ui.goals.archived.ArchivedGoalsScreen
import com.princelumpy.breakvault.ui.battlecombos.list.BattleComboListScreen
import com.princelumpy.breakvault.ui.battlecombos.managetags.BattleTagListScreen
import com.princelumpy.breakvault.ui.combogenerator.ComboGeneratorScreen
import com.princelumpy.breakvault.ui.goals.list.GoalsScreen
import com.princelumpy.breakvault.ui.moves.list.MoveListScreen
import com.princelumpy.breakvault.ui.moves.managetags.MoveTagListScreen
import com.princelumpy.breakvault.ui.moves.movesbytag.MovesByTagScreen
import com.princelumpy.breakvault.ui.savedcombos.list.SavedCombosScreen
import com.princelumpy.breakvault.ui.settings.SettingsScreen
import com.princelumpy.breakvault.ui.timer.TimerScreen
import com.princelumpy.breakvault.ui.theme.ComboGeneratorTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

sealed class Screen(
    val route: String,
    @StringRes val labelResId: Int? = null,
    val icon: ImageVector? = null
) {
    object MoveList :
        Screen("move_list", R.string.screen_label_moves, Icons.AutoMirrored.Filled.List)

    object SavedCombos :
        Screen("saved_combos", R.string.screen_label_saved_combos, Icons.Filled.Favorite)

    object Goals : Screen("goals", R.string.screen_label_goals, Icons.Filled.Flag)
    object Timer : Screen("timer", R.string.screen_label_timer, Icons.Filled.Timer)
    object Battle : Screen("battle_mode", R.string.screen_label_battle, Icons.Filled.FlashOn)

    object TagList : Screen("tag_list")
    object ArchivedGoals : Screen("archived_goals")
    object Settings : Screen("settings", R.string.screen_label_settings, Icons.Filled.Settings)
    object ComboGenerator :
        Screen("combo_generator", R.string.screen_label_combo_generator, Icons.Filled.PlayArrow)

    object Flashcard : Screen("flashcard", R.string.screen_label_flashcards, Icons.Filled.School)
    object AddEditMove : Screen("create_edit_move")
    object AddEditCombo : Screen("create_edit_combo")
    object MovesByTag : Screen("moves_by_tag")
    object AddEditBattleCombo : Screen("add_edit_battle_combo")
    object BattleTagList : Screen("battle_tag_list")
    object AddEditGoal : Screen("edit_goal")
    object AddEditGoalStage : Screen("add_edit_goal_stage")

    fun withOptionalArgs(map: Map<String, String>): String {
        return buildString {
            append(route)
            if (map.isNotEmpty()) {
                append("?")
                append(map.entries.joinToString("&") { "${it.key}=${it.value}" })
            }
        }
    }

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}

val bottomNavItems = listOf(
    Screen.MoveList,
    Screen.SavedCombos,
    Screen.Goals,
    Screen.Timer,
    Screen.Battle
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComboGeneratorTheme {
                MainAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var navigating by remember { mutableStateOf(false) }

    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

    LaunchedEffect(drawerState.currentValue) {
        if (drawerState.currentValue == DrawerValue.Closed) {
            navigating = false
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
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
                        if (navigating) return@NavigationDrawerItem
                        navigating = true

                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.TagList.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )

                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.drawer_item_battle_tags)) },
                    selected = false,
                    onClick = {
                        if (navigating) return@NavigationDrawerItem
                        navigating = true

                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.BattleTagList.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )

                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.drawer_item_archived_goals)) },
                    selected = false,
                    onClick = {
                        if (navigating) return@NavigationDrawerItem
                        navigating = true

                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.ArchivedGoals.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )

                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.screen_label_settings)) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    selected = false,
                    onClick = {
                        if (navigating) return@NavigationDrawerItem
                        navigating = true

                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Settings.route) {
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
    ) {
        Scaffold(
            topBar = {
                if (showBottomBar) {
                    CenterAlignedTopAppBar(
                        title = { Text(stringResource(id = R.string.app_name)) },
                        navigationIcon = {
                            IconButton(onClick = {
                                if (drawerState.isClosed) {
                                    scope.launch { drawerState.open() }
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu"
                                )
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (showBottomBar) {
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
                startDestination = Screen.MoveList.route,
                modifier = Modifier.padding(
                    top = innerPadding.calculateTopPadding() / (3) * 2,
                    bottom = innerPadding.calculateBottomPadding()
                )
            ) {
                composable(Screen.MoveList.route) { MoveListScreen(navController = navController) }
                composable(Screen.TagList.route) { MoveTagListScreen(navController = navController) }
                composable(Screen.ComboGenerator.route) { ComboGeneratorScreen(navController = navController) }
                composable(Screen.SavedCombos.route) { SavedCombosScreen(navController = navController) }
                composable(Screen.Goals.route) { GoalsScreen(navController = navController) }
                composable(Screen.Timer.route) { TimerScreen(navController = navController) }
                composable(Screen.Battle.route) { BattleComboListScreen(navController = navController) }
                composable(Screen.BattleTagList.route) { BattleTagListScreen(navController = navController) }
                composable(Screen.ArchivedGoals.route) { ArchivedGoalsScreen(navController = navController) }
                composable(Screen.Settings.route) { SettingsScreen(navController = navController) }
                composable(
                    route = Screen.AddEditMove.route + "?moveId={moveId}",
                    arguments = listOf(navArgument("moveId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    })
                ) { backStackEntry ->
                    val moveId = backStackEntry.arguments?.getString("moveId")
                    AddEditMoveScreen(navController = navController, moveId = moveId)
                }

                composable(
                    route = Screen.AddEditCombo.route + "?comboId={comboId}",
                    arguments = listOf(navArgument("comboId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    })
                ) { backStackEntry ->
                    val comboId = backStackEntry.arguments?.getString("comboId")
                    AddEditComboScreen(navController = navController, comboId = comboId)
                }

                composable(
                    route = Screen.AddEditGoal.route + "?goalId={goalId}",
                    arguments = listOf(navArgument("goalId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    })
                ) { backStackEntry ->
                    val goalId = backStackEntry.arguments?.getString("goalId")
                    AddEditGoalScreen(navController = navController, goalId = goalId)
                }

                composable(
                    route = Screen.AddEditGoalStage.route + "?goalId={goalId}&stageId={stageId}",
                    arguments = listOf(
                        navArgument("goalId") { type = NavType.StringType },
                        navArgument("stageId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val goalId = backStackEntry.arguments?.getString("goalId") ?: ""
                    val stageId = backStackEntry.arguments?.getString("stageId")
                    AddEditGoalStageScreen(
                        navController = navController,
                        goalId = goalId,
                        stageId = stageId
                    )
                }

                composable(
                    route = Screen.MovesByTag.route + "/{tagId}/{tagName}",
                    arguments = listOf(
                        navArgument("tagId") { type = NavType.StringType },
                        navArgument("tagName") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val tagId = backStackEntry.arguments?.getString("tagId") ?: ""
                    val tagName = backStackEntry.arguments?.getString("tagName") ?: ""
                    MovesByTagScreen(
                        navController = navController,
                        tagId = tagId,
                        tagName = tagName
                    )
                }

                composable(
                    route = Screen.AddEditBattleCombo.route + "?comboId={comboId}",
                    arguments = listOf(navArgument("comboId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    })
                ) { backStackEntry ->
                    val comboId = backStackEntry.arguments?.getString("comboId")
                    AddEditBattleComboScreen(navController = navController, comboId = comboId)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ComboGeneratorTheme {
        MainAppScreen()
    }
}
