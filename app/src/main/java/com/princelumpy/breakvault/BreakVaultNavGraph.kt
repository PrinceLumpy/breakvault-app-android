package com.princelumpy.breakvault

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.princelumpy.breakvault.ui.battlecombos.addedit.AddEditBattleComboScreen
import com.princelumpy.breakvault.ui.battlecombos.list.BattleComboListScreen
import com.princelumpy.breakvault.ui.battlecombos.managetags.BattleTagListScreen
import com.princelumpy.breakvault.ui.combogenerator.ComboGeneratorScreen
import com.princelumpy.breakvault.ui.goals.addedit.AddEditGoalScreen
import com.princelumpy.breakvault.ui.goals.addedit.stage.AddEditGoalStageScreen
import com.princelumpy.breakvault.ui.goals.archived.ArchivedGoalsScreen
import com.princelumpy.breakvault.ui.goals.list.GoalsScreen
import com.princelumpy.breakvault.ui.moves.addedit.AddEditMoveScreen
import com.princelumpy.breakvault.ui.moves.list.MoveListScreen
import com.princelumpy.breakvault.ui.moves.managetags.MoveTagListScreen
import com.princelumpy.breakvault.ui.moves.movesbytag.MovesByTagScreen
import com.princelumpy.breakvault.ui.savedcombos.addedit.AddEditComboScreen
import com.princelumpy.breakvault.ui.savedcombos.list.SavedComboListScreen
import com.princelumpy.breakvault.ui.settings.SettingsScreen
import com.princelumpy.breakvault.ui.timer.TimerScreen

@Composable
fun BreakVaultNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String = BreakVaultDestinations.MOVE_LIST_ROUTE,
    navActions: BreakVaultNavigationActions = remember(navController) {
        BreakVaultNavigationActions(navController)
    }
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(
            BreakVaultDestinations.MOVE_LIST_ROUTE
        ) {
            MoveListScreen(
                onNavigateToAddEditMove = { navActions.navigateToAddEditMove(it) },
                onNavigateToComboGenerator = { navActions.navigateToComboGenerator() },
                onNavigateToTagList = { navActions.navigateToTagList() }
            )
        }
        composable(BreakVaultDestinations.TAG_LIST_ROUTE) {
            MoveTagListScreen(
                onNavigateToMovesByTag = { tagId, tagName ->
                    navActions.navigateToMovesByTag(tagId, tagName)
                },
                onNavigateBack = {
                    navActions.navigateUp()
                }
            )
        }
        composable(
            route = BreakVaultDestinations.MOVES_BY_TAG_ROUTE,
            arguments = listOf(
                navArgument(BreakVaultDestinationsArgs.TAG_ID_ARG) { type = NavType.StringType },
                navArgument(BreakVaultDestinationsArgs.TAG_NAME_ARG) { type = NavType.StringType }
            )
        ) {
            MovesByTagScreen(
                onNavigateUp = { navActions.navigateUp() },
                onNavigateToMove = { navActions.navigateToAddEditMove(it) },
            )
        }
        composable(
            route = BreakVaultDestinations.ADD_EDIT_MOVE_ROUTE,
            arguments = listOf(navArgument(BreakVaultDestinationsArgs.MOVE_ID_ARG) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val moveId = backStackEntry.arguments?.getString(BreakVaultDestinationsArgs.MOVE_ID_ARG)
            AddEditMoveScreen(
                onNavigateUp = { navActions.navigateUp() },
                moveId = moveId
            )
        }
        composable(BreakVaultDestinations.COMBO_GENERATOR_ROUTE) {
            ComboGeneratorScreen(
                onNavigateUp = { navActions.navigateUp() }
            )
        }
        composable(BreakVaultDestinations.SAVED_COMBOS_ROUTE) {
            SavedComboListScreen(
                onNavigateToAddEditCombo = { navActions.navigateToAddEditCombo(it) }
            )
        }
        composable(
            route = BreakVaultDestinations.ADD_EDIT_COMBO_ROUTE,
            arguments = listOf(navArgument(BreakVaultDestinationsArgs.COMBO_ID_ARG) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val comboId =
                backStackEntry.arguments?.getString(BreakVaultDestinationsArgs.COMBO_ID_ARG)
            AddEditComboScreen(
                onNavigateUp = { navActions.navigateUp() },
                comboId = comboId
            )
        }
        composable(BreakVaultDestinations.GOALS_ROUTE) {
            GoalsScreen(
                onNavigateToAddEditGoal = { navActions.navigateToAddEditGoal(it) },
                onNavigateToArchivedGoals = { navActions.navigateToArchivedGoals() }
            )
        }
        composable(
            route = BreakVaultDestinations.ADD_EDIT_GOAL_ROUTE,
            arguments = listOf(navArgument(BreakVaultDestinationsArgs.GOAL_ID_ARG) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) {
            AddEditGoalScreen(
                onNavigateUp = { navActions.navigateUp() },
                onNavigateToAddEditStage = { gId, stageId ->
                    navActions.navigateToAddEditGoalStage(
                        gId,
                        stageId
                    )
                }
            )
        }
        composable(
            route = BreakVaultDestinations.ADD_EDIT_GOAL_STAGE_ROUTE,
            arguments = listOf(
                navArgument(BreakVaultDestinationsArgs.GOAL_ID_ARG) { type = NavType.StringType },
                navArgument(BreakVaultDestinationsArgs.STAGE_ID_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val goalId =
                backStackEntry.arguments?.getString(BreakVaultDestinationsArgs.GOAL_ID_ARG) ?: ""
            val stageId =
                backStackEntry.arguments?.getString(BreakVaultDestinationsArgs.STAGE_ID_ARG)
            AddEditGoalStageScreen(
                onNavigateUp = { navActions.navigateUp() },
                goalId = goalId,
                stageId = stageId
            )
        }
        composable(BreakVaultDestinations.ARCHIVED_GOALS_ROUTE) {
            ArchivedGoalsScreen(
                onNavigateUp = { navActions.navigateUp() },
            )
        }
        composable(BreakVaultDestinations.TIMER_ROUTE) { TimerScreen() }
        composable(BreakVaultDestinations.BATTLE_ROUTE) {
            BattleComboListScreen(
                onNavigateUp = { navActions.navigateUp() },
                onNavigateToAddEditBattleCombo = { navActions.navigateToAddEditBattleCombo(it) },
                onNavigateToBattleTagList = { navActions.navigateToBattleTagList() }
            )
        }
        composable(
            route = BreakVaultDestinations.ADD_EDIT_BATTLE_COMBO_ROUTE,
            arguments = listOf(navArgument(BreakVaultDestinationsArgs.COMBO_ID_ARG) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val comboId =
                backStackEntry.arguments?.getString(BreakVaultDestinationsArgs.COMBO_ID_ARG)
            AddEditBattleComboScreen(
                onNavigateUp = { navActions.navigateUp() },
                comboId = comboId
            )
        }
        composable(BreakVaultDestinations.BATTLE_TAG_LIST_ROUTE) {
            BattleTagListScreen(
                onNavigateUp = { navActions.navigateUp() }
            )
        }

        composable(BreakVaultDestinations.SETTINGS_ROUTE) {
            SettingsScreen(
                onNavigateUp = { navActions.navigateUp() }
            )
        }
    }
}
