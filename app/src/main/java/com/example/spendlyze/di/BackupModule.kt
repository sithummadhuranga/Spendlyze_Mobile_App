package com.example.spendlyze.di

import android.content.Context
import com.example.spendlyze.data.repository.TransactionRepository
import com.example.spendlyze.utils.BackupManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BackupModule {

    @Provides
    @Singleton
    fun provideBackupManager(
        @ApplicationContext context: Context,
        repository: TransactionRepository
    ): BackupManager {
        return BackupManager(context, repository)
    }
} 