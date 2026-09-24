// Modified by Claude Code - 2026-09-24
package com.princelumpy.breakvault.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sort order for the battle combo list. Persisted by name, so renaming an entry resets
 * users back to the default ([NEWEST]).
 */
enum class BattleSortOption { NEWEST, NAME, COLOR }

private val Context.userPreferencesDataStore by preferencesDataStore(name = "user_prefs")

/**
 * Small user preferences (UI choices that should survive app restarts), backed by DataStore.
 * Not cleared by Settings -> reset database.
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val battleSortOption: Flow<BattleSortOption> =
        context.userPreferencesDataStore.data.map { prefs ->
            prefs[BATTLE_SORT_OPTION]
                ?.let { stored -> BattleSortOption.entries.firstOrNull { it.name == stored } }
                ?: BattleSortOption.NEWEST
        }

    suspend fun setBattleSortOption(option: BattleSortOption) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[BATTLE_SORT_OPTION] = option.name
        }
    }

    private companion object {
        val BATTLE_SORT_OPTION = stringPreferencesKey("battle_sort_option")
    }
}
