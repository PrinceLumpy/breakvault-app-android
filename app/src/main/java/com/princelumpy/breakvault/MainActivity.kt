package com.princelumpy.breakvault // Updated package

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
// import androidx.compose.material.icons.filled.AddCircle // Already replaced by School for Flashcard
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
// import androidx.compose.material.icons.filled.Settings // Already replaced by Label for TagList
import androidx.compose.material.icons.filled.Label // Suggested new icon for Tags
import androidx.compose.material.icons.filled.School // Suggested new icon for Flashcards
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.navArgument

// Updated imports for project classes
import com.princelumpy.breakvault.R // Added R class import
import com.princelumpy.breakvault.ui.screens.AddEditMoveScreen
import com.princelumpy.breakvault.ui.screens.ComboGeneratorScreen
import com.princelumpy.breakvault.ui.screens.FlashcardScreen
import com.princelumpy.breakvault.ui.screens.MoveListScreen
import com.princelumpy.breakvault.ui.screens.SavedCombosScreen
import com.princelumpy.breakvault.ui.screens.SettingsScreen
import com.princelumpy.breakvault.ui.screens.TagListScreen
import com.princelumpy.breakvault.ui.theme.ComboGeneratorTheme

sealed class Screen(val route: String, @StringRes val labelResId: Int? = null, val icon: ImageVector? = null) {
    object MoveList : Screen("move_list", R.string.screen_label_moves, Icons.AutoMirrored.Filled.List)
    object SavedCombos : Screen("saved_combos", R.string.screen_label_saved_combos, Icons.Filled.Favorite)
    object TagList : Screen("tag_list", R.string.screen_label_tags, Icons.Filled.Label)
    object Settings : Screen("settings", R.string.screen_label_settings, Icons.Filled.Build)
    object AddEditMove : Screen("add_edit_move", R.string.screen_label_add_edit_move)
    object ComboGenerator : Screen("combo_generator", R.string.screen_label_combo_generator, Icons.Filled.PlayArrow)
    object Flashcard : Screen("flashcard", R.string.screen_label_flashcards, Icons.Filled.School)

    fun withOptionalArgs(map: Map<String, String>): String {
        return buildString {
            append(route)
            if (map.isNotEmpty()) {
                append("?")
                append(map.entries.joinToString("&") { "${it.key}=${it.value}" })
            }
        }
    }
}

val bottomNavItems = listOf(
    Screen.MoveList,
    Screen.SavedCombos,
    Screen.TagList,
    Screen.Settings
)

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

@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                bottomNavItems.forEach { screen ->
                    screen.icon?.let { // Ensure icon is not null for bottom nav items
                        val itemLabel = screen.labelResId?.let { stringResource(id = it) } ?: ""
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = itemLabel) },
                            label = { Text(itemLabel) },
                            selected = currentDestination?.hierarchy?.any {
                                it.route == screen.route || currentDestination.route?.startsWith(
                                    screen.route + "?"
                                ) == true
                            } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = false // <--- Changed from true to false
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
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.MoveList.route) { MoveListScreen(navController = navController) }
            composable(Screen.SavedCombos.route) { SavedCombosScreen() } 
            composable(Screen.TagList.route) { TagListScreen() } 
            composable(Screen.Settings.route) { SettingsScreen() } 

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
            composable(Screen.ComboGenerator.route) { ComboGeneratorScreen(navController = navController) }
            composable(Screen.Flashcard.route) { FlashcardScreen(navController = navController) }
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
