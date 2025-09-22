package com.princelumpy.breakvault.data // Updated package

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

// Entities and DAOs are now implicitly in the same package, so direct references are fine.

@Database(
    entities = [Move::class, Tag::class, MoveTagCrossRef::class, SavedCombo::class, SavedComboMoveLink::class],
    version = 3 // Version remains 3
)
abstract class AppDB : RoomDatabase() {
    abstract fun moveTagDao(): MoveTagDao
    abstract fun savedComboDao(): SavedComboDao

    companion object {
        @Volatile
        private var INSTANCE: AppDB? = null

        private val PREPOPULATE_TAGS = listOf("Toprock", "Footwork", "Freeze", "Power")

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `saved_combos` (
                        `id` TEXT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `createdAt` INTEGER NOT NULL, 
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `saved_combo_move_link` (
                        `saved_combo_id` TEXT NOT NULL, 
                        `move_id` TEXT NOT NULL, 
                        `order_in_combo` INTEGER NOT NULL, 
                        PRIMARY KEY(`saved_combo_id`, `order_in_combo`),
                        FOREIGN KEY(`saved_combo_id`) REFERENCES `saved_combos`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, 
                        FOREIGN KEY(`move_id`) REFERENCES `moves`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_saved_combo_move_link_saved_combo_id` ON `saved_combo_move_link` (`saved_combo_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_saved_combo_move_link_move_id` ON `saved_combo_move_link` (`move_id`)")
            }
        }

        // Empty migration from version 2 to 3, assuming no schema changes
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // No schema changes from version 2 to 3, so this is empty.
                // This migration path ensures users on v2 don't lose data due to fallbackToDestructiveMigration.
                Log.i("AppDB", "Executing empty migration from version 2 to 3.")
            }
        }

        fun getDatabase(context: Context): AppDB {
            val appContext = context.applicationContext
                ?: throw IllegalStateException("Application context cannot be null when getting database.")

            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    appContext,
                    AppDB::class.java,
                    "combo_generator_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // Added MIGRATION_2_3
                    .fallbackToDestructiveMigration() // Retained as a safeguard for future unhandled migrations
                    .addCallback(AppDbCallback(appContext))
                    .build()
                    .also { INSTANCE = it }
            }
        }

        suspend fun prepopulateDefaultTags(database: AppDB) {
            val dao = database.moveTagDao()
            val existingTags = dao.getAllTagsList().map { it.name }.toSet()
            PREPOPULATE_TAGS.forEach { tagName ->
                if (!existingTags.contains(tagName)) {
                    dao.addTag(Tag(id = UUID.randomUUID().toString(), name = tagName))
                }
            }
            Log.i("AppDB", "Pre-populated default tags (if any were missing): $PREPOPULATE_TAGS")
        }
    }

    private class AppDbCallback(private val applicationContext: Context) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    prepopulateDefaultTags(it)
                }
            } ?: Log.e("AppDbCallback", "INSTANCE was null during onCreate, cannot prepopulate tags.")
        }
    }
}
