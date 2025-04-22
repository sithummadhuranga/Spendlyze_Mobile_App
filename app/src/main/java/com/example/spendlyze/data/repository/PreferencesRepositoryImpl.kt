package com.example.spendlyze.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PreferencesRepository {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREF_NAME,
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    private val monthlyBudgetFlow = MutableStateFlow(0.0)
    private val currencyFlow = MutableStateFlow("USD")
    private val darkModeFlow = MutableStateFlow(false)
    private val notificationEnabledFlow = MutableStateFlow(true)
    private val notificationTimeFlow = MutableStateFlow(20) // 8 PM default

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        monthlyBudgetFlow.value = sharedPreferences.getFloat(KEY_MONTHLY_BUDGET, 0f).toDouble()
        currencyFlow.value = sharedPreferences.getString(KEY_CURRENCY, "USD") ?: "USD"
        darkModeFlow.value = sharedPreferences.getBoolean(KEY_DARK_MODE, false)
        notificationEnabledFlow.value = sharedPreferences.getBoolean(KEY_NOTIFICATION_ENABLED, true)
        notificationTimeFlow.value = sharedPreferences.getInt(KEY_NOTIFICATION_TIME, 20)
    }

    override fun getMonthlyBudget(): Flow<Double> = monthlyBudgetFlow

    override suspend fun setMonthlyBudget(budget: Double) {
        sharedPreferences.edit().putFloat(KEY_MONTHLY_BUDGET, budget.toFloat()).apply()
        monthlyBudgetFlow.value = budget
    }

    override fun getCurrency(): Flow<String> = currencyFlow

    override suspend fun setCurrency(currency: String) {
        sharedPreferences.edit().putString(KEY_CURRENCY, currency).apply()
        currencyFlow.value = currency
    }

    override fun isDarkMode(): Flow<Boolean> = darkModeFlow

    override suspend fun setDarkMode(isDark: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_DARK_MODE, isDark).apply()
        darkModeFlow.value = isDark
    }

    override fun getNotificationEnabled(): Flow<Boolean> = notificationEnabledFlow

    override suspend fun setNotificationEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply()
        notificationEnabledFlow.value = enabled
    }

    override fun getNotificationTime(): Flow<Int> = notificationTimeFlow

    override suspend fun setNotificationTime(hour: Int) {
        sharedPreferences.edit().putInt(KEY_NOTIFICATION_TIME, hour).apply()
        notificationTimeFlow.value = hour
    }

    override suspend fun exportTransactions(): String {
        val transactionsJson = sharedPreferences.getString(KEY_TRANSACTIONS, "[]")
        return transactionsJson ?: "[]"
    }

    override suspend fun importTransactions(json: String) {
        sharedPreferences.edit().putString(KEY_TRANSACTIONS, json).apply()
    }

    companion object {
        private const val PREF_NAME = "spendlyze_preferences"
        private const val KEY_MONTHLY_BUDGET = "monthly_budget"
        private const val KEY_CURRENCY = "currency"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        private const val KEY_NOTIFICATION_TIME = "notification_time"
        private const val KEY_TRANSACTIONS = "transactions"
    }
} 