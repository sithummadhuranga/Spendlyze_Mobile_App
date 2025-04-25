package com.example.spendlyze.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendlyze.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val isDarkMode: Boolean = false,
    val currency: String = "USD",
    val monthlyBudget: Double = 0.0
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val theme = settingsRepository.getTheme()
            val currency = settingsRepository.getCurrency()
            _settingsState.value = SettingsState(
                isDarkMode = theme == "dark",
                currency = currency
            )
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            settingsRepository.setTheme(theme)
            _settingsState.value = _settingsState.value.copy(
                isDarkMode = theme == "dark"
            )
        }
    }

    fun getCurrency(): String = settingsRepository.getCurrency()

    fun setCurrency(currency: String) {
        viewModelScope.launch {
            settingsRepository.setCurrency(currency)
            _settingsState.value = _settingsState.value.copy(
                currency = currency
            )
        }
    }

    fun updateMonthlyBudget(amount: Double) {
        _settingsState.value = _settingsState.value.copy(
            monthlyBudget = amount
        )
    }
} 