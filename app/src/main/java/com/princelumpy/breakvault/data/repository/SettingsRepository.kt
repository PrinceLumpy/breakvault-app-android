package com.princelumpy.breakvault.data.repository

import androidx.room.withTransaction
import com.princelumpy.breakvault.data.local.database.AppDB
import com.princelumpy.breakvault.data.service.export.model.AppDataExport
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val db: AppDB
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
        db.clearAllTables()
    }
}
