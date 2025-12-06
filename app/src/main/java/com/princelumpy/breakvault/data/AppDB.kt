package com.princelumpy.breakvault.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        Move::class,
        MoveTag::class,
        MoveTagCrossRef::class,
        SavedCombo::class,
        BattleCombo::class,
        BattleTag::class,
        BattleComboTagCrossRef::class,
        Goal::class,
        GoalStage::class
    ],
    version = 1
)

@TypeConverters(Converters::class)
abstract class AppDB : RoomDatabase() {
    abstract fun moveTagDao(): MoveTagDao
    abstract fun savedComboDao(): SavedComboDao
    abstract fun battleComboDao(): BattleComboDao
    abstract fun battleTagDao(): BattleTagDao
    abstract fun goalDao(): GoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDB? = null

        fun getDatabase(context: Context): AppDB {
            val appContext = context.applicationContext
                ?: throw IllegalStateException("Application context cannot be null when getting database.")

            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    appContext,
                    AppDB::class.java,
                    "break_vault_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(AppDbCallback(appContext))
                    .build()
                    .also { INSTANCE = it }
            }
        }

        suspend fun prepopulateExampleData(database: AppDB) {
            val moveTagDao = database.moveTagDao()
            val savedComboDao = database.savedComboDao()
            val battleDao = database.battleComboDao()
            val battleTagDao = database.battleTagDao()

            // --- 1. Tags ---
            val tagsToEnsure = listOf("Toprock", "Footwork", "Freeze", "Power")
            val tagMap = mutableMapOf<String, String>() // Name -> ID
            tagsToEnsure.forEach { name ->
                val newMoveListTag = MoveTag(id = UUID.randomUUID().toString(), name = name)
                moveTagDao.addTag(newMoveListTag)
                tagMap[name] = newMoveListTag.id
            }

            // Only prepopulate the following data if developing, not in production
            if (com.princelumpy.breakvault.BuildConfig.DEBUG) {
                // --- 2. Moves ---
                val movesData = listOf(
                    Pair("6-Step", "Footwork"),
                    Pair("CC", "Footwork"),
                    Pair("Windmill", "Power"),
                    Pair("Baby Freeze", "Freeze"),
                    Pair("Toprock Basic", "Toprock"),
                    Pair("Backspin", "Power")
                )
                movesData.forEach { (moveName, tagName) ->
                    val realMoveId = UUID.randomUUID().toString()
                    moveTagDao.addMove(Move(id = realMoveId, name = moveName))
                    tagMap[tagName]?.let { tagId ->
                        moveTagDao.link(MoveTagCrossRef(moveId = realMoveId, tagId = tagId))
                    }
                }
                Log.i("AppDB", "Populated 4 move tags and 6 example moves.")

                // --- 3. Saved Combos ---
                val savedCombosData = listOf(
                    Pair("Classic Footwork", listOf("6-Step", "CC")),
                    Pair("Power Finisher", listOf("Windmill", "Baby Freeze")),
                    Pair("Top to Down", listOf("Toprock Basic", "Backspin"))
                )

                savedCombosData.forEach { (name, moves) ->
                    savedComboDao.insertSavedCombo(SavedCombo(name = name, moves = moves))
                }
                Log.i("AppDB", "Populated 3 saved combos.")

                // --- 4. Battle Tags ---
                val battleTagsToEnsure = listOf("Power", "Technique")
                val battleTagMap = mutableMapOf<String, String>()
                battleTagsToEnsure.forEach { name ->
                    val newTag = BattleTag(id = UUID.randomUUID().toString(), name = name)
                    battleTagDao.insertBattleTag(newTag)
                    battleTagMap[name] = newTag.id
                }

                // --- 5. Battle Combos ---
                val battleCombosData = listOf(
                    Pair(
                        BattleCombo(
                            id = UUID.randomUUID().toString(),
                            description = "Windmill -> Backspin -> Freeze",
                            energy = EnergyLevel.HIGH,
                            status = TrainingStatus.READY
                        ),
                        "Power"
                    ),
                    Pair(
                        BattleCombo(
                            id = UUID.randomUUID().toString(),
                            description = "Smooth transitions to CC",
                            energy = EnergyLevel.MEDIUM,
                            status = TrainingStatus.READY
                        ),
                        "Technique"
                    ),
                    Pair(
                        BattleCombo(
                            id = UUID.randomUUID().toString(),
                            description = "Aggressive Toprock to Drop",
                            energy = EnergyLevel.HIGH,
                            status = TrainingStatus.TRAINING
                        ),
                        "Technique"
                    ),
                    Pair(
                        BattleCombo(
                            id = UUID.randomUUID().toString(),
                            description = "Slow intro to floor",
                            energy = EnergyLevel.LOW,
                            status = TrainingStatus.READY
                        ),
                        "Technique"
                    )
                )

                battleCombosData.forEach { (combo, tagName) ->
                    battleDao.insertBattleCombo(combo)
                    // Link Tags
                    battleTagMap[tagName]?.let { tagId ->
                        battleDao.link(
                            BattleComboTagCrossRef(
                                battleComboId = combo.id,
                                battleTagId = tagId
                            )
                        )
                    }
                }
                Log.i("AppDB", "Populated 2 battle tags and 4 battle combos.")
            }
        }
    }

    private class AppDbCallback(private val applicationContext: Context) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    prepopulateExampleData(it)
                }
            } ?: Log.e(
                "AppDbCallback",
                "INSTANCE was null during onCreate, cannot prepopulate moveListTags."
            )
        }
    }
}
