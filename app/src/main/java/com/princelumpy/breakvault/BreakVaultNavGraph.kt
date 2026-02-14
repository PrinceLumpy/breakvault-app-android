package com.princelumpy.breakvault

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
    onOpenDrawer: () -> Unit = {},
    startDestination: String = BreakVaultDestinations.MOVE_LIST_ROUTE,
    navActions: BreakVaultNavigationActions = remember(navController) {
        BreakVaultNavigationActions(navController)
    }
) {
    // Define screen categories for different transition types
    val addEditScreens = setOf(
        BreakVaultDestinations.ADD_EDIT_MOVE_ROUTE,
        BreakVaultDestinations.ADD_EDIT_COMBO_ROUTE,
        BreakVaultDestinations.ADD_EDIT_GOAL_ROUTE,
        BreakVaultDestinations.ADD_EDIT_GOAL_STAGE_ROUTE,
        BreakVaultDestinations.ADD_EDIT_BATTLE_COMBO_ROUTE
    )

    val managementScreens = setOf(
        BreakVaultDestinations.TAG_LIST_ROUTE,
        BreakVaultDestinations.MOVES_BY_TAG_ROUTE,
        BreakVaultDestinations.BATTLE_TAG_LIST_ROUTE,
        BreakVaultDestinations.SETTINGS_ROUTE,
        BreakVaultDestinations.ARCHIVED_GOALS_ROUTE
    )

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            val targetRoute = targetState.destination.route
            val initialRoute = initialState.destination.route

            // Get indices in bottom nav bar
            val targetIndex = bottomNavItems.indexOfFirst { it.route == targetRoute }
            val initialIndex = bottomNavItems.indexOfFirst { it.route == initialRoute }

            when {
                // Bottom nav transitions: horizontal slide based on position
                targetIndex != -1 && initialIndex != -1 -> {
                    if (targetIndex > initialIndex) {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = tween(300)
                        )
                    } else {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = tween(300)
                        )
                    }
                }
                // Add/Edit screens: slide up from bottom
                addEditScreens.any { targetRoute?.startsWith(it.substringBefore('?')) == true } -> {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Up,
                        animationSpec = tween(300)
                    )
                }
                // Management screens: slide from right
                managementScreens.any { targetRoute?.startsWith(it.substringBefore('?')) == true } -> {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(300)
                    )
                }
                // Default: fade
                else -> fadeIn(animationSpec = tween(150))
            }
        },
        exitTransition = {
            val targetRoute = targetState.destination.route
            val initialRoute = initialState.destination.route

            val targetIndex = bottomNavItems.indexOfFirst { it.route == targetRoute }
            val initialIndex = bottomNavItems.indexOfFirst { it.route == initialRoute }

            when {
                // Bottom nav transitions
                targetIndex != -1 && initialIndex != -1 -> {
                    if (targetIndex > initialIndex) {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = tween(300)
                        )
                    } else {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = tween(300)
                        )
                    }
                }
                // Add/Edit screens: no exit transition
                addEditScreens.any { targetRoute?.startsWith(it.substringBefore('?')) == true } -> {
                    ExitTransition.None
                }
                // Management screens: no exit transition
                managementScreens.any { targetRoute?.startsWith(it.substringBefore('?')) == true } -> {
                    ExitTransition.None
                }
                // Default: None
                else -> ExitTransition.None
            }
        },
        popEnterTransition = {
            val targetRoute = targetState.destination.route
            val initialRoute = initialState.destination.route

            // Check if we're returning to a bottom nav item
            val targetIndex = bottomNavItems.indexOfFirst { it.route == targetRoute }
            val initialIndex = bottomNavItems.indexOfFirst { it.route == initialRoute }

            when {
                // Returning to a bottom nav item from another bottom nav item
                targetIndex != -1 && initialIndex != -1 -> {
                    if (targetIndex < initialIndex) {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = tween(300)
                        )
                    } else {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = tween(300)
                        )
                    }
                }
                // Coming back from Add/Edit screens: fade in
                addEditScreens.any { initialRoute?.startsWith(it.substringBefore('?')) == true } -> {
                    fadeIn(animationSpec = tween(150))
                }
                // Coming back from management screens: fade in
                managementScreens.any { initialRoute?.startsWith(it.substringBefore('?')) == true } -> {
                    fadeIn(animationSpec = tween(150))
                }
                // Default: fade
                else -> fadeIn(animationSpec = tween(150))
            }
        },
        popExitTransition = {
            val targetRoute = targetState.destination.route
            val initialRoute = initialState.destination.route

            val targetIndex = bottomNavItems.indexOfFirst { it.route == targetRoute }
            val initialIndex = bottomNavItems.indexOfFirst { it.route == initialRoute }

            when {
                // Exiting from a bottom nav item to another bottom nav item
                targetIndex != -1 && initialIndex != -1 -> {
                    if (targetIndex < initialIndex) {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = tween(300)
                        )
                    } else {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = tween(300)
                        )
                    }
                }
                // Add/Edit screens: slide down when dismissed
                addEditScreens.any { initialRoute?.startsWith(it.substringBefore('?')) == true } -> {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Down,
                        animationSpec = tween(300)
                    )
                }
                // Management screens: slide to right when going back
                managementScreens.any { initialRoute?.startsWith(it.substringBefore('?')) == true } -> {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(300)
                    )
                }
                // Default: fade
                else -> fadeOut(animationSpec = tween(150))
            }
        }
    ) {
        composable(
            BreakVaultDestinations.MOVE_LIST_ROUTE
        ) {
            MoveListScreen(
                onNavigateToAddEditMove = { navActions.navigateToAddEditMove(it) },
                onNavigateToComboGenerator = { navActions.navigateToComboGenerator() },
                onNavigateToMoveTagList = { navActions.navigateToMoveTagList() },
                onOpenDrawer = onOpenDrawer
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
                onNavigateToAddEditCombo = { navActions.navigateToAddEditCombo(it) },
                onNavigateToComboGenerator = { navActions.navigateToComboGenerator() },
                onOpenDrawer = onOpenDrawer
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
                onNavigateToAddEditStage = { goalId, stageId ->
                    navActions.navigateToAddEditGoalStage(goalId, stageId)
                },
                onOpenDrawer = onOpenDrawer
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
                onNavigateUp = { navActions.navigateFromNewStageToParentGoal(goalId) },
                goalId = goalId,
                stageId = stageId
            )
        }
        composable(BreakVaultDestinations.ARCHIVED_GOALS_ROUTE) {
            ArchivedGoalsScreen(
                onNavigateUp = { navActions.navigateUp() },
            )
        }
        composable(BreakVaultDestinations.TIMER_ROUTE) { TimerScreen(onOpenDrawer = onOpenDrawer) }
        composable(BreakVaultDestinations.BATTLE_ROUTE) {
            BattleComboListScreen(
                onNavigateToAddEditBattleCombo = { navActions.navigateToAddEditBattleCombo(it) },
                onNavigateToBattleTagList = { navActions.navigateToBattleTagListDirect() },
                onOpenDrawer = onOpenDrawer
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
