package com.princelumpy.breakvault.data.repository

import androidx.lifecycle.LiveData
import com.princelumpy.breakvault.data.local.dao.SavedComboDao
import com.princelumpy.breakvault.data.local.entity.SavedCombo
import javax.inject.Inject

class SavedComboRepository @Inject constructor(
    private val savedComboDao: SavedComboDao
) {

    fun getSavedCombos(): LiveData<List<SavedCombo>> = savedComboDao.getAllSavedCombosLiveData()

    suspend fun getSavedCombo(id: String): SavedCombo? = savedComboDao.getSavedComboById(id)

    suspend fun insertSavedCombo(savedCombo: SavedCombo) {
        savedComboDao.insertSavedCombo(savedCombo)
    }

    suspend fun updateSavedCombo(savedCombo: SavedCombo) {
        savedComboDao.updateSavedCombo(savedCombo)
    }

    suspend fun deleteSavedCombo(id: String) {
        savedComboDao.deleteSavedComboById(id)
    }
}
