package com.princelumpy.breakvault.data.repository

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.princelumpy.breakvault.data.local.database.AppDB
import com.princelumpy.breakvault.data.service.export.model.AppDataExport
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val db: AppDB,
    @ApplicationContext private val context: Context // Injecting the Application Context
) {
    suspend fun getAppDataForExport(): AppDataExport {
        val moveDao = db.moveDao()
        val savedComboDao = db.savedComboDao()
        val battleDao = db.battleDao()
        val goalDao = db.goalDao()

        return AppDataExport(
            moves = moveDao.getAllMoves(),
            moveTags = moveDao.getAllMoveTags(),
            moveTagCrossRefs = moveDao.getAllMoveTagCrossRefs(),
            savedCombos = savedComboDao.getAllSavedCombosList(),
            battleCombos = battleDao.getAllBattleCombos(),
            battleTags = battleDao.getAllBattleTags(),
            battleComboTagCrossRefs = battleDao.getAllBattleComboTagCrossRefs(),
            goals = goalDao.getAllGoals(),
            goalStages = goalDao.getAllGoalStages()
        )
    }

    suspend fun importAppData(appData: AppDataExport) {
        db.withTransaction {
            db.clearAllTables()

            val moveDao = db.moveDao()
            val savedComboDao = db.savedComboDao()
            val battleDao = db.battleDao()
            val goalDao = db.goalDao()

            moveDao.insertAllMoves(appData.moves)
            moveDao.insertAllMoveTags(appData.moveTags)
            moveDao.insertAllMoveTagCrossRefs(appData.moveTagCrossRefs)
            savedComboDao.insertAllSavedCombos(appData.savedCombos)
            battleDao.insertAllBattleCombos(appData.battleCombos)
            battleDao.insertAllBattleTags(appData.battleTags)
            battleDao.insertAllBattleComboTagCrossRefs(appData.battleComboTagCrossRefs)
            goalDao.insertAllGoals(appData.goals)
            goalDao.insertAllGoalStages(appData.goalStages)
        }
    }

    suspend fun resetDatabase() {
        withContext(Dispatchers.IO) {
            db.clearAllTables()
            db.prepopulateExampleData()
        }
    }

    // ADDED: Function to write data to a user-selected file URI.
    fun writeDataToUri(uri: Uri, jsonString: String) {
        context.contentResolver.openFileDescriptor(uri, "w")?.use { parcelFileDescriptor ->
            FileOutputStream(parcelFileDescriptor.fileDescriptor).use { fileOutputStream ->
                fileOutputStream.write(jsonString.toByteArray())
            }
        }
    }

    // ADDED: Function to read data from a user-selected file URI.
    fun readDataFromUri(uri: Uri): String? {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readText()
            }
        }
    }
}
