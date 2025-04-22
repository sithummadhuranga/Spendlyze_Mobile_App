package com.example.spendlyze.di

import android.content.Context
import com.example.spendlyze.data.repository.PreferencesRepository
import com.example.spendlyze.data.repository.PreferencesRepositoryImpl
import com.example.spendlyze.data.repository.TransactionRepository
import com.example.spendlyze.data.repository.TransactionRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTransactionRepository(
        @ApplicationContext context: Context
    ): TransactionRepository {
        return TransactionRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun providePreferencesRepository(
        @ApplicationContext context: Context
    ): PreferencesRepository {
        return PreferencesRepositoryImpl(context)
    }
} 