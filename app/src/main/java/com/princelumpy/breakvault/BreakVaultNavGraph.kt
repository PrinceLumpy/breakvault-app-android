package com.princelumpy.breakvault

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
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

/**
 * Inner NavHost containing only the 5 bottom nav screens.
 * Used inside MainAppScreen with bottom bar.
 */
@Composable
fun BottomNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    outerNavController: NavHostController,
    onOpenDrawer: () -> Unit = {},
) {
    val outerNavActions = remember(outerNavController) {
        BreakVaultNavigationActions(outerNavController)
    }

    NavHost(
        navController = navController,
        startDestination = BreakVaultDestinations.MOVE_LIST_ROUTE,
        modifier = modifier,
        enterTransition = {
            val targetRoute = targetState.destination.route
            val initialRoute = initialState.destination.route

            // Get indices in bottom nav bar
            val targetIndex = bottomNavItems.indexOfFirst { it.route == targetRoute }
            val initialIndex = bottomNavItems.indexOfFirst { it.route == initialRoute }

            // Horizontal slide based on position
            if (targetIndex != -1 && initialIndex != -1) {
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
            } else {
                fadeIn(animationSpec = tween(150))
            }
        },
        exitTransition = {
            val targetRoute = targetState.destination.route
            val initialRoute = initialState.destination.route

            val targetIndex = bottomNavItems.indexOfFirst { it.route == targetRoute }
            val initialIndex = bottomNavItems.indexOfFirst { it.route == initialRoute }

            if (targetIndex != -1 && initialIndex != -1) {
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
            } else {
                ExitTransition.None
            }
        },
        popEnterTransition = {
            val targetRoute = targetState.destination.route
            val initialRoute = initialState.destination.route

            val targetIndex = bottomNavItems.indexOfFirst { it.route == targetRoute }
            val initialIndex = bottomNavItems.indexOfFirst { it.route == initialRoute }

            if (targetIndex != -1 && initialIndex != -1) {
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
            } else {
                fadeIn(animationSpec = tween(150))
            }
        },
        popExitTransition = {
            val targetRoute = targetState.destination.route
            val initialRoute = initialState.destination.route

            val targetIndex = bottomNavItems.indexOfFirst { it.route == targetRoute }
            val initialIndex = bottomNavItems.indexOfFirst { it.route == initialRoute }

            if (targetIndex != -1 && initialIndex != -1) {
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
            } else {
                ExitTransition.None
            }
        }
    ) {
        // Bottom nav screens only
        composable(BreakVaultDestinations.MOVE_LIST_ROUTE) {
            MoveListScreen(
                onNavigateToAddEditMove = { outerNavActions.navigateToAddEditMove(it) },
                onNavigateToComboGenerator = { outerNavActions.navigateToComboGenerator() },
                onNavigateToMoveTagList = { outerNavActions.navigateToMoveTagList() },
                onOpenDrawer = onOpenDrawer
            )
        }

        composable(BreakVaultDestinations.SAVED_COMBOS_ROUTE) {
            SavedComboListScreen(
                onNavigateToAddEditCombo = { outerNavActions.navigateToAddEditCombo(it) },
                onNavigateToComboGenerator = { outerNavActions.navigateToComboGenerator() },
                onOpenDrawer = onOpenDrawer
            )
        }

        composable(BreakVaultDestinations.GOALS_ROUTE) {
            GoalsScreen(
                onNavigateToAddEditGoal = { outerNavActions.navigateToAddEditGoal(it) },
                onNavigateToAddEditStage = { goalId, stageId ->
                    outerNavActions.navigateToAddEditGoalStage(goalId, stageId)
                },
                onOpenDrawer = onOpenDrawer
            )
        }

        composable(BreakVaultDestinations.TIMER_ROUTE) {
            TimerScreen(onOpenDrawer = onOpenDrawer)
        }

        composable(BreakVaultDestinations.BATTLE_ROUTE) {
            BattleComboListScreen(
                onNavigateToAddEditBattleCombo = { outerNavActions.navigateToAddEditBattleCombo(it) },
                onNavigateToBattleTagList = { outerNavActions.navigateToBattleTagListDirect() },
                onOpenDrawer = onOpenDrawer
            )
        }
    }
}

/**
 * Extension function adding overlay screen composables to the outer NavHost.
 * These screens slide over the bottom nav bar as full-screen overlays.
 */
