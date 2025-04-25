package com.example.spendlyze.data.repository

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getTheme(): String = prefs.getString(KEY_THEME, "light") ?: "light"

    fun setTheme(theme: String) {
        prefs.edit().putString(KEY_THEME, theme).apply()
    }

    fun getCurrency(): String = prefs.getString(KEY_CURRENCY, "USD") ?: "USD"

    fun setCurrency(currency: String) {
        prefs.edit().putString(KEY_CURRENCY, currency).apply()
    }

    companion object {
        private const val PREFS_NAME = "settings_prefs"
        private const val KEY_THEME = "theme"
        private const val KEY_CURRENCY = "currency"
    }
} 