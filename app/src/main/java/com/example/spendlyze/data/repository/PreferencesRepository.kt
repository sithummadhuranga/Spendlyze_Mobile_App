package com.example.spendlyze.data.repository

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun getMonthlyBudget(): Flow<Double>
    suspend fun setMonthlyBudget(budget: Double)
    fun getCurrency(): Flow<String>
    suspend fun setCurrency(currency: String)
    fun isDarkMode(): Flow<Boolean>
    suspend fun setDarkMode(isDark: Boolean)
    fun getNotificationEnabled(): Flow<Boolean>
    suspend fun setNotificationEnabled(enabled: Boolean)
    fun getNotificationTime(): Flow<Int>
    suspend fun setNotificationTime(hour: Int)
    suspend fun exportTransactions(): String
    suspend fun importTransactions(json: String)
} 