fun NavGraphBuilder.overlayNavGraph(
    navController: NavHostController
) {
    // TAG_LIST_ROUTE - Management screen
    composable(
        route = BreakVaultDestinations.TAG_LIST_ROUTE,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300)
            )
        },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300)
            )
        }
    ) {
        val navActions = remember(navController) {
            BreakVaultNavigationActions(navController)
        }
        MoveTagListScreen(
            onNavigateToMovesByTag = { tagId, tagName ->
                navActions.navigateToMovesByTag(tagId, tagName)
            },
            onNavigateBack = {
                navActions.navigateUp()
            }
        )
    }

    // MOVES_BY_TAG_ROUTE - Management screen
    composable(
        route = BreakVaultDestinations.MOVES_BY_TAG_ROUTE,
        arguments = listOf(
            navArgument(BreakVaultDestinationsArgs.TAG_ID_ARG) { type = NavType.StringType },
            navArgument(BreakVaultDestinationsArgs.TAG_NAME_ARG) { type = NavType.StringType }
        ),
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300)
            )
        }
    ) {
        val navActions = remember(navController) {
            BreakVaultNavigationActions(navController)
        }
        MovesByTagScreen(
            onNavigateUp = { navActions.navigateUp() },
            onNavigateToMove = { navActions.navigateToAddEditMove(it) },
        )
    }

    // ADD_EDIT_MOVE_ROUTE - AddEdit screen
    composable(
        route = BreakVaultDestinations.ADD_EDIT_MOVE_ROUTE,
        arguments = listOf(navArgument(BreakVaultDestinationsArgs.MOVE_ID_ARG) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        }),
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = tween(300)
            )
        }
    ) { backStackEntry ->
        val navActions = remember(navController) {
            BreakVaultNavigationActions(navController)
        }
        val moveId = backStackEntry.arguments?.getString(BreakVaultDestinationsArgs.MOVE_ID_ARG)
        AddEditMoveScreen(
            onNavigateUp = { navActions.navigateUp() },
            moveId = moveId
        )
    }

    // COMBO_GENERATOR_ROUTE - Overlay screen
    composable(
        route = BreakVaultDestinations.COMBO_GENERATOR_ROUTE,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = tween(300)
            )
        }
    ) {
        val navActions = remember(navController) {
            BreakVaultNavigationActions(navController)
        }
        ComboGeneratorScreen(
            onNavigateUp = { navActions.navigateUp() }
        )
    }

    // ADD_EDIT_COMBO_ROUTE - AddEdit screen
    composable(
        route = BreakVaultDestinations.ADD_EDIT_COMBO_ROUTE,
        arguments = listOf(navArgument(BreakVaultDestinationsArgs.COMBO_ID_ARG) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        }),
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = tween(300)
            )
        }
    ) { backStackEntry ->
        val navActions = remember(navController) {
            BreakVaultNavigationActions(navController)
        }
        val comboId = backStackEntry.arguments?.getString(BreakVaultDestinationsArgs.COMBO_ID_ARG)
        AddEditComboScreen(
            onNavigateUp = { navActions.navigateUp() },
            comboId = comboId
        )
    }

    // ADD_EDIT_GOAL_ROUTE - AddEdit screen
    composable(
        route = BreakVaultDestinations.ADD_EDIT_GOAL_ROUTE,
        arguments = listOf(navArgument(BreakVaultDestinationsArgs.GOAL_ID_ARG) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        }),
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = tween(300)
            )
        }
    ) {
        val navActions = remember(navController) {
            BreakVaultNavigationActions(navController)
        }
        AddEditGoalScreen(
            onNavigateUp = { navActions.navigateUp() },
            onNavigateToAddEditStage = { gId, stageId ->
                navActions.navigateToAddEditGoalStage(gId, stageId)
            }
        )
    }

    // ADD_EDIT_GOAL_STAGE_ROUTE - AddEdit screen
    composable(
        route = BreakVaultDestinations.ADD_EDIT_GOAL_STAGE_ROUTE,
        arguments = listOf(
            navArgument(BreakVaultDestinationsArgs.GOAL_ID_ARG) { type = NavType.StringType },
            navArgument(BreakVaultDestinationsArgs.STAGE_ID_ARG) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        ),
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = tween(300)
            )
        }
    ) { backStackEntry ->
        val navActions = remember(navController) {
            BreakVaultNavigationActions(navController)
        }
        val goalId =
            backStackEntry.arguments?.getString(BreakVaultDestinationsArgs.GOAL_ID_ARG) ?: ""
        val stageId = backStackEntry.arguments?.getString(BreakVaultDestinationsArgs.STAGE_ID_ARG)
        AddEditGoalStageScreen(
            onNavigateUp = { navActions.navigateFromNewStageToParentGoal(goalId) },
            goalId = goalId,
            stageId = stageId
        )
    }

    // ARCHIVED_GOALS_ROUTE - Management screen
    composable(
        route = BreakVaultDestinations.ARCHIVED_GOALS_ROUTE,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300)
            )
        }
    ) {
        val navActions = remember(navController) {
            BreakVaultNavigationActions(navController)
        }
        ArchivedGoalsScreen(
            onNavigateUp = { navActions.navigateUp() },
        )
    }

    // ADD_EDIT_BATTLE_COMBO_ROUTE - AddEdit screen
    composable(
        route = BreakVaultDestinations.ADD_EDIT_BATTLE_COMBO_ROUTE,
        arguments = listOf(navArgument(BreakVaultDestinationsArgs.COMBO_ID_ARG) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        }),
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = tween(300)
            )
        }
    ) { backStackEntry ->
        val navActions = remember(navController) {
            BreakVaultNavigationActions(navController)
        }
        val comboId = backStackEntry.arguments?.getString(BreakVaultDestinationsArgs.COMBO_ID_ARG)
        AddEditBattleComboScreen(
            onNavigateUp = { navActions.navigateUp() },
            comboId = comboId
        )
    }

    // BATTLE_TAG_LIST_ROUTE - Management screen
    composable(
        route = BreakVaultDestinations.BATTLE_TAG_LIST_ROUTE,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300)
            )
        }
    ) {
        val navActions = remember(navController) {
            BreakVaultNavigationActions(navController)
        }
        BattleTagListScreen(
            onNavigateUp = { navActions.navigateUp() }
        )
    }

    // SETTINGS_ROUTE - Management screen
    composable(
        route = BreakVaultDestinations.SETTINGS_ROUTE,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300)
            )
        }
    ) {
        val navActions = remember(navController) {
            BreakVaultNavigationActions(navController)
        }
        SettingsScreen(
            onNavigateUp = { navActions.navigateUp() }
        )
    }
}
