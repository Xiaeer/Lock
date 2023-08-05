package com.xiaeer.lock.di

import android.content.Context
import com.xiaeer.lock.data.room.AppDatabase
import com.xiaeer.lock.data.room.dao.LockTaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun provideFreezeTaskerDao(appDatabase: AppDatabase): LockTaskDao {
        return appDatabase.lockTaskDao()
    }
}