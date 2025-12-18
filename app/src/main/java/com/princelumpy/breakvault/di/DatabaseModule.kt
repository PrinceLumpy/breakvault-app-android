package com.princelumpy.breakvault.di

import android.content.Context
import com.princelumpy.breakvault.data.local.database.AppDB
import com.princelumpy.breakvault.data.local.dao.GoalDao
import com.princelumpy.breakvault.data.local.dao.MoveDao
import com.princelumpy.breakvault.data.local.dao.SavedComboDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDB {
        return AppDB.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideMoveDao(appDB: AppDB): MoveDao {
        return appDB.moveDao()
    }

    @Provides
    @Singleton
    fun provideSavedComboDao(appDB: AppDB): SavedComboDao {
        return appDB.savedComboDao()
    }

    @Provides
    @Singleton
    fun provideGoalDao(appDB: AppDB): GoalDao {
        return appDB.goalDao()
    }
}