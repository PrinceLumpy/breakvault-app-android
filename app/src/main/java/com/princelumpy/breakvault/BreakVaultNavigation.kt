package com.princelumpy.breakvault

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

sealed class Screen(
    val route: String,
    @StringRes val labelResId: Int? = null,
    val icon: ImageVector? = null
) {
    data object Main : Screen("main")
    data object MoveList :
        Screen("move_list", R.string.screen_label_moves, Icons.AutoMirrored.Filled.List)

    data object PracticeComboList :
        Screen("practice_combos_list", R.string.screen_label_practice_combos, Icons.Filled.Science)

    data object BattleComboList :
        Screen("battle_combo_list", R.string.screen_label_battle, Icons.Filled.FlashOn)

    data object TagList : Screen("tag_list")
    data object BattleTagList : Screen("battle_tag_list")
    data object Settings : Screen("settings", R.string.screen_label_settings, Icons.Filled.Settings)
    data object ComboGenerator :
        Screen("combo_generator", R.string.screen_label_combo_generator, Icons.Filled.PlayArrow)

    data object AddEditMove : Screen("add_edit_move")
    data object AddEditPracticeCombo : Screen("add_edit_practice_combo")
    data object AddEditBattleCombo : Screen("add_edit_battle_combo")
}

val bottomNavItems = listOf(
    Screen.MoveList,
    Screen.PracticeComboList,
    Screen.BattleComboList
)

object BreakVaultDestinationsArgs {
    const val MOVE_ID_ARG = "moveId"
    const val COMBO_ID_ARG = "comboId"
}

object BreakVaultDestinations {
    val MOVE_LIST_ROUTE = Screen.MoveList.route
    val PRACTICE_COMBOS_LIST_ROUTE = Screen.PracticeComboList.route
    val BATTLE_COMBO_LIST_ROUTE = Screen.BattleComboList.route
    val TAG_LIST_ROUTE = Screen.TagList.route
    val BATTLE_TAG_LIST_ROUTE = Screen.BattleTagList.route
    val SETTINGS_ROUTE = Screen.Settings.route
    val COMBO_GENERATOR_ROUTE = Screen.ComboGenerator.route
    val ADD_EDIT_MOVE_ROUTE =
        "${Screen.AddEditMove.route}?${BreakVaultDestinationsArgs.MOVE_ID_ARG}={${BreakVaultDestinationsArgs.MOVE_ID_ARG}}"
    val ADD_EDIT_PRACTICE_COMBO_ROUTE =
        "${Screen.AddEditPracticeCombo.route}?${BreakVaultDestinationsArgs.COMBO_ID_ARG}={${BreakVaultDestinationsArgs.COMBO_ID_ARG}}"
    val ADD_EDIT_BATTLE_COMBO_ROUTE =
        "${Screen.AddEditBattleCombo.route}?${BreakVaultDestinationsArgs.COMBO_ID_ARG}={${BreakVaultDestinationsArgs.COMBO_ID_ARG}}"
}

class BreakVaultNavigationActions(private val navController: NavHostController) {
    fun navigateTo(screen: Screen) {
        navController.navigate(screen.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateToComboGenerator() {
        navController.navigate(Screen.ComboGenerator.route)
    }

    fun navigateToMoveTagList() {
        navController.navigate(Screen.TagList.route) {
            popUpTo(BreakVaultDestinations.MOVE_LIST_ROUTE) {
                inclusive = false
            }
        }
    }

    fun navigateToBattleTagList() {
        navController.navigate(Screen.BattleTagList.route) {
            popUpTo(BreakVaultDestinations.MOVE_LIST_ROUTE) {
                inclusive = false
            }
        }
    }

    fun navigateToBattleTagListDirect() {
        navController.navigate(Screen.BattleTagList.route)
    }

    fun navigateToAddEditMove(moveId: String?) {
        val route = if (moveId != null) {
            "${Screen.AddEditMove.route}?${BreakVaultDestinationsArgs.MOVE_ID_ARG}=$moveId"
        } else {
            Screen.AddEditMove.route
        }
        navController.navigate(route)
    }

    fun navigateToAddEditPracticeCombo(comboId: String?) {
        val route = if (comboId != null) {
            "${Screen.AddEditPracticeCombo.route}?${BreakVaultDestinationsArgs.COMBO_ID_ARG}=$comboId"
        } else {
            Screen.AddEditPracticeCombo.route
        }
        navController.navigate(route)
    }

    fun navigateToAddEditBattleCombo(comboId: String?) {
        val route = if (comboId != null) {
            "${Screen.AddEditBattleCombo.route}?${BreakVaultDestinationsArgs.COMBO_ID_ARG}=$comboId"
        } else {
            Screen.AddEditBattleCombo.route
        }
        navController.navigate(route)
    }

    fun navigateUp() {
        navController.popBackStack()
    }
}
