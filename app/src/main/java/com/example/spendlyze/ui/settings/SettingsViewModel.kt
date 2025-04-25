package com.example.spendlyze.ui.settings

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendlyze.data.repository.TransactionRepository
import com.example.spendlyze.data.repository.UserRepository
import com.example.spendlyze.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val theme: String = "System Default",
    val currency: String = "LKR",
    val monthlyBudget: Double = 0.0
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val currentUser: User? = userRepository.getCurrentUser()

    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _settingsState.value = SettingsState(
                theme = transactionRepository.getTheme(),
                currency = transactionRepository.getCurrency(),
                monthlyBudget = transactionRepository.getMonthlyBudget()
            )
        }
    }

    fun updateTheme(theme: String) {
        transactionRepository.updateTheme(theme)
        _settingsState.value = _settingsState.value.copy(theme = theme)
        
        // Apply theme immediately
        when (theme) {
            "Dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun updateCurrency(currency: String) {
        transactionRepository.updateCurrency(currency)
        _settingsState.value = _settingsState.value.copy(currency = currency)
    }

    fun updateMonthlyBudget(amount: Double) {
        transactionRepository.updateMonthlyBudget(amount)
        _settingsState.value = _settingsState.value.copy(monthlyBudget = amount)
    }

    fun logout() {
        userRepository.logout()
    }
} 