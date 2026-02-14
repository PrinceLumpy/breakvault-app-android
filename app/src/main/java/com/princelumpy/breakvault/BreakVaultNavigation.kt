package com.princelumpy.breakvault

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
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

    data object SavedCombos :
        Screen("saved_combos", R.string.screen_label_saved_combos, Icons.Filled.Favorite)

    data object Goals : Screen("goals", R.string.screen_label_goals, Icons.Filled.Flag)
    data object Timer : Screen("timer", R.string.screen_label_timer, Icons.Filled.Timer)
    data object Battle : Screen("battle_mode", R.string.screen_label_battle, Icons.Filled.FlashOn)
    data object TagList : Screen("tag_list")
    data object ArchivedGoals : Screen("archived_goals")
    data object Settings : Screen("settings", R.string.screen_label_settings, Icons.Filled.Settings)
    data object ComboGenerator :
        Screen("combo_generator", R.string.screen_label_combo_generator, Icons.Filled.PlayArrow)

    data object AddEditMove : Screen("create_edit_move")
    data object AddEditCombo : Screen("create_edit_combo")
    data object MovesByTag : Screen("moves_by_tag")
    data object AddEditBattleCombo : Screen("add_edit_battle_combo")
    data object BattleTagList : Screen("battle_tag_list")
    data object AddEditGoal : Screen("edit_goal")
    data object AddEditGoalStage : Screen("add_edit_goal_stage")
}

val bottomNavItems = listOf(
    Screen.MoveList,
    Screen.SavedCombos,
    Screen.Goals,
    Screen.Timer,
    Screen.Battle
)

object BreakVaultDestinationsArgs {
    const val MOVE_ID_ARG = "moveId"
    const val COMBO_ID_ARG = "comboId"
    const val GOAL_ID_ARG = "goalId"
    const val STAGE_ID_ARG = "stageId"
    const val TAG_ID_ARG = "tagId"
    const val TAG_NAME_ARG = "tagName"
}

object BreakVaultDestinations {
    val MOVE_LIST_ROUTE = Screen.MoveList.route
    val SAVED_COMBOS_ROUTE = Screen.SavedCombos.route
    val GOALS_ROUTE = Screen.Goals.route
    val TIMER_ROUTE = Screen.Timer.route
    val BATTLE_ROUTE = Screen.Battle.route
    val TAG_LIST_ROUTE = Screen.TagList.route
    val ARCHIVED_GOALS_ROUTE = Screen.ArchivedGoals.route
    val SETTINGS_ROUTE = Screen.Settings.route
    val COMBO_GENERATOR_ROUTE = Screen.ComboGenerator.route
    val BATTLE_TAG_LIST_ROUTE = Screen.BattleTagList.route
    val ADD_EDIT_MOVE_ROUTE =
        "${Screen.AddEditMove.route}?${BreakVaultDestinationsArgs.MOVE_ID_ARG}={${BreakVaultDestinationsArgs.MOVE_ID_ARG}}"
    val ADD_EDIT_COMBO_ROUTE =
        "${Screen.AddEditCombo.route}?${BreakVaultDestinationsArgs.COMBO_ID_ARG}={${BreakVaultDestinationsArgs.COMBO_ID_ARG}}"
    val ADD_EDIT_GOAL_ROUTE =
        "${Screen.AddEditGoal.route}?${BreakVaultDestinationsArgs.GOAL_ID_ARG}={${BreakVaultDestinationsArgs.GOAL_ID_ARG}}"
    val ADD_EDIT_GOAL_STAGE_ROUTE =
        "${Screen.AddEditGoalStage.route}?${BreakVaultDestinationsArgs.GOAL_ID_ARG}={${BreakVaultDestinationsArgs.GOAL_ID_ARG}}&${BreakVaultDestinationsArgs.STAGE_ID_ARG}={${BreakVaultDestinationsArgs.STAGE_ID_ARG}}"
    val MOVES_BY_TAG_ROUTE =
        "${Screen.MovesByTag.route}/{${BreakVaultDestinationsArgs.TAG_ID_ARG}}/{${BreakVaultDestinationsArgs.TAG_NAME_ARG}}"
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

    fun navigateToArchivedGoals() {
        navController.navigate(Screen.ArchivedGoals.route) {
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

    fun navigateToAddEditCombo(comboId: String?) {
        val route = if (comboId != null) {
            "${Screen.AddEditCombo.route}?${BreakVaultDestinationsArgs.COMBO_ID_ARG}=$comboId"
        } else {
            Screen.AddEditCombo.route
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

    fun navigateToMovesByTag(tagId: String, tagName: String) {
        navController.navigate("${Screen.MovesByTag.route}/$tagId/$tagName")
    }

    fun navigateToAddEditGoal(goalId: String?) {
        val route = if (goalId != null) {
            "${Screen.AddEditGoal.route}?${BreakVaultDestinationsArgs.GOAL_ID_ARG}=$goalId"
        } else {
            Screen.AddEditGoal.route
        }
        navController.navigate(route)
    }

    fun navigateFromNewStageToParentGoal(goalId: String) {
        val route = "${Screen.AddEditGoal.route}?${BreakVaultDestinationsArgs.GOAL_ID_ARG}=$goalId"
        navController.navigate(route) {
            popUpTo(BreakVaultDestinations.GOALS_ROUTE) {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    fun navigateToAddEditGoalStage(goalId: String, stageId: String?) {
        var route =
            "${Screen.AddEditGoalStage.route}?${BreakVaultDestinationsArgs.GOAL_ID_ARG}=$goalId"
        if (stageId != null) {
            route += "&${BreakVaultDestinationsArgs.STAGE_ID_ARG}=$stageId"
        }
        navController.navigate(route)
    }

    fun navigateUp() {
        navController.popBackStack()
    }
}
